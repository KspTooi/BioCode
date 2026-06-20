import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询模型供应商列表Dto
 */
export interface GetProviderListDto extends PageQuery {
  name?: string; // 供应商名称
  code?: string; // 供应商代码
  status?: number; // 状态 0:禁用 1:启用
}

/**
 * 查询模型供应商列表Vo
 */
export interface GetProviderListVo {
  id: string; // 主键ID
  name: string; // 供应商名称
  code: string; // 供应商代码
  apiKind: number; // 接口类型 0:OpenAi 1:Anthropic
  apiHost: string; // 接口地址
  apiUrl: string; // 接口端点
  status: number; // 状态 0:禁用 1:启用
}

/**
 * 查询模型供应商详情Vo
 */
export interface GetProviderDetailsVo {
  id: string; // 主键ID
  name: string; // 供应商名称
  code: string; // 供应商代码
  apiKind: number; // 接口类型 0:OpenAi 1:Anthropic
  apiKey: string; // 接口密钥
  apiHost: string; // 接口地址
  apiUrl: string; // 接口端点
  proxyKind: number; // 代理类型 0:无 1:HTTP 2:SOCKS5
  proxyUrl: string; // 代理地址
  status: number; // 状态 0:禁用 1:启用
}

/**
 * 新增模型供应商Dto
 */
export interface AddProviderDto {
  name: string; // 供应商名称
  code: string; // 供应商代码
  apiKind: number; // 接口类型 0:OpenAi 1:Anthropic
  apiKey: string; // 接口密钥
  apiHost: string; // 接口地址
  apiUrl: string; // 接口端点
  proxyKind: number; // 代理类型 0:无 1:HTTP 2:SOCKS5
  proxyUrl: string; // 代理地址
  status: number; // 状态 0:禁用 1:启用
}

/**
 * 编辑模型供应商Dto
 */
export interface EditProviderDto {
  id: string; // 主键ID
  name: string; // 供应商名称
  code: string; // 供应商代码
  apiKind: number; // 接口类型 0:OpenAi 1:Anthropic
  apiKey: string; // 接口密钥
  apiHost: string; // 接口地址
  apiUrl: string; // 接口端点
  proxyKind: number; // 代理类型 0:无 1:HTTP 2:SOCKS5
  proxyUrl: string; // 代理地址
  status: number; // 状态 0:禁用 1:启用
}

export default {
  /**
   * 获取模型供应商列表
   */
  getProviderList: async (dto: GetProviderListDto): Promise<PageResult<GetProviderListVo>> => {
    return await Http.postEntity<PageResult<GetProviderListVo>>("/provider/getProviderList", dto);
  },

  /**
   * 获取模型供应商详情
   */
  getProviderDetails: async (dto: CommonIdDto): Promise<GetProviderDetailsVo> => {
    const result = await Http.postEntity<Result<GetProviderDetailsVo>>("/provider/getProviderDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增模型供应商
   */
  addProvider: async (dto: AddProviderDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/provider/addProvider", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑模型供应商
   */
  editProvider: async (dto: EditProviderDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/provider/editProvider", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除模型供应商
   */
  removeProvider: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/provider/removeProvider", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
