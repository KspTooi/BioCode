import { onMounted, ref, computed, type Ref } from "vue";
import { ElMessage } from "element-plus";
import QfProcApi, { type GetProcNodeDefineVo, type LaunchMemberParamDto } from "@/views/qf/api/QfProcApi.ts";
import UserAuthService from "@/views/auth/service/UserAuthService.ts";
import type { GetUserListVo } from "@/views/core/api/UserApi";

/**
 * 流程节点定义预览组件 props
 */
export interface QfProcDefineProps {
  code: string; // 流程模型编码
}

/**
 * 流程节点定义预览组件 emits
 */
export interface QfProcDefineEmits {
  (e: "onLmpUpdate", lmp: LaunchMemberParamDto[]): void; //启动成员参数更新事件
}

export default {
  /**
   * 流程节点定义预览：按模型编码拉取节点列表，供竖排渲染
   * @param props 组件 props，code 为空时不发起请求
   */
  useQfProcDefine(props: QfProcDefineProps, emits: QfProcDefineEmits, bindLmp: Ref<LaunchMemberParamDto[]>) {
    const nodes = ref<GetProcNodeDefineVo[]>([]);
    const loading = ref(false);

    const authStore = UserAuthService.AuthStore();

    // 用户选择器状态
    const userSelectorVisible = ref(false);
    const selectedUserIds = ref<string[]>([]);
    const activeNode = ref<GetProcNodeDefineVo | null>(null);

    // 内部维护已选用户的 entity 列表，用于回显名称
    const selectedUsersMap = ref<Record<string, { id: string; name: string }[]>>({});

    const currentUserDisplayName = computed(() => {
      const info = authStore.userInfo;
      if (!info) {
        return "";
      }
      return info.nickname || info.username || "";
    });

    /**
     * 打开用户选择器
     */
    const onSelectUser = (node: GetProcNodeDefineVo): void => {
      activeNode.value = node;
      
      // 回显已选中的用户ID
      const currentSelected = bindLmp.value
        .filter((item) => item.nodeId === node.nodeId)
        .map((item) => item.memberId);
      selectedUserIds.value = currentSelected;
      
      userSelectorVisible.value = true;
    };

    /**
     * 用户选择提交回调
     */
    const onUserSelected = (vos: GetUserListVo[]): void => {
      if (!activeNode.value) {
        return;
      }
      const nodeId = activeNode.value.nodeId;

      // 1. 更新 selectedUsersMap 用于回显名称
      selectedUsersMap.value[nodeId] = vos.map((v) => ({
        id: String(v.id),
        name: v.nickname || v.username || "",
      }));

      // 2. 过滤掉该节点旧的绑定，并合并新绑定的启动成员参数
      const otherNodesLmp = bindLmp.value.filter((item) => item.nodeId !== nodeId);
      const newLmp: LaunchMemberParamDto[] = vos.map((v) => ({
        nodeId,
        memberId: String(v.id),
      }));

      bindLmp.value = [...otherNodesLmp, ...newLmp];
      emits("onLmpUpdate", bindLmp.value);
    };

    /**
     * 获取已选人员名称列表
     */
    const getSelectedNames = (node: GetProcNodeDefineVo): string[] => {
      const users = selectedUsersMap.value[node.nodeId] || [];
      return users.map((u) => u.name);
    };

    /**
     * 加载流程节点定义
     */
    const loadNodes = async (): Promise<void> => {
      if (!props.code) {
        return;
      }
      loading.value = true;
      try {
        nodes.value = await QfProcApi.getProcNodeDefine({ code: props.code });
      } catch (error) {
        ElMessage.error((error as Error).message ?? "加载流程节点失败");
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      void loadNodes();
    });

    return {
      nodes,
      loading,
      currentUserDisplayName,
      userSelectorVisible,
      selectedUserIds,
      onSelectUser,
      onUserSelected,
      getSelectedNames,
    };
  },
};
