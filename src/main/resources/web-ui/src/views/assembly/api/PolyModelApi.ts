import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询聚合模型列表 DTO（对应后端 GetPolyModelListDto）
 */
export interface GetPolyModelListDto {
  outputSchemaId?: string; // 输出方案ID
}

/**
 * 聚合模型列表 VO（对应后端 GetPolyModelListVo）
 */
export interface GetPolyModelListVo {
  id: string; // 主键ID
  outputSchemaId: string; // 输出方案ID
  name: string; // 字段名
  dataType: string; // 数据类型
  length: number; // 长度
  require: number; // 必填 0:否 1:是
  policyCrudJson: string[]; // 可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW
  policyQuery: number; // 查询策略 0:等于
  policyView: number; // 显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT
  pk: number; // 是否主键 0:否 1:是
  remark: string; // 字段备注
  seq: number; // 排序
}

/**
 * 新增 DTO（对应后端 AddPolyModelDto）
 */
export interface AddPolyModelDto {
  outputSchemaId: string; // 输出方案ID
  name: string; // 字段名
  dataType: string; // 数据类型
  length: number; // 长度
  require: number; // 必填 0:否 1:是
  policyCrudJson: string[]; // 可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW
  policyQuery: number; // 查询策略 0:等于
  policyView: number; // 显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT
  remark: string; // 字段备注
  pk: number; // 是否主键 0:否 1:是
  seq: number; // 排序
}

/**
 * 编辑 DTO（对应后端 EditPolyModelDto）
 */
export interface EditPolyModelDto {
  id: string; // 主键ID
  outputSchemaId: string; // 输出方案ID
  name: string; // 字段名
  dataType: string; // 数据类型
  length: number; // 长度
  require: number; // 必填 0:否 1:是
  policyCrudJson: string[]; // 可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW
  policyQuery: number; // 查询策略 0:等于
  policyView: number; // 显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT
  remark: string; // 字段备注
  pk: number; // 是否主键 0:否 1:是
  seq: number; // 排序
}

/**
 * 从原始模型导入 DTO
 */
export interface ImportPolyModelFromRawDto {
  outputSchemaId: string; // 输出方案ID
  polyTemplateId?: string; // 聚合模板ID(可选)
}

export default {
  /**
   * 获取输出方案聚合模型列表
   */
  getPolyModelList: async (dto: GetPolyModelListDto): Promise<PageResult<GetPolyModelListVo>> => {
    return await Http.postEntity<PageResult<GetPolyModelListVo>>("/polyModel/getPolyModelList", dto);
  },

  /**
   * 新增输出方案聚合模型
   */
  addPolyModel: async (dto: AddPolyModelDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyModel/addPolyModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑输出方案聚合模型
   */
  editPolyModel: async (dto: EditPolyModelDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyModel/editPolyModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除输出方案聚合模型
   */
  removePolyModel: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyModel/removePolyModel", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 从原始模型导入聚合模型（输出方案ID + 可选聚合模板ID）
   */
  importFromRaw: async (dto: ImportPolyModelFromRawDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/polyModel/importFromRaw", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
