import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询AACP应用列表Dto
 */
export interface GetAacpAppListDto extends PageQuery {
  name?: string; // 应用名称
  code?: string; // 应用代码
  status?: number; // 状态 0:禁用 1:启用
}

/**
 * 查询AACP应用列表Vo
 */
export interface GetAacpAppListVo {
  id: string; // 主键ID
  name: string; // 应用名称
  code: string; // 应用代码
  isPublic: number; // 是否公开 0:不公开 1:公开
  status: number; // 状态 0:禁用 1:启用
  createTime: string; // 创建时间
}

/**
 * 查询AACP应用详情Vo
 */
export interface GetAacpAppDetailsVo {
  id: string; // 主键ID
  name: string; // 应用名称
  code: string; // 应用代码
  appKey: string; // 访问密钥
  isPublic: number; // 是否公开 0:不公开 1:公开
  ips: string; // IP白名单列表
  remark: string; // 备注
  status: number; // 状态 0:禁用 1:启用
}

/**
 * 新增AACP应用Dto
 */
export interface AddAacpAppDto {
  name: string; // 应用名称
  code: string; // 应用代码
  isPublic: number; // 是否公开 0:不公开 1:公开
  ips: string; // IP白名单列表
  remark: string; // 备注
  status: number; // 状态 0:禁用 1:启用
}

/**
 * 编辑AACP应用Dto
 */
export interface EditAacpAppDto {
  id: string; // 主键ID
  name: string; // 应用名称
  code: string; // 应用代码
  isPublic: number; // 是否公开 0:不公开 1:公开
  ips: string; // IP白名单列表
  remark: string; // 备注
  status: number; // 状态 0:禁用 1:启用
}

export default {
  /**
   * 获取AACP应用列表
   */
  getAacpAppList: async (dto: GetAacpAppListDto): Promise<PageResult<GetAacpAppListVo>> => {
    return await Http.postEntity<PageResult<GetAacpAppListVo>>("/aacpApp/getAacpAppList", dto);
  },

  /**
   * 获取AACP应用详情
   */
  getAacpAppDetails: async (dto: CommonIdDto): Promise<GetAacpAppDetailsVo> => {
    const result = await Http.postEntity<Result<GetAacpAppDetailsVo>>("/aacpApp/getAacpAppDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增AACP应用
   */
  addAacpApp: async (dto: AddAacpAppDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpApp/addAacpApp", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑AACP应用
   */
  editAacpApp: async (dto: EditAacpAppDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpApp/editAacpApp", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除AACP应用
   */
  removeAacpApp: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpApp/removeAacpApp", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
