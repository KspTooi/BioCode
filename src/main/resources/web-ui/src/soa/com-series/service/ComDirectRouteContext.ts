/**
 * CDRC(Com Direct Route Context) 直接路由上下文系统
 *
 * 一、用途
 * 用于在两个页面之间进行“带上下文的直接跳转”。
 * 它解决的不是普通路由跳转，而是下面这一类场景：
 * 1. 来源页跳转到目标页时，需要携带一个较复杂的对象。
 * 2. 目标页刷新后，仍然需要恢复这份上下文。
 * 3. 目标页回到来源页时，还需要把一份回传数据带回去。
 * 4. 标签页模式下，目标页标签需要记录完整查询参数，避免刷新或再次激活时丢失CDRC状态。
 *
 * 二、设计思路
 * 1. 跳转时不把完整上下文直接塞进URL，而是先写入 sessionStorage。
 * 2. URL 中只放少量CDRC标识参数：
 *    - cdrc-source: 来源页路径
 *    - cdrc-source-name: 来源页名称
 *    - cdrc-redirect-id: 本次跳转上下文ID
 *    - cdrc-return-id: 回源上下文ID
 * 3. 目标页进入后，通过 cdrc-redirect-id 从 sessionStorage 取出 send/return 上下文。
 * 4. 目标页执行回源时，将原目标上下文转成回源上下文，并通过 cdrc-return-id 带回来源页。
 * 5. 来源页进入后，通过 cdrc-return-id 读取回源上下文。
 *
 * 三、上下文生命周期
 * 1. cdrc-context:
 *    目标页上下文。
 *    用于“来源页 -> 目标页”的 send 数据传递。
 *    它支持目标页刷新恢复，所以不会在首次读取后立即删除。
 * 2. cdrc-return:
 *    回源页上下文。
 *    用于“目标页 -> 来源页”的 return 数据传递。
 *    它是一次性的，来源页读取成功后会立即删除。
 * 3. TTL:
 *    两类上下文都会附带过期时间，超过TTL后自动失效并清理，避免 sessionStorage 长期堆积脏数据。
 *
 * 四、标签页同步原理
 * 由于本项目存在标签页机制，普通路由跳转后，标签页中保存的 path 可能不包含 CDRC 查询参数。
 * 目标页一旦刷新，若标签记录的 path 不完整，就可能丢失 cdrc-redirect-id。
 * 因此在目标页识别到自己是 CDRC 页面后，会把“当前完整路由 + query”回写到当前标签页 path 中。
 * 这样标签页刷新、重新激活时，仍然可以恢复到完整的 CDRC 地址。
 *
 * 五、使用方式
 * 来源页：
 * 1. 调用 useDirectRouteContext()。
 * 2. 使用 cdrcRedirect(nameOrPath, sendQuery, returnQuery) 跳转到目标页。
 * 3. sendQuery 为发给目标页的数据，returnQuery 为目标页回源时要返还给来源页的数据模板或上下文。
 *
 * 目标页：
 * 1. 调用 useDirectRouteContext()。
 * 2. 使用 getCdrcQuery() 获取来源页传入的数据。
 * 3. 使用 cdrcReturn() 回源。
 * 4. 如果目标页不是通过CDRC进入，getCdrcQuery() 获取失败时会自动尝试回源。
 *
 * 来源页回源后：
 * 1. 调用 getCdrcReturnQuery() 获取目标页回传的数据。
 *
 * 六、典型调用示例
 * 来源页：
 * const { cdrcRedirect, getCdrcReturnQuery } = ComDirectRouteContext.useDirectRouteContext();
 * cdrcRedirect("out-model-origin-manager", scope.row, { pageNum: 1, pageSize: 20 });
 *
 * 目标页：
 * const { getCdrcQuery, cdrcReturn } = ComDirectRouteContext.useDirectRouteContext();
 * const query = getCdrcQuery();
 *
 * 七、注意事项
 * 1. CDRC 适合传递页面工作上下文，不适合替代长期状态管理。
 * 2. send/return 建议传递可序列化对象，不要传函数、类实例等不可稳定序列化的数据。
 * 3. 当前实现依赖 sessionStorage，因此只在当前浏览器会话内有效。
 * 4. 如果调用方需要区分“未通过CDRC进入”和“CDRC上下文已失效”，应结合 cdrcCanReturn 与 getCdrcQuery() 的结果一起判断。
 */
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter, type RouteRecordNameGeneric, type RouteRecordRaw } from "vue-router";
import { ElMessage } from "element-plus";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";
import ComMenuService from "@/soa/com-series/service/ComMenuService.ts";
import GenricRouteService from "@/soa/genric-route/service/GenricRouteService";

/** CDRC上下文前缀 */
const CDRC_CONTEXT_PREFIX = "cdrc-context";

/** CDRC回退上下文前缀 */
const CDRC_RETURN_CONTEXT_PREFIX = "cdrc-return";

/** CDRC目标页上下文TTL(毫秒) */
const CDRC_CONTEXT_TTL_MS = 30 * 60 * 1000;

/** CDRC回源页上下文TTL(毫秒) */
const CDRC_RETURN_CONTEXT_TTL_MS = 10 * 60 * 1000;

/**
 * CDRC上下文对象
 */
export interface CDRCContext {
  return: any; //从目标路由返回源路由时需要携带的参数
  send: any; //从源路由跳转到目标路由时需要发送的参数
}

/**
 * CDRC缓存对象
 * 用于给上下文增加过期时间，避免sessionStorage长期堆积
 */
interface CDRCCache {
  context: CDRCContext; //实际存储的CDRC上下文
  expireAt: number; //过期时间戳，超过该时间后自动失效
}

/**
 * 获取一个唯一的CDRC跳转ID
 */
function nextRedirectId(): string {
  return "cdrc-redirect-" + Date.now() + "-" + Math.random().toString(36).substring(2, 15);
}

/**
 * 获取CDRC上下文TTL
 * @param prefix CDRC上下文前缀
 * @returns TTL毫秒数
 */
function getCdrcContextTtl(prefix: string): number {
  //回源上下文是一次性的，TTL应更短
  if (prefix === CDRC_RETURN_CONTEXT_PREFIX) {
    return CDRC_RETURN_CONTEXT_TTL_MS;
  }

  //目标页上下文需要支持刷新恢复，TTL可适当更长
  return CDRC_CONTEXT_TTL_MS;
}

/**
 * 解析CDRC上下文
 * @param prefix CDRC上下文前缀
 * @param cdrcContextId CDRC上下文ID
 * @returns CDRC上下文对象
 */
function resolveCdrcContext(prefix: string, cdrcContextId: string): CDRCContext | null {
  if (!cdrcContextId) {
    return null;
  }

  const cdrcContextStr = sessionStorage.getItem(prefix + "_" + cdrcContextId);

  //如果CDRC上下文存在，则解析为CDRC上下文对象
  if (cdrcContextStr) {
    try {
      const cdrcCache = JSON.parse(cdrcContextStr) as CDRCCache | CDRCContext;
      let cdrcContext: CDRCContext = null;

      //优先按新结构解析：{ context, expireAt }
      if ("expireAt" in cdrcCache && "context" in cdrcCache) {
        //上下文已过期则直接清理
        if (cdrcCache.expireAt <= Date.now()) {
          removeCdrcContext(prefix, cdrcContextId);
          return null;
        }

        cdrcContext = cdrcCache.context;
      }

      //兼容旧结构：直接存储CDRCContext
      if (!cdrcContext && ("send" in cdrcCache || "return" in cdrcCache)) {
        cdrcContext = cdrcCache as CDRCContext;
      }

      //结构不符合预期时直接清理，避免脏数据残留
      if (!cdrcContext) {
        removeCdrcContext(prefix, cdrcContextId);
        return null;
      }

      //如果CDRC上下文里面send 和 return都为空，则认为CDRC上下文已经失效，删除SessionStorage中的CDRC上下文
      if (cdrcContext.send == null && cdrcContext.return == null) {
        removeCdrcContext(prefix, cdrcContextId);
        return null;
      }

      return cdrcContext;
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error);
      console.error("解析CDRC上下文失败:", error);
      ElMessage.error("解析CDRC上下文失败:" + errorMessage);

      //删除SessionStorage中的CDRC上下文
      removeCdrcContext(prefix, cdrcContextId);
      return null;
    }
  }

  //如果CDRC上下文不存在，则返回null
  return null;
}

/**
 * 删除CDRC上下文
 * @param prefix CDRC上下文前缀
 * @param cdrcRedirectId CDRC跳转ID或回退ID
 */
function removeCdrcContext(prefix: string, cdrcRedirectId: string): void {
  if (!cdrcRedirectId) {
    return;
  }

  sessionStorage.removeItem(prefix + "_" + cdrcRedirectId);
}

/**
 * 放入CDRC上下文
 * @param prefix CDRC上下文前缀
 * @param cdrcRedirectId CDRC跳转ID或回退ID
 * @param cdrcContext CDRC上下文对象
 */
function putCdrcContext(prefix: string, cdrcRedirectId: string, cdrcContext: CDRCContext): void {
  //如果上下文为空 不进行存储
  if (!cdrcContext) {
    return;
  }

  if (!cdrcRedirectId) {
    return;
  }

  //如果上下文中send、return为null 不进行存储
  if (cdrcContext.send == null && cdrcContext.return == null) {
    return;
  }

  const cdrcCache: CDRCCache = {
    context: cdrcContext,
    //不同前缀使用不同TTL，兼顾刷新恢复与自动清理
    expireAt: Date.now() + getCdrcContextTtl(prefix),
  };

  //放入CDRC上下文
  sessionStorage.setItem(prefix + "_" + cdrcRedirectId, JSON.stringify(cdrcCache));
}

export default {
  /**
   * 使用直接路由上下文
   */
  useDirectRouteContext() {
    const route = useRoute();
    const router = useRouter();

    //获取标签页服务
    const { getActiveTab, updateTab, openTab } = ComTabService.useTabService();

    //获取菜单服务
    const { getMenuByPath, openMenu } = ComMenuService.useMenuService();

    //获取GRS服务
    const { getRouteByNameOrPath } = GenricRouteService.useGenricRoute();

    //当前CDRC上下文
    let cdrcContext: CDRCContext = null;
    let cdrcQuery = null;
    let cdrcReturnQuery = null;
    const cdrcSource = route.query["cdrc-source"] as string;
    const cdrcSourceName = route.query["cdrc-source-name"] as string;
    const cdrcRedirectId = route.query["cdrc-redirect-id"] as string;
    const cdrcReturnId = route.query["cdrc-return-id"] as string;

    //是否可以回退
    const cdrcCanReturn = cdrcSource != null;

    //回退名称 如果为空则返回默认值
    const cdrcReturnName = "返回" + (cdrcSourceName ?? "");

    //解析CDRC上下文
    cdrcContext = resolveCdrcContext(CDRC_CONTEXT_PREFIX, cdrcRedirectId);

    //如果CDRC上下文存在，则解析send和return参数
    if (cdrcContext) {
      cdrcQuery = cdrcContext.send;
      cdrcReturnQuery = cdrcContext.return;
    }

    //如果cdrc上下文不存在，则解析回退上下文
    if (!cdrcContext) {
      cdrcContext = resolveCdrcContext(CDRC_RETURN_CONTEXT_PREFIX, cdrcReturnId);
      if (cdrcContext) {
        //删除CDRC回退上下文(CDRC回源上下文是一次性的,从目标回源需要读后删)
        removeCdrcContext(CDRC_RETURN_CONTEXT_PREFIX, cdrcReturnId);

        cdrcReturnQuery = cdrcContext.return;
      }
    }

    //如果现在正在CDRC目标页，则将当前标签页设置为CDRC目标页
    if (cdrcRedirectId) {
      //查找当前活动的标签页
      const currentActiveTab = getActiveTab();
      if (currentActiveTab) {
        //获取当前的路由地址和查询参数
        const currentRoute = router.currentRoute.value;
        const currentRouteQuery = currentRoute.query;

        //将当前的路由地址和查询参数拼接起来
        const currentRoutePath =
          currentRoute.path + "?" + new URLSearchParams(currentRouteQuery as Record<string, string>).toString();

        //更新当前标签页的地址
        updateTab({
          id: currentActiveTab.id,
          icon: currentActiveTab.icon,
          title: currentActiveTab.title,
          path: currentRoutePath,
          closable: currentActiveTab.closable,
          kind: currentActiveTab.kind,
        });
      }
    }

    /**
     * 跳转到一个已有的菜单项并携带一次性上下文，并创建一个新的标签页
     * 并创建一个新的标签页
     * @param nameOrPath 路由的名称或路径
     * @param tabId 标签页ID
     * @param tabName 标签页名称
     * @param sendQuery 需发送的查询参数
     * @returns 是否跳转成功 true:成功 false:失败
     */
    const cdrcRedirectWithNewTab = (nameOrPath: string, tabId: string, tabName: string, sendQuery?: any): boolean => {
      //查找目标路由
      const targetRoute = getRouteByNameOrPath(nameOrPath);

      if (!targetRoute) {
        console.error(`CDRC新标签跳转失败，无法通过名称或路径 ${nameOrPath} 找到对应的路由!`);
        ElMessage.error(`CDRC新标签跳转失败，无法通过名称或路径 ${nameOrPath} 找到对应的路由!`);
        return false;
      }

      if (!tabId) {
        console.error("CDRC新标签跳转失败，tabId不能为空!");
        ElMessage.error("CDRC新标签跳转失败，tabId不能为空!");
        return false;
      }

      //获取当前标签页信息（作为来源页）
      const currentTab = getActiveTab();

      if (!currentTab) {
        console.error("无法使用CDRC新标签跳转，当前激活标签不存在!");
        ElMessage.error("无法使用CDRC新标签跳转，无法找到当前激活的标签!");
        return false;
      }

      //生成唯一的CDRC跳转ID
      const redirectId = nextRedirectId();

      //构建CDRC上下文（新标签模式下不需要returnQuery，目标页无需回源）
      const newTabCdrcContext: CDRCContext = {
        return: null,
        send: sendQuery ?? null,
      };

      //将上下文存入sessionStorage
      putCdrcContext(CDRC_CONTEXT_PREFIX, redirectId, newTabCdrcContext);

      //构建目标路由的完整路径（含CDRC查询参数）
      const targetPath = targetRoute.buildPath();
      const cdrcQuery = new URLSearchParams({
        "cdrc-source": currentTab.path,
        "cdrc-source-name": currentTab.title,
        "cdrc-redirect-id": redirectId,
      }).toString();
      const tabPath = targetPath + "?" + cdrcQuery;

      //创建新标签页并激活（如果同ID标签已存在则更新路径后激活）
      openTab({
        id: tabId,
        icon: null,
        title: tabName ?? tabId,
        path: tabPath,
        closable: true,
        kind: "normal",
      });

      return true;
    };

    /**
     * 直接跳转
     * @param nameOrPath 路由的名称或路径
     * @param sendQuery 需发送的查询参数
     * @param returnQuery 从目标返回时会携带的查询参数
     * @returns 是否跳转成功 true:成功 false:失败
     */
    const cdrcRedirect = (nameOrPath: string, sendQuery?: any, returnQuery?: any): boolean => {
      //查找Vue路由里面是否有匹配的名称
      const hasFindName = router.getRoutes().find((route) => route.name === nameOrPath);
      const hasFindPath = router.getRoutes().find((route) => route.path === nameOrPath);

      if (!hasFindName && !hasFindPath) {
        console.error(`CDRC跳转失败，无法通过名称或路径 ${nameOrPath} 找到对应的路由!`);
        ElMessage.error(`CDRC跳转失败，无法通过名称或路径 ${nameOrPath} 找到对应的路由!`);
        return false;
      }

      //查询当前标签的name
      const currentTab = getActiveTab();

      if (!currentTab) {
        console.error("无法使用CDRC跳转，当前激活标签的name不存在!");
        ElMessage.error("无法使用CDRC跳转，无法找到当前激活的标签!");
        return false;
      }

      //获取一个唯一的CDRC跳转ID
      const redirectId = nextRedirectId();

      //构建CDRC上下文
      const cdrcContext: CDRCContext = {
        return: null,
        send: null,
      };

      if (sendQuery != null) {
        cdrcContext.send = sendQuery;
      }
      if (returnQuery != null) {
        cdrcContext.return = returnQuery;
      }

      //放入CDRC上下文
      putCdrcContext(CDRC_CONTEXT_PREFIX, redirectId, cdrcContext);

      //优先通过名称跳转
      if (hasFindName) {
        router.push({
          name: nameOrPath,
          query: {
            "cdrc-source": currentTab.path,
            "cdrc-source-name": currentTab.title,
            "cdrc-redirect-id": redirectId,
          },
        });
      }
      //如果名称不存在，则通过路径跳转
      if (hasFindPath) {
        router.push({
          path: nameOrPath,
          query: {
            "cdrc-source": currentTab.path,
            "cdrc-source-name": currentTab.title,
            "cdrc-redirect-id": redirectId,
          },
        });
      }

      return true;
    };

    /**
     * 使用CDRC回退
     */
    const cdrcReturn = (): void => {
      if (!cdrcCanReturn) {
        console.error("无法使用CDRC回退，当前路由没有配置回退!");
        return;
      }

      //如果现在有CDRC上下文，删除它 并将它转为回退上下文
      if (cdrcContext) {
        //删除CDRC上下文
        removeCdrcContext(CDRC_CONTEXT_PREFIX, cdrcRedirectId);

        //放入CDRC回退上下文
        putCdrcContext(CDRC_RETURN_CONTEXT_PREFIX, cdrcRedirectId, cdrcContext);
      }

      //更新当前标签页的地址
      const currentTab = getActiveTab();
      if (currentTab) {
        updateTab({
          id: currentTab.id,
          icon: currentTab.icon,
          title: currentTab.title,
          path: cdrcSource,
          closable: currentTab.closable,
          kind: currentTab.kind,
        });
      }

      //跳转到源路由并携带返回参数
      router.push({
        path: route.query["cdrc-source"] as string,
        query: {
          "cdrc-return-id": cdrcRedirectId,
        },
      });
    };

    /**
     * 获取CDRC查询参数
     * @param autoReturn 是否自动回源 如果为true，则当CDRC上下文失效时执行自动回源
     * @returns CDRC查询参数 如果CDRC上下文失效且未开启自动回源，则返回null
     */
    const getCdrcQuery = (autoReturn: boolean = true): any => {
      //如果CDRC上下文不存在，则自动回退
      if (cdrcQuery == null) {
        if (autoReturn) {
          cdrcReturn();
          ElMessage.error("CDRC上下文获取失败,自动回源到来源路由!");
        }
        return null;
      }

      return cdrcQuery;
    };

    /**
     * 获取CDRC返回查询参数
     */
    const getCdrcReturnQuery = (): any => {
      return cdrcReturnQuery;
    };

    return {
      //CDRC来源路由 这里记录了从哪个路由跳转过来的
      cdrcSource,

      //CDRC发送的查询参数 通常为来源路由需要传递给目标路由的参数
      getCdrcQuery,

      //CDRC返回的查询参数 由来源路由指定，通过CDRC回退时会自动携带这些参数(可以用来保存当时的表单数据等，避免跳转后再跳回时丢失数据)
      getCdrcReturnQuery,

      //是否可以回退
      cdrcCanReturn,

      //回退名称 如果为空则返回默认值
      cdrcReturnName,

      /**
       * CDRC回退函数
       * 用于回退到来源路由
       * 通常在目标路由的按钮上使用，点击后会自动回退到来源路由
       */
      cdrcReturn,

      /**
       * CDRC跳转函数
       * @param nameOrPath 路由的名称或路径
       * @param sendQuery 需发送的查询参数
       * @param returnQuery 从目标返回时会携带的查询参数
       */
      cdrcRedirect,

      /**
       * CDRC新标签跳转函数
       * @param nameOrPath 路由的名称或路径
       * @param tabId 标签页ID
       * @param tabName 标签页名称
       * @param sendQuery 需发送的查询参数
       */
      cdrcRedirectWithNewTab,
    };
  },
};
