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
          <el-button type="primary" @click="loadList" :disabled="listLoading">查询</el-button>
          <el-button @click="resetList" :disabled="listLoading">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction class="flex gap-2">
      <el-button type="success" @click="openModal('add', null)">新增AACP数据源</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="id" label="主键ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="数据源名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="数据源编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="kind" label="数据源类型 0:MYSQL" min-width="120" show-overflow-tooltip />
        <el-table-column prop="url" label="连接字符串" min-width="120" show-overflow-tooltip />
        <el-table-column prop="defaultDb" label="默认数据库" min-width="120" show-overflow-tooltip />
        <el-table-column prop="queryMaxRows" label="最大查询行数" min-width="120" show-overflow-tooltip />
        <el-table-column prop="executeBatch" label="是否支持批处理 0:不支持 1:支持" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)" :icon="EditIcon">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon"> 删除 </el-button>
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
          background
        />
      </template>
    </StdListAreaTable>

    <!-- 新增/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑AACP数据源' : '新增AACP数据源'"
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
          <el-input v-model="modalForm.name" placeholder="请输入数据源名称" clearable :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="数据源编码" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入数据源编码" clearable :maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="数据源类型 0:MYSQL" prop="kind">
          <el-input v-model.number="modalForm.kind" placeholder="请输入数据源类型 0:MYSQL" clearable />
        </el-form-item>
        <el-form-item label="JDBC驱动" prop="drive">
          <el-input v-model="modalForm.drive" placeholder="请输入JDBC驱动" clearable :maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="连接字符串" prop="url">
          <el-input v-model="modalForm.url" placeholder="请输入连接字符串" clearable />
        </el-form-item>
        <el-form-item label="连接用户名" prop="username">
          <el-input v-model="modalForm.username" placeholder="请输入连接用户名" clearable :maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="连接密码" prop="password">
          <el-input v-model="modalForm.password" placeholder="请输入连接密码" clearable :maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="默认数据库" prop="defaultDb">
          <el-input v-model="modalForm.defaultDb" placeholder="请输入默认数据库" clearable :maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="最大查询行数" prop="queryMaxRows">
          <el-input v-model.number="modalForm.queryMaxRows" placeholder="请输入最大查询行数" clearable />
        </el-form-item>
        <el-form-item label="是否支持批处理 0:不支持 1:支持" prop="executeBatch">
          <el-input v-model.number="modalForm.executeBatch" placeholder="请输入是否支持批处理 0:不支持 1:支持" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">取消</el-button>
          <el-button type="primary" @click="submitModal" :loading="modalLoading">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import AacpDatasourceService from "@/views/aacpDatasource/service/AacpDatasourceService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = AacpDatasourceService.useAacpDatasourceList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  AacpDatasourceService.useAacpDatasourceModal(modalFormRef, loadList);
</script>

<style scoped></style>
