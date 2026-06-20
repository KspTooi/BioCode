<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="SCM名称">
            <el-input v-model="listForm.name" placeholder="输入SCM名称" clearable />
          </el-form-item>
          <el-form-item label="项目名称">
            <el-input v-model="listForm.projectName" placeholder="输入项目名称" clearable />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
          <el-button :disabled="listLoading" @click="resetList">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction class="flex gap-2">
      <el-button type="success" @click="openModal('add', null)">新增SCM</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column label="序号" min-width="60" align="center">
          <template #default="scope">
            {{ (listForm.pageNum - 1) * listForm.pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="SCM名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="projectName" label="项目名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="scmUrl" label="SCM仓库地址" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="100" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="300">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="primary" size="small" :icon="CopyIcon" @click="openModal('copy', scope.row)"> 复制 </el-button>
            <el-button link type="success" size="small" :icon="TestIcon" @click="testScmConnection(scope.row)">
              测试SCM连接
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
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
    </StdListAreaTable>

    <!-- 新增/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'edit' ? '编辑SCM' : modalMode === 'copy' ? '复制SCM' : '新增SCM'"
      width="640px"
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
        :validate-on-rule-change="true"
      >
        <el-form-item label="SCM名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入SCM名称" clearable maxlength="32" show-word-limit />
        </el-form-item>
        <el-form-item label="项目名称" prop="projectName" v-if="modalMode !== 'copy'">
          <el-input v-model="modalForm.projectName" placeholder="请输入项目名称" clearable maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="SCM类型" v-if="modalMode !== 'copy'">
          <el-select model-value="git" disabled style="width: 100%">
            <el-option label="Git" value="git" />
          </el-select>
        </el-form-item>
        <el-form-item label="SCM仓库地址" prop="scmUrl" v-if="modalMode !== 'copy'">
          <el-input v-model="modalForm.scmUrl" placeholder="请输入SCM仓库地址" clearable maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="SCM认证方式" prop="scmAuthKind" v-if="modalMode !== 'copy'">
          <el-select v-model="modalForm.scmAuthKind" placeholder="请选择SCM认证方式" style="width: 100%">
            <el-option label="公开" :value="0" />
            <el-option label="账号密码" :value="1" />
            <el-option label="SSH KEY" :value="2" />
            <el-option label="PAT" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="(modalForm.scmAuthKind === 1 || modalForm.scmAuthKind === 3) && modalMode !== 'copy'" label="SCM用户名" prop="scmUsername">
          <el-input v-model="modalForm.scmUsername" placeholder="请输入SCM用户名" clearable maxlength="10000" show-word-limit />
        </el-form-item>
        <el-form-item v-if="(modalForm.scmAuthKind === 1 || modalForm.scmAuthKind === 3) && modalMode !== 'copy'" label="SCM密码" prop="scmPassword">
          <el-input
            v-model="modalForm.scmPassword"
            :placeholder="modalForm.scmAuthKind === 3 ? '请输入PAT令牌' : '请输入SCM密码'"
            clearable
            show-password
            maxlength="10000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="modalForm.scmAuthKind === 2 && modalMode !== 'copy'" label="SSH KEY" prop="scmPk">
          <el-input
            v-model="modalForm.scmPk"
            placeholder="SSH私钥(-----BEGIN OPENSSH PRIVATE KEY-----开头)"
            type="textarea"
            :rows="3"
            maxlength="10000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="SCM分支" prop="scmBranch" v-if="modalMode !== 'copy'">
          <el-input v-model="modalForm.scmBranch" placeholder="请输入SCM分支" clearable maxlength="80" show-word-limit />
        </el-form-item>
        <el-form-item label="SCM备注" prop="remark" v-if="modalMode !== 'copy'">
          <el-input
            v-model="modalForm.remark"
            placeholder="请输入SCM备注"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">取消</el-button>
          <el-button type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" || modalMode === "copy" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete, Connection, CopyDocument } from "@element-plus/icons-vue";
import { type FormInstance } from "element-plus";
import ScmService from "@/views/assembly/service/ScmService";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const TestIcon = markRaw(Connection);
const CopyIcon = markRaw(CopyDocument);

//列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList, testScmConnection } =
  ScmService.useScmList();

const modalFormRef = ref<FormInstance>();

//模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  ScmService.useScmModal(modalFormRef, loadList);
</script>

<style scoped></style>
