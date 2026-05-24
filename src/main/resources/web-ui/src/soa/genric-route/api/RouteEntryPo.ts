import type { Component } from "vue";

/**
 * 路由条目类
 * 用于构建路由条目
 */
export class RouteEntryPo {
  biz: string; //领域或业务代码 为null时直接使用path
  path: string; //路由路径
  name: string; //路由名称 为null时直接使用path
  component: () => Promise<Component>; //可异步加载的组件
  meta: {
    keepAlive?: boolean; //是否缓存
    breadcrumb?: string | null; //面包屑名称 为null时直接使用path
    layout?: string | null; //布局,由布局管理器决定，null或default时使用默认布局、blank:全局布局(不带控制台框架)
  };

  /**
   * 构造函数
   * @param props 属性
   */
  public constructor(props?: {
    biz: string;
    path: string;
    name: string;
    component: () => Promise<Component>;
    meta: {
      keepAlive?: boolean;
      breadcrumb?: string | null;
      layout?: string | null;
    };
  }) {
    if (!props) {
      return;
    }
    this.biz = props.biz;
    this.path = props.path;
    this.name = props.name;
    this.component = props.component;
    this.meta = props.meta;
  }

  /**
   * 验证路由条目
   */
  public validate(): void {
    //path和component不能为空
    if (this.path == null || this.component == null) {
      throw new Error("path和component不能为空");
    }

    //path不允许包含/
    if (this.path.includes("/")) {
      throw new Error("path不允许包含/");
    }

    //Name为空时使用path
    if (this.name == null) {
      this.name = this.path;
    }

    //meta为空时初始化
    if (this.meta == null) {
      this.meta = {};
    }

    //meta.breadcrumb为空时使用path
    if (this.meta.breadcrumb == null) {
      this.meta.breadcrumb = this.path;
    }

    //面包屑为空时 将面包屑注册为path
    if (this.meta.breadcrumb == null) {
      this.meta.breadcrumb = this.path;
    }

    //布局为空时 使用默认布局
    if (this.meta.layout == null) {
      this.meta.layout = "default";
    }
  }

  /**
   * 构建路由路径
   * 这个方法会根据biz和path构建路由路径
   *
   * 例如: biz = auth path = login 则返回 /auth/login
   * @returns 构建完成的路由路径
   */
  public buildPath(): string {
    if (this.biz == null) {
      return "/" + this.path;
    }
    return "/" + this.biz + "/" + this.path;
  }

  public static build(props: {
    biz: string;
    path: string;
    name: string;
    component: () => Promise<Component>;
    meta: {
      keepAlive?: boolean;
      breadcrumb?: string | null;
      layout?: string | null;
    };
  }): RouteEntryPo {
    return new RouteEntryPo(props);
  }
}

/**
 * 带有冲突的路由条目
 */
export interface RouteEntryWithConflict extends RouteEntryPo {
  //注册业务域
  biz: string;

  //注册路径
  path: string;

  //注册组件路径
  componentPath: string;

  //冲突原因
  reason: string;
}
