import { defineAsyncComponent, type Component } from "vue";

export interface PublicCompEntry {
  biz: string; // 业务域（小写）
  name: string; // 组件文件名（不含 .vue，小写）
  key: string; // "biz:name"，全小写
  path: string; // 完整 import path
}

/**
 * 构建期扫描 views/<biz>/public/*.vue，key = "biz:name"（全小写）。
 * 单层 * 严格对齐 §8，不会扫到 views/<biz>/components/public/ 等历史遗留位置。
 */
const publicCompModules = import.meta.glob<{ default: Component }>(
  "/src/main/resources/web-ui/src/views/*/public/*.vue"
);

/** 从 path 解析 biz 与 name（均转小写） */
const parsePath = (path: string): { biz: string; name: string } => {
  const m = /\/views\/([^/]+)\/public\/([^/]+)\.vue$/.exec(path);
  if (!m) {
    return { biz: "", name: "" };
  }
  return { biz: m[1].toLowerCase(), name: m[2].toLowerCase() };
};

/** 按 name 分组，找出冲突（同一 name 出现在多个域）*/
const buildRegistry = (): {
  registry: Map<string, () => Promise<{ default: Component }>>;
  entries: PublicCompEntry[];
} => {
  const nameToKeys = new Map<string, string[]>();

  for (const path of Object.keys(publicCompModules)) {
    const { name } = parsePath(path);
    const existing = nameToKeys.get(name) ?? [];
    existing.push(path);
    nameToKeys.set(name, existing);
  }

  const registry = new Map<string, () => Promise<{ default: Component }>>();
  const entries: PublicCompEntry[] = [];
  const conflictLines: string[] = [];

  for (const [name, paths] of nameToKeys) {
    if (paths.length > 1) {
      conflictLines.push(`  name="${name}" 冲突于: ${paths.join(", ")}`);
      continue;
    }
    const path = paths[0];
    const { biz } = parsePath(path);
    const key = `${biz}:${name}`;
    registry.set(key, publicCompModules[path] as () => Promise<{ default: Component }>);
    entries.push({ biz, name, key, path });
  }

  if (conflictLines.length > 0) {
    console.warn(
      `[ComPublicCompService] 以下组件因跨域重名被跳过，不会注册：\n${conflictLines.join("\n")}`
    );
  }

  return { registry, entries };
};

const { registry, entries } = buildRegistry();

export default {
  /**
   * 公共组件动态扫描器
   *
   * 扫描每个域 views/<biz>/public/ 下的 .vue，按 biz:name 注册；同名跨域全部不注册。
   */
  usePublicComp() {
    /**
     * 按 "biz:name" 返回已 defineAsyncComponent 包装好的组件，未注册时返回 null。
     * @param key 格式为 "biz:name"（大小写不敏感，内部统一转小写），例如 "qf:qfapprove"
     */
    const resolvePublicComp = (key: string): Component | null => {
      const loader = registry.get(key.toLowerCase());
      if (!loader) {
        return null;
      }
      return defineAsyncComponent(loader);
    };

    /**
     * 列出所有已成功注册的公共组件（供选择器列表使用）。
     */
    const listPublicComps = (): PublicCompEntry[] => entries;

    return { resolvePublicComp, listPublicComps };
  },
};
