import { computed, h, markRaw, reactive, ref, type Component } from "vue";
import { RouterView, useRoute } from "vue-router";

const DEFAULT_LAYOUT_KEY = "default";

const BlankLayout = markRaw({
  name: "ComBlankLayout",
  render: () => h(RouterView),
} as Component);

const NoDefaultLayout = markRaw({
  name: "ComNoDefaultLayout",
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
      '未注册默认布局：请先 registerLayout 注册布局，再调用 setDefaultLayout(name) 或 registerLayout("default", YourLayoutComponent)'
    ),
} as Component);

/**
 * 布局组件记录
 */
const layouts = reactive<Record<string, Component>>({
  blank: BlankLayout,
});

/**
 * 当前默认布局名称（未设置时回退到 default 键）
 */
const defaultLayoutName = ref<string | null>(null);

export default {
  /**
   * 注册或替换布局组件
   */
  registerLayout(name: string, component: Component): void {
    layouts[name] = markRaw(component);
  },

  /**
   * 获取已注册布局组件
   */
  getLayout(name: string): Component | undefined {
    return layouts[name];
  },

  /**
   * 判断布局是否已注册
   */
  hasLayout(name: string): boolean {
    return name in layouts;
  },

  /**
   * 设置默认布局（须已通过 registerLayout 注册）
   */
  setDefaultLayout(name: string): void {
    if (!layouts[name]) {
      throw new Error(`布局「${name}」未注册，请先调用 registerLayout`);
    }
    defaultLayoutName.value = name;
  },

  /**
   * 根据当前路由 meta.layout 解析布局组件
   */
  useFrameworkLayout() {
    const route = useRoute();

    const resolveDefaultLayout = (): Component | undefined => {
      const key = defaultLayoutName.value || DEFAULT_LAYOUT_KEY;
      return layouts[key];
    };

    /**
     * 当前布局组件
     */
    const currentLayout = computed<Component>(() => {
      const routeLayoutKey = route.meta?.layout as string | undefined;

      // meta.layout 为 "default" 表示使用当前默认布局（setDefaultLayout），非 layouts["default"] 键
      if (routeLayoutKey && routeLayoutKey !== DEFAULT_LAYOUT_KEY) {
        const layout = layouts[routeLayoutKey];
        if (layout) {
          return layout;
        }
      }

      const defaultLayout = resolveDefaultLayout();
      if (defaultLayout) {
        return defaultLayout;
      }

      return NoDefaultLayout;
    });

    return { currentLayout };
  },
};
