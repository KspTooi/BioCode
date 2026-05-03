<template>
  <div class="list-layout">
    <splitpanes class="custom-theme">
      <!-- 左侧树形列表 -->
      <pane size="20" min-size="10" max-size="40">
        <div class="h-full flex flex-col pt-2 px-1">
          <RegistryNodeTree @on-select="onSelectNode" />
        </div>
      </pane>

      <!-- 右侧内容区 -->
      <pane size="80">
        <StdListContainer>
          <StdListAreaQuery>
            <el-form :model="listForm" inline class="flex justify-between">
              <div>
                <el-form-item label="条目Key">
                  <el-input v-model="listForm.nkey" placeholder="输入条目Key" clearable @keyup.enter="onSearch" />
                </el-form-item>
                <el-form-item label="标签">
                  <el-input v-model="listForm.label" placeholder="输入标签" clearable @keyup.enter="onSearch" />
                </el-form-item>
              </div>
              <el-form-item>
                <el-dropdown split-button type="primary" :disabled="listLoading || !currentKeyPath" @click="onSearch">
                  查询
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :icon="DownloadIcon" @click="onExport">导出查询结果</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button :disabled="listLoading" style="margin-left: 12px" @click="resetList(currentKeyPath)">重置</el-button>
              </el-form-item>
            </el-form>
          </StdListAreaQuery>

          <StdListAreaAction>
            <el-button type="success" :disabled="!currentKeyPath" @click="openModal('add', null, currentNodeId)">
              创建条目
            </el-button>
            <el-button type="danger" :disabled="!currentKeyPath || listSelected.length === 0" @click="removeListBatch">
              批量删除
            </el-button>
            <el-button
              type="primary"
              :disabled="!currentKeyPath"
              :icon="UploadIcon"
              @click="importWizardRef?.openModal({ keyPath: currentKeyPath })"
            >
              导入条目
            </el-button>
          </StdListAreaAction>

          <StdListAreaTable>
            <el-table
              v-loading="listLoading"
              :data="listData"
              stripe
              border
              height="100%"
              @selection-change="onSelectionChange"
            >
              <el-table-column type="selection" width="40" />
              <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
              <el-table-column prop="nkey" label="Key" min-width="150" show-overflow-tooltip />
              <el-table-column
                prop="label"
                label="标签"
                min-width="150"
                show-overflow-tooltip
                :formatter="(_, __, value) => value ?? '-'"
              />
              <el-table-column label="数据类型" width="100">
                <template #default="scope">
                  <el-tag v-if="scope.row.nvalueKind === 0" size="small">字符串</el-tag>
                  <el-tag v-if="scope.row.nvalueKind === 1" size="small" type="success">整数</el-tag>
                  <el-tag v-if="scope.row.nvalueKind === 2" size="small" type="warning">浮点</el-tag>
                  <el-tag v-if="scope.row.nvalueKind === 3" size="small" type="info">日期</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="nvalue" label="数据值" min-width="200" show-overflow-tooltip />
              <el-table-column
                prop="remark"
                label="说明"
                min-width="150"
                show-overflow-tooltip
                :formatter="(_, __, value) => value ?? '-'"
              />
              <el-table-column label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
                    {{ scope.row.status === 0 ? "正常" : "停用" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" min-width="170" />
              <el-table-column label="操作" fixed="right" width="140">
                <template #default="scope">
                  <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
                    编辑
                  </el-button>
                  <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)">
                    删除
                  </el-button>
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
                background
                @size-change="
                  (val: number) => {
                    listForm.pageSize = val;
                    loadList(currentKeyPath);
                  }
                "
                @current-change="
                  (val: number) => {
                    listForm.pageNum = val;
                    loadList(currentKeyPath);
                  }
                "
              />
            </template>
          </StdListAreaTable>
        </StdListContainer>
      </pane>
    </splitpanes>

    <!-- 导入向导 -->
    <ImportWizardModal
      ref="importWizardRef"
      url="/registry/importRegistry"
      template-code="core_registry"
      @on-success="loadList(currentKeyPath)"
      @on-close="loadList(currentKeyPath)"
    />

    <!-- 注册表编辑/创建模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑条目' : '创建条目'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form v-if="modalVisible" ref="modalFormRef" :model="modalForm" :rules="modalRules" label-width="100px">
        <el-form-item label="Key" prop="nkey">
          <el-input
            v-model="modalForm.nkey"
            placeholder="请输入Key（字母、数字、下划线或中划线）"
            maxlength="128"
            show-word-limit
            :disabled="modalMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="标签" prop="label">
          <el-input v-model="modalForm.label" placeholder="请输入标签" maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="数据类型" prop="nvalueKind">
          <el-select v-model="modalForm.nvalueKind" placeholder="选择类型" style="width: 100%" :disabled="modalMode === 'edit'">
            <el-option label="字符串" :value="0" />
            <el-option label="整数" :value="1" />
            <el-option label="浮点" :value="2" />
            <el-option label="日期(yyyy-MM-dd HH:mm:ss)" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据值" prop="nvalue">
          <el-input
            v-model="modalForm.nvalue"
            type="textarea"
            :rows="3"
            placeholder="请输入数据值"
            maxlength="1024"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input-number v-model="modalForm.seq" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modalForm.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="说明" prop="remark">
          <el-input v-model="modalForm.remark" type="textarea" placeholder="请输入说明" maxlength="1000" show-word-limit />
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
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete, Upload, Download } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";
import RegistryManagerService from "@/views/core/service/RegistryManagerService.ts";
import RegistryApi, { type GetRegistryNodeTreeVo } from "@/views/core/api/RegistryApi";
import RegistryNodeTree from "@/views/core/components/RegistryNodeTree.vue";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import ImportWizardModal from "@/soa/com-series/ImportWizardModal.vue";
import { ElMessage } from "element-plus";

// 静态图标引用 (使用 markRaw 避免响应式开销)
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const UploadIcon = markRaw(Upload);
const DownloadIcon = markRaw(Download);

// 导入向导引用
const importWizardRef = ref<InstanceType<typeof ImportWizardModal>>();

// 组件状态管理
const currentKeyPath = ref<string | null>(null); // 当前选中的树节点路径
const currentNodeId = ref<string | null>(null); // 当前选中的树节点 ID

/**
 * 处理树节点选中事件
 * @param node 树形组件传回的节点对象
 */
const onSelectNode = (node: GetRegistryNodeTreeVo | null): void => {
  currentKeyPath.value = node?.keyPath ?? null;
  currentNodeId.value = node?.id ?? null;
  resetList(currentKeyPath.value); // 选中节点后重置并刷新列表
};

// 注册表条目列表打包
const {
  listForm,
  listData,
  listTotal,
  listLoading,
  loadList,
  resetList,
  removeList,
  removeListBatch,
  onSelectionChange,
  listSelected,
} = RegistryManagerService.useRegistryList(currentKeyPath);

const modalFormRef = ref<FormInstance>();
const _loadList = (): Promise<void> => loadList(currentKeyPath.value);

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, submitModal } =
  RegistryManagerService.useRegistryModal(modalFormRef, _loadList);

/**
 * 触发查询动作
 */
const onSearch = (): void => {
  listForm.value.pageNum = 1; // 重置为第一页
  loadList(currentKeyPath.value);
};

/**
 * 导出查询结果
 */
const onExport = async (): Promise<void> => {
  if (!currentKeyPath.value) {
    ElMessage.warning("请先选择节点");
    return;
  }

  try {
    await RegistryApi.exportRegistry(listForm.value);
  } catch (e: any) {
    console.error(e);
    ElMessage.error("导出失败");
  }
};
</script>

<style scoped>
.list-layout {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color);
}

:deep(.splitpanes.custom-theme) {
  border: none;
}

:deep(.splitpanes.custom-theme .splitpanes__pane) {
  background-color: transparent;
}

:deep(.splitpanes.custom-theme .splitpanes__splitter) {
  background-color: var(--el-border-color-extra-light);
  width: 1px;
  border: none;
  cursor: col-resize;
  position: relative;
  transition: background-color 0.2s;
}

:deep(.splitpanes.custom-theme .splitpanes__splitter:hover) {
  background-color: var(--el-color-primary);
  width: 3px;
}

:deep(.splitpanes.custom-theme .splitpanes__splitter:after) {
  content: "";
  position: absolute;
  left: -5px;
  right: -5px;
  top: 0;
  bottom: 0;
  z-index: 1;
}

:deep(.splitpanes__pane) {
  transition: none !important;
}
</style>
