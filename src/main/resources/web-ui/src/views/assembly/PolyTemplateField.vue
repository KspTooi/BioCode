<template>
  <StdListContainer>
    <StdListAreaAction class="flex gap-2">
      <el-button v-if="cdrcCanReturn" type="primary" @click="cdrcReturn">{{ cdrcReturnName }}</el-button>
      <el-button type="success" @click="openModal('add', null)">创建聚合模板字段</el-button>
    </StdListAreaAction>

    <div class="template-info-bar">
      <div class="template-info-item">
        <span class="template-info-label">模板名称</span>
        <span class="template-info-value">{{ templateRow?.name ?? "—" }}</span>
      </div>
      <div class="template-info-divider" />
      <div class="template-info-item">
        <span class="template-info-label">模板代码</span>
        <span class="template-info-value">{{ templateRow?.code ?? "—" }}</span>
      </div>
    </div>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="字段名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="policyQuery" label="查询策略" min-width="80" show-overflow-tooltip />
        <el-table-column prop="policyView" label="显示策略" min-width="80" show-overflow-tooltip />
        <el-table-column prop="seq" label="排序" min-width="65" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)" :icon="EditIcon">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑聚合模板字段' : '创建聚合模板字段'"
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
        label-width="110px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="字段名" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入字段名" clearable :maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="查询策略" prop="policyQuery">
          <el-select v-model="modalForm.policyQuery" placeholder="选择查询策略">
            <el-option label="等于" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示策略" prop="policyView">
          <el-select v-model="modalForm.policyView" placeholder="选择显示策略">
            <el-option label="文本框" :value="0" />
            <el-option label="文本域" :value="1" />
            <el-option label="下拉" :value="2" />
            <el-option label="单" :value="3" />
            <el-option label="多" :value="4" />
            <el-option label="LD" :value="5" />
            <el-option label="LDT" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input v-model.number="modalForm.seq" placeholder="请输入排序" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">关闭</el-button>
          <el-button type="primary" @click="submitModal" :loading="modalLoading">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw, onMounted } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import PolyTemplateFieldService from "@/views/assembly/service/PolyTemplateFieldService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import ComDirectRouteContext from "@/soa/com-series/service/ComDirectRouteContext.ts";
import type { GetPolyTemplateListVo } from "@/views/assembly/api/PolyTemplateApi";

const { cdrcCanReturn, cdrcReturnName, cdrcReturn, getCdrcQuery } = ComDirectRouteContext.useDirectRouteContext();
const templateRow = getCdrcQuery() as GetPolyTemplateListVo | null;
const polyTemplateId = ref(templateRow?.id ?? "");

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

const { listForm, listData, listTotal, listLoading, loadList, removeList } =
  PolyTemplateFieldService.usePolyTemplateFieldList(polyTemplateId);

const modalFormRef = ref<FormInstance>();

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  PolyTemplateFieldService.usePolyTemplateFieldModal(modalFormRef, polyTemplateId, loadList);

onMounted(async () => {
  if (!polyTemplateId.value) {
    return;
  }
  await loadList();
});
</script>

<style scoped>
.template-info-bar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 0;
  border: 1px solid #e4e7ed;
  border-left: 4px solid #409eff;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.template-info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 20px;
}

.template-info-label {
  font-size: 11px;
  color: #909399;
  letter-spacing: 0.5px;
}

.template-info-value {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.template-info-divider {
  width: 1px;
  height: 36px;
  background: #e4e7ed;
  flex-shrink: 0;
}
</style>