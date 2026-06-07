import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";

export interface GetAacpFuncListDto extends PageQuery {
  name: string | null;
  code: string | null;
  description: string | null;
}

export interface GetAacpFuncListVo {
  id: string;
  name: string;
  code: string;
  description: string;
}

export interface AddAacpFuncDto {
  name: string | null;
  code: string | null;
  description: string | null;
  schema: string | null;
  target: string | null;
  remark: string | null;
}

export interface EditAacpFuncDto {
  id: string | null;
  name: string | null;
  code: string | null;
  description: string | null;
  schema: string | null;
  target: string | null;
  remark: string | null;
}

export interface GetAacpFuncDetailsVo {
  id: string | null;
  name: string | null;
  code: string | null;
  description: string | null;
  schema: string | null;
  target: string | null;
  remark: string | null;
}

export default {
  getAacpFuncList: async (dto: GetAacpFuncListDto): Promise<RestPageableView<GetAacpFuncListVo>> => {
    const ret = await Http.postEntity<RestPageableView<GetAacpFuncListVo>>("/aacpFunc/getAacpFuncList", dto);
    return ret;
  },

  addAacpFunc: async (dto: AddAacpFuncDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpFunc/addAacpFunc", dto);
  },

  editAacpFunc: async (dto: EditAacpFuncDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpFunc/editAacpFunc", dto);
  },

  getAacpFuncDetails: async (id: string): Promise<GetAacpFuncDetailsVo> => {
    const ret = await Http.postEntity<Result<GetAacpFuncDetailsVo>>("/aacpFunc/getAacpFuncDetails", { id: id } as CommonIdDto);
    return ret.data;
  },

  removeAacpFunc: async (id: string): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpFunc/removeAacpFunc", { id: id } as CommonIdDto);
  },
};
