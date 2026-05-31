<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="模型分组">
            <el-input v-model="listForm.groupName" placeholder="输入模型分组" clearable />
          </el-form-item>
          <el-form-item label="模型名称">
            <el-input v-model="listForm.name" placeholder="输入模型名称" clearable />
          </el-form-item>
          <el-form-item label="模型编码">
            <el-input v-model="listForm.code" placeholder="输入模型编码" clearable />
          </el-form-item>
          <el-form-item label="模型状态">
            <el-select v-model="listForm.status" placeholder="选择模型状态" clearable multiple>
              <el-option label="草稿" :value="0" />
              <el-option label="已部署" :value="1" />
              <el-option label="历史" :value="2" />
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
      <el-button type="primary" @click="openModal('add', null)">创建流程模型</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="groupName" label="模型分组" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.groupName">{{ scope.row.groupName }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="模型名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="模型编码" min-width="120" show-overflow-tooltip />
        <el-table-column label="业务表单" min-width="140" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.bizFormName && scope.row.bizFormCode">{{ scope.row.bizFormName }}({{ scope.row.bizFormCode }})</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="模型版本号" min-width="75" show-overflow-tooltip align="center">
          <template #default="scope">
            <el-tag>V{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="模型状态" min-width="100" show-overflow-tooltip align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">草稿</el-tag>
            <el-tag v-if="scope.row.status === 1" type="success">已部署</el-tag>
            <el-tag v-if="scope.row.status === 2" type="warning">历史</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="seq" label="排序" min-width="90" show-overflow-tooltip>
          <template #default="scope">
            <ComSeqFixer
              :id="scope.row.id"
              seq-field="seq"
              :get-detail-api="(id) => QfModelApi.getQfModelDetails({ id })"
              :edit-api="(id, dto) => QfModelApi.editQfModel({ id, name: dto.name, groupId: dto.groupId, seq: dto.seq })"
              :display-value="scope.row.seq"
              :on-success="loadList"
              :disabled="scope.row.status !== 0"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="240">
          <template #default="scope">
            <el-button
              link
              type="primary"
              size="small"
              :icon="Edit"
              :disabled="scope.row.status !== 1"
              @click="createNewVersion(scope.row)"
            >
              创建新版本
            </el-button>
            <el-button link type="primary" size="small" :icon="Edit" @click="cdrcRedirect('qfModelDesigner', scope.row)">
              设计
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              :icon="Edit"
              :disabled="scope.row.status !== 0"
              @click="deployQfModel(scope.row)"
            >
              部署
            </el-button>
            <el-button
              v-show="scope.row.status === 0"
              link
              type="primary"
              size="small"
              :icon="EditIcon"
              @click="openModal('edit', scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-show="scope.row.status !== 0"
              link
              type="primary"
              size="small"
              :icon="View"
              @click="openModal('view', scope.row)"
            >
              查看
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 创建/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑流程模型' : '创建流程模型'"
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
        <el-form-item label="模型分组" prop="groupId">
          <el-select
            v-model="modalForm.groupId"
            placeholder="请选择模型分组"
            clearable
            style="width: 100%"
            filterable
            :disabled="modalMode === 'view'"
          >
            <el-option v-for="group in groupList" :key="group.id" :label="group.name" :value="group.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="模型名称" prop="name">
          <el-input
            v-model="modalForm.name"
            placeholder="请输入模型名称"
            clearable
            :maxlength="80"
            show-word-limit
            :disabled="modalMode === 'view'"
          />
        </el-form-item>
        <el-form-item label="模型编码" prop="code">
          <el-input
            v-model="modalForm.code"
            placeholder="请输入模型编码"
            clearable
            :maxlength="32"
            show-word-limit
            :disabled="modalMode === 'edit' || modalMode === 'view'"
          />
        </el-form-item>
        <el-form-item label="业务表单" prop="formId">
          <el-select
            v-model="modalForm.formId"
            placeholder="请选择业务表单"
            clearable
            style="width: 100%"
            filterable
            :disabled="modalMode === 'view'"
          >
            <el-option v-for="form in modelFormList" :key="form.id" :label="`${form.name}(${form.code})`" :value="form.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input
            v-model.number="modalForm.seq"
            type="number"
            placeholder="请输入排序"
            clearable
            :disabled="modalMode === 'view'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">关闭</el-button>
          <el-button v-show="modalMode !== 'view'" type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw, onMounted } from "vue";
import { Edit, Delete, View } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import QfModelService from "@/views/qf/service/QfModelService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import type { GetQfModelListVo } from "@/views/qf/api/QfModelApi.ts";
import QfModelApi from "@/views/qf/api/QfModelApi.ts";
import ComDirectRouteContext from "@/soa/com-series/service/ComDirectRouteContext.ts";
import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue";
// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const ViewIcon = markRaw(View);

/** CDRC上下文服务 */
const { cdrcRedirect, getCdrcQuery } = ComDirectRouteContext.useDirectRouteContext();

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList, createNewVersion, deployQfModel } =
  QfModelService.useQfModelList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const {
  modalVisible,
  modalLoading,
  modalMode,
  modalForm,
  modalRules,
  groupList,
  modelFormList,
  openModal,
  resetModal,
  submitModal,
} = QfModelService.useQfModelModal(modalFormRef, loadList);

onMounted(async () => {});
</script>

<style scoped></style>
