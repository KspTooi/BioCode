<template>
  <StdListLayout>
    <!-- 查询条件区域 -->
    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="4" :offset="1">
            <el-form-item label="模板名称">
              <el-input v-model="listForm.name" placeholder="输入模板名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4" :offset="1">
            <el-form-item label="唯一编码">
              <el-input v-model="listForm.code" placeholder="输入唯一编码" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4" :offset="1">
            <el-form-item label="状态">
              <el-select v-model="listForm.status" placeholder="选择状态" clearable class="w-full">
                <el-option label="启用" :value="0" />
                <el-option label="禁用" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="4">
            <el-form-item>
              <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
              <el-button :disabled="listLoading" @click="resetList">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="uiState.isAdvancedSearch">
          <el-row>
            <el-col :span="4" :offset="1">
              <el-form-item label="模板内容">
                <el-input v-model="listForm.content" placeholder="输入模板内容" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="4" :offset="1">
              <el-form-item label="备注">
                <el-input v-model="listForm.remark" placeholder="输入备注" clearable />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>
    </template>

    <!-- 操作按钮区域 -->
    <template #actions>
      <el-button type="primary" @click="openModal('add', null)">创建通知模板</el-button>
    </template>

    <!-- 列表表格区域 -->
    <template #table>
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="模板名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="code" label="唯一编码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="150" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

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

    <template #modal>
      <!-- 创建/编辑模态框 -->
      <el-dialog
        v-model="modalVisible"
        :title="modalMode === 'edit' ? '编辑通知模板' : '创建通知模板'"
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
          <el-form-item label="模板名称" prop="name">
            <el-input v-model="modalForm.name" placeholder="请输入模板名称" maxlength="32" show-word-limit clearable />
          </el-form-item>
          <el-form-item label="唯一编码" prop="code">
            <el-input v-model="modalForm.code" placeholder="请输入业务调用唯一编码" maxlength="32" show-word-limit clearable />
          </el-form-item>
          <el-form-item label="模板内容" prop="content">
            <el-input
              v-model="modalForm.content"
              type="textarea"
              :autosize="{ minRows: 4 }"
              maxlength="10000"
              show-word-limit
              placeholder="请输入模板内容 (含占位符)"
              clearable
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="modalForm.status" placeholder="请选择状态" class="w-full">
              <el-radio label="启用" :value="0" />
              <el-radio label="禁用" :value="1" />
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="modalForm.remark"
              type="textarea"
              :autosize="{ minRows: 3 }"
              maxlength="1000"
              show-word-limit
              placeholder="请输入备注"
              clearable
            />
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
    </template>
  </StdListLayout>
</template>

<script setup lang="ts">
import { ref, markRaw, reactive } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import NoticeTemplateService from "@/views/core/service/NoticeTemplateService.ts";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// UI状态
const uiState = reactive({
  isAdvancedSearch: false,
});

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } =
  NoticeTemplateService.useNoticeTemplateList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  NoticeTemplateService.useNoticeTemplateModal(modalFormRef, loadList);
</script>

<style scoped></style>
