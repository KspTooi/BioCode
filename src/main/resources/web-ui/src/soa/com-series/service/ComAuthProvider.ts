import { computed, h, markRaw, reactive, ref, type Component } from "vue";

const DEFAULT_AUTH_KEY = "default";

const NoDefaultAuth = markRaw({
  name: "ComNoDefaultAuth",
  render: () =>
    h(
      "div",
      {
        style: {
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          height: "100%",
          width: "100%",
          color: "var(--el-color-danger)",
          fontSize: "14px",
          padding: "24px",
          textAlign: "center",
        },
      },
      '未注册认证组件：请先调用 registerAuth 注册，再调用 setDefaultAuth(name) 或注册为 "default" 键'
    ),
} as Component);

/**
 * 认证组件记录
 */
const auths = reactive<Record<string, Component>>({});

/**
 * 当前默认认证组件名称
 */
const defaultAuthName = ref<string | null>(null);

export default {
  /**
   * 注册或替换认证组件
   */
  registerAuth(name: string, component: Component): void {
    auths[name] = markRaw(component);
  },

  /**
   * 获取已注册的认证组件
   */
  getAuth(name: string): Component | undefined {
    return auths[name];
  },

  /**
   * 判断认证组件是否已注册
   */
  hasAuth(name: string): boolean {
    return name in auths;
  },

  /**
   * 设置默认认证组件
   */
  setDefaultAuth(name: string): void {
    if (!auths[name]) {
      throw new Error(`认证组件「${name}」未注册，请先调用 registerAuth`);
    }
    defaultAuthName.value = name;
  },

  /**
   * 提供当前生效认证组件的组合式函数
   */
  useAuthComponent() {
    const authComponent = computed<Component>(() => {
      const key = defaultAuthName.value || DEFAULT_AUTH_KEY;
      const component = auths[key];
      if (component) {
        return component;
      }
      return NoDefaultAuth;
    });

    return { authComponent };
  },
};
