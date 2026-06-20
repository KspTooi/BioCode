import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询列表DTO
 */
export interface GetScmListDto extends PageQuery {
  name?: string; // SCM名称
  projectName?: string; // 项目名称
}

/**
 * 列表VO
 */
export interface GetScmListVo {
  id: string; // 主键ID
  name: string; // SCM名称
  projectName: string; // 项目名称
  scmUrl: string; // SCM仓库地址
  createTime: string; // 创建时间
}

/**
 * 详情VO
 */
export interface GetScmDetailsVo {
  id: string; // 主键ID
  name: string; // SCM名称
  projectName: string; // 项目名称
  scmUrl: string; // SCM仓库地址
  scmAuthKind: number; // SCM认证方式 0:公开 1:账号密码 2:SSH KEY 3:PAT
  scmUsername: string; // SCM用户名
  scmPassword: string; // SCM密码
  scmPk: string; // SSH KEY
  scmBranch: string; // SCM分支
  remark: string; // SCM备注
}

/**
 * 新增DTO
 */
export interface AddScmDto {
  name: string; // SCM名称
  projectName: string; // 项目名称
  scmUrl: string; // SCM仓库地址
  scmAuthKind: number; // SCM认证方式 0:公开 1:账号密码 2:SSH KEY 3:PAT
  scmUsername: string; // SCM用户名
  scmPassword: string; // SCM密码
  scmPk: string; // SSH KEY
  scmBranch: string; // SCM分支
  remark: string; // SCM备注
}

/**
 * 编辑DTO
 */
export interface EditScmDto {
  id: string; // 主键ID
  name: string; // SCM名称
  projectName: string; // 项目名称
  scmUrl: string; // SCM仓库地址
  scmAuthKind: number; // SCM认证方式 0:公开 1:账号密码 2:SSH KEY 3:PAT
  scmUsername: string; // SCM用户名
  scmPassword: string; // SCM密码
  scmPk: string; // SSH KEY
  scmBranch: string; // SCM分支
  remark: string; // SCM备注
}

/**
 * 获取导航锚点DTO
 */
export interface GetAnchorPointsDto {
  scmId: string; // SCM ID
  kind: number; // 类型 0:输入 1:输出
}

/**
 * 获取导航锚点VO
 */
export interface GetAnchorPointsVo {
  name: string; // 名称
  relativePath: string; // 相对路径
}

/**
 * 复制SCM DTO
 */
export interface CopyScmDto {
  id: string; // 源SCM ID
  name: string; // SCM名称 max:32
}

export default {
  /**
   * 获取SCM列表
   */
  getScmList: async (dto: GetScmListDto): Promise<PageResult<GetScmListVo>> => {
    return await Http.postEntity<PageResult<GetScmListVo>>("/scm/getScmList", dto);
  },

  /**
   * 获取SCM详情
   */
  getScmDetails: async (dto: CommonIdDto): Promise<GetScmDetailsVo> => {
    const result = await Http.postEntity<Result<GetScmDetailsVo>>("/scm/getScmDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增SCM
   */
  addScm: async (dto: AddScmDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/scm/addScm", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑SCM
   */
  editScm: async (dto: EditScmDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/scm/editScm", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除SCM
   */
  removeScm: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/scm/removeScm", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 复制SCM
   */
  copyScm: async (dto: CopyScmDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/scm/copyScm", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 测试SCM连接
   */
  testScmConnection: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/scm/testScmConnection", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 获取导航锚点
   */
  getAnchorPoints: async (dto: GetAnchorPointsDto): Promise<GetAnchorPointsVo[]> => {
    const result = await Http.postEntity<Result<GetAnchorPointsVo[]>>("/scm/getAnchorPoints", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },
};
