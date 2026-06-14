import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";

export interface GetMicroFuncListDto extends PageQuery {
  name: string | null; //微函数名称
  code: string | null; //微函数标识
  description: string | null; //意图词
}

export interface GetMicroFuncListVo {
  id: string; //主键ID
  name: string; //微函数名称
  code: string; //微函数标识
  description: string; //意图词
}

export interface AddMicroFuncDto {
  name: string | null; //微函数名称
  code: string | null; //微函数标识
  description: string | null; //意图词
  schema: string | null; //入参规范
  target: string | null; //目标方法
  remark: string | null; //备注
}

export interface EditMicroFuncDto {
  id: string | null; //主键ID
  name: string | null; //微函数名称
  code: string | null; //微函数标识
  description: string | null; //意图词
  schema: string | null; //入参规范
  target: string | null; //目标方法
  remark: string | null; //备注
}

export interface GetMicroFuncDetailsVo {
  id: string | null; //主键ID
  name: string | null; //微函数名称
  code: string | null; //微函数标识
  description: string | null; //意图词
  schema: string | null; //入参规范
  target: string | null; //目标方法
  remark: string | null; //备注
}

/**
 * 已注册微函数列表VO
 */
export interface GetMicroFuncRegistryVo {
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
  getMicroFuncList: async (dto: GetMicroFuncListDto): Promise<RestPageableView<GetMicroFuncListVo>> => {
    const ret = await Http.postEntity<RestPageableView<GetMicroFuncListVo>>("/microFunc/getMicroFuncList", dto);
    return ret;
  },

  /**
   * 添加微函数
   * @param dto 微函数信息
   * @returns 操作结果
   */
  addMicroFunc: async (dto: AddMicroFuncDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/microFunc/addMicroFunc", dto);
  },

  /**
   * 编辑微函数
   * @param dto 微函数信息
   * @returns 操作结果
   */
  editMicroFunc: async (dto: EditMicroFuncDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/microFunc/editMicroFunc", dto);
  },

  /**
   * 获取微函数详情
   * @param id 微函数ID
   * @returns 微函数详情
   */
  getMicroFuncDetails: async (id: string): Promise<GetMicroFuncDetailsVo> => {
    const ret = await Http.postEntity<Result<GetMicroFuncDetailsVo>>("/microFunc/getMicroFuncDetails", { id: id } as CommonIdDto);
    return ret.data;
  },

  /**
   * 删除微函数
   * @param id 微函数ID
   * @returns 操作结果
   */
  removeMicroFunc: async (id: string): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/microFunc/removeMicroFunc", { id: id } as CommonIdDto);
  },

  /**
   * 获取已注册微函数列表
   * @returns 已注册微函数列表
   */
  getMicroFuncRegistryList: async (): Promise<Result<GetMicroFuncRegistryVo[]>> => {
    return await Http.postEntity<Result<GetMicroFuncRegistryVo[]>>("/microFunc/getMicroFuncRegistryList", {});
  },

  /**
   * 同步微函数：从运行时注册容器自动补全缺失的数据库记录
   * @returns 同步结果描述
   */
  syncMicroFuncs: async (): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/microFunc/syncMicroFuncs", {});
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
