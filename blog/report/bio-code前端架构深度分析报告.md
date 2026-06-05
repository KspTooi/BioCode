# bio-code 前端架构深度分析报告

---

## 1. 整体架构

### 1.1 技术栈全览

| 维度 | 技术选型 | 版本 |
|------|---------|------|
| 框架 | Vue 3 (Composition API) | ~3.5.33 |
| 语言 | TypeScript | ~6.0.3 |
| 构建工具 | Vite | ~8.0.10 |
| UI 组件库 | Element Plus | ~2.13.7 |
| CSS 框架 | Tailwind CSS | ~4.2.4 |
| 状态管理 | Pinia + pinia-plugin-persistedstate | ~3.0.4 / ~4.7.1 |
| 路由 | Vue Router (Hash 模式) | ~5.0.6 |
| HTTP 客户端 | Axios | ~1.15.2 |
| 包管理 | pnpm | 10.33.0 |
| 代码检查 | ESLint + Prettier | ~10.3.0 / ~3.8.3 |
| 类型检查 | vue-tsc | ~3.2.7 |
| 图标 | Element Plus Icons + Iconify | - |
| 工具库 | @vueuse/core, sass-embedded | ~14.3.0 / ~1.99.0 |
| 文件预览 | @vue-office (docx/excel/pdf) | - |
| 流程设计 | bpmn-js + 自研 flowable-designer | ~18.10.0 |
| 代码编辑器 | monaco-editor-vue3 | ~0.1.10 |
| 图表 | ECharts + vue-echarts | ~6.0.0 / ~8.0.1 |
| Markdown | marked + highlight.js | ~15.0.7 / ~11.11.1 |
| Cron | cron-parser + cronstrue | ~5.5.0 / ~3.12.0 |
| 拖拽 | vuedraggable | ~4.1.0 |
| 分割面板 | splitpanes | ~4.0.4 |
| 图片裁剪 | vue-cropper | ~1.1.4 |
| 哈希计算 | hash-wasm (Web Worker) | ~4.12.0 |

### 1.2 应用入口

项目没有使用传统的 `main.ts` / `App.vue` 命名，而是采用语义化命名：

- **AdminMain.ts**：应用入口，创建 Vue 实例、初始化 Pinia、注册布局和路由、挂载
- **AdminRoot.vue**：根组件，仅作为最外层容器，渲染 `ComLayoutProvider` 布局管理器

初始化流程：

```typescript
const app = createApp(AdminRoot);
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);

// 注册布局
ComLayoutProviderService.registerLayout("default", ComFramework);
ComLayoutProviderService.setDefaultLayout("default");

// 注册路由注册器
const { initialize, addRoute } = GenricRouteService.useGenricRoute();
addRoute(new AuthRouteRegister());
addRoute(new CoreRouteRegister());
// ...更多路由注册器

initialize(app, grsFixedRoutes);
app.mount("#app");
```

### 1.3 路由模式

统一使用 **Hash 模式** (`createWebHashHistory`)。路由管理通过自研 **GenricRouteService (GRS)** 体系实现，禁止外部直接操作 Vue Router 的 addRoute/removeRoute。

路由路径构建规则：`/<biz>/<path>`
- 例如 `biz: "auth", path: "login"` 构建为 `/auth/login`
- `biz` 为 `null` 时直接使用 `/<path>`

固定路由包括 `index`、`NotFound`(404)、`no-permission`(401)、`external-link`(iframe 外链)。

### 1.4 状态管理方案

使用 **Pinia + pinia-plugin-persistedstate** 进行状态管理和持久化：
- **AuthStore**：用户认证信息 (sessionId + userInfo)，通过 localStorage 手动持久化
- **TabStore**：标签页管理 (`np_soa_tabs`)，自动持久化
- **MenuStore**：菜单展开状态 (`np_soa_menu_opened`)，自动持久化

---

## 2. 目录结构设计

### 2.1 整体结构

```
src/
├── AdminMain.ts                    # 应用入口 (替代 main.ts)
├── AdminRoot.vue                   # 根组件 (替代 App.vue)
├── assets/tailwind.css             # Tailwind 入口
├── styles/                         # 全局样式
│   ├── element-theme.scss          # Element Plus 主题覆盖
│   ├── element-btn.scss
│   ├── element-confirm.scss
│   ├── element-modal.scss
│   └── element-search.scss
├── commons/                        # 共享基础设施
│   ├── Http.ts                     # Axios 封装
│   ├── Iconify.ts                  # Iconify 图标注册
│   ├── FileUtils.ts                # 文件工具
│   ├── model/                      # 通用数据模型
│   │   ├── Result.ts               # 后端响应结果接口
│   │   ├── PageQuery.ts            # 分页查询基类
│   │   ├── PageResult.ts           # 分页结果接口
│   │   ├── CommonIdDto.ts          # 通用 ID DTO
│   │   └── RestPageableView.ts
│   └── service/                    # 通用 Service
│       ├── QueryPersistService.ts  # 查询持久化
│       ├── GenricHotkeyService.ts  # 快捷键服务
│       ├── ContentTypeService.ts
│       ├── ElmentFocusService.ts
│       └── FileCategoryService.ts
├── soa/                            # SOA 通用组件体系 ★核心
│   ├── com-series/                 # 框架级组件
│   │   ├── ComLayoutProvider.vue   # 布局管理器
│   │   ├── ComFramework.vue        # 主框架 (菜单+标签页+面包屑)
│   │   ├── ComLeftMenu.vue         # 完整侧边菜单
│   │   ├── ComLeftMenuShort.vue    # 折叠侧边菜单
│   │   ├── ComMultiTab.vue         # 多标签页
│   │   ├── ComBreadcrumb.vue       # 面包屑
│   │   ├── ComSeqFixer.vue         # 排序修改器
│   │   ├── ComCronFixer.vue        # Cron 查看器
│   │   ├── ComUserProfile.vue      # 用户头像+下拉菜单
│   │   ├── ImportWizardModal.vue   # 导入向导
│   │   └── service/                # 配套 Service
│   │       ├── ComLayoutProviderService.ts
│   │       ├── ComFrameworkService.ts
│   │       ├── ComTabService.ts    # 标签页 Pinia Store
│   │       ├── ComMenuService.ts   # 菜单 Pinia Store
│   │       └── ComSeqFixerService.ts
│   ├── std-series/                 # 标准列表页组件
│   │   ├── StdListContainer.vue    # 最外层容器
│   │   ├── StdListAreaQuery.vue    # 查询区域
│   │   ├── StdListAreaTable.vue    # 表格+分页
│   │   ├── StdListAreaAction.vue   # 操作按钮区域
│   │   ├── StdListLayout.vue       # 快捷组合布局
│   │   ├── StdAdvTree.vue          # 高级树组件
│   │   ├── StdIframe.vue           # iframe 容器
│   │   ├── StdIconPicker.vue       # 图标选择器
│   │   └── ...
│   ├── genric-route/               # 路由注册机制
│   │   ├── api/RouteEntryPo.ts     # 路由条目 PO
│   │   ├── service/GenricRouteService.ts
│   │   ├── service/GenricRouteRegister.ts
│   │   └── GrConflictOverlay.vue   # 冲突检测覆盖层
│   └── template/                   # CRUD 代码生成模板
│       ├── api/ExampleApi.ts
│       ├── service/ExampleService.ts
│       └── Example.vue
├── views/                          # 业务视图模块
│   ├── auth/                       # 认证授权 (login, userGroup, session, permission, profile)
│   ├── core/                       # 核心管理 (org, user, menu, post, pack, registry, notice, ...)
│   ├── assembly/                   # 代码生成装配
│   ├── qf/                         # 流程引擎
│   ├── qt/                         # 任务调度
│   └── audit/                      # 审计日志
└── components/                     # 全局共享组件
    └── RequestPreviewModal.vue
```

**关键架构特征**：
- 没有 `router/`、`store/`、`utils/`、`global/` 独立目录 —— 路由管理下沉到 SOA，状态管理内聚在 Service
- 每个业务域内部有标准化子目录：`api/`、`service/`、`route/`、`components/`、`sfc_private/`

### 2.2 commons/ 共享模块

| 文件 | 职责 |
|------|------|
| `Http.ts` | Axios 实例封装，统一拦截器、错误处理、多种请求方法 |
| `Iconify.ts` | 注册 Element Plus、Material Design、Bootstrap、Carbon、Line MD 图标集 |
| `model/Result.ts` | 统一响应接口 `Result<T> { code, message, data }`，含 `isSuccess()`/`isError()` 辅助函数 |
| `model/PageQuery.ts` | 分页查询基类 `{ pageNum, pageSize }` |
| `model/PageResult.ts` | 分页结果 `{ code, message, data: T[], total }` |
| `model/CommonIdDto.ts` | 通用 ID DTO `{ id?, ids? }` |
| `service/QueryPersistService.ts` | 查询条件 localStorage 持久化/恢复/清除 |
| `service/GenricHotkeyService.ts` | 全局快捷键 (Ctrl+1~9, F5, Enter, Delete, Esc 等) |

---

## 3. SOA 组件体系（核心架构）

### 3.1 ComSeries —— 框架级组件

#### 3.1.1 ComLayoutProvider 布局管理器

`ComLayoutProvider.vue` 使用 `<component :is="currentLayout" />` 动态渲染布局。核心 Service 方法：

```typescript
registerLayout(name: string, component: Component): void    // 注册布局组件
getLayout(name: string): Component | undefined              // 获取布局
hasLayout(name: string): boolean                            // 检查是否存在
setDefaultLayout(name: string): void                        // 设置默认布局
useFrameworkLayout(): { currentLayout: ComputedRef<Component> }  // 根据 route.meta.layout 动态解析
```

- `meta.layout === "default"` → 使用 `setDefaultLayout` 指定的布局
- `meta.layout === "blank"` → 使用空白布局（用于登录页、404/401 页）

#### 3.1.2 ComFramework 主框架

组件结构：
- **左侧**：`ComLeftMenu` / `ComLeftMenuShort` (折叠时切换)
- **顶部**：`ComMultiTab` (多标签页) + `ComUserProfile` + 用户通知
- **头部**：菜单折叠按钮 + `ComBreadcrumb` 面包屑
- **内容**：`router-view` (keep-alive 支持)

通过 `viewKey = route.fullPath + refreshCounter` 强制重建非 keep-alive 页面。

#### 3.1.3 ComSeqFixer 排序修改器

Props 定义：
```typescript
{
  id: string;                                          // 数据ID
  seqField: string;                                    // 排序字段名称
  getDetailApi: (id: string) => Promise<any>;          // 获取详情接口
  editApi: (id: string, dto: any) => Promise<any>;     // 编辑接口
  displayValue?: number | string;                      // 显示值
  onSuccess?: () => void;                              // 成功回调
}
```

交互模式：通过 `el-popover` 悬浮触发，内部有 `el-input-number` 和上/下箭头快速调整。Service 提供 `useSeqQuickPopover()` 封装完整交互逻辑。

#### 3.1.4 ComCronFixer Cron 查看器

Props：`{ cron: string, displayValue?: string }`

功能：语义化解析 (cronstrue 中文输出) + 未来 5 次执行时间计算 (cron-parser + Asia/Shanghai 时区)。纯展示组件。

#### 3.1.5 ComTabService 标签页服务

基于 Pinia Store 的完整标签系统：

```typescript
interface Tab {
  id: string;           // 标签唯一ID
  icon: string | null;  // 标签图标
  title: string;        // 标签标题
  path: string;         // 标签路径
  closable?: boolean;   // 是否可关闭
  kind: "normal" | "iframe";  // 标签类型
}
```

关键方法：
- `openTab(tab)` - 创建新标签并激活
- `closeTab(tabId)` - 关闭标签（固定标签不可关闭）
- `refreshActiveTab()` - 刷新计数器+1，强制重建视图
- `useRouterTabService()` - 路由同步层，监听 `route.path` 自动同步 `activeTabId`

持久化 key: `np_soa_tabs`

#### 3.1.6 ComMenuService 菜单服务

基于 Pinia Store (`menuStore`)：
- 后端加载菜单树 (`MenuApi.getUserMenuTree()`)
- 支持菜单类型：`kind: 0` 目录, `kind: 1` 菜单项, `kind: 3` iframe, `kind: 4` 外部链接
- `openMenu(item)` 根据 kind 决定跳转方式
- 自动过滤无权限菜单
- 持久化菜单展开状态 (`np_soa_menu_opened`)

#### 3.1.7 路由上下文机制

- **ComDirectRouteContext (CDRC)**：页面间带上下文跳转，支持跳转→目标→回源的数据传递。使用 sessionStorage + TTL 管理生命周期
  - `cdrcRedirect(nameOrPath, sendQuery, returnQuery)` - 跳转
  - `cdrcReturn()` - 回源
- **ComOneTimeRouteContext**：一次性上下文跳转，通过 localStorage 传递，读后即删

---

### 3.2 StdSeries —— 标准列表页组件体系

#### 3.2.1 组件关系

```
StdListContainer (最外层容器, 20px padding)
├── StdListAreaQuery (查询区域, 虚线底边 + 持久化指示器)
├── StdListAreaAction (操作按钮区域)
├── StdListAreaTable (表格+分页区域)
│   ├── <slot /> (表格)
│   └── <slot name="pagination"> 或 内置 el-pagination
└── <slot name="modal"> (模态框)
```

#### 3.2.2 StdListAreaTable —— 核心组件

核心 Props：
```typescript
const listForm = defineModel<PageQuery>("listForm", { required: false });  // 双向绑定分页参数
listTotal?: number;   // 数据总条数
loadList?: () => any; // 翻页回调
```

设计亮点：
- 使用 `defineModel` 双向绑定 `listForm`
- 内置智能分页：当 `listForm` + `listTotal` + `loadList` 三要素齐全时自动渲染 `el-pagination`
- 支持 `#pagination` 插槽覆盖内置分页

#### 3.2.3 StdListLayout 快捷布局

组合上述四个原子组件，提供命名插槽：`#query`、`#actions`、`#table`、`#pagination`、`#modal`、`#tutorial`。

#### 3.2.4 查询持久化机制

`QueryPersistService` 提供三个方法：
- `persistQuery(prefix, query)` - 非空值持久化到 localStorage
- `loadQuery(prefix, query)` - 从 localStorage 恢复，自动识别 number/string/object
- `clearQuery(prefix)` - 移除 prefix_ 开头的所有 key

---

### 3.3 GenricRoute —— 路由注册机制

#### 3.3.1 RouteEntryPo

```typescript
class RouteEntryPo {
  biz: string;
  path: string;
  name: string;
  component: () => Promise<Component>;  // 异步懒加载
  meta: {
    keepAlive?: boolean;
    breadcrumb?: string | null;
    layout?: string | null;
  };
}
```

- `validate()` - 校验 path 不含 `/`（biz/path 共同构建最终路径），自动补齐 name/breadcrumb/layout
- `buildPath()` - 若 `biz != null` 返回 `/<biz>/<path>`，否则 `/<path>`

#### 3.3.2 GenricRouteService (GRS)

核心架构设计：
- **禁止外部直接操作 Vue Router**：`vueRouter.addRoute`/`removeRoute` 被重写为抛错误，仅保留内部引用
- **路由冲突检测**：检测同一 biz 出现在多个注册器、buildPath 重复等问题，冲突时覆盖渲染 `GrConflictOverlay`
- **路由注册器模式**：通过 `addRoute(register)` 注册，`initialize()` 时统一注入
- **根路由守卫**：访问 `/` 时恢复上次激活标签页，找不到则回退最近访问的业务标签

```typescript
abstract class GenricRouteRegister {
  abstract doRegister(): RouteEntryPo[];                        // 子类实现
  doBeforeEach(): NavigationGuardWithThis<undefined> { ... }    // 可选前置守卫
  doAfterEach(): NavigationHookAfter { ... }                    // 可选后置守卫
}
```

---

## 4. 分层架构

### 4.1 SFC 层 (.vue 文件)

**职责边界**：
- 只负责模板渲染、用户交互绑定、样式
- 不含业务逻辑、不含 HTTP 调用
- 通过解构 Service 的 `useXxx()` 获取状态和方法

标准模式：
```typescript
// 列表管理
const { listForm, listData, listTotal, listLoading, loadList, resetList, removeList } = XxxService.useXxxList();
// 模态框管理
const { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal } = XxxService.useXxxModal(modalFormRef, loadList);
```

### 4.2 API 层

**职责**：
1. 全部类型声明（Dto / Vo / Enum / Options 常量）
2. 全部 HTTP 请求方法

**标准文件结构**：
```typescript
// DTO/VO 类型定义
export interface GetXxxListDto extends PageQuery { ... }
export interface GetXxxListVo { ... }
export interface GetXxxDetailsVo { ... }
export interface AddXxxDto { ... }
export interface EditXxxDto { ... }

// API 方法
export default {
  getXxxList: async (dto: GetXxxListDto): Promise<PageResult<GetXxxListVo>> => {
    return await Http.postEntity<PageResult<GetXxxListVo>>("/xxx/getXxxList", dto);
  },
  getXxxDetails: async (dto: CommonIdDto): Promise<GetXxxDetailsVo> => {
    const result = await Http.postEntity<Result<GetXxxDetailsVo>>("/xxx/getXxxDetails", dto);
    if (result.code === 0) return result.data;
    throw new Error(result.message);
  },
};
```

**硬性规则**：
- 列表查询 Dto 必须 `extends PageQuery`
- 删除统一用 `CommonIdDto`，不新建专用 Dto
- 所有请求走 `Http.postEntity`（文件下载除外）
- ID 字段一律 `string`，Boolean 字段一律 `number` (0/1)
- Api 方法内禁止 `try/catch`
- URL 格式 `/<camelCaseControllerName>/<methodName>`

### 4.3 HTTP 层 (Http.ts)

```typescript
const axiosInstance = axios.create({
  baseURL: "/api",
  headers: { "AE-Request-With": "XHR" },
});
```

**请求拦截器**：自动附加 `Authorization: Bearer <sessionId>`

**响应拦截器**：
- 403 → 跳转 `no-permission` 页
- 401 → 跳转 `/auth/login` + 清除 auth
- 400 + code===1 → 抛出参数校验错误

**请求方法**：
- `postEntity<T>(url, body)` - 过滤空字符串参数，返回拆包 T（code===0），失败抛 Error
- `post<T>(url, body, config)` - 原始 POST，返回 `Result<T>`
- `postForm<T>(url, body)` - FormData 提交

### 4.4 Service 层

**职责**：
1. 管理响应式状态（ref / reactive）
2. 编排调用流程（调用 Api → 处理 Result → 更新状态 → ElMessage 反馈）
3. 暴露 Composition 函数

**标准函数签名**：
```typescript
useXxxList() {
  // 状态: listForm(ref<Dto>), listData(ref<Vo[]>), listTotal(ref), listLoading(ref)
  // 方法: loadList(), resetList(), removeList(row)
  // onMounted 中自动调用 loadList()
  return { listForm, listData, listTotal, listLoading, loadList, resetList, removeList }
}

useXxxModal(modalFormRef: Ref<FormInstance | undefined>, reloadCallback: () => void) {
  // 状态: modalVisible(ref), modalLoading(ref), modalMode(ref<"add"|"edit">), modalForm(reactive<Vo>), modalRules(reactive<FormRules>)
  // 方法: openModal(mode, row?), resetModal(), submitModal()
  return { modalVisible, modalLoading, modalMode, modalForm, modalRules, openModal, resetModal, submitModal }
}
```

---

## 5. 权限控制

### 5.1 前端权限判定

基于 `UserAuthService.usePreAuthorize()`：
- `hasCode(codes)` - 检查用户是否拥有指定权限码（单个或多个，满足其一即可）
- `hasSuper()` - 检查是否超级管理员 (`*:*:*`)
- `v-hasCode` 指令 - 无权限时隐藏元素 (`display: none`)
- `v-hasSuper` 指令 - 超级管理员专属元素

### 5.2 动态菜单

后端返回菜单树，前端根据用户权限自动过滤和渲染（无权限菜单不可见）。

---

## 6. 构建与优化

### 6.1 Vite 配置

- **路径别名**：`@/` → `src/`
- **开发服务器**：`0.0.0.0:27501`，WebSocket 支持
- **代理**：`/api` → `http://127.0.0.1:27500`（移除 `/api` 前缀）
- **构建输出**：`dist/assets/` 扁平化命名
- **关键插件**：`@vitejs/plugin-vue`、`unplugin-vue-components`、`unplugin-icons`、`vite-plugin-checker`

### 6.2 按需加载

所有业务路由使用动态 `import()`：
```typescript
component: () => import("@/views/auth/UserLogin.vue")
```

### 6.3 Tailwind CSS 4

使用 Tailwind CSS v4 语法（`@import "tailwindcss"`），配合 `@tailwindcss/postcss` 和 `autoprefixer`。

---

## 总结：架构亮点与设计思想

1. **SOA 组件体系**：将布局、标签页、菜单、路由等框架能力封装为可插拔的 ComSeries 组件，将列表页布局标准化为 StdSeries 组件，大幅降低页面开发成本

2. **路由管控**：GRS 体系彻底管控路由操作，防止外部绕过导致路由不同步，同时提供冲突检测等安全机制

3. **Service 层标准化**：`useXxxList` + `useXxxModal` 的固定模式让同类页面代码结构高度一致

4. **类型安全**：严格区分 Dto/Vo，ID 统一 `string`，Boolean 统一 `number`(0/1)

5. **查询持久化**：`QueryPersistService` 提供开箱即用的 localStorage 查询条件持久化

6. **标签页机制**：`TabStore` 配合路由守卫实现刷新恢复，`useRouterTabService` 实现路由与标签的双向同步

7. **上下文传递**：CDRC 提供完整的页面间上下文传递方案（sessionStorage + TTL）

8. **代码生成模板**：Template 三件套覆盖 CRUD 全流程，可直接复用生成新模块
