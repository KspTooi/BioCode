import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

export interface UserGroupVo {
  id: string; // 用户组ID
  name: string; // 用户组名称
  remark: string; // 用户组描述
  seq: number; // 排序号
  isSystem: boolean; // 是否为系统内置组
  hasGroup: boolean; // 用户是否属于此组
  status: number; // 组状态 0:禁用，1:启用
}

export interface UserPermissionVo {
  id: string; // 权限ID
  permKey: string; // 权限键
  name: string; // 权限名称
  remark: string; // 权限描述
  isSystem: boolean; // 是否为系统内置权限
}

export interface GetUserListDto extends PageQuery {
  rootName?: string | null; // 租户名称
  username?: string; // 用户名查询
  nickname?: string; // 昵称查询
  phone?: string; // 手机号查询
  orgId?: string | null; // 组织架构ID
  status?: number | null; // 用户状态 0:封禁 1:正常
  userIds?: string[]; // 用户ID列表
}

export interface GetUserListVo {
  id: string; // 用户ID
  username: string; // 用户名
  nickname: string; // 用户昵称
  gender: number; // 用户性别
  phone: string; // 用户手机号
  email: string; // 用户邮箱
  createTime: string; // 创建时间
  lastLoginTime: string; // 最后登录时间
  status: number; // 用户状态
  rootName?: string; // 所属租户名称
  orgName?: string; // 所属企业名称
  deptName?: string; // 部门名称
  isSystem: number; // 是否为系统内置用户 0:否 1:是
}

export interface GetUserDetailsVo {
  id: string; // 用户ID
  username: string; // 用户名
  nickname: string; // 昵称
  gender: number; // 用户性别
  phone: string; // 用户手机号
  email: string; // 用户邮箱
  status: number; // 用户状态
  createTime: string; // 创建时间
  lastLoginTime: string; // 最后登录时间
  orgId: string; // 所属组织机构ID
  isSystem: number; // 是否为系统内置用户 0:否 1:是
  groups: UserGroupVo[]; // 用户组列表
  permissions: UserPermissionVo[]; // 用户权限列表
}

export interface AddUserDto {
  username: string; // 用户名
  password: string; // 用户密码
  nickname?: string; // 昵称
  gender: number; // 性别 0:男 1:女 2:不愿透露
  phone?: string; // 用户手机号
  email?: string; // 用户邮箱
  status?: number; // 用户状态：0-封禁，1-正常
  orgId?: string; // 所属组织机构ID
  groupIds?: string[]; // 用户组ID列表
}

export interface EditUserDto {
  id: string; // 用户ID
  username: string; // 用户名
  password?: string; // 用户密码，编辑时可选
  nickname?: string; // 昵称
  gender: number; // 性别 0:男 1:女 2:不愿透露
  phone?: string; // 用户手机号
  email?: string; // 用户邮箱
  status?: number; // 用户状态：0-封禁，1-正常
  orgId?: string; // 所属组织机构ID
  groupIds?: string[]; // 用户组ID列表
}

export interface BatchEditUserDto {
  ids: string[]; // 用户ID列表（后端Long类型对应前端string）
  kind: number; // 批量操作类型 0:启用 1:封禁 2:删除 3:变更部门
  orgId?: string; // 变更组织机构ID 当kind为3时必填
}

export default {
  /**
   * 获取用户列表
   */
  getUserList: async (dto: GetUserListDto): Promise<PageResult<GetUserListVo>> => {
    return await Http.postEntity<PageResult<GetUserListVo>>("/user/getUserList", dto);
  },

  /**
   * 获取用户详情
   */
  getUserDetails: async (dto: CommonIdDto): Promise<GetUserDetailsVo> => {
    const result = await Http.postEntity<Result<GetUserDetailsVo>>("/user/getUserDetails", dto);
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增用户
   */
  addUser: async (dto: AddUserDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/user/addUser", dto);
  },

  /**
   * 编辑用户
   */
  editUser: async (dto: EditUserDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/user/editUser", dto);
  },

  /**
   * 批量编辑用户
   */
  batchEditUser: async (dto: BatchEditUserDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/user/batchEditUser", dto);
  },

  /**
   * 删除用户
   */
  removeUser: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/user/removeUser", dto);
    if (result.code == 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
