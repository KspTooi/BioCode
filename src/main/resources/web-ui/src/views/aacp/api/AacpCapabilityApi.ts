import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";

export interface GetAacpCapabilityListDto extends PageQuery {
  name: string | null; //能力包名称
  kind: number | null; //类型 0:微函数
}

export interface GetAacpCapabilityListVo {
  id: string; //主键ID
  name: string; //能力包名称
  kind: number; //类型 0:微函数
  remark: string; //备注
  funcCount: number; //关联微函数数量
}

export interface AddAacpCapabilityDto {
  name: string | null; //能力包名称
  kind: number | null; //类型 0:微函数
  remark: string | null; //备注
  funcIds: string[]; //微函数ID列表
}

export interface EditAacpCapabilityDto {
  id: string | null; //主键ID
  name: string | null; //能力包名称
  kind: number | null; //类型 0:微函数
  remark: string | null; //备注
  funcIds: string[]; //微函数ID列表
}

export interface GetAacpCapabilityDetailsVo {
  id: string; //主键ID
  name: string | null; //能力包名称
  kind: number | null; //类型 0:微函数
  remark: string | null; //备注
  funcIds: string[]; //关联的微函数ID列表
}

export default {
  /**
   * 获取能力包列表
   * @param dto 查询条件
   * @returns 能力包列表
   */
  getAacpCapabilityList: async (dto: GetAacpCapabilityListDto): Promise<RestPageableView<GetAacpCapabilityListVo>> => {
    const ret = await Http.postEntity<RestPageableView<GetAacpCapabilityListVo>>("/aacpCapability/getAacpCapabilityList", dto);
    return ret;
  },

  /**
   * 添加能力包
   * @param dto 能力包信息
   * @returns 操作结果
   */
  addAacpCapability: async (dto: AddAacpCapabilityDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpCapability/addAacpCapability", dto);
  },

  /**
   * 编辑能力包
   * @param dto 能力包信息
   * @returns 操作结果
   */
  editAacpCapability: async (dto: EditAacpCapabilityDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpCapability/editAacpCapability", dto);
  },

  /**
   * 获取能力包详情
   * @param id 能力包ID
   * @returns 能力包详情
   */
  getAacpCapabilityDetails: async (id: string): Promise<GetAacpCapabilityDetailsVo> => {
    const ret = await Http.postEntity<Result<GetAacpCapabilityDetailsVo>>("/aacpCapability/getAacpCapabilityDetails", {
      id: id,
    } as CommonIdDto);
    return ret.data;
  },

  /**
   * 删除能力包
   * @param id 能力包ID
   * @returns 操作结果
   */
  removeAacpCapability: async (id: string): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpCapability/removeAacpCapability", { id: id } as CommonIdDto);
  },
};
