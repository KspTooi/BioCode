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
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
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
        <el-table-column prop="seq" label="排序" min-width="90" align="center">
          <template #default="scope">
            <ComSeqFixer
              :id="String(scope.row.id)"
              seq-field="seq"
              :get-detail-api="(id) => QfBizFormApi.getQfBizFormDetails({ id })"
              :edit-api="
                (id, dto) =>
                  QfBizFormApi.editQfBizForm({
                    id,
                    name: dto.name,
                    formType: dto.formType,
                    icon: dto.icon,
                    tableName: dto.tableName,
                    routePc: dto.routePc,
                    routeMobile: dto.routeMobile,
                    status: dto.status,
                    seq: dto.seq,
                    summaryTemplate: dto.summaryTemplate,
                  })
              "
              :display-value="scope.row.seq"
              :on-success="loadList"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="SettingIcon" @click="openFieldListModal(scope.row)">
              配置字段
            </el-button>
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 创建/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑业务表单' : '创建业务表单'"
      width="800px"
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务名称" prop="name">
              <el-input v-model="modalForm.name" placeholder="请输入业务名称" clearable :maxlength="40" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="modalMode === 'add'" label="业务编码" prop="code">
              <el-input v-model="modalForm.code" placeholder="请输入业务编码" clearable :maxlength="16" show-word-limit />
            </el-form-item>
            <el-form-item v-else label="业务编码">
              <el-input v-model="modalForm.code" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="表单类型" prop="formType">
              <el-select v-model="modalForm.formType" placeholder="请选择表单类型" clearable class="w-full">
                <el-option label="手搓表单" :value="0" />
                <el-option label="动态表单" :value="1" :disabled="true" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="modalForm.status">
                <el-radio label="正常" :value="0" />
                <el-radio label="停用" :value="1" />
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物理表名" prop="tableName">
              <el-input v-model="modalForm.tableName" placeholder="请输入物理表名" clearable :maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="seq">
              <el-input-number v-model="modalForm.seq" placeholder="请输入排序" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="表单图标" prop="icon">
              <StdIconPicker v-model="modalForm.icon" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
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
          </el-col>
          <el-col :span="24">
            <el-form-item label="移动端路由" prop="routeMobile">
              <el-input
                class="w-full"
                v-model="modalForm.routeMobile"
                placeholder="请输入移动端路由名"
                clearable
                :maxlength="512"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="摘要模板" prop="summaryTemplate">
              <el-input
                v-model="modalForm.summaryTemplate"
                type="textarea"
                placeholder="请输入摘要模板"
                clearable
                :maxlength="200"
                show-word-limit
                :autosize="{ minRows: 3 }"
              />
            </el-form-item>
          </el-col>
        </el-row>
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

    <!-- 字段配置模态框 -->
    <el-dialog
      v-model="fieldListModalVisible"
      :title="`配置字段 - ${fieldListModalFormName}`"
      width="900px"
      :close-on-click-modal="false"
      destroy-on-close
      @close="closeFieldListModal"
    >
      <div class="mb-3">
        <el-button type="primary" :loading="fieldListLoading || fieldModalLoading" @click="openFieldModal('add', null)"
          >新增字段</el-button
        >
      </div>
      <el-table v-loading="fieldListLoading" :data="fieldListData" stripe border :height="400">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="fieldName" label="字段名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="140">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openFieldModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeFieldList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-3 flex justify-end">
        <el-pagination
          v-model:current-page="fieldListForm.pageNum"
          v-model:page-size="fieldListForm.pageSize"
          :total="fieldListTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadFieldList"
          @current-change="loadFieldList"
        />
      </div>
    </el-dialog>

    <!-- 字段新增/编辑模态框 -->
    <el-dialog
      v-model="fieldModalVisible"
      :title="fieldModalMode === 'edit' ? '编辑字段' : '新增字段'"
      width="520px"
      :close-on-click-modal="false"
      append-to-body
      destroy-on-close
      @close="resetFieldModal"
    >
      <el-form
        v-if="fieldModalVisible"
        ref="fieldModalFormRef"
        :model="fieldModalForm"
        :rules="fieldModalRules"
        label-width="80px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="字段名" prop="fieldName">
          <el-input v-model="fieldModalForm.fieldName" placeholder="请输入字段名" clearable :maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="fieldModalForm.remark" placeholder="请输入备注" clearable :maxlength="32" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="fieldModalVisible = false">关闭</el-button>
          <el-button type="primary" :loading="fieldModalLoading" @click="submitFieldModal">
            {{ fieldModalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 公共组件选择器 -->
    <ComPublicCompChooseModal
      v-model="modalForm.routePc"
      v-model:modal-visible="cpcmVisible"
      v-model:search-keyword="cpcmQuery"
    />
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete, Setting } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import QfBizFormService from "@/views/qf/service/QfBizFormService.ts";
import QfBizFormFieldService from "@/views/qf/service/QfBizFormFieldService.ts";
import QfBizFormApi from "@/views/qf/api/QfBizFormApi.ts";
import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import StdIconPicker from "@/soa/std-series/StdIconPicker.vue";
import ComPublicCompChooseModal from "@/soa/com-series/ComPublicCompChooseModal.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const SettingIcon = markRaw(Setting);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = QfBizFormService.useQfBizFormList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  QfBizFormService.useQfBizFormModal(modalFormRef, loadList);

const cpcmVisible = ref(false);
const cpcmQuery = ref("");

// 字段配置列表模态框
const {
  modalVisible: fieldListModalVisible,
  modalFormName: fieldListModalFormName,
  listForm: fieldListForm,
  listData: fieldListData,
  listTotal: fieldListTotal,
  listLoading: fieldListLoading,
  loadList: loadFieldList,
  removeList: removeFieldList,
  openModal: openFieldListModal,
  closeModal: closeFieldListModal,
} = QfBizFormFieldService.useBizFormFieldListModal();

const fieldModalFormRef = ref<FormInstance>();

const {
  modalVisible: fieldModalVisible,
  modalLoading: fieldModalLoading,
  modalMode: fieldModalMode,
  modalForm: fieldModalForm,
  modalRules: fieldModalRules,
  openModal: openFieldModal,
  resetModal: resetFieldModal,
  submitModal: submitFieldModal,
} = QfBizFormFieldService.useBizFormFieldModal(fieldModalFormRef, loadFieldList, fieldListForm);
</script>

<style scoped></style>
