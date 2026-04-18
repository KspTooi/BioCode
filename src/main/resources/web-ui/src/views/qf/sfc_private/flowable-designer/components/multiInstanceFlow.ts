import { computed, onBeforeUnmount, reactive, ref, watch, type Ref, type WritableComputedRef } from "vue";
import type { GetUserListVo } from "@/views/core/api/UserApi";
import OrgApi, { type GetOrgTreeVo } from "@/views/core/api/OrgApi";
import GroupApi, { type GetGroupListVo } from "@/views/auth/api/GroupApi";

export type BpmnEl = { businessObject?: Record<string, unknown> };

//与开始事件「发起人变量」一致，默认 initiator
export const INITIATOR_EXPR = "${initiator}";

//用户集合变量
export const MI_USER_COLLECTION = "${assigneeList}";

//用户组集合变量
export const MI_GROUP_COLLECTION = "${groupList}";

//用户元素
export const MI_USER_ELEM = "assignee";

//用户组元素
export const MI_GROUP_ELEM = "group";

//任务办理人表达式
export const TASK_ASSIGNEE_EXPR = "${assignee}";

//办理人类型
export type AssigneeKind = "user" | "dept" | "group" | "initiator";

//审批方式
export type ApprovalMultiMode = "none" | "countersign" | "orSign" | "custom";

//循环类型
export type CustomLoopMode = "none" | "parallel" | "sequential";

export type PickUser = { id: string; nickname?: string; username?: string };
export type PickDept = { id: string; name: string };
export type PickGroup = { id: string; name: string };

export type MultiInstancePanelProps = {
  modeler: unknown;
  element: unknown;
};

export type MultiInstancePanelApi = {
  assigneeKind: Ref<AssigneeKind>;
  approvalMultiMode: Ref<ApprovalMultiMode>;
  selectedUsers: Ref<PickUser[]>;
  selectedDepts: Ref<PickDept[]>;
  /** 内部状态；界面绑定请用 selectedGroupIds */
  selectedGroups: Ref<PickGroup[]>;
  selectedGroupIds: WritableComputedRef<string[]>;
  groupOptions: Ref<GetGroupListVo[]>;
  orgTreeOptions: Ref<GetOrgTreeVo[]>;
  selectedDeptId: WritableComputedRef<string | undefined>;
  customLoop: {
    mode: CustomLoopMode;
    collection: string;
    elementVariable: string;
    completionCondition: string;
    loopCardinality: string;
  };
  userModalVisible: Ref<boolean>;
  defaultUserIds: Ref<string[]>;
  onAssigneeKindChange: () => void;
  openUserModal: () => void;
  onUsersConfirmed: (data: GetUserListVo | GetUserListVo[]) => void;
  onSelectedGroupIdsChange: () => void;
  onSelectedDeptIdChange: () => void;
  removeUser: (id: string) => void;
  commit: () => void;
  displayUser: (u: PickUser) => string;
};

function getM(props: MultiInstancePanelProps): any {
  return props.modeler;
}

export function displayUser(u: PickUser): string {
  if (u.nickname) {
    return u.nickname;
  }
  if (u.username) {
    return u.username;
  }
  return u.id;
}

function readFormalBody(expr: unknown): string {
  if (!expr || typeof expr !== "object") {
    return "";
  }
  const o = expr as Record<string, unknown>;
  const body = o.body;
  if (typeof body === "string") {
    return body;
  }
  return "";
}

function normExpr(s: string): string {
  return s.replace(/\s+/g, "");
}

function inferApprovalMode(loop: Record<string, unknown> | undefined): ApprovalMultiMode {
  if (!loop || loop.$type !== "bpmn:MultiInstanceLoopCharacteristics") {
    return "none";
  }
  const coll = ((loop.collection as string) || "").trim();
  const comp = normExpr(readFormalBody(loop.completionCondition));
  const seq = loop.isSequential === true;
  const compAll = normExpr("nrOfCompletedInstances == nrOfInstances");
  const compOne = normExpr("nrOfCompletedInstances > 0");
  if (!seq && (coll === MI_USER_COLLECTION || coll === MI_GROUP_COLLECTION)) {
    if (comp === compOne) {
      return "orSign";
    }
    if (comp === compAll || comp === "") {
      return "countersign";
    }
  }
  return "custom";
}

function splitCsv(s: string): string[] {
  return s
    .split(",")
    .map((x) => x.trim())
    .filter(Boolean);
}

function mergeUserRows(ids: string[], nameCsv: string): PickUser[] {
  const names = splitCsv(nameCsv);
  return ids.map((id, i) => {
    const label = names[i] || "";
    if (!label) {
      return { id, username: id };
    }
    return { id, nickname: label, username: label };
  });
}

function mergeDeptRows(ids: string[], nameCsv: string): PickDept[] {
  const names = splitCsv(nameCsv);
  return ids.map((id, i) => ({ id, name: names[i] || id }));
}

function mergeGroupRows(ids: string[], nameCsv: string): PickGroup[] {
  const names = splitCsv(nameCsv);
  return ids.map((id, i) => ({ id, name: names[i] || id }));
}

function findOrgNodeById(nodes: GetOrgTreeVo[], id: string): GetOrgTreeVo | null {
  for (const n of nodes) {
    if (n.id === id) {
      return n;
    }
    const ch = n.children;
    if (ch?.length) {
      const found = findOrgNodeById(ch, id);
      if (found) {
        return found;
      }
    }
  }
  return null;
}

/** 组织机构改为单选，历史多条仅保留第一条 */
function singleDeptRow(rows: PickDept[]): PickDept[] {
  if (rows.length <= 1) {
    return rows;
  }
  return [rows[0]];
}

function clearLoop(diagramEl: BpmnEl, bo: Record<string, unknown>, modeling: any): void {
  modeling.updateModdleProperties(diagramEl, bo, { loopCharacteristics: undefined });
}

/** 清空设计器扩展属性（审批人类型与名称回显） */
const CLEAR_ASSIGNEE_META = {
  assigneeKind: undefined,
  candidateUserNames: undefined,
  candidateDeptNames: undefined,
  candidateGroupNames: undefined,
} as const;

export function useMultiInstancePanel(props: MultiInstancePanelProps): MultiInstancePanelApi {
  const assigneeKind = ref<AssigneeKind>("user");
  const approvalMultiMode = ref<ApprovalMultiMode>("none");
  const selectedUsers = ref<PickUser[]>([]);
  const selectedDepts = ref<PickDept[]>([]);
  const selectedGroups = ref<PickGroup[]>([]);
  const groupOptions = ref<GetGroupListVo[]>([]);
  const orgTreeOptions = ref<GetOrgTreeVo[]>([]);

  const selectedGroupIds = computed<string[]>({
    get: () => selectedGroups.value.map((g) => g.id),
    set: (ids: string[]) => {
      const optMap = new Map(groupOptions.value.map((g) => [g.id, g] as const));
      selectedGroups.value = ids.map((id) => {
        const opt = optMap.get(id);
        if (opt) {
          return { id: opt.id, name: opt.name };
        }
        const prev = selectedGroups.value.find((x) => x.id === id);
        if (prev) {
          return prev;
        }
        return { id, name: id };
      });
    },
  });

  const selectedDeptId = computed<string | undefined>({
    get: () => selectedDepts.value[0]?.id,
    set: (id: string | undefined | null) => {
      if (id == null || id === "") {
        selectedDepts.value = [];
        return;
      }
      const node = findOrgNodeById(orgTreeOptions.value, id);
      const prev = selectedDepts.value.find((d) => d.id === id);
      const name = node?.name ?? prev?.name ?? id;
      selectedDepts.value = [{ id, name }];
    },
  });

  const customLoop = reactive({
    mode: "none" as CustomLoopMode,
    collection: "",
    elementVariable: "",
    completionCondition: "",
    loopCardinality: "",
  });

  const userModalVisible = ref(false);
  const defaultUserIds = computed(() => selectedUsers.value.map((u) => u.id));

  let stackOff: (() => void) | null = null;

  async function fetchOrgTree(): Promise<void> {
    if (orgTreeOptions.value.length > 0) {
      return;
    }
    try {
      orgTreeOptions.value = await OrgApi.getOrgTree({});
    } catch {
      orgTreeOptions.value = [];
    }
  }

  async function fetchGroups(): Promise<void> {
    if (groupOptions.value.length > 0) {
      return;
    }
    try {
      const res = await GroupApi.getGroupList({ pageNum: 1, pageSize: 10000 });
      groupOptions.value = res.data ?? [];
    } catch {
      groupOptions.value = [];
    }
  }

  function resetCustomLoopEmpty(): void {
    customLoop.mode = "none";
    customLoop.collection = "";
    customLoop.elementVariable = "";
    customLoop.completionCondition = "";
    customLoop.loopCardinality = "";
  }

  function finishLoadFromLoop(loop: Record<string, unknown> | undefined): void {
    approvalMultiMode.value = inferApprovalMode(loop);
    if (approvalMultiMode.value === "custom" && loop && loop.$type === "bpmn:MultiInstanceLoopCharacteristics") {
      customLoop.mode = loop.isSequential === true ? "sequential" : "parallel";
      customLoop.collection = (loop.collection as string) || "";
      customLoop.elementVariable = (loop.elementVariable as string) || "";
      customLoop.completionCondition = readFormalBody(loop.completionCondition);
      customLoop.loopCardinality = readFormalBody(loop.loopCardinality);
    }
    if (approvalMultiMode.value !== "custom") {
      resetCustomLoopEmpty();
    }
  }

  function loadFromBo(): void {
    const el = props.element as BpmnEl | null;
    if (!el?.businessObject) {
      assigneeKind.value = "user";
      approvalMultiMode.value = "none";
      selectedUsers.value = [];
      selectedDepts.value = [];
      selectedGroups.value = [];
      resetCustomLoopEmpty();
      return;
    }
    const b = el.businessObject;
    const storedKind = ((b.assigneeKind as string) || "").trim();
    const assignee = ((b.assignee as string) || "").trim();
    const candU = ((b.candidateUsers as string) || "").trim();
    const candG = ((b.candidateGroups as string) || "").trim();
    const userNamesCsv = ((b.candidateUserNames as string) || "").trim();
    const deptNamesCsv = ((b.candidateDeptNames as string) || "").trim();
    const groupNamesCsv = ((b.candidateGroupNames as string) || "").trim();
    const loop = b.loopCharacteristics as Record<string, unknown> | undefined;

    const clearAllPicks = (): void => {
      selectedUsers.value = [];
      selectedDepts.value = [];
      selectedGroups.value = [];
    };

    if (assignee === INITIATOR_EXPR) {
      assigneeKind.value = "initiator";
      clearAllPicks();
      finishLoadFromLoop(loop);
      return;
    }

    if (storedKind === "group" && candG && !candG.includes("${")) {
      assigneeKind.value = "group";
      selectedGroups.value = mergeGroupRows(splitCsv(candG), groupNamesCsv);
      selectedUsers.value = [];
      selectedDepts.value = [];
      finishLoadFromLoop(loop);
      void fetchGroups();
      return;
    }

    if (storedKind === "dept" && candG && !candG.includes("${")) {
      assigneeKind.value = "dept";
      selectedDepts.value = singleDeptRow(mergeDeptRows(splitCsv(candG), deptNamesCsv));
      selectedUsers.value = [];
      selectedGroups.value = [];
      finishLoadFromLoop(loop);
      void fetchOrgTree();
      return;
    }

    if (storedKind === "user") {
      if (assignee === TASK_ASSIGNEE_EXPR && candU) {
        assigneeKind.value = "user";
        selectedUsers.value = mergeUserRows(splitCsv(candU), userNamesCsv);
        selectedDepts.value = [];
        selectedGroups.value = [];
        finishLoadFromLoop(loop);
        return;
      }
      if (candU) {
        assigneeKind.value = "user";
        selectedUsers.value = mergeUserRows(splitCsv(candU), userNamesCsv);
        selectedDepts.value = [];
        selectedGroups.value = [];
        finishLoadFromLoop(loop);
        return;
      }
      if (assignee && !assignee.includes("${")) {
        assigneeKind.value = "user";
        const names = splitCsv(userNamesCsv);
        const label = names[0] || "";
        selectedUsers.value = label
          ? [{ id: assignee, nickname: label, username: label }]
          : [{ id: assignee, username: assignee }];
        selectedDepts.value = [];
        selectedGroups.value = [];
        finishLoadFromLoop(loop);
        return;
      }
    }

    if (assignee === TASK_ASSIGNEE_EXPR && candU) {
      assigneeKind.value = "user";
      selectedUsers.value = mergeUserRows(splitCsv(candU), userNamesCsv);
      selectedDepts.value = [];
      selectedGroups.value = [];
      finishLoadFromLoop(loop);
      return;
    }
    if (candU) {
      assigneeKind.value = "user";
      selectedUsers.value = mergeUserRows(splitCsv(candU), userNamesCsv);
      selectedDepts.value = [];
      selectedGroups.value = [];
      finishLoadFromLoop(loop);
      return;
    }
    if (candG && !candG.includes("${")) {
      assigneeKind.value = "dept";
      selectedDepts.value = singleDeptRow(mergeDeptRows(splitCsv(candG), deptNamesCsv || groupNamesCsv));
      selectedUsers.value = [];
      selectedGroups.value = [];
      finishLoadFromLoop(loop);
      void fetchOrgTree();
      return;
    }
    if (assignee && !assignee.includes("${")) {
      assigneeKind.value = "user";
      const names = splitCsv(userNamesCsv);
      const label = names[0] || "";
      selectedUsers.value = label
        ? [{ id: assignee, nickname: label, username: label }]
        : [{ id: assignee, username: assignee }];
      selectedDepts.value = [];
      selectedGroups.value = [];
      finishLoadFromLoop(loop);
      return;
    }
    assigneeKind.value = "user";
    clearAllPicks();
    finishLoadFromLoop(loop);
  }

  function bindStack(): void {
    if (stackOff) {
      stackOff();
      stackOff = null;
    }
    const m = getM(props);
    if (!m) {
      return;
    }
    const eventBus = m.get("eventBus");
    const handler = (): void => {
      loadFromBo();
    };
    eventBus.on("commandStack.changed", handler);
    stackOff = () => {
      eventBus.off("commandStack.changed", handler);
    };
  }

  watch(
    () => [props.modeler, props.element],
    () => {
      bindStack();
      loadFromBo();
    },
    { immediate: true }
  );

  watch(
    () => props.modeler,
    (m) => {
      if (!m) {
        return;
      }
      void fetchGroups();
      void fetchOrgTree();
    },
    { immediate: true }
  );

  onBeforeUnmount(() => {
    if (!stackOff) {
      return;
    }
    stackOff();
    stackOff = null;
  });

  function onAssigneeKindChange(): void {
    selectedUsers.value = [];
    selectedDepts.value = [];
    selectedGroups.value = [];
    if (assigneeKind.value === "initiator") {
      approvalMultiMode.value = "none";
    }
    if (assigneeKind.value === "group") {
      void fetchGroups();
    }
    if (assigneeKind.value === "dept") {
      void fetchOrgTree();
    }
    commit();
  }

  function openUserModal(): void {
    userModalVisible.value = true;
  }

  function onUsersConfirmed(data: GetUserListVo | GetUserListVo[]): void {
    const list = Array.isArray(data) ? data : [data];
    const map = new Map(selectedUsers.value.map((u) => [u.id, u] as const));
    list.forEach((row) => {
      map.set(row.id, { id: row.id, nickname: row.nickname, username: row.username });
    });
    selectedUsers.value = [...map.values()];
    commit();
  }

  function onSelectedGroupIdsChange(): void {
    commit();
  }

  function onSelectedDeptIdChange(): void {
    commit();
  }

  function removeUser(id: string): void {
    selectedUsers.value = selectedUsers.value.filter((u) => u.id !== id);
    commit();
  }

  function writeCustomLoop(diagramEl: BpmnEl, bo: Record<string, unknown>, modeling: any, moddle: any): void {
    if (customLoop.mode === "none") {
      clearLoop(diagramEl, bo, modeling);
      return;
    }
    let loop = bo.loopCharacteristics as Record<string, unknown> | undefined;
    if (!loop || loop.$type !== "bpmn:MultiInstanceLoopCharacteristics") {
      loop = moddle.create("bpmn:MultiInstanceLoopCharacteristics", {
        isSequential: customLoop.mode === "sequential",
      });
      modeling.updateModdleProperties(diagramEl, bo, { loopCharacteristics: loop });
    }
    modeling.updateModdleProperties(diagramEl, loop, {
      isSequential: customLoop.mode === "sequential",
      collection: customLoop.collection?.trim() || undefined,
      elementVariable: customLoop.elementVariable?.trim() || undefined,
    });
    const compText = customLoop.completionCondition?.trim();
    if (!compText) {
      modeling.updateModdleProperties(diagramEl, loop, { completionCondition: undefined });
    }
    if (compText) {
      const comp = moddle.create("bpmn:FormalExpression", { body: compText });
      modeling.updateModdleProperties(diagramEl, loop, { completionCondition: comp });
    }
    const cardText = customLoop.loopCardinality?.trim();
    if (!cardText) {
      modeling.updateModdleProperties(diagramEl, loop, { loopCardinality: undefined });
      return;
    }
    const cardExpr = moddle.create("bpmn:FormalExpression", { body: cardText });
    modeling.updateModdleProperties(diagramEl, loop, { loopCardinality: cardExpr });
  }

  function writeSignLoop(
    diagramEl: BpmnEl,
    bo: Record<string, unknown>,
    modeling: any,
    moddle: any,
    kind: "user" | "dept" | "group",
    mode: "countersign" | "orSign"
  ): void {
    const useGroupCollection = kind === "dept" || kind === "group";
    const coll = useGroupCollection ? MI_GROUP_COLLECTION : MI_USER_COLLECTION;
    const elem = useGroupCollection ? MI_GROUP_ELEM : MI_USER_ELEM;
    const compBody = mode === "orSign" ? "nrOfCompletedInstances > 0" : "nrOfCompletedInstances == nrOfInstances";
    let loop = bo.loopCharacteristics as Record<string, unknown> | undefined;
    if (!loop || loop.$type !== "bpmn:MultiInstanceLoopCharacteristics") {
      loop = moddle.create("bpmn:MultiInstanceLoopCharacteristics", { isSequential: false });
      modeling.updateModdleProperties(diagramEl, bo, { loopCharacteristics: loop });
    }
    modeling.updateModdleProperties(diagramEl, loop, {
      isSequential: false,
      collection: coll,
      elementVariable: elem,
    });
    const compExpr = moddle.create("bpmn:FormalExpression", { body: compBody });
    modeling.updateModdleProperties(diagramEl, loop, { completionCondition: compExpr });
  }

  function commit(): void {
    const m = getM(props);
    const diagramEl = props.element as BpmnEl | null;
    if (!m || !diagramEl?.businessObject) {
      return;
    }
    const bo = diagramEl.businessObject;
    const modeling = m.get("modeling");
    const moddle = m.get("moddle");

    if (assigneeKind.value === "initiator") {
      modeling.updateProperties(diagramEl, {
        assignee: INITIATOR_EXPR,
        candidateUsers: undefined,
        candidateGroups: undefined,
        assigneeKind: "initiator",
        candidateUserNames: undefined,
        candidateDeptNames: undefined,
        candidateGroupNames: undefined,
      });
      clearLoop(diagramEl, bo, modeling);
      return;
    }

    if (assigneeKind.value === "user") {
      const ids = selectedUsers.value.map((u) => u.id).filter(Boolean);
      const nameCsv = selectedUsers.value.map((u) => displayUser(u)).join(",");
      if (ids.length === 0) {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: undefined,
          ...CLEAR_ASSIGNEE_META,
        });
        if (approvalMultiMode.value === "custom") {
          writeCustomLoop(diagramEl, bo, modeling, moddle);
          return;
        }
        clearLoop(diagramEl, bo, modeling);
        return;
      }
      if (ids.length === 1) {
        modeling.updateProperties(diagramEl, {
          assignee: ids[0],
          candidateUsers: undefined,
          candidateGroups: undefined,
          assigneeKind: "user",
          candidateUserNames: nameCsv,
          candidateDeptNames: undefined,
          candidateGroupNames: undefined,
        });
        if (approvalMultiMode.value === "countersign" || approvalMultiMode.value === "orSign") {
          clearLoop(diagramEl, bo, modeling);
          return;
        }
        if (approvalMultiMode.value === "custom") {
          writeCustomLoop(diagramEl, bo, modeling, moddle);
          return;
        }
        clearLoop(diagramEl, bo, modeling);
        return;
      }
      const csv = ids.join(",");
      if (approvalMultiMode.value === "none") {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: csv,
          candidateGroups: undefined,
          assigneeKind: "user",
          candidateUserNames: nameCsv,
          candidateDeptNames: undefined,
          candidateGroupNames: undefined,
        });
        clearLoop(diagramEl, bo, modeling);
        return;
      }
      if (approvalMultiMode.value === "countersign" || approvalMultiMode.value === "orSign") {
        modeling.updateProperties(diagramEl, {
          assignee: TASK_ASSIGNEE_EXPR,
          candidateUsers: csv,
          candidateGroups: undefined,
          assigneeKind: "user",
          candidateUserNames: nameCsv,
          candidateDeptNames: undefined,
          candidateGroupNames: undefined,
        });
        writeSignLoop(diagramEl, bo, modeling, moddle, "user", approvalMultiMode.value);
        return;
      }
      if (approvalMultiMode.value === "custom") {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: csv,
          candidateGroups: undefined,
          assigneeKind: "user",
          candidateUserNames: nameCsv,
          candidateDeptNames: undefined,
          candidateGroupNames: undefined,
        });
        writeCustomLoop(diagramEl, bo, modeling, moddle);
        return;
      }
    }

    if (assigneeKind.value === "dept") {
      const ids = selectedDepts.value.map((d) => d.id).filter(Boolean);
      const nameCsv = selectedDepts.value.map((d) => d.name).join(",");
      if (ids.length === 0) {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: undefined,
          ...CLEAR_ASSIGNEE_META,
        });
        if (approvalMultiMode.value === "custom") {
          writeCustomLoop(diagramEl, bo, modeling, moddle);
          return;
        }
        clearLoop(diagramEl, bo, modeling);
        return;
      }
      if (ids.length === 1) {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: ids[0],
          assigneeKind: "dept",
          candidateUserNames: undefined,
          candidateDeptNames: nameCsv,
          candidateGroupNames: undefined,
        });
        if (approvalMultiMode.value === "countersign" || approvalMultiMode.value === "orSign") {
          clearLoop(diagramEl, bo, modeling);
          return;
        }
        if (approvalMultiMode.value === "custom") {
          writeCustomLoop(diagramEl, bo, modeling, moddle);
          return;
        }
        clearLoop(diagramEl, bo, modeling);
        return;
      }
      const csv = ids.join(",");
      if (approvalMultiMode.value === "none") {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: csv,
          assigneeKind: "dept",
          candidateUserNames: undefined,
          candidateDeptNames: nameCsv,
          candidateGroupNames: undefined,
        });
        clearLoop(diagramEl, bo, modeling);
        return;
      }
      if (approvalMultiMode.value === "countersign" || approvalMultiMode.value === "orSign") {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: csv,
          assigneeKind: "dept",
          candidateUserNames: undefined,
          candidateDeptNames: nameCsv,
          candidateGroupNames: undefined,
        });
        writeSignLoop(diagramEl, bo, modeling, moddle, "dept", approvalMultiMode.value);
        return;
      }
      if (approvalMultiMode.value === "custom") {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: csv,
          assigneeKind: "dept",
          candidateUserNames: undefined,
          candidateDeptNames: nameCsv,
          candidateGroupNames: undefined,
        });
        writeCustomLoop(diagramEl, bo, modeling, moddle);
        return;
      }
    }

    if (assigneeKind.value === "group") {
      const ids = selectedGroups.value.map((g) => g.id).filter(Boolean);
      const nameCsv = selectedGroups.value.map((g) => g.name).join(",");
      if (ids.length === 0) {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: undefined,
          ...CLEAR_ASSIGNEE_META,
        });
        if (approvalMultiMode.value === "custom") {
          writeCustomLoop(diagramEl, bo, modeling, moddle);
          return;
        }
        clearLoop(diagramEl, bo, modeling);
        return;
      }
      if (ids.length === 1) {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: ids[0],
          assigneeKind: "group",
          candidateUserNames: undefined,
          candidateDeptNames: undefined,
          candidateGroupNames: nameCsv,
        });
        if (approvalMultiMode.value === "countersign" || approvalMultiMode.value === "orSign") {
          clearLoop(diagramEl, bo, modeling);
          return;
        }
        if (approvalMultiMode.value === "custom") {
          writeCustomLoop(diagramEl, bo, modeling, moddle);
          return;
        }
        clearLoop(diagramEl, bo, modeling);
        return;
      }
      const csv = ids.join(",");
      if (approvalMultiMode.value === "none") {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: csv,
          assigneeKind: "group",
          candidateUserNames: undefined,
          candidateDeptNames: undefined,
          candidateGroupNames: nameCsv,
        });
        clearLoop(diagramEl, bo, modeling);
        return;
      }
      if (approvalMultiMode.value === "countersign" || approvalMultiMode.value === "orSign") {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: csv,
          assigneeKind: "group",
          candidateUserNames: undefined,
          candidateDeptNames: undefined,
          candidateGroupNames: nameCsv,
        });
        writeSignLoop(diagramEl, bo, modeling, moddle, "group", approvalMultiMode.value);
        return;
      }
      if (approvalMultiMode.value === "custom") {
        modeling.updateProperties(diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: csv,
          assigneeKind: "group",
          candidateUserNames: undefined,
          candidateDeptNames: undefined,
          candidateGroupNames: nameCsv,
        });
        writeCustomLoop(diagramEl, bo, modeling, moddle);
        return;
      }
    }
  }

  return {
    assigneeKind,
    approvalMultiMode,
    selectedUsers,
    selectedDepts,
    selectedGroups,
    selectedGroupIds,
    groupOptions,
    orgTreeOptions,
    selectedDeptId,
    customLoop,
    userModalVisible,
    defaultUserIds,
    onAssigneeKindChange,
    openUserModal,
    onUsersConfirmed,
    onSelectedGroupIdsChange,
    onSelectedDeptIdChange,
    removeUser,
    commit,
    displayUser,
  };
}
