import Http from "@/commons/Http.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 最新附件池扫描记录 VO
 */
export interface GetLatestScanRecordVo {
  poolPath: string; // 存储池地址
  poolCapacityBytes: string; // 总可用字节
  poolAttachesBytes: string; // 附件占用字节
  indexedCount: number; // 已索引附件数
  driftCount: number; // 游离附件数
  scanStartTime: string; // 扫描开始时间
  scanEndTime: string; // 扫描结束时间
  scanStatus: number; // 扫描状态 0:正在扫描 1:成功
}

export default {
  /**
   * 查询最新的附件池扫描记录
   */
  getLatestScanRecord: async (): Promise<GetLatestScanRecordVo> => {
    const result = await Http.postEntity<Result<GetLatestScanRecordVo>>("/attachPool/getLatestScanRecord", {});
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 扫描附件池
   */
  scanAttachPool: async (): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/attachPool/scanAttachPool", {});
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
