<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="数据源名称">
            <el-input v-model="listForm.name" placeholder="请输入数据源名称" clearable />
          </el-form-item>
          <el-form-item label="数据源编码">
            <el-input v-model="listForm.code" placeholder="请输入数据源编码" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <StdListAreaAction>
      <el-button type="success" @click="openModal('add', null)">新增数据源</el-button>
    </StdListAreaAction>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="数据源名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="code" label="数据源编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="kind" label="数据源类型" min-width="100" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.kind === 0" class="text-green-500">MYSQL</span>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="连接字符串" min-width="200" show-overflow-tooltip />
        <el-table-column prop="defaultDb" label="默认数据库" min-width="120" show-overflow-tooltip />
        <el-table-column prop="queryMaxRows" label="最大查询行数" min-width="120" show-overflow-tooltip />
        <el-table-column prop="executeBatch" label="批处理" min-width="120" show-overflow-tooltip align="center">
          <template #default="scope">
            <span v-if="scope.row.executeBatch === 1" class="text-green-500">支持</span>
            <span v-else class="text-gray-400">不支持</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="260">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="success" size="small" :icon="ConnectionIcon" @click="testConnection(scope.row)">
              测试数据源连接
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑数据源' : '新增数据源'"
      width="600px"
      :close-on-click-modal="false"
      @close="
        resetModal();
        loadList();
      "
    >
      <el-form
        v-if="modalVisible"
        ref="modalFormRef"
        :model="modalForm"
        :rules="modalRules"
        label-width="120px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入数据源名称" clearable :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="数据源编码" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入数据源编码" clearable :maxlength="32" show-word-limit @blur="onCodeBlur" />
        </el-form-item>
        <el-form-item label="数据源类型" prop="kind">
          <el-select v-model="modalForm.kind" placeholder="请选择数据源类型" style="width: 100%">
            <el-option :value="0" label="MYSQL" />
          </el-select>
        </el-form-item>
        <el-form-item label="JDBC驱动" prop="drive">
          <el-input v-model="modalForm.drive" placeholder="请输入JDBC驱动" clearable :maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="连接字符串" prop="url">
          <el-input v-model="modalForm.url" placeholder="请输入连接字符串" clearable />
        </el-form-item>
        <el-form-item label="连接用户名" prop="username">
          <el-input
            v-model="modalForm.username"
            :placeholder="modalMode === 'edit' ? '留空不修改' : '请输入连接用户名'"
            clearable
            :maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="连接密码" prop="password">
          <el-input
            v-model="modalForm.password"
            :placeholder="modalMode === 'edit' ? '留空不修改' : '请输入连接密码'"
            type="password"
            clearable
            :maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="默认数据库" prop="defaultDb">
          <el-input v-model="modalForm.defaultDb" placeholder="请输入默认数据库" clearable :maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="最大查询行数" prop="queryMaxRows">
          <el-input-number v-model="modalForm.queryMaxRows" placeholder="最大查询行数" :min="0" />
        </el-form-item>
        <el-form-item label="批处理" prop="executeBatch">
          <el-radio-group v-model="modalForm.executeBatch">
            <el-radio :value="1">支持</el-radio>
            <el-radio :value="0">不支持</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">取消</el-button>
          <el-button type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete, Connection } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import AacpDatasourceService from "@/views/aacp/service/AacpDatasourceService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const ConnectionIcon = markRaw(Connection);

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList, testConnection } =
  AacpDatasourceService.useAacpDatasourceList();

const modalFormRef = ref<FormInstance>();

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal, onCodeBlur } =
  AacpDatasourceService.useAacpDatasourceModal(modalFormRef, loadList);
</script>

<style scoped></style>
