<template>
  <el-popover
    placement="bottom-end"
    :width="340"
    trigger="click"
    popper-style="padding: 0; border-radius: 0; overflow: hidden; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1); border: 1px solid #ebeef5;"
  >
    <template #reference>
      <div class="user-info">
        <el-avatar :size="24" :src="avatarUrl" style="margin-right: 8px" shape="square" />
        <div class="username-display">
          {{ profile?.nickname || profile?.username || "Operator" }}
        </div>
      </div>
    </template>

    <div class="profile-drop-menu">
      <div class="modal-gradient-bar"></div>
      <div class="profile-header">
        <div class="avatar-uploader" @click="onAvatarClick">
          <el-avatar :size="64" :src="avatarUrl" shape="square" />
          <div class="avatar-mask">
            <span class="avatar-mask-text">修改头像</span>
          </div>
        </div>
        <input ref="fileInputRef" type="file" accept="image/jpeg,image/png,image/webp" hidden @change="onFileChange" />
        <div class="header-info">
          <div class="nickname">{{ profile?.nickname || "未设置昵称" }}</div>
          <div class="username">@{{ profile?.username }}</div>
        </div>
      </div>

      <div class="profile-details">
        <div class="info-item">
          <el-icon class="item-icon"><Message /></el-icon>
          <span class="label">电子邮箱:</span>
          <span class="value">{{ profile?.email || "未绑定" }}</span>
        </div>
        <div class="info-item">
          <el-icon class="item-icon"><Phone /></el-icon>
          <span class="label">手机号码:</span>
          <span class="value">{{ profile?.phone || "未绑定" }}</span>
        </div>
        <div class="info-item">
          <el-icon class="item-icon"><User /></el-icon>
          <span class="label">用户性别:</span>
          <span class="value">{{ genderText }}</span>
        </div>
        <div class="info-item">
          <el-icon class="item-icon"><Operation /></el-icon>
          <span class="label">所属角色:</span>
          <div class="group-tags">
            <el-tag v-for="group in profile?.groups" :key="group" size="small" effect="plain" type="info">
              {{ group }}
            </el-tag>
            <span v-if="!profile?.groups?.length" class="value">无</span>
          </div>
        </div>
        <div class="info-item">
          <el-icon class="item-icon"><Key /></el-icon>
          <span class="label">权限节点:</span>
          <span class="value count-badge">{{ profile?.permissions?.length || 0 }} 个节点</span>
        </div>
        <div class="info-item">
          <el-icon class="item-icon"><Calendar /></el-icon>
          <span class="label">注册时间:</span>
          <span class="value">{{ profile?.createTime }}</span>
        </div>
        <div class="info-item">
          <el-icon class="item-icon"><Clock /></el-icon>
          <span class="label">最后登录:</span>
          <span class="value">{{ profile?.lastLoginTime }}</span>
        </div>
      </div>

      <div class="profile-actions">
        <div class="action-row">
          <el-button class="action-btn" type="primary" plain @click="onRefreshProfile" :loading="refreshing">
            <el-icon><Refresh /></el-icon>
            更新权限信息
          </el-button>
          <el-button class="action-btn" type="primary" plain @click="onChangePassword">
            <el-icon><Key /></el-icon>
            修改密码
          </el-button>
        </div>
        <el-button class="action-btn" type="danger" plain @click="onLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>

      <!-- 修改密码弹窗 -->
      <com-password-reset ref="changePasswordModalRef" />
      <!-- 头像裁剪弹窗 -->
      <com-modal-avatar-cropper ref="avatarCropperRef" @confirm="handleAvatarConfirm" />
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElAvatar, ElTag, ElIcon, ElButton, ElMessage, ElMessageBox, ElPopover } from "element-plus";
import { Message, Phone, User, Operation, Key, Calendar, Clock, SwitchButton, Refresh } from "@element-plus/icons-vue";
import type { GetCurrentUserProfile } from "@/soa/com-series/api/AuthApi.ts";
import AuthApi from "@/soa/com-series/api/AuthApi.ts";
import ComPasswordReset from "@/soa/com-series/components/ComPasswordReset.vue";
import ComModalAvatarCropper from "@/soa/com-series/components/ComModalAvatarCropper.vue";
import UserAuthService from "@/views/auth/service/UserAuthService.ts";
import { Result } from "@/commons/model/Result.ts";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";
import { useThrottleFn } from "@vueuse/core";

const authStore = UserAuthService.AuthStore();

const profile = ref<GetCurrentUserProfile | null>(null);
const changePasswordModalRef = ref();
const avatarCropperRef = ref<InstanceType<typeof ComModalAvatarCropper>>();
const fileInputRef = ref<HTMLInputElement>();
const refreshing = ref(false);
const avatarVersion = ref(0);

//多标签服务打包
const { refreshCounter } = ComTabService.useRouterTabService();

/**
 * 加载用户信息
 */
const loadUserProfile = async (): Promise<void> => {
  try {
    profile.value = await AuthApi.getUserProfile();
    avatarVersion.value++;
  } catch (error: any) {
    console.error("加载用户信息失败:", error);
  }
};

/**
 * 刷新用户信息
 */
const onRefreshProfile = useThrottleFn(async (): Promise<void> => {
  refreshing.value = true;
  try {
    profile.value = await AuthApi.getUserProfile({ forceUpdate: 1 });
    avatarVersion.value++;
    ElMessage.success("用户信息已刷新");

    //1秒后刷新标签
    setTimeout(() => {
      refreshCounter.value++;
    }, 1000);
  } catch (error: any) {
    ElMessage.error(error.message || "刷新用户信息失败");
  } finally {
    refreshing.value = false;
  }
}, 1000);

/**
 * 注销登录
 */
const onLogout = async (): Promise<void> => {
  await ElMessageBox.confirm("确定要注销登录吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  });

  const ret = await AuthApi.logout();

  if (Result.isSuccess(ret)) {
    ElMessage.success("注销成功");
    window.location.href = "/login";
    return;
  }

  //业务错误
  if (ret.code === 1) {
    // 会话不存在退出登录，需要把sessionIds删掉，然后回退到登录页
    UserAuthService.AuthStore().setSessionId(null);
    ElMessage.success("注销成功");
    window.location.href = "/login";
    return;
  }

  //网络错误
  ElMessage.error("注销失败: " + ret.message);
};

/**
 * 修改密码
 */
const onChangePassword = (): void => {
  if (changePasswordModalRef.value) {
    changePasswordModalRef.value.openModal();
  }
};

/**
 * 点击头像
 */
const onAvatarClick = (): void => {
  fileInputRef.value?.click();
};

/**
 * 文件变化
 */
const onFileChange = (e: Event): void => {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = "";

  if (!file) {
    return;
  }

  const allowedTypes = ["image/jpeg", "image/png", "image/webp"];
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error("仅支持 JPG、PNG、WEBP 格式的图片");
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error("图片大小不能超过 5MB");
    return;
  }

  const reader = new FileReader();
  reader.onload = (ev) => {
    const dataUrl = ev.target?.result as string;
    avatarCropperRef.value?.openModal(dataUrl);
  };
  reader.readAsDataURL(file);
};

/**
 * 确认头像
 */
const handleAvatarConfirm = async (file: File): Promise<void> => {
  try {
    await AuthApi.updateUserAvatar(file);
    profile.value = await AuthApi.getUserProfile({ forceUpdate: 1 });
    avatarVersion.value++;
    ElMessage.success("头像已更新");
  } catch (error: any) {
    ElMessage.error(error.message || "头像更新失败");
  }
};

/**
 * 头像URL
 */
const avatarUrl = computed(() => {
  let token = "";

  if (authStore.getSessionId) {
    token = authStore.getSessionId;
  }

  return `/api/profile/getUserAvatar?token=${token}&v=${avatarVersion.value}`;
});

const genderText = computed(() => {
  if (profile.value?.gender === 0) {
    return "男";
  }
  if (profile.value?.gender === 1) {
    return "女";
  }
  return "保密";
});

onMounted(() => {
  loadUserProfile();
});
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 0;
  transition: background-color 0.2s;
  height: 100%;
}

.user-info:hover {
  background-color: #f1f3f4;
}

.username-display {
  margin-left: 8px;
  font-size: 14px;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}

.profile-drop-menu {
  width: 320px;
  background-color: #fff;
  color: #333;
  position: relative;
}

.modal-gradient-bar {
  height: 4px;
  width: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.profile-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-uploader:hover .avatar-mask {
  opacity: 1;
}

.avatar-mask-text {
  color: #fff;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
  user-select: none;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-size: 18px;
  font-weight: 600;
  color: #000;
}

.username {
  font-size: 13px;
  color: #999;
}

.profile-details {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  line-height: 1.5;
  padding: 4px 8px;
  margin: 0 -8px;
  transition: background-color 0.2s;
}

.info-item:hover {
  background-color: #f5f7fa;
}

.item-icon {
  margin-right: 10px;
  color: #909399;
  font-size: 14px;
}

.label {
  color: #888;
  width: 75px;
  flex-shrink: 0;
}

.value {
  color: #333;
  word-break: break-all;
  flex: 1;
}

.count-badge {
  font-weight: 600;
  color: #009688;
}

.profile-actions {
  padding: 12px 20px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-row {
  display: flex;
  gap: 8px;
}

.action-row .action-btn {
  flex: 1;
}

.action-btn {
  width: 100%;
  border-radius: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-left: 0 !important; /* 覆盖 el-button 的默认左间距 */
}

.group-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

:deep(.el-tag) {
  border-radius: 0; /* 直角风格 */
}

:deep(.el-avatar) {
  border-radius: 0; /* 直角风格 */
}
</style>
