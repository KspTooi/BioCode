<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <!-- 多选模式（默认） -->
      <el-card header="基础能力演示" style="margin-bottom: 20px">
        <el-checkbox v-model="isReadonly" style="margin-right: 16px">只读模式</el-checkbox>
        <el-checkbox v-model="isSingle" style="margin-right: 16px">单选模式</el-checkbox>

        <el-input-number
          v-model="maxLimit"
          :min="0"
          :step="1"
          controls-position="right"
          placeholder="数量限制"
          style="width: 140px"
        />

        <span style="margin-left: 6px; color: var(--el-text-color-secondary); font-size: 13px">数量限制（0=不限）</span>

        <el-button size="small" type="danger" @click="multiVos = []">删除Vo数据</el-button>
        <el-form label-width="100px" style="margin-top: 16px">
          <el-form-item label="选择用户">
            <InputUserSelector
              v-model="multiIds"
              v-model:checked-user-names="multiNames"
              width="85%"
              title="基础能力演示"
              :mode="isSingle ? 'single' : 'multiple'"
              :readonly="isReadonly"
              :max="maxLimit || undefined"
              @on-submit-entity="(vos) => (multiVos = vos)"
              @on-close="onClose"
            />
          </el-form-item>
          <el-form-item label="已选ID">
            <el-tag v-for="id in multiIds" :key="id" style="margin-right: 4px">{{ id }}</el-tag>
            <span v-if="multiIds.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
          <el-form-item label="VO数据">
            <el-tag v-for="vo in multiVos" :key="vo.id" type="success" style="margin-right: 4px">
              {{ vo.nickname }}（{{ vo.username }}） - {{ vo.phone }}
            </el-tag>
            <span v-if="multiVos.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 裁剪演示 -->
      <el-card header="裁剪演示（根据左侧选定的组织机构剪裁可选的用户范围）">
        <div style="display: flex; gap: 24px">
          <div style="width: 260px; flex-shrink: 0; border: 1px solid var(--el-border-color); border-radius: 4px; padding: 8px">
            <div style="font-size: 13px; color: var(--el-text-color-secondary); margin-bottom: 8px">选择裁剪根节点</div>
            <OrgTree style="height: 300px" @on-select="(node) => (cropOrgId = node.id)" />
          </div>
          <div style="flex: 1">
            <el-form label-width="100px">
              <el-form-item label="裁剪根ID">
                <el-tag v-if="cropOrgId" type="warning">{{ cropOrgId }}</el-tag>
                <span v-else style="color: var(--el-text-color-placeholder)">未选择（不裁剪）</span>
                <el-button size="small" style="margin-left: 8px" @click="cropOrgId = null">清除</el-button>
              </el-form-item>
              <el-form-item label="选择用户">
                <InputUserSelector
                  v-model="cropIds"
                  v-model:checked-user-names="cropNames"
                  :crop-org-id="cropOrgId"
                  width="85%"
                  title="裁剪演示"
                  placeholder="请选择剪裁范围内的用户"
                  @on-submit-entity="(vos) => (cropVos = vos)"
                />
              </el-form-item>
              <el-form-item label="已选ID">
                <el-tag v-for="id in cropIds" :key="id" style="margin-right: 4px">{{ id }}</el-tag>
                <span v-if="cropIds.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
              </el-form-item>
              <el-form-item label="VO数据">
                <el-tag v-for="vo in cropVos" :key="vo.id" type="success" style="margin-right: 4px">
                  {{ vo.nickname }}（{{ vo.username }}）
                </el-tag>
                <span v-if="cropVos.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>

      <!-- 初始组织演示 -->
      <el-card header="初始组织演示（选择打开模态框时自动选中的初始组织机构）" style="margin-top: 20px">
        <div style="display: flex; gap: 24px">
          <div style="width: 260px; flex-shrink: 0; border: 1px solid var(--el-border-color); border-radius: 4px; padding: 8px">
            <div style="font-size: 13px; color: var(--el-text-color-secondary); margin-bottom: 8px">选择初始组织节点</div>
            <OrgTree style="height: 300px" @on-select="(node) => (initOrgId = node.id)" />
          </div>
          <div style="flex: 1">
            <el-form label-width="100px">
              <el-form-item label="初始组织ID">
                <el-tag v-if="initOrgId" type="warning">{{ initOrgId }}</el-tag>
                <span v-else style="color: var(--el-text-color-placeholder)">未选择（默认根节点）</span>
                <el-button size="small" style="margin-left: 8px" @click="initOrgId = null">清除</el-button>
              </el-form-item>
              <el-form-item label="选择用户">
                <InputUserSelector
                  v-model="initIds"
                  v-model:checked-user-names="initNames"
                  v-model:current-org-id="initOrgId"
                  width="85%"
                  title="初始组织演示"
                  @on-submit-entity="(vos) => (initVos = vos)"
                />
              </el-form-item>
              <el-form-item label="已选ID">
                <el-tag v-for="id in initIds" :key="id" style="margin-right: 4px">{{ id }}</el-tag>
                <span v-if="initIds.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
              </el-form-item>
              <el-form-item label="VO数据">
                <el-tag v-for="vo in initVos" :key="vo.id" type="success" style="margin-right: 4px">
                  {{ vo.nickname }}（{{ vo.username }}）
                </el-tag>
                <span v-if="initVos.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>

      <!-- #button 插槽演示：仅替换按钮 -->
      <el-card header="#button 插槽演示（仅替换触发按钮，保留 el-input）" style="margin-top: 20px">
        <el-form label-width="100px">
          <el-form-item label="选择用户">
            <InputUserSelector v-model="slotButtonIds" v-model:checked-user-names="slotButtonNames">
              <template #button="{ open }">
                <el-button type="warning" :icon="EditIcon" @click="open">自定义按钮</el-button>
              </template>
            </InputUserSelector>
          </el-form-item>
          <el-form-item label="已选ID">
            <el-tag v-for="id in slotButtonIds" :key="id" style="margin-right: 4px">{{ id }}</el-tag>
            <span v-if="slotButtonIds.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 默认插槽演示：完全自定义触发区域 -->
      <el-card header="默认插槽演示（完全替换 el-input，自定义触发区域）" style="margin-top: 20px">
        <el-form label-width="100px">
          <el-form-item label="选择用户">
            <InputUserSelector v-model="slotDefaultIds" v-model:checked-user-names="slotDefaultNames">
              <template #default="{ open, displayText }">
                <div style="display: inline-flex; align-items: center; gap: 8px">
                  <el-button type="success" @click="open">点击选择人员</el-button>
                  <span v-if="displayText" style="color: var(--el-text-color-primary); font-size: 13px"
                    >已选：{{ displayText }}</span
                  >
                  <span v-else style="color: var(--el-text-color-placeholder); font-size: 13px">未选择</span>
                </div>
              </template>
            </InputUserSelector>
          </el-form-item>
          <el-form-item label="已选ID">
            <el-tag v-for="id in slotDefaultIds" :key="id" style="margin-right: 4px">{{ id }}</el-tag>
            <span v-if="slotDefaultIds.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <template #props>
      <el-table :data="propsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="属性名" width="200" />
        <el-table-column prop="type" label="类型" width="200" />
        <el-table-column prop="required" label="必填" width="80" />
        <el-table-column prop="default" label="默认值" width="150" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>

    <template #emits>
      <el-table :data="emitsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="事件名" width="220" />
        <el-table-column prop="payload" label="参数" width="250" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>

      <el-divider content-position="left">v-model 双向绑定</el-divider>

      <el-table :data="vModelTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="绑定名" width="240" />
        <el-table-column prop="type" label="类型" width="200" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>

      <el-divider content-position="left">插槽</el-divider>

      <el-table :data="slotsTableData" stripe border style="width: 100%">
        <el-table-column prop="name" label="插槽名" width="160" />
        <el-table-column prop="params" label="参数" width="280" />
        <el-table-column prop="desc" label="说明" min-width="200" />
      </el-table>
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { Edit as EditIcon } from "@element-plus/icons-vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import InputUserSelector from "@/views/core/public/InputUserSelector.vue";
import OrgTree from "@/views/core/public/OrgTree.vue";
import type { GetUserListVo } from "@/views/core/api/UserApi";

const slotButtonIds = ref<string[]>([]);
const slotButtonNames = ref<string>("");

const slotDefaultIds = ref<string[]>([]);
const slotDefaultNames = ref<string>("");

const initOrgId = ref<string | null>(null);
const initIds = ref<string[]>([]);
const initVos = ref<GetUserListVo[]>([]);
const initNames = ref<string>("");

const cropOrgId = ref<string | null>(null);
const cropIds = ref<string[]>([]);
const cropVos = ref<GetUserListVo[]>([]);
const cropNames = ref<string>("");

const multiIds = ref<string[]>([]);
const multiVos = ref<GetUserListVo[]>([]);
const multiNames = ref<string>("");
const isReadonly = ref(false);
const isSingle = ref(false);
const maxLimit = ref(0);

watch(isSingle, (newVal) => {
  if (newVal) {
    multiIds.value = [];
    multiVos.value = [];
    multiNames.value = "";
  }
});

const onClose = (): void => {
  console.log("onClose");
};

const propsTableData = [
  { name: "placeholder", type: "string", required: "否", default: '"请选择用户"', desc: "输入框占位符" },
  { name: "readonly", type: "boolean", required: "否", default: "false", desc: "是否只读，只读时按钮显示查看且不可选" },
  { name: "title", type: "string", required: "否", default: '"选择用户"', desc: "模态框标题（透传 ModalUserSelector）" },
  { name: "width", type: "string | number", required: "否", default: '"80%"', desc: "模态框宽度（透传 ModalUserSelector）" },
  {
    name: "mode",
    type: '"single" | "multiple"',
    required: "否",
    default: '"multiple"',
    desc: "选择模式（透传 ModalUserSelector）",
  },
  { name: "max", type: "number | null", required: "否", default: "null", desc: "限制最大选择数量（透传 ModalUserSelector）" },
  {
    name: "cropOrgId",
    type: "string | null",
    required: "否",
    default: "null",
    desc: "左侧组织树裁剪根ID（透传 ModalUserSelector）",
  },
];

const emitsTableData = [
  { name: "on-submit-entity", payload: "data: GetUserListVo[]", desc: "模态框提交时触发，返回选中的用户VO列表" },
  { name: "on-close", payload: "—", desc: "模态框关闭时触发" },
];

const vModelTableData = [
  { name: "v-model", type: "string[]", desc: "当前已选用户ID数组（双向绑定）" },
  { name: "v-model:checked-user-names", type: "string", desc: "当前已选用户姓名，用于回显（双向绑定）" },
  { name: "v-model:current-org-id", type: "string | null", desc: "当前选中组织ID（双向绑定）" },
];

const slotsTableData = [
  { name: "default", params: "{ open, displayText }", desc: "完全自定义触发区域，替换默认的 el-input + 按钮" },
  { name: "button", params: "{ open }", desc: "仅替换触发按钮，保留 el-input 只读输入框" },
];
</script>
