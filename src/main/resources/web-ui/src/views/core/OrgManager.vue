<template>
  <StdListLayout>
    <template #query>
      <el-form :model="queryForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="组织机构名称">
              <el-input v-model="queryForm.name" placeholder="输入组织机构名称查询" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <!-- 占位，保持布局一致性 -->
          </el-col>
          <el-col :span="5" :offset="1">
            <!-- 占位，保持布局一致性 -->
          </el-col>
          <el-col :span="3" :offset="3">
            <el-form-item>
              <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
              <el-button :disabled="listLoading" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </template>

    <template #actions>
      <el-button type="success" @click="openModal('add', null)">创建组织机构</el-button>
    </template>

    <template #table>
      <el-table
        ref="listTableRef"
        v-loading="listLoading"
        :data="filteredData"
        stripe
        border
        row-key="id"
        default-expand-all
        height="100%"
      >
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="组织机构名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="kind" label="类型" min-width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.kind === 0" type="primary">企业</el-tag>
            <el-tag v-if="scope.row.kind === 1" type="warning">子企业</el-tag>
            <el-tag v-if="scope.row.kind === 2" type="info">部门</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" min-width="100" />
        <el-table-column prop="seq" label="排序" min-width="100">
          <template #default="scope">
            <ComSeqFixer
              :id="scope.row.id"
              :seq-field="'seq'"
              :get-detail-api="getOrgDetail"
              :edit-api="editOrgSeq"
              :display-value="scope.row.seq"
              :on-success="loadList"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="200">
          <template #default="scope">
            <el-button link type="success" size="small" :icon="PlusIcon" @click="openModal('add-item', scope.row)">
              创建子级
            </el-button>
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row.id)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </StdListLayout>

  <!-- 组织机构编辑/创建模态框 -->
  <el-dialog
    v-model="modalVisible"
    :title="modalMode === 'edit' ? '编辑' + modalKindName : modalMode === 'add-item' ? '创建子级' : '创建' + modalKindName"
    width="500px"
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
      label-width="120px"
      :validate-on-rule-change="false"
    >
      <el-form-item :label="modalKindName + '名称'" prop="name">
        <el-input v-model="modalForm.name" :placeholder="'请输入' + modalKindName + '名称'" maxlength="80" show-word-limit />
      </el-form-item>
      <el-form-item :label="modalKindName + '简称'" prop="shortName">
        <el-input
          v-model="modalForm.shortName"
          :placeholder="'请输入' + modalKindName + '简称'"
          maxlength="40"
          show-word-limit
        />
      </el-form-item>
      <el-form-item v-if="modalMode !== 'edit'" :label="modalKindName + '类型'" prop="kind">
        <el-radio-group
          v-model="modalForm.kind"
          @change="
            (e) => {
              if (e === 0) modalForm.parentId = null;
            }
          "
        >
          <el-radio :value="0" :disabled="modalMode === 'add-item'">企业</el-radio>
          <el-radio :value="1">子企业</el-radio>
          <el-radio :value="2">部门</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="[1, 2].includes(modalForm.kind)" label="上级组织" prop="parentId">
        <el-tree-select
          v-model="modalForm.parentId"
          :data="filterTreeSelectData"
          placeholder="请选择上级组织"
          clearable
          check-strictly
          :render-after-expand="true"
          :disabled="modalMode === 'add-item' && modalForm.kind === 0"
          node-key="value"
        />
      </el-form-item>
      <el-form-item :label="modalKindName + '排序'" prop="seq">
        <el-input-number v-model="modalForm.seq" :min="0" style="width: 100%" />
      </el-form-item>
      <el-form-item :label="modalKindName + '备注'" prop="remark">
        <el-input v-model="modalForm.remark" placeholder="请输入备注" type="textarea" maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="modalVisible = false">关闭</el-button>
        <el-button type="primary" :loading="modalLoading" @click="submitModal">
          {{ modalMode === "add" ? "创建" : modalMode === "add-item" ? "创建" : "保存" }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { Edit, Delete, Plus } from "@element-plus/icons-vue";
import { markRaw } from "vue";
import type { FormInstance } from "element-plus";
import OrgManagerService from "@/views/core/service/OrgManagerService.ts";
import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue";
import OrgApi from "@/views/core/api/OrgApi.ts";
import { Result } from "@/commons/model/Result";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";
import type { GetOrgDetailsVo } from "@/views/core/api/OrgApi.ts";

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const PlusIcon = markRaw(Plus);

const modalFormRef = ref<FormInstance | null>(null);

const { queryForm, listLoading, filteredData, treeSelectData, resetQuery, removeList, loadList } =
  OrgManagerService.useOrgTree();

const { modalKindName, modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  OrgManagerService.useOrgModal(modalFormRef, loadList);

const filterTreeSelectData = computed(() => {
  const treeSelect = JSON.parse(JSON.stringify(treeSelectData.value));

  const disableNode = (tree: any[], id: string): any[] => {
    return tree.map((node) => {
      if (node.value === id) {
        const disableAllChildren = (children: any[]): any[] => {
          return children.map((child) => ({
            ...child,
            disabled: true,
            children: child.children ? disableAllChildren(child.children) : undefined,
          }));
        };

        return { ...node, disabled: true, children: node.children ? disableAllChildren(node.children) : undefined };
      }
      if (node.children && node.children.length > 0) {
        return { ...node, children: disableNode(node.children, id) };
      }
      return node;
    });
  };

  // 如果当前是编辑，并且编辑的是企业(kind===0)或子企业(kind===1)，则屏蔽所有kind===2（部门）节点
  if (modalMode.value !== "add-item" && (modalForm.kind === 0 || modalForm.kind === 1)) {
    const findKind2Nodes = (nodes: any[]): any[] => {
      let kind2Nodes: any[] = [];
      nodes.forEach((node) => {
        if (node.kind === 2) {
          kind2Nodes.push(node);
        }
        if (node.children && Array.isArray(node.children) && node.children.length > 0) {
          kind2Nodes = kind2Nodes.concat(findKind2Nodes(node.children));
        }
      });
      return kind2Nodes;
    };
    const kind2Nodes = findKind2Nodes(treeSelect);
    // 遍历kind2Nodes，添加disabled：true
    kind2Nodes.forEach((node) => {
      node.disabled = true;
    });
  }

  // 如果当前是编辑，并且编辑的是部门(kind===2)，则只能选择本企业的节点
  if (modalMode.value === "edit" && modalForm.kind === 2) {
    // for (const item of treeSelect) {
    // 递归查找指定rootId的对象节点
    const findNodeById = (nodes: any[], id: string | null): any | null => {
      if (!id) {
        return null;
      }
      for (const node of nodes) {
        if (node.value === id) {
          return node;
        }
        if (node.children && Array.isArray(node.children)) {
          const found = findNodeById(node.children, id);
          if (found) {
            return found;
          }
        }
      }
      return null;
    };

    const nodeWithRootId = findNodeById(treeSelect, modalForm.id);

    // 查找某个节点的最顶级父节点
    function findTopLevelParent(nodes: any[], targetNode: any): any | null {
      // 用于递归查找父节点链
      function findParentRecursively(currentNodes: any[], id: string): any[] {
        for (const node of currentNodes) {
          if (node.value === id) {
            // 找到目标节点，直接返回自身
            return [node];
          }
          if (node.children && Array.isArray(node.children)) {
            const path = findParentRecursively(node.children, id);
            if (path.length > 0) {
              // 递归返回的路径上添加当前节点（父级）
              return [node, ...path];
            }
          }
        }
        return [];
      }
      if (!targetNode) {
        return null;
      }
      const path = findParentRecursively(nodes, targetNode.value);
      if (path.length > 0) {
        return path[0]; // 最顶上的父级对象
      }
      return null;
    }

    const topLevelParent = findTopLevelParent(treeSelect, nodeWithRootId);

    const filteredItems = treeSelect.filter((item) => item.value !== topLevelParent?.value);

    // 提取filteredItems数组中第一层的value，合并成一个数组并打印出来
    const firstLevelValues = filteredItems.map((item) => item.value);
    let result = treeSelect;
    firstLevelValues.forEach((id) => {
      result = disableNode(result, id);
    });
    return result;
  }

  return treeSelect;
});

const getOrgDetail = async (id: string): Promise<GetOrgDetailsVo> => {
  const result = await OrgApi.getOrgDetails({ id });
  if (!result) {
    throw new Error("获取数据失败");
  }
  return result;
};

const editOrgSeq = async (id: string, dto: any): Promise<void> => {
  const result = await OrgApi.editOrg(dto);
  if (!Result.isSuccess(result)) {
    throw new Error(result.message);
  }
};
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: var(--el-color-info);
  margin-top: 5px;
}
</style>
