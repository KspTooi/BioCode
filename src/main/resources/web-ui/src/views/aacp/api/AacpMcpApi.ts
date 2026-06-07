import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";

export interface GetAacpMcpListDto extends PageQuery {
  name: string | null;
  code: string | null;
  networkKind: number | null;
  authPsk: string | null;
  status: number | null;
}

export interface GetAacpMcpListVo {
  id: string;
  name: string;
  code: string;
  networkKind: number;
  host: string;
  port: number;
  authKind: number;
  authPsk: string;
  status: number;
}

export interface AddAacpMcpDto {
  name: string | null;
  code: string | null;
  networkKind: number | null;
  host: string | null;
  port: number | null;
  authKind: number | null;
  authPsk: string | null;
  status: number | null;
}

export interface EditAacpMcpDto {
  id: string | null;
  name: string | null;
  code: string | null;
  networkKind: number | null;
  host: string | null;
  port: number | null;
  authKind: number | null;
  authPsk: string | null;
  status: number | null;
}

export interface GetAacpMcpDetailsVo {
  id: string | null;
  name: string | null;
  code: string | null;
  networkKind: number | null;
  host: string | null;
  port: number | null;
  authKind: number | null;
  authPsk: string | null;
  status: number | null;
}

export default {
  getAacpMcpList: async (dto: GetAacpMcpListDto): Promise<RestPageableView<GetAacpMcpListVo>> => {
    const ret = await Http.postEntity<RestPageableView<GetAacpMcpListVo>>("/aacpMcp/getAacpMcpList", dto);
    return ret;
  },

  addAacpMcp: async (dto: AddAacpMcpDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpMcp/addAacpMcp", dto);
  },

  editAacpMcp: async (dto: EditAacpMcpDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpMcp/editAacpMcp", dto);
  },

  getAacpMcpDetails: async (id: string): Promise<GetAacpMcpDetailsVo> => {
    const ret = await Http.postEntity<Result<GetAacpMcpDetailsVo>>("/aacpMcp/getAacpMcpDetails", { id: id } as CommonIdDto);
    return ret.data;
  },

  removeAacpMcp: async (id: string): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpMcp/removeAacpMcp", { id: id } as CommonIdDto);
  },
};
