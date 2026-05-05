<template>
  <el-dialog
    :model-value="props.visible"
    :title="'管理菜单包 - ' + (props.data?.name ?? '')"
    width="720px"
    :close-on-click-modal="false"
    destroy-on-close
    @close="onClose"
  >
    <el-form :model="svc.searchForm.value" inline class="search-form">
      <el-form-item label="菜单包名">
        <el-input v-model="svc.searchForm.value.name" placeholder="输入菜单包名" clearable />
      </el-form-item>
      <el-form-item label="菜单包编码">
        <el-input v-model="svc.searchForm.value.code" placeholder="输入菜单包编码" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="svc.loading.value" @click="svc.search">查询</el-button>
        <el-button :disabled="svc.loading.value" @click="svc.resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-wrap">
      <el-table
        ref="
          (el: any) => {
            svc.tableRef.value = el;
          }
        "
        :data="svc.packList.value"
        stripe
        border
        height="100%"
        v-loading="svc.loading.value"
        @selection-change="svc.onSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="菜单包名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="code" label="菜单包编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="mCount" label="包含菜单数" min-width="100" align="center" />
      </el-table>
    </div>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="svc.pageNum.value"
        v-model:page-size="svc.pageSize.value"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="svc.total.value"
        background
        small
        @size-change="svc.loadPackList"
        @current-change="svc.loadPackList"
      />
    </div>

    <template #footer>
      <el-button @click="onClose">关闭</el-button>
      <el-button type="primary" :loading="svc.loading.value" @click="svc.submitModal">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import RootRpModalService, { type RootRpModalProps } from "@/views/core/components/service/RootRpModalService.ts";

const props = defineProps<RootRpModalProps>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "success"): void;
}>();

const svc = RootRpModalService.useRootRpModal(props, emit);

const onClose = (): void => {
  emit("close");
};
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}

.table-wrap {
  height: 400px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  overflow: hidden;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>
