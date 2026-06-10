import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";

export interface GetAacpFuncListDto extends PageQuery {
  name: string | null; //微函数名称
  code: string | null; //微函数标识
  description: string | null; //意图词
}

export interface GetAacpFuncListVo {
  id: string; //主键ID
  name: string; //微函数名称
  code: string; //微函数标识
  description: string; //意图词
}

export interface AddAacpFuncDto {
  name: string | null; //微函数名称
  code: string | null; //微函数标识
  description: string | null; //意图词
  schema: string | null; //入参规范
  target: string | null; //调用目标Bean
  remark: string | null; //备注
}

export interface EditAacpFuncDto {
  id: string | null; //主键ID
  name: string | null; //微函数名称
  code: string | null; //微函数标识
  description: string | null; //意图词
  schema: string | null; //入参规范
  target: string | null; //调用目标Bean
  remark: string | null; //备注
}

export interface GetAacpFuncDetailsVo {
  id: string | null; //主键ID
  name: string | null; //微函数名称
  code: string | null; //微函数标识
  description: string | null; //意图词
  schema: string | null; //入参规范
  target: string | null; //调用目标Bean
  remark: string | null; //备注
}

/**
 * 已注册微函数列表VO
 */
export interface GetMicroFuncListVo {
  target: string; //微函数标识
  name: string; //微函数名称
  description: string; //微函数描述
  parameterCount: number; //参数数量
  parameterTypes: string[]; //参数类型列表
}

export default {
  /**
   * 获取微函数列表
   * @param dto 查询条件
   * @returns 微函数列表
   */
  getAacpFuncList: async (dto: GetAacpFuncListDto): Promise<RestPageableView<GetAacpFuncListVo>> => {
    const ret = await Http.postEntity<RestPageableView<GetAacpFuncListVo>>("/aacpFunc/getAacpFuncList", dto);
    return ret;
  },

  /**
   * 添加微函数
   * @param dto 微函数信息
   * @returns 操作结果
   */
  addAacpFunc: async (dto: AddAacpFuncDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpFunc/addAacpFunc", dto);
  },

  /**
   * 编辑微函数
   * @param dto 微函数信息
   * @returns 操作结果
   */
  editAacpFunc: async (dto: EditAacpFuncDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpFunc/editAacpFunc", dto);
  },

  /**
   * 获取微函数详情
   * @param id 微函数ID
   * @returns 微函数详情
   */
  getAacpFuncDetails: async (id: string): Promise<GetAacpFuncDetailsVo> => {
    const ret = await Http.postEntity<Result<GetAacpFuncDetailsVo>>("/aacpFunc/getAacpFuncDetails", { id: id } as CommonIdDto);
    return ret.data;
  },

  /**
   * 删除微函数
   * @param id 微函数ID
   * @returns 操作结果
   */
  removeAacpFunc: async (id: string): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpFunc/removeAacpFunc", { id: id } as CommonIdDto);
  },

  /**
   * 获取已注册微函数列表
   * @returns 已注册微函数列表
   */
  getMicroFuncList: async (): Promise<Result<GetMicroFuncListVo[]>> => {
    return await Http.postEntity<Result<GetMicroFuncListVo[]>>("/aacpFunc/getMicroFuncList", {});
  },
};
