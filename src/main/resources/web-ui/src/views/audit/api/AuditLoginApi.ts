import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

export interface GetAuditLoginListDto extends PageQuery {
  username?: string; // 用户名查询
  status?: number | null; // 状态查询: 0-成功, 1-失败
}

export interface GetAuditLoginListVo {
  id: string; // 日志ID
  userId: number; // 用户ID
  username: string; // 用户账号
  loginKind: number; // 登录方式 0:用户名密码 1:PAT登录
  ipAddr: string; // 登录 IP
  location: string; // IP 归属地
  browser: string; // 浏览器/客户端指纹
  os: string; // 操作系统
  status: number; // 状态: 0:成功 1:失败
  message: string; // 提示消息
  createTime: string; // 创建时间
}

export interface GetAuditLoginDetailsVo {
  id: string; // 日志ID
  userId: number; // 用户ID
  username: string; // 用户账号
  loginKind: number; // 登录方式 0:用户名密码 1:PAT登录
  ipAddr: string; // 登录 IP
  location: string; // IP 归属地
  browser: string; // 浏览器/客户端指纹
  os: string; // 操作系统
  status: number; // 状态: 0:成功 1:失败
  message: string; // 提示消息
  createTime: string; // 创建时间
}

export default {
  /**
   * 获取登录日志列表
   */
  getAuditLoginList: async (dto: GetAuditLoginListDto): Promise<PageResult<GetAuditLoginListVo>> => {
    return await Http.postEntity<PageResult<GetAuditLoginListVo>>("/auditLogin/getAuditLoginList", dto);
  },

  /**
   * 获取登录日志详情
   */
  getAuditLoginDetails: async (dto: CommonIdDto): Promise<GetAuditLoginDetailsVo> => {
    const result = await Http.postEntity<Result<GetAuditLoginDetailsVo>>("/auditLogin/getAuditLoginDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 删除登录日志
   */
  removeAuditLogin: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/auditLogin/removeAuditLogin", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
