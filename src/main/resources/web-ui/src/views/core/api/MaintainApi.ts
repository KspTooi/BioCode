import Http from "@/commons/Http.ts";
import type Result from "@/commons/model/Result.ts";

export interface MaintainUpdateVo {
  existCount: number; // 已存在的数量
  addedCount: number; // 新增的数量
  removedCount: number; // 删除的数量
  addedList: string[]; // 新增列表
  removedList: string[]; // 删除列表
  message?: string; // 提示信息
}

export interface MaintainOperation {
  title: string; // 操作标题
  description: string; // 操作描述
  icon: any; // 操作图标
  buttonText: string; // 操作按钮文本
  bgColor: string; // 操作按钮背景颜色
  iconColor: string; // 操作按钮图标颜色
  key: string; // 操作键
  warning?: string; // 操作警告
  action: () => Promise<MaintainUpdateVo | string>; // 操作方法
  onComplete?: () => void; // 操作完成回调
}

export interface ExecuteInstallWizardVo {
  oldVersion: string; // 旧版本
  newVersion: string; // 新版本
  changesContent: string[]; // 变更项内容
}

export default {
  /**
   * 校验系统内置权限节点
   */
  validatePermissions: async (): Promise<MaintainUpdateVo> => {
    const result = await Http.postEntity<Result<MaintainUpdateVo>>("/maintain/validatePermissions", {});
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 初始化内置数据（默认租户、超级管理员账号及权限组）
   */
  userSystemColdStartup: async (): Promise<MaintainUpdateVo> => {
    const result = await Http.postEntity<Result<MaintainUpdateVo>>("/maintain/userSystemColdStartup", {});
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 维护中心:重置菜单
   */
  resetMenus: async (): Promise<string> => {
    const result = await Http.postEntity<Result<MaintainUpdateVo>>("/maintain/resetMenus", {});
    if (result.code === 0) {
      return result.message;
    }
    throw new Error(result.message);
  },

  /**
   * 维护中心:升级数据库
   */
  upgradeDatabase: async (): Promise<MaintainUpdateVo> => {
    const result = await Http.postEntity<Result<MaintainUpdateVo>>("/maintain/upgradeDatabase", {});
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 维护中心:修复注册表
   */
  repairRegistry: async (): Promise<MaintainUpdateVo> => {
    const result = await Http.postEntity<Result<MaintainUpdateVo>>("/maintain/repairRegistry", {});
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },

  /**
   * 维护中心:检查当前是否处于安装向导模式
   */
  checkInstallWizardMode: async (): Promise<boolean> => {
    const result = await Http.postEntity<Result<number>>("/maintain/checkInstallWizardMode", {});
    if (result.code === 0) {
      return result.data === 1;
    }
    throw new Error(result.message);
  },

  /**
   * 维护中心:执行安装向导
   */
  executeInstallWizard: async (): Promise<ExecuteInstallWizardVo> => {
    const result = await Http.postEntity<Result<ExecuteInstallWizardVo>>("/maintain/executeInstallWizard", {});
    if (result.code === 0) {
      return result.data;
    }
    throw new Error(result.message);
  },
};
