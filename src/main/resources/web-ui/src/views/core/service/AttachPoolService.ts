import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { GetLatestScanRecordVo } from "@/views/core/api/AttachPoolApi";
import AttachPoolApi from "@/views/core/api/AttachPoolApi";

export default {
  /**
   * 附件池扫描状态打包：加载最新扫描记录、触发扫描
   */
  useAttachPoolStatus() {
    const record = ref<GetLatestScanRecordVo | null>(null);
    const loading = ref(false);
    const scanning = ref(false);

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
     * 触发附件池扫描
     */
    const onScan = async (): Promise<void> => {
      if (scanning.value) {
        return;
      }
      try {
        await ElMessageBox.confirm("扫描将遍历附件池目录并统计文件，可能需要较长时间，是否继续？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
      } catch {
        return;
      }
      scanning.value = true;
      try {
        const msg = await AttachPoolApi.scanAttachPool();
        ElMessage.success(msg);
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

    onMounted(() => {
      loadRecord();
    });

    return {
      record,
      loading,
      scanning,
      loadRecord,
      onScan,
      formatBytes,
    };
  },
};
