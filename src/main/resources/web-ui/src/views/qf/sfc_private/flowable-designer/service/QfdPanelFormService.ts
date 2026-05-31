/**
 * QFD「表单配置」：加载业务表单字段，读写 qfe:utFormAllowEditFields
 */
import { nextTick, reactive, ref, watch } from "vue";
import QfdPanelFormApi, {
  type GetQfdPanelFormFieldListVo,
  type QfdPanelFormDetailsVo,
} from "@/views/qf/sfc_private/flowable-designer/api/QfdPanelFormApi";

type BpmnEl = { businessObject?: Record<string, unknown> };

type ModelingLike = {
  updateProperties: (el: unknown, props: Record<string, unknown>) => void;
};

/** 从 BPMN 灌入 panelForm 期间，抑制回写 */
let bpmnSyncHydrating = false;

const panelForm = reactive<QfdPanelFormDetailsVo>({
  utFormAllowEditFields: [],
});

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

async function loadFromBpmn(_modeler: unknown, element: unknown): Promise<void> {
  const diagramEl = element as BpmnEl | null;
  if (!diagramEl?.businessObject) {
    return;
  }
  bpmnSyncHydrating = true;
  const b = diagramEl.businessObject;
  panelForm.utFormAllowEditFields = splitCsv(b.utFormAllowEditFields);
  await nextTick();
  bpmnSyncHydrating = false;
}

function uploadToBpmn(modeler: unknown, element: unknown): void {
  if (bpmnSyncHydrating) {
    return;
  }
  const diagramEl = element as BpmnEl | null;
  if (!diagramEl?.businessObject) {
    return;
  }
  const m = modeler as { get: (n: string) => unknown } | null;
  if (!m?.get) {
    return;
  }
  const modeling = m.get("modeling") as ModelingLike;
  modeling.updateProperties(diagramEl, {
    utFormAllowEditFields: joinCsv(panelForm.utFormAllowEditFields),
  });
}

export default {
  useQfdPanelForm(
    modelerGetter: () => unknown,
    elementGetter: () => unknown | null | undefined,
    formIdGetter: () => string | null | undefined
  ) {
    const fieldList = ref<GetQfdPanelFormFieldListVo[]>([]);
    const fieldLoading = ref(false);

    function loadFieldList(): Promise<void> {
      const formId = formIdGetter()?.trim();
      if (!formId) {
        fieldList.value = [];
        return Promise.resolve();
      }
      fieldLoading.value = true;
      return QfdPanelFormApi.getFormFieldList({ pageNum: 1, pageSize: 10000, formId })
        .then((res) => {
          fieldList.value = res.data ?? [];
        })
        .finally(() => {
          fieldLoading.value = false;
        });
    }

    watch(
      () => formIdGetter(),
      () => {
        void loadFieldList();
      },
      { immediate: true }
    );

    watch(
      () => [modelerGetter(), elementGetter()],
      () => {
        void loadFromBpmn(modelerGetter(), elementGetter());
      },
      { immediate: true }
    );

    watch(
      panelForm,
      () => {
        uploadToBpmn(modelerGetter(), elementGetter());
      },
      { deep: true }
    );

    /**
     * 全选当前表单全部字段为可编辑
     */
    function selectAllFields(): void {
      panelForm.utFormAllowEditFields = fieldList.value.map((row) => row.fieldName).filter(Boolean);
    }

    /**
     * 清空可编辑字段
     */
    function clearAllFields(): void {
      panelForm.utFormAllowEditFields = [];
    }

    /**
     * 字段是否已勾选为可编辑
     */
    function isFieldEditable(fieldName: string): boolean {
      return panelForm.utFormAllowEditFields.includes(fieldName);
    }

    /**
     * 切换单行可编辑状态
     */
    function toggleFieldEditable(fieldName: string, checked: boolean): void {
      const idx = panelForm.utFormAllowEditFields.indexOf(fieldName);
      if (checked) {
        if (idx >= 0) {
          return;
        }
        panelForm.utFormAllowEditFields.push(fieldName);
        return;
      }
      if (idx < 0) {
        return;
      }
      panelForm.utFormAllowEditFields.splice(idx, 1);
    }

    return {
      panelForm,
      fieldList,
      fieldLoading,
      selectAllFields,
      clearAllFields,
      isFieldEditable,
      toggleFieldEditable,
    };
  },
};
