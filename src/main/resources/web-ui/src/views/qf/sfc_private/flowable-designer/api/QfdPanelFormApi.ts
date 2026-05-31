import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";

/**
 * 查询设计器表单字段列表 Dto
 */
export interface GetQfdPanelFormFieldListDto extends PageQuery {
  formId: string; // 业务表单 ID
}

/**
 * 设计器表单字段行 Vo
 */
export interface GetQfdPanelFormFieldListVo {
  id: string; // 主键 ID
  formId: string; // 业务表单 ID
  fieldName: string; // 字段名
  remark: string; // 备注
}

/**
 * 表单配置面板数据（与 BPMN qfe:utFormAllowEditFields 对应）
 */
export interface QfdPanelFormDetailsVo {
  utFormAllowEditFields: string[]; // 可编辑字段 fieldName 列表
}

export default {
  /**
   * 按业务表单 ID 获取字段列表（供设计器勾选可编辑字段）
   */
  getFormFieldList: async (dto: GetQfdPanelFormFieldListDto): Promise<PageResult<GetQfdPanelFormFieldListVo>> => {
    return await Http.postEntity<PageResult<GetQfdPanelFormFieldListVo>>("/QfBizFormField/getQfBizFormFieldList", dto);
  },
};
