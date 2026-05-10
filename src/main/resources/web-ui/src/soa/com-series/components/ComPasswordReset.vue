<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="400px"
    :close-on-click-modal="false"
    destroy-on-close
    append-to-body
    class="change-password-dialog"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="compact-form">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" placeholder="请输入原有的登录密码" show-password />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" placeholder="请输入新的登录密码" show-password />
      </el-form-item>

      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="onSubmit"> 确认修改 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import AuthApi from "@/soa/com-series/api/AuthApi.js";

const visible = ref(false);
const loading = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const validateConfirmPassword = (rule: any, value: any, callback: any): void => {
  if (value === "") {
    callback(new Error("请再次输入新密码"));
    return;
  }
  if (value !== form.newPassword) {
    callback(new Error("两次输入的密码不一致"));
    return;
  }
  callback();
};

const rules = reactive<FormRules>({
  oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度需在 6-20 位之间", trigger: "blur" },
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: "blur" }],
});

const openModal = (): void => {
  visible.value = true;
  loading.value = false;
  form.oldPassword = "";
  form.newPassword = "";
  form.confirmPassword = "";
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

const onSubmit = async (): Promise<void> => {
  if (!formRef.value) {
    return;
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    loading.value = true;
    try {
      await AuthApi.changePassword(form.oldPassword, form.newPassword);
      ElMessage.success("密码修改成功，请重新登录");
      visible.value = false;
      // 修改密码后通常需要重新登录
      setTimeout(() => {
        window.location.href = "/login";
      }, 1500);
    } catch (error: any) {
      ElMessage.error(error.message || "修改失败");
    } finally {
      loading.value = false;
    }
  });
};

defineExpose({
  openModal,
});
</script>

<style scoped>
.change-password-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}

.compact-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.compact-form :deep(.el-form-item__label) {
  padding-bottom: 4px;
  font-weight: 600;
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-input__wrapper) {
  border-radius: 0;
}

:deep(.el-button) {
  border-radius: 0;
}
</style>
