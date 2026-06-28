import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { GetLatestScanRecordVo } from "@/views/core/api/AttachPoolApi";
import AttachPoolApi from "@/views/core/api/AttachPoolApi";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

type StatExplainKind = "indexed" | "drift";

export default {
  /**
   * 附件池扫描状态打包：加载最新扫描记录、触发扫描、附件池堆叠图配置
   */
  useAttachPoolStatus() {
    const record = ref<GetLatestScanRecordVo | null>(null);
    const loading = ref(false);
    const scanning = ref(false);
    const statExplainVisible = ref(false);
    const statExplainTitle = ref("");
    const statExplainText = ref("");
    const tabState = ref({ activeTab: "overview" });

    const activeTab = computed({
      get: (): string => tabState.value.activeTab,
      set: (val: string): void => {
        tabState.value.activeTab = val;
        QueryPersistService.persistQuery("attach-pool-tab", tabState.value);
      },
    });

    /**
     * 打开指标说明模态框
     */
    const openStatExplain = (kind: StatExplainKind): void => {
      if (kind === "indexed") {
        statExplainTitle.value = "已索引附件";
        statExplainText.value =
          "已在系统中登记且状态有效的附件。这类附件拥有完整的索引记录，可被业务正常引用、检索和访问。";
      }
      if (kind === "drift") {
        statExplainTitle.value = "游离附件";
        statExplainText.value =
          "存在于附件池目录中、但系统中没有对应有效索引记录的文件。这类文件无法被业务正常引用，通常由上传中断、索引丢失或历史残留产生，建议定期扫描并人工排查清理。";
      }
      statExplainVisible.value = true;
    };

    /**
     * 关闭指标说明模态框
     */
    const closeStatExplain = (): void => {
      statExplainVisible.value = false;
    };

    /**
     * 加载最新扫描记录
     */
    const loadRecord = async (): Promise<void> => {
      if (loading.value) {
        return;
      }
      loading.value = true;
      try {
        record.value = await AttachPoolApi.getLatestScanRecord();
      } catch (error: any) {
        record.value = null;
        if (error.message !== "无数据") {
          ElMessage.error(error.message);
        }
      } finally {
        loading.value = false;
      }
    };

    /**
     * 更新统计数据
     */
    const onQuickScan = async (): Promise<void> => {
      if (scanning.value) {
        return;
      }
      try {
        await ElMessageBox.confirm(
          "将统计附件池目录文件数量与磁盘占用，不校验已索引附件是否仍存在于磁盘，是否继续？",
          "更新统计数据",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          },
        );
      } catch {
        return;
      }
      scanning.value = true;
      try {
        await AttachPoolApi.scanAttachPool({ scanMode: 0 });
        ElMessage.success("统计数据已更新");
        await loadRecord();
      } catch (error: any) {
        ElMessage.error(error.message);
      } finally {
        scanning.value = false;
      }
    };

    /**
     * 检查索引完整性
     */
    const onDeepScan = async (): Promise<void> => {
      if (scanning.value) {
        return;
      }
      try {
        await ElMessageBox.confirm(
          "将遍历附件池并校验已索引附件是否仍存在于磁盘，耗时较长，是否继续？",
          "检查索引完整性",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          },
        );
      } catch {
        return;
      }
      scanning.value = true;
      try {
        await AttachPoolApi.scanAttachPool({ scanMode: 1 });
        ElMessage.success("索引完整性检查完成");
        await loadRecord();
      } catch (error: any) {
        ElMessage.error(error.message);
      } finally {
        scanning.value = false;
      }
    };

    /**
     * 格式化字节数为可读字符串
     */
    const formatBytes = (bytes: string | undefined | null): string => {
      const val = Number(bytes);
      if (bytes == null || bytes === "" || Number.isNaN(val) || val <= 0) {
        return "0 B";
      }
      if (val < 1024) {
        return `${val} B`;
      }
      if (val < 1024 * 1024) {
        return `${(val / 1024).toFixed(1)} KB`;
      }
      if (val < 1024 * 1024 * 1024) {
        return `${(val / 1024 / 1024).toFixed(1)} MB`;
      }
      if (val < 1024 * 1024 * 1024 * 1024) {
        return `${(val / 1024 / 1024 / 1024).toFixed(2)} GB`;
      }
      return `${(val / 1024 / 1024 / 1024 / 1024).toFixed(2)} TB`;
    };

    const diskUsageOption = computed(() => {
      const r = record.value;
      if (!r) {
        return null;
      }
      const capacity = Number(r.poolCapacityBytes);
      if (Number.isNaN(capacity) || capacity <= 0) {
        return null;
      }
      const usage = Number(r.poolUsageBytes);
      const attach = Number(r.poolAttachesBytes);
      let other = usage - attach;
      if (Number.isNaN(other) || other < 0) {
        other = 0;
      }
      let free = capacity - usage;
      if (Number.isNaN(free) || free < 0) {
        free = 0;
      }
      const usagePercent = Math.min(100, Math.round((usage / capacity) * 100));
      return {
        tooltip: {
          trigger: "item",
          formatter: (params: { seriesName: string; value: number }) =>
            `${params.seriesName}: ${formatBytes(String(params.value))}`,
        },
        legend: {
          bottom: 0,
          data: ["附件占用", "其他占用", "可用空间"],
          textStyle: {
            color: "#909399",
            fontSize: 12,
          },
        },
        grid: {
          left: 0,
          right: 0,
          top: 8,
          bottom: 36,
          containLabel: false,
        },
        xAxis: {
          type: "value",
          max: capacity,
          show: false,
        },
        yAxis: {
          type: "category",
          data: [`附件池使用率 ${usagePercent}%`],
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: {
            fontSize: 12,
            color: "#909399",
          },
        },
        series: [
          {
            name: "附件占用",
            type: "bar",
            stack: "disk",
            barWidth: 22,
            itemStyle: { color: "#409eff" },
            data: [Number.isNaN(attach) || attach < 0 ? 0 : attach],
          },
          {
            name: "其他占用",
            type: "bar",
            stack: "disk",
            itemStyle: { color: "#909399" },
            data: [other],
          },
          {
            name: "可用空间",
            type: "bar",
            stack: "disk",
            itemStyle: { color: "#dcdfe6" },
            data: [free],
          },
        ],
      };
    });

    onMounted(() => {
      QueryPersistService.loadQuery("attach-pool-tab", tabState.value);
      if (tabState.value.activeTab !== "overview" && tabState.value.activeTab !== "details") {
        tabState.value.activeTab = "overview";
      }
      loadRecord();
    });

    return {
      activeTab,
      record,
      loading,
      scanning,
      statExplainVisible,
      statExplainTitle,
      statExplainText,
      loadRecord,
      onQuickScan,
      onDeepScan,
      openStatExplain,
      closeStatExplain,
      formatBytes,
      diskUsageOption,
    };
  },
};
