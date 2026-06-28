<template>
  <div class="pool-details">
    <StdListAreaQuery show-persist-tip>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="索引状态">
            <el-select v-model="listForm.indexFilter" style="width: 160px">
              <el-option
                v-for="item in AttachIndexFilterOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="path" label="文件路径" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sha256" label="文件摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="totalSize" label="大小" min-width="140" show-overflow-tooltip align="right">
          <template #default="scope">
            {{ formatBytes(scope.row.receiveSize) }} of {{ formatBytes(scope.row.totalSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-tag
              size="small"
              :type="
                scope.row.status === 3
                  ? 'success'
                  : scope.row.status === 2
                    ? 'warning'
                    : scope.row.status === 1
                      ? 'danger'
                      : 'info'
              "
            >
              {{
                scope.row.status === 3
                  ? "已索引"
                  : scope.row.status === 2
                    ? "校验中"
                    : scope.row.status === 1
                      ? "区块不完整"
                      : "未索引"
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="verifyTime" label="校验时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
      </el-table>
    </StdListAreaTable>
  </div>
</template>

<script setup lang="ts">
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import { AttachIndexFilterOptions } from "@/views/core/api/AttachPoolApi";
import AttachPoolDetailsService from "@/views/core/service/AttachPoolDetailsService.ts";

const { listForm, listData, listTotal, listLoading, loadList, resetList, formatBytes } =
  AttachPoolDetailsService.useAttachPoolDetailsList();
</script>

<style scoped>
.pool-details {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
}
</style>
