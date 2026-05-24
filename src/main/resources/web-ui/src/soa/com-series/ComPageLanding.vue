<template>
  <div
    class="h-full w-full overflow-hidden bg-gradient-to-br from-slate-50 to-teal-50/30 flex flex-col items-center justify-center p-6 relative"
  >
    <!-- 背景装饰 -->
    <div class="absolute top-[-10%] right-[-10%] w-[40%] h-[40%] bg-teal-200/20 blur-[120px] rounded-full"></div>
    <div class="absolute bottom-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-200/20 blur-[120px] rounded-full"></div>

    <div class="max-w-5xl w-full space-y-12 z-10 animate-page-in">
      <!-- 欢迎区域 -->
      <div class="text-center space-y-6">
        <div
          class="inline-flex items-center gap-2 px-4 py-1.5 mt-8 bg-teal-50 border border-teal-100/50 text-teal-700 text-sm font-medium mb-2"
        >
          <el-icon><StarFilled /></el-icon>
          <span>欢迎回来，{{ displayName }}</span>
        </div>
        <h1 class="text-5xl font-extrabold text-slate-900 tracking-tight leading-tight">
          {{ greeting }}
        </h1>
        <div class="flex flex-col items-center gap-2">
          <p class="text-xl text-slate-500 font-medium">
            {{ userDept }}
          </p>
          <div v-if="userLastLogin" class="text-sm text-slate-400 flex items-center gap-2">
            <el-icon><Clock /></el-icon>
            <span>上次登录：{{ userLastLogin }}</span>
          </div>
        </div>
      </div>

      <!-- 核心操作区 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- 通知中心 -->
        <div class="glass-card p-8 group cursor-pointer" @click="openMessageCenter">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center gap-4">
              <div class="icon-box bg-teal-50 text-teal-600 group-hover:bg-teal-600 group-hover:text-white">
                <el-icon><Bell /></el-icon>
              </div>
              <div>
                <h3 class="font-bold text-xl text-slate-800">通知中心</h3>
                <p class="text-xs text-slate-400 mt-0.5">查看最新的系统消息与提醒</p>
              </div>
            </div>
            <el-badge :value="processedNoticeCount" :hidden="noticeCount === 0" :max="99" class="custom-badge" />
          </div>

          <div v-loading="noticeLoading" class="space-y-4">
            <div v-if="noticeList.length === 0" class="text-sm text-slate-400 py-4 flex flex-col items-center gap-2">
              <el-icon class="text-2xl opacity-20"><ChatDotRound /></el-icon>
              <span>暂无新通知</span>
            </div>
            <div v-for="item in noticeList.slice(0, 3)" :key="item.id" class="notice-item">
              <div class="w-1.5 h-1.5 bg-teal-400 shrink-0"></div>
              <span class="text-slate-600 truncate flex-1 font-medium">{{ item.title }}</span>
              <span class="text-slate-400 shrink-0 text-xs font-mono">{{ item.createTime.split(" ")[0] }}</span>
            </div>
          </div>
        </div>

        <!-- 系统状态 -->
        <div class="glass-card p-8 group cursor-pointer" @click="openAppStatus">
          <div class="flex items-center gap-4 mb-6">
            <div class="icon-box bg-blue-50 text-blue-600 group-hover:bg-blue-600 group-hover:text-white">
              <el-icon><Monitor /></el-icon>
            </div>
            <div>
              <h3 class="font-bold text-xl text-slate-800">系统探针</h3>
              <p class="text-xs text-slate-400 mt-0.5">监控系统运行状态与环境信息</p>
            </div>
          </div>

          <div class="space-y-5">
            <div class="flex items-center justify-between">
              <span class="text-sm text-slate-600 font-medium">运行环境</span>
              <el-tag type="success" effect="light" size="small">正常运行中</el-tag>
            </div>
            <div class="p-3 bg-slate-50 border border-slate-100 group-hover:border-blue-100 transition-colors">
              <div class="text-[10px] uppercase tracking-wider text-slate-400 mb-1 font-bold">API Endpoint</div>
              <div class="text-xs text-slate-500 font-mono truncate">{{ apiUrl }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部装饰 -->
      <div class="pt-12 text-center">
        <div
          class="inline-flex items-center gap-3 px-6 py-2.5 bg-white/60 backdrop-blur-sm border border-white/80 text-xs font-medium text-slate-400"
        >
          <span class="tracking-widest uppercase">Bio Code</span>
          <span class="w-1 h-1 bg-slate-300"></span>
          <span class="font-mono">{{ new Date().getFullYear() }}</span>
        </div>
      </div>
    </div>

    <!-- 通知详情模态框 -->
    <el-dialog v-model="modalVisible" title="通知详情" width="600px" :close-on-click-modal="false" class="custom-dialog">
      <div v-loading="modalLoading" class="min-h-[200px] select-text">
        <template v-if="detailsData">
          <div class="flex items-center gap-3 mb-6 pb-4 border-b border-slate-100">
            <h3 class="flex-1 m-0 text-xl font-bold text-slate-900">{{ detailsData.title }}</h3>
            <el-tag v-if="detailsData.kind === 0" type="primary" effect="plain">公告</el-tag>
            <el-tag v-if="detailsData.kind === 1" type="warning" effect="plain">业务提醒</el-tag>
            <el-tag v-if="detailsData.kind === 2" type="success" effect="plain">私信</el-tag>
          </div>
          <div class="grid grid-cols-2 gap-4 mb-6">
            <div class="bg-slate-50 p-3">
              <div class="text-xs text-slate-400 mb-1">发送人</div>
              <div class="text-sm font-bold text-slate-700">{{ detailsData.senderName || "系统" }}</div>
            </div>
            <div class="bg-slate-50 p-3">
              <div class="text-xs text-slate-400 mb-1">发送时间</div>
              <div class="text-sm font-bold text-slate-700">{{ detailsData.createTime }}</div>
            </div>
          </div>
          <div class="content-box">
            <div class="text-sm font-bold text-slate-900 mb-3 flex items-center gap-2">
              <div class="w-1 h-4 bg-teal-500"></div>
              通知内容
            </div>
            <div
              class="text-sm leading-relaxed text-slate-600 whitespace-pre-wrap break-words p-4 bg-slate-50/50"
              v-html="detailsData.content"
            ></div>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="closeModal">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { Bell, Monitor, StarFilled, Clock, ChatDotRound } from "@element-plus/icons-vue";
import Http from "@/commons/Http.ts";
import UserAuthService from "@/views/auth/service/UserAuthService.ts";
import NoticeRcdService from "@/views/core/service/NoticeRcdService.ts";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";

const apiUrl = Http.getApiUrl();

const authStore = UserAuthService.AuthStore();
const userInfo = computed(() => authStore.getUserInfo);

const { openTab } = ComTabService.useTabService();

const displayName = computed(() => {
  if (userInfo.value?.nickname) {
    return userInfo.value.nickname;
  }
  if (userInfo.value?.username) {
    return userInfo.value.username;
  }
  return "用户";
});

const userDept = computed(() => {
  const root = userInfo.value?.rootName || "";
  const dept = userInfo.value?.deptName || "";
  if (root && dept) {
    return `${root} / ${dept}`;
  }
  if (root) {
    return root;
  }
  if (dept) {
    return dept;
  }
  return "";
});

const userLastLogin = computed(() => {
  if (!userInfo.value?.lastLoginTime) {
    return "";
  }
  return userInfo.value.lastLoginTime;
});

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 11) {
    return "早上好";
  }
  if (hour < 18) {
    return "下午好";
  }
  return "晚上好";
});

/**
 * 通知未读数
 */
const { count: noticeCount, processedCount: processedNoticeCount, loadCount } = NoticeRcdService.useNoticeRcdCount();

/**
 * 通知列表
 */
const { listData: noticeList, listLoading: noticeLoading, loadList: loadNoticeList } = NoticeRcdService.useNoticeRcdList();

/**
 * 通知详情模态框
 */
const { modalVisible, modalLoading, detailsData, closeModal } = NoticeRcdService.useNoticeRcdModal();

const openMessageCenter = (): void => {
  openTab({
    id: "notice-rcd",
    icon: null,
    title: "个人消息中心",
    path: "/core/notice-rcd",
    kind: "normal",
  });
};

const openAppStatus = (): void => {
  openTab({
    id: "app-status",
    icon: null,
    title: "系统探针",
    path: "/core/app-status",
    kind: "normal",
  });
};

onMounted(() => {
  loadCount();
  loadNoticeList();
});
</script>

<style scoped>
.animate-page-in {
  animation: pageIn 1s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes pageIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 24px -1px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
}

.glass-card:hover {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 32px -12px rgba(0, 150, 136, 0.15);
  border-color: rgba(0, 150, 136, 0.3);
}

.icon-box {
  padding: 12px;
  font-size: 24px;
  transition: all 0.3s ease;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.02);
}

.animate-pulse-slow {
  animation: pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

:deep(.custom-badge .el-badge__content) {
  background-color: #009688;
  border: none;
  border-radius: 0;
  box-shadow: 0 2px 8px rgba(0, 150, 136, 0.3);
}

:deep(.custom-dialog) {
  border-radius: 0;
  overflow: hidden;
}
</style>
