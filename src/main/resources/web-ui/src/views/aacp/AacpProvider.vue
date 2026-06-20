<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="供应商名称">
            <el-input v-model="listForm.name" placeholder="输入供应商名称" clearable />
          </el-form-item>
          <el-form-item label="供应商代码">
            <el-input v-model="listForm.code" placeholder="输入供应商代码" clearable />
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
      <el-button type="primary" @click="openModal('add', null)">创建供应商</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="供应商名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="供应商代码" min-width="120" show-overflow-tooltip />
        <el-table-column label="接口类型" min-width="120">
          <template #default="scope">
            <span v-if="scope.row.apiKind === 0" class="text-blue-600">OpenAi</span>
            <span v-if="scope.row.apiKind === 1" class="text-purple-600">Anthropic</span>
          </template>
        </el-table-column>
        <el-table-column prop="apiHost" label="接口地址" min-width="120" show-overflow-tooltip />
        <el-table-column prop="apiUrl" label="接口端点" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" min-width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-if="scope.row.status === 0" type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
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
      :title="modalMode === 'edit' ? '编辑供应商' : '创建供应商'"
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
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入供应商名称" clearable :maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="供应商代码" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入供应商代码" clearable :maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="接口密钥" prop="apiKey">
          <el-input v-model="modalForm.apiKey" placeholder="请输入接口密钥" clearable :maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="接口类型" prop="apiKind">
          <el-radio-group v-model="modalForm.apiKind" @change="onApiKindChange">
            <el-radio :value="0">OpenAi</el-radio>
            <el-radio :value="1">Anthropic</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="接口地址" prop="apiHost">
          <el-input v-model="modalForm.apiHost" placeholder="请输入接口地址" clearable :maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item label="接口端点" prop="apiUrl">
          <el-select
            v-model="modalForm.apiUrl"
            placeholder="请输入或选择接口端点"
            allow-create
            filterable
            clearable
            :maxlength="512"
          >
            <el-option v-for="item in apiUrlOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="代理类型" prop="proxyKind">
          <el-radio-group v-model="modalForm.proxyKind" @change="onProxyKindChange">
            <el-radio :value="0">无</el-radio>
            <el-radio :value="1">HTTP</el-radio>
            <el-radio :value="2">SOCKS5</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="代理地址" prop="proxyUrl" v-if="modalForm.proxyKind !== 0">
          <el-input v-model="modalForm.proxyUrl" placeholder="请输入代理地址" clearable :maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modalForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, markRaw, computed } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import ProviderService from "@/views/aacp/service/AacpProviderService";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = ProviderService.useProviderList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  ProviderService.useProviderModal(modalFormRef, loadList);

// 接口端点选项，根据接口类型动态切换
const apiUrlOptions = computed(() => {
  if (modalForm.apiKind === 0) {
    return ["/v1/chat/completions", "/v1/responses"];
  }
  return ["/v1/messages"];
});

/** 切换接口类型时清空接口端点 */
const onApiKindChange = (): void => {
  modalForm.apiUrl = "";
};

/** 切换代理类型时自动补充缺省前缀 */
const onProxyKindChange = (val: number): void => {
  if (val === 0) {
    modalForm.proxyUrl = "";
    return;
  }
  if (val === 1) {
    modalForm.proxyUrl = "http://";
    return;
  }
  modalForm.proxyUrl = "socks5://";
};
</script>

<style scoped></style>
