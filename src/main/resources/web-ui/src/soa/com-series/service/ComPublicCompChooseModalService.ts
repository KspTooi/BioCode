import { computed, ref } from "vue";
import ComPublicCompService from "@/soa/com-series/service/ComPublicCompService";
import type { PublicCompEntry } from "@/soa/com-series/service/ComPublicCompService";

export type { PublicCompEntry };

/**
 * 公共组件选择模态框服务
 * 提供组件列表过滤、当前选中行管理，以及根据外部 key 回填选中状态。
 * @since 1.6.25(Y).23
 */
export default {
  /**
   * 公共组件选择模态框状态管理
   *
   * 提供组件列表过滤、当前选中行管理，以及根据外部 key 回填选中状态。
   * modalVisible 由 SFC 的 defineModel 直接管理，不在此 service 中保留。
   */
  useComPublicCompChooseModal() {
    const { listPublicComps } = ComPublicCompService.usePublicComp();

    const searchKeyword = ref("");
    const selectedRow = ref<PublicCompEntry | null>(null);

    const compList = computed<PublicCompEntry[]>(() => listPublicComps());

    /**
     * 搜索过滤：biz / name / key 任一包含关键字（大小写不敏感）
     */
    const filteredCompList = computed<PublicCompEntry[]>(() => {
      const kw = searchKeyword.value.trim().toLowerCase();
      if (!kw) {
        return compList.value;
      }
      return compList.value.filter(
        (row) => row.biz.toLowerCase().includes(kw) || row.name.toLowerCase().includes(kw) || row.key.toLowerCase().includes(kw)
      );
    });

    /**
     * 根据外部传入的 key 字符串恢复选中行（打开模态框时回填用）。
     * @param key 格式为 "biz:name"，为 null 时清空选中
     */
    const restoreSelection = (key: string | null): void => {
      if (!key) {
        selectedRow.value = null;
        return;
      }
      const found = compList.value.find((row) => row.key === key) ?? null;
      selectedRow.value = found;
    };

    /**
     * 清空选中行。
     */
    const clearSelection = (): void => {
      selectedRow.value = null;
    };

    return { searchKeyword, selectedRow, compList, filteredCompList, restoreSelection, clearSelection };
  },
};
