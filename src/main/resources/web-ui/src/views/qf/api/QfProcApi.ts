import type Result from "@/commons/model/Result.ts";
import Http from "@/commons/Http.ts";
import type { ApproveFlowRecordVo } from "@/views/qf/api/QfTodoApi.ts";

/**
 * 发起审批流程Dto
 */
export interface LaunchQfProcessDto {
  code: string; // 模型编码
  bizFormCode: string; // 业务表单编码
  dataId: string; // 业务数据ID
}

/**
 * 查询流程实例Dto
 */
export interface GetProcessDto {
  engProcId: string; // 工程流程ID
}

export default {
  /**
   * 发起审批流程
   */
  launchQfProcess: async (dto: LaunchQfProcessDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfProc/launchQfProcess", dto);
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
};
