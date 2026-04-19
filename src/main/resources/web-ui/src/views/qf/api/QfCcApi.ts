import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询抄送列表Dto
 */
export interface GetQfCcListDto extends PageQuery {
  summary?: string; // 摘要(如：张三提交的 5000 元报销)
  fromName?: string; // 抄送发起人姓名
  isRead?: number; // 是否读 0:未读 1:已读
}

/**
 * 查询抄送列表Vo
 */
export interface GetQfCcListVo {
  id: string; // 主键ID
  nodeName: string; // 当前节点名称 (如: 财务总监审批)
  summary: string; // 摘要(如：张三提交的 5000 元报销)
  fromName: string; // 抄送发起人姓名
  isRead: number; // 是否读 0:未读 1:已读
  readTime: string; // 读取时间
  createTime: string; // 抄送时间
}

/**
 * 查询抄送详情Vo
 */
export interface GetQfCcDetailsVo {
  id: string; // 主键ID
  engProcId: string; // 引擎流程ID
  bizFormId: string; // 业务表单ID
  tableName: string; // 物理表名(带入业务表单数据)
  dataId: string; // 物理表数据主键ID
  nodeName: string; // 当前节点名称 (如: 财务总监审批)
  summary: string; // 摘要(如：张三提交的 5000 元报销)
  fromId: string; // 抄送发起人ID(自动抄送为null)
  fromName: string; // 抄送发起人姓名
  isRead: number; // 是否读 0:未读 1:已读
}

/**
 * 编辑抄送Dto
 */
export interface EditQfCcDto {
  id: string; // 主键ID
}

export default {
  /**
   * 获取抄送列表
   */
  getQfCcList: async (dto: GetQfCcListDto): Promise<PageResult<GetQfCcListVo>> => {
    return await Http.postEntity<PageResult<GetQfCcListVo>>("/qfCc/getQfCcList", dto);
  },

  /**
   * 获取抄送详情
   */
  getQfCcDetails: async (dto: CommonIdDto): Promise<GetQfCcDetailsVo> => {
    const result = await Http.postEntity<Result<GetQfCcDetailsVo>>("/qfCc/getQfCcDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑抄送
   */
  editQfCc: async (dto: EditQfCcDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfCc/editQfCc", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除抄送
   */
  removeQfCc: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfCc/removeQfCc", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
