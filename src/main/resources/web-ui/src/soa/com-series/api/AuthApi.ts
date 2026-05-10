import Http from "@/commons/Http.ts";
import type Result from "@/commons/model/Result.ts";
import type { AxiosResponse } from "axios";

export interface GetCurrentUserProfilePermissionVo {
  code: string; // 权限代码
  name: string; // 权限名称
}

export interface GetCurrentUserProfile {
  id: string; // 用户ID
  username: string; // 用户名
  nickname: string; // 用户昵称
  gender: number; // 用户性别
  phone: string; // 用户手机号
  email: string; // 用户邮箱
  status: number; // 用户状态
  createTime: string; // 创建时间
  lastLoginTime: string; // 最后登录时间
  isSystem: number; // 是否为系统内置用户 0:否 1:是
  avatarAttachId: string; // 用户头像附件ID
  groups: string[]; // 拥有的用户组
  permissions: GetCurrentUserProfilePermissionVo[]; // 用户权限列表
}

/**
 * 获取用户信息DTO
 */
export interface GetUserProfileDto {
  forceUpdate?: number; // 是否强制刷新缓存 0:否 1:是
}

export default {
  /**
   * 获取当前用户信息
   */
  getUserProfile: async (dto: GetUserProfileDto = {}): Promise<GetCurrentUserProfile> => {
    const result = await Http.postEntity<Result<GetCurrentUserProfile>>("/profile/getUserProfile", dto);
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 用户注销
   */
  logout: async (): Promise<Result<string>> => {
    return await Http.postRaw<string>("/auth/logout", {});
  },

  /**
   * 更新当前用户头像
   */
  updateUserAvatar: async (file: File): Promise<void> => {
    const formData = new FormData();
    formData.append("file", file);
    const response: AxiosResponse = await Http.axios().post("/profile/updateUserAvatar", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      responseType: "blob",
    });
    if (response.status === 200) {
      return;
    }
    throw new Error("头像上传失败");
  },

  /**
   * 用户更改密码
   */
  changePassword: async (oldPassword: string, newPassword: string): Promise<void> => {
    const result = await Http.postEntity<Result<string>>("/profile/changePassword", {
      oldPassword: oldPassword,
      newPassword: newPassword,
    });
    if (result.code == 0) {
      return;
    }
    throw new Error(result.message);
  },
};
