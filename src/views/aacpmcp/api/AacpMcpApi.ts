import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询MCP服务器列表Dto
 */
export interface GetAacpMcpListDto extends PageQuery {
  name?: string; // 服务器名称
  code?: string; // 唯一编码
  networkKind?: number; // 通信协议 0:HTTP+SSE 1:WS
  authPsk?: string; // 预共享密钥
  status?: number; // 状态 0:离线 1:在线
}

/**
 * 查询MCP服务器列表Vo
 */
export interface GetAacpMcpListVo {
  id: string; // 主键ID
  name: string; // 服务器名称
  code: string; // 唯一编码
  networkKind: number; // 通信协议 0:HTTP+SSE 1:WS
  host: string; // 主机
  port: number; // 端口
  authKind: number; // 鉴权类型 0:无 1:PSK
  authPsk: string; // 预共享密钥
  status: number; // 状态 0:离线 1:在线
}

/**
 * 查询MCP服务器详情Vo
 */
export interface GetAacpMcpDetailsVo {
  id: string; // 主键ID
  name: string; // 服务器名称
  code: string; // 唯一编码
  networkKind: number; // 通信协议 0:HTTP+SSE 1:WS
  host: string; // 主机
  port: number; // 端口
  authKind: number; // 鉴权类型 0:无 1:PSK
  authPsk: string; // 预共享密钥
  status: number; // 状态 0:离线 1:在线
}

/**
 * 新增MCP服务器Dto
 */
export interface AddAacpMcpDto {
  name: string; // 服务器名称
  code: string; // 唯一编码
  networkKind: number; // 通信协议 0:HTTP+SSE 1:WS
  host: string; // 主机
  port: number; // 端口
  authKind: number; // 鉴权类型 0:无 1:PSK
  authPsk: string; // 预共享密钥
  status: number; // 状态 0:离线 1:在线
}

/**
 * 编辑MCP服务器Dto
 */
export interface EditAacpMcpDto {
  id: string; // 主键ID
  name: string; // 服务器名称
  code: string; // 唯一编码
  networkKind: number; // 通信协议 0:HTTP+SSE 1:WS
  host: string; // 主机
  port: number; // 端口
  authKind: number; // 鉴权类型 0:无 1:PSK
  authPsk: string; // 预共享密钥
  status: number; // 状态 0:离线 1:在线
}

export default {
  /**
   * 获取MCP服务器列表
   */
  getAacpMcpList: async (dto: GetAacpMcpListDto): Promise<PageResult<GetAacpMcpListVo>> => {
    return await Http.postEntity<PageResult<GetAacpMcpListVo>>("/aacpMcp/getAacpMcpList", dto);
  },

  /**
   * 获取MCP服务器详情
   */
  getAacpMcpDetails: async (dto: CommonIdDto): Promise<GetAacpMcpDetailsVo> => {
    const result = await Http.postEntity<Result<GetAacpMcpDetailsVo>>("/aacpMcp/getAacpMcpDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增MCP服务器
   */
  addAacpMcp: async (dto: AddAacpMcpDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpMcp/addAacpMcp", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑MCP服务器
   */
  editAacpMcp: async (dto: EditAacpMcpDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpMcp/editAacpMcp", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除MCP服务器
   */
  removeAacpMcp: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpMcp/removeAacpMcp", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
