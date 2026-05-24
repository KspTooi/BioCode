<!--
  * 组织机构树
  * 其他未声明props全量透传给底层的StdAdvTree组件 具体参考StdAdvTree组件的属性 @see StdAdvTree.vue
  * 这里只轻量级包装了StdAdvTree组件 使其支持组织机构树的特有功能
-->
<template>
  <StdAdvTree
    ref="advTreeRef"
    v-bind="$attrs"
    v-model="bindCheckedOrgId"
    :nr="nrVisible"
    :nr-value="nrValue"
    :data="dataWithIcons"
    :loading="loading"
    :ni="ICON_FIELD"
    :exclude-node-method="excludeNodeMethod"
    :check-enable-method="checkEnableMethod"
    @on-select="emit('on-select', $event)"
    @on-root-select="emit('on-root-select', $event)"
    @on-add="emit('on-add', $event)"
    @on-edit="emit('on-edit', $event)"
    @on-remove="emit('on-remove', $event)"
    @on-search="emit('on-search', $event)"
    @on-refresh="emit('on-refresh', $event)"
  >
    <template v-if="showKindTag === true" #append="{ data: nodeData }">
      <el-tag v-if="nodeData.kind === 0" size="small" type="primary" class="kind-tag">企业</el-tag>
      <el-tag v-if="nodeData.kind === 1" size="small" type="warning" class="kind-tag">子企业</el-tag>
      <el-tag v-if="nodeData.kind === 2" size="small" type="info" class="kind-tag">部门</el-tag>
    </template>
  </StdAdvTree>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import OrgApi, { type GetOrgTreeVo } from "@/views/core/api/OrgApi";
import StdAdvTree from "@/soa/std-series/StdAdvTree.vue";

/**
 * 组织机构树参数
 */
const props = withDefaults(
  defineProps<{
    //是否显示根节点
    nr?: boolean;

    //根组织机构ID
    nrValue?: string;

    //左侧组织树裁剪根ID 将会以该ID为根节点进行裁剪 只显示该组织及下级组织
    cropOrgId?: string;

    //是否显示kind标签
    showKindTag?: boolean;

    //排除节点方法 如果返回false则排除该节点
    excludeNodeMethod?: (node: GetOrgTreeVo) => boolean;

    //禁用节点方法 如果返回false则禁用该节点
    checkEnableMethod?: (node: GetOrgTreeVo) => boolean;
  }>(),
  {
    cropOrgId: null,
    nr: false,
    nrValue: null,
    showKindTag: false,
    readonly: false,
    excludeNodeMethod: undefined,
    checkEnableMethod: undefined,
  }
);

//当前已选组织机构ID 外部用v-model绑定
const bindCheckedOrgId = defineModel<string | number>({ default: null });

/**
 * 组织机构树事件发射器
 */
const emit = defineEmits<{
  (e: "on-select", node: GetOrgTreeVo): void;
  (e: "on-root-select", value: string): void;
  (e: "on-add", node: GetOrgTreeVo): void;
  (e: "on-edit", node: GetOrgTreeVo): void;
  (e: "on-remove", node: GetOrgTreeVo): void;
  (e: "on-search", value: string): void;
  (e: "on-refresh", value: string): void;
  (e: "on-exception", error: Error): void;
}>();

//计算nr是否显示
const nrVisible = computed(() => {
  //如果有裁剪根ID 直接不显示nr
  return props.nr && !props.cropOrgId;
});

const ICON_FIELD = "_icon";

const rawData = ref<GetOrgTreeVo[]>([]);
const loading = ref(false);

const iconMap: Record<number, string> = {
  0: "ep:office-building",
  1: "mdi:domain",
  2: "mdi:sitemap",
  3: "mdi:account-group",
};

const mapIcons = (nodes: GetOrgTreeVo[]): GetOrgTreeVo[] =>
  nodes.map((node) => ({
    ...node,
    [ICON_FIELD]: iconMap[node.kind] ?? "",
    children: node.children?.length ? mapIcons(node.children) : [],
  }));

// 在整棵树中查找指定id的节点，返回该节点（含其子孙）
const findNode = (nodes: GetOrgTreeVo[], id: string): GetOrgTreeVo | null => {
  for (const node of nodes) {
    if (node.id === id) {
      return node;
    }
    if (node.children?.length) {
      const found = findNode(node.children, id);
      if (found) {
        return found;
      }
    }
  }
  return null;
};

/**
 * 计算组织机构树数据
 * 如果设置了裁剪根ID，则只返回裁剪根ID的节点及下级节点
 * 否则返回所有节点
 */
const dataWithIcons = computed(() => {
  const withIcons = mapIcons(rawData.value);
  if (!props.cropOrgId) {
    return withIcons;
  }
  const cropped = findNode(withIcons, props.cropOrgId);
  return cropped ? [cropped] : [];
});

const advTreeRef = ref();

const loadTreeData = async (): Promise<void> => {
  loading.value = true;
  try {
    rawData.value = await OrgApi.getOrgTree({});
  } catch (error: any) {
    //抛出异常到外部
    emit("on-exception", error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadTreeData();

  //如果有剪裁根ID 向外推送剪裁根ID
  if (props.cropOrgId) {
    bindCheckedOrgId.value = props.cropOrgId;
  }
});

defineExpose({ loadTreeData, advTreeRef });
</script>

<style scoped lang="scss">
.kind-tag {
  margin-left: 8px;
  flex-shrink: 0;
}
</style>
