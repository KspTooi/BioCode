<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="PAT名称">
            <el-input v-model="listForm.name" placeholder="输入PAT名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="请选择状态" clearable>
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
      <el-button type="success" @click="openModal('add', null)">新增基本PAT</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="PAT名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="patPt" label="部分明文" min-width="120" show-overflow-tooltip />
        <el-table-column label="过期时间" min-width="120">
          <template #default="scope">
            <span v-if="scope.row.expire">{{ scope.row.expire }}</span>
            <span v-if="!scope.row.expire" class="text-green-500">永久</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="120" show-overflow-tooltip />
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
      :title="modalMode === 'edit' ? '编辑基本PAT' : '新增基本PAT'"
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
        <el-form-item label="PAT名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入PAT名称" clearable :maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="过期时间" prop="expire">
          <el-date-picker
            v-model="modalForm.expire"
            type="datetime"
            placeholder="不填则永不过期"
            clearable
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
          <div class="flex gap-1 mt-1">
            <el-button size="small" @click="modalForm.expire = addExpire(7, 'day')">7天</el-button>
            <el-button size="small" @click="modalForm.expire = addExpire(15, 'day')">15天</el-button>
            <el-button size="small" @click="modalForm.expire = addExpire(1, 'month')">1个月</el-button>
            <el-button size="small" @click="modalForm.expire = addExpire(3, 'month')">3个月</el-button>
            <el-button size="small" @click="modalForm.expire = addExpire(1, 'year')">1年</el-button>
          </div>
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
import BasicPatService from "@/views/auth/basicpat/service/BasicPatService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = BasicPatService.useBasicPatList();

const modalFormRef = ref<FormInstance>();

const pad = (n: number) => String(n).padStart(2, "0");
const addExpire = (amount: number, unit: "day" | "month" | "year") => {
  const d = new Date();
  if (unit === "day") d.setDate(d.getDate() + amount);
  if (unit === "month") d.setMonth(d.getMonth() + amount);
  if (unit === "year") d.setFullYear(d.getFullYear() + amount);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
};

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  BasicPatService.useBasicPatModal(modalFormRef, loadList);
</script>

<style scoped></style>
