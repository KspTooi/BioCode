<template>
  <StdListContainer>
    <!-- 查询条件区域 -->
    <StdListAreaQuery>
      <el-form :model="listForm" inline class="flex justify-between">
        <div>
          <el-form-item label="模型变体名称">
            <el-input v-model="listForm.name" placeholder="输入模型变体名称" clearable />
          </el-form-item>
          <el-form-item label="模型标识">
            <el-input v-model="listForm.code" placeholder="输入模型标识" clearable />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="listForm.kind" placeholder="请选择类型" clearable>
              <el-option label="文本" :value="0" />
              <el-option label="图形" :value="1" />
              <el-option label="音频" :value="2" />
              <el-option label="多模态" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="listForm.status" placeholder="请选择状态" clearable>
              <el-option label="禁用" :value="0" />
              <el-option label="启用" :value="1" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" @click="loadList" :disabled="listLoading">查询</el-button>
          <el-button @click="resetList" :disabled="listLoading">重置</el-button>
        </el-form-item>
      </el-form>
    </StdListAreaQuery>

    <!-- 操作按钮区域 -->
    <StdListAreaAction class="flex gap-2">
      <el-button type="primary" @click="openModal('add', null)">创建模型变体</el-button>
    </StdListAreaAction>

    <!-- 列表表格区域 -->
    <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
      <el-table :data="listData" stripe v-loading="listLoading" border height="100%">
        <el-table-column type="index" label="序号" width="60" show-overflow-tooltip align="center" />
        <el-table-column prop="name" label="模型变体名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="模型标识" min-width="120" show-overflow-tooltip />
        <el-table-column label="类型" min-width="50" align="center">
          <template #default="scope">
            <span v-if="scope.row.kind === 0" class="text-indigo-600">文本</span>
            <span v-if="scope.row.kind === 1" class="text-slate-400">图形</span>
            <span v-if="scope.row.kind === 2" class="text-slate-400">音频</span>
            <span v-if="scope.row.kind === 3" class="text-slate-400">多模态</span>
          </template>
        </el-table-column>
        <el-table-column label="规格" min-width="130">
          <template #default="scope">
            <div class="flex items-center gap-3">
              <el-tooltip :content="`最大上下文: ${formatRaw(scope.row.maxContext)}`" placement="top">
                <span class="flex items-center gap-1 text-slate-500">
                  <el-icon :size="14"><FullScreenIcon /></el-icon>
                  {{ formatNumber(scope.row.maxContext) }}
                </span>
              </el-tooltip>
              <el-tooltip :content="`最大输出词元: ${formatRaw(scope.row.maxOutputToken)}`" placement="top">
                <span class="flex items-center gap-1 text-slate-500">
                  <el-icon :size="14"><PointerIcon /></el-icon>
                  {{ formatNumber(scope.row.maxOutputToken) }}
                </span>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="推理" width="75" align="center">
          <template #default="scope">
            <span v-if="scope.row.apiReasoning === 0" class="text-slate-500">不支持</span>
            <span v-if="scope.row.apiReasoning === 1 && scope.row.apiReasoningEffort === 0">关</span>
            <span v-if="scope.row.apiReasoning === 1 && scope.row.apiReasoningEffort === 1" class="text-slate-500">低</span>
            <span v-if="scope.row.apiReasoning === 1 && scope.row.apiReasoningEffort === 2" class="text-indigo-600">中</span>
            <span v-if="scope.row.apiReasoning === 1 && scope.row.apiReasoningEffort === 3" class="text-amber-600">高</span>
            <span v-if="scope.row.apiReasoning === 1 && scope.row.apiReasoningEffort === 4" class="text-rose-600">极高</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" min-width="185">
          <template #default="scope">
            <div class="flex items-center gap-3">
              <el-tooltip :content="`输入单价: ${formatRaw(scope.row.fincInput)}`" placement="top">
                <span class="flex items-center gap-1 text-slate-500">
                  <el-icon :size="14"><CoinIcon /></el-icon>
                  <span v-if="scope.row.fincInput">{{ scope.row.fincInput }}</span>
                  <span v-if="!scope.row.fincInput" class="text-slate-300">-</span>
                </span>
              </el-tooltip>
              <el-tooltip :content="`输入单价(缓存): ${formatRaw(scope.row.fincInputCached)}`" placement="top">
                <span class="flex items-center gap-1 text-slate-500">
                  <el-icon :size="14"><TimerIcon /></el-icon>
                  <span v-if="scope.row.fincInputCached">{{ scope.row.fincInputCached }}</span>
                  <span v-if="!scope.row.fincInputCached" class="text-slate-300">-</span>
                </span>
              </el-tooltip>
              <el-tooltip :content="`输出单价: ${formatRaw(scope.row.fincOutput)}`" placement="top">
                <span class="flex items-center gap-1 text-slate-500">
                  <el-icon :size="14"><SoldOutIcon /></el-icon>
                  <span v-if="scope.row.fincOutput">{{ scope.row.fincOutput }}</span>
                  <span v-if="!scope.row.fincOutput" class="text-slate-300">-</span>
                </span>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="测试情况" min-width="130" align="center">
          <template #default="scope">
            <el-tooltip
              v-if="scope.row.testRate || scope.row.testTtfb"
              :content="`响应速率: ${scope.row.testRate || 0} T/S · 首字响应: ${scope.row.testTtfb || 0} MS`"
              placement="top"
            >
              <span class="text-green-500"> {{ scope.row.testRate || 0 }}T/S+{{ scope.row.testTtfb || 0 }}MS </span>
            </el-tooltip>
            <span v-if="!scope.row.testRate &amp;&amp; !scope.row.testTtfb" class="text-slate-400">未测试</span>
          </template>
        </el-table-column>
        <el-table-column prop="seq" label="排序" min-width="60" align="center" />
        <el-table-column label="状态" min-width="70" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-if="scope.row.status === 0" type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="165" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openModal('view', scope.row)" :icon="ViewIcon">
              查看
            </el-button>
            <el-button link type="primary" size="small" @click="openModal('edit', scope.row)" :icon="EditIcon">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="removeList(scope.row)" :icon="DeleteIcon"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </StdListAreaTable>

    <!-- 创建/编辑模态框 -->
    <el-dialog
      v-model="modalVisible"
      :title="modalMode === 'view' ? '查看模型变体' : (modalMode === 'edit' ? '编辑模型变体' : '创建模型变体')"
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
        label-width="120px"
        :validate-on-rule-change="false"
      >
        <el-form-item label="模型变体名称" prop="name">
          <el-input v-model="modalForm.name" placeholder="请输入模型变体名称" clearable :maxlength="80" show-word-limit :disabled="modalMode === 'view'" />
        </el-form-item>
        <el-form-item label="模型标识" prop="code">
          <el-input v-model="modalForm.code" placeholder="请输入模型标识" clearable :maxlength="64" show-word-limit :disabled="modalMode === 'view'" />
        </el-form-item>
        <el-form-item label="类型" prop="kind">
          <el-radio-group v-model="modalForm.kind" :disabled="modalMode === 'view'">
            <el-radio :value="0">文本</el-radio>
            <el-radio :value="1" disabled>图形</el-radio>
            <el-radio :value="2" disabled>音频</el-radio>
            <el-radio :value="3" disabled>多模态</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="最大上下文" prop="maxContext">
          <el-input-number v-model="modalForm.maxContext" :min="1" placeholder="请输入最大上下文长度" style="width: 100%" :disabled="modalMode === 'view'" />
        </el-form-item>
        <el-form-item label="最大输出词元" prop="maxOutputToken">
          <el-input-number v-model="modalForm.maxOutputToken" :min="1" placeholder="请输入最大输出词元" style="width: 100%" :disabled="modalMode === 'view'" />
        </el-form-item>
        <el-form-item label="推理" prop="apiReasoning">
          <el-radio-group v-model="modalForm.apiReasoning" @change="onApiReasoningChange" :disabled="modalMode === 'view'">
            <el-radio :value="1">支持</el-radio>
            <el-radio :value="0">不支持</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="推理强度" prop="apiReasoningEffort" v-if="modalForm.apiReasoning === 1">
          <el-radio-group v-model="modalForm.apiReasoningEffort" :disabled="modalMode === 'view'">
            <el-radio :value="0">关</el-radio>
            <el-radio :value="1">低</el-radio>
            <el-radio :value="2">中</el-radio>
            <el-radio :value="3">高</el-radio>
            <el-radio :value="4">极高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="附加参数" prop="apiAppendParam">
          <el-input v-model="modalForm.apiAppendParam" placeholder="请输入附加参数(JSON)" type="textarea" :rows="2" :disabled="modalMode === 'view'" />
        </el-form-item>
        <el-form-item label="附加请求头" prop="apiAppendHeaders">
          <el-input v-model="modalForm.apiAppendHeaders" placeholder="请输入附加请求头(JSON)" type="textarea" :rows="2" :disabled="modalMode === 'view'" />
        </el-form-item>
        <el-form-item label="输入单价" prop="fincInput">
          <el-input-number v-model="modalForm.fincInput" :min="0" placeholder="请输入输入单价" style="width: 100%" :disabled="modalMode === 'view'" />
        </el-form-item>
        <el-form-item label="输入单价(缓存)" prop="fincInputCached">
          <el-input-number
            v-model="modalForm.fincInputCached"
            :min="0"
            placeholder="请输入输入单价(缓存)"
            style="width: 100%"
            :disabled="modalMode === 'view'"
          />
        </el-form-item>
        <el-form-item label="输出单价" prop="fincOutput">
          <el-input-number v-model="modalForm.fincOutput" :min="0" placeholder="请输入输出单价" style="width: 100%" :disabled="modalMode === 'view'" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="modalForm.remark"
            placeholder="请输入备注"
            type="textarea"
            :rows="2"
            :maxlength="200"
            show-word-limit
            :disabled="modalMode === 'view'"
          />
        </el-form-item>
        <el-form-item label="排序" prop="seq">
          <el-input-number v-model="modalForm.seq" :min="0" :max="255" placeholder="排序" style="width: 100%" :disabled="modalMode === 'view'" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modalForm.status" :disabled="modalMode === 'view'">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="modalVisible = false">关闭</el-button>
          <el-button v-if="modalMode !== 'view'" type="primary" @click="submitModal" :loading="modalLoading">
            {{ modalMode === "add" ? "创建" : "保存" }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </StdListContainer>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { Edit, Delete, View, Coin, SoldOut, Timer, FullScreen, Pointer } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import ModelService from "@/views/aacp/service/AacpModelService";
import StdListContainer from "@/soa/std-series/StdListContainer.vue";
import StdListAreaQuery from "@/soa/std-series/StdListAreaQuery.vue";
import StdListAreaAction from "@/soa/std-series/StdListAreaAction.vue";
import StdListAreaTable from "@/soa/std-series/StdListAreaTable.vue";

// 使用markRaw包装图标组件，防止被Vue响应式系统处理
const EditIcon = markRaw(Edit);
const DeleteIcon = markRaw(Delete);
const ViewIcon = markRaw(View);
const CoinIcon = markRaw(Coin);
const SoldOutIcon = markRaw(SoldOut);
const TimerIcon = markRaw(Timer);
const FullScreenIcon = markRaw(FullScreen);
const PointerIcon = markRaw(Pointer);

/** 格式化数字为 千/百万/亿*/
const formatNumber = (val: number | undefined | null): string => {
  if (val === undefined || val === null) {
    return "-";
  }

  const absVal = Math.abs(val);
  const sign = val < 0 ? "-" : "";

  // 亿 (>= 10,000,000) -> 格式为 X.XX亿 或 0.XX亿 (例如 15,000,000 -> 0.15亿)
  // 将阈值设为 10_000_000，确保一千万以上的数字统一使用“亿”作为单位
  if (absVal >= 10_000_000) {
    return `${sign}${(absVal / 100_000_000).toFixed(2)}亿`;
  }

  // 百万 (>= 10,000) -> 格式为 X.XX百万 或 0.XX百万 (例如 150,000 -> 0.15百万)
  if (absVal >= 10_000) {
    return `${sign}${(absVal / 1_000_000).toFixed(2)}百万`;
  }

  // 千 (>= 1,000) -> 格式为 X.X千 或 X千 (仅用于 1,000 ~ 9,999 的数字)
  if (absVal >= 1_000) {
    return `${sign}${(absVal / 1_000).toFixed(1).replace(/\.0$/, "")}千`;
  }

  // 小于 1000 的数字直接原样显示
  return `${sign}${absVal}`;
};

/** 格式化原始数值为带千分位的字符串 */
const formatRaw = (val: number | undefined | null): string => {
  if (val === undefined || val === null) {
    return "-";
  }
  return val.toLocaleString("zh-CN");
};

// 列表管理打包
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = ModelService.useModelList();

// 模态框表单引用
const modalFormRef = ref<FormInstance>();

// 模态框打包
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } =
  ModelService.useModelModal(modalFormRef, loadList);

/** 切换推理开关时清空推理强度 */
const onApiReasoningChange = (val: number): void => {
  if (val === 0) {
    modalForm.apiReasoningEffort = 0;
  }
};
</script>

<style scoped></style>
