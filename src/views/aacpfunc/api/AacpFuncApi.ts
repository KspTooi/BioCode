import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询微函数列表Dto
 */
export interface GetAacpFuncListDto extends PageQuery {
  name?: string; // 微函数名称
  code?: string; // 微函数标识
  description?: string; // 意图词
}

/**
 * 查询微函数列表Vo
 */
export interface GetAacpFuncListVo {
  id: string; // 主键ID
  name: string; // 微函数名称
  code: string; // 微函数标识
  description: string; // 意图词
}

/**
 * 查询微函数详情Vo
 */
export interface GetAacpFuncDetailsVo {
  id: string; // 主键ID
  name: string; // 微函数名称
  code: string; // 微函数标识
  description: string; // 意图词
  schema: string; // 入参规范
  target: string; // 调用目标Bean
  remark: string; // 备注
}

/**
 * 新增微函数Dto
 */
export interface AddAacpFuncDto {
  name: string; // 微函数名称
  code: string; // 微函数标识
  description: string; // 意图词
  schema: string; // 入参规范
  target: string; // 调用目标Bean
  remark: string; // 备注
}

/**
 * 编辑微函数Dto
 */
export interface EditAacpFuncDto {
  id: string; // 主键ID
  name: string; // 微函数名称
  code: string; // 微函数标识
  description: string; // 意图词
  schema: string; // 入参规范
  target: string; // 调用目标Bean
  remark: string; // 备注
}

export default {
  /**
   * 获取微函数列表
   */
  getAacpFuncList: async (dto: GetAacpFuncListDto): Promise<PageResult<GetAacpFuncListVo>> => {
    return await Http.postEntity<PageResult<GetAacpFuncListVo>>("/aacpFunc/getAacpFuncList", dto);
  },

  /**
   * 获取微函数详情
   */
  getAacpFuncDetails: async (dto: CommonIdDto): Promise<GetAacpFuncDetailsVo> => {
    const result = await Http.postEntity<Result<GetAacpFuncDetailsVo>>("/aacpFunc/getAacpFuncDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增微函数
   */
  addAacpFunc: async (dto: AddAacpFuncDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpFunc/addAacpFunc", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑微函数
   */
  editAacpFunc: async (dto: EditAacpFuncDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpFunc/editAacpFunc", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除微函数
   */
  removeAacpFunc: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpFunc/removeAacpFunc", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
