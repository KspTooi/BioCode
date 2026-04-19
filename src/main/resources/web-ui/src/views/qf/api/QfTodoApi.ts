import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询待办事项列表Dto
 */
export interface GetQfTodoListDto extends PageQuery {
  summary?: string; // 摘要(如：张三提交的 5000 元报销)
  memberId?: string; // 办理成员ID (用户ID或用户组标识)
  initiatorId?: string; // 发起人ID
  createTime?: string; // 任务到达时间
}

/**
 * 查询待办事项列表Vo
 */
export interface GetQfTodoListVo {
  id: string; // 主键ID
  nodeName: string; // 当前节点名称 (如: 财务总监审批)
  summary: string; // 摘要(如：张三提交的 5000 元报销)
  memberId: string; // 办理成员ID (用户ID或用户组标识)
  initiatorId: string; // 发起人ID
  initiatorName: string; // 发起人名
  initiatorTime: string; // 发起时间
  createTime: string; // 任务到达时间
}

/**
 * 查询待办事项详情Vo
 */
export interface GetQfTodoDetailsVo {
  id: string; // 主键ID
  nodeName: string; // 当前节点名称 (如: 财务总监审批)
  summary: string; // 摘要(如：张三提交的 5000 元报销)
  memberId: string; // 办理成员ID (用户ID或用户组标识)
  initiatorId: string; // 发起人ID
}

/**
 * 编辑待办事项Dto
 */
export interface EditQfTodoDto {
  id: string; // 主键ID
}

/**
 * 审批待办事项Dto
 */
export interface ApproveQfTodoDto {
  id: string; // 主键ID
  action: number; // 操作 0:同意 1:驳回
  comment: string; // 审批意见
}

export default {
  /**
   * 获取待办事项列表
   */
  getQfTodoList: async (dto: GetQfTodoListDto): Promise<PageResult<GetQfTodoListVo>> => {
    return await Http.postEntity<PageResult<GetQfTodoListVo>>("/qfTodo/getQfTodoList", dto);
  },

  /**
   * 获取待办事项详情
   */
  getQfTodoDetails: async (dto: CommonIdDto): Promise<GetQfTodoDetailsVo> => {
    const result = await Http.postEntity<Result<GetQfTodoDetailsVo>>("/qfTodo/getQfTodoDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑待办事项
   */
  editQfTodo: async (dto: EditQfTodoDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfTodo/editQfTodo", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除待办事项
   */
  removeQfTodo: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfTodo/removeQfTodo", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 审批待办事项
   */
  approveQfTodo: async (dto: ApproveQfTodoDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfTodo/approveQfTodo", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
