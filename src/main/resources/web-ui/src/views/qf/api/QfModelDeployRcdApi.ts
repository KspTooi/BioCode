import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询流程模型部署历史列表Dto
 */
export interface GetQfModelDeployRcdListDto extends PageQuery {
  name?: string; // 模型名称
  code?: string; // 模型编码
  status?: number; // 部署状态 0:正常 1:部署失败 2:已挂起
}

/**
 * 查询流程模型部署历史列表Vo
 */
export interface GetQfModelDeployRcdListVo {
  id: string; // 主键ID
  modelId: string; // 模型ID
  name: string; // 模型名称
  code: string; // 模型编码
  version: number; // 模型版本号
  status: number; // 部署状态 0:正常 1:部署失败 2:已挂起
  createTime: string; // 部署时间
}

/**
 * 查询流程模型部署历史详情Vo
 */
export interface GetQfModelDeployRcdDetailsVo {
  id: string; // 主键ID
  bpmnXml: string; // 模型BPMN XML
}

export default {
  /**
   * 获取流程模型部署历史列表
   */
  getQfModelDeployRcdList: async (dto: GetQfModelDeployRcdListDto): Promise<PageResult<GetQfModelDeployRcdListVo>> => {
    return await Http.postEntity<PageResult<GetQfModelDeployRcdListVo>>("/qfModelDeployRcd/getQfModelDeployRcdList", dto);
  },

  /**
   * 获取流程模型部署历史详情
   */
  getQfModelDeployRcdDetails: async (dto: CommonIdDto): Promise<GetQfModelDeployRcdDetailsVo> => {
    const result = await Http.postEntity<Result<GetQfModelDeployRcdDetailsVo>>(
      "/qfModelDeployRcd/getQfModelDeployRcdDetails",
      dto
    );
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 删除流程模型部署历史
   */
  removeQfModelDeployRcd: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfModelDeployRcd/removeQfModelDeployRcd", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 挂起流程模型部署
   */
  suspendQfModelDeployRcd: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfModelDeployRcd/suspendQfModelDeployRcd", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 激活流程模型部署
   */
  activateQfModelDeployRcd: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfModelDeployRcd/activateQfModelDeployRcd", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
