import Http from "@/commons/Http.ts";
import type PageQuery from "@/commons/model/PageQuery.ts";
import type PageResult from "@/commons/model/PageResult.ts";
import type CommonIdDto from "@/commons/model/CommonIdDto.ts";
import type Result from "@/commons/model/Result.ts";
import axios from "axios";

export interface GetExcelTemplateListDto extends PageQuery {
  id?: number | null;
  attachId?: number | null;
  name?: string | null;
  code?: string | null;
  remark?: string | null;
  status?: number | null;
}

export interface GetExcelTemplateListVo {
  id: number;
  attachId: number;
  name: string;
  code: string;
  remark: string;
  status: number;
  createTime: string;
}

export interface EditExcelTemplateDto {
  id: number;
  name?: string;
  code?: string;
  remark?: string;
  status?: number;
}

export default {
  /**
   * 获取Excel模板列表
   */
  getExcelTemplateList: async (dto: GetExcelTemplateListDto): Promise<PageResult<GetExcelTemplateListVo>> => {
    return await Http.postEntity<PageResult<GetExcelTemplateListVo>>("/excelTemplate/getExcelTemplateList", dto);
  },

  /**
   * 编辑Excel模板
   */
  editExcelTemplate: async (dto: EditExcelTemplateDto): Promise<Result<string>> => {
    return await Http.postEntity<Result<string>>("/excelTemplate/editExcelTemplate", dto);
  },

  /**
   * 删除Excel模板
   */
  removeExcelTemplate: async (dto: CommonIdDto): Promise<string> => {
    const result = await Http.postEntity<Result<string>>("/excelTemplate/removeExcelTemplate", dto);
    if (result.code == 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 上传Excel模板
   */
  uploadExcelTemplate: async (files: File[]): Promise<Result<string>> => {
    try {
      const formData = new FormData();
      files.forEach((file) => {
        formData.append("file", file);
      });

      console.log("开始上传文件，文件数量：", files.length);
      console.log("FormData内容：", formData);

      const response = await Http.axios().post<Result<string>>("/excelTemplate/uploadExcelTemplate", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
          "AE-Request-With": "XHR",
        },
      });

      console.log("上传响应：", response);
      return response.data;
    } catch (error: any) {
      console.error("上传请求异常：", error);
      console.error("错误详情：", error.response);
      throw error;
    }
  },

  /**
   * 下载Excel模板
   */
  downloadExcelTemplate: async (code: string): Promise<void> => {
    const response = await Http.axios().get(`/excelTemplate/downloadExcelTemplate`, {
      params: { code },
      responseType: "blob",
      headers: {
        "AE-Request-With": "XHR",
      },
    });

    const contentType = response.headers["content-type"] as string;

    if (contentType && contentType.includes("application/json")) {
      const text = await response.data.text();
      const result = JSON.parse(text);
      throw new Error(result.message || "下载模板失败");
    }

    const contentDisposition = response.headers["content-disposition"];
    let filename = "template.xlsx";

    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename\*=UTF-8''(.+)/);
      if (filenameMatch && filenameMatch[1]) {
        filename = decodeURIComponent(filenameMatch[1]);
      }
    }

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },
};
