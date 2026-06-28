import { ref, computed, onMounted, onUnmounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { GetLatestScanRecordVo, GetRebuildIndexStatusVo } from "@/views/core/api/AttachPoolApi";
import AttachPoolApi from "@/views/core/api/AttachPoolApi";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";

type StatExplainKind = "indexed" | "indexedLost" | "drift";

export default {
  /**
   * 附件存储池扫描状态打包：加载最新扫描记录、触发扫描、附件存储池堆叠图配置
   */
  useAttachPoolStatus() {
    const record = ref<GetLatestScanRecordVo | null>(null);
    const loading = ref(false);
    const scanning = ref(false);
    const statExplainVisible = ref(false);
    const statExplainTitle = ref("");
    const statExplainIntro = ref("");
    const statExplainTip = ref("");
    const tabState = ref({ activeTab: "overview" });
    const rebuildStatus = ref<GetRebuildIndexStatusVo | null>(null);
    const rebuildStarting = ref(false);
    const clearingInvalid = ref(false);
    let rebuildPollTimer: ReturnType<typeof setInterval> | null = null;

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
      statExplainTip.value = "";
      if (kind === "indexed") {
        statExplainTitle.value = "已索引附件";
        statExplainIntro.value =
          "已在系统中登记且状态有效的附件。这类附件拥有完整的索引记录，可被业务正常引用、检索和访问。";
      }
      if (kind === "indexedLost") {
        statExplainTitle.value = "失效索引";
        statExplainIntro.value = "数据库中有索引记录，但附件存储池目录中已找不到对应的物理文件。";
        statExplainTip.value = "可将内容完全一致（SHA256 相同）的文件复制进附件存储池，再通过「重建索引」修复。";
      }
      if (kind === "drift") {
        statExplainTitle.value = "游离附件";
        statExplainIntro.value =
          "存在于附件存储池目录中、但系统中没有对应有效索引记录的文件。这类文件无法被业务正常引用，通常由上传中断、索引丢失或历史残留产生，建议定期扫描并人工排查清理。";
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
          "将统计附件存储池目录文件数量与磁盘占用，不校验已索引附件是否仍存在于磁盘，是否继续？",
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
          "将遍历附件存储池并校验已索引附件是否仍存在于磁盘，耗时较长，是否继续？",
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
     * 停止重建索引轮询
     */
    const stopRebuildPoll = (): void => {
      if (rebuildPollTimer !== null) {
        clearInterval(rebuildPollTimer);
        rebuildPollTimer = null;
      }
    };

    /**
     * 加载重建索引进度
     */
    const loadRebuildStatus = async (): Promise<boolean> => {
      try {
        rebuildStatus.value = await AttachPoolApi.getRebuildIndexStatus();
        return rebuildStatus.value.running === true;
      } catch (error: any) {
        ElMessage.error(error.message);
        return false;
      }
    };

    /**
     * 启动重建索引轮询
     */
    const startRebuildPoll = (): void => {
      stopRebuildPoll();
      rebuildPollTimer = setInterval(async () => {
        const running = await loadRebuildStatus();
        if (running) {
          return;
        }
        stopRebuildPoll();
        await loadRecord();
        const msg = rebuildStatus.value?.message;
        if (!msg || msg === "空闲" || msg === "任务启动中" || msg === "重建索引进行中") {
          return;
        }
        if (msg.startsWith("重建索引失败") || msg.includes("正在扫描中")) {
          ElMessage.error(msg);
          return;
        }
        ElMessage.success(msg);
      }, 2000);
    };

    /**
     * 启动重建索引
     */
    const onStartRebuild = async (): Promise<void> => {
      if (rebuildStarting.value || rebuildStatus.value?.running || scanning.value) {
        return;
      }
      try {
        await ElMessageBox.confirm(
          "用于恢复游离文件与索引的对应关系。将扫描游离文件并补建或修复索引，执行期间不可进行其他扫描操作，是否开始？",
          "重建索引",
          {
            confirmButtonText: "开始",
            cancelButtonText: "取消",
            type: "warning",
          },
        );
      } catch {
        return;
      }
      rebuildStarting.value = true;
      try {
        await AttachPoolApi.startRebuildIndex();
        ElMessage.success("重建索引已开始，可在下方查看进度");
        await loadRebuildStatus();
        startRebuildPoll();
      } catch (error: any) {
        ElMessage.error(error.message);
      } finally {
        rebuildStarting.value = false;
      }
    };

    /**
     * 清除无效索引
     */
    const onClearInvalidIndexes = async (): Promise<void> => {
      if (clearingInvalid.value || scanning.value || rebuildStatus.value?.running) {
        return;
      }
      try {
        await ElMessageBox.confirm(
          "将永久删除全部无效索引记录，磁盘文件不受影响。若索引仍被业务引用，删除后关联无法恢复，确定要继续吗？",
          "清除无效索引",
          {
            confirmButtonText: "确认清除",
            cancelButtonText: "取消",
            type: "warning",
          },
        );
      } catch {
        return;
      }
      clearingInvalid.value = true;
      try {
        const msg = await AttachPoolApi.clearInvalidIndexes();
        ElMessage.success(msg);
        await loadRecord();
      } catch (error: any) {
        ElMessage.error(error.message);
      } finally {
        clearingInvalid.value = false;
      }
    };

    const rebuildRunning = computed(() => rebuildStatus.value?.running === true);

    const rebuildProgressPercent = computed(() => {
      const s = rebuildStatus.value;
      if (!s) {
        return 0;
      }
      if (!s.running && s.endTime) {
        return 100;
      }
      if (!s.total || s.total <= 0) {
        return 0;
      }
      return Math.min(100, Math.round(((s.processed ?? 0) / s.total) * 100));
    });

    const rebuildProgressStatus = computed((): "success" | "warning" | undefined => {
      if (rebuildRunning.value) {
        return undefined;
      }
      if (!rebuildStatus.value?.endTime) {
        return undefined;
      }
      if ((rebuildStatus.value.failed ?? 0) > 0) {
        return "warning";
      }
      return "success";
    });

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
          data: [`附件存储池使用率 ${usagePercent}%`],
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

    onMounted(async () => {
      QueryPersistService.loadQuery("attach-pool-tab", tabState.value);
      if (tabState.value.activeTab !== "overview" && tabState.value.activeTab !== "details") {
        tabState.value.activeTab = "overview";
      }
      await loadRecord();
      const running = await loadRebuildStatus();
      if (running) {
        startRebuildPoll();
      }
    });

    onUnmounted(() => {
      stopRebuildPoll();
    });

    return {
      activeTab,
      record,
      loading,
      scanning,
      rebuildStatus,
      rebuildStarting,
      rebuildRunning,
      rebuildProgressPercent,
      rebuildProgressStatus,
      statExplainVisible,
      statExplainTitle,
      statExplainIntro,
      statExplainTip,
      loadRecord,
      onQuickScan,
      onDeepScan,
      onStartRebuild,
      onClearInvalidIndexes,
      clearingInvalid,
      openStatExplain,
      closeStatExplain,
      formatBytes,
      diskUsageOption,
    };
  },
};
