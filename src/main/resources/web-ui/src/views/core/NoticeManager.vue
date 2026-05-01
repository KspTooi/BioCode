<template>
  <StdListLayout>
    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="标题">
              <el-input v-model="listForm.title" placeholder="请输入标题" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="种类">
              <el-select v-model="listForm.kind" placeholder="请选择种类" clearable style="width: 100%">
                <el-option label="公告" :value="0" />
                <el-option label="业务提醒" :value="1" />
                <el-option label="私信" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="优先级">
              <el-select v-model="listForm.priority" placeholder="请选择优先级" clearable style="width: 100%">
                <el-option label="低" :value="0" />
                <el-option label="中" :value="1" />
                <el-option label="高" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item>
              <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
              <el-button :disabled="listLoading" @click="resetList">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
        <template v-if="uiState.isAdvancedSearch">
          <el-row>
            <el-col :span="5" :offset="1">
              <el-form-item label="通知内容">
                <el-input v-model="listForm.content" placeholder="请输入通知内容" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="5" :offset="1">
              <el-form-item label="业务类型">
                <el-input v-model="listForm.category" placeholder="请输入业务类型" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="5" :offset="1">
              <el-form-item label="发送人姓名">
                <el-input v-model="listForm.senderName" placeholder="请输入发送人姓名" clearable />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>
    </template>

    <template #actions>
      <el-button type="success" @click="openModal('add', null)">创建消息</el-button>
    </template>

    <template #table>
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="title" label="标题" min-width="128" show-overflow-tooltip />
        <el-table-column prop="kind" label="种类" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.kind === 0" type="info">公告</el-tag>
            <el-tag v-if="scope.row.kind === 1" type="warning">业务提醒</el-tag>
            <el-tag v-if="scope.row.kind === 2" type="success">私信</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="75">
          <template #default="scope">
            <el-tag v-if="scope.row.priority === 0" type="info">低</el-tag>
            <el-tag v-if="scope.row.priority === 1" type="warning">中</el-tag>
            <el-tag v-if="scope.row.priority === 2" type="danger">高</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="业务类型" width="90" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.category">{{ scope.row.category }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="senderName" label="发送人" width="100" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ scope.row.senderName || "系统" }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetKind" label="接收对象类型" width="120" align="center">
          <template #default="scope">
            <span v-if="scope.row.targetKind === 0"> 全员 </span>
            <span v-if="scope.row.targetKind === 1"> 指定部门 </span>
            <span v-if="scope.row.targetKind === 2"> 指定用户 </span>
          </template>
        </el-table-column>
        <el-table-column prop="targetCount" label="接收人数" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="200" />
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
      <!-- 部门选择器 -->
      <CoreOrgDeptSelectModal
        v-model="deptSelectVisible"
        :default-selected="modalForm.targetIds"
        title="选择接收部门"
        multiple
        type="dept"
        @confirm="onDeptSelect"
      />

      <!-- 用户选择器 -->
      <CoreUserSelectModal
        v-model="userSelectVisible"
        :default-selected="modalForm.targetIds"
        title="选择接收用户"
        multiple
        @confirm="onUserSelect"
      />

      <!-- 创建/编辑模态框 -->
      <el-dialog
        v-model="modalVisible"
        :title="modalMode === 'edit' ? '编辑消息' : '创建消息'"
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
          <el-form-item label="标题" prop="title">
            <el-input v-model="modalForm.title" placeholder="请输入标题" maxlength="32" show-word-limit clearable />
          </el-form-item>
          <el-form-item label="种类" prop="kind">
            <el-select v-model="modalForm.kind" placeholder="请选择种类" style="width: 100%">
              <el-option label="公告" :value="0" />
              <el-option label="业务提醒" :value="1" />
              <el-option label="私信" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="通知内容" prop="content">
            <el-input v-model="modalForm.content" type="textarea" :rows="4" placeholder="请输入通知内容" clearable />
          </el-form-item>
          <el-form-item label="优先级" prop="priority">
            <el-select v-model="modalForm.priority" placeholder="请选择优先级" style="width: 100%">
              <el-option label="低" :value="0" />
              <el-option label="中" :value="1" />
              <el-option label="高" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="业务类型" prop="category">
            <el-input v-model="modalForm.category" placeholder="请输入业务类型" maxlength="32" show-word-limit clearable />
          </el-form-item>
          <el-form-item label="接收对象类型" prop="targetKind">
            <el-radio-group v-model="modalForm.targetKind" style="width: 100%" :disabled="modalMode === 'edit'">
              <el-radio label="全员" :value="0" />
              <el-radio label="指定部门" :value="1" />
              <el-radio label="指定用户" :value="2" />
            </el-radio-group>
          </el-form-item>

          <el-form-item v-if="modalForm.targetKind === 1 && modalMode === 'add'" label="选择接收部门" prop="targetIds">
            <div class="flex items-center gap-4 text-cyan-600 ml-4">
              <el-button type="primary" size="small" @click="deptSelectVisible = true">选择接收部门</el-button>
              <span>已选择 {{ modalForm.targetIds.length }} 个部门</span>
            </div>
          </el-form-item>
          <el-form-item v-if="modalForm.targetKind === 2 && modalMode === 'add'" label="选择接收用户" prop="targetIds">
            <div class="flex items-center gap-4 text-cyan-600 ml-4">
              <el-button type="primary" size="small" @click="userSelectVisible = true">选择接收用户</el-button>
              <span>已选择 {{ modalForm.targetIds.length }} 位用户</span>
            </div>
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
import NoticeService from "@/views/core/service/NoticeService.ts";
import CoreUserSelectModal from "@/views/core/components/public/CoreUserSelectModal.vue";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";
import CoreOrgDeptSelectModal from "@/views/core/components/public/CoreOrgDeptSelectModal.vue";

// 部门选择器引用
const deptSelectVisible = ref(false);

// 用户选择器引用
const userSelectVisible = ref(false);

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// UI状态
const uiState = reactive({
  isAdvancedSearch: false,
});

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = NoticeService.useNoticeList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const {
  modalVisible,
  modalLoading,
  modalMode,
  modalForm,
  modalRules,
  openModal,
  resetModal,
  submitModal,
  onDeptSelect,
  onUserSelect,
} = NoticeService.useNoticeModal(modalFormRef, loadList);
</script>

<style scoped></style>
