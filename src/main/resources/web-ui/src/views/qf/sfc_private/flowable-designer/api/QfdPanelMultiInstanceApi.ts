/**
 * 多实例详情
 */
export interface QfdPanelMiDetailsVo {
  utAprKind: string; //审批节点类型 0:标准 1:发起时指定
  utAprMemberKind: string; //审批成员类型 0:指定用户 1:用户组 2:组织机构 3:发起人 10:任意人
  utAprMemberIds: string[]; //人员IDS
  utAprMemberNames: string; //人员名称S
  utAprMi: string; //多实例方式 0:无 1:会签 2:或签 3:自定义
  utAprMiExpress: string; //多实例表达式
  utAprActions: string[]; //允许的审批操作
  utAprActionNames: string[]; //允许的审批操作名称
  utAprComment: string; //是否允许填写审批意见 0:不写 1:要写
}

/**
 * 多实例表单定义
 */
export const QfdPanelMiFormDefine = {
  //审批节点类型
  utAprKind: [
    { v: "0", l: "标准" },
    { v: "1", l: "发起时指定" },
  ],
  //审批成员类型(标准)
  utAprMemberKindStandard: [
    { v: "0", l: "指定用户", d: false },
    { v: "1", l: "用户组", d: false },
    { v: "2", l: "组织机构", d: true },
    { v: "3", l: "发起人", d: false },
  ],
  //审批成员类型(发起时指定)
  utAprMemberKindInit: [
    { v: "10", l: "任意人", d: false },
    { v: "0", l: "指定用户", d: false },
    { v: "1", l: "用户组", d: false },
    { v: "2", l: "组织机构", d: true },
  ],
  //多实例方式(标准)
  utAprMiStandard: [
    { v: "0", l: "无", d: false },
    { v: "1", l: "会签", d: false },
    { v: "2", l: "或签", d: false },
    { v: "3", l: "自定义", d: false },
  ],
  //多实例方式(发起时指定)
  utAprMiInit: [
    { v: "0", l: "无", d: false },
    { v: "1", l: "会签", d: true },
    { v: "2", l: "或签", d: true },
    { v: "3", l: "自定义", d: true },
  ],
  //允许的审批操作 0:同意 1:驳回 2:转交 3:驳回节点
  utAprActions: [
    { v: "0", l: "同意" },
    { v: "1", l: "驳回" },
    { v: "2", l: "转交" },
    { v: "3", l: "驳回节点" },
  ],
  //是否允许填写审批意见 0:不写 1:要写
  utAprComment: [
    { v: "0", l: "不填写" },
    { v: "1", l: "需填写" },
  ],
};
