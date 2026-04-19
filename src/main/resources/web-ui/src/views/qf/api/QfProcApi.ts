import type Result from "@/commons/model/Result.ts";
import Http from "@/commons/Http.ts";

/**
 * 发起审批流程Dto
 */
export interface LaunchQfProcessDto {
  code: string; // 模型编码
  bizFormCode: string; // 业务表单编码
  dataId: string; // 业务数据ID
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
};
