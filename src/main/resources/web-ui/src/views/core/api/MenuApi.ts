import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import Http from "@/commons/Http.ts";
import type Result from "@/commons/model/Result.ts";
import type { GetPackListVo } from "@/views/core/api/PackApi.ts";

export interface AddMenuDto {
  parentId?: string | null; // 父级ID null:根节点
  name?: string | null; // 菜单/按钮名称
  kind?: number | null; // 菜单类型 0:目录 1:菜单 2:按钮
  path?: string | null; // 菜单路径(目录不能填写)
  icon?: string | null; // 菜单图标
  hide?: number | null; // 是否隐藏 0:否 1:是
  permissionCode?: string[]; // 所需权限码列表
  seq?: number | null; // 排序
  remark?: string | null; // 备注
}

export interface EditMenuDto {
  id?: string | null; // 菜单ID
  parentId?: string | null; // 父级ID -1:根节点
  name?: string | null; // 菜单名称
  kind?: number | null; // 菜单类型 0:目录 1:菜单 2:按钮 3:外链嵌套 4:外链跳转
  path?: string | null; // 菜单路径(目录不能填写)
  icon?: string | null; // 菜单图标
  hide?: number | null; // 是否隐藏 0:否 1:是
  permissionCode?: string[]; // 所需权限码列表
  seq?: number | null; // 排序
  remark?: string | null; // 备注
}

export interface GetMenuDetailsVo {
  id?: string | null; // 菜单ID
  parentId?: string | null; // 父级ID null:根节点
  name?: string | null; // 菜单名称
  kind?: number | null; // 菜单类型 0:目录 1:菜单 2:按钮 3:外链嵌套 4:外链跳转
  path?: string | null; // 菜单路径(目录不能填写)
  icon?: string | null; // 菜单图标
  hide?: number | null; // 是否隐藏 0:否 1:是
  permissionCode?: string[]; // 所需权限码列表
  seq?: number | null; // 排序
  remark?: string | null; // 备注
}

export interface GetMenuTreeDto {
  name?: string | null; // 菜单名称(模糊)
  kind?: number | null; // 菜单类型
  permissionCode?: string | null; // 权限码(模糊)
}

export interface GetMenuTreeVo {
  id?: string | null; // 菜单ID
  parentId?: string | null; // 父级ID null:根节点
  name?: string | null; // 菜单名称
  kind?: number | null; // 菜单类型 0:目录 1:菜单 2:按钮 3:外链嵌套 4:外链跳转
  path?: string | null; // 菜单路径
  icon?: string | null; // 菜单图标
  hide?: number | null; // 是否隐藏 0:否 1:是
  permissionCode?: string[]; // 所需权限码列表
  seq?: number | null; // 排序
  children: GetMenuTreeVo[]; // 子菜单
}

export interface GetUserMenuTreeVo {
  id?: string | null; // 菜单ID
  parentId?: string | null; // 父级ID null:根节点
  name?: string | null; // 菜单名称
  icon?: string | null; // 菜单图标
  kind?: number | null; // 菜单类型 0:目录 1:菜单 2:按钮 3:外链嵌套 4:外链跳转
  path?: string | null; // 菜单路径
  hide?: number | null; // 是否隐藏 0:否 1:是
  permissionCode?: string[]; // 所需权限码列表
  seq?: number | null; // 排序
  children: GetUserMenuTreeVo[]; // 子菜单
}

export default {
  /**
   * 获取用户菜单树
   */
  getUserMenuTree: async (): Promise<Result<GetUserMenuTreeVo[]>> => {
    return await Http.postEntity<Result<GetUserMenuTreeVo[]>>("/menu/getUserMenuTree", {});
  },

  /**
   * 获取菜单树
   */
  getMenuTree: async (dto: GetMenuTreeDto): Promise<Result<GetMenuTreeVo[]>> => {
    return await Http.postEntity<Result<GetMenuTreeVo[]>>("/menu/getMenuTree", dto);
  },

  /**
   * 新增菜单
   */
  addMenu: async (dto: AddMenuDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/menu/addMenu", dto);
  },

  /**
   * 编辑菜单
   */
  editMenu: async (dto: EditMenuDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/menu/editMenu", dto);
  },

  /**
   * 获取菜单详情
   */
  getMenuDetails: async (dto: CommonIdDto): Promise<Result<GetMenuDetailsVo>> => {
    return await Http.postEntity<Result<GetMenuDetailsVo>>("/menu/getMenuDetails", dto);
  },

  /**
   * 删除菜单
   */
  removeMenu: async (dto: CommonIdDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/menu/removeMenu", dto);
  },

  /**
   * 根据菜单ID查询所属菜单包
   */
  getPacksByMenuId: async (dto: CommonIdDto): Promise<Result<GetPackListVo[]>> => {
    return await Http.postEntity<Result<GetPackListVo[]>>("/pack/getPacksByMenuId", dto);
  },
};
