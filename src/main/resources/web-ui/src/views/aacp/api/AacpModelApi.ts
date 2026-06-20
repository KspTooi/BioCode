import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询模型变体列表Dto
 */
export interface GetModelListDto extends PageQuery {
  name?: string; // 模型变体名称
  code?: string; // 模型标识
  kind?: number; // 类型 0:文本 1:图形 2:音频 3:多模态
  status?: number; // 状态 0:禁用 1:启用
}

/**
 * 查询模型变体列表Vo
 */
export interface GetModelListVo {
  id: string; // 主键ID
  name: string; // 模型变体名称
  code: string; // 模型标识
  kind: number; // 类型 0:文本 1:图形 2:音频 3:多模态
  maxContext: number; // 最大上下文长度
  maxOutputToken: number; // 最大输出词元
  apiReasoning: number; // 推理 0:不支持 1:支持
  apiReasoningEffort: number; // 推理强度 0:关 1:低 2:中 3:高 4:极高
  fincInput: string; // 输入单价
  fincInputCached: string; // 输入单价(缓存)
  fincOutput: string; // 输出单价
  testTtfb: number; // 测试首字响应时间 MS
  testRate: number; // 测试响应速率 T/S
  seq: number; // 排序
  status: number; // 状态 0:禁用 1:启用
  createTime: string; // 创建时间
}

/**
 * 查询模型变体详情Vo
 */
export interface GetModelDetailsVo {
  id: string; // 主键ID
  name: string; // 模型变体名称
  code: string; // 模型标识
  kind: number; // 类型 0:文本 1:图形 2:音频 3:多模态
  maxContext: number; // 最大上下文长度
  maxOutputToken: number; // 最大输出词元
  apiReasoning: number; // 推理 0:不支持 1:支持
  apiReasoningEffort: number; // 推理强度 0:关 1:低 2:中 3:高 4:极高
  apiAppendParam: string; // 附加参数
  apiAppendHeaders: string; // 附加请求头
  fincInput: string; // 输入单价
  fincInputCached: string; // 输入单价(缓存)
  fincOutput: string; // 输出单价
  testTtfb: number; // 测试首字响应时间 MS
  testRate: number; // 测试响应速率 T/S
  testTime: string; // 最后测试时间
  remark: string; // 备注
  seq: number; // 排序
  status: number; // 状态 0:禁用 1:启用
}

/**
 * 新增模型变体Dto
 */
export interface AddModelDto {
  name: string; // 模型变体名称
  code: string; // 模型标识
  kind: number; // 类型 0:文本 1:图形 2:音频 3:多模态
  maxContext: number; // 最大上下文长度
  maxOutputToken: number; // 最大输出词元
  apiReasoning: number; // 推理 0:不支持 1:支持
  apiReasoningEffort: number; // 推理强度 0:关 1:低 2:中 3:高 4:极高
  apiAppendParam: string; // 附加参数
  apiAppendHeaders: string; // 附加请求头
  fincInput: string; // 输入单价
  fincInputCached: string; // 输入单价(缓存)
  fincOutput: string; // 输出单价
  testTtfb: number; // 测试首字响应时间 MS
  testRate: number; // 测试响应速率 T/S
  testTime: string; // 最后测试时间
  remark: string; // 备注
  seq: number; // 排序
  status: number; // 状态 0:禁用 1:启用
}

/**
 * 编辑模型变体Dto
 */
export interface EditModelDto {
  id: string; // 主键ID
  name: string; // 模型变体名称
  code: string; // 模型标识
  kind: number; // 类型 0:文本 1:图形 2:音频 3:多模态
  maxContext: number; // 最大上下文长度
  maxOutputToken: number; // 最大输出词元
  apiReasoning: number; // 推理 0:不支持 1:支持
  apiReasoningEffort: number; // 推理强度 0:关 1:低 2:中 3:高 4:极高
  apiAppendParam: string; // 附加参数
  apiAppendHeaders: string; // 附加请求头
  fincInput: string; // 输入单价
  fincInputCached: string; // 输入单价(缓存)
  fincOutput: string; // 输出单价
  testTtfb: number; // 测试首字响应时间 MS
  testRate: number; // 测试响应速率 T/S
  testTime: string; // 最后测试时间
  remark: string; // 备注
  seq: number; // 排序
  status: number; // 状态 0:禁用 1:启用
}

export default {
  /**
   * 获取模型变体列表
   */
  getModelList: async (dto: GetModelListDto): Promise<PageResult<GetModelListVo>> => {
    return await Http.postEntity<PageResult<GetModelListVo>>("/model/getModelList", dto);
  },

  /**
   * 获取模型变体详情
   */
  getModelDetails: async (dto: CommonIdDto): Promise<GetModelDetailsVo> => {
    const result = await Http.postEntity<Result<GetModelDetailsVo>>("/model/getModelDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增模型变体
   */
  addModel: async (dto: AddModelDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/model/addModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑模型变体
   */
  editModel: async (dto: EditModelDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/model/editModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除模型变体
   */
  removeModel: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/model/removeModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
