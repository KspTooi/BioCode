<template>
  <StdListLayout show-persist-tip :has-tutorial="true">
    <!-- 文档说明 -->
    <template #tutorial>
      <el-alert type="info" :closable="false" class="mb-4">
        <template #title>
          <div class="flex items-center gap-2">
            <el-icon><InfoFilled /></el-icon>
            <span class="font-bold">权限节点缺失指示器</span>
          </div>
        </template>
        <div class="text-sm leading-relaxed">
          <div>
            <span class="text-green-500 font-bold">● 绿色</span>：权限完整，所有权限节点已在系统中定义
            <span class="ml-4 text-orange-500 font-bold">● 橙色</span>：部分缺失，部分权限节点未在系统中定义
            <span class="ml-4 text-red-500 font-bold">● 红色</span>：完全缺失，所有权限节点均未在系统中定义
          </div>
        </div>
      </el-alert>
    </template>

    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="菜单名称">
              <el-input v-model="listForm.name" placeholder="请输入菜单名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="所需权限">
              <el-input v-model="listForm.permissionCode" placeholder="请输入所需权限" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="菜单类型">
              <el-select v-model="listForm.kind" placeholder="请选择菜单类型" clearable>
                <el-option label="目录" value="0" />
                <el-option label="菜单" value="1" />
                <el-option label="按钮" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="3" :offset="3">
            <el-form-item>
              <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
              <el-button :disabled="listLoading" @click="resetList">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </template>

    <template #actions>
      <el-button type="success" @click="openModal('add', null)">创建菜单</el-button>
      <el-button type="primary" @click="listExpandToggle()"> {{ listExpand ? "收起全部" : "展开全部" }} </el-button>
    </template>

    <template #table>
      <el-table
        ref="listTableRef"
        v-loading="listLoading"
        :data="listData"
        border
        row-key="id"
        default-expand-all
        height="100%"
      >
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column label="菜单名称" prop="name" show-overflow-tooltip width="360">
          <template #default="scope">
            <div class="inline-flex items-center gap-2">
              <Icon v-if="scope.row.icon" :icon="scope.row.icon" :width="16" :height="16" class="align-middle inline" />
              {{ scope.row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="kind" width="70">
          <template #default="scope">
            <el-tag v-if="scope.row.kind === 0">目录</el-tag>
            <el-tag v-if="scope.row.kind === 1" type="success">菜单</el-tag>
            <el-tag v-if="scope.row.kind === 2" type="info">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="菜单路径" prop="path" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.kind === 0 || scope.row.kind === 2" class="text-gray-400 text-xs">不适用</span>
            <span v-else>{{ scope.row.path }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所需权限" prop="permissionCode" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.kind === 0" class="text-gray-400 text-xs">不适用</span>
            <span
              v-else
              :class="{
                'text-red-500': scope.row.missingPermission === 1,
                'text-orange-500': scope.row.missingPermission === 2,
                'text-green-500': scope.row.missingPermission === 0,
              }"
            >
              {{ scope.row.permissionCode }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="seq" width="100">
          <template #default="scope">
            <ComSeqFixer
              :id="scope.row.id"
              :seq-field="'seq'"
              :get-detail-api="getMenuDetail"
              :edit-api="editMenuSeq"
              :display-value="scope.row.seq"
              :on-success="loadList"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="hide" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.hide === 1 ? 'danger' : 'success'">
              {{ scope.row.hide === 1 ? "隐藏" : "正常" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="230">
          <template #default="scope">
            <div class="inline-flex justify-end items-center gap-2 w-full">
              <!-- 按钮下无法创建子项 -->
              <el-button
                v-if="scope.row.kind !== 2"
                link
                type="success"
                size="small"
                :icon="PlusIcon"
                @click="openModal('add-item', scope.row)"
              >
                创建子项
              </el-button>

              <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
                编辑
              </el-button>
              <el-button
                link
                type="danger"
                size="small"
                :icon="DeleteIcon"
                :disabled="scope.row.children && scope.row.children.length > 0"
                @click="removeList(scope.row.id)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </StdListLayout>

  <!-- 选择路由模态框 -->
  <GenricRouteChooseModal ref="grcmRef" v-model="modalForm.path" v-model:search-keyword="grcmQuery" />

  <!-- 菜单编辑模态框 -->
  <el-dialog
    v-model="modalVisible"
    :title="modalMode === 'edit' ? '编辑' + modalFormLabel : modalMode === 'add' ? '创建' + modalFormLabel : '创建子项' + modalFormLabel"
    width="550px"
    :close-on-click-modal="false"
    @close="
      resetModal(true);
      loadList();
    "
  >
    <el-form
      v-if="modalVisible"
      ref="modalFormRef"
      :model="modalForm"
      :rules="modalRules"
      label-width="80px"
      :validate-on-rule-change="false"
    >
      <el-form-item label="父级菜单" prop="parentId">
        <el-tree-select
          v-model="modalForm.parentId"
          :data="menuTreeForSelect"
          node-key="id"
          :props="{ value: 'id', label: 'name', children: 'children' }"
          check-strictly
          placeholder="请选择父级菜单"
          clearable
          default-expand-all
        />
      </el-form-item>
      <el-form-item label="菜单类型" prop="kind">
        <el-select v-model="modalForm.kind" placeholder="请选择菜单类型" clearable :disabled="modalMode === 'edit'">
          <el-option label="目录" :value="0" :disabled="modalMode === 'add-item' && modalCurrentRow?.kind == 1" />
          <el-option label="菜单" :value="1" :disabled="modalMode === 'add-item' && modalCurrentRow?.kind == 1" />
          <el-option label="按钮" :value="2" :disabled="modalMode === 'add-item' && modalCurrentRow?.kind == 0" />
        </el-select>
      </el-form-item>
      <el-form-item :label="modalFormLabel + '名称'" prop="name">
        <el-input v-model="modalForm.name" placeholder="请输入菜单名称" clearable maxlength="32" show-word-limit />
      </el-form-item>
      <el-form-item v-if="modalForm.kind == 1" :label="modalFormLabel + '路径'" prop="path">
        <el-input v-model="modalForm.path" placeholder="请输入菜单路径" clearable maxlength="500" show-word-limit>
          <template #append>
            <el-button @click="openGRCM">选择路由</el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="modalForm.kind == 1 || modalForm.kind == 2" label="所需权限" prop="permissionCode">
        <el-input v-model="modalForm.permissionCode" placeholder="请输入所需权限" clearable maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item v-if="modalForm.kind == 0 || modalForm.kind == 1" :label="modalFormLabel + '图标'" prop="icon">
        <StdIconPicker v-model="modalForm.icon" />
      </el-form-item>
      <el-form-item label="状态" prop="hide">
        <el-radio-group v-model="modalForm.hide">
          <el-radio :value="0">正常</el-radio>
          <el-radio :value="1">隐藏</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序" prop="seq">
        <el-input-number v-model.number="modalForm.seq" :min="0" placeholder="请输入排序" clearable />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="modalForm.remark" placeholder="请输入备注" clearable maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="modalVisible = false">关闭</el-button>
        <el-button type="primary" :loading="modalLoading" @click="submitModal">
          {{ modalMode === "edit" ? "保存" : "创建" }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { FormInstance, TableInstance } from "element-plus";
import { ref } from "vue";
import { Delete as DeleteIcon, Plus as PlusIcon, InfoFilled, Edit as EditIcon } from "@element-plus/icons-vue";
import StdIconPicker from "@/soa/std-series/StdIconPicker.vue";
import { Icon } from "@iconify/vue";
import type { GetMenuDetailsVo } from "@/views/core/api/MenuApi.ts";
import MenuManagerService from "@/views/core/service/MenuManagerService.ts";
import GenricRouteChooseModal from "@/soa/genric-route/GenricRouteChooseModal.vue";
import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue";
import MenuApi from "@/views/core/api/MenuApi.ts";
import { Result } from "@/commons/model/Result";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";
import ComMenuService from "@/soa/com-series/service/ComMenuService.ts";

const grcmRef = ref<InstanceType<typeof GenricRouteChooseModal>>();

//路由选择模态框查询参数
const grcmQuery = ref<string>("");

const openGRCM = (): void => {
  grcmRef.value?.openModal();
};

const listTableRef = ref<TableInstance>();
const modalFormRef = ref<FormInstance>();

/**
 * 菜单列表打包
 */
const {
  listForm,
  listExpand,
  listData,
  listLoading,
  fullMenuTree,
  loadList,
  resetList,
  removeList,
  loadFullMenuTree,
  listExpandToggle,
} = MenuManagerService.useMenuList(listTableRef);

/**
 * 菜单模态框打包
 */
const {
  modalVisible,
  modalLoading,
  modalMode,
  modalCurrentRow,
  modalForm,
  modalFormLabel,
  modalRules,
  menuTreeForSelect,
  openModal,
  resetModal,
  submitModal,
} = MenuManagerService.useMenuModal(modalFormRef, loadList, fullMenuTree, loadFullMenuTree);

//先加载菜单服务
const { loadMenus } = ComMenuService.useMenuService();

const getMenuDetail = async (id: string): Promise<GetMenuDetailsVo> => {
  const result = await MenuApi.getMenuDetails({ id });
  if (!Result.isSuccess(result)) {
    throw new Error(result.message);
  }
  return result.data;
};

const editMenuSeq = async (id: string, dto: any): Promise<void> => {
  const result = await MenuApi.editMenu(dto);
  if (!Result.isSuccess(result)) {
    throw new Error(result.message);
  }
  //通知左侧菜单重新加载
  loadMenus();
};
</script>

<style scoped></style>
