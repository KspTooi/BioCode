---
name: layout-provider
description: 接入与扩展 SOA 布局管理器（ComLayoutProvider、ComLayoutProviderService）。当需要"布局/layout/默认布局/blank布局/切换框架/注册布局/setDefaultLayout/ComLayoutProvider"时使用本 Skill，避免误用 meta.layout 或 setDefaultLayout 无效。
---

# 布局管理器接入指南

## 架构

| 角色 | 文件 |
| --- | --- |
| 根挂载 | `AdminRoot.vue` → `<com-layout-provider />` |
| 布局渲染器 | `soa/com-series/ComLayoutProvider.vue` |
| 注册与解析 | `soa/com-series/service/ComLayoutProviderService.ts` |

`ComLayoutProvider` 根据当前路由 `meta.layout` 动态 `<component :is="currentLayout" />`。

---

## 启动时注册（AdminMain.ts，须在 `createApp` 之前）

```ts
import ComLayoutProviderService from "@/soa/com-series/service/ComLayoutProviderService.ts";
import ComFramework from "@/soa/com-series/ComFramework.vue";
import ComFrameworkOldSchool from "@/soa/com-series/ComFrameworkOldSchool.vue";

ComLayoutProviderService.registerLayout("framework", ComFramework);
ComLayoutProviderService.registerLayout("old-school", ComFrameworkOldSchool);
ComLayoutProviderService.setDefaultLayout("old-school");
```

| API | 说明 |
| --- | --- |
| `registerLayout(name, component)` | 注册或替换具名布局 |
| `setDefaultLayout(name)` | 设置全局默认布局（`name` 须已注册） |
| `hasLayout(name)` / `getLayout(name)` | 查询是否已注册 / 获取组件 |

内置布局：`blank`（裸 `<router-view>`，无需注册）。

---

## 路由 meta.layout 解析规则

| `meta.layout` | 实际渲染 |
| --- | --- |
| 未设置 / `null` | `RouteEntryPo` 会补为 `"default"` → 走**当前默认布局**（`setDefaultLayout`） |
| `"default"` | **当前默认布局**（`setDefaultLayout`），**不是** `layouts["default"]` 键 |
| `"blank"` | 内置空白布局（无侧栏/顶栏） |
| 其他已注册名（如 `"framework"`） | 对应注册组件 |
| 未知名 | 回退到当前默认布局；仍无则显示红色提示文案 |

**强制规则**：不要把业务默认布局注册在键名 `"default"` 上并指望 `setDefaultLayout` 生效——`meta.layout === "default"` 始终表示「当前默认布局」，与注册表键 `"default"` 无关。具名布局请用 `framework`、`old-school` 等。

---

## 路由侧指定布局

**固定路由**（`AdminMain.ts` 的 `grsFixedRoutes`）：

```ts
meta: { layout: "blank" }  // 404、401、登录页等
```

**业务路由**（`RouteEntryPo`）：`meta.layout` 为空时自动设为 `"default"`，即走 `setDefaultLayout`。

单条路由强制某布局：

```ts
meta: { layout: "framework" }  // 仅此路由用新框架，其余仍走默认
```

---

## 新增自定义布局组件

1. 新建布局 SFC（须包含 `<router-view />` 承载页面内容，参考 `ComFramework.vue`）。
2. `AdminMain.ts` 中 `registerLayout("my-layout", MyLayout)`。
3. 需要全局默认时：`setDefaultLayout("my-layout")`；仅部分路由：`meta: { layout: "my-layout" }`。

运行时替换：

```ts
ComLayoutProviderService.registerLayout("framework", NewFramework);
```

---

## 常见陷阱

- **setDefaultLayout 无效**：业务路由 `meta.layout` 已被 `RouteEntryPo` 填为 `"default"`；服务已将其解析为 `setDefaultLayout` 目标，勿再注册键 `"default"` 与 `setDefaultLayout` 混用。
- **未注册默认布局**：未 `setDefaultLayout` 且 `layouts["default"]` 也不存在时，页面显示红色提示；至少执行 `registerLayout` + `setDefaultLayout`，或 `registerLayout("default", Component)` 且不调用 `setDefaultLayout`（回退键名 `default`）。
- **import 路径**：统一 `@/soa/com-series/service/ComLayoutProviderService.ts`，勿用 `.js` 后缀，避免模块双实例导致注册丢失。
- **布局组件职责**：只负责外壳（菜单、标签、顶栏）；页面内容由内部 `<router-view />` 渲染。

## 源码

- `src/main/resources/web-ui/src/soa/com-series/ComLayoutProvider.vue`
- `src/main/resources/web-ui/src/soa/com-series/service/ComLayoutProviderService.ts`
- `src/main/resources/web-ui/src/AdminRoot.vue`
- `src/main/resources/web-ui/src/AdminMain.ts`
- `src/main/resources/web-ui/src/soa/genric-route/api/RouteEntryPo.ts`（`meta.layout` 默认值）
