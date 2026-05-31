import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询业务表单列表Dto
 */
export interface GetQfBizFormListDto extends PageQuery {
  name?: string; // 业务名称
  code?: string; // 业务编码
  tableName?: string; // 物理表名
  status?: number; // 状态 0:正常 1:停用
  seq?: number; // 排序
}

/**
 * 查询业务表单列表Vo
 */
export interface GetQfBizFormListVo {
  id: string; // 主键ID
  name: string; // 业务名称
  code: string; // 业务编码
  formType: number; // 表单类型 0:手搓表单 1:动态表单
  tableName: string; // 物理表名
  status: number; // 状态 0:正常 1:停用
  seq: number; // 排序
}

/**
 * 查询业务表单详情Vo
 */
export interface GetQfBizFormDetailsVo {
  id: string; // 主键ID
  name: string; // 业务名称
  code: string; // 业务编码
  formType: number; // 表单类型 0:手搓表单 1:动态表单
  icon: string; // 表单图标
  tableName: string; // 物理表名
  routePc?: string; // PC端路由名
  routeMobile?: string; // 移动端路由名
  status: number; // 状态 0:正常 1:停用
  seq: number; // 排序
  summaryTemplate?: string; // 摘要模板
}

/**
 * 新增业务表单Dto
 */
export interface AddQfBizFormDto {
  name: string; // 业务名称
  code: string; // 业务编码
  formType: number; // 表单类型 0:手搓表单 1:动态表单
  icon: string; // 表单图标
  tableName: string; // 物理表名
  routePc?: string; // PC端路由名
  routeMobile?: string; // 移动端路由名
  status: number; // 状态 0:正常 1:停用
  seq: number; // 排序
  summaryTemplate?: string; // 摘要模板
}

/**
 * 编辑业务表单Dto
 */
export interface EditQfBizFormDto {
  id: string; // 主键ID
  name: string; // 业务名称
  formType: number; // 表单类型 0:手搓表单 1:动态表单
  icon: string; // 表单图标
  tableName: string; // 物理表名
  routePc?: string; // PC端路由名
  routeMobile?: string; // 移动端路由名
  status: number; // 状态 0:正常 1:停用
  seq: number; // 排序
  summaryTemplate?: string; // 摘要模板
}

export default {
  /**
   * 获取业务表单列表
   */
  getQfBizFormList: async (dto: GetQfBizFormListDto): Promise<PageResult<GetQfBizFormListVo>> => {
    return await Http.postEntity<PageResult<GetQfBizFormListVo>>("/bizForm/getBizFormList", dto);
  },

  /**
   * 获取业务表单详情
   */
  getQfBizFormDetails: async (dto: CommonIdDto): Promise<GetQfBizFormDetailsVo> => {
    const result = await Http.postEntity<Result<GetQfBizFormDetailsVo>>("/bizForm/getBizFormDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增业务表单
   */
  addQfBizForm: async (dto: AddQfBizFormDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/bizForm/addBizForm", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑业务表单
   */
  editQfBizForm: async (dto: EditQfBizFormDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/bizForm/editBizForm", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除业务表单
   */
  removeQfBizForm: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/bizForm/removeBizForm", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
