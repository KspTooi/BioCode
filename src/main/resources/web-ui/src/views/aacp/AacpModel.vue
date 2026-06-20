<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="模型变体名称">
            <el-input v-model="listForm.name" placeholder="输入模型变体名称" clearable />
          </el-form-item>
          <el-form-item label="模型标识">
            <el-input v-model="listForm.code" placeholder="输入模型标识" clearable />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="listForm.kind" placeholder="请选择类型" clearable>
              <el-option label="文本" :value="0" />
              <el-option label="图形" :value="1" />
              <el-option label="音频" :value="2" />
              <el-option label="多模态" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="请选择状态" clearable>
              <el-option label="禁用" :value="0" />
              <el-option label="启用" :value="1" />
            </el-select>
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
      <el-button type="primary" @click="openModal('add', null)">创建模型变体</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="模型变体名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="模型标识" min-width="120" show-overflow-tooltip />
        <el-table-column label="类型" min-width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.kind === 0" size="small">文本</el-tag>
            <el-tag v-if="scope.row.kind === 1" type="warning" size="small">图形</el-tag>
            <el-tag v-if="scope.row.kind === 2" type="success" size="small">音频</el-tag>
            <el-tag v-if="scope.row.kind === 3" type="primary" size="small">多模态</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxContext" label="最大上下文" min-width="100" show-overflow-tooltip />
        <el-table-column prop="maxOutputToken" label="最大输出词元" min-width="100" show-overflow-tooltip />
        <el-table-column label="推理" min-width="70" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.apiReasoning === 1" type="success" size="small">支持</el-tag>
            <el-tag v-if="scope.row.apiReasoning === 0" type="info" size="small">不支持</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推理强度" min-width="80" align="center">
          <template #default="scope">
            <span v-if="scope.row.apiReasoningEffort === 0">关</span>
            <span v-if="scope.row.apiReasoningEffort === 1" class="text-gray-600">低</span>
            <span v-if="scope.row.apiReasoningEffort === 2" class="text-blue-600">中</span>
            <span v-if="scope.row.apiReasoningEffort === 3" class="text-orange-600">高</span>
            <span v-if="scope.row.apiReasoningEffort === 4" class="text-red-600">极高</span>
          </template>
        </el-table-column>
        <el-table-column prop="fincInput" label="输入单价" min-width="90" show-overflow-tooltip />
        <el-table-column prop="fincInputCached" label="输入单价(缓存)" min-width="110" show-overflow-tooltip />
        <el-table-column prop="fincOutput" label="输出单价" min-width="90" show-overflow-tooltip />
        <el-table-column prop="testTtfb" label="首字响应(MS)" min-width="100" show-overflow-tooltip />
        <el-table-column prop="testRate" label="响应速率(T/S)" min-width="100" show-overflow-tooltip />
        <el-table-column prop="seq" label="排序" min-width="60" align="center" />
        <el-table-column label="状态" min-width="70" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-if="scope.row.status === 0" type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="155" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)" :icon="EditIcon">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 创建/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑模型变体' : '创建模型变体'"
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
        <el-form-item label="模型变体名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入模型变体名称" clearable :maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="模型标识" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入模型标识" clearable :maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="类型 0:文本 1:图形 2:音频 3:多模态" prop="kind">
          <el-input v-model.number="modalForm.kind" placeholder="请输入类型 0:文本 1:图形 2:音频 3:多模态" clearable />
        </el-form-item>
        <el-form-item label="最大上下文长度" prop="maxContext">
          <el-input v-model.number="modalForm.maxContext" placeholder="请输入最大上下文长度" clearable />
        </el-form-item>
        <el-form-item label="最大输出词元" prop="maxOutputToken">
          <el-input v-model.number="modalForm.maxOutputToken" placeholder="请输入最大输出词元" clearable />
        </el-form-item>
        <el-form-item label="推理 0:不支持 1:支持" prop="apiReasoning">
          <el-input v-model.number="modalForm.apiReasoning" placeholder="请输入推理 0:不支持 1:支持" clearable />
        </el-form-item>
        <el-form-item label="推理强度 0:关 1:低 2:中 3:高 4:极高" prop="apiReasoningEffort">
          <el-input
            v-model.number="modalForm.apiReasoningEffort"
            placeholder="请输入推理强度 0:关 1:低 2:中 3:高 4:极高"
            clearable
          />
        </el-form-item>
        <el-form-item label="附加参数" prop="apiAppendParam">
          <el-input v-model="modalForm.apiAppendParam" placeholder="请输入附加参数" clearable />
        </el-form-item>
        <el-form-item label="附加请求头" prop="apiAppendHeaders">
          <el-input v-model="modalForm.apiAppendHeaders" placeholder="请输入附加请求头" clearable />
        </el-form-item>
        <el-form-item label="输入单价" prop="fincInput">
          <el-input v-model="modalForm.fincInput" placeholder="请输入输入单价" clearable />
        </el-form-item>
        <el-form-item label="输入单价(缓存)" prop="fincInputCached">
          <el-input v-model="modalForm.fincInputCached" placeholder="请输入输入单价(缓存)" clearable />
        </el-form-item>
        <el-form-item label="输出单价" prop="fincOutput">
          <el-input v-model="modalForm.fincOutput" placeholder="请输入输出单价" clearable />
        </el-form-item>
        <el-form-item label="测试首字响应时间 MS" prop="testTtfb">
          <el-input v-model.number="modalForm.testTtfb" placeholder="请输入测试首字响应时间 MS" clearable />
        </el-form-item>
        <el-form-item label="测试响应速率 T/S" prop="testRate">
          <el-input v-model.number="modalForm.testRate" placeholder="请输入测试响应速率 T/S" clearable />
        </el-form-item>
        <el-form-item label="最后测试时间" prop="testTime">
          <el-input v-model="modalForm.testTime" placeholder="请输入最后测试时间" clearable />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="modalForm.remark" placeholder="请输入备注" clearable :maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input v-model.number="modalForm.seq" placeholder="请输入排序" clearable />
        </el-form-item>
        <el-form-item label="状态 0:禁用 1:启用" prop="status">
          <el-input v-model.number="modalForm.status" placeholder="请输入状态 0:禁用 1:启用" clearable />
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
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import ModelService from "@/views/aacp/service/AacpModelService";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = ModelService.useModelList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  ModelService.useModelModal(modalFormRef, loadList);
</script>

<style scoped></style>
