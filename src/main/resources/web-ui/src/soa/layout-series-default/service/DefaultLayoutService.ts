import { computed, ref } from "vue";
import GenricHotkeyService from "@/commons/service/GenricHotkeyService.ts";
import { useRoute, useRouter } from "vue-router";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";

export default {
  /**
   * 框架快捷键服务
   */
  useComTabHotkey() {
    //获取标签页服务
    const { activeOf } = ComTabService.useTabService();

    /**
     * 初始化快捷键
     */
    GenricHotkeyService.useHotkeyFunction(
      {
        ctrl_1: () => activeOf(1),
        ctrl_2: () => activeOf(2),
        ctrl_3: () => activeOf(3),
        ctrl_4: () => activeOf(4),
        ctrl_5: () => activeOf(5),
        ctrl_6: () => activeOf(6),
        ctrl_7: () => activeOf(7),
        ctrl_8: () => activeOf(8),
        ctrl_9: () => activeOf(9),
      },
      ref(true)
    );
  },

  /**
   * 框架布局服务
   */
  useComFramework() {
    const router = useRouter();
    const route = useRoute();

    /**
     * 自动生成面包屑导航
     */
    const autoBreadcrumbs = computed(() => {
      const revisedBreadcrumbs: Array<{ text: string; to?: string | object }> = [];

      // 尝试添加首页/根路径面板屑
      let homeTitle = "控制台";
      const homeRouteConfig = router.options.routes.find((r) => r.path === "/");
      const homeBreadcrumbMeta = homeRouteConfig?.meta?.breadcrumb as any;

      if (homeBreadcrumbMeta) {
        if (typeof homeBreadcrumbMeta === "string") {
          homeTitle = homeBreadcrumbMeta;
        }
        if (typeof homeBreadcrumbMeta === "object" && homeBreadcrumbMeta.title) {
          homeTitle = homeBreadcrumbMeta.title;
        }
      }

      // 如果当前不是根路径，则添加控制台首页
      if (route.path !== "/") {
        revisedBreadcrumbs.push({
          text: homeTitle,
          to: "/",
        });
      }

      // 遍历匹配的路由记录
      route.matched.forEach((record, index): void => {
        // 如果已经添加了首页，并且当前记录是根路径，则跳过
        if (record.path === "/" && revisedBreadcrumbs.length > 0 && revisedBreadcrumbs[0].to === "/") {
          return;
        }

        const meta = record.meta;
        let title = "";
        let hidden = false;
        const path = record.path; // 使用匹配路由的路径

        // 处理 meta.breadcrumb (支持字符串和对象)
        const breadcrumbMeta = meta?.breadcrumb as any;

        if (typeof breadcrumbMeta === "string") {
          title = breadcrumbMeta;
        }

        //处理 meta.breadcrumb 对象
        if (breadcrumbMeta && typeof breadcrumbMeta === "object") {
          if (breadcrumbMeta.title) {
            title = breadcrumbMeta.title;
          }
          hidden = breadcrumbMeta.hidden === true;
        }

        // 如果 breadcrumb 中没有 title，尝试使用 meta.title
        if (!title && meta?.title && typeof meta.title === "string") {
          title = meta.title;
        }

        // 如果有标题且不隐藏，则添加到面包屑数组中
        if (title && !hidden) {
          // 检查是否已存在（基于路径）
          const alreadyExists = revisedBreadcrumbs.some((b) => b.to === path);
          if (!alreadyExists) {
            revisedBreadcrumbs.push({
              text: title,
              // 最后一个面包屑（当前页面）不设置链接
              to: index === route.matched.length - 1 ? undefined : path,
            });
          }
        }
      });

      return revisedBreadcrumbs;
    });

    /**
     * 菜单折叠状态
     */
    const isMenuCollapse = ref(localStorage.getItem("isMenuCollapse") !== "false");

    /**
     * 切换菜单状态
     */
    const toggleMenu = (): void => {
      isMenuCollapse.value = !isMenuCollapse.value;
      localStorage.setItem("isMenuCollapse", isMenuCollapse.value.toString());
    };

    return {
      autoBreadcrumbs,
      isMenuCollapse,
      toggleMenu,
    };
  },
};
