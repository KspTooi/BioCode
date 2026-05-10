<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="业务名称">
            <el-input v-model="listForm.name" placeholder="输入业务名称" clearable />
          </el-form-item>
          <el-form-item label="业务编码">
            <el-input v-model="listForm.code" placeholder="输入业务编码" clearable />
          </el-form-item>
          <el-form-item label="物理表名">
            <el-input v-model="listForm.tableName" placeholder="输入物理表名" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="请选择状态" clearable>
              <el-option label="正常" :value="0" />
              <el-option label="停用" :value="1" />
            </el-select>
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
      <el-button type="primary" @click="openModal('add', null)">创建业务表单</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="业务名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="业务编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="formType" label="表单类型" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <el-tag v-if="scope.row.formType === 0" type="success">手搓表单</el-tag>
            <el-tag v-else type="danger">动态表单</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tableName" label="物理表名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="success">正常</el-tag>
            <el-tag v-else type="danger">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="seq" label="排序" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
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

    <!-- 创建/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑业务表单' : '创建业务表单'"
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
        <el-form-item label="业务名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入业务名称" clearable :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item v-if="modalMode === 'add'" label="业务编码" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入业务编码" clearable :maxlength="16" show-word-limit />
        </el-form-item>
        <el-form-item v-else label="业务编码">
          <el-input v-model="modalForm.code" disabled />
        </el-form-item>
        <el-form-item label="表单类型" prop="formType">
          <el-select v-model="modalForm.formType" placeholder="请选择表单类型" clearable>
            <el-option label="手搓表单" :value="0" />
            <el-option label="动态表单" :value="1" :disabled="true" />
          </el-select>
        </el-form-item>
        <el-form-item label="表单图标" prop="icon">
          <StdIconPicker v-model="modalForm.icon" />
        </el-form-item>
        <el-form-item label="物理表名" prop="tableName">
          <el-input v-model="modalForm.tableName" placeholder="请输入物理表名" clearable :maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="PC端组件" prop="routePc">
          <el-input
            v-model="modalForm.routePc"
            placeholder="手动输入或使用组件选择器"
            clearable
            maxlength="512"
            show-word-limit
          >
            <template #append>
              <el-button @click="cpcmVisible = true">组件选择器</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="移动端路由名" prop="routeMobile">
          <el-input
            v-model="modalForm.routeMobile"
            placeholder="请输入移动端路由名"
            clearable
            :maxlength="512"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modalForm.status" placeholder="请选择状态" clearable>
            <el-radio label="正常" :value="0" />
            <el-radio label="停用" :value="1" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input-number v-model="modalForm.seq" placeholder="请输入排序" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">关闭</el-button>
          <el-button type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>

  <!-- 公共组件选择器 -->
  <ComPublicCompChooseModal
    v-model="modalForm.routePc"
    v-model:modal-visible="cpcmVisible"
    v-model:search-keyword="cpcmQuery"
  />
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import QfBizFormService from "@/views/qf/service/QfBizFormService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import StdIconPicker from "@/soa/std-series/StdIconPicker.vue";
import ComPublicCompChooseModal from "@/soa/com-series/ComPublicCompChooseModal.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = QfBizFormService.useQfBizFormList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  QfBizFormService.useQfBizFormModal(modalFormRef, loadList);

const cpcmVisible = ref(false);
const cpcmQuery = ref("");
</script>

<style scoped></style>
