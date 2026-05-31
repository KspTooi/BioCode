import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询流程模型列表Dto
 */
export interface GetQfModelListDto extends PageQuery {
  groupName?: string; // 模型分组
  name?: string; // 模型名称
  code?: string; // 模型编码
  status?: number[]; // 模型状态 0:草稿 1:已部署 2:历史
}

/**
 * 查询流程模型列表Vo
 */
export interface GetQfModelListVo {
  id: string; // 主键ID
  groupName: string; // 模型分组
  name: string; // 模型名称
  code: string; // 模型编码
  bizFormName: string; // 关联业务表单名称
  bizFormCode: string; // 关联业务表单编码
  version: number; // 模型版本号
  status: number; // 模型状态 0:草稿 1:已部署 2:历史
  seq: number; // 排序
  createTime: string; // 创建时间
}

/**
 * 查询流程模型详情Vo
 */
export interface GetQfModelDetailsVo {
  id: string; // 主键ID
  groupId: string; // 模型分组ID
  formId: string; // 关联表单ID
  name: string; // 模型名称
  code: string; // 模型编码
  bpmnXml: string; // 模型BPMN XML
  seq: number; // 排序
}

/**
 * 新增流程模型Dto
 */
export interface AddQfModelDto {
  groupId?: string; // 模型分组ID
  formId?: string; // 关联表单ID
  name: string; // 模型名称
  code: string; // 模型编码
  seq: number; // 排序
}

/**
 * 编辑流程模型Dto
 */
export interface EditQfModelDto {
  id: string; // 主键ID
  groupId?: string; // 模型分组ID
  formId?: string; // 关联表单ID
  name: string; // 模型名称
  seq: number; // 排序
}

/**
 * 设计流程模型BPMNDto
 */
export interface DesignQfModelDto {
  id: string; // 流程模型ID
  bpmnXml: string; // BPMN XML
}

export default {
  /**
   * 获取流程模型列表
   */
  getQfModelList: async (dto: GetQfModelListDto): Promise<PageResult<GetQfModelListVo>> => {
    return await Http.postEntity<PageResult<GetQfModelListVo>>("/qfModel/getQfModelList", dto);
  },

  /**
   * 获取流程模型详情
   */
  getQfModelDetails: async (dto: CommonIdDto): Promise<GetQfModelDetailsVo> => {
    const result = await Http.postEntity<Result<GetQfModelDetailsVo>>("/qfModel/getQfModelDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 创建新版本流程模型
   */
  createNewVersionQfModel: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfModel/createNewVersion", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 新增流程模型
   */
  addQfModel: async (dto: AddQfModelDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfModel/addQfModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑流程模型
   */
  editQfModel: async (dto: EditQfModelDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfModel/editQfModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 设计流程模型BPMN
   */
  designQfModel: async (dto: DesignQfModelDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfModel/designQfModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除流程模型
   */
  removeQfModel: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfModel/removeQfModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 部署流程模型
   */
  deployQfModel: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfModel/deployQfModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
