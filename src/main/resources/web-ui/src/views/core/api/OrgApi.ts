import Http from "@/commons/Http.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery";
import type PageResult from "@/commons/model/PageResult";
import type Result from "@/commons/model/Result.ts";

export interface GetOrgTreeDto {
  name?: string; // 组织机构名称
  topId?: string; // 顶级组织ID
  orgId?: string; // 直属企业ID
}

export interface GetOrgListDto extends PageQuery {
  orgIds: string[]; //组织机构ID集合
}

export interface GetOrgListVo {
  id: string; // 主键id
  topId: string; // 顶级组织ID
  parentId: string | null; // 上级组织ID NULL顶级组织
  kind: number; // 0:企业 1:子企业 2:部门
  name: string; // 组织机构名称
  level: number; // 级别
  seq: number; // 排序
}

export interface GetOrgTreeVo {
  id: string; // 主键id
  orgId: string; // 直属企业ID
  topId: string; // 顶级组织ID
  parentId: string | null; // 上级组织ID NULL顶级组织
  kind: number; // 0:企业 1:子企业 2:部门
  name: string; // 组织机构名称
  level: number; // 级别
  seq: number; // 排序
  children: GetOrgTreeVo[]; // 子组织
}

export interface GetOrgDetailsVo {
  id: string; // 主键id
  topId?: string; // 顶级组织ID
  orgId?: string; // 直属企业ID
  parentId: string | null; // 上级组织ID NULL顶级组织
  kind: number; // 0:企业 1:子企业 2:部门
  name: string; // 组织机构名称
  shortName: string; // 组织机构简称
  seq: number; // 排序
  remark: string; // 备注
}

export interface AddOrgDto {
  parentId?: string | null; // 上级组织ID NULL顶级组织
  kind: number; // 0:企业 1:子企业 2:部门 3:班组
  name: string; // 组织机构名称
  shortName: string; // 组织机构简称
  seq: number; // 排序
  remark?: string; // 备注
}

export interface EditOrgDto {
  id: string; // 主键id
  parentId?: string | null; // 上级组织ID NULL顶级组织
  name: string; // 组织机构名称
  shortName: string; // 组织机构简称
  seq: number; // 排序
  remark?: string; // 备注
}

export default {
  /**
   * 获取组织机构树
   */
  getOrgTree: async (dto: GetOrgTreeDto): Promise<GetOrgTreeVo[]> => {
    const result = await Http.postEntity<Result<GetOrgTreeVo[]>>("/org/getOrgTree", dto);
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 获取组织机构列表
   */
  getOrgList: async (dto: GetOrgListDto): Promise<GetOrgListVo[]> => {
    const result = await Http.postEntity<PageResult<GetOrgListVo>>("/org/getOrgList", dto);
    if (result.code == 0) {
      return result.data;
    }
  },

  /**
   * 获取组织机构详情
   */
  getOrgDetails: async (dto: CommonIdDto): Promise<GetOrgDetailsVo> => {
    const result = await Http.postEntity<Result<GetOrgDetailsVo>>("/org/getOrgDetails", dto);
    if (result.code == 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增组织机构
   */
  addOrg: async (dto: AddOrgDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/org/addOrg", dto);
  },

  /**
   * 编辑组织机构
   */
  editOrg: async (dto: EditOrgDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/org/editOrg", dto);
  },

  /**
   * 删除组织机构
   */
  removeOrg: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/org/removeOrg", dto);
    if (result.code == 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
