<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery show-persist-tip>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="任务分组">
            <el-select v-model="listForm.groupId" placeholder="选择任务分组" clearable class="!w-[200px]" filterable>
              <el-option v-for="item in groupListData" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="任务名">
            <el-input v-model="listForm.name" placeholder="输入任务名" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="请选择状态" clearable class="!w-[180px]">
              <el-option label="正常" :value="0" />
              <el-option label="暂停" :value="1" />
              <el-option label="暂停(异常)" :value="2" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item>
          <el-dropdown split-button type="primary" :disabled="listLoading" @click="loadList">
            查询
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="DownloadIcon" @click="onExport">导出查询结果</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button :disabled="listLoading" style="margin-left: 12px" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction>
      <el-button type="primary" @click="openModal('add', null)">创建任务调度</el-button>
      <el-button type="danger" :disabled="listSelected.length === 0" @click="removeListBatch">批量删除</el-button>
      <el-button type="primary" :icon="UploadIcon" @click="importWizardRef?.openModal()">导入任务</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="40" />
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="groupName" label="任务分组" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="!scope.row.groupName" class="text-gray-400 text-sm"> 未配置 </span>
            <span v-else>{{ scope.row.groupName }} </span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="任务名称" min-width="150" show-overflow-tooltip />

        <el-table-column prop="kind" label="任务类型" min-width="100" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.kind === 0 ? "本地BEAN" : "远程HTTP" }}
          </template>
        </el-table-column>
        <el-table-column prop="cron" label="CRON表达式" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <ComCronFixer :cron="scope.row.cron" />
          </template>
        </el-table-column>
        <el-table-column prop="target" label="调用目标" min-width="200" show-overflow-tooltip />
        <el-table-column prop="expireTime" label="任务有效期截止" min-width="160" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.expireTime" :class="{ 'text-red-700': scope.row.isExpired === 1 }">
              {{ scope.row.expireTime }}
            </span>
            <span v-if="!scope.row.expireTime" class="text-gray-400 text-sm">长期有效</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="70" show-overflow-tooltip align="center">
          <template #default="scope">
            <div class="flex justify-center">
              <el-tag :type="scope.row.status === 0 ? 'success' : scope.row.status === 1 ? 'info' : 'danger'">
                {{ scope.row.status === 0 ? "正常" : scope.row.status === 1 ? "暂停" : "终止" }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastExecStatus" label="最近执行状态" min-width="110" show-overflow-tooltip>
          <template #default="scope">
            <div class="flex justify-center">
              <el-tag :type="scope.row.lastExecStatus === 0 ? 'success' : 'danger'">
                {{ scope.row.lastExecStatus === 0 ? "成功" : "异常" }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastStartTime" label="最近一次开始时间" min-width="170" show-overflow-tooltip />
        <el-table-column prop="lastEndTime" label="最近一次结束时间" min-width="170" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="220">
          <template #default="scope">
            <el-button
              link
              type="primary"
              size="small"
              icon="Refresh"
              :disabled="scope.row.status !== 0"
              @click="execOpenModal(scope.row)"
            >
              运行
            </el-button>
            <el-button link type="primary" size="small" icon="Edit" @click="openModal('edit', scope.row)"> 编辑 </el-button>
            <el-button link type="danger" size="small" icon="Delete" @click="removeList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 立即执行任务模态框 -->
    <el-dialog
      ref="execModalFormRef"
      v-model="execModalVisible"
      :title="`正在准备调度任务: ${execRowData?.name}`"
      width="600px"
      :close-on-click-modal="false"
      :loading="execModalLoading"
      @close="execModalVisible = false"
    >
      <div v-if="execRowData" class="mb-4 p-3 bg-gray-50 rounded border border-gray-100">
        <div class="flex items-center mb-2">
          <span class="text-gray-400 w-20">任务ID:</span>
          <span class="font-mono text-blue-600">{{ execRowData.id }}</span>
        </div>
        <div class="flex items-center">
          <span class="text-gray-400 w-20">调用目标:</span>
          <span class="truncate" :title="execRowData.target">{{ execRowData.target }}</span>
        </div>
      </div>

      <el-form
        v-if="execModalVisible"
        ref="execModalFormRef"
        :model="execModalForm"
        :rules="execModalRules"
        label-position="top"
        :validate-on-rule-change="false"
      >
        <el-form-item label="一次性参数 (JSON)" prop="targetParam">
          <el-input
            v-model="execModalForm.targetParam"
            placeholder='请输入 JSON 格式参数，例如: {"key": "value"}'
            clearable
            type="textarea"
            :autosize="{ minRows: 6 }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="execModalVisible = false">关闭</el-button>
          <el-button type="primary" :loading="execModalLoading" icon="CaretRight" @click="execSubmitModal">
            立即执行
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 创建/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑任务调度' : '创建任务调度'"
      width="850px"
      :close-on-click-modal="false"
      @close="
        resetModal();
        loadList();
      "
    >
      <el-form
        v-if="modalVisible"
        ref="modalFormRef"
        :model="modalForm"
        :rules="modalRules"
        label-width="120px"
        :validate-on-rule-change="false"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务名称" prop="name">
              <el-input v-model="modalForm.name" placeholder="请输入任务名称" clearable maxlength="80" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务分组" prop="groupId">
              <el-select v-model="modalForm.groupId" placeholder="选择任务分组" clearable filterable>
                <el-option v-for="item in groupListData" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务类型" prop="kind">
              <el-radio-group v-model="modalForm.kind">
                <el-radio label="本地BEAN" :value="0" />
                <el-radio label="远程HTTP(暂不支持)" :value="1" disabled />
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="modalForm.kind === 1" label="请求方法" prop="reqMethod">
              <el-select v-model="modalForm.reqMethod" placeholder="请选择请求方法" clearable style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="调用目标" prop="target">
              <el-input
                v-if="modalForm.kind === 1"
                v-model="modalForm.target"
                placeholder="请输入HTTP地址"
                clearable
                type="text"
                :rows="2"
                maxlength="1000"
                show-word-limit
              />
              <el-select
                v-if="modalForm.kind === 0"
                v-model="modalForm.target"
                v-loading="localBeanListLoading"
                placeholder="请选择本地任务Bean"
                clearable
                style="width: 100%"
                filterable
              >
                <el-option v-for="item in localBeanListData" :key="item.name" :label="item.name" :value="item.name">
                  <span>{{ item.name }}</span>
                  <span class="text-gray-400 text-sm ml-2">{{ item.fullClassName }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col>
            <el-form-item label="CRON表达式" prop="cron">
              <el-input
                v-model="modalForm.cron"
                placeholder="请输入CRON表达式"
                clearable
                maxlength="64"
                show-word-limit
                @focus="cronInputFocused = true"
                @blur="cronInputFocused = false"
              >
                <template #append>
                  <el-button @click="cronCalculatorRef?.openModal(modalForm.cron)">
                    <el-icon><Calendar /></el-icon>
                    使用Cron计算器
                  </el-button>
                </template>
              </el-input>
              <!-- Cron 预览区域 -->
              <div v-if="modalForm.cron && cronInputFocused" class="cron-preview-container mt-2 w-full">
                <div class="flex items-start bg-blue-50 p-2 rounded border border-blue-100 mb-2">
                  <el-icon class="mt-1 mr-2 text-blue-500"><InfoFilled /></el-icon>
                  <div>
                    <div class="text-blue-600 font-medium text-xs">语义描述：</div>
                    <div class="text-gray-600 text-xs mt-0.5">{{ cronDescription }}</div>
                  </div>
                </div>
                <div v-if="nextRunTimes.length > 0" class="next-runs bg-gray-50 p-2 rounded border border-gray-100">
                  <div class="text-[10px] text-gray-400 mb-1 font-bold uppercase tracking-wider">未来 5 次执行预览：</div>
                  <div class="grid grid-cols-1 gap-0.5">
                    <div v-for="(run, index) in nextRunTimes" :key="index" class="text-xs flex items-center text-gray-500">
                      <span class="w-4 text-gray-300">{{ index + 1 }}.</span>
                      <span>{{ run }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="过期策略" prop="policyMisfire">
              <el-select v-model="modalForm.policyMisfire" placeholder="请选择过期策略" clearable style="width: 100%">
                <el-option label="放弃执行" :value="0">
                  <span>放弃执行</span>
                  <span class="text-gray-400 text-sm ml-2 text-xs">不补跑,等待下次调度</span>
                </el-option>
                <el-option label="立即执行" :value="1">
                  <span>立即执行</span>
                  <span class="text-gray-400 text-sm ml-2 text-xs">补跑一次</span>
                </el-option>
                <el-option label="全部执行(补跑全部)" :value="2">
                  <span>全部执行</span>
                  <span class="text-gray-400 text-sm ml-2 text-xs">
                    <span class="text-red-500 font-bold">! </span>补跑全部;不建议使用</span
                  >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务有效期截止" prop="expireTime">
              <el-date-picker
                v-model="modalForm.expireTime"
                type="datetime"
                placeholder="请选择任务有效期截止"
                clearable
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="失败策略" prop="policyError">
              <el-radio-group v-model="modalForm.policyError" placeholder="请选择失败策略" clearable style="width: 100%">
                <el-radio label="默认" :value="0" />
                <el-radio label="自动暂停" :value="1" />
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="日志策略" prop="policyRcd">
              <el-select v-model="modalForm.policyRcd" placeholder="请选择日志策略" clearable style="width: 100%">
                <el-option label="全部" :value="0">
                  <span>全部</span>
                  <span class="text-gray-400 text-sm ml-2 text-xs">性能最差,适合低频任务</span>
                </el-option>
                <el-option label="仅异常" :value="1">
                  <span>仅异常</span>
                  <span class="text-gray-400 text-sm ml-2 text-xs">性能较好,适合中频任务</span>
                </el-option>
                <el-option label="不记录" :value="2">
                  <span>不记录</span>
                  <span class="text-gray-400 text-sm ml-2 text-xs">性能最佳,适合高频任务</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="并发执行" prop="concurrent">
              <el-radio-group v-model="modalForm.concurrent">
                <el-radio label="允许" :value="0" />
                <el-radio label="禁止" :value="1" />
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="modalForm.status">
                <el-radio label="正常" :value="0" />
                <el-radio label="暂停" :value="1" />
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="调用参数JSON" prop="targetParam">
              <el-input
                v-model="modalForm.targetParam"
                placeholder="请输入调用参数JSON"
                clearable
                type="textarea"
                :autosize="{ minRows: 3 }"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">关闭</el-button>
          <el-button type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Cron 计算器 -->
    <CronCalculatorModal ref="cronCalculatorRef" @on-confirm="(cron) => (modalForm.cron = cron || '')" />

    <!-- 导入向导 -->
    <ImportWizardModal
      ref="importWizardRef"
      url="/qtTask/importQtTask"
      template-code="qt_task"
      @on-success="loadList"
      @on-close="loadList"
    />
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, watch, computed, markRaw } from "vue";
import { Calendar, InfoFilled, Upload, Download } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import cronstrue from "cronstrue/i18n";
import { CronExpressionParser } from "cron-parser";
import QtTaskService from "@/views/qt/service/QtTaskService.ts";
import QtTaskApi from "@/views/qt/api/QtTaskApi.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import QtTaskGroupService from "@/views/qt/service/QtTaskGroupService";
import CronCalculatorModal from "@/views/qt/components/public/CronCalculatorModal.vue";
import ComCronFixer from "@/soa/com-series/ComCronFixer.vue";
import ImportWizardModal from "@/soa/com-series/ImportWizardModal.vue";

// 使用markRaw包装图标组件
const UploadIcon = markRaw(Upload);
const DownloadIcon = markRaw(Download);

// Cron 计算器引用
const cronCalculatorRef = ref<InstanceType<typeof CronCalculatorModal>>();

// 导入向导引用
const importWizardRef = ref<InstanceType<typeof ImportWizardModal>>();

// 导出功能
const onExport = async (): Promise<void> => {
  try {
    await QtTaskApi.exportQtTask(listForm.value);
  } catch (e: any) {
    console.error(e);
  }
};

// Cron 输入框焦点状态
const cronInputFocused = ref(false);

// 列表管理打包
const {
  listForm,
  listData,
  listTotal,
  listLoading,
  listSelected,
  loadList,
  resetList,
  removeList,
  removeListBatch,
  onSelectionChange,
} = QtTaskService.useQtTaskList();

// 任务分组列表管理打包
const { listData: groupListData, listForm: groupListForm } = QtTaskGroupService.useQtTaskGroupList();
groupListForm.value.pageSize = 10000;

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 立即执行任务模态框表单引用
const execModalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  QtTaskService.useQtTaskModal(modalFormRef, loadList);

// 立即执行任务模态框打包
const {
  modalVisible: execModalVisible,
  modalLoading: execModalLoading,
  modalForm: execModalForm,
  modalRules: execModalRules,
  openModal: execOpenModal,
  submitModal: execSubmitModal,
  rowData: execRowData,
} = QtTaskService.useExecuteTaskModal(execModalFormRef);

// 本地任务Bean列表管理打包
const {
  listData: localBeanListData,
  listLoading: localBeanListLoading,
  loadList: loadLocalBeanList,
} = QtTaskService.useLocalBeanList();

// --- Cron 预览逻辑 ---
const cronDescription = computed(() => {
  const cron = modalForm.cron;
  if (!cron) {
    return "未输入表达式";
  }
  try {
    return cronstrue.toString(cron, { locale: "zh_CN" });
  } catch {
    return "无效的表达式";
  }
});

const nextRunTimes = computed(() => {
  const cron = modalForm.cron;
  if (!cron) {
    return [];
  }
  try {
    const interval = CronExpressionParser.parse(cron, { tz: "Asia/Shanghai" });
    const times = [];
    for (let i = 0; i < 5; i++) {
      const nextDate = interval.next();
      const date = nextDate instanceof Date ? nextDate : (nextDate as any).toDate();
      times.push(formatDateTime(date));
    }
    return times;
  } catch {
    return [];
  }
});

const formatDateTime = (date: Date): string => {
  const pad = (n: number): string => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};
// --------------------

//模态框打开时加载本地任务Bean列表
watch(
  modalVisible,
  async (newVal) => {
    if (newVal) {
      await loadLocalBeanList();
    }
  },
  { immediate: true }
);
</script>

<style scoped>
.cron-preview-container {
  line-height: 1.4;
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 10;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px;
  border-radius: 4px;
}
.next-runs {
  max-height: 120px;
  overflow-y: auto;
}
</style>
