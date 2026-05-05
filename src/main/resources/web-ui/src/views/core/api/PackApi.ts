import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询菜单包列表Dto
 */
export interface GetPackListDto extends PageQuery {
  name?: string; // 菜单包名
  code?: string; // 菜单包编码
  status?: number; // 状态 0:禁用 1:启用
}

/**
 * 查询菜单包列表Vo
 */
export interface GetPackListVo {
  id: string; // 主键ID
  name: string; // 菜单包名
  code: string; // 菜单包编码
  status: number; // 状态 0:禁用 1:启用
  seq: number; // 排序
  createTime: string; // 创建时间
}

/**
 * 查询菜单包详情Vo
 */
export interface GetPackDetailsVo {
  id: string; // 主键ID
  name: string; // 菜单包名
  code: string; // 菜单包编码
  status: number; // 状态 0:禁用 1:启用
  seq: number; // 排序
  remark: string; // 备注
}

/**
 * 新增菜单包Dto
 */
export interface AddPackDto {
  name: string; // 菜单包名
  code: string; // 菜单包编码
  status: number; // 状态 0:禁用 1:启用
  seq: number; // 排序
  remark: string; // 备注
}

/**
 * 编辑菜单包Dto
 */
export interface EditPackDto {
  id: string; // 主键ID
  name: string; // 菜单包名
  status: number; // 状态 0:禁用 1:启用
  seq: number; // 排序
  remark: string; // 备注
}

export default {
  /**
   * 获取菜单包列表
   */
  getPackList: async (dto: GetPackListDto): Promise<PageResult<GetPackListVo>> => {
    return await Http.postEntity<PageResult<GetPackListVo>>("/pack/getPackList", dto);
  },

  /**
   * 获取菜单包详情
   */
  getPackDetails: async (dto: CommonIdDto): Promise<GetPackDetailsVo> => {
    const result = await Http.postEntity<Result<GetPackDetailsVo>>("/pack/getPackDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增菜单包
   */
  addPack: async (dto: AddPackDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/pack/addPack", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑菜单包
   */
  editPack: async (dto: EditPackDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/pack/editPack", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除菜单包
   */
  removePack: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/pack/removePack", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
