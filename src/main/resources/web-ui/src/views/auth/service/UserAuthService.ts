import { defineStore } from "pinia";
import type { Directive, DirectiveBinding } from "vue";
import type { UserLoginDto, UserLoginVo, GetLoginConfigVo } from "@/views/auth/api/AuthApi";
import AuthApi from "@/views/auth/api/AuthApi";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";
import { chacha20poly1305 } from "@noble/ciphers/chacha.js";

//超级操作权限码
export const SA_CODE = "*:*:*";

//超级数据权限码
export const SR_CODE = "*:*:*:*";

// ChaCha20-Poly1305 预共享密钥（32字节 Base64，前后端必须一致）
const PSK_BASE64 = "BJdWywEjfzLqfEUaYsPQClCI8bvOnsVwd48HM5jdfak=";

const REMEMBERED_USERNAME = "rememberedUsername";
const REMEMBERED_PASSWORD = "rememberedPassword";

/**
 * ChaCha20-Poly1305 加密，IV 拼入密文末尾（格式：密文Base64:IV-Base64）
 *
 * @param plaintext 明文
 * @returns 密文:IV
 */
const encrypt = (plaintext: string): string => {
  const encoder = new TextEncoder();

  const pskBytes = Uint8Array.from(atob(PSK_BASE64), (c) => c.charCodeAt(0));
  const ivBytes = crypto.getRandomValues(new Uint8Array(12));

  const cipher = chacha20poly1305(pskBytes, ivBytes);
  const ctBytes = cipher.encrypt(encoder.encode(plaintext));

  const ctB64 = btoa(String.fromCharCode(...ctBytes));
  const ivB64 = btoa(String.fromCharCode(...ivBytes));
  return `${ctB64}:${ivB64}`;
};

/**
 * ChaCha20-Poly1305 解密（格式：密文Base64:IV-Base64）
 *
 * @param ctIv 密文:IV
 * @returns 明文
 */
const decrypt = (ctIv: string): string => {
  const decoder = new TextDecoder();
  const sep = ctIv.lastIndexOf(":");
  const ctB64 = ctIv.substring(0, sep);
  const ivB64 = ctIv.substring(sep + 1);

  const pskBytes = Uint8Array.from(atob(PSK_BASE64), (c) => c.charCodeAt(0));
  const ivBytes = Uint8Array.from(atob(ivB64), (c) => c.charCodeAt(0));
  const ctBytes = Uint8Array.from(atob(ctB64), (c) => c.charCodeAt(0));

  const cipher = chacha20poly1305(pskBytes, ivBytes);
  const ptBytes = cipher.decrypt(ctBytes);
  return decoder.decode(ptBytes);
};

const AuthStore = defineStore("AuthStore", {
  state: () => {
    const userInfo = localStorage.getItem("userInfo");
    const sessionId = localStorage.getItem("sessionId");

    return {
      userInfo: userInfo ? (JSON.parse(userInfo) as UserLoginVo) : null,
      sessionId: sessionId,
    };
  },
  getters: {
    getUserInfo: (state) => {
      return state.userInfo;
    },
    getSessionId: (state) => {
      return state.sessionId;
    },
  },
  actions: {
    setUserInfo(userInfo: UserLoginVo | null) {
      this.userInfo = userInfo;

      if (!userInfo) {
        localStorage.removeItem("userInfo");
        return;
      }

      localStorage.setItem("userInfo", JSON.stringify(userInfo));
    },
    setSessionId(sessionId: string | null) {
      this.sessionId = sessionId;

      if (!sessionId) {
        localStorage.removeItem("sessionId");
        return;
      }

      localStorage.setItem("sessionId", sessionId);
    },

    clearAuth() {
      localStorage.removeItem("userInfo");
      localStorage.removeItem("sessionId");
      this.userInfo = null;
      this.sessionId = null;
    },
  },
});

export default {
  AuthStore,
  /**
   * 用户认证服务
   */
  useUserAuth() {
    /**
     * 获取登录配置（验证码开关、密码策略等）
     */
    const getLoginConfig = async (): Promise<GetLoginConfigVo> => {
      return await AuthApi.getLoginConfig();
    };

    /**
     * 保存账号（明文，内部加密后存 localStorage）
     *
     * @param username 登录账号
     * @param password 密码
     */
    const saveAccount = (username: string, password: string): void => {
      localStorage.setItem(REMEMBERED_USERNAME, encrypt(username));
      localStorage.setItem(REMEMBERED_PASSWORD, encrypt(password));
    };

    /**
     * 清除保存的账号
     */
    const clearAccount = (): void => {
      localStorage.removeItem(REMEMBERED_USERNAME);
      localStorage.removeItem(REMEMBERED_PASSWORD);
    };

    /**
     * 加载保存的账号（解密为明文）
     *
     * @returns { username, password } | null
     */
    const loadAccount = (): { username: string; password: string } | null => {
      const encUsername = localStorage.getItem(REMEMBERED_USERNAME);
      const encPassword = localStorage.getItem(REMEMBERED_PASSWORD);
      if (!encUsername || !encPassword) {
        return null;
      }
      return {
        username: decrypt(encUsername),
        password: decrypt(encPassword),
      };
    };

    const login = async (username: string, password: string): Promise<UserLoginVo> => {
      const dto = {
        username: encrypt(username),
        password: encrypt(password),
      } as UserLoginDto;

      const result = await AuthApi.userLogin(dto);

      if (result.code === 0 && result.data) {
        AuthStore().setUserInfo(result.data);
        AuthStore().setSessionId(result.data.sessionId);

        //清除用户多标签缓存
        ComTabService.useTabService().clearTabs();
        return result.data;
      }

      throw new Error(result.message);
    };

    return {
      getLoginConfig,
      saveAccount,
      clearAccount,
      loadAccount,
      login,
    };
  },

  /**
   * 使用按钮检查权限
   */
  usePreAuthorize() {
    // 检查用户是否拥有指定权限码（单个或多个，满足其一即可）
    const hasCode = (codes: string | string[]): boolean => {
      //如果用户有超级权限 则直接返回true
      if (AuthStore().userInfo?.authorities?.includes("*:*:*")) {
        return true;
      }

      const authorities = AuthStore().userInfo?.authorities ?? [];
      const codeList = Array.isArray(codes) ? codes : [codes];
      return codeList.some((code) => authorities.includes(code));
    };

    // 检查当前用户是否拥有超级权限
    const hasSuper = (): boolean => {
      return AuthStore().userInfo?.authorities?.includes("*:*:*") ?? false;
    };

    // v-hasCode 自定义指令，无权限时隐藏元素
    const vHasCode: Directive = {
      mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
        if (!hasCode(binding.value)) {
          el.style.display = "none";
        }
      },
      updated(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
        if (!hasCode(binding.value)) {
          el.style.display = "none";
          return;
        }
        el.style.display = "";
      },
    };

    // v-hasSuper 自定义指令，非超级权限时隐藏元素
    const vHasSuper: Directive = {
      mounted(el: HTMLElement) {
        if (!hasSuper()) {
          el.style.display = "none";
        }
      },
      updated(el: HTMLElement) {
        if (!hasSuper()) {
          el.style.display = "none";
          return;
        }
        el.style.display = "";
      },
    };

    return { hasCode, vHasCode, hasSuper, vHasSuper };
  },
};
