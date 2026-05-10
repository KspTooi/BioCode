<template>
  <el-dropdown trigger="click" class="notice-dropdown" @visible-change="onVisibleChange">
    <div class="flex items-center justify-center h-full w-full cursor-pointer transition-colors hover:bg-black/5">
      <el-badge :value="processedCount" :hidden="count === 0" :max="99">
        <el-icon title="通知中心" class="control-btn">
          <Bell />
        </el-icon>
      </el-badge>
    </div>
    <template #dropdown>
      <div class="w-80 rounded overflow-hidden bg-white">
        <div class="flex justify-between items-center px-4 py-3 border-b border-gray-200 font-bold text-sm">
          <span>通知中心 (未读数:{{ count }})</span>
          <el-button v-if="listData.length > 0" link type="primary" @click="readAll">全部已读</el-button>
        </div>

        <el-scrollbar height="300px">
          <ul
            v-if="listData.length > 0 || loading"
            v-infinite-scroll="loadMore"
            :infinite-scroll-disabled="disabled"
            class="list-none p-0 m-0"
          >
            <li
              v-for="item in listData"
              :key="item.id"
              class="group flex px-4 py-3 cursor-pointer transition-colors border-b border-gray-100 hover:bg-gray-50"
              @click="onRead(item)"
            >
              <div class="mr-3 flex items-center">
                <el-avatar :size="32" :icon="getIcon(item.kind)" :class="getIconClass(item.kind)" />
              </div>
              <div class="flex-1 overflow-hidden">
                <div class="text-sm text-gray-900 mb-1 truncate" :title="item.title">{{ item.title }}</div>
                <div class="text-xs text-gray-500">{{ item.createTime }}</div>
              </div>
              <div class="hidden group-hover:flex items-center ml-2">
                <el-button link type="danger" :icon="Delete" class="!p-1" @click.stop="remove(item.id)"></el-button>
              </div>
            </li>
            <li v-if="loading" class="text-center py-2.5 text-xs text-gray-500">加载中...</li>
            <li v-if="noMore && listData.length > 0" class="text-center py-2.5 text-xs text-gray-500">没有更多了</li>
          </ul>

          <div v-if="listData.length === 0 && !loading" class="py-5">
            <el-empty description="暂无新通知" :image-size="60" />
          </div>
        </el-scrollbar>

        <!-- 底部查看全部按钮 -->
        <div class="border-t border-gray-200 px-4 py-3 text-center">
          <el-button link type="primary" class="w-full" @click="onViewAll">查看全部消息</el-button>
        </div>
      </div>
    </template>
  </el-dropdown>

  <!-- 通知详情模态框 -->
  <el-dialog v-model="modalVisible" title="通知详情" width="600px" :close-on-click-modal="false" @close="closeModal">
    <div v-loading="modalLoading" class="min-h-[200px] select-text">
      <template v-if="detailsData">
        <!-- 标题和标签 -->
        <div class="flex items-center gap-3 mb-5 pb-4 border-b border-gray-200">
          <h3 class="flex-1 m-0 text-lg font-bold text-gray-900">{{ detailsData.title }}</h3>
          <el-tag v-if="detailsData.kind === 0" type="primary">公告</el-tag>
          <el-tag v-if="detailsData.kind === 1" type="warning">业务提醒</el-tag>
          <el-tag v-if="detailsData.kind === 2" type="success">私信</el-tag>
        </div>

        <!-- 元信息 -->
        <div class="flex flex-col gap-2 mb-5">
          <div class="flex text-sm">
            <span class="text-gray-500 min-w-[80px]">发送人：</span>
            <span class="text-gray-900">{{ detailsData.senderName || "系统" }}</span>
          </div>
          <div class="flex text-sm">
            <span class="text-gray-500 min-w-[80px]">发送时间：</span>
            <span class="text-gray-900">{{ detailsData.createTime }}</span>
          </div>
          <div v-if="detailsData.category" class="flex text-sm">
            <span class="text-gray-500 min-w-[80px]">分类：</span>
            <span class="text-gray-900">{{ detailsData.category }}</span>
          </div>
        </div>

        <!-- 通知内容 -->
        <div>
          <div class="text-sm font-bold text-gray-900 mb-3">通知内容：</div>
          <div class="text-sm leading-relaxed text-gray-700 whitespace-pre-wrap break-words" v-html="detailsData.content"></div>
        </div>
      </template>
    </div>

    <template #footer>
      <el-button @click="closeModal">关闭</el-button>
      <el-button v-if="detailsData?.forward" type="primary" @click="onForward"> 前往查看 </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, type Component } from "vue";
import { Bell, Message, Warning, Promotion, Delete } from "@element-plus/icons-vue";
import UserNoticeService from "@/views/core/service/NoticeRcdService.ts";
import type { GetUserNoticeRcdListVo } from "@/views/core/api/NoticeRcdApi";
import { ElMessage } from "element-plus";
import ComTabService from "@/soa/com-series/service/ComTabService.ts";

const { openTab } = ComTabService.useTabService();

// 使用 Service 中的逻辑
const { count, processedCount, loadCount } = UserNoticeService.useNoticeRcdCount();
const {
  listData,
  listLoading: loading,
  noMore,
  disabled,
  loadMore,
  resetList,
  remove,
  readAll,
} = UserNoticeService.useNoticeRcdRollingList(loadCount);
const { modalVisible, modalLoading, detailsData, openModal, closeModal } = UserNoticeService.useNoticeRcdModal();

/**
 * 下拉框显示状态变化
 */
const onVisibleChange = (visible: boolean): void => {
  if (!visible) {
    return;
  }
  // 重置并加载第一页
  resetList();
  loadMore();
};

/**
 * 获取通知图标
 */
const getIcon = (kind: number): Component => {
  if (kind === 0) {
    return Promotion;
  }
  if (kind === 1) {
    return Warning;
  }
  if (kind === 2) {
    return Message;
  }
  return Bell;
};

/**
 * 获取图标样式类
 */
const getIconClass = (kind: number): string => {
  if (kind === 0) {
    return "kind-notice";
  }
  if (kind === 1) {
    return "kind-alert";
  }
  if (kind === 2) {
    return "kind-message";
  }
  return "";
};

/**
 * 点击通知项
 */
const onRead = (item: GetUserNoticeRcdListVo): void => {
  // 打开详情模态框
  openModal(item.id);
};

/**
 * 跳转到关联页面
 */
const onForward = (): void => {
  if (!detailsData.value?.forward) {
    return;
  }
  // router.push(detailsData.value.forward);
  closeModal();
  ElMessage.info("功能开发中");
};

/**
 * 查看全部消息
 */
const onViewAll = (): void => {
  openTab({
    id: "notice-rcd",
    icon: null,
    title: "个人消息中心",
    path: "/core/notice-rcd",
  });
};

onMounted(() => {
  loadCount();
});
</script>

<style scoped>
/* 让 dropdown 占满容器高度 */
.notice-dropdown {
  display: flex;
  align-items: center;
  height: 100%;
}

/* Element Plus Avatar 颜色类 */
.kind-notice {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
}

.kind-alert {
  background-color: var(--el-color-warning-light-8);
  color: var(--el-color-warning);
}

.kind-message {
  background-color: var(--el-color-success-light-8);
  color: var(--el-color-success);
}
.control-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s ease;
  height: 100%;
  color: var(--com-tab-text);
}
</style>
