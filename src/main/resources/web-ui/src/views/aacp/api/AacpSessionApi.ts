import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 在线会话列表Vo
 */
export interface GetOnlineSessionListVo {
  sessionId: string; //会话ID
  serverName: string; //枢纽名称
  serverCode: string; //枢纽编码
  connectTime: string; //连接时间
  status: number; //状态 0:初始化 1:活跃
  inboundCount: number; //总请求次数
}

export default {
  /**
   * 获取在线会话列表
   * @returns 在线会话列表
   */
  getOnlineSessionList: async (): Promise<RestPageableView<GetOnlineSessionListVo>> => {
    return await Http.postEntity<RestPageableView<GetOnlineSessionListVo>>("/aacpSession/getOnlineSessionList", {});
  },

  /**
   * 关闭在线会话
   * @param sessionIds 会话ID列表
   * @returns 操作结果
   */
  closeSession: async (sessionIds: string[]): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpSession/closeSession", { sessionIds });
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
