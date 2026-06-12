<template>
  <div class="login-page">
    <div class="bg-grid"></div>
    <div class="bg-dots"></div>

    <div class="login-wrapper">
      <div class="scan-line"></div>

      <div class="login-panel">
        <header class="panel-header">
          <div class="brand-box">
            <div class="brand-square"></div>
          </div>
          <h1 class="system-title">ENDPOINT ANALYSIS</h1>
          <p class="system-desc">PAT 令牌登录</p>
        </header>

        <main class="panel-body">
          <el-form ref="formRef" :model="loginForm" size="large">
            <div class="form-item">
              <div class="item-header">ACCESS TOKEN</div>
              <el-form-item prop="patToken">
                <el-input
                  v-model="loginForm.patToken"
                  type="textarea"
                  :rows="4"
                  placeholder="粘贴您的 PAT 令牌"
                  :prefix-icon="Key"
                  clearable
                  @keyup.enter="onLogin"
                />
              </el-form-item>
            </div>

            <transition name="slide-up">
              <div v-if="errorMessage" class="error-notification">
                <span class="err-tag">ERR_CODE_01:</span>
                {{ errorMessage }}
              </div>
            </transition>

            <div class="button-container">
              <el-button type="primary" class="auth-button" :loading="isLoading" @click="onLogin">
                {{ isLoading ? "正在验证" : "验证令牌" }}
              </el-button>
            </div>
          </el-form>
        </main>

        <footer class="panel-footer">
          <span class="version-tag">CORE 1.7.F1</span>
          <a class="nav-link" @click="onUserLogin">账号密码登录</a>
        </footer>
      </div>
    </div>

    <aside class="side-info left">127.0.0.1 / SECURED</aside>
    <aside class="side-info right">UTC+8:00 / ACTIVE</aside>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Key } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import UserAuthService from "@/views/auth/service/UserAuthService";
import ComTabService from "@/soa/com-series/service/ComTabService";

const router = useRouter();
const { patLogin } = UserAuthService.useUserAuth();
const { clearTabs } = ComTabService.useTabService();

const formRef = ref<FormInstance | null>(null);

const loginForm = ref<{ patToken: string }>({
  patToken: "",
});

const errorMessage = ref<string>("");
const isLoading = ref<boolean>(false);

const onLogin = async (): Promise<void> => {
  errorMessage.value = "";

  if (!loginForm.value.patToken.trim()) {
    errorMessage.value = "请输入PAT令牌";
    return;
  }

  if (isLoading.value) {
    return;
  }

  isLoading.value = true;

  try {
    await patLogin(loginForm.value.patToken.trim());
    ElMessage.success("PAT令牌验证通过");
    clearTabs();
    await router.push({ path: "/" });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : "令牌验证失败";
  } finally {
    isLoading.value = false;
  }
};

const onUserLogin = (): void => {
  router.push({ name: "login" });
};
</script>

<style scoped>
.login-page {
  --p-main: #7c3aed;
  --p-bg: #f8fafc;
  --p-panel: #ffffff;
  --p-border: #e2e8f0;
  --p-text: #1e293b;
  --p-text-light: #64748b;
  --p-input: #fcfcfc;
}

.login-page {
  width: 100vw;
  height: 100vh;
  background-color: var(--p-bg);
  color: var(--p-text);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  font-family: "PingFang SC", "Segoe UI", "Consolas", sans-serif;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(to right, rgba(124, 58, 237, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(124, 58, 237, 0.05) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 1;
}

.bg-dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(124, 58, 237, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: 2;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(124, 58, 237, 0.4), transparent);
  box-shadow: 0 0 10px rgba(124, 58, 237, 0.2);
  animation: scan 5s infinite ease-in-out;
  z-index: 11;
  opacity: 0.8;
}

@keyframes scan {
  0% {
    transform: translateY(0);
    opacity: 0;
  }
  20% {
    opacity: 1;
  }
  80% {
    opacity: 1;
  }
  100% {
    transform: translateY(400px);
    opacity: 0;
  }
}

.login-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 460px;
  padding: 20px;
}

.login-panel {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border: 1px solid var(--p-border);
  box-shadow: 0 40px 80px -20px rgba(0, 0, 0, 0.1);
  padding: 48px;
  position: relative;
  z-index: 12;
}

.panel-header {
  text-align: center;
  margin-bottom: 40px;
}

.brand-box {
  width: 44px;
  height: 44px;
  border: 1.5px solid var(--p-main);
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-square {
  width: 18px;
  height: 18px;
  background: var(--p-main);
}

.system-title {
  font-size: 1.25rem;
  font-weight: 600;
  letter-spacing: 3px;
  margin: 0;
  color: var(--p-text);
}

.system-desc {
  font-size: 0.7rem;
  color: var(--p-text-light);
  margin-top: 10px;
  letter-spacing: 1px;
  text-transform: uppercase;
  font-weight: 500;
}

.form-item {
  margin-bottom: 24px;
}

.item-header {
  font-size: 0.7rem;
  color: var(--p-text-light);
  margin-bottom: 10px;
  font-weight: bold;
  letter-spacing: 0.5px;
}

:deep(.el-textarea__inner) {
  background-color: var(--p-input) !important;
  box-shadow: none !important;
  border: 1px solid var(--p-border) !important;
  border-radius: 0 !important;
  padding: 12px 14px !important;
  transition: all 0.3s;
  font-family: "Consolas", monospace;
  font-size: 0.85rem;
  resize: none;
}

:deep(.el-textarea__inner:focus) {
  border-color: var(--p-main) !important;
  background-color: #fff !important;
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.1) !important;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

.error-notification {
  margin-top: 8px;
  margin-bottom: 16px;
  font-size: 0.75rem;
  color: #ef4444;
  background: #fef2f2;
  padding: 10px 15px;
  border-left: 3px solid #ef4444;
}

.err-tag {
  font-weight: bold;
  margin-right: 8px;
}

.auth-button {
  width: 100%;
  height: 52px;
  border-radius: 0 !important;
  background-color: var(--p-main) !important;
  border: none !important;
  font-size: 0.85rem;
  font-weight: bold;
  letter-spacing: 1px;
  text-transform: uppercase;
  transition: all 0.3s;
  box-shadow: 0 8px 16px -4px rgba(124, 58, 237, 0.3);
}

.auth-button:hover {
  filter: brightness(1.05);
  box-shadow: 0 12px 24px -6px rgba(124, 58, 237, 0.4);
  transform: translateY(-1px);
}

.auth-button:active {
  transform: translateY(1px);
}

.panel-footer {
  margin-top: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.65rem;
}

.version-tag {
  color: var(--p-text-light);
  font-family: "Consolas", monospace;
  letter-spacing: 1px;
}

.nav-link {
  color: var(--p-main);
  text-decoration: none;
  font-weight: 600;
  cursor: pointer;
}

.nav-link:hover {
  text-decoration: underline;
}

.side-info {
  position: absolute;
  bottom: 24px;
  font-family: "Consolas", monospace;
  font-size: 0.6rem;
  font-weight: 600;
  color: var(--p-text-light);
  letter-spacing: 2px;
  opacity: 0.5;
}

.side-info.left {
  left: 40px;
}
.side-info.right {
  right: 40px;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
