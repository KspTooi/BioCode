<template>
  <!-- 正常维护中心 -->
  <el-scrollbar v-show="!isInstallWizardMode" class="maintain-scrollbar-wrapper w-full">
    <div class="maintain-admin-portal">
      <header class="portal-header">
        <h1 class="portal-title">系统核心维护中心</h1>
        <p class="portal-subtitle">管理底层数据校验、权限同步及核心资产重置逻辑</p>
      </header>

      <div class="maintain-grid">
        <div
          v-for="(operation, index) in maintainOperations"
          :key="index"
          class="maintain-card"
          :style="{ '--op-accent': operation.iconColor }"
        >
          <div class="card-accent-bar"></div>
          <div class="card-content">
            <div class="icon-wrapper" :style="{ color: operation.iconColor, backgroundColor: operation.bgColor }">
              <el-icon :size="28">
                <component :is="operation.icon" />
              </el-icon>
            </div>
            <div class="text-wrapper">
              <h3 class="label">{{ operation.title }}</h3>
              <p class="desc">{{ operation.description }}</p>
            </div>
            <div class="action-wrapper">
              <el-button type="primary" class="execute-btn" :loading="loading" @click="executeOperation(operation)">
                <el-icon class="mr-1"><component :is="operation.icon" /></el-icon>
                {{ operation.buttonText }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-scrollbar>

  <!-- 安装向导 -->
  <div v-if="isInstallWizardMode" class="wizard-wrapper">
    <!-- 欢迎步骤 -->
    <div v-if="wizardStep === 'welcome'" class="wizard-panel">
      <div class="wizard-icon-wrap">
        <el-icon :size="52" color="#009688"><Setting /></el-icon>
      </div>
      <h1 class="wizard-title">系统安装向导</h1>
      <p class="wizard-subtitle">检测到系统需要初始化或升级，向导将自动完成以下操作：</p>

      <div class="wizard-steps-preview">
        <div v-for="(step, i) in wizardStepDescs" :key="i" class="step-item">
          <el-icon class="step-icon" color="#009688"><CircleCheck /></el-icon>
          <span>{{ step }}</span>
        </div>
      </div>

      <div class="wizard-actions">
        <el-button type="primary" size="large" class="wizard-start-btn" @click="startWizard">
          <el-icon class="mr-1"><VideoPlay /></el-icon>
          开始执行
        </el-button>
      </div>
    </div>

    <!-- 执行中步骤 -->
    <div v-if="wizardStep === 'running'" class="wizard-panel wizard-running-panel">
      <div class="wizard-spinner">
        <div class="spinner-ring"></div>
      </div>
      <h2 class="wizard-running-title">正在执行安装向导</h2>
      <p class="wizard-running-hint">请勿关闭此页面，稍等片刻...</p>
    </div>

    <!-- 完成步骤 -->
    <div v-if="wizardStep === 'complete'" class="wizard-panel">
      <div class="wizard-icon-wrap wizard-icon-success">
        <el-icon :size="52" color="#67C23A"><CircleCheckFilled /></el-icon>
      </div>
      <h1 class="wizard-title">安装向导已完成</h1>

      <div v-if="wizardResult" class="wizard-result-box">
        <div v-for="(line, i) in wizardResult.changesContent" :key="i" class="wizard-result-item">
          <el-icon color="#009688" style="flex-shrink: 0"><Right /></el-icon>
          <span>{{ line }}</span>
        </div>
      </div>

      <div class="wizard-reload-tip">
        <el-icon color="#E6A23C" style="flex-shrink: 0"><WarningFilled /></el-icon>
        <span>请刷新页面并重新登录以使配置生效</span>
      </div>

      <div class="wizard-actions">
        <el-button type="primary" size="large" class="wizard-start-btn" @click="reloadPage"> 刷新页面 </el-button>
      </div>
    </div>

    <!-- 失败步骤 -->
    <div v-if="wizardStep === 'error'" class="wizard-panel">
      <div class="wizard-icon-wrap wizard-icon-error">
        <el-icon :size="52" color="#F56C6C"><CircleCloseFilled /></el-icon>
      </div>
      <h1 class="wizard-title">执行失败</h1>
      <p class="wizard-error-msg">{{ wizardError }}</p>
      <div class="wizard-actions">
        <el-button size="large" class="wizard-start-btn" @click="wizardStep = 'welcome'"> 重新尝试 </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import MaintainService from "@/views/core/service/MaintainService.ts";
import MaintainApi, { type ExecuteInstallWizardVo } from "@/views/core/api/MaintainApi.ts";
import { ref, onMounted } from "vue";
import {
  Setting,
  CircleCheck,
  VideoPlay,
  CircleCheckFilled,
  CircleCloseFilled,
  Right,
  WarningFilled,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const isInstallWizardMode = ref(false);

// 向导步骤: welcome | running | complete | error
const wizardStep = ref<"welcome" | "running" | "complete" | "error">("welcome");
const wizardResult = ref<ExecuteInstallWizardVo | null>(null);
const wizardError = ref("");

const wizardStepDescs = [
  "修复系统注册表（补全缺失的内置配置项）",
  "升级数据库（执行待执行的迁移脚本）",
  "校验系统内置权限码（新增/移除已变更的权限）",
  "用户体系冷启动（初始化用户组和内置账号）",
];

onMounted(async () => {
  try {
    isInstallWizardMode.value = await MaintainApi.checkInstallWizardMode();
  } catch (error) {
    console.error(error);
    isInstallWizardMode.value = false;
  }
});

/**
 * 开始执行安装向导
 */
const startWizard = async (): Promise<void> => {
  wizardStep.value = "running";
  try {
    wizardResult.value = await MaintainApi.executeInstallWizard();
    wizardStep.value = "complete";
  } catch (error: any) {
    wizardError.value = error.message || "安装向导执行失败，请重试。";
    wizardStep.value = "error";
    ElMessage.error(wizardError.value);
  }
};

/**
 * 向导完成后刷新页面
 */
const reloadPage = (): void => {
  window.location.reload();
};

/**
 * 维护中心管理逻辑
 */
const { loading, maintainOperations, executeOperation } = MaintainService.useMaintainOperation();
</script>

<style scoped lang="scss">
.maintain-scrollbar-wrapper {
  height: 100%;
  background-color: #fff;
}

.maintain-admin-portal {
  padding: 30px;
  width: 100%;
  box-sizing: border-box;
}

.portal-header {
  margin-bottom: 40px;
  border-left: 4px solid #009688;
  padding-left: 20px;

  .portal-title {
    font-size: 22px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
  }

  .portal-subtitle {
    font-size: 13px;
    color: #999;
    margin: 0;
  }
}

.maintain-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.maintain-card {
  position: relative;
  background-color: #fff;
  border: 1px solid #ebeef5;
  transition: all 0.25s cubic-bezier(0.645, 0.045, 0.355, 1);
  display: flex;
  flex-direction: column;
  min-height: 280px;

  &:hover {
    border-color: var(--op-accent);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);

    .card-accent-bar {
      opacity: 1;
    }
  }

  .card-accent-bar {
    position: absolute;
    top: -1px;
    left: -1px;
    right: -1px;
    height: 3px;
    background: linear-gradient(90deg, var(--op-accent) 0%, #00a8be 100%);
    opacity: 0.6;
    transition: opacity 0.3s;
  }
}

.card-content {
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  height: 100%;
  box-sizing: border-box;
}

.icon-wrapper {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  flex-shrink: 0;
}

.text-wrapper {
  flex-grow: 1;
  margin-bottom: 24px;

  .label {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 12px 0;
  }

  .desc {
    font-size: 13px;
    color: #666;
    line-height: 1.6;
    margin: 0;
  }
}

.action-wrapper {
  width: 100%;

  .execute-btn {
    width: 100%;
    height: 38px;
    font-size: 13px;
    border-radius: 0 !important;
  }
}

// ==================== 安装向导 ====================
.wizard-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.wizard-panel {
  background: #fff;
  border: 1px solid #ebeef5;
  padding: 48px 56px;
  width: 560px;
  max-width: 90vw;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.wizard-icon-wrap {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 88px;
  background: rgba(0, 150, 136, 0.08);
  border-radius: 50%;
}

.wizard-icon-success {
  background: rgba(103, 194, 58, 0.08);
}

.wizard-icon-error {
  background: rgba(245, 108, 108, 0.08);
}

.wizard-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.wizard-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0 0 28px 0;
  line-height: 1.6;
}

.wizard-steps-preview {
  width: 100%;
  margin-bottom: 36px;
  text-align: left;
  background: #f8fafc;
  border: 1px solid #ebeef5;
  padding: 20px 24px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #606266;

  .step-icon {
    flex-shrink: 0;
  }
}

.wizard-actions {
  width: 100%;
}

.wizard-start-btn {
  width: 100%;
  height: 42px;
  font-size: 14px;
  border-radius: 0 !important;
}

// 执行中面板
.wizard-running-panel {
  padding: 64px 56px;
}

.wizard-spinner {
  margin-bottom: 32px;
}

.spinner-ring {
  width: 56px;
  height: 56px;
  border: 4px solid #e4e7ed;
  border-top-color: #009688;
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.wizard-running-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
}

.wizard-running-hint {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

// 结果区域
.wizard-result-box {
  width: 100%;
  margin-bottom: 32px;
  background: #f8fafc;
  border: 1px solid #ebeef5;
  padding: 16px 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: left;
}

.wizard-result-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.wizard-reload-tip {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #e6a23c;
  background: rgba(230, 162, 60, 0.08);
  border: 1px solid rgba(230, 162, 60, 0.3);
  padding: 10px 14px;
  box-sizing: border-box;
  margin-bottom: 20px;
}

.wizard-error-msg {
  font-size: 13px;
  color: #f56c6c;
  margin: 0 0 32px 0;
  line-height: 1.6;
  word-break: break-all;
}
</style>
