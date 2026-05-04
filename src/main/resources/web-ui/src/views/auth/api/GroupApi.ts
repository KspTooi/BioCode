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
  rowScope: number; // RS数据权限等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织
  deptIds?: string[]; // 部门ID列表
  permissions: GroupPermissionDefinitionVo[]; // 权限节点列表
  menuIds?: string[]; // 菜单ID列表
}

export interface AddGroupDto {
  code: string; // 组编码
  name: string; // 组名称
  remark?: string; // 组描述
  status: number; // 组状态：0-禁用，1-启用
  seq: number; // 排序号
  rowScope: number; // RS数据权限等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织
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
  rowScope: number; // RS数据权限等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织
  deptIds?: string[]; // 部门ID列表
  permissionIds?: string[]; // 权限ID列表
}

export interface UpdateGroupGmDto {
  groupId: string; // 组ID
  menuIds?: string[]; // 菜单ID列表
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
   * 删除组
   */
  removeGroup: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/group/removeGroup", dto);
    if (result.code == 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 更新组菜单
   */
  updateGroupGm: async (dto: UpdateGroupGmDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/group/updateGroupGm", dto);
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
};
