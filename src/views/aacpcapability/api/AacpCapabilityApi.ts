import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询能力包列表Dto
 */
export interface GetAacpCapabilityListDto extends PageQuery {
  name?: string; // 能力包名称
  kind?: number; // 类型 0:微函数
}

/**
 * 查询能力包列表Vo
 */
export interface GetAacpCapabilityListVo {
  id: string; // 主键ID
  name: string; // 能力包名称
  kind: number; // 类型 0:微函数
  remark: string; // 备注
}

/**
 * 查询能力包详情Vo
 */
export interface GetAacpCapabilityDetailsVo {
  id: string; // 主键ID
  name: string; // 能力包名称
  kind: number; // 类型 0:微函数
  remark: string; // 备注
}

/**
 * 新增能力包Dto
 */
export interface AddAacpCapabilityDto {
  name: string; // 能力包名称
  kind: number; // 类型 0:微函数
  remark: string; // 备注
}

/**
 * 编辑能力包Dto
 */
export interface EditAacpCapabilityDto {
  id: string; // 主键ID
  name: string; // 能力包名称
  kind: number; // 类型 0:微函数
  remark: string; // 备注
}

export default {
  /**
   * 获取能力包列表
   */
  getAacpCapabilityList: async (dto: GetAacpCapabilityListDto): Promise<PageResult<GetAacpCapabilityListVo>> => {
    return await Http.postEntity<PageResult<GetAacpCapabilityListVo>>("/aacpCapability/getAacpCapabilityList", dto);
  },

  /**
   * 获取能力包详情
   */
  getAacpCapabilityDetails: async (dto: CommonIdDto): Promise<GetAacpCapabilityDetailsVo> => {
    const result = await Http.postEntity<Result<GetAacpCapabilityDetailsVo>>("/aacpCapability/getAacpCapabilityDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增能力包
   */
  addAacpCapability: async (dto: AddAacpCapabilityDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpCapability/addAacpCapability", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑能力包
   */
  editAacpCapability: async (dto: EditAacpCapabilityDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpCapability/editAacpCapability", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除能力包
   */
  removeAacpCapability: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpCapability/removeAacpCapability", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
