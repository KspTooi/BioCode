import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询聚合模板字段列表Dto
 */
export interface GetPolyTemplateFieldListDto extends PageQuery {
  polyTemplateId: string; // 聚合模板ID
  name?: string; // 字段名
}

/**
 * 查询聚合模板字段列表Vo
 */
export interface GetPolyTemplateFieldListVo {
  id: string; // 主键ID
  polyTemplateId: string; // 聚合模板ID
  name: string; // 字段名
  policyCrudJson: string[]; // 可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW
  policyQuery: number; // 查询策略 0:等于
  policyView: number; // 显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT
  seq: number; // 排序
}

/**
 * 查询聚合模板字段详情Vo
 */
export interface GetPolyTemplateFieldDetailsVo {
  id: string; // 主键ID
  polyTemplateId: string; // 聚合模板ID
  name: string; // 字段名
  policyCrudJson: string[]; // 可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW
  policyQuery: number; // 查询策略 0:等于
  policyView: number; // 显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT
  seq: number; // 排序
}

/**
 * 新增聚合模板字段Dto
 */
export interface AddPolyTemplateFieldDto {
  polyTemplateId: string; // 聚合模板ID
  name: string; // 字段名
  policyCrudJson: string[]; // 可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW
  policyQuery: number; // 查询策略 0:等于
  policyView: number; // 显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT
  seq: number; // 排序
}

/**
 * 编辑聚合模板字段Dto
 */
export interface EditPolyTemplateFieldDto {
  id: string; // 主键ID
  polyTemplateId: string; // 聚合模板ID
  name: string; // 字段名
  policyCrudJson: string[]; // 可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW
  policyQuery: number; // 查询策略 0:等于
  policyView: number; // 显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT
  seq: number; // 排序
}

export default {
  /**
   * 获取聚合模板字段列表
   */
  getPolyTemplateFieldList: async (dto: GetPolyTemplateFieldListDto): Promise<PageResult<GetPolyTemplateFieldListVo>> => {
    return await Http.postEntity<PageResult<GetPolyTemplateFieldListVo>>("/polyTemplateField/getPolyTemplateFieldList", dto);
  },

  /**
   * 获取聚合模板字段详情
   */
  getPolyTemplateFieldDetails: async (dto: CommonIdDto): Promise<GetPolyTemplateFieldDetailsVo> => {
    const result = await Http.postEntity<Result<GetPolyTemplateFieldDetailsVo>>("/polyTemplateField/getPolyTemplateFieldDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增聚合模板字段
   */
  addPolyTemplateField: async (dto: AddPolyTemplateFieldDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyTemplateField/addPolyTemplateField", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑聚合模板字段
   */
  editPolyTemplateField: async (dto: EditPolyTemplateFieldDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyTemplateField/editPolyTemplateField", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除聚合模板字段
   */
  removePolyTemplateField: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyTemplateField/removePolyTemplateField", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
