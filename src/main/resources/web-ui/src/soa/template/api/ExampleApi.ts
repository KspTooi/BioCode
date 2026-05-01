import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询列表DTO
 */
export interface GetExampleListDto extends PageQuery {
  name?: string; // 名称查询
  status?: number | null; // 状态查询
}

/**
 * 列表VO
 */
export interface GetExampleListVo {
  id: string; // ID
  name: string; // 名称
  description: string; // 描述
  status: number; // 状态: 0:启用 1:禁用
  createTime: string; // 创建时间
  updateTime: string; // 更新时间
}

/**
 * 详情VO
 */
export interface GetExampleDetailsVo {
  id: string; // ID
  name: string; // 名称
  description: string; // 描述
  status: number; // 状态: 0:启用 1:禁用
  createTime: string; // 创建时间
  updateTime: string; // 更新时间
}

/**
 * 创建DTO
 */
export interface AddExampleDto {
  name: string; // 名称
  description: string; // 描述
  status: number; // 状态: 0:启用 1:禁用
}

/**
 * 编辑DTO
 */
export interface EditExampleDto {
  id: string; // ID
  name: string; // 名称
  description: string; // 描述
  status: number; // 状态: 0:启用 1:禁用
}

export default {
  /**
   * 获取示例列表
   */
  getExampleList: async (dto: GetExampleListDto): Promise<PageResult<GetExampleListVo>> => {
    return await Http.postEntity<PageResult<GetExampleListVo>>("/example/getExampleList", dto);
  },

  /**
   * 获取示例详情
   */
  getExampleDetails: async (dto: CommonIdDto): Promise<GetExampleDetailsVo> => {
    const result = await Http.postEntity<Result<GetExampleDetailsVo>>("/example/getExampleDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 创建示例
   */
  addExample: async (dto: AddExampleDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/example/addExample", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑示例
   */
  editExample: async (dto: EditExampleDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/example/editExample", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除示例
   */
  removeExample: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/example/removeExample", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
