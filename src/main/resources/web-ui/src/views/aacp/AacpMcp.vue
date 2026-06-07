<template>
  <StdListLayout show-persist-tip>
    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="服务器名称">
              <el-input v-model="listForm.name" placeholder="请输入服务器名称" clearable style="width: 200px" />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="唯一编码">
              <el-input v-model="listForm.code" placeholder="请输入唯一编码" clearable style="width: 200px" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="状态">
              <el-select v-model="listForm.status" placeholder="请选择" clearable style="width: 200px">
                <el-option label="离线" :value="0" />
                <el-option label="在线" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" :offset="4" style="display: flex; justify-content: flex-end">
            <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
            <el-button :disabled="listLoading" @click="resetList">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </template>

    <template #actions>
      <el-button type="success" @click="openModal('add', null)">创建MCP服务器</el-button>
    </template>

    <template #table>
      <el-table v-loading="listLoading" :data="listData" border row-key="id" height="100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="服务器名称" prop="name" />
        <el-table-column label="唯一编码" prop="code" width="130" />
        <el-table-column label="通信协议" width="110" align="center">
          <template #default="scope">
            <span v-show="scope.row.networkKind === 0">HTTP+SSE</span>
          </template>
        </el-table-column>
        <el-table-column label="主机" prop="host" />
        <el-table-column label="端口" prop="port" width="80" align="center" />
        <el-table-column label="鉴权类型" width="100" align="center">
          <template #default="scope">
            <span v-show="scope.row.authKind === 0">无</span>
            <span v-show="scope.row.authKind === 1">PSK</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <span v-show="scope.row.status === 0" style="color: #999">离线</span>
            <span v-show="scope.row.status === 1" style="color: #67c23a">在线</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="ViewIcon" @click="openModal('edit', scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)">删除</el-button>
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
  </StdListLayout>

  <el-dialog
    v-model="modalVisible"
    :title="modalMode === 'edit' ? '编辑MCP服务器' : '创建MCP服务器'"
    width="550px"
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
      <el-form-item label="服务器名称" prop="name">
        <el-input v-model="modalForm.name" placeholder="请输入服务器名称" :maxlength="40" show-word-limit />
      </el-form-item>
      <el-form-item label="唯一编码" prop="code">
        <el-input v-model="modalForm.code" placeholder="请输入唯一编码" :maxlength="16" show-word-limit />
      </el-form-item>
      <el-form-item label="通信协议">
        <el-select v-model="modalForm.networkKind" placeholder="请选择通信协议" disabled>
          <el-option label="HTTP+SSE" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="主机" prop="host">
        <el-input v-model="modalForm.host" placeholder="请输入主机" :maxlength="45" show-word-limit />
      </el-form-item>
      <el-form-item label="端口" prop="port">
        <el-input v-model.number="modalForm.port" placeholder="请输入端口" type="number" />
      </el-form-item>
      <el-form-item label="鉴权类型" prop="authKind">
        <el-select v-model="modalForm.authKind" placeholder="请选择鉴权类型">
          <el-option label="无" :value="0" />
          <el-option label="PSK" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item v-show="modalForm.authKind === 1" label="预共享密钥" prop="authPsk">
        <el-input
          v-model="modalForm.authPsk"
          placeholder="请输入预共享密钥"
          type="textarea"
          :rows="4"
          :maxlength="2000"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="modalForm.status" placeholder="请选择状态">
          <el-option label="离线" :value="0" />
          <el-option label="在线" :value="1" />
        </el-select>
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

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { View, Delete, Plus } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";
import AacpMcpService from "@/views/aacp/service/AacpMcpService.ts";

const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);
const PlusIcon = markRaw(Plus);

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = AacpMcpService.useAacpMcpList();

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  AacpMcpService.useAacpMcpModal(modalFormRef, loadList);
</script>
