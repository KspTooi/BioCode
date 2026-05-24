import { defineStore, getActivePinia, storeToRefs } from "pinia";
import { watch } from "vue";
import { useRouter } from "vue-router";

/**
 * 标签持久化对象
 */
export interface Tab {
  id: string; // 标签唯一ID
  icon: string | null; // 标签图标名称
  title: string; // 标签标题
  path: string; // 标签路径
  closable?: boolean; // 是否可关闭 默认true
  kind: "normal" | "iframe"; // 标签类型 普通 外链嵌入
}

/**
 * 固定标签列表（由入口通过 addFixedTabs 注入）
 */
const fixedTabs: Tab[] = [];

/**
 * 固定标签ID集合
 */
const fixedTabIds = new Set<string>();

const useTabStoreDef = defineStore("tabStore", {
  state: () => ({
    //标签列表（含固定标签）
    tabs: [...fixedTabs] as Tab[],

    //当前激活标签ID
    activeTabId: null as string | null,

    //刷新计数器
    refreshCounter: 0,
  }),

  //持久化标签列表和当前激活标签ID
  persist: {
    key: "np_soa_tabs",
    pick: ["tabs", "activeTabId"],
  },
});

export default {
  /**
   * 添加固定标签页
   * 须在 Pinia 注册后、首次 useTabStore / useTabService 之前调用
   * @param tabs 固定标签列表
   */
  addFixedTabs(incoming: Tab[]): void {
    if (!incoming?.length) {
      return;
    }

    for (const tab of incoming) {
      if (!tab?.id || fixedTabIds.has(tab.id)) {
        continue;
      }
      fixedTabs.push(tab);
      fixedTabIds.add(tab.id);
    }

    const pinia = getActivePinia();
    if (!pinia) {
      return;
    }

    const store = useTabStoreDef(pinia);
    for (const tab of fixedTabs) {
      if (store.tabs.find((t) => t.id === tab.id)) {
        continue;
      }
      store.tabs.unshift(tab);
    }
  },

  /**
   * 使用标签存储
   * 标签存储管理全局状态，只负责存储标签列表、当前激活标签ID和刷新计数器
   */
  useTabStore() {
    return useTabStoreDef();
  },

  /**
   * 使用标签服务
   * 标签服务作为业务胶水层，负责把 Vue Router 与 Pinia 状态组合在一起提供给组件（不含路由与激活标签的自动同步，见 useRouterTabService）
   */
  useTabService() {
    const tabStore = this.useTabStore();
    const router = useRouter();
    const { tabs, activeTabId, refreshCounter } = storeToRefs(tabStore);

    /**
     * 激活一个标签（同步路由）
     * @param tabId 标签ID
     */
    const activeTab = (tabId: string): void => {
      //先查找要激活的标签
      const tabToActivate = tabs.value.find((t) => t.id === tabId);

      //要激活的标签不存在，就不处理
      if (!tabToActivate) {
        return;
      }
      //激活标签
      activeTabId.value = tabToActivate.id;

      //如果标签是外链嵌入,
      if (tabToActivate.kind === "iframe") {
        router.push({
          path: "/external-link",
          query: {
            url: tabToActivate.path,
          },
        });
        return;
      }

      //如果当前的路由路径与要激活的标签的路径不一致，才会跳转
      if (router.currentRoute.value.path !== tabToActivate.path) {
        router.push(tabToActivate.path);
      }
    };

    /**
     * 激活一个标签(根据索引)
     * @param index 索引
     */
    const activeOf = (index: number): void => {
      //如果索引小于1或大于标签列表的长度，则不处理
      if (index < 1 || index > tabs.value.length) {
        return;
      }
      //激活标签
      activeTab(tabs.value[index - 1].id);
    };

    /**
     * 获取一个标签
     * @param tabId 标签ID
     * @returns 成功返回Tab对象，失败返回null
     */
    const getTab = (tabId: string): Tab => {
      return tabs.value.find((t) => t.id === tabId) ?? null;
    };

    /**
     * 创建一个新标签
     * @param tab 标签
     * @returns 返回标签ID
     */
    const createTab = (tab: Tab): string => {
      if (!tab?.id) {
        return null;
      }
      //如果标签没有名字,其ID作为名字
      if (!tab?.title) {
        tab.title = tab.id;
      }
      //存在同ID标签,则更新
      const existingTab = getTab(tab.id);

      if (existingTab) {
        updateTab(tab);
        return existingTab.id;
      }
      //不存在同ID标签,则创建
      tabs.value.push(tab);
      return tab.id;
    };

    /**
     * 打开一个新标签，并追加到所有标签的最后
     * @param tab 标签
     * @returns 成功返回标签ID，失败返回null
     */
    const openTab = (tab: Tab): string | null => {
      //先创建标签
      const tabId = createTab(tab);
      //激活标签
      activeTab(tabId);
      return tab.id;
    };

    /**
     * 打开一个新标签，并插入到指定索引位置
     * @param tab 标签
     * @param index 索引（从0开始，最小为fixedTabs.length）
     * @returns 成功返回标签ID，失败返回null
     */
    const openTabAt = (tab: Tab, index: number): string | null => {
      if (!tab?.id) {
        return null;
      }
      // 已存在相同ID的标签时直接激活，不重复插入
      const existingTab = tabs.value.find((t) => t.id === tab.id);
      if (existingTab) {
        activeTab(existingTab.id);
        return existingTab.id;
      }
      let insertIndex = index;
      // 如果索引小于固定标签的数量，则插入到固定标签的末尾
      if (insertIndex < fixedTabs.length) {
        insertIndex = fixedTabs.length;
      }
      // 如果索引大于标签列表的长度，则插入到标签列表的末尾
      if (insertIndex > tabs.value.length) {
        insertIndex = tabs.value.length;
      }
      //插入标签
      tabs.value.splice(insertIndex, 0, tab);
      //激活标签
      activeTab(tab.id);
      return tab.id;
    };

    /**
     * 关闭一个标签
     * @param tabId 标签ID
     */
    const closeTab = (tabId: string): void => {
      // 固定标签不可关闭
      if (fixedTabIds.has(tabId)) {
        return;
      }

      //先根据ID查找标签
      //const existingTab = getTab(tabId);

      //如果标签不可关闭，就不处理
      //if (!existingTab?.closable) {
      //  return;
      //}

      //根据ID查找标签在标签列表中的索引
      const index = tabs.value.findIndex((t) => t.id === tabId);

      if (index === -1) {
        return;
      }
      const wasActive = activeTabId.value === tabId;
      // 若关闭的是当前激活标签，提前确定下一个激活目标：优先右侧，其次左侧
      const nextActive = wasActive ? tabs.value[index + 1] || tabs.value[index - 1] : null;

      //删除标签
      tabs.value.splice(index, 1);

      //如果关闭的不是当前激活标签，就不处理
      if (!wasActive) {
        return;
      }

      //如果下一个激活标签存在，则激活下一个激活标签
      if (nextActive) {
        activeTab(nextActive.id);
        return;
      }

      //如果下一个激活标签不存在，则设置当前激活标签ID为null
      activeTabId.value = null;
    };

    /**
     * 关闭所有非激活标签
     * 这个函数不会关闭那些固定标签和不可关闭的标签
     */
    const closeNoActiveTab = (): void => {
      //先获取当前激活的标签
      const currentActiveTab = getActiveTab();

      //如果当前没有正在激活的标签
      if (!currentActiveTab) {
        //如果当前连一个标签都没有,则不处理
        if (tabs.value.length === 0) {
          return;
        }

        //如果有标签,就激活第一个标签
        activeTab(tabs.value[0].id);
      }

      //删除所有标签(除了固定标签、不可关闭标签和当前激活标签)
      tabs.value = tabs.value.filter((t) => fixedTabIds.has(t.id) || t.closable === false || t.id === activeTabId.value);
    };

    /**
     * 获取当前激活标签
     * @returns 成功返回Tab对象，失败返回null
     */
    const getActiveTab = (): Tab | null => {
      if (!activeTabId.value) {
        return null;
      }
      return getTab(activeTabId.value);
    };

    /**
     * 获取当前激活标签的索引
     * @returns 成功返回索引，失败返回0
     */
    const getActiveTabIndex = (): number => {
      if (!activeTabId.value) {
        return 0;
      }

      const index = tabs.value.findIndex((t) => t.id === activeTabId.value);
      if (index === -1) {
        return 0;
      }
      return index;
    };

    /**
     * 更新一个已存在的标签
     * @param tab 标签
     * @returns 成功返回true，失败返回false
     */
    const updateTab = (tab: Tab): boolean => {
      if (!tab?.id) {
        return false;
      }
      //先根据ID查找标签
      const existing = getTab(tab.id);

      //如果标签不存在,则返回false
      if (!existing) {
        return false;
      }

      //更新标签
      existing.title = tab.title;
      existing.path = tab.path;
      existing.icon = tab.icon;
      existing.closable = tab.closable;
      return true;
    };

    /**
     * 刷新当前激活标签(只刷标签,不刷浏览器)
     */
    const refreshActiveTab = (): void => {
      refreshCounter.value++;
    };

    return {
      //标签列表
      tabs,

      //当前激活标签ID
      activeTabId,

      //刷新计数器
      refreshCounter,

      //打开一个新标签，并追加到所有标签的最后
      openTab,

      //创建一个新标签
      createTab,

      //打开一个新标签，并插入到指定索引位置
      openTabAt,

      //关闭一个标签
      closeTab,

      //关闭所有非激活标签
      closeNoActiveTab,

      //激活一个标签
      activeTab,

      //激活一个标签(根据索引)
      activeOf,

      //获取当前激活标签
      getActiveTab,

      //获取当前激活标签的索引
      getActiveTabIndex,

      //更新一个已存在的标签
      updateTab,

      //刷新当前激活标签(只刷标签,不刷浏览器)
      refreshActiveTab,
    };
  },

  /**
   * 使用路由标签服务
   * 在 useTabService 基础上增加：路由 path 变化时自动将 activeTabId 同步到对应标签
   */
  useRouterTabService() {
    const tabService = this.useTabService();
    const router = useRouter();
    const { tabs, activeTabId } = tabService;

    // 监听路由路径变化，自动将 activeTabId 同步到路径匹配的标签
    watch(
      () => router.currentRoute.value.path,
      (newPath) => {
        const matchingTab = tabs.value.find((t) => t.path === newPath);
        if (!matchingTab) {
          return;
        }
        if (activeTabId.value === matchingTab.id) {
          return;
        }
        activeTabId.value = matchingTab.id;
      },
      { immediate: true }
    );

    return tabService;
  },
};
