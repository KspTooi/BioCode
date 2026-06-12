<template>
  <div class="login-page">
    <!-- 亮色系数字化蓝图背景 -->
    <div class="bg-grid"></div>
    <div class="bg-dots"></div>

    <div class="login-wrapper">
      <!-- 动态扫描线 (青色光束) -->
      <div class="scan-line"></div>

      <div class="login-panel">
        <header class="panel-header">
          <div class="brand-box">
            <div class="brand-square"></div>
          </div>
          <h1 class="system-title">ENDPOINT ANALYSIS</h1>
          <p class="system-desc">EAS 系统登录</p>
        </header>

        <main class="panel-body">
          <el-form ref="formRef" :model="loginForm" :rules="formRules" size="large">
            <div class="form-item">
              <div class="item-header">ACCOUNT ID</div>
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  placeholder="输入您的账户 ID"
                  :prefix-icon="User"
                  clearable
                  @keyup.enter="onLogin"
                />
              </el-form-item>
            </div>

            <div class="form-item">
              <div class="item-header">密钥</div>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="输入您的安全密钥"
                  :prefix-icon="Lock"
                  show-password
                  @keyup.enter="onLogin"
                />
              </el-form-item>
            </div>

            <el-form-item v-if="loginConfig?.enabledSavePasswordOnClient === 1">
              <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
            </el-form-item>

            <transition name="slide-up">
              <div v-if="errorMessage" class="error-notification">
                <span class="err-tag">ERR_CODE_01:</span>
                {{ errorMessage }}
              </div>
            </transition>

            <div class="button-container">
              <el-button type="primary" class="auth-button" :loading="isLoading" @click="onLogin">
                {{ isLoading ? "正在处理" : "登录" }}
              </el-button>
            </div>
          </el-form>
        </main>

        <footer class="panel-footer">
          <span class="version-tag">CORE 1.7.F1</span>
          <div class="footer-links">
            <a class="nav-link" @click="onPatLogin">PAT令牌登录</a>
            <!-- <a class="nav-link" @click="onRegister">申请系统访问权限</a> -->
          </div>
        </footer>
      </div>
    </div>

    <!-- 装饰性脚注 -->
    <aside class="side-info left">127.0.0.1 / SECURED</aside>
    <aside class="side-info right">UTC+8:00 / ACTIVE</aside>
  </div>
  <ComTacCaptchaDialog ref="captchaDialogRef" @on-success="onCaptchaSuccess" @on-error="onCaptchaError" />
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock } from "@element-plus/icons-vue";
import type { FormInstance, FormRules } from "element-plus";
import UserAuthService from "@/views/auth/service/UserAuthService.ts";
import type { UserLoginDto, GetLoginConfigVo } from "@/views/auth/api/AuthApi.ts";
import ComTacCaptchaDialog from "@/soa/com-series/components/ComTacCaptchaDialog.vue";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";

const router = useRouter();
const { getLoginConfig, saveAccount, clearAccount, loadAccount, login } = UserAuthService.useUserAuth();
const { clearTabs } = ComTabService.useTabService();

const formRef = ref<FormInstance | null>(null);

const loginForm = ref<UserLoginDto>({
  username: "",
  password: "",
});

const formRules: FormRules = {
  username: { required: true, message: "请输入登录账号", trigger: "blur" },
  password: { required: true, message: "请输入密码", trigger: "blur" },
};

const errorMessage = ref<string>("");
const isLoading = ref<boolean>(false);
const loginConfig = ref<GetLoginConfigVo | null>(null);
const rememberPassword = ref<boolean>(false);
const captchaDialogRef = ref<{ openModal: () => void } | null>(null);

const doLogin = async (): Promise<void> => {
  if (isLoading.value) {
    return;
  }

  isLoading.value = true;

  try {
    await login(loginForm.value.username, loginForm.value.password);
    ElMessage.success("用户验证通过");

    //重新登录后清空标签页
    clearTabs();

    if (rememberPassword.value && loginConfig.value?.enabledSavePasswordOnClient === 1) {
      saveAccount(loginForm.value.username, loginForm.value.password);
    }
    if (!rememberPassword.value) {
      clearAccount();
    }

    await router.push({ path: "/" });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : "网络同步异常，同步失败";
  } finally {
    isLoading.value = false;
  }
};

const openCaptcha = (): void => {
  if (!captchaDialogRef.value?.openModal) {
    errorMessage.value = "验证码组件未就绪，请刷新页面重试";
    return;
  }

  captchaDialogRef.value.openModal();
};

const onCaptchaSuccess = async (): Promise<void> => {
  await doLogin();
};

const onCaptchaError = (message: string): void => {
  if (!message) {
    errorMessage.value = "验证码初始化失败，请稍后重试";
    return;
  }

  errorMessage.value = message;
};

const onLogin = (): void => {
  errorMessage.value = "";

  formRef.value?.validate((valid) => {
    if (!valid) {
      return;
    }

    if (loginConfig.value?.captchaEnabledLogin !== 1) {
      doLogin();
      return;
    }

    openCaptcha();
  });
};

const onRegister = (): void => {
  router.push({ name: "register" });
};

const onPatLogin = (): void => {
  router.push({ name: "pat-login" });
};

onMounted(async () => {
  try {
    loginConfig.value = await getLoginConfig();
  } catch {
    // 配置拉取失败不阻塞登录流程，默认走无验证码
  }

  if (loginConfig.value?.enabledSavePasswordOnClient !== 1) {
    clearAccount();
    return;
  }

  const saved = loadAccount();
  if (saved) {
    loginForm.value.username = saved.username;
    loginForm.value.password = saved.password;
    rememberPassword.value = true;
  }
});
</script>

<style scoped>
.login-page {
  /* 定义亮色设计变量 */
  --p-main: #009688;
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

/* 数字化蓝图背景 */
.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(to right, rgba(0, 150, 136, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(0, 150, 136, 0.05) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 1;
}

.bg-dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(0, 150, 136, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: 2;
}

/* 扫描线效果 */
.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(0, 150, 136, 0.4), transparent);
  box-shadow: 0 0 10px rgba(0, 150, 136, 0.2);
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

/* 核心面板 (硬朗直角风格) */
.login-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
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

/* 顶部品牌设计 */
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

/* 状态栏 */
.auth-infobar {
  border-top: 1px solid var(--p-border);
  border-bottom: 1px solid var(--p-border);
  padding: 10px 0;
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
  gap: 12px;
  font-size: 0.65rem;
  font-weight: 600;
  letter-spacing: 1px;
}

.info-label {
  color: var(--p-text-light);
}

.info-value {
  color: var(--p-main);
}

.blink {
  animation: blink-anim 0.8s infinite;
}

@keyframes blink-anim {
  50% {
    opacity: 0.2;
  }
}

/* 表单逻辑 */
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

/* Element Plus 重写为硬核直角 */
:deep(.el-input__wrapper) {
  background-color: var(--p-input) !important;
  box-shadow: none !important;
  border: 1px solid var(--p-border) !important;
  border-radius: 0 !important; /* 强制直角 */
  padding: 6px 14px !important;
  transition: all 0.3s;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--p-main) !important;
  background-color: #fff !important;
  box-shadow: 0 4px 12px rgba(0, 150, 136, 0.1) !important;
}

:deep(.el-input__inner) {
  color: var(--p-text) !important;
  font-weight: 500;
}

/* 表单校验与布局适配 */
:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-form-item.is-error .el-input__wrapper) {
  border-color: #ef4444 !important;
}

:deep(.el-checkbox__label) {
  color: var(--p-text-light);
  font-size: 0.75rem;
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

/* 极简直角按钮 */
.auth-button {
  width: 100%;
  height: 52px;
  border-radius: 0 !important; /* 彻底直角 */
  background-color: var(--p-main) !important;
  border: none !important;
  font-size: 0.85rem;
  font-weight: bold;
  letter-spacing: 1px;
  text-transform: uppercase;
  transition: all 0.3s;
  box-shadow: 0 8px 16px -4px rgba(0, 150, 136, 0.3);
}

.auth-button:hover {
  filter: brightness(1.05);
  box-shadow: 0 12px 24px -6px rgba(0, 150, 136, 0.4);
  transform: translateY(-1px);
}

.auth-button:active {
  transform: translateY(1px);
}

/* 底部操作 */
.panel-footer {
  margin-top: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.65rem;
}

.footer-links {
  display: flex;
  gap: 16px;
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

/* 侧边装饰文本 */
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

/* 动画效果 */
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
