<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="服务器名称">
            <el-input v-model="listForm.name" placeholder="输入服务器名称" clearable />
          </el-form-item>
          <el-form-item label="唯一编码">
            <el-input v-model="listForm.code" placeholder="输入唯一编码" clearable />
          </el-form-item>
          <el-form-item label="通信协议 0:HTTP+SSE 1:WS">
            <el-input v-model.number="listForm.networkKind" placeholder="输入通信协议 0:HTTP+SSE 1:WS" clearable />
          </el-form-item>
          <el-form-item label="预共享密钥">
            <el-input v-model="listForm.authPsk" placeholder="输入预共享密钥" clearable />
          </el-form-item>
          <el-form-item label="状态 0:离线 1:在线">
            <el-input v-model.number="listForm.status" placeholder="输入状态 0:离线 1:在线" clearable />
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
      <el-button type="success" @click="openModal('add', null)">新增MCP服务器</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="id" label="主键ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="服务器名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="唯一编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="networkKind" label="通信协议 0:HTTP+SSE 1:WS" min-width="120" show-overflow-tooltip />
        <el-table-column prop="host" label="主机" min-width="120" show-overflow-tooltip />
        <el-table-column prop="port" label="端口" min-width="120" show-overflow-tooltip />
        <el-table-column prop="authKind" label="鉴权类型 0:无 1:PSK" min-width="120" show-overflow-tooltip />
        <el-table-column prop="authPsk" label="预共享密钥" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态 0:离线 1:在线" min-width="120" show-overflow-tooltip />
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
      :title="modalMode === 'edit' ? '编辑MCP服务器' : '新增MCP服务器'"
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
        <el-form-item label="服务器名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入服务器名称" clearable :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="唯一编码" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入唯一编码" clearable :maxlength="16" show-word-limit />
        </el-form-item>
        <el-form-item label="通信协议 0:HTTP+SSE 1:WS" prop="networkKind">
          <el-input v-model.number="modalForm.networkKind" placeholder="请输入通信协议 0:HTTP+SSE 1:WS" clearable />
        </el-form-item>
        <el-form-item label="主机" prop="host">
          <el-input v-model="modalForm.host" placeholder="请输入主机" clearable :maxlength="45" show-word-limit />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input v-model.number="modalForm.port" placeholder="请输入端口" clearable />
        </el-form-item>
        <el-form-item label="鉴权类型 0:无 1:PSK" prop="authKind">
          <el-input v-model.number="modalForm.authKind" placeholder="请输入鉴权类型 0:无 1:PSK" clearable />
        </el-form-item>
        <el-form-item label="预共享密钥" prop="authPsk">
          <el-input v-model="modalForm.authPsk" placeholder="请输入预共享密钥" clearable :maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="状态 0:离线 1:在线" prop="status">
          <el-input v-model.number="modalForm.status" placeholder="请输入状态 0:离线 1:在线" clearable />
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
import AacpMcpService from "@/views/aacpMcp/service/AacpMcpService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = AacpMcpService.useAacpMcpList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  AacpMcpService.useAacpMcpModal(modalFormRef, loadList);
</script>

<style scoped></style>
