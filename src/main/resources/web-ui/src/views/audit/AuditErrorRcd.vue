<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="4" :offset="1">
            <el-form-item label="错误代码">
              <el-input v-model="listForm.errorCode" placeholder="输入错误代码" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4" :offset="1">
            <el-form-item label="请求地址">
              <el-input v-model="listForm.requestUri" placeholder="输入请求地址" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4" :offset="1">
            <el-form-item label="异常类型">
              <el-input v-model="listForm.errorType" placeholder="输入异常类型" clearable />
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
              <el-form-item label="操作人ID">
                <el-input v-model="listForm.userId" placeholder="输入操作人ID" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="4" :offset="1">
              <el-form-item label="异常简述">
                <el-input v-model="listForm.errorMessage" placeholder="输入异常简述" clearable />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>
    </StdListAreaQuery>

    <StdListAreaAction>
      <el-button
        type="danger"
        :disabled="listSelected.length === 0"
        :loading="listLoading"
        @click="() => removeListBatch(listSelected)"
      >
        批量删除
      </el-button>
    </StdListAreaAction>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table
        v-loading="listLoading"
        :data="listData"
        stripe
        border
        height="100%"
        @selection-change="(val: GetAuditErrorRcdListVo[]) => (listSelected = val)"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="errorCode" label="错误代码" min-width="50" show-overflow-tooltip />
        <el-table-column prop="requestUri" label="请求地址" min-width="120" show-overflow-tooltip />
        <el-table-column prop="errorType" label="异常类型" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="发生时间" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="ViewIcon" @click="openModal('view', scope.row)">
              查看
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 查看/新增/编辑模态框 -->
    <el-dialog v-model="modalVisible" :title="modalDialogTitle" width="800px" :close-on-click-modal="false" @close="loadList()">
      <el-scrollbar max-height="65vh">
        <el-form v-if="modalVisible" ref="modalFormRef" :model="modalForm" label-width="120px" :validate-on-rule-change="false">
          <el-row>
            <el-col :span="12">
              <el-form-item label="请求地址" prop="requestUri">
                <el-input
                  v-model="modalForm.requestUri"
                  :readonly="modalMode === 'view'"
                  placeholder="请输入请求地址"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="错误代码" prop="errorCode">
                <el-input
                  v-model="modalForm.errorCode"
                  :readonly="modalMode === 'view'"
                  placeholder="请输入错误代码"
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="12">
              <el-form-item label="操作人ID" prop="userId">
                <el-input v-model="modalForm.userId" :readonly="modalMode === 'view'" placeholder="请输入操作人ID" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="操作人用户名" prop="userName">
                <el-input
                  v-model="modalForm.userName"
                  :readonly="modalMode === 'view'"
                  placeholder="请输入操作人用户名"
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="12">
              <el-form-item label="异常类型" prop="errorType">
                <el-input
                  v-model="modalForm.errorType"
                  :readonly="modalMode === 'view'"
                  placeholder="请输入异常类型"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item v-if="modalMode === 'view'" label="发生时间" prop="createTime">
                <el-input v-model="modalForm.createTime" readonly placeholder="发生时间" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="异常简述" prop="errorMessage">
            <el-input
              v-model="modalForm.errorMessage"
              type="textarea"
              :autosize="{ minRows: 3 }"
              :readonly="modalMode === 'view'"
              placeholder="请输入异常简述"
            />
          </el-form-item>

          <el-form-item label="完整堆栈信息" prop="errorStackTrace">
            <el-input
              v-model="modalForm.errorStackTrace"
              type="textarea"
              :autosize="{ minRows: 12, maxRows: 20 }"
              :readonly="modalMode === 'view'"
              placeholder="请输入完整堆栈信息"
            />
          </el-form-item>
        </el-form>
      </el-scrollbar>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw, reactive, computed } from "vue";
import { View, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import AuditErrorRcdService from "@/views/audit/service/AuditErrorRcdService.ts";
import type { GetAuditErrorRcdListVo } from "@/views/audit/api/AuditErrorRcdApi.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);

// UI状态
const uiState = reactive({
  isAdvancedSearch: false,
});

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList, removeListBatch } =
  AuditErrorRcdService.useAuditErrorRcdList();

// 选中的列表项
const listSelected = ref<GetAuditErrorRcdListVo[]>([]);

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalMode, modalForm, openModal } = AuditErrorRcdService.useAuditErrorRcdModal();

const modalDialogTitle = computed((): string => {
  if (modalMode.value === "view") {
    return "查看系统错误记录";
  }
  if (modalMode.value === "edit") {
    return "编辑系统错误记录";
  }
  return "新增系统错误记录";
});
</script>

<style scoped>
:deep(.el-input.is-readonly .el-input__wrapper) {
  background-color: var(--el-fill-color-light);
}

:deep(.el-textarea.is-readonly .el-textarea__inner) {
  background-color: var(--el-fill-color-light);
}
</style>
