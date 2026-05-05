<template>
  <div class="list-layout">
    <splitpanes class="custom-theme">
      <!-- 左侧菜单树 -->
      <pane max-size="55" style="min-width: 375px">
        <div class="left-pane">
          <div class="tree-area">
            <StdAdvTree
              v-model="treeCurrent"
              :data="treeData"
              :init-value="treeCurrent"
              :loading="treeLoading"
              :check="false"
              :nr="true"
              :nr-title="'根菜单'"
              :nr-icon="'ep:menu'"
              :nr-value="'-1'"
              ni="icon"
              :nk="'id'"
              :nt="'name'"
              :nc="'children'"
              :search="true"
              :search-placeholder="'请输入菜单名称'"
              :search-refresh="true"
              :expand-on-click="true"
              @on-refresh="loadTree"
              @on-select="openPanel('edit', $event)"
              @on-add="openPanel('add-item', $event)"
              @on-edit="openPanel('edit', $event)"
              @on-remove="removeNode($event.id)"
              @on-root-select="closePanel()"
            >
              <template #label="{ data: nodeData }">
                <span v-show="nodeData.hide == 0">{{ nodeData.name }}</span>
                <span v-show="nodeData.hide == 1" class="line-through">{{ nodeData.name }}</span>
              </template>
              <template #root-actions>
                <el-button link type="success" size="small" :icon="PlusIcon" @click="openPanel('add', null)">
                  创建菜单
                </el-button>
              </template>
              <template #append="{ data }">
                <el-tag v-if="data.kind === 0" size="small" type="info" class="menu-kind-tag">目录</el-tag>
                <el-tag v-if="data.kind === 1" size="small" type="success" class="menu-kind-tag">菜单</el-tag>
                <el-tag v-if="data.kind === 2" size="small" class="menu-kind-tag">按钮</el-tag>
              </template>
              <template #actions="{ data }">
                <el-button
                  v-show="data.kind !== 2"
                  link
                  type="success"
                  size="small"
                  :icon="PlusIcon"
                  @click="openPanel('add-item', data)"
                >
                  创建
                </el-button>
                <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeNode(data.id)">删除</el-button>
              </template>
            </StdAdvTree>
          </div>
        </div>
      </pane>

      <!-- 右侧详情面板 -->
      <pane size="75">
        <div class="right-pane">
          <el-skeleton v-show="panelLoading" :rows="14" animated class="panel-skeleton" />

          <div v-show="!panelLoading && panelVisible">
            <!-- 顶部装饰线 -->
            <div class="panel-accent-bar" :class="`accent-${panelForm.kind}`" />

            <!-- 顶部标题条 -->
            <div class="panel-header">
              <div class="panel-header-left">
                <div class="panel-header-icon-shell" :class="`icon-${panelForm.kind}`">
                  <el-icon class="panel-header-icon">
                    <component :is="resolveIcon(panelForm.icon || 'ep:menu')" />
                  </el-icon>
                </div>
                <div class="panel-header-titles">
                  <div class="panel-title">
                    {{
                      panelMode === "edit"
                        ? "编辑" + panelFormLabel
                        : panelMode === "add"
                          ? "创建" + panelFormLabel
                          : "创建子项" + panelFormLabel
                    }}
                    <span v-if="panelMode === 'edit' && panelForm.name" class="panel-title-name"> · {{ panelForm.name }} </span>
                  </div>
                  <div class="panel-breadcrumb">{{ panelBreadcrumb.join(" / ") }}</div>
                </div>
              </div>
              <div class="panel-header-right">
                <el-button @click="closePanel">关闭</el-button>
                <el-button type="primary" :loading="panelLoading" @click="submitPanel">
                  {{ panelMode === "edit" ? "保存" : "创建" }}
                </el-button>
              </div>
            </div>

            <!-- 表单内容区 -->
            <el-scrollbar class="panel-body">
              <el-form
                ref="panelFormRef"
                :model="panelForm"
                :rules="panelRules"
                label-width="90px"
                :validate-on-rule-change="false"
                class="panel-form"
              >
                <section class="panel-section">
                  <div class="panel-section-title">{{ panelMode === "edit" ? "编辑" : "创建" }}{{ panelFormLabel }}</div>
                  <el-form-item label="父级菜单" prop="parentId">
                    <el-tree-select
                      v-model="panelForm.parentId"
                      :data="panelParentMenuTree"
                      node-key="id"
                      :props="{ value: 'id', label: 'name', children: 'children' }"
                      check-strictly
                      placeholder="请选择父级菜单"
                      clearable
                      default-expand-all
                    />
                  </el-form-item>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item :label="panelFormLabel + '名称'" prop="name">
                        <el-input
                          v-model="panelForm.name"
                          placeholder="请输入菜单名称"
                          clearable
                          maxlength="40"
                          show-word-limit
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="菜单类型" prop="kind">
                        <el-select
                          v-model="panelForm.kind"
                          placeholder="请选择菜单类型"
                          clearable
                          :disabled="panelMode === 'edit'"
                        >
                          <el-option
                            label="目录"
                            :value="0"
                            :disabled="panelMode === 'add-item' && panelCurrentRow?.kind == 1"
                          />
                          <el-option
                            label="菜单"
                            :value="1"
                            :disabled="panelMode === 'add-item' && panelCurrentRow?.kind == 1"
                          />
                          <el-option
                            label="按钮"
                            :value="2"
                            :disabled="panelMode === 'add-item' && panelCurrentRow?.kind == 0"
                          />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item v-if="panelForm.kind == 1" :label="panelFormLabel + '路径'" prop="path">
                    <el-input v-model="panelForm.path" placeholder="请输入菜单路径" clearable maxlength="512" show-word-limit>
                      <template #append>
                        <el-button @click="openGRCM">选择路由</el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                  <el-form-item v-if="panelForm.kind == 1 || panelForm.kind == 2" label="所需权限" prop="permissionCode">
                    <el-select
                      v-model="panelForm.permissionCode"
                      multiple
                      filterable
                      allow-create
                      default-first-option
                      placeholder="请选择或输入所需权限"
                      clearable
                    >
                      <el-option
                        v-for="item in panelPermissionCodes"
                        :key="item.code"
                        :label="`${item.name} (${item.code})`"
                        :value="item.code"
                      />
                    </el-select>
                    <div class="panel-permission-hint">菜单中的权限码发生变更后，已拥有该菜单的用户组需重新登录方可生效。</div>
                  </el-form-item>
                  <el-form-item v-if="panelForm.kind == 0 || panelForm.kind == 1" :label="panelFormLabel + '图标'" prop="icon">
                    <StdIconPicker v-model="panelForm.icon" />
                  </el-form-item>
                  <div v-if="panelForm.kind == 2" class="panel-section-empty">按钮类型无需配置路径与图标</div>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="状态" prop="hide">
                        <el-radio-group v-model="panelForm.hide" :disabled="panelForm.kind === 2">
                          <el-radio :value="0">正常</el-radio>
                          <el-radio :value="1">隐藏</el-radio>
                        </el-radio-group>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="排序" prop="seq">
                        <el-input-number v-model.number="panelForm.seq" :min="0" placeholder="请输入排序" clearable />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item label="备注" prop="remark">
                    <el-input
                      v-model="panelForm.remark"
                      type="textarea"
                      :rows="3"
                      placeholder="请输入备注"
                      clearable
                      maxlength="200"
                      show-word-limit
                    />
                  </el-form-item>
                </section>
              </el-form>
            </el-scrollbar>
          </div>

          <div v-show="!panelVisible" class="panel-empty">
            <el-icon class="panel-empty-icon"><Menu /></el-icon>
            <p class="panel-empty-title">暂无选中菜单</p>
            <p class="panel-empty-desc">在左侧选择菜单查看详情，或点击下方按钮快速创建</p>
            <el-button type="primary" :icon="PlusIcon" @click="openPanel('add', null)">创建菜单</el-button>
          </div>
        </div>
      </pane>
    </splitpanes>

    <!-- 选择路由模态框 -->
    <GenricRouteChooseModal ref="grcmRef" v-model="panelForm.path" v-model:search-keyword="grcmQuery" />
  </div>
</template>

<script setup lang="ts">
import type { FormInstance } from "element-plus";
import { markRaw, ref } from "vue";
import { Delete, Plus, Menu } from "@element-plus/icons-vue";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";
import StdIconPicker from "@/soa/std-series/StdIconPicker.vue";
import MenuManagerService from "@/views/core/service/MenuManagerService.ts";
import GenricRouteChooseModal from "@/soa/genric-route/GenricRouteChooseModal.vue";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";
import ComIconService from "@/soa/com-series/service/ComIconService";

const DeleteIcon = markRaw(Delete);
const PlusIcon = markRaw(Plus);

const { resolveIcon } = ComIconService.useIconService();

const grcmRef = ref<InstanceType<typeof GenricRouteChooseModal>>();
const grcmQuery = ref<string>("");
const openGRCM = (): void => {
  grcmRef.value?.openModal();
};

const panelFormRef = ref<FormInstance>();

const { treeData, treeLoading, treeCurrent, loadTree, removeNode } = MenuManagerService.useMenuTree();

const {
  panelVisible,
  panelLoading,
  panelMode,
  panelCurrentRow,
  panelForm,
  panelPermissionCodes,
  panelFormLabel,
  panelBreadcrumb,
  panelRules,
  panelParentMenuTree,
  openPanel,
  closePanel,
  submitPanel,
} = MenuManagerService.useMenuTreePanel(panelFormRef, loadTree);
</script>

<style scoped>
.list-layout {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color);
}

/* splitpanes 主题 */
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

/* 左侧面板 */
.left-pane {
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.tree-area {
  flex: 1;
  min-height: 0;
}

/* ---- 右侧面板 ---- */
.right-pane {
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  background: var(--el-bg-color);
  position: relative;
}

/* ---- 顶部色条（按菜单类型着色） ---- */
.panel-accent-bar {
  flex-shrink: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--el-color-primary), var(--el-color-primary-light-5), transparent);
  opacity: 0.85;
}
.panel-accent-bar.accent-0 {
  background: linear-gradient(90deg, var(--el-color-info), var(--el-color-info-light-5), transparent);
}
.panel-accent-bar.accent-1 {
  background: linear-gradient(90deg, var(--el-color-success), var(--el-color-success-light-5), transparent);
}

/* ---- 顶部标题条 ---- */
.panel-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: linear-gradient(180deg, var(--el-fill-color-lighter) 0%, var(--el-bg-color) 100%);
}

.panel-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.panel-header-icon-shell {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  background: var(--el-color-primary-light-9);
}
.panel-header-icon-shell.icon-0 {
  background: var(--el-color-info-light-9);
}
.panel-header-icon-shell.icon-1 {
  background: var(--el-color-success-light-9);
}
.panel-header-icon-shell.icon-2 {
  background: var(--el-color-primary-light-9);
}

.panel-header-icon {
  font-size: 22px;
  color: var(--el-color-primary);
}
.panel-header-icon-shell.icon-0 .panel-header-icon {
  color: var(--el-color-info);
}
.panel-header-icon-shell.icon-1 .panel-header-icon {
  color: var(--el-color-success);
}

.panel-header-titles {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.panel-title-name {
  font-weight: 400;
  color: var(--el-text-color-secondary);
}

.panel-breadcrumb {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.panel-header-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  margin-left: 16px;
}

/* ---- 内容区 ---- */
.panel-body {
  flex: 1;
  min-height: 0;
}

.panel-form {
  padding: 20px 28px 8px;
}

.panel-form :deep(.el-select) {
  width: 100%;
}

.panel-section {
  margin-bottom: 0;
}

.panel-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  padding-bottom: 12px;
  margin-bottom: 18px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.panel-skeleton {
  padding: 24px 28px;
}

.panel-permission-hint {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-top: 6px;
  line-height: 1.4;
}

.panel-section-empty {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  padding: 4px 0 8px;
}

/* ---- 空状态 ---- */
.panel-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-placeholder);
  gap: 10px;
  background: radial-gradient(circle at 50% 35%, var(--el-color-primary-light-9) 0%, transparent 60%), var(--el-bg-color);
}

.panel-empty-icon {
  font-size: 56px;
  opacity: 0.15;
  color: var(--el-color-primary);
}

.panel-empty-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  margin: 0;
}

.panel-empty-desc {
  font-size: 13px;
  color: var(--el-text-color-placeholder);
  margin: 0;
}

/* 菜单类型标签 */
.menu-kind-tag {
  flex-shrink: 0;
  margin-left: 6px;
  font-size: 11px;
  padding: 0 4px;
  height: 18px;
  line-height: 18px;
  border-radius: 3px;
  opacity: 0.85;
}
</style>
