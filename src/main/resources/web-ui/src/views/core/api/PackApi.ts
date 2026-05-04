import Http from "@/commons/Http.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type Result from "@/commons/model/Result.ts";

/**
 * 查询菜单包列表Dto
 */
export interface GetPackageListDto extends PageQuery {
  name?: string; // 菜单包名
  code?: string; // 菜单包编码
  status?: number; // 状态 0:禁用 1:启用
}

/**
 * 查询菜单包列表Vo
 */
export interface GetPackageListVo {
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
export interface GetPackageDetailsVo {
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
export interface AddPackageDto {
  name: string; // 菜单包名
  code: string; // 菜单包编码
  status: number; // 状态 0:禁用 1:启用
  seq: number; // 排序
  remark: string; // 备注
}

/**
 * 编辑菜单包Dto
 */
export interface EditPackageDto {
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
  getPackageList: async (dto: GetPackageListDto): Promise<PageResult<GetPackageListVo>> => {
    return await Http.postEntity<PageResult<GetPackageListVo>>("/package/getPackageList", dto);
  },

  /**
   * 获取菜单包详情
   */
  getPackageDetails: async (dto: CommonIdDto): Promise<GetPackageDetailsVo> => {
    const result = await Http.postEntity<Result<GetPackageDetailsVo>>("/package/getPackageDetails", dto);
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 新增菜单包
   */
  addPackage: async (dto: AddPackageDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/package/addPackage", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 编辑菜单包
   */
  editPackage: async (dto: EditPackageDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/package/editPackage", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 删除菜单包
   */
  removePackage: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/package/removePackage", dto);
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },
};
