<template>
  <StdListLayout show-persist-tip>
    <template #query>
      <el-form :model="listForm">
        <el-row>
          <el-col :span="5" :offset="1">
            <el-form-item label="中继通道名称">
              <el-input v-model="listForm.name" placeholder="请输入中继通道名称" clearable style="width: 200px" />
            </el-form-item>
          </el-col>
          <el-col :span="5" :offset="1">
            <el-form-item label="桥接目标URL">
              <el-input v-model="listForm.forwardUrl" placeholder="请输入桥接目标URL" clearable style="width: 200px" />
            </el-form-item>
          </el-col>
          <el-col :span="8" :offset="4" style="display: flex; justify-content: flex-end">
            <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
            <el-button :disabled="listLoading" @click="resetList">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </template>

    <template #actions>
      <el-button type="success" @click="openModal('add', null)">创建中继通道</el-button>
    </template>

    <template #table>
      <el-table v-loading="listLoading" :data="listData" border row-key="id" default-expand-all height="100%">
        <el-table-column label="通道名称" prop="name" />
        <el-table-column label="主机" prop="host" />
        <el-table-column label="桥接目标" prop="forwardUrl" show-overflow-tooltip>
          <template #default="scope">
            <span v-show="scope.row.forwardType === 0"> {{ scope.row.forwardUrl }} </span>
            <el-button v-show="scope.row.forwardType === 1" link type="primary" @click="showRouteStateModal(scope.row)">
              已配置路由策略
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="自动运行" prop="autoStart" width="90" align="center">
          <template #default="scope">
            <span v-show="scope.row.autoStart === 1" style="color: #67c23a"> 已启用 </span>
            <span v-show="scope.row.autoStart === 0" style="color: #f56c6c"> 未启用 </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" min-width="50">
          <template #default="scope">
            <span v-show="scope.row.status === 0" style="color: #999"> 已禁用 </span>
            <span v-show="scope.row.status === 1" style="color: #e6a23c"> 未启动 </span>
            <span v-show="scope.row.status === 2" style="color: #67c23a"> 运行中 </span>
            <span v-show="scope.row.status === 3" style="color: #f56c6c"> 启动失败 </span>
            <el-button
              v-show="scope.row.status === 1 || scope.row.status === 3"
              style="margin-left: 0"
              inline
              link
              type="primary"
              size="small"
              :icon="CaretTopIcon"
              @click="startRelayServer(scope.row)"
            >
              启动
            </el-button>
            <el-button
              v-show="scope.row.status === 2"
              style="margin-left: 0"
              inline
              link
              type="primary"
              size="small"
              :icon="CaretBottomIcon"
              @click="stopRelayServer(scope.row)"
            >
              停止
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" />
        <el-table-column label="操作" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" :icon="ViewIcon" @click="openModal('edit', scope.row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" :icon="DeleteIcon" @click="removeList(scope.row)"> 删除 </el-button>
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

  <!-- 路由状态模态框 -->
  <el-dialog v-model="routeStateModalVisible" title="路由状态" width="700px" :close-on-click-modal="false">
    <el-table v-loading="routeStateLoading" :data="routeStateData" border>
      <el-table-column label="目标主机" prop="targetHost" />
      <el-table-column label="目标端口" prop="targetPort" width="100" />
      <el-table-column label="请求数量" prop="hitCount" width="120" />
      <el-table-column label="熔断状态" prop="isBreaked" width="100" align="center">
        <template #default="scope">
          <span v-show="scope.row.isBreaked === 0" style="color: #67c23a">正常</span>
          <span v-show="scope.row.isBreaked === 1" style="color: #f56c6c">已熔断</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="resetBreaker(scope.row)">复位熔断</el-button>
          <el-button link type="danger" size="small" @click="breakHost(scope.row)">置为熔断</el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button :loading="routeStateLoading" @click="refreshRouteState">刷新</el-button>
        <el-button type="primary" @click="resetAllBreaker">复位所有</el-button>
        <el-button @click="routeStateModalVisible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 请求编辑模态框 -->
  <el-dialog
    v-model="modalVisible"
    :title="modalMode === 'edit' ? '编辑中继通道' : '添加中继通道'"
    width="550px"
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
      label-width="115px"
      :validate-on-rule-change="false"
    >
      <el-form-item label="名称" prop="name">
        <el-input v-model="modalForm.name" placeholder="请输入中继通道名称" />
      </el-form-item>
      <el-form-item label="主机" prop="host">
        <el-input v-model="modalForm.host" placeholder="192.168.1.1" />
      </el-form-item>
      <el-form-item label="端口" prop="port">
        <el-input v-model="modalForm.port" placeholder="8080" type="number" />
      </el-form-item>
      <el-form-item label="桥接模式" prop="forwardType">
        <el-select v-model="modalForm.forwardType" placeholder="请选择桥接目标类型">
          <el-option label="直接" :value="0" />
          <el-option label="路由" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item v-show="modalForm.forwardType === 1" label="路由规则" prop="routeRules">
        <el-select
          v-model="modalForm.routeRules"
          placeholder="请选择路由规则"
          multiple
          value-key="routeRuleId"
          :collapse-tags="2"
        >
          <el-option
            v-for="item in modalRouteRuleData"
            :key="item.id"
            :label="item.name"
            :value="{
              routeRuleId: item.id,
              routeRuleName: item.name,
              seq: 1,
            }"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-show="modalForm.forwardType === 1" label="路由权重">
        <el-table :data="modalForm.routeRules" style="width: 100%" max-height="250" border size="small">
          <el-table-column prop="routeRuleName" label="路由规则名称" width="180" />
          <el-table-column label="权重">
            <template #default="scope">
              <el-input-number v-model="scope.row.seq" :min="1" :max="100" size="small" />
            </template>
          </el-table-column>
        </el-table>
      </el-form-item>

      <el-form-item v-show="modalForm.forwardType === 0" label="桥接目标URL" prop="forwardUrl">
        <el-input v-model="modalForm.forwardUrl" placeholder="https://www.baidu.com" />
      </el-form-item>
      <el-form-item label="自动运行" prop="autoStart">
        <el-switch v-model="modalForm.autoStart" :active-value="1" :inactive-value="0" style="margin-right: 10px" />
        <el-tooltip
          content="自动运行：如果勾选，则中继服务器会在EAS系统启动后自动启动，否则当EAS系统停机后再运行时需要手动启动这些中继服务器"
          placement="top"
        >
          <div style="display: flex; align-items: center; gap: 5px; color: #999">
            <span>查看说明</span>
            <el-icon><InfoFilled /></el-icon>
          </div>
        </el-tooltip>
      </el-form-item>
      <el-form-item label="重定向覆写" prop="overrideRedirect">
        <el-switch v-model="modalForm.overrideRedirect" :active-value="1" :inactive-value="0" style="margin-right: 10px" />
        <el-tooltip
          content="当桥接目标响应30X重定向时,桥接通道会将其跳转地址统一改为下方填写的‘重定向覆写URL’；不勾选则原样返回目标站重定向。仅对重定向响应生效，普通 200 响应不受影响。"
          placement="top"
        >
          <div style="display: flex; align-items: center; gap: 5px; color: #999">
            <span>查看说明</span>
            <el-icon><InfoFilled /></el-icon>
          </div>
        </el-tooltip>
      </el-form-item>
      <el-form-item v-show="modalForm.overrideRedirect === 1" label="重定向覆写URL" prop="overrideRedirectUrl">
        <el-input
          v-model="modalForm.overrideRedirectUrl"
          placeholder="例如：https://example.com/after-login 或 https://example.com/home"
        />
      </el-form-item>
      <el-form-item label="请求ID策略" prop="requestIdStrategy">
        <el-switch v-model="modalForm.requestIdStrategy" :active-value="1" :inactive-value="0" style="margin-right: 10px" />
        <el-tooltip content="请求ID策略：0-随机生成，1-从请求头获取。勾选表示从请求头获取，需要指定请求头名称" placement="top">
          <div style="display: flex; align-items: center; gap: 5px; color: #999">
            <span>查看说明</span>
            <el-icon><InfoFilled /></el-icon>
          </div>
        </el-tooltip>
      </el-form-item>
      <el-form-item v-show="modalForm.requestIdStrategy === 1" label="请求ID头名称" prop="requestIdHeaderName">
        <el-input v-model="modalForm.requestIdHeaderName" placeholder="例如：X-Request-ID 或 Request-ID" />
      </el-form-item>
      <el-form-item label="业务错误策略" prop="bizErrorStrategy">
        <el-switch v-model="modalForm.bizErrorStrategy" :active-value="1" :inactive-value="0" style="margin-right: 10px" />
        <el-tooltip content="业务错误策略：0-由HTTP状态码决定，1-由业务错误码决定。勾选表示使用业务错误码判断" placement="top">
          <div style="display: flex; align-items: center; gap: 5px; color: #999">
            <span>查看说明</span>
            <el-icon><InfoFilled /></el-icon>
          </div>
        </el-tooltip>
      </el-form-item>
      <el-form-item v-show="modalForm.bizErrorStrategy === 1" label="业务错误码字段" prop="bizErrorCodeField">
        <el-input v-model="modalForm.bizErrorCodeField" placeholder="例如：$.code 或 $.result.errorCode" />
      </el-form-item>
      <el-form-item v-show="modalForm.bizErrorStrategy === 1" label="业务成功码值" prop="bizSuccessCodeValue">
        <el-input v-model="modalForm.bizSuccessCodeValue" placeholder="例如：0 或 success" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="modalVisible = false">取消</el-button>
        <el-button type="primary" :loading="modalLoading" @click="submitModal">
          {{ modalMode === "add" ? "创建" : "保存" }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, markRaw } from "vue";
import { View, Delete, CaretTop, CaretBottom } from "@element-plus/icons-vue";
import type { FormInstance } from "element-plus";
import StdListLayout from "@/soa/std-series/StdListLayout.vue";
import RelayServerService from "@/views/relay/service/RelayServerService";

const ViewIcon = markRaw(View);
const DeleteIcon = markRaw(Delete);
const CaretTopIcon = markRaw(CaretTop);
const CaretBottomIcon = markRaw(CaretBottom);

const modalFormRef = ref<FormInstance>();

const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList, startRelayServer, stopRelayServer } =
  RelayServerService.useRelayServerList();

const { modalVisible, modalLoading, modalMode, modalForm, modalRules, modalRouteRuleData, openModal, resetModal, submitModal } =
  RelayServerService.useRelayServerModal(modalFormRef, loadList);

const {
  routeStateModalVisible,
  routeStateData,
  routeStateLoading,
  showRouteStateModal,
  refreshRouteState,
  resetBreaker,
  resetAllBreaker,
  breakHost,
} = RelayServerService.useRouteStateModal();
</script>

<style scoped>
.copy-icon {
  cursor: pointer;
}
</style>
