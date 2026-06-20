<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="输出方案名称">
            <el-input v-model="listForm.name" placeholder="输入输出方案名称" clearable />
          </el-form-item>
          <el-form-item label="模型名称">
            <el-input v-model="listForm.modelName" placeholder="输入模型名称" clearable />
          </el-form-item>
          <el-form-item label="数据源表名">
            <el-input v-model="listForm.tableName" placeholder="输入数据源表名" clearable />
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
      <el-button type="success" @click="openModal('add', null)">新增输出方案</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable>
      <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
        <el-table-column label="序号" min-width="60" align="center">
          <template #default="scope">
            {{ (listForm.pageNum - 1) * listForm.pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="输出方案名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="modelName" label="模型名称" min-width="180" show-overflow-tooltip />
        <el-table-column
          prop="tableName"
          label="数据源表名"
          min-width="120"
          show-overflow-tooltip
          :formatter="
            (row: GetOpSchemaListVo) => {
              if (!row.tableName) {
                return '-';
              }
              return row.tableName;
            }
          "
        />
        <el-table-column label="字段数(原始/聚合)" min-width="140" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.fieldCountOrigin }} of {{ scope.row.fieldCountPoly }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="300">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="EditIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              :icon="ManagementIcon"
              @click="cdrcRedirect('op-schema-design', scope.row)"
            >
              设计
            </el-button>
            <el-button
              link
              type="success"
              size="small"
              :icon="SimulationIcon"
              @click="cdrcRedirect('op-schema-preview', scope.row)"
            >
              模拟
            </el-button>
            <el-button link type="warning" size="small" :icon="CopyIcon" @click="openCopyModal(scope.row)"> 复制 </el-button>
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
      :title="modalMode === 'edit' ? '编辑输出方案' : '新增输出方案'"
      width="1200px"
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
        label-position="top"
        :validate-on-rule-change="false"
      >
        <el-row :gutter="20" class="flex items-stretch">
          <!-- 左侧：输入配置 -->
          <el-col :span="8">
            <el-card class="h-full">
              <template #header>
                <div class="flex items-center font-bold">
                  <span>输入配置</span>
                </div>
              </template>

              <el-form-item label="输入数据源" prop="dataSourceId">
                <el-select
                  v-model="modalForm.dataSourceId"
                  placeholder="请选择输入数据源"
                  filterable
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in modalDataSource"
                    :key="item.id"
                    :value="item.id"
                    :label="`${item.name}（${item.code}）`"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="数据源表名" prop="tableName">
                <DataSourceTableBrowser v-model="modalForm.tableName" :data-source-id="modalForm.dataSourceId" />
              </el-form-item>
              <el-form-item label="类型映射方案" prop="typeSchemaId">
                <el-select
                  v-model="modalForm.typeSchemaId"
                  placeholder="请选择类型映射方案"
                  filterable
                  clearable
                  style="width: 100%"
                >
                  <el-option v-for="item in modalTypeSchema" :key="item.id" :value="item.id" :label="item.name" />
                </el-select>
              </el-form-item>
              <el-form-item label="从SCM读取蓝图" prop="inputScmId">
                <el-select
                  v-model="modalForm.inputScmId"
                  placeholder="请选择输入SCM"
                  filterable
                  clearable
                  style="width: 100%"
                  :class="{ 'scm-not-found': modalForm.inputScmId && !modalScm.find((s) => s.id === modalForm.inputScmId) }"
                >
                  <el-option v-for="item in modalScm" :key="item.id" :value="item.id" :label="item.name" />
                  <el-option
                    v-if="modalForm.inputScmId && !modalScm.find((s) => s.id === modalForm.inputScmId)"
                    :value="modalForm.inputScmId"
                    label="该SCM无法访问"
                    class="text-red-500!"
                    disabled
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="输入基准路径" prop="baseInput">
                <AnchorPointBrowser v-model="modalForm.baseInput" :scm-id="modalForm.inputScmId" :kind="0" />
              </el-form-item>
            </el-card>
          </el-col>

          <!-- 中间：基础配置 -->
          <el-col :span="8">
            <el-card class="h-full border-purple-200">
              <template #header>
                <div class="flex items-center font-bold">
                  <span>转换配置</span>
                </div>
              </template>

              <el-form-item label="方案名称" prop="name">
                <el-input v-model="modalForm.name" placeholder="请输入输出方案名称" clearable maxlength="32" show-word-limit />
              </el-form-item>
              <el-form-item label="模型名称 (用于代码模型)" prop="modelName">
                <el-input v-model="modalForm.modelName" placeholder="请输入模型名称" clearable maxlength="255" show-word-limit>
                  <template #append>
                    <el-button
                      :disabled="!modalForm.tableName"
                      :icon="MagicStickIcon"
                      @click="
                        () => {
                          const parts = modalForm.tableName.split('_');
                          const nameParts = parts.length > 1 ? parts.slice(1) : parts;
                          modalForm.modelName = nameParts.map((p) => p.charAt(0).toUpperCase() + p.slice(1).toLowerCase()).join('');
                        }
                      "
                    >
                      推断
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="模型备注 (用于接口文档、代码注释)" prop="modelRemark">
                <el-input
                  v-model="modalForm.modelRemark"
                  placeholder="请输入模型备注"
                  clearable
                  maxlength="80"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item label="业务域" prop="bizDomain">
                <el-input
                  v-model="modalForm.bizDomain"
                  placeholder="请输入业务域"
                  clearable
                  maxlength="80"
                  show-word-limit
                >
                  <template #append>
                    <el-button
                      :disabled="!modalForm.tableName"
                      :icon="MagicStickIcon"
                      @click="
                        () => {
                          const parts = modalForm.tableName.split('_');
                          if (parts.length > 1) {
                            modalForm.bizDomain = parts[0];
                          }
                        }
                      "
                    >
                      推断
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="权限码前缀" prop="permCodePrefix">
                <el-input
                  v-model="modalForm.permCodePrefix"
                  placeholder="请输入权限码前缀"
                  clearable
                  maxlength="32"
                  show-word-limit
                >
                  <template #append>
                    <el-button
                      :disabled="!modalForm.tableName"
                      :icon="MagicStickIcon"
                      @click="modalForm.permCodePrefix = modalForm.tableName.toLowerCase().replace(/_/g, ':')"
                    >
                      推断
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
            </el-card>
          </el-col>

          <!-- 右侧：输出配置 -->
          <el-col :span="8">
            <el-card class="h-full">
              <template #header>
                <div class="flex items-center font-bold">
                  <span>输出配置</span>
                </div>
              </template>

              <el-form-item label="输出到SCM" prop="outputScmId">
                <el-select
                  v-model="modalForm.outputScmId"
                  placeholder="请选择输出SCM"
                  filterable
                  clearable
                  style="width: 100%"
                  :class="{ 'scm-not-found': modalForm.outputScmId && !modalScm.find((s) => s.id === modalForm.outputScmId) }"
                >
                  <el-option v-for="item in modalScm" :key="item.id" :value="item.id" :label="item.name" />
                  <el-option
                    v-if="modalForm.outputScmId && !modalScm.find((s) => s.id === modalForm.outputScmId)"
                    :value="modalForm.outputScmId"
                    label="该SCM无法访问"
                    class="text-red-500!"
                    disabled
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="输出基准路径" prop="baseOutput">
                <AnchorPointBrowser v-model="modalForm.baseOutput" :scm-id="modalForm.outputScmId" :kind="1" />
              </el-form-item>
              <el-form-item v-if="false" label="写出策略" prop="policyOverride">
                <el-select v-model="modalForm.policyOverride" placeholder="请选择写出策略" style="width: 100%">
                  <el-option :value="0" label="不覆盖" />
                  <el-option :value="1" label="覆盖" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="false" label="移除表前缀" prop="removeTablePrefix">
                <el-input
                  v-model="modalForm.removeTablePrefix"
                  placeholder="请输入移除表前缀"
                  clearable
                  maxlength="80"
                  show-word-limit
                >
                  <template #append>
                    <el-button
                      :disabled="!modalForm.tableName"
                      :icon="MagicStickIcon"
                      @click="
                        () => {
                          const parts = modalForm.tableName.split('_');
                          modalForm.removeTablePrefix = parts.length > 1 ? parts[0] + '_' : '';
                        }
                      "
                    >
                      推断
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="方案备注" prop="remark">
                <el-input
                  v-model="modalForm.remark"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入方案备注"
                  clearable
                  show-word-limit
                  maxlength="1000"
                />
              </el-form-item>
            </el-card>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">取消</el-button>
          <el-button type="primary" :loading="modalLoading" @click="submitModal">
            {{ modalMode === "add" ? "创建并导入原始字段" : "保存并更新原始字段" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 复制输出方案模态框 -->
    <el-dialog
      v-model="copyModalVisible"
      title="复制输出方案"
      width="500px"
      :close-on-click-modal="false"
      @close="resetCopyModal(); loadList();"
    >
      <el-form
        v-if="copyModalVisible"
        ref="copyModalRef"
        :model="copyForm"
        label-width="100px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="方案名称" prop="name" :rules="[{ required: true, message: '请输入输出方案名称', trigger: 'blur' }, { max: 32, message: '输出方案名称不能超过32个字符', trigger: 'blur' }]">
          <el-input v-model="copyForm.name" placeholder="请输入输出方案名称" clearable maxlength="32" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="copyModalVisible = false">关闭</el-button>
          <el-button type="primary" :loading="modalLoading" @click="submitModal">创建</el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete, Management, MagicStick, View } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import OpSchemaService from "@/views/assembly/service/OpSchemaService";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";
import DataSourceTableBrowser from "@/views/assembly/components/DataSourceTableBrowser.vue";
import AnchorPointBrowser from "@/views/assembly/components/AnchorPointBrowser.vue";
import ComDirectRouteContext from "@/soa/com-series/service/ComDirectRouteContext.ts";
import ComIconService from "@/soa/com-series/service/ComIconService";
import type { GetOpSchemaListVo } from "@/views/assembly/api/OpSchemaApi";

const { resolveIcon } = ComIconService.useIconService();

const EditIcon = resolveIcon("edit");
const DeleteIcon = resolveIcon("delete");
const ManagementIcon = resolveIcon("management");
const MagicStickIcon = resolveIcon("magic-stick");
const ViewIcon = resolveIcon("view");
const SimulationIcon = resolveIcon("monitor");
const CopyIcon = resolveIcon("copy-document");

//列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } =
  OpSchemaService.useOpSchemaList();

//使用CDRC打包上下文
const { cdrcRedirect } = ComDirectRouteContext.useDirectRouteContext();

const modalFormRef = ref<FormInstance>();

//模态框打包
const {
  modalVisible,
  modalLoading,
  modalMode,
  modalForm,
  modalRules,
  modalDataSource,
  modalTypeSchema,
  modalScm,
  openModal,
  resetModal,
  submitModal,
  copyModalVisible,
  copyForm,
  copyModalRef,
  openCopyModal,
  resetCopyModal,
} = OpSchemaService.useOpSchemaModal(modalFormRef, loadList);
</script>

<style scoped>
.el-card {
  box-shadow: none !important;
  border-radius: 0 !important;
}

.scm-not-found :deep(.el-select__placeholder),
.scm-not-found :deep(.el-select__selected-item) {
  color: var(--el-color-danger) !important;
}
</style>
