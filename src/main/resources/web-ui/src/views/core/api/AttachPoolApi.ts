import Http from "@/commons/Http.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 最新附件存储池扫描记录 VO
 */
export interface GetLatestScanRecordVo {
  poolPath: string; // 附件存储池地址
  poolCapacityBytes: string; // 总可用字节
  poolUsageBytes: string; // 总已用字节
  poolAttachesBytes: string; // 附件占用字节
  indexedCount: number; // 已索引附件数
  indexedLostCount: number; // 失效索引数
  driftCount: number; // 游离附件数
  scanStartTime: string; // 扫描开始时间
  scanEndTime: string; // 扫描结束时间
  scanStatus: number; // 扫描状态 0:正在扫描 1:成功
}

/**
 * 附件列表查询 DTO
 */
export interface GetAttachListDto extends PageQuery {
  indexFilter?: number | null; // 1:已索引 0:无效
}

/**
 * 附件列表 VO
 */
export interface GetAttachListVo {
  path: string; // 文件路径
  sha256: string; // 文件摘要
  totalSize: number; // 文件总大小
  receiveSize: number; // 已接收大小
  status: number; // 状态 0:未索引 1:区块不完整 2:校验中 3:有效
  verifyTime: string; // 校验时间
  createTime: string; // 创建时间
}

/**
 * 附件索引筛选项
 */
export const AttachIndexFilterOptions = [
  { label: "已索引", value: 1 },
  { label: "无效", value: 0 },
];

/**
 * 扫描附件存储池 DTO
 */
export interface ScanAttachPoolDto {
  scanMode: number; // 0:快速扫描 1:深度扫描
}

/**
 * 重建索引进度 VO
 */
export interface GetRebuildIndexStatusVo {
  running: boolean; // 是否执行中
  total: number; // 游离文件总数
  processed: number; // 已处理数
  imported: number; // 新建数
  repaired: number; // 修复数
  deleted: number; // 删除重复游离数
  failed: number; // 失败数
  message: string; // 任务摘要
  startTime: string; // 开始时间
  endTime: string; // 结束时间
}

export default {
  /**
   * 查询最新的附件存储池扫描记录
   */
  getLatestScanRecord: async (): Promise<GetLatestScanRecordVo> => {
    const result = await Http.postEntity<Result<GetLatestScanRecordVo>>("/attachPool/getLatestScanRecord", {});
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 扫描附件存储池
   */
  scanAttachPool: async (dto: ScanAttachPoolDto): Promise<void> => {
    const result = await Http.postEntity<Result<string>>("/attachPool/scanAttachPool", dto);
    if (result.code === 0) {
      return;
    }
    throw new Error(result.message);
  },

  /**
   * 查询附件列表
   */
  getAttachList: async (dto: GetAttachListDto): Promise<PageResult<GetAttachListVo>> => {
    return await Http.postEntity<PageResult<GetAttachListVo>>("/attachPool/getAttachList", dto);
  },

  /**
   * 启动重建索引
   */
  startRebuildIndex: async (): Promise<void> => {
    const result = await Http.postEntity<Result<string>>("/attachPool/startRebuildIndex", {});
    if (result.code === 0) {
      return;
    }
    throw new Error(result.message);
  },

  /**
   * 查询重建索引进度
   */
  getRebuildIndexStatus: async (): Promise<GetRebuildIndexStatusVo> => {
    const result = await Http.postEntity<Result<GetRebuildIndexStatusVo>>("/attachPool/getRebuildIndexStatus", {});
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 清除无效索引
   */
  clearInvalidIndexes: async (): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/attachPool/clearInvalidIndexes", {});
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },
};
