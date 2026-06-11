<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form inline class="flex justify-between">
        <div>
          <el-form-item label="枢纽编码">
            <el-input v-model="serverCode" placeholder="请输入枢纽编码" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <StdListAreaTable v-model:list-form="listForm">
      <el-table v-loading="listLoading" :data="listData" border stripe height="100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="会话ID" prop="sessionId" min-width="200" show-overflow-tooltip />
        <el-table-column label="枢纽名称" prop="serverName" width="140" />
        <el-table-column label="枢纽编码" prop="serverCode" width="120" />
        <el-table-column label="连接时间" prop="connectTime" width="180" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">初始化</el-tag>
            <el-tag v-if="scope.row.status === 1" type="success">活跃</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求次数" prop="inboundCount" width="100" align="center" />
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="scope">
            <el-button link type="danger" size="small" :icon="CloseIcon" @click="removeList(scope.row)">
              关闭会话
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>
  </StdListContainer>
</template>

<script setup lang="ts">
import { markRaw } from "vue";
import { CloseBold } from "@element-plus/icons-vue";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import AacpSessionService from "@/views/aacp/service/AacpSessionService.ts";

const CloseIcon = markRaw(CloseBold);

const { listForm, serverCode, listData, listLoading, loadList, resetList, removeList } =
  AacpSessionService.useOnlineSessionList();
</script>

<style scoped></style>
