import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询列表DTO
 */
export interface GetTymSchemaListDto extends PageQuery {
  id?: string; // 主键ID
  name?: string; // 方案名称
  code?: string; // 方案编码
}

/**
 * 列表VO
 */
export interface GetTymSchemaListVo {
  id: string; // 主键ID
  name: string; // 方案名称
  code: string; // 方案编码
  typeCount: number; // 类型数量
  defaultType: string; // 默认类型
  seq: number; // 排序
  createTime: string; // 创建时间
}

/**
 * 详情VO
 */
export interface GetTymSchemaDetailsVo {
  id: string; // 主键ID
  name: string; // 方案名称
  code: string; // 方案编码
  typeCount: number; // 类型数量
  defaultType: string; // 默认类型
  seq: number; // 排序
  remark: string; // 备注
  createTime: string; // 创建时间
}

/**
 * 新增DTO
 */
export interface AddTymSchemaDto {
  name: string; // 方案名称
  code: string; // 方案编码
  defaultType: string; // 默认类型
  seq: number; // 排序
  remark: string; // 备注
}

/**
 * 编辑DTO
 */
export interface EditTymSchemaDto {
  id: string; // 主键ID
  name: string; // 方案名称
  code: string; // 方案编码
  defaultType: string; // 默认类型
  seq: number; // 排序
  remark: string; // 备注
}

/**
 * 复制类型映射方案DTO
 */
export interface CopyTymSchemaDto {
  id: string; // 源类型映射方案ID
  name: string; // 方案名称 max:32
  code: string; // 方案编码 max:32
}

export default {
  /**
   * 获取类型映射方案表列表
   */
  getTymSchemaList: async (dto: GetTymSchemaListDto): Promise<PageResult<GetTymSchemaListVo>> => {
    return await Http.postEntity<PageResult<GetTymSchemaListVo>>("/tymSchema/getTymSchemaList", dto);
  },

  /**
   * 获取类型映射方案表详情
   */
  getTymSchemaDetails: async (dto: CommonIdDto): Promise<GetTymSchemaDetailsVo> => {
    const result = await Http.postEntity<Result<GetTymSchemaDetailsVo>>("/tymSchema/getTymSchemaDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增类型映射方案表
   */
  addTymSchema: async (dto: AddTymSchemaDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/tymSchema/addTymSchema", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑类型映射方案表
   */
  editTymSchema: async (dto: EditTymSchemaDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/tymSchema/editTymSchema", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除类型映射方案表
   */
  removeTymSchema: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/tymSchema/removeTymSchema", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
  /**
   * 复制类型映射方案
   */
  copyTymSchema: async (dto: CopyTymSchemaDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/tymSchema/copyTymSchema", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
