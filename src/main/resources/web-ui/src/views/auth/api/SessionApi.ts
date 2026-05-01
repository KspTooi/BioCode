import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

export interface GetSessionListDto extends PageQuery {
  userName: string | null; // 用户名
}

export interface GetSessionListVo {
  id: string; // 会话ID
  username: string; // 用户名
  createTime: string; // 登入时间
  rsMax: number; // 最大RowScope等级 0:全部权限 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:本部门 50:仅本人 60:指定组织
  expiresAt: string; // 过期时间
}

export interface GetSessionDetailsVo {
  id: string; // 会话ID
  username: string; // 用户名
  createTime: string; // 登入时间
  expiresAt: string; // 过期时间
  permissions: string[]; // 权限节点
  rsMax: number; // 最大RowScope等级 0:全部权限 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:本部门 50:仅本人 60:指定组织
  rsDeptNames: string[]; // RowScope允许访问的部门名称列表
}

export default {
  /**
   * 获取会话列表
   */
  getSessionList: async (dto: GetSessionListDto): Promise<PageResult<GetSessionListVo>> => {
    return await Http.postEntity<PageResult<GetSessionListVo>>("/session/getSessionList", dto);
  },

  /**
   * 获取会话详情
   */
  getSessionDetails: async (dto: CommonIdDto): Promise<GetSessionDetailsVo> => {
    const result = await Http.postEntity<Result<GetSessionDetailsVo>>("/session/getSessionDetails", dto);
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 关闭会话
   */
  closeSession: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/session/closeSession", dto);
    if (result.code == 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
