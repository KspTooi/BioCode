import { ref, watch } from "vue";
import { useRoute } from "vue-router";

const searchExpandStateMap = new Map<string, ReturnType<typeof ref<boolean>>>();
const SEARCH_EXPAND_STORAGE_PREFIX = "search-expand:";

/**
 * 搜索表单展开收起 Hook
 * @returns { isExpand, toggleExpand }
 */
export function useSearchExpand(key?: string): {
  isExpand: ReturnType<typeof ref<boolean>>;
  toggleExpand: () => void;
} {
  const route = useRoute();
  const stateKey = key ?? route.path ?? "default";
  const storageKey = SEARCH_EXPAND_STORAGE_PREFIX + stateKey;

  if (!searchExpandStateMap.has(stateKey)) {
    const storageValue = sessionStorage.getItem(storageKey);
    const initialValue = storageValue === "true";

    searchExpandStateMap.set(stateKey, ref(initialValue));
  }

  const isExpand = searchExpandStateMap.get(stateKey);

  if (!isExpand) {
    throw new Error("search expand state init failed");
  }

  watch(
    isExpand,
    (value) => {
      sessionStorage.setItem(storageKey, String(value));
    },
    {
      immediate: true,
    }
  );

  const toggleExpand = (): void => {
    isExpand.value = !isExpand.value;
  };

  return {
    isExpand,
    toggleExpand,
  };
}
