import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";

export interface GetAacpMcpListDto extends PageQuery {
  name: string | null; //服务器名称
  code: string | null; //唯一编码
  status: number | null; //状态 0:离线 1:在线
}

export interface GetAacpMcpListVo {
  id: string; //主键ID
  name: string; //服务器名称
  code: string; //唯一编码
  networkKind: number; //通信协议 0:HTTP+SSE 1:WS
  authKind: number; //鉴权类型 0:无 1:PSK
  authPsk: string; //预共享密钥
  status: number; //状态 0:离线 1:在线
  capabilityCount: number; //关联能力包数量
  funcCount: number; //关联微函数数量
}

export interface AddAacpMcpDto {
  name: string | null; //服务器名称
  code: string | null; //唯一编码
  networkKind: number | null; //通信协议 0:HTTP+SSE 1:WS
  authKind: number | null; //鉴权类型 0:无 1:PSK
  authPsk: string | null; //预共享密钥
  status: number | null; //状态 0:离线 1:在线
}

export interface EditAacpMcpDto {
  id: string | null; //主键ID
  name: string | null; //服务器名称
  code: string | null; //唯一编码
  networkKind: number | null; //通信协议 0:HTTP+SSE 1:WS
  authKind: number | null; //鉴权类型 0:无 1:PSK
  authPsk: string | null; //预共享密钥
  status: number | null; //状态 0:离线 1:在线
}

export interface GetAacpMcpDetailsVo {
  id: string | null; //主键ID
  name: string | null; //服务器名称
  code: string | null; //唯一编码
  networkKind: number | null; //通信协议 0:HTTP+SSE 1:WS
  authKind: number | null; //鉴权类型 0:无 1:PSK
  authPsk: string | null; //预共享密钥
  status: number | null; //状态 0:离线 1:在线
}

export default {
  /**
   * 获取MCP服务器列表
   * @param dto 查询条件
   * @returns MCP服务器列表
   */
  getAacpMcpList: async (dto: GetAacpMcpListDto): Promise<RestPageableView<GetAacpMcpListVo>> => {
    const ret = await Http.postEntity<RestPageableView<GetAacpMcpListVo>>("/aacpMcp/getAacpMcpList", dto);
    return ret;
  },

  /**
   * 添加MCP服务器
   * @param dto MCP服务器信息
   * @returns 操作结果
   */
  addAacpMcp: async (dto: AddAacpMcpDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpMcp/addAacpMcp", dto);
  },

  /**
   * 编辑MCP服务器
   * @param dto MCP服务器信息
   * @returns 操作结果
   */
  editAacpMcp: async (dto: EditAacpMcpDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpMcp/editAacpMcp", dto);
  },

  /**
   * 获取MCP服务器详情
   * @param id MCP服务器ID
   * @returns MCP服务器详情
   */
  getAacpMcpDetails: async (id: string): Promise<GetAacpMcpDetailsVo> => {
    const ret = await Http.postEntity<Result<GetAacpMcpDetailsVo>>("/aacpMcp/getAacpMcpDetails", { id: id } as CommonIdDto);
    return ret.data;
  },

  /**
   * 删除MCP服务器
   * @param id MCP服务器ID
   * @returns 操作结果
   */
  removeAacpMcp: async (id: string): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/aacpMcp/removeAacpMcp", { id: id } as CommonIdDto);
  },
};
