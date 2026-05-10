/**
 * QFD「审批与多实例」折叠块：审批人类型 + 多实例审批方式 + 自定义循环
 *
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-10
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
import { computed, onBeforeUnmount, reactive, ref, watch, type Ref, type WritableComputedRef } from "vue";
import type { GetUserListVo } from "@/views/core/api/UserApi";
import OrgApi, { type GetOrgTreeVo } from "@/views/core/api/OrgApi";
import GroupApi, { type GetGroupListVo } from "@/views/auth/api/GroupApi";

export type BpmnEl = { businessObject?: Record<string, unknown> };

type ModelingLike = {
  updateProperties: (el: unknown, props: Record<string, unknown>) => void;
  updateModdleProperties: (el: unknown, target: unknown, props: Record<string, unknown>) => void;
};
type ModdleLike = {
  create: (type: string, props: Record<string, unknown>) => Record<string, unknown>;
};

export const INITIATOR_EXPR = "${initiator}";
export const MI_USER_COLLECTION = "${assigneeList}";
export const MI_GROUP_COLLECTION = "${groupList}";
export const MI_USER_ELEM = "assignee";
export const MI_GROUP_ELEM = "group";
export const TASK_ASSIGNEE_EXPR = "${assignee}";

export type AssigneeKind = "user" | "dept" | "group" | "initiator";
export type ApprovalMultiMode = "none" | "countersign" | "orSign" | "custom";
export type CustomLoopMode = "none" | "parallel" | "sequential";

export type PickUser = { id: string; nickname?: string; username?: string };
export type PickDept = { id: string; name: string };
export type PickGroup = { id: string; name: string };

export type QfdMultiInstanceApi = {
  assigneeKind: Ref<AssigneeKind>;
  approvalMultiMode: Ref<ApprovalMultiMode>;
  selectedUsers: Ref<PickUser[]>;
  selectedDepts: Ref<PickDept[]>;
  selectedGroups: Ref<PickGroup[]>;
  selectedGroupIds: WritableComputedRef<string[] | string>;
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
  onApprovalMultiModeChange: () => void;
  openUserModal: () => void;
  onUsersConfirmed: (data: GetUserListVo[]) => void;
  onUserModalClose: () => void;
  onSelectedGroupIdsChange: () => void;
  onSelectedDeptIdChange: () => void;
  removeUser: (id: string) => void;
  commit: () => void;
  displayUser: (u: PickUser) => string;
};

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
  return s.replace(/\s+/g, "").replace(/^\$\{/, "").replace(/\}$/, "");
}

function wrapEl(s: string): string {
  if (!s) {
    return s;
  }
  if (s.startsWith("${") && s.endsWith("}")) {
    return s;
  }
  return "${" + s + "}";
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

function singleDeptRow(rows: PickDept[]): PickDept[] {
  if (rows.length <= 1) {
    return rows;
  }
  return [rows[0]];
}

function clearLoop(diagramEl: BpmnEl, bo: Record<string, unknown>, modeling: ModelingLike): void {
  modeling.updateModdleProperties(diagramEl, bo, { loopCharacteristics: undefined });
}

/**
 * 确保顶层 Process 下所有 StartEvent 都具有 flowable:initiator="initiator"。
 * 当审批人类型选为"发起人"时调用，避免 UserTask 使用 ${initiator} 但 StartEvent 未声明导致运行时报错。
 */
function ensureStartEventInitiator(modeler: { get: (n: string) => unknown }, modeling: ModelingLike): void {
  const registry = modeler.get("elementRegistry") as {
    getAll: () => Array<{ type: string; businessObject: Record<string, unknown> }>;
  } | null;
  if (!registry?.getAll) {
    return;
  }
  const list = registry.getAll();
  for (const el of list) {
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

const CLEAR_ASSIGNEE_META = {
  assigneeKind: undefined,
  candidateUserNames: undefined,
  candidateDeptNames: undefined,
  candidateGroupNames: undefined,
} as const;

export default {
  useQfdPanelMultiInstance(modelerGetter: () => unknown, elementGetter: () => unknown | null | undefined): QfdMultiInstanceApi {
    const assigneeKind = ref<AssigneeKind>("user");
    const approvalMultiMode = ref<ApprovalMultiMode>("none");
    const selectedUsers = ref<PickUser[]>([]);
    const selectedDepts = ref<PickDept[]>([]);
    const selectedGroups = ref<PickGroup[]>([]);
    const groupOptions = ref<GetGroupListVo[]>([]);
    const orgTreeOptions = ref<GetOrgTreeVo[]>([]);

    const selectedGroupIds = computed<string[] | string>({
      get: () => {
        const ids = selectedGroups.value.map((g) => g.id);
        if (approvalMultiMode.value === "none") {
          return ids[0] ?? "";
        }
        return ids;
      },
      set: (val: string[] | string) => {
        let ids: string[] = [];
        if (Array.isArray(val)) {
          ids = val;
        }
        if (!Array.isArray(val) && val) {
          ids = [val];
        }
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
    const defaultUserIds = ref<string[]>([]);

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

    function clearAllPicks(): void {
      selectedUsers.value = [];
      selectedDepts.value = [];
      selectedGroups.value = [];
    }

    function applyUserPicks(candU: string, userNamesCsv: string): void {
      assigneeKind.value = "user";
      selectedUsers.value = mergeUserRows(splitCsv(candU), userNamesCsv);
      selectedDepts.value = [];
      selectedGroups.value = [];
    }

    function applyAssigneePick(assignee: string, userNamesCsv: string): void {
      assigneeKind.value = "user";
      const label = splitCsv(userNamesCsv)[0] || "";
      selectedUsers.value = label
        ? [{ id: assignee, nickname: label, username: label }]
        : [{ id: assignee, username: assignee }];
      selectedDepts.value = [];
      selectedGroups.value = [];
    }

    function applyDeptPicks(candG: string, deptNamesCsv: string): void {
      assigneeKind.value = "dept";
      selectedDepts.value = singleDeptRow(mergeDeptRows(splitCsv(candG), deptNamesCsv));
      selectedUsers.value = [];
      selectedGroups.value = [];
    }

    interface ResolveCtx {
      assignee: string;
      candU: string;
      candG: string;
      userNamesCsv: string;
      deptNamesCsv: string;
      groupNamesCsv: string;
      loop: Record<string, unknown> | undefined;
    }

    function resolveUserOrDeptKind(ctx: ResolveCtx): void {
      const { assignee, candU, candG, userNamesCsv, deptNamesCsv, groupNamesCsv, loop } = ctx;
      if (candU) {
        applyUserPicks(candU, userNamesCsv);
        finishLoadFromLoop(loop);
        return;
      }
      if (candG && !candG.includes("${")) {
        applyDeptPicks(candG, deptNamesCsv || groupNamesCsv);
        finishLoadFromLoop(loop);
        void fetchOrgTree();
        return;
      }
      if (assignee && !assignee.includes("${")) {
        applyAssigneePick(assignee, userNamesCsv);
        finishLoadFromLoop(loop);
        return;
      }
      assigneeKind.value = "user";
      clearAllPicks();
      finishLoadFromLoop(loop);
    }

    function loadFromBo(): void {
      const el = elementGetter() as BpmnEl | null;
      if (!el?.businessObject) {
        assigneeKind.value = "user";
        approvalMultiMode.value = "none";
        clearAllPicks();
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
        applyDeptPicks(candG, deptNamesCsv);
        finishLoadFromLoop(loop);
        void fetchOrgTree();
        return;
      }

      resolveUserOrDeptKind({ assignee, candU, candG, userNamesCsv, deptNamesCsv, groupNamesCsv, loop });
    }

    function bindStack(): void {
      if (stackOff) {
        stackOff();
        stackOff = null;
      }
      const m = modelerGetter() as { get: (n: string) => unknown } | null;
      if (!m) {
        return;
      }
      const eventBus = m.get("eventBus") as {
        on: (e: string, h: () => void) => void;
        off: (e: string, h: () => void) => void;
      };
      const handler = (): void => {
        loadFromBo();
      };
      eventBus.on("commandStack.changed", handler);
      stackOff = () => {
        eventBus.off("commandStack.changed", handler);
      };
    }

    watch(
      () => [modelerGetter(), elementGetter()],
      () => {
        bindStack();
        loadFromBo();
      },
      { immediate: true }
    );

    watch(
      () => modelerGetter(),
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

    function onApprovalMultiModeChange(): void {
      if (approvalMultiMode.value === "none") {
        if (selectedUsers.value.length > 1) {
          selectedUsers.value = [selectedUsers.value[0]];
        }
        if (selectedGroups.value.length > 1) {
          selectedGroups.value = [selectedGroups.value[0]];
        }
      }
      commit();
    }

    function openUserModal(): void {
      defaultUserIds.value = selectedUsers.value.map((u) => u.id);
      userModalVisible.value = true;
    }

    function onUserModalClose(): void {
      defaultUserIds.value = [];
    }

    function onUsersConfirmed(data: GetUserListVo[]): void {
      const list = data;
      if (approvalMultiMode.value === "none") {
        const row = list[0];
        if (row) {
          selectedUsers.value = [{ id: row.id, nickname: row.nickname, username: row.username }];
        }
        commit();
        return;
      }
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

    function writeCustomLoop(diagramEl: BpmnEl, bo: Record<string, unknown>, modeling: ModelingLike, moddle: ModdleLike): void {
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
        const comp = moddle.create("bpmn:FormalExpression", { body: wrapEl(compText) });
        modeling.updateModdleProperties(diagramEl, loop, { completionCondition: comp });
      }
      const cardText = customLoop.loopCardinality?.trim();
      if (!cardText) {
        modeling.updateModdleProperties(diagramEl, loop, { loopCardinality: undefined });
        return;
      }
      const cardBody = /^\d+$/.test(cardText) ? cardText : wrapEl(cardText);
      const cardExpr = moddle.create("bpmn:FormalExpression", { body: cardBody });
      modeling.updateModdleProperties(diagramEl, loop, { loopCardinality: cardExpr });
    }

    function writeSignLoop(
      diagramEl: BpmnEl,
      bo: Record<string, unknown>,
      modeling: ModelingLike,
      moddle: ModdleLike,
      kind: "user" | "dept" | "group",
      mode: "countersign" | "orSign"
    ): void {
      const useGroupCollection = kind === "dept" || kind === "group";
      const coll = useGroupCollection ? MI_GROUP_COLLECTION : MI_USER_COLLECTION;
      const elem = useGroupCollection ? MI_GROUP_ELEM : MI_USER_ELEM;
      const compBody = mode === "orSign" ? "${nrOfCompletedInstances > 0}" : "${nrOfCompletedInstances == nrOfInstances}";
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

    type CommitCtx = {
      diagramEl: BpmnEl;
      bo: Record<string, unknown>;
      modeling: ModelingLike;
      moddle: ModdleLike;
    };

    function applyLoop(ctx: CommitCtx, mode: ApprovalMultiMode, kind: "user" | "dept" | "group"): void {
      if (mode === "custom") {
        writeCustomLoop(ctx.diagramEl, ctx.bo, ctx.modeling, ctx.moddle);
        return;
      }
      if (mode === "countersign" || mode === "orSign") {
        writeSignLoop(ctx.diagramEl, ctx.bo, ctx.modeling, ctx.moddle, kind, mode);
        return;
      }
      clearLoop(ctx.diagramEl, ctx.bo, ctx.modeling);
    }

    function commitUser(ctx: CommitCtx): void {
      const ids = selectedUsers.value.map((u) => u.id).filter(Boolean);
      const nameCsv = selectedUsers.value.map((u) => displayUser(u)).join(",");
      if (ids.length === 0) {
        ctx.modeling.updateProperties(ctx.diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: undefined,
          ...CLEAR_ASSIGNEE_META,
        });
        applyLoop(ctx, approvalMultiMode.value, "user");
        return;
      }
      if (ids.length === 1) {
        ctx.modeling.updateProperties(ctx.diagramEl, {
          assignee: ids[0],
          candidateUsers: undefined,
          candidateGroups: undefined,
          assigneeKind: "user",
          candidateUserNames: nameCsv,
          candidateDeptNames: undefined,
          candidateGroupNames: undefined,
        });
        if (approvalMultiMode.value === "custom") {
          writeCustomLoop(ctx.diagramEl, ctx.bo, ctx.modeling, ctx.moddle);
          return;
        }
        clearLoop(ctx.diagramEl, ctx.bo, ctx.modeling);
        return;
      }
      const csv = ids.join(",");
      const isSignMode = approvalMultiMode.value === "countersign" || approvalMultiMode.value === "orSign";
      ctx.modeling.updateProperties(ctx.diagramEl, {
        assignee: isSignMode ? TASK_ASSIGNEE_EXPR : undefined,
        candidateUsers: csv,
        candidateGroups: undefined,
        assigneeKind: "user",
        candidateUserNames: nameCsv,
        candidateDeptNames: undefined,
        candidateGroupNames: undefined,
      });
      applyLoop(ctx, approvalMultiMode.value, "user");
    }

    function commitGroupKind(ctx: CommitCtx, kind: "dept" | "group", ids: string[], nameCsv: string): void {
      if (ids.length === 0) {
        ctx.modeling.updateProperties(ctx.diagramEl, {
          assignee: undefined,
          candidateUsers: undefined,
          candidateGroups: undefined,
          ...CLEAR_ASSIGNEE_META,
        });
        applyLoop(ctx, approvalMultiMode.value, kind);
        return;
      }
      const csv = ids.join(",");
      const nameKey = kind === "dept" ? "candidateDeptNames" : "candidateGroupNames";
      const otherKey = kind === "dept" ? "candidateGroupNames" : "candidateDeptNames";
      ctx.modeling.updateProperties(ctx.diagramEl, {
        assignee: undefined,
        candidateUsers: undefined,
        candidateGroups: ids.length === 1 ? ids[0] : csv,
        assigneeKind: kind,
        candidateUserNames: undefined,
        [nameKey]: nameCsv,
        [otherKey]: undefined,
      });
      if (ids.length === 1) {
        if (approvalMultiMode.value === "custom") {
          writeCustomLoop(ctx.diagramEl, ctx.bo, ctx.modeling, ctx.moddle);
          return;
        }
        clearLoop(ctx.diagramEl, ctx.bo, ctx.modeling);
        return;
      }
      applyLoop(ctx, approvalMultiMode.value, kind);
    }

    function commit(): void {
      const m = modelerGetter() as { get: (n: string) => unknown } | null;
      const diagramEl = elementGetter() as BpmnEl | null;
      if (!m || !diagramEl?.businessObject) {
        return;
      }
      const bo = diagramEl.businessObject;
      const modeling = m.get("modeling") as ModelingLike;
      const moddle = m.get("moddle") as ModdleLike;
      const ctx: CommitCtx = { diagramEl, bo, modeling, moddle };

      if (assigneeKind.value === "initiator") {
        ensureStartEventInitiator(m, modeling);
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
        commitUser(ctx);
        return;
      }
      if (assigneeKind.value === "dept") {
        const ids = selectedDepts.value.map((d) => d.id).filter(Boolean);
        const nameCsv = selectedDepts.value.map((d) => d.name).join(",");
        commitGroupKind(ctx, "dept", ids, nameCsv);
        return;
      }
      if (assigneeKind.value === "group") {
        const ids = selectedGroups.value.map((g) => g.id).filter(Boolean);
        const nameCsv = selectedGroups.value.map((g) => g.name).join(",");
        commitGroupKind(ctx, "group", ids, nameCsv);
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
      onApprovalMultiModeChange,
      openUserModal,
      onUsersConfirmed,
      onUserModalClose,
      onSelectedGroupIdsChange,
      onSelectedDeptIdChange,
      removeUser,
      commit,
      displayUser,
    };
  },
};
