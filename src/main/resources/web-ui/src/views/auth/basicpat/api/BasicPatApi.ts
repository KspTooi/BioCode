import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询基本PAT列表Dto
 */
export interface GetBasicPatListDto extends PageQuery {
  name?: string; // PAT名称
  status?: number; // 状态: 0:禁用 1:启用
}

/**
 * 查询基本PAT列表Vo
 */
export interface GetBasicPatListVo {
  id: string; // 主键ID
  name: string; // PAT名称
  patPt: string; // 部分明文
  expire: string; // 过期时间
  status: number; // 状态: 0:禁用 1:启用
  createTime: string; // 创建时间
}

/**
 * 查询基本PAT详情Vo
 */
export interface GetBasicPatDetailsVo {
  id: string; // 主键ID
  name: string; // PAT名称
  patPt: string; // 部分明文
  expire: string; // 过期时间
  status: number; // 状态: 0:禁用 1:启用
  createTime: string; // 创建时间
}

/**
 * 新增基本PATDto
 */
export interface AddBasicPatDto {
  name: string; // PAT名称
  expire?: string; // 过期时间 yyyy-MM-dd HH:mm:ss
}

/**
 * 编辑基本PATDto
 */
export interface EditBasicPatDto {
  id: string; // 主键ID
  name: string; // PAT名称
  status: number; // 状态: 0:禁用 1:启用
}

export default {
  /**
   * 获取基本PAT列表
   */
  getBasicPatList: async (dto: GetBasicPatListDto): Promise<PageResult<GetBasicPatListVo>> => {
    return await Http.postEntity<PageResult<GetBasicPatListVo>>("/basicPat/getBasicPatList", dto);
  },

  /**
   * 获取基本PAT详情
   */
  getBasicPatDetails: async (dto: CommonIdDto): Promise<GetBasicPatDetailsVo> => {
    const result = await Http.postEntity<Result<GetBasicPatDetailsVo>>("/basicPat/getBasicPatDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增基本PAT
   */
  addBasicPat: async (dto: AddBasicPatDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/basicPat/addBasicPat", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑基本PAT
   */
  editBasicPat: async (dto: EditBasicPatDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/basicPat/editBasicPat", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除基本PAT
   */
  removeBasicPat: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/basicPat/removeBasicPat", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
