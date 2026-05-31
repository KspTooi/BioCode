/**
 * QFD「审批与多实例 (QFE)」：读写 qfe 命名空间扩展属性
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-05-26
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
import { computed, nextTick, onMounted, reactive, ref, watch, type Ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import type { GetUserListVo } from "@/views/core/api/UserApi";
import GroupApi, { type GetGroupListVo } from "@/views/auth/api/GroupApi";
import {
  QfdPanelMiFormDefine,
  type QfdPanelMiDetailsVo,
} from "@/views/qf/sfc_private/flowable-designer/api/QfdPanelMultiInstanceApi";

type ModalMode = "add" | "edit";

export type QfeMiActionRow = {
  utAprAction: string;
  utAprActionName: string;
  index: number;
};
export type BpmnEl = { businessObject?: Record<string, unknown> };

type ModelingLike = {
  updateProperties: (el: unknown, props: Record<string, unknown>) => void;
  updateModdleProperties: (el: unknown, target: unknown, props: Record<string, unknown>) => void;
};
type ModdleLike = {
  create: (type: string, props: Record<string, unknown>) => Record<string, unknown>;
};

//必须与后端 QfMiRenameParseHandler.VAR_PREFIX 保持一致
const MI_VAR_PREFIX = "qfMi_";
const MI_ELEM_VAR = "assignee";
const TASK_ASSIGNEE_EXPR = "${assignee}";
const INITIATOR_EXPR = "${initiator}";
const COMP_COUNTERSIGN = "${nrOfCompletedInstances == nrOfInstances}";
const COMP_OR_SIGN = "${nrOfCompletedInstances >= 1}";

const CLEAR_ASSIGNEE_META = {
  assigneeKind: undefined,
  candidateUserNames: undefined,
  candidateDeptNames: undefined,
  candidateGroupNames: undefined,
} as const;

export type PickDept = { id: string; name: string };
export type PickGroup = { id: string; name: string };

/**
 * 取用户展示名（昵称优先，其次用户名，最后 ID）
 */
function userDisplayName(row: GetUserListVo): string {
  if (row.nickname) {
    return row.nickname;
  }
  if (row.username) {
    return row.username;
  }
  return row.id;
}

/** 从 BPMN 灌入 panelForm 期间，抑制回写与联动 watch 清空字段 */
let bpmnSyncHydrating = false;

const DEFAULT_UT_APR_ACTIONS = ["0", "1"];
const DEFAULT_UT_APR_ACTION_NAMES = ["同意", "驳回"];

function splitCsv(raw: unknown): string[] {
  if (typeof raw !== "string") {
    return [];
  }
  return raw
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean);
}

function joinCsv(arr: string[]): string | undefined {
  const s = arr
    .map((x) => x.trim())
    .filter(Boolean)
    .join(",");
  if (!s) {
    return undefined;
  }
  return s;
}

function strAttr(raw: unknown, fallback: string): string {
  if (typeof raw !== "string") {
    return fallback;
  }
  const s = raw.trim();
  if (!s) {
    return fallback;
  }
  return s;
}

/**
 * 确保顶层 Process 下所有 StartEvent 都具有 flowable:initiator="initiator"
 */
function ensureStartEventInitiator(modeler: { get: (n: string) => unknown }, modeling: ModelingLike): void {
  const registry = modeler.get("elementRegistry") as {
    getAll: () => Array<{ type: string; businessObject: Record<string, unknown> }>;
  } | null;
  if (!registry?.getAll) {
    return;
  }
  for (const el of registry.getAll()) {
    if (el.type !== "bpmn:StartEvent") {
      continue;
    }
    const bo = el.businessObject;
    if (!bo) {
      continue;
    }
    const parent = bo.$parent as Record<string, unknown> | undefined;
    if (parent?.$type !== "bpmn:Process") {
      continue;
    }
    if (bo.initiator === "initiator") {
      continue;
    }
    modeling.updateProperties(el, { initiator: "initiator" });
  }
}

/**
 * 清除 UserTask 上由发起人投影写入的原生 Flowable 办理人字段
 */
function clearInitiatorAssignee(diagramEl: BpmnEl, bo: Record<string, unknown>, modeling: ModelingLike): void {
  const assignee = String(bo.assignee ?? "");
  const assigneeKind = String(bo.assigneeKind ?? "");
  if (assignee !== INITIATOR_EXPR && assigneeKind !== "initiator") {
    return;
  }
  modeling.updateProperties(diagramEl, {
    assignee: undefined,
    candidateUsers: undefined,
    candidateGroups: undefined,
    ...CLEAR_ASSIGNEE_META,
  });
}

/**
 * 发起人办理人投影到原生 Flowable：StartEvent.initiator + UserTask.assignee
 */
function projectInitiatorAssignee(
  modeler: { get: (n: string) => unknown },
  diagramEl: BpmnEl,
  bo: Record<string, unknown>,
  modeling: ModelingLike
): void {
  ensureStartEventInitiator(modeler, modeling);
  modeling.updateModdleProperties(diagramEl, bo, { loopCharacteristics: undefined });
  modeling.updateProperties(diagramEl, {
    assignee: INITIATOR_EXPR,
    candidateUsers: undefined,
    candidateGroups: undefined,
    ...CLEAR_ASSIGNEE_META,
    assigneeKind: "initiator",
  });
}

/**
 * 从 BPMN 读取 qfe 扩展属性到 panelForm
 */
async function loadFromBpmn(_modeler: unknown, element: unknown): Promise<void> {
  const diagramEl = element as BpmnEl | null;
  if (!diagramEl?.businessObject) {
    return;
  }
  bpmnSyncHydrating = true;
  const b = diagramEl.businessObject;

  panelForm.utAprKind = strAttr(b.utAprKind, "0");
  panelForm.utAprMemberKind = strAttr(b.utAprMemberKind, "0");
  //qfe:utAprMemberKind 缺失时，从原生 assignee/assigneeKind 反推（兼容旧流程/外部导入）
  if (typeof b.utAprMemberKind !== "string" || !b.utAprMemberKind.trim()) {
    const assignee = strAttr(b.assignee, "");
    const assigneeKind = strAttr(b.assigneeKind, "");
    if (assignee === INITIATOR_EXPR || assigneeKind === "initiator") {
      panelForm.utAprMemberKind = "3";
    }
  }
  panelForm.utAprMi = strAttr(b.utAprMi, "0");
  panelForm.utAprMiExpress = strAttr(b.utAprMiExpress, "");

  const actions = splitCsv(b.utAprActions);
  panelForm.utAprActions = actions.length > 0 ? actions : [...DEFAULT_UT_APR_ACTIONS];

  const actionNames = splitCsv(b.utAprActionNames);
  panelForm.utAprActionNames = actionNames.length > 0 ? actionNames : [...DEFAULT_UT_APR_ACTION_NAMES];

  panelForm.utAprComment = strAttr(b.utAprComment, "1");
  panelForm.utAprMemberIds = splitCsv(b.utAprMemberIds);
  panelForm.utAprMemberNames = strAttr(b.utAprMemberNames, "");

  //qfe:utAprMi 缺失时 从原生 loopCharacteristics 反推一次（兼容老流程/外部导入）
  if (typeof b.utAprMi !== "string" || !b.utAprMi) {
    const loop = b.loopCharacteristics as Record<string, unknown> | undefined;
    if (loop && loop.$type === "bpmn:MultiInstanceLoopCharacteristics") {
      const comp = loop.completionCondition as { body?: string } | undefined;
      const body = (comp?.body ?? "").replace(/\s+/g, "");
      if (body === "${nrOfCompletedInstances>=1}" || body === "${nrOfCompletedInstances>0}") {
        panelForm.utAprMi = "2";
        return;
      }
      if (body === "" || body === "${nrOfCompletedInstances==nrOfInstances}") {
        panelForm.utAprMi = "1";
        return;
      }
      if (panelForm.utAprMi === "0") {
        panelForm.utAprMi = "3";
        panelForm.utAprMiExpress = comp?.body ?? "";
      }
    }
  }

  await nextTick();
  bpmnSyncHydrating = false;
}

/**
 * 将 panelForm 写入 BPMN qfe 扩展属性
 */
function uploadToBpmn(modeler: unknown, element: unknown): Promise<void> {
  if (bpmnSyncHydrating) {
    return Promise.resolve();
  }
  const diagramEl = element as BpmnEl | null;
  if (!diagramEl?.businessObject) {
    return Promise.resolve();
  }
  const m = modeler as { get: (n: string) => unknown } | null;
  if (!m?.get) {
    return Promise.resolve();
  }
  const modeling = m.get("modeling") as ModelingLike;
  const moddle = m.get("moddle") as ModdleLike;
  modeling.updateProperties(diagramEl, {
    utAprKind: panelForm.utAprKind,
    utAprMemberKind: panelForm.utAprMemberKind,
    utAprMemberIds: joinCsv(panelForm.utAprMemberIds),
    utAprMemberNames: panelForm.utAprMemberNames || undefined,
    utAprMi: panelForm.utAprMi,
    utAprMiExpress: panelForm.utAprMiExpress || undefined,
    utAprActions: joinCsv(panelForm.utAprActions),
    utAprActionNames: joinCsv(panelForm.utAprActionNames),
    utAprComment: panelForm.utAprComment,
  });

  //投影到原生 Flowable 办理人 / 多实例
  const bo = diagramEl.businessObject;
  const taskId = String(bo.id ?? "");
  const mi = panelForm.utAprMi;
  const memberKind = panelForm.utAprMemberKind;

  if (memberKind === "3") {
    projectInitiatorAssignee(m, diagramEl, bo, modeling);
    return Promise.resolve();
  }

  if (mi === "0") {
    modeling.updateModdleProperties(diagramEl, bo, { loopCharacteristics: undefined });
    clearInitiatorAssignee(diagramEl, bo, modeling);
    if (bo.assignee === TASK_ASSIGNEE_EXPR) {
      modeling.updateProperties(diagramEl, { assignee: undefined });
    }
    return Promise.resolve();
  }

  clearInitiatorAssignee(diagramEl, bo, modeling);

  let loop = bo.loopCharacteristics as Record<string, unknown> | undefined;
  if (!loop || loop.$type !== "bpmn:MultiInstanceLoopCharacteristics") {
    loop = moddle.create("bpmn:MultiInstanceLoopCharacteristics", { isSequential: false });
    modeling.updateModdleProperties(diagramEl, bo, { loopCharacteristics: loop });
  }
  modeling.updateModdleProperties(diagramEl, loop, {
    isSequential: false,
    collection: "${" + MI_VAR_PREFIX + taskId + "}",
    elementVariable: MI_ELEM_VAR,
  });

  let compBody = "";
  if (mi === "1") {
    compBody = COMP_COUNTERSIGN;
  }
  if (mi === "2") {
    compBody = COMP_OR_SIGN;
  }
  if (mi === "3") {
    compBody = panelForm.utAprMiExpress?.trim() ?? "";
  }
  if (!compBody) {
    modeling.updateModdleProperties(diagramEl, loop, { completionCondition: undefined });
  }
  if (compBody) {
    const expr = moddle.create("bpmn:FormalExpression", { body: compBody });
    modeling.updateModdleProperties(diagramEl, loop, { completionCondition: expr });
  }
  modeling.updateProperties(diagramEl, { assignee: TASK_ASSIGNEE_EXPR });

  return Promise.resolve();
}

//多实例表单数据
const panelForm = reactive<QfdPanelMiDetailsVo>({
  utAprKind: "0",
  utAprMemberKind: "0",
  utAprMi: "0",
  utAprMemberIds: [],
  utAprMemberNames: "",
  utAprMiExpress: "",
  utAprActions: ["0", "1"],
  utAprActionNames: ["同意", "驳回"],
  utAprComment: "1",
});
export default {
  /**
   * 处理操作：列表展示 + 新增/编辑模态框（提交时写入 panelForm）
   */
  useQfePanelMiAction(modalFormRef: Ref<FormInstance | undefined>) {
    const modalVisible = ref(false);
    const modalMode = ref<ModalMode>("add");
    const editingIndex = ref(-1);
    const modalForm = reactive({ utAprAction: "", utAprActionName: "" });

    const modalRules = reactive<FormRules>({
      utAprAction: [{ required: true, message: "请选择操作类型", trigger: "change" }],
      utAprActionName: [
        { required: true, message: "请输入操作名称", trigger: "blur" },
        { max: 16, message: "操作名称长度不能超过16个字符", trigger: "blur" },
      ],
    });

    /** 由 panelForm 中 utAprActions / utAprActionNames 派生的表格行 */
    const actionList = computed<QfeMiActionRow[]>(() =>
      panelForm.utAprActions.map((utAprAction, index) => ({
        utAprAction,
        utAprActionName: panelForm.utAprActionNames[index] ?? "",
        index,
      }))
    );

    /**
     * 根据操作类型编码取显示名称
     */
    function actionTypeLabel(v: string): string {
      const opt = QfdPanelMiFormDefine.utAprActions.find((o) => o.v === v);
      if (opt) {
        return opt.l;
      }
      return v;
    }

    /**
     * 重置模态框表单与编辑状态
     */
    function resetModal(): void {
      modalFormRef.value?.resetFields();
      modalMode.value = "add";
      editingIndex.value = -1;
      modalForm.utAprAction = "";
      modalForm.utAprActionName = "";
    }

    /**
     * 打开模态框（新增或编辑指定行）
     */
    function openModal(mode: ModalMode, row?: QfeMiActionRow): void {
      resetModal();
      modalMode.value = mode;
      if (mode === "add") {
        modalVisible.value = true;
        return;
      }
      if (!row) {
        return;
      }
      editingIndex.value = row.index;
      modalForm.utAprAction = row.utAprAction;
      modalForm.utAprActionName = row.utAprActionName;
      modalVisible.value = true;
    }

    /**
     * 关闭模态框
     */
    function closeModal(): void {
      modalVisible.value = false;
    }

    /**
     * 校验并提交模态框，写入 panelForm.utAprActions / utAprActionNames
     */
    async function commitModal(): Promise<void> {
      try {
        await modalFormRef.value?.validate();
      } catch {
        return;
      }
      const action = modalForm.utAprAction;
      const name = modalForm.utAprActionName.trim();
      if (modalMode.value === "add") {
        if (panelForm.utAprActions.includes(action)) {
          ElMessage.warning("该操作类型已存在");
          return;
        }
        panelForm.utAprActions.push(action);
        panelForm.utAprActionNames.push(name);
        modalVisible.value = false;
        return;
      }
      const i = editingIndex.value;
      if (i < 0) {
        return;
      }
      panelForm.utAprActionNames[i] = name;
      modalVisible.value = false;
    }

    /**
     * 删除列表中的一条处理操作
     */
    async function removeAction(row: QfeMiActionRow): Promise<void> {
      try {
        await ElMessageBox.confirm("确定删除该操作吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }
      panelForm.utAprActions.splice(row.index, 1);
      panelForm.utAprActionNames.splice(row.index, 1);
    }

    return {
      actionList,
      actionTypeLabel,
      modalVisible,
      modalMode,
      modalForm,
      modalRules,
      openModal,
      closeModal,
      resetModal,
      commitModal,
      removeAction,
    };
  },

  /**
   * QFE 多实例面板：处理人选择、多实例配置与 panelForm 联动
   */
  useQfdPanelMultiInstanceQfe(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined) {
    //角色数据
    const groupList = ref<GetGroupListVo[]>([]);

    //草稿已选的用户/用户组IDS
    const draftMemberIds = ref<string[]>([]);

    //是否允许多选用户
    const multiMode = computed(() => {
      //当前开启了多实例
      if (panelForm.utAprMi !== "0") {
        return true;
      }

      //当前审批节点类型为发起时指定
      if (panelForm.utAprKind === "1") {
        return true;
      }

      return false;
    });

    /**
     * 加载角色数据
     */
    function loadGroupList(): Promise<void> {
      return GroupApi.getGroupList({ pageNum: 1, pageSize: 10000 }).then((res) => {
        groupList.value = res.data ?? [];
      });
    }

    onMounted(async () => {
      await loadGroupList();
    });

    watch(
      () => [modelerGetter(), elementGetter()],
      () => {
        void loadFromBpmn(modelerGetter(), elementGetter()).then(() => {
          draftMemberIds.value = [...panelForm.utAprMemberIds];
        });
      },
      { immediate: true }
    );

    watch(
      panelForm,
      () => {
        void uploadToBpmn(modelerGetter(), elementGetter());
      },
      { deep: true }
    );

    /**
     * 用户选择：同步 panelForm.utAprMemberIds / utAprMemberNames
     */
    const onUserSelected = (data: GetUserListVo[]): void => {
      const ids = data.map((row) => row.id).filter(Boolean);
      const names = data.map((row) => userDisplayName(row)).join("、");
      panelForm.utAprMemberIds = ids;
      panelForm.utAprMemberNames = names;
    };

    /**
     * 用户组选择：同步 panelForm.utAprMemberIds / utAprMemberNames
     */
    const onGroupSelected = (): void => {
      const ids = draftMemberIds.value.filter(Boolean);
      const names = ids.map((id) => groupList.value.find((g) => g.id === id)?.name ?? id).join(",");
      panelForm.utAprMemberIds = ids;
      panelForm.utAprMemberNames = names;
    };

    //监听审批人类型变化
    const onAprKindChanged = (val: string): void => {
      panelForm.utAprMemberIds = [];
      panelForm.utAprMemberNames = "";

      //标准
      if (val === "0") {
        panelForm.utAprMemberKind = "0";
        panelForm.utAprMi = "0";
      }

      //发起时指定
      if (val === "1") {
        panelForm.utAprMemberKind = "10";
        panelForm.utAprMi = "0";
      }
    };

    //监听审批人配置变化
    const onMemberKindChanged = (val: string): void => {
      if (val === "3") {
        panelForm.utAprMi = "0";
        panelForm.utAprMiExpress = "";
      }

      draftMemberIds.value = [];
      panelForm.utAprMemberIds = [];
      panelForm.utAprMemberNames = "";
    };

    //监听多实例方式变化
    const onMiChanged = (val: string): void => {
      //无
      if (val === "0") {
        panelForm.utAprMiExpress = "";
      }

      //会签
      if (val === "1") {
        panelForm.utAprMiExpress = COMP_COUNTERSIGN;
      }

      //或签
      if (val === "2") {
        panelForm.utAprMiExpress = COMP_OR_SIGN;
      }

      //自定义
      if (val === "3") {
        panelForm.utAprMiExpress = "";
      }

      draftMemberIds.value = [];
      panelForm.utAprMemberIds = [];
      panelForm.utAprMemberNames = "";
    };

    return {
      draftMemberIds,
      panelForm,
      groupList,
      multiMode,
      onUserSelected,
      onGroupSelected,
      onAprKindChanged,
      onMemberKindChanged,
      onMiChanged,
    };
  },
};
