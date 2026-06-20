import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询聚合模板列表Dto
 */
export interface GetPolyTemplateListDto extends PageQuery {
  name?: string; // 模板名称
  code?: string; // 模板代码
  status?: number; // 状态 0:禁用 1:启用
}

/**
 * 查询聚合模板列表Vo
 */
export interface GetPolyTemplateListVo {
  id: string; // 主键ID
  name: string; // 模板名称
  code: string; // 模板代码
  seq: number; // 排序
  status: number; // 状态 0:禁用 1:启用
  createTime: string; // 创建时间
}

/**
 * 查询聚合模板详情Vo
 */
export interface GetPolyTemplateDetailsVo {
  id: string; // 主键ID
  name: string; // 模板名称
  code: string; // 模板代码
  seq: number; // 排序
  status: number; // 状态 0:禁用 1:启用
}

/**
 * 新增聚合模板Dto
 */
export interface AddPolyTemplateDto {
  name: string; // 模板名称
  code: string; // 模板代码
  seq: number; // 排序
  status: number; // 状态 0:禁用 1:启用
}

/**
 * 编辑聚合模板Dto
 */
export interface EditPolyTemplateDto {
  id: string; // 主键ID
  name: string; // 模板名称
  code: string; // 模板代码
  seq: number; // 排序
  status: number; // 状态 0:禁用 1:启用
}

export default {
  /**
   * 获取聚合模板列表
   */
  getPolyTemplateList: async (dto: GetPolyTemplateListDto): Promise<PageResult<GetPolyTemplateListVo>> => {
    return await Http.postEntity<PageResult<GetPolyTemplateListVo>>("/polyTemplate/getPolyTemplateList", dto);
  },

  /**
   * 获取聚合模板详情
   */
  getPolyTemplateDetails: async (dto: CommonIdDto): Promise<GetPolyTemplateDetailsVo> => {
    const result = await Http.postEntity<Result<GetPolyTemplateDetailsVo>>("/polyTemplate/getPolyTemplateDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增聚合模板
   */
  addPolyTemplate: async (dto: AddPolyTemplateDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyTemplate/addPolyTemplate", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑聚合模板
   */
  editPolyTemplate: async (dto: EditPolyTemplateDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyTemplate/editPolyTemplate", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除聚合模板
   */
  removePolyTemplate: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyTemplate/removePolyTemplate", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
