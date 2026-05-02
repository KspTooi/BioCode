import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";

export interface GetRequestListDto extends PageQuery {
  relayServerId: string | null; //中继服务器ID
  requestId: string | null; //请求ID
  method: string | null; //请求方法
  url: string | null; //请求URL
  source: string | null; //来源
  status: number | null; //状态 0:正常 1:HTTP失败 2:业务失败 3:连接超时
  startTime: string | null; //开始时间
  endTime: string | null; //结束时间
  replay: number | null; //是否重放 0:否 1:是
}

export interface GetRequestListVo {
  id: number; //数据ID
  requestId: string; //请求ID
  method: string; //请求方法
  url: string; //请求URL
  source: string; //来源
  requestBody: object; //请求体
  responseBody: object; //响应体
  status: number; //状态 0:正常 1:HTTP失败 2:业务失败 3:连接超时
  requestTime: string; //请求时间
  responseTime: string; //响应时间
  replayCount: number; //重放次数
}

export interface GetRequestDetailsVo {
  id: number; //数据ID
  requestId: string; //请求ID
  method: string; //请求方法
  url: string; //请求URL
  source: string; //来源
  requestHeaders: string; //请求头
  requestBodyLength: number; //请求体长度
  requestBodyType: string; //请求体类型
  requestBody: string; //请求体
  responseHeaders: string; //响应头
  responseBodyLength: number; //响应体长度
  responseBodyType: string; //响应体类型
  responseBody: string; //响应体
  statusCode: number; //HTTP响应状态码
  redirectUrl: string; //重定向URL
  status: number; //状态 0:正常 1:HTTP失败 2:业务失败 3:连接超时
  requestTime: string; //请求时间
  responseTime: string; //响应时间
}

export default {
  getRequestList: async (dto: GetRequestListDto): Promise<RestPageableView<GetRequestListVo>> => {
    return await Http.postEntity<RestPageableView<GetRequestListVo>>("/request/getRequestList", dto);
  },

  getRequestDetails: async (id: string): Promise<GetRequestDetailsVo> => {
    const res = await Http.postEntity<Result<GetRequestDetailsVo>>("/request/getRequestDetails", { id: id });
    if (res.code === 0) {
      return res.data;
    }
    throw new Error(res.message);
  },
};
