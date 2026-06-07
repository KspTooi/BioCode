import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";

export interface GetAacpCapabilityListDto extends PageQuery {
  name: string | null;
  kind: number | null;
}

export interface GetAacpCapabilityListVo {
  id: string;
  name: string;
  kind: number;
  remark: string;
}

export interface AddAacpCapabilityDto {
  name: string | null;
  kind: number | null;
  remark: string | null;
}

export interface EditAacpCapabilityDto {
  id: string | null;
  name: string | null;
  kind: number | null;
  remark: string | null;
}

export interface GetAacpCapabilityDetailsVo {
  id: string | null;
  name: string | null;
  kind: number | null;
  remark: string | null;
}

export default {
  getAacpCapabilityList: async (dto: GetAacpCapabilityListDto): Promise<RestPageableView<GetAacpCapabilityListVo>> => {
    const ret = await Http.postEntity<RestPageableView<GetAacpCapabilityListVo>>("/aacpCapability/getAacpCapabilityList", dto);
    return ret;
  },

  addAacpCapability: async (dto: AddAacpCapabilityDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpCapability/addAacpCapability", dto);
  },

  editAacpCapability: async (dto: EditAacpCapabilityDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpCapability/editAacpCapability", dto);
  },

  getAacpCapabilityDetails: async (id: string): Promise<GetAacpCapabilityDetailsVo> => {
    const ret = await Http.postEntity<Result<GetAacpCapabilityDetailsVo>>("/aacpCapability/getAacpCapabilityDetails", { id: id } as CommonIdDto);
    return ret.data;
  },

  removeAacpCapability: async (id: string): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpCapability/removeAacpCapability", { id: id } as CommonIdDto);
  },
};
