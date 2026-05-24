<template>
  <StdPgLayout>
    <div style="padding: 24px">
      <!-- 多选模式 -->
      <el-card header="基础能力演示（多选）" style="margin-bottom: 20px">
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

        <el-button size="small" type="danger" style="margin-left: 16px" @click="multiIds = []">清除已选</el-button>
        <el-form label-width="100px" style="margin-top: 16px">
          <el-form-item label="选择组织">
            <InputOrgTree
              v-model="multiIds"
              v-model:checked-org-names="multiNames"
              width="500px"
              title="选择组织机构"
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
              {{ vo.name }}（{{ vo.kind === 0 ? "企业" : vo.kind === 1 ? "子企业" : "部门" }}）
            </el-tag>
            <span v-if="multiVos.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 裁剪演示 -->
      <el-card header="裁剪演示（根据左侧选定的组织机构剪裁可选的组织范围）" style="margin-bottom: 20px">
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
              <el-form-item label="选择组织">
                <InputOrgTree
                  v-model="cropIds"
                  v-model:checked-org-names="cropNames"
                  :crop-org-id="cropOrgId"
                  width="450px"
                  title="裁剪演示"
                  placeholder="请选择剪裁范围内的组织机构"
                  @on-submit-entity="(vos) => (cropVos = vos)"
                />
              </el-form-item>
              <el-form-item label="已选ID">
                <el-tag v-for="id in cropIds" :key="id" style="margin-right: 4px">{{ id }}</el-tag>
                <span v-if="cropIds.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
              </el-form-item>
              <el-form-item label="VO数据">
                <el-tag v-for="vo in cropVos" :key="vo.id" type="success" style="margin-right: 4px">
                  {{ vo.name }}
                </el-tag>
                <span v-if="cropVos.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>

      <!-- 仅可选企业演示 -->
      <el-card header="只能选部门" style="margin-bottom: 20px">
        <el-form label-width="100px">
          <el-form-item label="选择部门">
            <InputOrgTree
              v-model="enterpriseIds"
              v-model:checked-org-names="enterpriseNames"
              width="500px"
              title="仅可选部门"
              placeholder="只能选择部门"
              :show-kind-tag="true"
              :check-enable-method="(node: GetOrgTreeVo) => node.kind === 2"
              @on-submit-entity="(vos) => (enterpriseVos = vos)"
            />
          </el-form-item>
          <el-form-item label="已选ID">
            <el-tag v-for="id in enterpriseIds" :key="id" style="margin-right: 4px">{{ id }}</el-tag>
            <span v-if="enterpriseIds.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
          <el-form-item label="VO数据">
            <el-tag v-for="vo in enterpriseVos" :key="vo.id" type="success" style="margin-right: 4px">
              {{ vo.name }}（kind={{ vo.kind }}）
            </el-tag>
            <span v-if="enterpriseVos.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card header="联动选择" style="margin-bottom: 20px">
        <el-form label-width="100px">
          <el-form-item label="先选企业">
            <InputOrgTree
              v-model="casOrgIds"
              v-model:checked-org-names="casOrgNames"
              width="500px"
              title="先选企业"
              placeholder="先选择企业"
              :show-kind-tag="true"
              :check-enable-method="(node: GetOrgTreeVo) => node.kind !== 2"
              :exclude-node-method="(node: GetOrgTreeVo) => node.kind !== 2"
              @on-submit-entity="(vos) => (casOrgVos = vos)"
            />
          </el-form-item>
          <el-form-item label="再选部门">
            <InputOrgTree
              v-model="casDeptIds"
              v-model:checked-org-names="casDeptNames"
              width="500px"
              title="再选部门"
              placeholder="再选择部门"
              :show-kind-tag="true"
              :check-enable-method="(node: GetOrgTreeVo) => node.kind === 2"
              :crop-org-id="casOrgIds[0]"
              :disabled="casOrgIds.length > 0 ? false : true"
              @on-submit-entity="(vos) => (casDeptVos = vos)"
            />
          </el-form-item>
          <el-form-item label="已选ID">
            <el-tag v-for="id in casDeptIds" :key="id" style="margin-right: 4px">{{ id }}</el-tag>
            <span v-if="casDeptIds.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
          </el-form-item>
          <el-form-item label="VO数据">
            <el-tag v-for="vo in casDeptVos" :key="vo.id" type="success" style="margin-right: 4px">
              {{ vo.name }}（kind={{ vo.kind }}）
            </el-tag>
            <span v-if="casDeptVos.length === 0" style="color: var(--el-text-color-placeholder)">暂无</span>
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
    </template>
  </StdPgLayout>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import StdPgLayout from "@/soa/std-series/StdPgLayout.vue";
import InputOrgTree from "@/views/core/public/InputOrgTree.vue";
import OrgTree from "@/views/core/public/OrgTree.vue";
import type { GetOrgListVo, GetOrgTreeVo } from "@/views/core/api/OrgApi";

const multiIds = ref<string[]>([]);
const multiVos = ref<GetOrgListVo[]>([]);
const multiNames = ref<string>("");
const isReadonly = ref(false);
const isSingle = ref(false);
const maxLimit = ref(0);

const enterpriseIds = ref<string[]>([]);
const enterpriseVos = ref<GetOrgListVo[]>([]);
const enterpriseNames = ref<string>("");

const cropOrgId = ref<string | null>(null);
const cropIds = ref<string[]>([]);
const cropVos = ref<GetOrgListVo[]>([]);
const cropNames = ref<string>("");

const casOrgIds = ref<string[]>([]);
const casOrgVos = ref<GetOrgListVo[]>([]);
const casOrgNames = ref<string>("");

const casDeptIds = ref<string[]>([]);
const casDeptVos = ref<GetOrgListVo[]>([]);
const casDeptNames = ref<string>("");

/**
 * 如果裁剪根有变动，则清空再选部门的选择
 */
watch(casOrgIds, (newVal) => {
  if (newVal) {
    console.log("cropOrgId changed to:", newVal);
    casDeptIds.value = [];
    casDeptVos.value = [];
    casDeptNames.value = "";
  }
});

const onClose = (): void => {
  console.log("onClose");
};

const propsTableData = [
  { name: "placeholder", type: "string", required: "否", default: '"请选择组织机构"', desc: "输入框占位符" },
  { name: "readonly", type: "boolean", required: "否", default: "false", desc: "是否只读，只读时按钮显示查看且不可选" },
  { name: "title", type: "string", required: "否", default: '"选择组织机构"', desc: "模态框标题（透传 ModalOrgTree）" },
  { name: "width", type: "string | number", required: "否", default: '"450px"', desc: "模态框宽度（透传 ModalOrgTree）" },
  { name: "mode", type: '"single" | "multiple"', required: "否", default: '"single"', desc: "选择模式（透传 ModalOrgTree）" },
  { name: "max", type: "number | null", required: "否", default: "null", desc: "限制最大选择数量（透传 ModalOrgTree）" },
  {
    name: "cropOrgId",
    type: "string | null",
    required: "否",
    default: "null",
    desc: "左侧组织树裁剪根ID（透传 ModalOrgTree）",
  },
];

const emitsTableData = [
  { name: "on-submit-entity", payload: "data: GetOrgListVo[]", desc: "模态框提交时触发，返回选中的组织机构VO列表" },
  { name: "on-close", payload: "—", desc: "模态框关闭时触发" },
];

const vModelTableData = [
  { name: "v-model", type: "string[]", desc: "当前已选组织机构ID数组（双向绑定）" },
  { name: "v-model:checked-org-names", type: "string", desc: "当前已选组织机构名称，用于回显（双向绑定）" },
];
</script>
