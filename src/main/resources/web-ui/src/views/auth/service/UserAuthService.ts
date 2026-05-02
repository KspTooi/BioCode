import { defineStore } from "pinia";
import type { Directive, DirectiveBinding } from "vue";
import type { UserLoginDto, UserLoginVo } from "@/views/auth/api/AuthApi";
import AuthApi from "@/views/auth/api/AuthApi";

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
    const login = async (username: string, password: string): Promise<UserLoginVo> => {
      const dto = {
        username,
        password,
      } as UserLoginDto;

      const result = await AuthApi.userLogin(dto);

      if (result.code === 0 && result.data) {
        AuthStore().setUserInfo(result.data);
        AuthStore().setSessionId(result.data.sessionId);
        return result.data;
      }

      throw new Error(result.message);
    };

    return {
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
