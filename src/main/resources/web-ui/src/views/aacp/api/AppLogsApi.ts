import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询模型调用记录列表Dto
 */
export interface GetAppLogsListDto extends PageQuery {
  appId?: string; // 应用ID
  providerId?: string; // 供应商ID
  modelId?: string; // 模型变体ID
  inputToken?: number; // 输入词元
  outputToken?: number; // 输出词元
  cost?: string; // 消耗金额
  startTime?: string; // 发起时间
  endTime?: string; // 结束时间
  durationMs?: number; // 总耗时MS
  ttfbMs?: number; // 首字响应时间
  statusCode?: string; // HTTP状态码
  clientIp?: string; // 客户端IP
}

/**
 * 查询模型调用记录列表Vo
 */
export interface GetAppLogsListVo {
  appId: string; // 应用ID
  providerId: string; // 供应商ID
  modelId: string; // 模型变体ID
  inputToken: number; // 输入词元
  outputToken: number; // 输出词元
  cost: string; // 消耗金额
  startTime: string; // 发起时间
  endTime: string; // 结束时间
  durationMs: number; // 总耗时MS
  ttfbMs: number; // 首字响应时间
  statusCode: string; // HTTP状态码
  clientIp: string; // 客户端IP
}

/**
 * 查询模型调用记录详情Vo
 */
export interface GetAppLogsDetailsVo {
  appId: string; // 应用ID
  providerId: string; // 供应商ID
  modelId: string; // 模型变体ID
  inputToken: number; // 输入词元
  outputToken: number; // 输出词元
  cost: string; // 消耗金额
  startTime: string; // 发起时间
  endTime: string; // 结束时间
  durationMs: number; // 总耗时MS
  ttfbMs: number; // 首字响应时间
  statusCode: string; // HTTP状态码
  clientIp: string; // 客户端IP
}

/**
 * 新增模型调用记录Dto
 */
export interface AddAppLogsDto {
}

/**
 * 编辑模型调用记录Dto
 */
export interface EditAppLogsDto {
  id: string; // 主键ID
}

export default {
  /**
   * 获取模型调用记录列表
   */
  getAppLogsList: async (dto: GetAppLogsListDto): Promise<PageResult<GetAppLogsListVo>> => {
    return await Http.postEntity<PageResult<GetAppLogsListVo>>("/appLogs/getAppLogsList", dto);
  },

  /**
   * 获取模型调用记录详情
   */
  getAppLogsDetails: async (dto: CommonIdDto): Promise<GetAppLogsDetailsVo> => {
    const result = await Http.postEntity<Result<GetAppLogsDetailsVo>>("/appLogs/getAppLogsDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增模型调用记录
   */
  addAppLogs: async (dto: AddAppLogsDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/appLogs/addAppLogs", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑模型调用记录
   */
  editAppLogs: async (dto: EditAppLogsDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/appLogs/editAppLogs", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除模型调用记录
   */
  removeAppLogs: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/appLogs/removeAppLogs", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
