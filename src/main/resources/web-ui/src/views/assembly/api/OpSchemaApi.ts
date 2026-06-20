import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询列表DTO
 */
export interface GetOpSchemaListDto extends PageQuery {
  name?: string; // 输出方案名称
  modelName?: string; // 模型名称
  tableName?: string; // 数据源表名
}

/**
 * 列表VO
 */
export interface GetOpSchemaListVo {
  id: string; // 主键ID
  name: string; // 输出方案名称
  modelName: string; // 模型名称
  tableName: string; // 数据源表名
  fieldCountOrigin: number; // 字段数(原始)
  fieldCountPoly: number; // 字段数(聚合)
  createTime: string; // 创建时间
}

/**
 * 详情VO
 */
export interface GetOpSchemaDetailsVo {
  id: string; // 主键ID
  dataSourceId: string; // 数据源ID
  typeSchemaId: string; // 类型映射方案ID
  inputScmId: string; // 输入SCM ID
  outputScmId: string; // 输出SCM ID
  name: string; // 输出方案名称
  modelName: string; // 模型名称
  modelRemark: string; // 模型备注
  bizDomain: string; // 业务域
  tableName: string; // 数据源表名
  removeTablePrefix: string; // 移除表前缀
  permCodePrefix: string; // 权限码前缀
  policyOverride: number; // 写出策略 0:不覆盖 1:覆盖
  baseInput: string; // 输入基准路径
  baseOutput: string; // 输出基准路径
  remark: string; // 备注
}

/**
 * 新增DTO
 */
export interface AddOpSchemaDto {
  dataSourceId: string; // 数据源ID
  typeSchemaId: string; // 类型映射方案ID
  inputScmId: string; // 输入SCM ID
  outputScmId: string; // 输出SCM ID
  name: string; // 输出方案名称
  modelName: string; // 模型名称
  modelRemark: string; // 模型备注
  bizDomain: string; // 业务域
  tableName: string; // 数据源表名
  removeTablePrefix: string; // 移除表前缀
  permCodePrefix: string; // 权限码前缀
  policyOverride: number; // 写出策略 0:不覆盖 1:覆盖
  baseInput: string; // 输入基准路径
  baseOutput: string; // 输出基准路径
  remark: string; // 备注
}

/**
 * 编辑DTO
 */
export interface EditOpSchemaDto {
  id: string; // 主键ID
  dataSourceId: string; // 数据源ID
  typeSchemaId: string; // 类型映射方案ID
  inputScmId: string; // 输入SCM ID
  outputScmId: string; // 输出SCM ID
  name: string; // 输出方案名称
  modelName: string; // 模型名称
  modelRemark: string; // 模型备注
  bizDomain: string; // 业务域
  tableName: string; // 数据源表名
  removeTablePrefix: string; // 移除表前缀
  permCodePrefix: string; // 权限码前缀
  policyOverride: number; // 写出策略 0:不覆盖 1:覆盖
  baseInput: string; // 输入基准路径
  baseOutput: string; // 输出基准路径
  remark: string; // 备注
}

/**
 * 蓝图列表VO
 */
export interface GetOpBluePrintListVo {
  fileName: string; // 蓝图文件名
  filePath: string; // 蓝图文件路径(相对于基准路径)
  parsedName: string; // 蓝图解析名(代入参数解析后的文件名)
  parsedPath: string; // 蓝图解析路径(代入参数解析后的路径)
  sha256Hex: string; // 蓝图SHA256
}

/**
 * 预览蓝图DTO
 */
export interface PreviewOpBluePrintDto {
  opSchemaId: string; // 输出方案ID
  sha256Hex: string; // 蓝图SHA256
}

/**
 * 执行输出方案DTO
 */
export interface ExecuteOpSchemaDto {
  opSchemaId: string; // 输出方案ID
  sha256Hexs: string[]; // 蓝图SHA256列表
}

/**
 * 复制输出方案DTO
 */
export interface CopyOpSchemaDto {
  id: string; // 源输出方案ID
  name: string; // 输出方案名称
}

export default {
  /**
   * 获取输出方案列表
   */
  getOpSchemaList: async (dto: GetOpSchemaListDto): Promise<PageResult<GetOpSchemaListVo>> => {
    return await Http.postEntity<PageResult<GetOpSchemaListVo>>("/opSchema/getOpSchemaList", dto);
  },

  /**
   * 获取输出方案详情
   */
  getOpSchemaDetails: async (dto: CommonIdDto): Promise<GetOpSchemaDetailsVo> => {
    const result = await Http.postEntity<Result<GetOpSchemaDetailsVo>>("/opSchema/getOpSchemaDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增输出方案
   */
  addOpSchema: async (dto: AddOpSchemaDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opSchema/addOpSchema", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑输出方案
   */
  editOpSchema: async (dto: EditOpSchemaDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opSchema/editOpSchema", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除输出方案
   */
  removeOpSchema: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opSchema/removeOpSchema", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 执行输出方案
   */
  executeOpSchema: async (dto: ExecuteOpSchemaDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opSchema/executeOpSchema", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 查询蓝图文件列表
   */
  getOpBluePrintList: async (dto: CommonIdDto): Promise<GetOpBluePrintListVo[]> => {
    const result = await Http.postEntity<Result<GetOpBluePrintListVo[]>>("/opSchema/getOpBluePrintList", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 预览蓝图输出
   */
  previewOpBluePrint: async (dto: PreviewOpBluePrintDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opSchema/previewOpBluePrint", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 预览Qbe模型JSON
   */
  previewQbeModel: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opSchema/previewQbeModel", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 复制输出方案
   */
  copyOpSchema: async (dto: CopyOpSchemaDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/opSchema/copyOpSchema", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
