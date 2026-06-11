import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";

export interface GetAgentHubListDto extends PageQuery {
  name: string | null; //服务器名称
  code: string | null; //唯一编码
  status: number | null; //状态 0:离线 1:在线
}

export interface GetAgentHubListVo {
  id: string; //主键ID
  name: string; //服务器名称
  code: string; //唯一编码
  networkKind: number; //通信协议 0:HTTP+SSE 1:WS
  authKind: number; //鉴权类型 0:无 1:PSK
  authPsk: string; //预共享密钥
  status: number; //状态 0:离线 1:在线
  capCount: number; //关联能力包数量
  funcCount: number; //关联微函数数量
}

export interface AddAgentHubDto {
  name: string | null; //服务器名称
  code: string | null; //唯一编码
  networkKind: number | null; //通信协议 0:HTTP+SSE 1:WS
  authKind: number | null; //鉴权类型 0:无 1:PSK
  authPsk: string | null; //预共享密钥
  status: number | null; //状态 0:离线 1:在线
  capIds: string[]; //能力包ID列表
}

export interface EditAgentHubDto {
  id: string | null; //主键ID
  name: string | null; //服务器名称
  code: string | null; //唯一编码
  networkKind: number | null; //通信协议 0:HTTP+SSE 1:WS
  authKind: number | null; //鉴权类型 0:无 1:PSK
  authPsk: string | null; //预共享密钥
  status: number | null; //状态 0:离线 1:在线
  capIds: string[]; //能力包ID列表
}

export interface GetAgentHubDetailsVo {
  id: string | null; //主键ID
  name: string | null; //服务器名称
  code: string | null; //唯一编码
  networkKind: number | null; //通信协议 0:HTTP+SSE 1:WS
  authKind: number | null; //鉴权类型 0:无 1:PSK
  authPsk: string | null; //预共享密钥
  status: number | null; //状态 0:离线 1:在线
  capIds: string[]; //关联的能力包ID列表
}

export default {
  /**
   * 获取智能体枢纽列表
   * @param dto 查询条件
   * @returns 智能体枢纽列表
   */
  getAgentHubList: async (dto: GetAgentHubListDto): Promise<RestPageableView<GetAgentHubListVo>> => {
    const ret = await Http.postEntity<RestPageableView<GetAgentHubListVo>>("/agentHub/getAgentHubList", dto);
    return ret;
  },

  /**
   * 添加智能体枢纽
   * @param dto 智能体枢纽信息
   * @returns 操作结果
   */
  addAgentHub: async (dto: AddAgentHubDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/agentHub/addAgentHub", dto);
  },

  /**
   * 编辑智能体枢纽
   * @param dto 智能体枢纽信息
   * @returns 操作结果
   */
  editAgentHub: async (dto: EditAgentHubDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/agentHub/editAgentHub", dto);
  },

  /**
   * 获取智能体枢纽详情
   * @param id 智能体枢纽ID
   * @returns 智能体枢纽详情
   */
  getAgentHubDetails: async (id: string): Promise<GetAgentHubDetailsVo> => {
    const ret = await Http.postEntity<Result<GetAgentHubDetailsVo>>("/agentHub/getAgentHubDetails", { id: id } as CommonIdDto);
    return ret.data;
  },

  /**
   * 删除智能体枢纽
   * @param id 智能体枢纽ID
   * @returns 操作结果
   */
  removeAgentHub: async (id: string): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/agentHub/removeAgentHub", { id: id } as CommonIdDto);
  },
};
