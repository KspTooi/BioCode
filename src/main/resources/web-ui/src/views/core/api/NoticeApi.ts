import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询列表DTO
 */
export interface GetNoticeListDto extends PageQuery {
  title?: string; // 标题
  kind?: number; // 种类: 0公告, 1业务提醒, 2私信
  content?: string; // 通知内容
  priority?: number; // 优先级: 0:低 1:中 2:高
  category?: string; // 业务类型/分类
  senderName?: string; // 发送人姓名
}

/**
 * 列表VO
 */
export interface GetNoticeListVo {
  id: string; // 主键ID
  title: string; // 标题
  kind: number; // 种类: 0公告, 1业务提醒, 2私信
  priority: number; // 优先级: 0:低 1:中 2:高
  category: string; // 业务类型/分类
  senderName: string; // 发送人姓名
  targetKind: number; // 接收对象类型 0:全员 1:指定部门 2:指定用户
  targetCount: number; // 接收人数
  createTime: string; // 创建时间
}

/**
 * 详情VO
 */
export interface GetNoticeDetailsVo {
  id: string; // 主键ID
  title: string; // 标题
  kind: number; // 种类: 0公告, 1业务提醒, 2私信
  content: string; // 通知内容
  priority: number; // 优先级: 0:低 1:中 2:高
  category: string; // 业务类型/分类
  targetKind: number; // 接收对象类型 0:全员 1:指定部门 2:指定用户
  senderId: number; // 发送人ID (NULL为系统)
  senderName: string; // 发送人姓名
  forward: string; // 跳转URL/路由地址
  params: string; // 动态参数 (JSON格式)
  createTime: string; // 创建时间
  _targetIds: string[]; // 接收对象ID列表
}

/**
 * 新增DTO
 */
export interface AddNoticeDto {
  title: string; // 标题
  kind: number; // 种类: 0公告, 1业务提醒, 2私信
  content?: string; // 通知内容
  priority: number; // 优先级: 0:低 1:中 2:高
  category?: string; // 业务类型/分类
  targetKind: number; // 接收对象类型 0:全员 1:指定部门 2:指定用户
  targetIds: string[]; // 接收对象ID列表
}

/**
 * 编辑DTO
 */
export interface EditNoticeDto {
  id: string; // 主键ID
  title: string; // 标题
  kind: number; // 种类: 0公告, 1业务提醒, 2私信
  content?: string; // 通知内容
  priority: number; // 优先级: 0:低 1:中 2:高
  category?: string; // 业务类型/分类
}

export default {
  /**
   * 获取消息表列表
   */
  getNoticeList: async (dto: GetNoticeListDto): Promise<PageResult<GetNoticeListVo>> => {
    return await Http.postEntity<PageResult<GetNoticeListVo>>("/notice/getNoticeList", dto);
  },

  /**
   * 获取消息表详情
   */
  getNoticeDetails: async (dto: CommonIdDto): Promise<GetNoticeDetailsVo> => {
    const result = await Http.postEntity<Result<GetNoticeDetailsVo>>("/notice/getNoticeDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增消息表
   */
  addNotice: async (dto: AddNoticeDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/notice/addNotice", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑消息表
   */
  editNotice: async (dto: EditNoticeDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/notice/editNotice", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除消息表
   */
  removeNotice: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/notice/removeNotice", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
