import type Result from "@/commons/model/Result.ts";
import Http from "@/commons/Http.ts";
import type { ApproveFlowRecordVo } from "@/views/qf/api/QfTodoApi.ts";

/**
 * 发起审批流程Dto
 */
export interface LaunchProcDto {
  code: string; // 模型编码
  dataId: string; // 业务数据ID
  members: LaunchMemberParamDto[]; // 启动成员参数列表
}

/**
 * 查询流程实例Dto
 */
export interface GetProcessDto {
  engProcId: string; // 工程流程ID
}

/**
 * 获取流程节点配置Dto
 */
export interface GetProcNodeDefineDto {
  code: string; // 模型编码
}

/**
 * 流程节点配置Vo
 */
export interface GetProcNodeDefineVo {
  nodeId: string; // 节点ID
  nodeName: string; // 节点名称
  aprKind: number; // 审批节点类型 0:固定人 1:发起时选人
  memberKind: number; // 办理成员类型 0:指定人 1:组 2:组织机构 3:发起人 10:任意人
  memberIds: string[]; // 成员ID列表
  memberNames: string[]; // 成员名称列表
}

/**
 * 启动成员参数Dto
 */
export interface LaunchMemberParamDto {
  nodeId: string; // 节点ID
  memberId: string; // 成员ID
}

export default {
  /**
   * 发起审批流程
   */
  launchProc: async (dto: LaunchProcDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfProc/launchProc", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 获取审批流程图 BPMN XML
   */
  getProcessApproveFlow: async (dto: GetProcessDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfProc/getProcessApproveFlow", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 获取审批流程流转记录
   */
  getProcessApproveFlowRecord: async (dto: GetProcessDto): Promise<ApproveFlowRecordVo[]> => {
    const result = await Http.postEntity<Result<ApproveFlowRecordVo[]>>("/qfProc/getProcessApproveFlowRecord", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 获取流程节点配置列表（发起流程时使用）
   */
  getProcNodeDefine: async (dto: GetProcNodeDefineDto): Promise<GetProcNodeDefineVo[]> => {
    const result = await Http.postEntity<Result<GetProcNodeDefineVo[]>>("/qfProc/getProcNodeDefine", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },
};
