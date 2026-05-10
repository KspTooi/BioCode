<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="菜单包名">
            <el-input v-model="listForm.name" placeholder="输入菜单包名" clearable />
          </el-form-item>
          <el-form-item label="菜单包编码">
            <el-input v-model="listForm.code" placeholder="输入菜单包编码" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="请选择状态" clearable style="width: 180px">
              <el-option :value="1" label="启用" />
              <el-option :value="0" label="禁用" />
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
      <el-button type="success" @click="openModal('add', null)">创建菜单包</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />

        <el-table-column prop="name" label="菜单包名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="菜单包编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="mCount" label="菜单总数" min-width="80" align="center" />
        <el-table-column prop="status" label="状态" min-width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="seq" label="排序" min-width="120" show-overflow-tooltip align="center">
          <template #default="scope">
            <ComSeqFixer
              :id="scope.row.id"
              seq-field="seq"
              :get-detail-api="
                async (id: string) => {
                  return await PackApi.getPackDetails({ id });
                }
              "
              :edit-api="
                async (id: string, dto: any) => {
                  return await PackApi.editPack({ id, ...dto });
                }
              "
              :display-value="scope.row.seq"
              :on-success="loadList"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)" :icon="EditIcon">
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="openMenuModal(scope.row)" :icon="EditIcon">
              管理菜单
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
      :title="modalMode === 'edit' ? '编辑菜单包' : '创建菜单包'"
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
        <el-form-item label="菜单包名" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入菜单包名" clearable :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="菜单包编码" prop="code">
          <el-input
            v-model="modalForm.code"
            placeholder="请输入菜单包编码"
            clearable
            :maxlength="16"
            show-word-limit
            :disabled="modalMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modalForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input-number v-model="modalForm.seq" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="modalForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            :maxlength="200"
            show-word-limit
          />
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

    <PackMenuModal :visible="menuModalVisible" :data="menuModalRow" @close="menuModalVisible = false" @success="loadList" />
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import type { GetPackListVo } from "@/views/core/api/PackApi.ts";
import PackService from "@/views/core/service/PackManagerService.js";
import PackApi from "@/views/core/api/PackApi.ts";
import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue";
import PackMenuModal from "@/views/core/components/PackMenuModal.vue";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = PackService.usePackList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  PackService.usePackModal(modalFormRef, loadList);

const menuModalVisible = ref(false);
const menuModalRow = ref<GetPackListVo | null>(null);

const openMenuModal = (row: GetPackListVo): void => {
  menuModalRow.value = row;
  menuModalVisible.value = true;
};
</script>

<style scoped></style>
