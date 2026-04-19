<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="模型名称">
            <el-input v-model="listForm.name" placeholder="输入模型名称" clearable />
          </el-form-item>
          <el-form-item label="模型编码">
            <el-input v-model="listForm.code" placeholder="输入模型编码" clearable />
          </el-form-item>
          <el-form-item label="部署状态">
            <el-select v-model="listForm.status" placeholder="选择部署状态" clearable>
              <el-option label="正常" :value="0" />
              <el-option label="部署失败" :value="1" />
              <el-option label="已挂起" :value="2" />
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
      <!-- <el-button type="success" @click="openModal('add', null)">新增流程模型部署历史</el-button> -->
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="模型名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="模型编码" min-width="120" show-overflow-tooltip />
        <el-table-column prop="version" label="模型版本号" min-width="60" show-overflow-tooltip align="center">
          <template #default="scope">
            <el-tag>V{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="部署状态" min-width="80" show-overflow-tooltip align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="success">正常</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="danger">失败</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="warning">已挂起</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="部署时间" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <!-- <el-button link type="primary" size="small" @click="openModal('edit', scope.row)" :icon="EditIcon">
              编辑
            </el-button> -->
            <el-button
              link
              type="primary"
              size="small"
              :icon="Edit"
              @click="suspendQfModelDeployRcd(scope.row)"
              :disabled="scope.row.status !== 0"
              >挂起</el-button
            >
            <el-button
              link
              type="primary"
              size="small"
              :icon="Edit"
              @click="activateQfModelDeployRcd(scope.row)"
              :disabled="scope.row.status !== 2"
              >激活</el-button
            >
            <el-button
              link
              type="success"
              size="small"
              :icon="VideoPlayIcon"
              @click="openLaunchModal(scope.row)"
              :disabled="scope.row.status !== 0"
              >发起</el-button
            >
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
      :title="modalMode === 'edit' ? '编辑流程模型部署历史' : '新增流程模型部署历史'"
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

    <!-- 发起流程模态框 -->
    <el-dialog
      v-model="launchVisible"
      title="发起审批流程"
      width="500px"
      :close-on-click-modal="false"
      @close="resetLaunchModal"
    >
      <el-form
        v-if="launchVisible"
        ref="launchFormRef"
        :model="launchForm"
        :rules="launchRules"
        label-width="110px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="模型编码" prop="code">
          <el-input v-model="launchForm.code" disabled />
        </el-form-item>
        <el-form-item label="业务表单编码" prop="bizFormCode">
          <el-select v-model="launchForm.bizFormCode" placeholder="选择业务表单" clearable>
            <el-option v-for="item in launchBizFormList" :key="item.id" :label="item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务数据ID" prop="dataId">
          <el-input-number
            v-model="launchForm.dataId"
            :min="1"
            :controls="false"
            style="width: 100%"
            placeholder="输入业务数据ID"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="launchVisible = false">取消</el-button>
          <el-button type="primary" @click="submitLaunchModal" :loading="launchLoading">发起</el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete, VideoPlay } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import QfModelDeployRcdService from "@/views/qf/service/QfModelDeployRcdService.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const VideoPlayIcon = markRaw(VideoPlay);

// 列表管理打包
const {
  listForm,
  listData,
  listTotal,
  listLoading,
  loadList,
  resetList,
  removeList,
  suspendQfModelDeployRcd,
  activateQfModelDeployRcd,
} = QfModelDeployRcdService.useQfModelDeployRcdList();

// 新增/编辑模态框打包
const modalFormRef = ref<FormInstance>();
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  QfModelDeployRcdService.useQfModelDeployRcdModal(modalFormRef, loadList);

// 发起流程模态框打包
const launchFormRef = ref<FormInstance>();
const {
  launchVisible,
  launchLoading,
  launchForm,
  launchRules,
  launchBizFormList,
  openLaunchModal,
  resetLaunchModal,
  submitLaunchModal,
} = QfModelDeployRcdService.useLaunchModal(launchFormRef, loadList);
</script>

<style scoped></style>
