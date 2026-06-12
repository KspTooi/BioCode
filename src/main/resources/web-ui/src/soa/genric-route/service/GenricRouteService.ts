import { createRouter, createWebHashHistory, type RouteRecordRaw, type Router } from "vue-router";
import { RouteEntryPo, type RouteEntryWithConflict } from "@/soa/genric-route/api/RouteEntryPo";
import { type App, createApp, h, ref } from "vue";
import GenricRouteRegister from "@/soa/genric-route/service/GenricRouteRegister";
import ComTabService from "@/soa/com-series/service/ComTabService";
import GrConflictOverlay from "@/soa/genric-route/component/GrConflictOverlay.vue";

/**
 * 全局路由服务(GRS)
 * GRS是整个项目的路由服务，负责管理整个项目的路由，包括路由的注册、删除、获取等。它是整个项目中路由的单一事实来源。
 * GRS的注册方式是通过注册器(GRR)来注册的，注册器是一个抽象类，需要子类来实现，在子类里面可以注册多条路由、配置前置守卫和后置守卫。
 *
 * 注意：你不可以绕过GRS直接操作Vue路由，这会破坏整个项目的路由一致性，也会破坏路由表和GRS的注册机制，严重时会导致项目无法正常运行。
 *
 */

//是否已初始化
let hasInitialized = false;

//路由表
const routes = ref<RouteEntryPo[]>([]);

//路由注册器
const routeRegistries = ref<GenricRouteRegister[]>([]);

//路由冲突
const conflicts = ref<RouteEntryWithConflict[]>([]);

/**
 * Vue路由
 */
const vueRouter = createRouter({
  history: createWebHashHistory(),
  routes: [],
});

//保存原始的写方法 GRS内部走这两个引用
const rawAddRoute = vueRouter.addRoute.bind(vueRouter);
const rawRemoveRoute = vueRouter.removeRoute.bind(vueRouter);

//防止外部绕过GRS直接操作Vue路由,这会导致路由管理混乱出现不同步问题。
const buildForbiddenFn = (methodName: string) => {
  return () => {
    throw new Error(`Vue路由管理器不支持直接调用, router.${methodName} 请通过 GenricRouteService(GRS) 进行路由管理`);
  };
};

Object.defineProperty(vueRouter, "addRoute", {
  value: buildForbiddenFn("addRoute"),
  writable: false,
  configurable: false,
});

Object.defineProperty(vueRouter, "removeRoute", {
  value: buildForbiddenFn("removeRoute"),
  writable: false,
  configurable: false,
});

// 路由守卫
vueRouter.beforeEach((to, from) => {
  // 仅在访问根路径时尝试恢复标签页，其他路径直接放行
  if (to.path !== "/") {
    return;
  }

  const { tabs, getActiveTab } = ComTabService.useTabService();

  //获取当前激活标签
  const activeTab = getActiveTab();

  // 优先恢复当前激活标签，但排除根路径和登录页，避免自跳转/无意义跳转
  if (activeTab && activeTab.path !== "/" && activeTab.path !== "/auth/login" && activeTab.path !== to.path) {
    return activeTab.path;
  }

  // 激活标签不可用时，回退到最近访问的业务标签（同样排除根路径和登录页）
  const fallbackTab = [...tabs.value].reverse().find((t) => t.path !== "/" && t.path !== "/auth/login");

  // 防止重定向到当前目标，避免产生循环跳转
  if (fallbackTab && fallbackTab.path !== to.path) {
    return fallbackTab.path;
  }

  //如果没有标签可用 直接跳转到首页
  return "/index";
});

/**
 * 获取路由冲突
 * 冲突规则：
 * 1. 同一个 biz 出现在两个不同的注册器中，第二个注册器中的所有路由均视为冲突
 * 2. buildPath() 全局唯一，重复出现即冲突
 * @param gr 路由注册器
 * @returns 路由冲突
 */
const getConflicts = (gr: GenricRouteRegister[]): RouteEntryWithConflict[] => {
  const result: RouteEntryWithConflict[] = [];

  // 记录已出现的 biz -> 首个注册器索引
  const bizOwnerIndex = new Map<string, number>();
  // 记录已出现的 buildPath()
  const pathSet = new Set<string>();

  gr.forEach((register, registerIndex) => {
    const entries = register.doRegister();

    // 收集当前注册器所有 biz（一个注册器内可能有多个不同 biz）
    const bizInThisRegister = new Set<string>();

    entries.forEach((entry) => {
      // 先 validate 以保证 name/meta 等字段完整
      entry.validate();

      const builtPath = entry.path;
      const reasons: string[] = [];

      // biz 冲突检测：同一 biz 首次出现记录其注册器索引，第二个注册器起算冲突
      if (entry.biz != null && !bizInThisRegister.has(entry.biz)) {
        bizInThisRegister.add(entry.biz);
        if (!bizOwnerIndex.has(entry.biz)) {
          bizOwnerIndex.set(entry.biz, registerIndex);
        }
        if (bizOwnerIndex.has(entry.biz) && bizOwnerIndex.get(entry.biz) !== registerIndex) {
          reasons.push(`域 "${entry.biz}" 已被其他注册器占用`);
        }
      }

      // path 冲突检测：buildPath() 全局唯一
      if (pathSet.has(builtPath)) {
        reasons.push(`路径 "${builtPath}" 已被其他路由占用`);
      }
      if (!pathSet.has(builtPath)) {
        pathSet.add(builtPath);
      }

      if (reasons.length === 0) {
        return;
      }

      const conflictEntry = new RouteEntryPo();
      conflictEntry.biz = entry.biz;
      conflictEntry.path = entry.path;
      conflictEntry.name = entry.name;
      conflictEntry.component = entry.component;
      conflictEntry.meta = entry.meta;

      result.push(
        Object.assign(conflictEntry, {
          componentPath: builtPath,
          reason: reasons.join("；"),
        }) as RouteEntryWithConflict
      );
    });
  });

  return result;
};

export default {
  /**
   * 获取Vue路由
   * @returns Vue路由
   */
  getVueRouter(): Router {
    return vueRouter;
  },

  /**
   * 使用全局路由服务
   */
  useGenricRoute() {
    /**
     * 初始化路由服务
     * @param app 应用实例
     * @param fixedRoutes 固定路由（由入口在初始化时注入，如首页、404、401、外链等）
     */
    const initialize = (app: App, fixedRoutes: RouteRecordRaw[]): void => {
      if (hasInitialized) {
        return;
      }

      hasInitialized = true;

      fixedRoutes.forEach((route) => {
        rawAddRoute(route);
      });

      //检测路由冲突（在注册前检测，避免冲突路由污染 Vue 路由）
      conflicts.value = getConflicts(routeRegistries.value);

      //注册路由注册器
      routeRegistries.value.forEach((register: GenricRouteRegister) => {
        //注册路由
        addRoutes(register.doRegister());

        //注册前置守卫
        const beforeEachGuard = register.doBeforeEach();
        if (beforeEachGuard) {
          vueRouter.beforeEach(beforeEachGuard);
        }

        //注册后置守卫
        const afterEachGuard = register.doAfterEach();
        if (afterEachGuard) {
          vueRouter.afterEach(afterEachGuard);
        }
      });

      //检测是否有路由冲突
      if (conflicts.value.length > 0) {
        const container = document.createElement("div");
        container.id = "grs-conflict-overlay-root";
        document.body.appendChild(container);

        const conflictOverlayApp = createApp({
          render: () =>
            h(GrConflictOverlay, {
              conflicts: conflicts.value,
            }),
        });

        conflictOverlayApp.mount(container);
      }

      app.use(vueRouter);
    };

    /**
     * 添加路由
     * @param entry 路由条目或路由注册器
     */
    const addRoute = (entry: RouteEntryPo | GenricRouteRegister): void => {
      //如果是路由注册器 则注册到路由注册器列表 初始化时会自动注册路由
      if (entry instanceof GenricRouteRegister) {
        routeRegistries.value.push(entry);
        return;
      }

      //校验路由条目
      entry.validate();

      let hasConflict = false;

      for (const route of routes.value) {
        //查找同名路由
        if (route.name === entry.name) {
          //删除Vue路由
          rawRemoveRoute(route.name);

          //更新路由条目
          route.biz = entry.biz;
          route.path = entry.path;
          route.name = entry.name;
          route.component = entry.component;
          route.meta = entry.meta;
          hasConflict = true;
          break;
        }
      }

      const breadcrumbTitle = entry.meta.breadcrumb;

      //如果无冲突 同时更新路由表+Vue路由
      if (!hasConflict) {
        //更新路由表
        routes.value.push(entry);

        //添加Vue路由
        rawAddRoute({
          path: entry.buildPath(),
          name: entry.name,
          component: entry.component,
          meta: {
            keepAlive: entry.meta.keepAlive,
            breadcrumb: breadcrumbTitle,
            layout: entry.meta.layout,
          },
        });
      }

      //路由有名称冲突 只更新Vue路由,不更新内置路由表
      if (hasConflict) {
        rawAddRoute({
          path: entry.buildPath(),
          name: entry.name,
          component: entry.component,
          meta: {
            keepAlive: entry.meta.keepAlive,
            breadcrumb: breadcrumbTitle,
            layout: entry.meta.layout,
          },
        });
      }
    };

    /**
     * 批量注册路由
     * @param entries 路由条目数组
     */
    const addRoutes = (entries: RouteEntryPo[]): void => {
      entries.forEach((entry) => addRoute(entry));
    };

    /**
     * 删除路由
     * @param name 路由名称
     */
    const removeRoute = (name: string): void => {
      rawRemoveRoute(name);
      routes.value = routes.value.filter((route) => route.name !== name);
    };

    /**
     * 获取路由表
     * @returns 路由表的副本 对副本操作不会改变原始路由表
     */
    const getRoutes = (): RouteEntryPo[] => {
      const result: RouteEntryPo[] = [];

      //遍历路由表
      for (const item of routes.value) {
        const po = new RouteEntryPo();
        po.biz = item.biz;
        po.path = item.path;
        po.name = item.name;
        po.component = item.component;
        po.meta = item.meta;
        result.push(po);
      }

      return result;
    };

    /**
     * 根据名称或路径获取路由
     * 优先匹配名称 其次匹配路径，只要有任意一个匹配成功就返回
     *
     * 请注意: 该方法返回的是路由PO对象 而不是Vue路由对象，该项目中的所有Vue路由都需要通过GRS进行注册，(调用GRS的addRoute方法)
     * 如果你绕过GRS直接操作Vue路由，将无法保证路由的正确性。
     *
     * @param nameOrPath 路由名称或路径
     * @returns 路由条目 如果无对应路由则返回null
     */
    const getRouteByNameOrPath = (nameOrPath: string): RouteEntryPo | null => {
      //先根据名称查找
      const routeByName = routes.value.find((route) => route.name === nameOrPath);
      if (routeByName) {
        return routeByName;
      }

      //再根据路径查找
      const routeByPath = routes.value.find((route) => route.path === nameOrPath);
      if (routeByPath) {
        return routeByPath;
      }

      //无匹配路由
      return null;
    };

    return {
      initialize,
      addRoute,
      addRoutes,
      removeRoute,
      getRoutes,
      getRouteByNameOrPath,
    };
  },
};
