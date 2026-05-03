import Http from "@/commons/Http.ts";
import type Result from "@/commons/model/Result.ts";

export interface UserLoginDto {
  username: string; // 用户名
  password: string; // 密码
}

export interface UserLoginVo {
  userId: string; // 用户ID
  username: string; // 用户名
  nickname: string; // 用户昵称
  gender: number; // 用户性别
  phone: string; // 用户手机号
  email: string; // 用户邮箱
  status: number; // 用户状态 0:封禁 1:正常
  lastLoginTime: string; // 最后登录时间 格式:yyyy-MM-dd HH:mm:ss
  avatarAttachId: number; // 用户头像附件ID
  rootId: string; // AUS字段: 租户ID
  orgId: string; // AUS字段: 直属企业ID
  deptId: string; // AUS字段: 直属部门ID
  rootName: string; // AUS字段: 租户名称
  orgName: string; // AUS字段: 直属企业名称
  deptName: string; // AUS字段: 直属部门名称
  isSystem: number; // 是否为系统内置用户 0:否 1:是
  createTime: string; // 创建时间
  sessionId: string; // 用户会话ID
  authorities: string[]; // 权限码
  appVersion: string; // 应用版本号
  appVersionNumeric: string; // 应用版本号(数字化)
}

export default {
  /**
   * 用户登录
   */
  userLogin: async (dto: UserLoginDto): Promise<Result<UserLoginVo>> => {
    return await Http.postEntity<Result<UserLoginVo>>("/auth/userLogin", dto);
  },
};
