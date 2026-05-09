import { Result } from "@/commons/model/Result.ts";
import type { GetUserMenuTreeVo } from "@/views/core/api/MenuApi.ts";
import MenuApi from "@/views/core/api/MenuApi.ts";
import { defineStore, storeToRefs } from "pinia";
import { ElMessage } from "element-plus";
import { computed, ref, type Ref } from "vue";
import { useRoute } from "vue-router";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";
import UserAuthService from "@/views/auth/service/UserAuthService.ts";

const { hasSuper } = UserAuthService.usePreAuthorize();
const fallbackMc: GetUserMenuTreeVo = {
  id: "fallback-maintenance-center",
  name: "维护中心(备用)",
  path: "/core/application-maintain",
  icon: "Setting",
  kind: 1,
  children: [],
};

export default {
  /**
   * 使用菜单存储
   * 菜单储存管理全局状态和逻辑操作（它本身不知道“路由”是什么，也不知道“路径”是什么）
   * 它只负责管理菜单树和菜单展开状态，不负责菜单的加载和过滤等业务逻辑
   */
  useMenuStore() {
    const menuStore = defineStore("menuStore", {
      state: () => ({
        //菜单树数据
        menuTree: [] as GetUserMenuTreeVo[],

        //菜单展开数据(记录哪些菜单是展开的)
        menuOpened: [] as string[],

        //菜单树加载状态
        loading: false,
      }),

      //持久化菜单展开数据
      persist: {
        key: "np_soa_menu_opened",
        pick: ["menuOpened"],
      },

      getters: {
        getMenuTree: (state) => state.menuTree,
        getMenuOpened: (state) => state.menuOpened,
        getLoading: (state) => state.loading,
      },
      actions: {},
    });

    return menuStore();
  },

  /**
   * 使用菜单服务
   * 菜单服务作为业务胶水层，负责把 Vue Router (useRoute())、计算属性 (computed) 和 Pinia 状态 (menuStore) 组合在一起提供给组件。
   */
  useMenuService() {
    //先获取菜单储存
    const menuStore = this.useMenuStore();

    //获取标签页服务
    const { openTab } = ComTabService.useTabService();

    //获取路由
    const route = useRoute();

    //解构菜单储存以便在服务里面返回给组件
    const { menuTree, menuOpened, loading } = storeToRefs(menuStore);

    /**
     * 从后端重新加载菜单树
     */
    const loadMenus = async (): Promise<void> => {
      menuStore.loading = true;

      try {
        const result = await MenuApi.getUserMenuTree();
        if (Result.isSuccess(result)) {
          menuStore.menuTree = result.data;
        }
      } catch (error) {
        ElMessage.error(error.message);
      } finally {
        menuStore.loading = false;
      }

      //检查菜单树里面是否有维护中心
      const mcMenu = getMenuByPath("/core/application-maintain");

      //如果维护中心菜单不存在，则在菜单树的第一位添加一个备用的维护中心菜单(仅sa权限用户需添加备用)
      if (mcMenu == null && hasSuper()) {
        console.log("添加备用维护中心菜单", fallbackMc);
        menuStore.menuTree.push(fallbackMc);
      }
    };

    /**
     * 展开/折叠菜单
     * @param menuId 菜单ID
     * @param open 是否展开
     */
    const expandMenu = (menuId: string, open: boolean): void => {
      if (open) {
        menuOpened.value.push(menuId);
        return;
      }

      if (menuOpened.value.includes(menuId)) {
        menuOpened.value.splice(menuOpened.value.indexOf(menuId), 1);
        return;
      }
    };

    /**
     * 根据路径获取菜单对象
     * @param path 路径
     * @param nodes 菜单树数据
     * @returns 菜单对象GetUserMenuTreeVo | null:未找到
     */
    const getMenuByPath = (path: string, nodes?: GetUserMenuTreeVo[]): GetUserMenuTreeVo | null => {
      const list = nodes ?? menuTree.value;
      for (const node of list) {
        if (node.path === path) {
          return node;
        }
        if (node.children?.length) {
          const found = getMenuByPath(path, node.children);
          if (found) {
            return found;
          }
        }
      }
      return null;
    };

    /**
     * 根据路径获取菜单ID
     * @param path 路径
     * @returns 菜单ID | null:未找到
     */
    const getMenuIdByPath = (path: string): string => {
      return getMenuByPath(path)?.id ?? null;
    };

    /**
     * 过滤目录类型菜单
     * @param menuTree 菜单树数据
     * @returns 过滤后的目录类型菜单数据
     */
    const filterDirectoryMenu = (menuTree: GetUserMenuTreeVo[]): GetUserMenuTreeVo[] => {
      return menuTree.filter((item) => item.kind === 0);
    };

    /**
     * 过滤菜单项类型菜单
     * @param menuTree 菜单树数据
     * @returns 过滤后的菜单类型菜单数据
     */
    const filterItemMenu = (menuTree: GetUserMenuTreeVo[]): GetUserMenuTreeVo[] => {
      return menuTree.filter((item) => item.kind === 1);
    };

    /**
     * 当前活动菜单ID
     */
    const activeMenuId = computed(() => {
      return getMenuIdByPath(route.path);
    });

    /**
     * 打开菜单并跳转路由
     * @param menuId 菜单ID
     */
    const openMenu = (item: GetUserMenuTreeVo): void => {
      //如果菜单类型不是菜单项类型，则不处理
      if (item.kind !== 1) {
        return;
      }

      //如果菜单路径为空，则不处理
      if (!item.path) {
        return;
      }

      //使用tabStore添加新标签
      openTab({
        id: item.id,
        icon: item.icon,
        title: item.name,
        path: item.path,
      });
    };

    return {
      //菜单树
      menuTree,

      //菜单加载状态
      loading,

      //菜单展开状态
      menuOpened,

      //当前活动菜单ID
      activeMenuId,

      //加载菜单树
      loadMenus,

      //展开菜单
      expandMenu,

      //打开菜单并跳转路由
      openMenu,

      //过滤目录类型菜单
      filterDirectoryMenu,

      //过滤菜单项类型菜单
      filterItemMenu,

      //根据路径获取菜单对象
      getMenuByPath,

      //根据路径获取菜单ID
      getMenuIdByPath,
    };
  },
};
