<template>
  <el-dialog
    v-model="visible"
    title="用户列表"
    width="900px"
    :close-on-click-modal="false"
    class="modal-centered"
    @close="onClose"
  >
    <!-- 查询表单 -->
    <div class="query-form">
      <el-form :model="queryForm">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="用户名">
              <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable @clear="loadList" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用户状态">
              <el-select v-model="queryForm.status" placeholder="请选择用户状态" clearable @clear="loadList">
                <el-option label="正常" :value="1" />
                <el-option label="封禁" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
              <el-button :disabled="listLoading" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <div class="user-table">
      <el-table
        v-loading="listLoading"
        :data="listData"
        border
        row-key="id"
        :row-class-name="allowSelect ? 'selectable-row' : ''"
        highlight-current-row
        @row-click="onRowClick"
      >
        <el-table-column label="用户名" prop="username" show-overflow-tooltip />
        <el-table-column label="昵称" prop="nickname" show-overflow-tooltip />
        <el-table-column label="邮箱" prop="email" show-overflow-tooltip />
        <el-table-column label="用户状态" prop="status" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
            <el-tag v-else type="danger">封禁</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <el-table-column v-if="allowSelect" label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="onSelect(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="listTotal"
        @size-change="loadList"
        @current-change="loadList"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="onClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { GetUserListDto, GetUserListVo } from "@/views/core/api/UserApi.ts";
import UserApi from "@/views/core/api/UserApi.ts";
import { Result } from "@/commons/model/Result.ts";
import { ElMessage } from "element-plus";
import { reactive, ref, watch } from "vue";

interface Props {
  modelValue: boolean;
  allowSelect?: boolean;
}

interface Emits {
  (e: "update:modelValue", value: boolean): void;
  (e: "onUserSelected", user: GetUserListVo): void;
}

const props = withDefaults(defineProps<Props>(), {
  allowSelect: false,
});

const emit = defineEmits<Emits>();

const visible = ref(false);
const listData = ref<GetUserListVo[]>([]);
const listTotal = ref(0);
const listLoading = ref(false);

const queryForm = reactive<GetUserListDto>({
  username: undefined,
  status: null,
  pageNum: 1,
  pageSize: 20,
});

watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal;
    if (newVal) {
      loadList();
    }
  }
);

watch(visible, (newVal) => {
  emit("update:modelValue", newVal);
});

const loadList = async (): Promise<void> => {
  listLoading.value = true;

  try {
    const result = await UserApi.getUserList(queryForm);

    if (Result.isSuccess(result)) {
      listData.value = result.data;
      listTotal.value = result.total;
    }

    if (Result.isError(result)) {
      ElMessage.error(result.message);
    }
  } catch (error: any) {
    ElMessage.error(error.message || "加载失败");
  } finally {
    listLoading.value = false;
  }
};

const resetQuery = (): void => {
  queryForm.pageNum = 1;
  queryForm.pageSize = 20;
  queryForm.username = undefined;
  queryForm.status = null;
  loadList();
};

const onRowClick = (row: GetUserListVo): void => {
  if (props.allowSelect) {
    onSelect(row);
  }
};

const onSelect = (user: GetUserListVo): void => {
  if (!props.allowSelect) {
    return;
  }
  emit("onUserSelected", user);
  onClose();
};

const onClose = (): void => {
  visible.value = false;
  resetQuery();
};
</script>

<style scoped>
.query-form {
  margin-bottom: 20px;
}

.user-table {
  margin-bottom: 20px;
  min-height: 300px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.selectable-row) {
  cursor: pointer;
}

:deep(.selectable-row:hover) {
  background-color: var(--el-table-row-hover-bg-color);
}

:deep(.modal-centered) {
  margin: 0 auto;
  top: 50%;
  transform: translateY(-50%);
}
</style>
