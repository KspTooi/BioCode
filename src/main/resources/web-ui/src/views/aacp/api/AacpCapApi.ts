import type PageQuery from "@/commons/model/PageQuery.ts";
import Http from "@/commons/Http.ts";
import type RestPageableView from "@/commons/model/RestPageableView.ts";
import type Result from "@/commons/model/Result.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";

export interface GetCapListDto extends PageQuery {
  name: string | null; //能力包名称
  kind: number | null; //类型 0:微函数
}

export interface GetCapListVo {
  id: string; //主键ID
  name: string; //能力包名称
  kind: number; //类型 0:微函数
  remark: string; //备注
  funcCount: number; //关联微函数数量
  datasourceCount: number; //关联数据源数量
}

export interface AddCapDto {
  name: string | null; //能力包名称
  kind: number | null; //类型 0:微函数
  remark: string | null; //备注
  funcIds: string[]; //微函数ID列表
  datasourceIds: string[]; //数据源ID列表
}

export interface EditCapDto {
  id: string | null; //主键ID
  name: string | null; //能力包名称
  kind: number | null; //类型 0:微函数
  remark: string | null; //备注
  funcIds: string[]; //微函数ID列表
  datasourceIds: string[]; //数据源ID列表
}

export interface GetCapDetailsVo {
  id: string; //主键ID
  name: string | null; //能力包名称
  kind: number | null; //类型 0:微函数
  remark: string | null; //备注
  funcIds: string[]; //关联的微函数ID列表
  datasourceIds: string[]; //关联的数据源ID列表
}

export default {
  /**
   * 获取能力包列表
   * @param dto 查询条件
   * @returns 能力包列表
   */
  getCapList: async (dto: GetCapListDto): Promise<RestPageableView<GetCapListVo>> => {
    const ret = await Http.postEntity<RestPageableView<GetCapListVo>>("/cap/getCapList", dto);
    return ret;
  },

  /**
   * 添加能力包
   * @param dto 能力包信息
   * @returns 操作结果
   */
  addCap: async (dto: AddCapDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/cap/addCap", dto);
  },

  /**
   * 编辑能力包
   * @param dto 能力包信息
   * @returns 操作结果
   */
  editCap: async (dto: EditCapDto): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/cap/editCap", dto);
  },

  /**
   * 获取能力包详情
   * @param id 能力包ID
   * @returns 能力包详情
   */
  getCapDetails: async (id: string): Promise<GetCapDetailsVo> => {
    const ret = await Http.postEntity<Result<GetCapDetailsVo>>("/cap/getCapDetails", {
      id: id,
    } as CommonIdDto);
    return ret.data;
  },

  /**
   * 删除能力包
   * @param id 能力包ID
   * @returns 操作结果
   */
  removeCap: async (id: string): Promise<Result<void>> => {
    return await Http.postEntity<Result<void>>("/cap/removeCap", { id: id } as CommonIdDto);
  },
};
