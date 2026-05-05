import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询租户列表Dto
 */
export interface GetCoreRootListDto extends PageQuery {
  name?: string; // 租户名称
  expireTime?: string; // 到期时间(null长期)
  status?: number; // 状态 1:正常，0:停用
}

/**
 * 查询租户列表Vo
 */
export interface GetCoreRootListVo {
  id: string; // 主键ID
  name: string; // 租户名称
  ruCount: number; // 用户总数
  expireTime: string; // 到期时间(null长期)
  status: number; // 状态 1:正常，0:停用
  adminUsername?: string; // 管理员账号
  isSystem: number; // 内置租户 0:否 1:是
  createTime: string; // 创建时间
}

/**
 * 查询租户详情Vo
 */
export interface GetCoreRootDetailsVo {
  id: string; // 主键ID
  name: string; // 租户名称
  expireTime: string; // 到期时间(null长期)
  remark: string; // 备注
  status: number; // 状态 1:正常，0:停用
  isSystem: number; // 内置租户 0:否 1:是
  packIds: string[]; // 已绑定的菜单包ID列表
  _adminUsername: string; // [前端] 管理员账号
  _adminPassword: string; // [前端] 管理员密码
}

/**
 * 更新租户绑定菜单包Dto
 */
export interface UpdateRootRpDto {
  rootId: string; // 租户ID
  packIds: string[]; // 菜单包ID列表
}

/**
 * 新增租户Dto
 */
export interface AddCoreRootDto {
  name: string; // 租户名称
  expireTime: string; // 到期时间(null长期)
  remark: string; // 备注
  status: number; // 状态 1:正常，0:停用
  adminUsername: string; // 管理员账号
  adminPassword: string; // 管理员密码
}

/**
 * 编辑租户Dto
 */
export interface EditCoreRootDto {
  id: string; // 主键ID
  name: string; // 租户名称
  expireTime: string; // 到期时间(null长期)
  // adminUsername: string; // 管理员账号
  // adminPassword: string; // 管理员密码
  remark: string; // 备注
  status: number; // 状态 1:正常，0:停用
}

export default {
  /**
   * 获取租户列表
   */
  getCoreRootList: async (dto: GetCoreRootListDto): Promise<PageResult<GetCoreRootListVo>> => {
    return await Http.postEntity<PageResult<GetCoreRootListVo>>("/coreRoot/getCoreRootList", dto);
  },

  /**
   * 获取租户详情
   */
  getCoreRootDetails: async (dto: CommonIdDto): Promise<GetCoreRootDetailsVo> => {
    const result = await Http.postEntity<Result<GetCoreRootDetailsVo>>("/coreRoot/getCoreRootDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增租户
   */
  addCoreRoot: async (dto: AddCoreRootDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/coreRoot/addCoreRoot", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑租户
   */
  editCoreRoot: async (dto: EditCoreRootDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/coreRoot/editCoreRoot", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除租户
   */
  removeCoreRoot: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/coreRoot/removeCoreRoot", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 更新租户绑定菜单包
   */
  updateRootRp: async (dto: UpdateRootRpDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/coreRoot/updateRootRp", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
