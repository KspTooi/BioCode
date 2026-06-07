import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询待办事项列表Dto
 */
export interface GetQfTodoListDto extends PageQuery {
  nodeName?: string; // 待办名称
  bizFormId?: string; // 业务表单ID
  status?: number; // 待办状态 0:待办 1:已办
}

/**
 * 查询待办事项列表Vo
 */
export interface GetQfTodoListVo {
  id: string; // 主键ID
  nodeName: string; // 当前节点名称 (如: 财务总监审批)
  bizFormName: string; // 业务表单名
  initiatorName: string; // 发起人名
  summary: string; // 摘要(如：张三提交的 5000 元报销)
  status: number; // 待办状态 0:待办 1:已办
  createTime: string; // 任务到达时间
  _mode: "approve" | "view"; // 操作模式 "approve" | "view"
}

/**
 * 查询待办事项详情Vo
 */
export interface GetQfTodoDetailsVo {
  id: string; // 主键ID
  nodeName: string; // 当前节点名称 (如: 财务总监审批)
  summary: string; // 摘要(如：张三提交的 5000 元报销)
  memberId: string; // 办理成员ID (用户ID或用户组编码)
  initiatorId: string; // 发起人ID
  routePc: string; // PC端路由名
  routeMobile: string; // 移动端路由名
  dataId: string; // 物理表数据主键ID
  engProcId: string; // 引擎流程ID
  allowComment: number; // 允许填写审批意见 0:不允许 1:允许
  // 操作类型 0:同意 1:驳回 2:转交 3:驳回节点
  allowActions: { kind: number; name: string }[];
  allowEditFields: string[]; // 允许编辑的字段
  status?: number; // 待办状态 0:待办 1:已办 10:已作废
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
  action: number; // 操作 0:同意 1:驳回 2:转交 3:驳回节点
  comment: string; // 审批意见
  nodeId: string; // 节点ID
  memberId: string; // 办理成员ID(用于转交，只能是用户ID)
}

/**
 * 审批流程流转记录Vo
 */
export interface ApproveFlowRecordVo {
  nodeName: string; // 节点名称
  finMemberName: string; // 节点审批人
  finTime: string; // 节点审批时间
  action: number; // 节点审批结果 0:同意 1:驳回
  comment: string; // 审批意见
  status: number; // 待办状态 0:待办 1:已办
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

  /**
   * 获取待办事项流程图
   */
  getQfTodoApproveFlow: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/qfTodo/getQfTodoApproveFlow", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 获取待办事项流程流转记录
   */
  getQfTodoApproveFlowRecord: async (dto: CommonIdDto): Promise<ApproveFlowRecordVo[]> => {
    const result = await Http.postEntity<Result<ApproveFlowRecordVo[]>>("/qfTodo/getQfTodoApproveFlowRecord", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },
};
