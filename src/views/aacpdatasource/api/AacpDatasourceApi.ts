import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询AACP数据源列表Dto
 */
export interface GetAacpDatasourceListDto extends PageQuery {
  name?: string; // 数据源名称
  code?: string; // 数据源编码
}

/**
 * 查询AACP数据源列表Vo
 */
export interface GetAacpDatasourceListVo {
  id: string; // 主键ID
  name: string; // 数据源名称
  code: string; // 数据源编码
  kind: number; // 数据源类型 0:MYSQL
  url: string; // 连接字符串
  defaultDb: string; // 默认数据库
  queryMaxRows: number; // 最大查询行数
  executeBatch: number; // 是否支持批处理 0:不支持 1:支持
}

/**
 * 查询AACP数据源详情Vo
 */
export interface GetAacpDatasourceDetailsVo {
  id: string; // 主键ID
  name: string; // 数据源名称
  code: string; // 数据源编码
  kind: number; // 数据源类型 0:MYSQL
  drive: string; // JDBC驱动
  url: string; // 连接字符串
  username: string; // 连接用户名
  password: string; // 连接密码
  defaultDb: string; // 默认数据库
  queryMaxRows: number; // 最大查询行数
  executeBatch: number; // 是否支持批处理 0:不支持 1:支持
}

/**
 * 新增AACP数据源Dto
 */
export interface AddAacpDatasourceDto {
  name: string; // 数据源名称
  code: string; // 数据源编码
  kind: number; // 数据源类型 0:MYSQL
  drive: string; // JDBC驱动
  url: string; // 连接字符串
  username: string; // 连接用户名
  password: string; // 连接密码
  defaultDb: string; // 默认数据库
  queryMaxRows: number; // 最大查询行数
  executeBatch: number; // 是否支持批处理 0:不支持 1:支持
}

/**
 * 编辑AACP数据源Dto
 */
export interface EditAacpDatasourceDto {
  id: string; // 主键ID
  name: string; // 数据源名称
  code: string; // 数据源编码
  kind: number; // 数据源类型 0:MYSQL
  drive: string; // JDBC驱动
  url: string; // 连接字符串
  username: string; // 连接用户名
  password: string; // 连接密码
  defaultDb: string; // 默认数据库
  queryMaxRows: number; // 最大查询行数
  executeBatch: number; // 是否支持批处理 0:不支持 1:支持
}

export default {
  /**
   * 获取AACP数据源列表
   */
  getAacpDatasourceList: async (dto: GetAacpDatasourceListDto): Promise<PageResult<GetAacpDatasourceListVo>> => {
    return await Http.postEntity<PageResult<GetAacpDatasourceListVo>>("/aacpDatasource/getAacpDatasourceList", dto);
  },

  /**
   * 获取AACP数据源详情
   */
  getAacpDatasourceDetails: async (dto: CommonIdDto): Promise<GetAacpDatasourceDetailsVo> => {
    const result = await Http.postEntity<Result<GetAacpDatasourceDetailsVo>>("/aacpDatasource/getAacpDatasourceDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增AACP数据源
   */
  addAacpDatasource: async (dto: AddAacpDatasourceDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpDatasource/addAacpDatasource", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑AACP数据源
   */
  editAacpDatasource: async (dto: EditAacpDatasourceDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpDatasource/editAacpDatasource", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除AACP数据源
   */
  removeAacpDatasource: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/aacpDatasource/removeAacpDatasource", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
