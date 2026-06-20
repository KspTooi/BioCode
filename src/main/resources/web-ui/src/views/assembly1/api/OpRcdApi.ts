import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询输出方案执行记录列表Dto
 */
export interface GetOpRcdListDto extends PageQuery {
  opName?: string; // 输出方案名称
  dsName?: string; // 数据源名称
  dsTableName?: string; // 数据源表名
  modelName?: string; // 模型名称
  bizDomain?: string; // 业务域
  creatorUsername?: string; // 操作人账号
}

/**
 * 查询输出方案执行记录列表Vo
 */
export interface GetOpRcdListVo {
  id: string; // 主键ID
  opName: string; // 输出方案名称
  dsName: string; // 数据源名称
  dsTableName: string; // 数据源表名
  modelName: string; // 模型名称
  bizDomain: string; // 业务域
  startTime: string; // 开始时间
  durationMs: number; // 耗时MS
  creatorUsername: string; // 操作人账号
}

/**
 * 查询输出方案执行记录详情Vo
 */
export interface GetOpRcdDetailsVo {
  id: string; // 主键ID
  opName: string; // 输出方案名称
  dsName: string; // 数据源名称
  dsTableName: string; // 数据源表名
  dsUrl: string; // 数据源连接字符串
  scmInputUrl: string; // 输入SCM仓库地址
  scmOutputUrl: string; // 输出SCM仓库地址
  modelName: string; // 模型名称
  modelRemark: string; // 模型备注
  bizDomain: string; // 业务域
  qbeParams: string; // QBE参数
  startTime: string; // 开始时间
  endTime: string; // 结束时间
  durationMs: number; // 耗时MS
  creatorUsername: string; // 操作人账号
}

/**
 * 新增输出方案执行记录Dto
 */
export interface AddOpRcdDto {
}

/**
 * 编辑输出方案执行记录Dto
 */
export interface EditOpRcdDto {
  id: string; // 主键ID
}

export default {
  /**
   * 获取输出方案执行记录列表
   */
  getOpRcdList: async (dto: GetOpRcdListDto): Promise<PageResult<GetOpRcdListVo>> => {
    return await Http.postEntity<PageResult<GetOpRcdListVo>>("/opRcd/getOpRcdList", dto);
  },

  /**
   * 获取输出方案执行记录详情
   */
  getOpRcdDetails: async (dto: CommonIdDto): Promise<GetOpRcdDetailsVo> => {
    const result = await Http.postEntity<Result<GetOpRcdDetailsVo>>("/opRcd/getOpRcdDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增输出方案执行记录
   */
  addOpRcd: async (dto: AddOpRcdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opRcd/addOpRcd", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑输出方案执行记录
   */
  editOpRcd: async (dto: EditOpRcdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opRcd/editOpRcd", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除输出方案执行记录
   */
  removeOpRcd: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opRcd/removeOpRcd", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
