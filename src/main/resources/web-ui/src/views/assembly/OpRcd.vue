<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="输出方案名称">
            <el-input v-model="listForm.opName" placeholder="输入输出方案名称" clearable />
          </el-form-item>
          <el-form-item label="数据源名称">
            <el-input v-model="listForm.dsName" placeholder="输入数据源名称" clearable />
          </el-form-item>
          <el-form-item label="数据源表名">
            <el-input v-model="listForm.dsTableName" placeholder="输入数据源表名" clearable />
          </el-form-item>
          <el-form-item label="模型名称">
            <el-input v-model="listForm.modelName" placeholder="输入模型名称" clearable />
          </el-form-item>
          <el-form-item label="业务域">
            <el-input v-model="listForm.bizDomain" placeholder="输入业务域" clearable />
          </el-form-item>
          <el-form-item label="操作人账号">
            <el-input v-model="listForm.creatorUsername" placeholder="输入操作人账号" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" @click="loadList" :disabled="listLoading">查询</el-button>
          <el-button @click="resetList" :disabled="listLoading">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="id" label="主键ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="opName" label="输出方案名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="dsName" label="数据源名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="dsTableName" label="数据源表名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="modelName" label="模型名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="bizDomain" label="业务域" min-width="120" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" min-width="120" show-overflow-tooltip />
        <el-table-column prop="durationMs" label="耗时MS" min-width="120" show-overflow-tooltip />
        <el-table-column prop="creatorUsername" label="操作人账号" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="scope">
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>
  </StdListContainer>
</template>

<script setup lang="ts">
import { markRaw } from "vue";
import { Delete } from "@element-plus/icons-vue";
import OpRcdService from "@/views/assembly/service/OpRcdService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = OpRcdService.useOpRcdList();
</script>

<style scoped></style>