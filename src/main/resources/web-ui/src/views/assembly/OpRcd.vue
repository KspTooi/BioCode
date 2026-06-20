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
        <el-form-item class="flex-shrink-0">
          <el-button type="primary" @click="loadList" :disabled="listLoading">查询</el-button>
          <el-button @click="resetList" :disabled="listLoading">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="opName" label="输出方案名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="dsName" label="数据源名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="dsTableName" label="数据源表名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="modelName" label="模型名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="bizDomain" label="业务域" min-width="120" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" min-width="120" show-overflow-tooltip />
        <el-table-column prop="durationMs" label="耗时MS" min-width="120" show-overflow-tooltip>
          <template #default="scope"> {{ scope.row.durationMs }} ms</template>
        </el-table-column>
        <el-table-column prop="creatorUsername" label="操作人账号" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openViewModal(scope.row)" :icon="ViewIcon">查看</el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 查看详情模态框 -->
    <el-dialog
      v-model="modalVisible"
      title="查看输出方案执行记录详情"
      width="800px"
      :close-on-click-modal="false"
      @close="resetModal()"
    >
      <el-form v-if="modalVisible" ref="modalFormRef" :model="modalForm" label-width="140px" disabled>
        <el-form-item label="主键ID">
          <el-input v-model="modalForm.id" />
        </el-form-item>
        <el-form-item label="输出方案名称">
          <el-input v-model="modalForm.opName" />
        </el-form-item>
        <el-form-item label="数据源名称">
          <el-input v-model="modalForm.dsName" />
        </el-form-item>
        <el-form-item label="数据源表名">
          <el-input v-model="modalForm.dsTableName" />
        </el-form-item>
        <el-form-item label="数据源连接字符串">
          <el-input v-model="modalForm.dsUrl" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="输入SCM仓库地址">
          <el-input v-model="modalForm.scmInputUrl" />
        </el-form-item>
        <el-form-item label="输出SCM仓库地址">
          <el-input v-model="modalForm.scmOutputUrl" />
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="modalForm.modelName" />
        </el-form-item>
        <el-form-item label="模型备注">
          <el-input v-model="modalForm.modelRemark" />
        </el-form-item>
        <el-form-item label="业务域">
          <el-input v-model="modalForm.bizDomain" />
        </el-form-item>
        <el-form-item label="QBE参数">
          <el-input v-model="modalForm.qbeParams" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-input v-model="modalForm.startTime" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-input v-model="modalForm.endTime" />
        </el-form-item>
        <el-form-item label="耗时MS">
          <el-input v-model="modalForm.durationMs" />
        </el-form-item>
        <el-form-item label="操作人账号">
          <el-input v-model="modalForm.creatorUsername" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modalVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { View, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import OpRcdService from "@/views/assembly/service/OpRcdService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = OpRcdService.useOpRcdList();

const { modalVisible, modalForm, openViewModal, resetModal } = OpRcdService.useOpRcdViewModal(modalFormRef);
</script>

<style scoped></style>
