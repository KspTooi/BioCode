<template>
  <StdListContainer>
    <StdListAreaQuery>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="用户名">
              <el-input v-model="listForm.userName" placeholder="输入用户名查询" clearable />
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
              <el-button :disabled="listLoading" @click="resetList">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </StdListAreaQuery>

    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="username" label="登录账号" min-width="150" />
        <el-table-column prop="rsMax" label="数据权限(RS)等级" min-width="150">
          <template #default="scope">
            <el-tag v-if="scope.row.rsMax === 0" type="success">全集团</el-tag>
            <el-tag v-if="scope.row.rsMax === 10">本公司+下级公司</el-tag>
            <el-tag v-if="scope.row.rsMax === 20">仅本公司</el-tag>
            <el-tag v-if="scope.row.rsMax === 30">本部门+下级部门</el-tag>
            <el-tag v-if="scope.row.rsMax === 40">仅本部门</el-tag>
            <el-tag v-if="scope.row.rsMax === 50">仅本人</el-tag>
            <el-tag v-if="scope.row.rsMax === 60" type="warning">指定组织</el-tag>
            <el-tag v-if="scope.row.rsMax === 100" type="error">无权限</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="登入时间" min-width="180" />
        <el-table-column prop="expiresAt" label="过期时间" min-width="180" />
        <el-table-column prop="isExpired" label="是否过期" min-width="180">
          <template #default="scope">
            <el-tag :type="scope.row.isExpired ? 'danger' : 'success'">
              {{ scope.row.isExpired ? "是" : "否" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="ViewIcon" @click="openModal(scope.row)">查看</el-button>
            <el-button link type="danger" size="small" :icon="CloseIcon" @click="onCloseSession(scope.row)">
              关闭会话
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 会话详情模态框 -->
    <el-dialog v-model="modalVisible" title="会话详情" width="900px" :close-on-click-modal="false" destroy-on-close>
      <div v-if="currentSessionDetails" class="session-details-container">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border title="基本信息" class="mb-4">
          <el-descriptions-item label="会话ID" :span="2">{{ currentSessionDetails.id }}</el-descriptions-item>
          <el-descriptions-item label="登录账号">{{ currentSessionDetails.username }}</el-descriptions-item>
          <el-descriptions-item label="数据权限(RS)等级">
            <el-tag v-if="currentSessionDetails.rsMax === 0" type="success">全集团</el-tag>
            <el-tag v-if="currentSessionDetails.rsMax === 10">本公司+下级公司</el-tag>
            <el-tag v-if="currentSessionDetails.rsMax === 20">仅本公司</el-tag>
            <el-tag v-if="currentSessionDetails.rsMax === 30">本部门+下级部门</el-tag>
            <el-tag v-if="currentSessionDetails.rsMax === 40">仅本部门</el-tag>
            <el-tag v-if="currentSessionDetails.rsMax === 50">仅本人</el-tag>
            <el-tag v-if="currentSessionDetails.rsMax === 60" type="warning">指定组织</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="登入时间">{{ currentSessionDetails.createTime }}</el-descriptions-item>
          <el-descriptions-item label="过期时间">{{ currentSessionDetails.expiresAt }}</el-descriptions-item>
        </el-descriptions>

        <el-row :gutter="20">
          <!-- 数据权限部门列表 -->
          <el-col v-if="currentSessionDetails.rsDeptNames && currentSessionDetails.rsDeptNames.length > 0" :span="10">
            <div class="section-title">允许访问部门(RSAD) ({{ currentSessionDetails.rsDeptNames.length }})</div>
            <el-table :data="currentSessionDetails.rsDeptNames" stripe border max-height="400px" size="small">
              <el-table-column label="部门名称">
                <template #default="scope">
                  <el-tag size="small" type="info">{{ scope.row }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-col>

          <!-- 权限节点列表 -->
          <el-col :span="currentSessionDetails.rsDeptNames && currentSessionDetails.rsDeptNames.length > 0 ? 14 : 24">
            <div class="section-title">
              权限节点 ({{ currentSessionDetails.permissions?.length || 0 }})
              <el-input
                v-model="permissionSearchKeyword"
                placeholder="搜索权限代码"
                clearable
                size="small"
                style="width: 200px; float: right"
              />
            </div>
            <el-table :data="filteredPermissions" stripe border max-height="400px" size="small">
              <el-table-column label="权限代码" show-overflow-tooltip>
                <template #default="scope">
                  <code>{{ scope.row }}</code>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="modalVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { reactive, ref, markRaw, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { View, CloseBold } from "@element-plus/icons-vue";
import AdminSessionApi, {
  type GetSessionDetailsVo,
  type GetSessionListDto,
  type GetSessionListVo,
} from "@/views/auth/api/SessionApi.ts";
import { Result } from "@/commons/model/Result.ts";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

const ViewIcon = markRaw(View);
const CloseIcon = markRaw(CloseBold);

const listForm = reactive<GetSessionListDto>({
  userName: null,
  pageNum: 1,
  pageSize: 20,
});

const listData = ref<GetSessionListVo[]>([]);
const listTotal = ref(0);
const listLoading = ref(false);

const modalVisible = ref(false);
const currentSessionDetails = ref<GetSessionDetailsVo | null>(null);
const permissionSearchKeyword = ref("");
const filteredPermissions = computed((): string[] => {
  if (!currentSessionDetails.value?.permissions) {
    return [];
  }

  return currentSessionDetails.value.permissions.filter((permission) =>
    permission.toLowerCase().includes(permissionSearchKeyword.value.toLowerCase())
  );
});

const loadList = async (): Promise<void> => {
  listLoading.value = true;
  const result = await AdminSessionApi.getSessionList(listForm);

  if (Result.isSuccess(result)) {
    listData.value = result.data;
    listTotal.value = result.total;
  }

  if (Result.isError(result)) {
    ElMessage.error(result.message);
  }

  listLoading.value = false;
};

const resetList = (): void => {
  listForm.pageNum = 1;
  listForm.pageSize = 20;
  listForm.userName = null;
  loadList();
};

const openModal = async (row: GetSessionListVo): Promise<void> => {
  listLoading.value = true;
  try {
    const res = await AdminSessionApi.getSessionDetails({ id: row.id });
    currentSessionDetails.value = res;
    permissionSearchKeyword.value = ""; // 重置搜索关键词
    modalVisible.value = true;
  } catch (error: any) {
    ElMessage.error(error.message || "获取会话详情失败");
  } finally {
    listLoading.value = false;
  }
};

const onCloseSession = async (row: GetSessionListVo): Promise<void> => {
  try {
    await ElMessageBox.confirm(`确定要关闭用户 ${row.username} 的会话吗？`, "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await AdminSessionApi.closeSession({ id: row.id });
    ElMessage.success("会话关闭成功");
    await loadList(); // Refresh the list
  } catch (error) {
    if (error !== "cancel") {
      const errorMsg = error instanceof Error ? error.message : "关闭会话失败";
      ElMessage.error(errorMsg);
    }
  }
};

loadList();
</script>

<style scoped>
.session-details-container {
  padding: 10px 0;
}

.mb-4 {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  line-height: 32px;
}

code {
  font-family: monospace;
  background-color: #f5f7fa;
  padding: 2px 4px;
  border-radius: 4px;
  color: #409eff;
}

.el-tag {
  margin-right: 5px;
}

/* Ensure tooltip content is readable if it becomes too long */
:deep(.el-tooltip__popper) {
  max-width: 400px; /* Adjust as needed */
  word-break: break-all;
}
</style>
