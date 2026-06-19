import type { GetUserMenuTreeVo } from "@/views/core/api/MenuApi.ts";

/**
 * NEO 二级菜单分组
 */
export interface NeoMenuSectionVo {
  id: string;
  title: string;
  items: GetUserMenuTreeVo[];
}
