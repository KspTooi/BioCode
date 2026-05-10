import Http from "@/commons/Http.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type Result from "@/commons/model/Result.ts";

export interface GetPermissionDefinitionVo {
  id: string; // 权限节点ID
  code: string; // 权限节点标识
  name: string; // 权限节点名称
}

export interface GetPermissionListDto extends PageQuery {
  code?: string | null; // 权限代码
  name?: string | null; // 权限名称
}

export interface GetPermissionListVo {
  id: string; // 权限ID
  name: string; // 权限名称
  code: string; // 权限代码
  remark: string; // 权限描述
  seq: number; // 排序号
  isSystem: number; // 是否为系统权限 1:是 0:否
}

export interface GetPermissionDetailsVo {
  id: string; // 权限ID
  name: string; // 权限名称
  code: string; // 权限代码
  remark: string; // 权限描述
  seq: number; // 排序号
  isSystem: number; // 是否为系统权限 1:是 0:否
  createTime: string; // 创建时间
  creatorId: string; // 创建人
  updateTime: string; // 修改时间
  updaterId: string; // 修改人
}

export interface AddPermissionDto {
  name: string; // 权限名称
  code: string; // 权限代码
  remark?: string | null; // 权限描述
  seq: number; // 排序号
}

export interface EditPermissionDto {
  id: string; // 权限ID
  name: string; // 权限名称
  code: string; // 权限代码
  remark?: string | null; // 权限描述
  seq: number; // 排序号
}

export default {
  /**
   * 获取所有权限定义
   */
  getPermissionDefinition: async (): Promise<GetPermissionDefinitionVo[]> => {
    const result = await Http.postEntity<Result<GetPermissionDefinitionVo[]>>("/permission/getPermissionDefinition", {});
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 获取权限列表（分页）
   */
  getPermissionList: async (dto: GetPermissionListDto): Promise<PageResult<GetPermissionListVo>> => {
    return await Http.postEntity<PageResult<GetPermissionListVo>>("/permission/getPermissionList", dto);
  },

  /**
   * 获取权限详情
   */
  getPermissionDetails: async (dto: CommonIdDto): Promise<GetPermissionDetailsVo> => {
    const result = await Http.postEntity<Result<GetPermissionDetailsVo>>("/permission/getPermissionDetails", dto);
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增权限
   */
  addPermission: async (dto: AddPermissionDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/permission/addPermission", dto);
  },

  /**
   * 编辑权限
   */
  editPermission: async (dto: EditPermissionDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/permission/editPermission", dto);
  },

  /**
   * 删除权限
   */
  removePermission: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/permission/removePermission", dto);
    if (result.code == 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
