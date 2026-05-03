import Http from "@/commons/Http.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type Result from "@/commons/model/Result.ts";

export interface GroupPermissionDefinitionVo {
  id: string; // 权限节点ID
  code: string; // 权限节点标识
  name: string; // 权限节点名称
  has: number; // 当前组是否拥有 0:拥有 1:不拥有
}

export interface GetGroupListDto extends PageQuery {
  keyword?: string; // 模糊匹配 组编码、组名称、组描述
  status?: number; // 组状态：0:禁用 1:启用
}

export interface GetGroupListVo {
  id: string; // 组ID
  code: string; // 组编码
  name: string; // 组名称
  memberCount: number; // 成员数量
  permissionCount: number; // 权限节点数量
  rowScope: number; // RS数据权限等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织
  isSystem: number; // 系统内置组 0:否 1:是
  status: number; // 组状态：0-禁用，1-启用
  seq: number; // 排序号
  createTime: string; // 创建时间
}

export interface GetGroupDetailsVo {
  id: string; // 组ID
  code: string; // 组编码
  name: string; // 组名称
  remark: string; // 组描述
  isSystem: number; // 系统内置组 0:否 1:是
  status: number; // 组状态：0-禁用，1-启用
  seq: number; // 排序号
  rowScope: number; // 数据权限 0:全部 1:本公司/租户及以下 2:本部门及以下 3:本部门 4:仅本人 5:指定部门
  deptIds?: string[]; // 部门ID列表
  permissions: GroupPermissionDefinitionVo[]; // 权限节点列表
}

export interface AddGroupDto {
  code: string; // 组编码
  name: string; // 组名称
  remark?: string; // 组描述
  status: number; // 组状态：0-禁用，1-启用
  seq: number; // 排序号
  rowScope: number; // 数据权限 0:全部 1:本公司/租户及以下 2:本部门及以下 3:本部门 4:仅本人 5:指定部门
  deptIds?: string[]; // 部门ID列表
  permissionIds?: string[]; // 权限ID列表
}

export interface EditGroupDto {
  id: string; // 组ID
  code: string; // 组编码
  name: string; // 组名称
  remark?: string; // 组描述
  status: number; // 组状态：0-禁用，1-启用
  seq: number; // 排序号
  rowScope: number; // 数据权限 0:全部 1:本公司/租户及以下 2:本部门及以下 3:本部门 4:仅本人 5:指定部门
  deptIds?: string[]; // 部门ID列表
  permissionIds?: string[]; // 权限ID列表
}

export interface GetGroupPermissionMenuViewDto {
  groupId: string; // 组ID
  keyword?: string; // 模糊匹配 菜单名称、菜单路径
  hasPermission?: number | null; // 是否已授权 0:否 1:是
}

export interface GetGroupPermissionMenuViewVo {
  id: string; // 菜单ID
  parentId: string | null; // 父级ID null:根节点
  name: string; // 菜单名称
  icon: string; // 菜单图标
  kind: number; // 菜单类型 0:目录 1:菜单 2:按钮
  path: string; // 菜单路径
  permissionCode: string; // 所需权限码
  missingPermission: number; // 是否缺失权限节点 0:否 1:完全缺失 2:部分缺失
  hasPermission: number; // 当前组是否有权限 0:否 1:是 2:部分授权
  seq: number; // 排序
  children: GetGroupPermissionMenuViewVo[]; // 子菜单
}

export interface GetGroupPermissionNodeDto extends PageQuery {
  groupId: string; // 组ID
  keyword?: string | null; // 模糊匹配 权限节点名称
  hasPermission?: number | null; // 是否已授权 0:否 1:是
}

export interface GetGroupPermissionNodeVo {
  id: string; // 权限ID
  name: string; // 权限名称
  code: string; // 权限标识
  remark: string; // 权限描述
  seq: number; // 排序号
  hasPermission: number; // 是否已授权 0:否 1:是
}

export interface GrantAndRevokeDto {
  groupId: string; // 组ID
  permissionCodes: string[]; // 权限代码列表
  type: number; // 类型 0:授权 1:取消授权
}

export interface SimulateRsDto {
  orgId: string; // 模拟用户所在的组织节点ID
  rsLevel: number; // 模拟的RS等级 0/10/20/30/40/50/100
}

export interface SimulateRsVo {
  rsLevel: number; // 本次模拟使用的RS等级
  orgId: string; // 模拟节点ID
  nodeKind: number; // 模拟节点的kind 0:企业 1:子企业 2:部门 3:班组
  allMode: boolean; // 是否为全量模式(rsLevel=0时为true)
  visibleOrgIds: string[]; // 该等级下可见的组织节点ID集合
}

export default {
  /**
   * 获取组列表
   */
  getGroupList: async (dto: GetGroupListDto): Promise<PageResult<GetGroupListVo>> => {
    return await Http.postEntity<PageResult<GetGroupListVo>>("/group/getGroupList", dto);
  },

  /**
   * 获取组详情
   */
  getGroupDetails: async (dto: CommonIdDto): Promise<GetGroupDetailsVo> => {
    const result = await Http.postEntity<Result<GetGroupDetailsVo>>("/group/getGroupDetails", dto);
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增组
   */
  addGroup: async (dto: AddGroupDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/group/addGroup", dto);
  },

  /**
   * 编辑组
   */
  editGroup: async (dto: EditGroupDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/group/editGroup", dto);
  },

  /**
   * 批量授权或取消授权
   */
  grantAndRevoke: async (dto: GrantAndRevokeDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/group/grantAndRevoke", dto);
  },

  /**
   * 获取组权限菜单视图
   */
  getGroupPermissionMenuView: async (dto: GetGroupPermissionMenuViewDto): Promise<GetGroupPermissionMenuViewVo[]> => {
    const result = await Http.postEntity<Result<GetGroupPermissionMenuViewVo[]>>("/group/getGroupPermissionMenuView", dto);
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 获取组权限节点视图
   */
  getGroupPermissionNodeView: async (dto: GetGroupPermissionNodeDto): Promise<PageResult<GetGroupPermissionNodeVo>> => {
    return await Http.postEntity<PageResult<GetGroupPermissionNodeVo>>("/group/getGroupPermissionNodeView", dto);
  },

  /**
   * 模拟RS数据权限
   */
  simulateRs: async (dto: SimulateRsDto): Promise<SimulateRsVo> => {
    const result = await Http.postEntity<Result<SimulateRsVo>>("/group/simulateRs", dto);
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 删除组
   */
  removeGroup: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/group/removeGroup", dto);
    if (result.code == 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
