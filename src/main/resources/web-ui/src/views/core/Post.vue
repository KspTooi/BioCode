<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="岗位名称">
            <el-input v-model="listForm.name" placeholder="输入岗位名称" clearable />
          </el-form-item>
          <el-form-item label="岗位编码">
            <el-input v-model="listForm.code" placeholder="输入岗位编码" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction class="">
      <el-button type="primary" @click="openModal('add', null)">创建岗位</el-button>
      <el-button
        type="danger"
        :disabled="listSelected.length === 0"
        :loading="listLoading"
        @click="() => removeListBatch(listSelected)"
      >
        批量删除
      </el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table
        v-loading="listLoading"
        :data="listData"
        stripe
        border
        height="100%"
        @selection-change="(val: GetPostListVo[]) => (listSelected = val)"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column
          type="index"
          label="岗位序号"
          width="100"
          align="center"
          :index="(index) => (listForm.pageNum - 1) * listForm.pageSize + index + 1"
        />
        <el-table-column prop="name" label="岗位名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="岗位编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="seq" label="岗位排序" min-width="120" show-overflow-tooltip align="center">
          <template #default="scope">
            <ComSeqFixer
              :id="scope.row.id"
              seq-field="seq"
              :get-detail-api="
                async (id: string) => {
                  return await PostApi.getPostDetails({ id });
                }
              "
              :edit-api="
                async (id: string, dto: any) => {
                  return await PostApi.editPost({ id, ...dto });
                }
              "
              :display-value="scope.row.seq"
              :on-success="loadList"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? "启用" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="100">
          <template #default="scope">
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
      :title="modalMode === 'edit' ? '编辑岗位' : '创建岗位'"
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
        <el-form-item label="岗位名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入岗位名称" maxlength="32" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="岗位编码" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入岗位编码" maxlength="32" show-word-limit clearable />
        </el-form-item>
        <el-form-item label="岗位排序" prop="seq">
          <el-input-number v-model="modalForm.seq" :min="0" placeholder="请输入岗位排序" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modalForm.status">
            <el-radio :value="0">启用</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="modalForm.remark"
            type="textarea"
            :autosize="{ minRows: 4 }"
            placeholder="请输入备注"
            maxlength="1000"
            show-word-limit
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
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import PostService from "@/views/core/service/PostService.ts";
import PostApi from "@/views/core/api/PostApi.ts";
import type { GetPostListVo } from "@/views/core/api/PostApi.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList, removeListBatch } =
  PostService.usePostList();

// 选中的列表项
const listSelected = ref<GetPostListVo[]>([]);

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  PostService.usePostModal(modalFormRef, loadList);
</script>

<style scoped></style>
