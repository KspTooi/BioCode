<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="数据源名称">
            <el-input v-model="listForm.name" placeholder="输入数据源名称" clearable />
          </el-form-item>
          <el-form-item label="数据源编码">
            <el-input v-model="listForm.code" placeholder="输入数据源编码" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction class="flex gap-2">
      <el-button type="success" @click="openModal('add', null)">新增数据源</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column label="序号" min-width="60" align="center">
          <template #default="scope">
            {{ (listForm.pageNum - 1) * listForm.pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="数据源名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="数据源编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="kind" label="数据源类型" min-width="100" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.kind === 0" class="text-green-500">MYSQL</span>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="连接字符串" min-width="200" show-overflow-tooltip />
        <el-table-column prop="dbSchema" label="默认数据库" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="240">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="success" size="small" :icon="ConnectionIcon" @click="testConnection(scope.row)">
              测试数据源连接
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
            <el-button link type="primary" size="small" :icon="CopyIcon" @click="openModal('copy', scope.row)">
              复制
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #pagination>
        <el-pagination
          v-model:current-page="listForm.pageNum"
          v-model:page-size="listForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="listTotal"
          background
          @size-change="
            (val: number) => {
              listForm.pageSize = val;
              loadList();
            }
          "
          @current-change="
            (val: number) => {
              listForm.pageNum = val;
              loadList();
            }
          "
        />
      </template>
    </StdListAreaTable>

    <!-- 新增/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑数据源' : modalMode === 'copy' ? '复制数据源' : '新增数据源'"
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
        label-width="100px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入数据源名称" clearable maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="数据源编码" prop="code">
          <el-input
            v-model="modalForm.code"
            placeholder="请输入数据源编码"
            clearable
            maxlength="32"
            show-word-limit
            @blur="onCodeBlur"
          />
        </el-form-item>
        <el-form-item label="数据源类型" prop="kind" v-if="modalMode !== 'copy'">
          <el-select v-model="modalForm.kind" placeholder="请选择数据源类型" style="width: 100%">
            <el-option :value="0" label="MYSQL" />
          </el-select>
        </el-form-item>
        <el-form-item label="JDBC驱动" prop="drive" v-if="modalMode !== 'copy'">
          <el-input v-model="modalForm.drive" placeholder="请输入JDBC驱动" clearable maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="连接字符串" prop="url" v-if="modalMode !== 'copy'">
          <el-input v-model="modalForm.url" placeholder="请输入连接字符串" clearable maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="连接用户名" prop="username" v-if="modalMode !== 'copy'">
          <el-input
            v-model="modalForm.username"
            :placeholder="modalMode === 'edit' ? '留空不修改' : '请输入连接用户名'"
            clearable
            maxlength="320"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="连接密码" prop="password" v-if="modalMode !== 'copy'">
          <el-input
            v-model="modalForm.password"
            :placeholder="modalMode === 'edit' ? '留空不修改' : '请输入连接密码'"
            clearable
            maxlength="1280"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="默认数据库" prop="dbSchema" v-if="modalMode !== 'copy'">
          <el-input v-model="modalForm.dbSchema" placeholder="请输入默认数据库" clearable maxlength="80" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">取消</el-button>
          <el-button type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" || modalMode === "copy" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete, Connection, CopyDocument } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import DataSourceService from "@/views/assembly/service/DataSourceService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const ConnectionIcon = markRaw(Connection);
const CopyIcon = markRaw(CopyDocument);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList, testConnection } =
  DataSourceService.useDataSourceList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal, onCodeBlur } =
  DataSourceService.useDataSourceModal(modalFormRef, loadList);
</script>

<style scoped></style>
