import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询流程表单字段配置列表Dto
 */
export interface GetQfBizFormFieldListDto extends PageQuery {
  formId?: string; // 业务表ID
}

/**
 * 查询流程表单字段配置列表Vo
 */
export interface GetQfBizFormFieldListVo {
  id: string; // 主键ID
  formId: string; // 业务表ID
  fieldName: string; // 字段名
  remark: string; // 备注
}

/**
 * 查询流程表单字段配置详情Vo
 */
export interface GetQfBizFormFieldDetailsVo {
  id: string; // 主键ID
  formId: string; // 业务表ID
  fieldName: string; // 字段名
  remark: string; // 备注
}

/**
 * 新增流程表单字段配置Dto
 */
export interface AddQfBizFormFieldDto {
  formId: string; // 业务表ID
  fieldName: string; // 字段名
  remark: string; // 备注
}

/**
 * 编辑流程表单字段配置Dto
 */
export interface EditQfBizFormFieldDto {
  id: string; // 主键ID
  formId: string; // 业务表ID
  fieldName: string; // 字段名
  remark: string; // 备注
}

export default {
  /**
   * 获取流程表单字段配置列表
   */
  getBizFormFieldList: async (dto: GetQfBizFormFieldListDto): Promise<PageResult<GetQfBizFormFieldListVo>> => {
    return await Http.postEntity<PageResult<GetQfBizFormFieldListVo>>("/QfBizFormField/getQfBizFormFieldList", dto);
  },

  /**
   * 获取流程表单字段配置详情
   */
  getBizFormFieldDetails: async (dto: CommonIdDto): Promise<GetQfBizFormFieldDetailsVo> => {
    const result = await Http.postEntity<Result<GetQfBizFormFieldDetailsVo>>("/QfBizFormField/getQfBizFormFieldDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增流程表单字段配置
   */
  addBizFormField: async (dto: AddQfBizFormFieldDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/QfBizFormField/addQfBizFormField", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑流程表单字段配置
   */
  editBizFormField: async (dto: EditQfBizFormFieldDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/QfBizFormField/editQfBizFormField", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除流程表单字段配置
   */
  removeBizFormField: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/QfBizFormField/removeQfBizFormField", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
