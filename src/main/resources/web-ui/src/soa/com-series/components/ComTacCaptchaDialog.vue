<template>
  <el-dialog
    v-model="visible"
    title="安全验证"
    width="360px"
    :close-on-click-modal="false"
    :show-close="true"
    destroy-on-close
    append-to-body
    class="com-tac-captcha-dialog"
    @close="handleDialogClose"
  >
    <div :id="captchaBindId" class="captcha-box"></div>
  </el-dialog>
</template>

<script setup lang="ts">
import Http from "@/commons/Http.js";
import { nextTick, onBeforeUnmount, ref } from "vue";

declare global {
  interface Window {
    initTAC?: (
      path: string | Record<string, unknown>,
      config: Record<string, unknown>,
      style?: Record<string, unknown>
    ) => Promise<any>;
  }
}

const emit = defineEmits<{
  (e: "on-success", payload: any): void;
  (e: "on-error", message: string): void;
  (e: "on-close"): void;
}>();

const visible = ref(false);
const tacInstance = ref<any>(null);
const captchaBindId = `com-tac-captcha-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;

const destroyTac = (): void => {
  if (tacInstance.value?.destroyWindow) {
    tacInstance.value.destroyWindow();
  }

  tacInstance.value = null;
};

const closeModal = (): void => {
  destroyTac();
  visible.value = false;
};

const handleDialogClose = (): void => {
  destroyTac();
  emit("on-close");
};

const buildCaptchaConfig = (): Record<string, unknown> => {
  return {
    requestCaptchaDataUrl: Http.resolve("/auth/genCaptcha"),
    validCaptchaUrl: Http.resolve("/auth/check"),
    bindEl: `#${captchaBindId}`,
    validSuccess: (res: any, _captcha: any, tac: any) => {
      if (tac?.destroyWindow) {
        tac.destroyWindow();
      }

      visible.value = false;
      emit("on-success", res);
    },
    validFail: (_res: any, _captcha: any, tac: any) => {
      if (!tac?.reloadCaptcha) {
        return;
      }

      tac.reloadCaptcha();
    },
    btnRefreshFun: (_el: any, tac: any) => {
      if (!tac?.reloadCaptcha) {
        return;
      }

      tac.reloadCaptcha();
    },
    btnCloseFun: (_el: any, tac: any) => {
      if (tac?.destroyWindow) {
        tac.destroyWindow();
      }

      visible.value = false;
      emit("on-close");
    },
  };
};

const initCaptcha = async (): Promise<void> => {
  if (!window.initTAC) {
    emit("on-error", "验证码脚本未加载");
    return;
  }

  destroyTac();

  try {
    tacInstance.value = await window.initTAC(
      {
        scriptUrls: [`/js/tac.min.js`],
        cssUrls: [`/css/tac.css`],
      },
      buildCaptchaConfig(),
      {
        logoUrl: null,
      }
    );
    tacInstance.value.init();
  } catch {
    emit("on-error", "验证码初始化失败");
  }
};

const openModal = async (): Promise<void> => {
  visible.value = true;
  await nextTick();
  await initCaptcha();
};

onBeforeUnmount(() => {
  destroyTac();
});

defineExpose({
  openModal,
  closeModal,
});
</script>

<style scoped>
.captcha-box {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

/* 深度覆盖 TAC 内部样式，匹配系统硬朗直角风格 */
:deep(#tianai-captcha-parent) {
  box-shadow: none !important;
  border: none !important;
  border-radius: 0 !important;
  padding: 0 !important;
  width: 300px !important;
  height: auto !important;
  background: transparent !important;
}

:deep(#tianai-captcha) {
  width: 300px !important;
}

:deep(.bg-img-div) {
  border: 1px solid #e2e8f0;
  box-sizing: border-box;
}

:deep(.bg-img-div img) {
  border-radius: 0 !important;
  display: block;
}

:deep(.slider-move-track) {
  border-radius: 0 !important;
  background: #f8fafc !important;
  border: 1px solid #e2e8f0 !important;
  height: 38px !important;
  line-height: 38px !important;
}

:deep(#tianai-captcha-slider-move-track-mask) {
  border-radius: 0 !important;
  background: rgba(0, 150, 136, 0.08) !important;
  border: none !important;
  border-right: 2px solid #009688 !important;
  height: 38px !important;
  top: -1px !important;
}

:deep(.slider-move-btn) {
  border-radius: 0 !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12) !important;
  border: 1px solid #e2e8f0 !important;
  height: 38px !important;
  width: 50px !important;
  top: -1px !important;
  background-color: #fff !important;
  transition: background-color 0.2s;
}

:deep(.slider-move-btn:hover) {
  background-color: #f1f5f9 !important;
}

:deep(.tianai-captcha-tips) {
  border-radius: 0 !important;
  font-size: 13px !important;
  height: 32px !important;
  line-height: 32px !important;
}

:deep(.slider-bottom) {
  border-top: 1px solid #f1f5f9;
  padding-top: 12px;
  margin-top: 15px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.slider-bottom .logo) {
  display: none !important;
}

:deep(.slider-bottom .refresh-btn),
:deep(.slider-bottom .close-btn) {
  margin: 0 !important;
  opacity: 0.6;
  transition: opacity 0.2s;
}

:deep(.slider-bottom .refresh-btn:hover),
:deep(.slider-bottom .close-btn:hover) {
  opacity: 1;
}

/* 弹窗样式调整 */
.com-tac-captcha-dialog :deep(.el-dialog) {
  border-radius: 0 !important;
  border: 1px solid #e2e8f0;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
}

.com-tac-captcha-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 14px 20px;
  border-bottom: 1px solid #f1f5f9;
  background-color: #fcfcfc;
}

.com-tac-captcha-dialog :deep(.el-dialog__title) {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1.5px;
  color: #334155;
  text-transform: uppercase;
}

.com-tac-captcha-dialog :deep(.el-dialog__headerbtn) {
  top: 14px;
}

.com-tac-captcha-dialog :deep(.el-dialog__body) {
  padding: 24px !important;
}

.com-tac-captcha-dialog :deep(.slider-tip) {
  font-size: 13px !important;
  color: #64748b !important;
  margin-bottom: 12px !important;
  font-weight: 500 !important;
}
</style>
