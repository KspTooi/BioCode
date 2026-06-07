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
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="模型名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="模型编码" min-width="120" show-overflow-tooltip />
        <el-table-column label="业务表单" min-width="140" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.bizFormName && scope.row.bizFormCode"
              >{{ scope.row.bizFormName }}({{ scope.row.bizFormCode }})</span
            >
            <span v-else>-</span>
          </template>
        </el-table-column>
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
              :disabled="scope.row.status !== 0"
              @click="suspendQfModelDeployRcd(scope.row)"
            >
              挂起
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              :icon="Edit"
              :disabled="scope.row.status !== 2"
              @click="activateQfModelDeployRcd(scope.row)"
            >
              激活
            </el-button>
            <el-button
              link
              type="success"
              size="small"
              :icon="VideoPlayIcon"
              :disabled="scope.row.status !== 0"
              @click="openLaunchModal(scope.row)"
            >
              发起
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
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
          <el-button type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 发起流程模态框 -->
    <el-dialog
      v-model="launchVisible"
      title="发起审批流程"
      width="600px"
      :close-on-click-modal="false"
      @close="resetLaunchModal"
    >
      <div class="launch-modal-body">
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
          <el-form-item label="业务数据ID" prop="dataId">
            <el-input v-model="launchForm.dataId" placeholder="输入业务数据ID" />
          </el-form-item>
        </el-form>

        <el-divider content-position="left">审批流程预览</el-divider>

        <QfProcDefine v-if="launchVisible && launchForm.code" :code="launchForm.code" v-model="launchForm.members" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="launchVisible = false">取消</el-button>
          <el-button type="primary" :loading="launchLoading" @click="submitLaunchModal">发起</el-button>
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
import QfProcDefine from "@/views/qf/public/QfProcDefine.vue";

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
const { launchVisible, launchLoading, launchForm, launchRules, openLaunchModal, resetLaunchModal, submitLaunchModal } =
  QfModelDeployRcdService.useLaunchModal(launchFormRef, loadList);
</script>

<style scoped>
.launch-modal-body {
  max-height: 60vh;
  overflow-y: auto;
}
</style>
