<template>
  <StdListLayout>
    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="模板名称">
              <el-input v-model="listForm.name" placeholder="输入模板名称查询" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="模板标识">
              <el-input v-model="listForm.code" placeholder="输入模板标识查询" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="状态">
              <el-select v-model="listForm.status" placeholder="请选择状态" clearable>
                <el-option label="启用" :value="0" />
                <el-option label="禁用" :value="1" />
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
      <el-button type="success" @click="openUploadDialog">上传模板</el-button>
      <el-button type="danger" :disabled="listSelected.length === 0" :loading="listLoading" @click="removeTemplateBatch">
        批量删除
      </el-button>
    </template>

    <template #table>
      <el-table
        v-loading="listLoading"
        :data="listData"
        stripe
        border
        row-key="id"
        height="100%"
        @selection-change="(val: GetExcelTemplateListVo[]) => (listSelected = val)"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column
          prop="name"
          label="模板名称"
          min-width="180"
          show-overflow-tooltip
          :show-overflow-tooltip-props="{
            effect: 'dark',
            placement: 'top',
            enterable: false,
          }"
        />
        <el-table-column
          prop="code"
          label="模板标识"
          min-width="150"
          show-overflow-tooltip
          :show-overflow-tooltip-props="{
            effect: 'dark',
            placement: 'top',
            enterable: false,
          }"
        />
        <el-table-column
          prop="remark"
          label="模板备注"
          min-width="200"
          show-overflow-tooltip
          :show-overflow-tooltip-props="{
            effect: 'dark',
            placement: 'top',
            enterable: false,
          }"
          :formatter="(_, __, value) => value ?? '-'"
        />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" fixed="right" min-width="200">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openEditModal(scope.row)"> 编辑 </el-button>
            <el-button link type="success" size="small" :icon="DownloadIcon" @click="downloadTemplate(scope.row.code)">
              下载
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeTemplate(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

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
  </StdListLayout>

  <!-- 上传模板对话框 -->
  <el-dialog
    v-model="uploadDialogVisible"
    title="上传Excel模板"
    width="600px"
    :close-on-click-modal="false"
    destroy-on-close
    class="upload-dialog"
    @close="resetUploadDialog"
  >
    <div class="upload-rules-container">
      <el-alert title="上传规则说明" type="info" :closable="false" show-icon>
        <template #default>
          <div class="rule-list">
            <div class="rule-item">
              <span class="rule-label">命名规则：</span>
              <span class="rule-value">模板名称-唯一标识符.xlsx</span>
            </div>
            <div class="rule-item">
              <span class="rule-label">示例名称：</span>
              <span class="rule-value">销售报表-salesReport.xlsx</span>
            </div>
            <div class="rule-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>唯一标识符仅限字母、数字和下划线，单次限50个文件。</span>
            </div>
            <div class="rule-warning">
              <el-icon><WarningFilled /></el-icon>
              <span>重要：相同标识的模板将被新文件直接覆盖。</span>
            </div>
          </div>
        </template>
      </el-alert>
    </div>

    <div class="upload-area">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="onFileChange"
        :file-list="fileList"
        :disabled="uploadLoading"
        accept=".xlsx"
        multiple
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或 <em>点击选择</em></div>
        <template #tip>
          <div v-if="uploadLoading" class="upload-progress-tip">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在处理并上传模板，请耐心等待...</span>
          </div>
        </template>
      </el-upload>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button :disabled="uploadLoading" @click="uploadDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="uploadLoading" :disabled="fileList.length === 0" @click="submitUpload()">
          上传 {{ fileList.length > 0 ? `(${fileList.length})` : "" }}
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 编辑模板对话框 -->
  <el-dialog
    v-model="editDialogVisible"
    title="编辑Excel模板"
    width="500px"
    :close-on-click-modal="false"
    @close="resetEditDialog"
  >
    <el-form
      v-if="editDialogVisible"
      ref="editFormRef"
      :model="editForm"
      :rules="editRules"
      label-width="100px"
      :validate-on-rule-change="false"
    >
      <el-form-item label="模板名称" prop="name">
        <el-input v-model="editForm.name" placeholder="请输入模板名称" />
      </el-form-item>
      <el-form-item label="模板标识" prop="code">
        <el-input v-model="editForm.code" placeholder="请输入模板标识" />
      </el-form-item>
      <el-form-item label="模板备注" prop="remark">
        <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入模板备注" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="editForm.status">
          <el-radio :value="0">启用</el-radio>
          <el-radio :value="1">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="editDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit"> 保存 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Edit, Delete, Download, UploadFilled, InfoFilled, WarningFilled, Loading } from "@element-plus/icons-vue";
import { markRaw } from "vue";
import type { FormInstance, UploadFile, UploadInstance } from "element-plus";
import ExcelTemplateApi, {
  type GetExcelTemplateListDto,
  type GetExcelTemplateListVo,
  type EditExcelTemplateDto,
} from "@/views/core/api/ExcelTemplateApi.ts";
import { Result } from "@/commons/model/Result.ts";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";

const listForm = reactive<GetExcelTemplateListDto>({
  name: null,
  code: null,
  status: null,
  pageNum: 1,
  pageSize: 20,
});

const listData = ref<GetExcelTemplateListVo[]>([]);
const listSelected = ref<GetExcelTemplateListVo[]>([]);
const listTotal = ref(0);
const listLoading = ref(false);

const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const DownloadIcon = markRaw(Download);

// 上传对话框相关
const uploadDialogVisible = ref(false);
const uploadRef = ref<UploadInstance>();
const uploadLoading = ref(false);
const fileList = ref<UploadFile[]>([]);

// 编辑对话框相关
const editDialogVisible = ref(false);
const editFormRef = ref<FormInstance>();
const editLoading = ref(false);
const editForm = reactive<EditExcelTemplateDto>({
  id: 0,
  name: "",
  code: "",
  remark: "",
  status: 0,
});
const editRules = {
  name: [
    { required: true, message: "模板名称不能为空", trigger: "blur" },
    { min: 2, max: 32, message: "模板名称长度必须在2-32个字符之间", trigger: "blur" },
  ],
  code: [
    { required: true, message: "模板标识不能为空", trigger: "blur" },
    { pattern: /^[a-zA-Z0-9_]+$/, message: "模板标识只能包含字母、数字和下划线", trigger: "blur" },
    { min: 2, max: 32, message: "模板标识长度必须在2-32个字符之间", trigger: "blur" },
  ],
  remark: [{ max: 1000, message: "模板备注长度不能超过1000个字符", trigger: "blur" }],
  status: [
    { required: true, message: "状态不能为空", trigger: "change" },
    { type: "number", min: 0, max: 1, message: "状态只能在0-1之间", trigger: "change" },
  ],
};

const loadList = async (): Promise<void> => {
  listLoading.value = true;
  const result = await ExcelTemplateApi.getExcelTemplateList(listForm);

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
  listForm.name = null;
  listForm.code = null;
  listForm.status = null;
  loadList();
};

loadList();

const openUploadDialog = (): void => {
  uploadDialogVisible.value = true;
};

const resetUploadDialog = (): void => {
  fileList.value = [];
  uploadRef.value?.clearFiles();
};

const onFileChange = (file: UploadFile, fileListParam: UploadFile[]): void => {
  if (!file.name.endsWith(".xlsx")) {
    ElMessage.error(`文件 ${file.name} 格式错误：只能上传.xlsx格式的文件`);
    uploadRef.value?.handleRemove(file);
    return;
  }

  if (!file.name.includes("-")) {
    ElMessage.error(`文件 ${file.name} 命名错误：文件名必须包含"-"符号`);
    uploadRef.value?.handleRemove(file);
    return;
  }

  const parts = file.name.split("-");
  if (parts.length !== 2) {
    ElMessage.error(`文件 ${file.name} 格式错误：应为"模板名称-唯一标识符.xlsx"`);
    uploadRef.value?.handleRemove(file);
    return;
  }

  const name = parts[0];
  if (!name || name.trim().length === 0) {
    ElMessage.error(`文件 ${file.name} 错误：模板名称不能为空`);
    uploadRef.value?.handleRemove(file);
    return;
  }

  const key = parts[1].replace(".xlsx", "");
  if (!key || key.trim().length === 0) {
    ElMessage.error(`文件 ${file.name} 错误：唯一标识符不能为空`);
    uploadRef.value?.handleRemove(file);
    return;
  }

  if (!/^[a-zA-Z0-9_]+$/.test(key)) {
    ElMessage.error(`文件 ${file.name} 错误：唯一标识符只能包含字母、数字和下划线`);
    uploadRef.value?.handleRemove(file);
    return;
  }

  if (fileListParam.length > 50) {
    ElMessage.error("最多同时上传50个模板");
    uploadRef.value?.handleRemove(file);
    return;
  }

  fileList.value = fileListParam;
};

const submitUpload = async (): Promise<void> => {
  if (fileList.value.length === 0) {
    ElMessage.warning("请先选择要上传的文件");
    return;
  }

  if (uploadLoading.value) {
    return;
  }

  uploadLoading.value = true;

  try {
    const files = fileList.value.map((f) => f.raw as File).filter((f) => f !== undefined && f !== null);

    if (files.length === 0) {
      ElMessage.warning("没有可上传的文件");
      return;
    }

    const result = await ExcelTemplateApi.uploadExcelTemplate(files);

    if (Result.isError(result)) {
      ElMessage.error(`上传失败：${result.message || "未知错误"}`);
      return;
    }
    ElMessage.success(`成功上传 ${files.length} 个模板文件`);
    uploadDialogVisible.value = false;
    resetUploadDialog();
    await loadList();
  } catch (error: any) {
    if (error.response) {
      ElMessage.error(`上传失败：${error.response.data?.message || error.message || "服务器错误"}`);
      return;
    }
    if (error.request) {
      ElMessage.error("上传失败：网络请求失败，请检查网络连接");
      return;
    }
    ElMessage.error(`上传失败：${error.message || "未知错误"}`);
  } finally {
    uploadLoading.value = false;
  }
};

const openEditModal = (row: GetExcelTemplateListVo): void => {
  editForm.id = row.id;
  editForm.name = row.name;
  editForm.code = row.code;
  editForm.remark = row.remark;
  editForm.status = row.status;
  editDialogVisible.value = true;
};

const resetEditDialog = (): void => {
  editForm.id = 0;
  editForm.name = "";
  editForm.code = "";
  editForm.remark = "";
  editForm.status = 0;

  if (editFormRef.value) {
    editFormRef.value.resetFields();
  }
};

const submitEdit = async (): Promise<void> => {
  try {
    await editFormRef?.value?.validate();
  } catch {
    return;
  }

  editLoading.value = true;

  try {
    const result = await ExcelTemplateApi.editExcelTemplate(editForm);
    if (Result.isSuccess(result)) {
      ElMessage.success("操作成功");
      await loadList();
    }
    if (Result.isError(result)) {
      ElMessage.error(result.message);
    }
  } catch (error: any) {
    ElMessage.error(error.message);
  } finally {
    editLoading.value = false;
  }
};

const removeTemplate = async (id: number): Promise<void> => {
  try {
    await ElMessageBox.confirm("确定删除该模板吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
  } catch {
    return;
  }

  try {
    await ExcelTemplateApi.removeExcelTemplate({ id: id.toString() });
    ElMessage.success("删除成功");
    await loadList();
  } catch (error: any) {
    ElMessage.error(error.message);
  }
};

const removeTemplateBatch = async (): Promise<void> => {
  try {
    await ElMessageBox.confirm("确定删除选中的模板吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
  } catch {
    return;
  }

  try {
    await ExcelTemplateApi.removeExcelTemplate({
      ids: listSelected.value.map((item) => item.id.toString()),
    });
    ElMessage.success("删除成功");
    await loadList();
  } catch (error: any) {
    ElMessage.error(error.message);
  }
};

const downloadTemplate = async (code: string): Promise<void> => {
  try {
    await ExcelTemplateApi.downloadExcelTemplate(code);
    ElMessage.success("下载成功");
  } catch (error: any) {
    ElMessage.error(error.message || "下载失败");
  }
};
</script>

<style scoped>
.el-icon--upload {
  font-size: 67px;
  color: #8c939d;
  margin-bottom: 16px;
  line-height: 50px;
}

.el-upload__text {
  color: #606266;
  font-size: 14px;
  text-align: center;
}

.el-upload__text em {
  color: #409eff;
  font-style: normal;
  font-weight: bold;
}

.upload-rules-container {
  margin-bottom: 20px;
}

.rule-list {
  margin-top: 8px;
  line-height: 1.8;
}

.rule-item {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.rule-label {
  color: #606266;
  font-weight: bold;
  width: 70px;
}

.rule-value {
  color: #409eff;
  font-family: monospace;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 0;
}

.rule-tip,
.rule-warning {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 8px;
  font-size: 12px;
}

.rule-tip {
  color: #e6a23c;
}

.rule-warning {
  color: #f56c6c;
  font-weight: bold;
}

.upload-area {
  padding: 10px 0;
}

.upload-progress-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 15px;
  color: #409eff;
  font-size: 14px;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 2px dashed #dcdfe6;
  border-radius: 0;
  transition: all 0.3s;
}

:deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.upload-dialog :deep(.el-dialog) {
  border-radius: 0;
}

.upload-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}

.upload-rules-container :deep(.el-alert) {
  border-radius: 0;
}

.el-icon.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
