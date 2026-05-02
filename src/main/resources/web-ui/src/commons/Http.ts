import type Result from "@/commons/model/Result.ts";
import axios, { type AxiosInstance } from "axios";
import GenricRouteService from "@/soa/genric-route/service/GenricRouteService";
import UserAuthService from "@/views/auth/service/UserAuthService";

const vueRouter = GenricRouteService.getVueRouter();

// 创建axios实例
const axiosInstance = axios.create({
  baseURL: "/api", // 统一为所有请求添加 /api 前缀，配合 Vite 代理
  // 为所有请求添加自定义请求头
  headers: {
    "AE-Request-With": "XHR",
  },
});

// 添加请求拦截器(如果用户已登录，则添加Authorization头)
axiosInstance.interceptors.request.use((config) => {
  const sessionId = UserAuthService.AuthStore().getSessionId;
  if (sessionId) {
    config.headers.Authorization = `Bearer ${sessionId}`;
  }
  return config;
});

// 添加响应拦截器
axiosInstance.interceptors.response.use(
  (response) => {
    //处理Http200状态码
    if (response.status === 200) {
      //检查响应体是否是Result类型
      const data = response.data as Result<unknown>;
      if (data.code === 102) {
        //ElMessage.warning(data.message);
      }
    }

    return response;
  },
  (error: any): Promise<never> => {
    console.log("----------------- ERROR -----------------", error.response);

    //处理Http403状态码
    if (error.response?.status === 403) {
      //跳转到无权限页面
      vueRouter.push({
        name: "no-permission",
        query: {
          message: "权限不足",
        },
      });
      const data = error.response.data as Result<unknown>;
      return Promise.reject(new Error(data.message || "权限不足"));
    }

    //处理Http401状态码
    if (error.response?.status === 401) {
      //跳转到登录页面
      vueRouter.push({
        path: "/auth/login",
      });
      UserAuthService.AuthStore().clearAuth();
      return Promise.reject(new Error("登录已过期或未登录"));
    }

    // 处理HTTP 400 (参数校验失败)
    if (error.response?.status === 400) {
      const data = error.response.data;
      if (data && typeof data === "object" && data.code === 1 && typeof data.message === "string") {
        // 直接抛出错误，让调用方处理
        throw new Error(`参数校验失败: \n${data.message}`);
      }
    }

    // 对于其他未处理的错误，继续传递
    return Promise.reject(error);
  }
);

export default {
  /**
   * 获取Axios实例
   * @returns Axios实例
   */
  axios: (): AxiosInstance => {
    return axiosInstance;
  },
  /**
   * 发送原始POST请求，不进行任何参数过滤或拦截处理
   * @param url 请求URL
   * @param body 请求体
   * @param config Axios配置
   * @returns 原始响应
   */
  async post<T = any>(url: string, body?: any, config?: any): Promise<Result<T>> {
    try {
      const response = await axiosInstance.post<Result<T>>(url, body, config);
      return response.data as Result<T>;
    } catch (error) {
      return {
        code: 2,
        data: null as T,
        message: error instanceof Error ? error.message : "请求失败",
      };
    }
  },

  /**
   * 解析URL 这会使用Axios中配置的baseURL和代理配置来解析URL
   * @param url 请求URL
   * @returns 解析后的URL
   */
  resolve(url: string): string {
    //获取Axios实例的baseURL
    const baseURL = axiosInstance.defaults.baseURL;
    return baseURL + url;
  },

  /**
   * 获取当前的后端接口地址, 这会使用Axios实例的baseURL和代理配置来获取当前的后端接口地址
   * @returns 当前的后端接口地址
   */
  getApiUrl(): string {
    //获取当前页面的协议、主机名和端口
    const origin = window.location.origin;
    const baseURL = axiosInstance.defaults.baseURL;
    return origin + baseURL;
  },

  /**
   * 发送POST请求并返回Result包装的响应
   * @param url 请求URL
   * @param body 请求体
   * @returns Result包装的响应
   */
  async postRaw<T>(url: string, body: any): Promise<Result<T>> {
    try {
      // 过滤空字符串参数，不应影响原始对象

      const filteredBody = { ...body }; // 创建body的副本
      Object.keys(filteredBody).forEach((key) => {
        if (filteredBody[key] === "") {
          delete filteredBody[key];
        }
      });
      const response = await axiosInstance.post<Result<T>>(url, filteredBody);
      return response.data;
    } catch (error) {
      // 拦截器已经处理了特定情况，这里只返回通用错误
      return {
        code: 2,
        data: null as T,
        message: error instanceof Error ? error.message : "请求失败",
      };
    }
  },

  /**
   * 发送POST请求并直接返回数据部分
   * @param url 请求URL
   * @param body 请求体
   * @returns 响应数据
   */
  async postEntity<T>(url: string, body: any): Promise<T> {
    const result = await this.postRaw<T>(url, body);

    // 拦截器已经处理了权限不足(code=2)和需要登录(401)的情况
    // 这里只需要处理 code 为 0 的成功情况和其他业务错误码
    if (result.code === 0) {
      // code 为 0 但 data 为 null 或 undefined 也可能是一种业务失败，但暂时按成功处理
      // 如果需要严格检查 data，可以在这里添加判断
      return result as T;
    }

    //处理Code为1的服务器内部错误 由各SFC各自处理
    //if (result.code === 1) {
    //  //直接弹出CONFIRM对话框 不显示取消按钮
    //  ElMessageBox.confirm(result.message || "服务器内部错误", "服务器内部错误", {
    //    confirmButtonText: "我明白了",
    //    showCancelButton: false,
    //    type: "warning",
    //    closeOnClickModal: false,
    //  }).then(() => {
    //    throw new Error(result.message || "服务器内部错误");
    //  });
    //  return Promise.reject(new Error(result.message || "服务器内部错误"));
    //}

    //需要团队
    if (result.code === 101) {
      return result as T;
    }

    // 抛出其他业务错误信息 (code != 0 且非权限不足)
    throw new Error(result.message || "请求失败或数据无效");
  },

  /**
   * 发送POST请求(FormData)
   * @param url 请求URL
   * @param body 请求体
   * @returns 响应数据
   */
  async postForm<T>(url: string, body: any): Promise<T> {
    try {
      const formData = new FormData();
      Object.keys(body).forEach((key) => {
        if (body[key] === null || body[key] === undefined || body[key] === "") {
          return;
        }
        formData.append(key, body[key]);
      });

      const response = await axiosInstance.post<Result<T>>(url, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      const result = response.data;

      if (result.code === 0) {
        return result as T;
      }

      throw new Error(result.message || "请求失败或数据无效");
    } catch (error) {
      if (error instanceof Error) {
        throw error;
      }
      throw new Error("请求失败或数据无效", { cause: error });
    }
  },
};
