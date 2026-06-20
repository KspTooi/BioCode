import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询列表DTO
 */
export interface GetDataSourceListDto extends PageQuery {
  name?: string; // 数据源名称
  code?: string; // 数据源编码
}

/**
 * 列表VO
 */
export interface GetDataSourceListVo {
  id: string; // 主键ID
  name: string; // 数据源名称
  code: string; // 数据源编码
  kind: number; // 数据源类型 0:MYSQL
  url: string; // 连接字符串
  dbSchema: string; // 默认模式
  createTime: string; // 创建时间
}

/**
 * 详情VO
 */
export interface GetDataSourceDetailsVo {
  id: string; // 主键ID
  name: string; // 数据源名称
  code: string; // 数据源编码
  kind: number; // 数据源类型 0:MYSQL
  drive: string; // JDBC驱动
  url: string; // 连接字符串
  dbSchema: string; // 默认模式
}

/**
 * 新增DTO
 */
export interface AddDataSourceDto {
  name: string; // 数据源名称 max:32
  code: string; // 数据源编码 max:32
  kind: number; // 数据源类型 0:MYSQL
  drive: string; // JDBC驱动 max:80
  url: string; // 连接字符串 max:1000
  username?: string; // 连接用户名 max:320
  password?: string; // 连接密码 max:1280
  dbSchema: string; // 默认模式 max:80
}

/**
 * 编辑DTO
 */
export interface EditDataSourceDto {
  id: string; // 主键ID
  name: string; // 数据源名称 max:32
  code: string; // 数据源编码 max:32
  kind: number; // 数据源类型 0:MYSQL
  drive: string; // JDBC驱动 max:80
  url: string; // 连接字符串 max:1000
  username?: string; // 连接用户名 max:320 留空不修改
  password?: string; // 连接密码 max:1280 留空不修改
  dbSchema: string; // 默认模式 max:80
}

/**
 * 复制数据源DTO
 */
export interface CopyDataSourceDto {
  id: string; // 源数据源ID
  name: string; // 数据源名称 max:32
  code: string; // 数据源编码 max:32
}

/**
 * 表列表VO
 */
export interface GetDataSourceTableListVo {
  tableName: string; // 表名
  tableComment: string; // 表注释
}

export default {
  /**
   * 获取数据源表列表
   */
  getDataSourceList: async (dto: GetDataSourceListDto): Promise<PageResult<GetDataSourceListVo>> => {
    return await Http.postEntity<PageResult<GetDataSourceListVo>>("/dataSource/getDataSourceList", dto);
  },

  /**
   * 获取数据源表详情
   */
  getDataSourceDetails: async (dto: CommonIdDto): Promise<GetDataSourceDetailsVo> => {
    const result = await Http.postEntity<Result<GetDataSourceDetailsVo>>("/dataSource/getDataSourceDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增数据源表
   */
  addDataSource: async (dto: AddDataSourceDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/dataSource/addDataSource", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑数据源表
   */
  editDataSource: async (dto: EditDataSourceDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/dataSource/editDataSource", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除数据源表
   */
  removeDataSource: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/dataSource/removeDataSource", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 复制数据源
   */
  copyDataSource: async (dto: CopyDataSourceDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/dataSource/copyDataSource", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 测试数据源连接
   */
  testDataSourceConnection: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/dataSource/testDataSourceConnection", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 查询数据源表列表
   */
  getDataSourceTableList: async (dto: CommonIdDto): Promise<Result<GetDataSourceTableListVo[]>> => {
    const result = await Http.postEntity<Result<GetDataSourceTableListVo[]>>("/dataSource/getDataSourceTableList", dto);
    if (result.code === 0) {
      return result;
    }
    throw new Error(result.message);
  },
};
