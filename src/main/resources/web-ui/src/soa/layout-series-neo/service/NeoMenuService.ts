import { computed, onMounted, ref } from "vue";
import type { GetUserMenuTreeVo } from "@/views/core/api/MenuApi.ts";
import type { NeoMenuSectionVo } from "@/soa/layout-series-neo/api/NeoMenuApi.ts";
import ComMenuService from "@/soa/com-series/service/ComMenuService.ts";

const selectedTopId = ref<string | null>(null);

const containsActiveMenu = (node: GetUserMenuTreeVo, activeId: string): boolean => {
  if (node.id === activeId) {
    return true;
  }

  if (!node.children?.length) {
    return false;
  }

  return node.children.some((child) => containsActiveMenu(child, activeId));
};

const findFirstOpenableMenu = (node: GetUserMenuTreeVo): GetUserMenuTreeVo | null => {
  if (node.kind === 1 || node.kind === 3 || node.kind === 4) {
    if (node.path) {
      return node;
    }
  }

  if (!node.children?.length) {
    return null;
  }

  for (const child of node.children) {
    const found = findFirstOpenableMenu(child);
    if (found) {
      return found;
    }
  }

  return null;
};

const appendDirectorySections = (
  sections: NeoMenuSectionVo[],
  dir: GetUserMenuTreeVo,
  filterItemMenu: (nodes: GetUserMenuTreeVo[]) => GetUserMenuTreeVo[],
  filterDirectoryMenu: (nodes: GetUserMenuTreeVo[]) => GetUserMenuTreeVo[]
): void => {
  const children = dir.children ?? [];
  const items = filterItemMenu(children);

  if (items.length && dir.id && dir.name) {
    sections.push({
      id: dir.id,
      title: dir.name,
      items,
    });
  }

  const subDirs = filterDirectoryMenu(children);
  for (const subDir of subDirs) {
    appendDirectorySections(sections, subDir, filterItemMenu, filterDirectoryMenu);
  }
};

const buildMenuSections = (
  topMenu: GetUserMenuTreeVo | null,
  filterItemMenu: (nodes: GetUserMenuTreeVo[]) => GetUserMenuTreeVo[],
  filterDirectoryMenu: (nodes: GetUserMenuTreeVo[]) => GetUserMenuTreeVo[]
): NeoMenuSectionVo[] => {
  if (!topMenu?.children?.length) {
    return [];
  }

  const sections: NeoMenuSectionVo[] = [];
  const children = topMenu.children;
  const directItems = filterItemMenu(children);

  for (const child of children) {
    if (child.kind !== 0) {
      continue;
    }

    appendDirectorySections(sections, child, filterItemMenu, filterDirectoryMenu);
  }

  if (directItems.length && topMenu.id && topMenu.name) {
    sections.push({
      id: `${topMenu.id}-direct`,
      title: topMenu.name,
      items: directItems,
    });
  }

  return sections;
};

const resolveActiveTopId = (menuTree: GetUserMenuTreeVo[], routeActiveId: string | null): string | null => {
  if (routeActiveId) {
    for (const item of menuTree) {
      if (containsActiveMenu(item, routeActiveId)) {
        return item.id ?? null;
      }
    }
  }

  return selectedTopId.value;
};

export default {
  /**
   * NEO 一级菜单
   */
  useNeoMenuT1() {
    const { menuTree, loading, activeMenuId, loadMenus, openMenu } = ComMenuService.useMenuService();

    const activeTopId = computed(() => resolveActiveTopId(menuTree.value, activeMenuId.value));

    const isTopMenuActive = (item: GetUserMenuTreeVo): boolean => {
      if (!activeTopId.value) {
        return false;
      }

      return item.id === activeTopId.value;
    };

    const onTopMenuClick = (item: GetUserMenuTreeVo): void => {
      selectedTopId.value = item.id ?? null;

      if (item.kind === 1 || item.kind === 3 || item.kind === 4) {
        openMenu(item);
        return;
      }

      const firstLeaf = findFirstOpenableMenu(item);
      if (!firstLeaf) {
        return;
      }

      openMenu(firstLeaf);
    };

    onMounted(() => {
      loadMenus();
    });

    return {
      menuTree,
      loading,
      isTopMenuActive,
      onTopMenuClick,
    };
  },

  /**
   * NEO 二级菜单
   */
  useNeoMenuT2() {
    const { menuTree, loading, activeMenuId, openMenu, filterDirectoryMenu, filterItemMenu } = ComMenuService.useMenuService();

    const activeTopId = computed(() => resolveActiveTopId(menuTree.value, activeMenuId.value));

    const activeTopMenu = computed(() => {
      if (!activeTopId.value) {
        return null;
      }

      return menuTree.value.find((item) => item.id === activeTopId.value) ?? null;
    });

    const menuSections = computed(() => buildMenuSections(activeTopMenu.value, filterItemMenu, filterDirectoryMenu));

    const menuT2Visible = computed(() => menuSections.value.length > 0);

    const onMenuItemClick = (item: GetUserMenuTreeVo): void => {
      openMenu(item);
    };

    return {
      loading,
      activeMenuId,
      menuSections,
      menuT2Visible,
      onMenuItemClick,
    };
  },
};
