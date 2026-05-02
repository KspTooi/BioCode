import { ref, type Ref } from "vue";
import OrgApi, { type GetOrgTreeVo } from "@/views/core/api/OrgApi";

export default class OrgTreeService {
  /**
   * 组织架构树逻辑
   */
  public static useOrgTree(): {
    treeData: Ref<GetOrgTreeVo[]>;
    loading: Ref<boolean>;
    filterText: Ref<string>;
    loadTreeData: () => Promise<void>;
    selectedOrg: Ref<GetOrgTreeVo | null>;
    onSelectOrg: (org: GetOrgTreeVo | null) => void;
    getDeptList: (orgId: string) => Promise<void>;
  } {
    const treeData = ref<GetOrgTreeVo[]>([]);
    const loading = ref(false);
    const filterText = ref("");
    const selectedOrg = ref<GetOrgTreeVo | null>(null);

    const onSelectOrg = (org: GetOrgTreeVo | null): void => {
      selectedOrg.value = org;
    };

    const loadTreeData = async (): Promise<void> => {
      loading.value = true;
      try {
        const data = await OrgApi.getOrgTree({ name: filterText.value });
        treeData.value = data;
      } catch (error) {
        console.error("加载组织架构树失败", error);
      } finally {
        loading.value = false;
      }
    };
    const getDeptList = async (orgId: string): Promise<void> => {
      console.log(111, orgId, treeData.value);
    };

    return {
      treeData,
      loading,
      filterText,
      loadTreeData,
      selectedOrg,
      onSelectOrg,
      getDeptList,
    };
  }
}
