# 查询持久化接入指南

## 1. 选型

| 业务场景 | 用法 | 关键区别 |
| --- | --- | --- |
| 列表页查询条件需跨页面刷新保留 | Service 层接入 `QueryPersistService` + `StdListAreaQuery` 开启 `showPersistTip` | 三步联动：onMounted 恢复 → loadList 存 → resetList 清 |
| 查询区显示持久化提示标识 | `<StdListAreaQuery show-persist-tip>` | 仅视觉提示，实际存取由 Service 完成 |
| 多 Tab 页共享持久化 key | 各 Tab 使用不同 `prefix` | 避免 Tab 间查询条件互相覆盖 |
| 不需持久化（默认） | 什么都不做 | 大多数列表页无需此功能 |

**强制规则**：接入查询持久化的列表页必须同时满足：
1. Service 层 `onMounted` 调用 `loadQuery` 恢复
2. Service 层 `loadList` 成功后调用 `persistQuery` 存储
3. Service 层 `resetList` 调用 `clearQuery` 清除
4. 模板层 `StdListAreaQuery` 开启 `show-persist-tip`

四者缺一不可。

---

## 2. 快速接入

1. 在 `*Service.ts` 中 `import QueryPersistService from "@/commons/service/QueryPersistService.ts"`
2. 在 `useXxxList()` 中确定一个全局唯一的持久化 `prefix`（建议取 `kebab-case`，如 `"user-manager"`、`"qt-task"`）
3. `onMounted` 内，`loadList()` 之前调用 `QueryPersistService.loadQuery(prefix, listForm.value)`
4. `loadList` 内，请求成功后调用 `QueryPersistService.persistQuery(prefix, listForm.value)`
5. `resetList` 内，清除表单后再调用 `QueryPersistService.clearQuery(prefix)`
6. 对应 `.vue` 中 `<StdListAreaQuery show-persist-tip>` 开启标识

---

## 3. 参数契约

### QueryPersistService

| 方法 | 签名 | 说明 |
| --- | --- | --- |
| `persistQuery` | `(prefix: string, query: Reactive<any>) => void` | 将查询条件写入 localStorage；值为空（`null`/`undefined`/`""`）的 key 会被删除 |
| `loadQuery` | `(prefix: string, query: Reactive<any>) => void` | 从 localStorage 读取已保存的查询条件回填；自动处理 `number`、`string`、JSON 类型 |
| `clearQuery` | `(prefix: string) => void` | 清除 `prefix` 下所有已保存的查询条件 |

**存储格式**：`localStorage.setItem(prefix + "_" + key, value)`

### StdIndicatorQueryPersist（视觉标识）

无 Props，无 v-model。仅作为 `StdListAreaQuery` 的内部子组件渲染一个青色图标 + tooltip 文案。

| 属性 | 说明 |
| --- | --- |
| 图标 | `IClarityBlockSolidAlerted`，颜色 `#0d9488` |
| tooltip | 「查询持久化指示器: 此页面已启用查询条件持久化，重新加载页面后会自动恢复上次的查询条件」 |

### StdListAreaQuery 的 showPersistTip

| Prop | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `show-persist-tip` | `boolean` | 否 | `false` | 为 `true` 时在查询区左上角渲染持久化指示器图标 |

---

## 4. 模板（Templates）

### Service 层完整接入

```ts
import { onMounted, ref } from "vue";
import QueryPersistService from "@/commons/service/QueryPersistService.ts";
import XxxApi from "@/views/xxx/api/XxxApi.ts";

export default {
  useXxxList() {
    const listForm = ref<GetXxxListDto>({
      pageNum: 1,
      pageSize: 20,
      name: "",
      status: null,
    });

    const listData = ref<GetXxxListVo[]>([]);
    const listTotal = ref(0);
    const listLoading = ref(false);

    const loadList = async (): Promise<void> => {
      listLoading.value = true;
      const result = await XxxApi.getXxxList(listForm.value);

      if (Result.isSuccess(result)) {
        listData.value = result.data;
        listTotal.value = result.total;
        // 查询成功后持久化
        QueryPersistService.persistQuery("xxx-list", listForm.value);
      }

      listLoading.value = false;
    };

    const resetList = (): void => {
      listForm.value.pageNum = 1;
      listForm.value.pageSize = 20;
      listForm.value.name = "";
      listForm.value.status = null;
      // 重置时清除持久化
      QueryPersistService.clearQuery("xxx-list");
      loadList();
    };

    onMounted(async () => {
      // 先恢复持久化条件，再请求
      QueryPersistService.loadQuery("xxx-list", listForm.value);
      await loadList();
    });

    return { listForm, listData, listTotal, listLoading, loadList, resetList };
  },
};
```

### 模板层（Vue）

```vue
<StdListContainer>
  <StdListAreaQuery show-persist-tip>
    <el-form :model="listForm" inline class="flex justify-between">
      <div>
        <el-form-item label="名称">
          <el-input v-model="listForm.name" placeholder="请输入名称" clearable />
        </el-form-item>
      </div>
      <el-form-item>
        <el-button type="primary" :disabled="listLoading" @click="loadList">查询</el-button>
        <el-button :disabled="listLoading" @click="resetList">重置</el-button>
      </el-form-item>
    </el-form>
  </StdListAreaQuery>

  <StdListAreaAction>...</StdListAreaAction>

  <StdListAreaTable v-model:list-form="listForm" :list-total="listTotal" :load-list="loadList">
    <el-table v-loading="listLoading" :data="listData" stripe border height="100%">
      <!-- 列定义 -->
    </el-table>
  </StdListAreaTable>
</StdListContainer>
```

### 经 StdListLayout

```vue
<StdListLayout show-persist-tip>
  <template #query>
    <el-form :model="listForm" inline>...</el-form>
  </template>
  <template #actions>...</template>
  <template #table>
    <el-table :data="listData" stripe border height="100%">...</el-table>
  </template>
</StdListLayout>
```

---

## 5. 陷阱（Traps）

❌ 只开了 `show-persist-tip` 但 Service 未接入三方法
```vue
<StdListAreaQuery show-persist-tip>...</StdListAreaQuery>
```
```ts
// Service 未 import QueryPersistService，未调用 persistQuery/loadQuery/clearQuery
```

✅ 标识 + Service 四者缺一不可
```ts
onMounted(async () => {
  QueryPersistService.loadQuery("xxx-list", listForm.value);
  await loadList();
});
const loadList = async () => {
  // 请求成功后
  QueryPersistService.persistQuery("xxx-list", listForm.value);
};
const resetList = () => {
  // 重置后
  QueryPersistService.clearQuery("xxx-list");
};
```

---

❌ `loadQuery` 在 `loadList` 之后调用（先请求再回填，导致首次请求不带持久化条件）
```ts
onMounted(async () => {
  await loadList();
  QueryPersistService.loadQuery("xxx-list", listForm.value); // ❌ 太晚
});
```

✅ 先恢复后请求
```ts
onMounted(async () => {
  QueryPersistService.loadQuery("xxx-list", listForm.value);
  await loadList();
});
```

---

❌ `prefix` 与其它页面重复
```ts
QueryPersistService.persistQuery("list", listForm.value); // ❌ 过于通用，多页冲突
```

✅ 取页面级唯一前缀
```ts
QueryPersistService.persistQuery("user-manager", listForm.value);
QueryPersistService.persistQuery("qt-task", listForm.value);
```

---

❌ `resetList` 只清内存未清 localStorage
```ts
const resetList = () => {
  listForm.value.name = "";
  loadList();
};
```

✅ 同时调用 clearQuery
```ts
const resetList = () => {
  listForm.value.name = "";
  QueryPersistService.clearQuery("xxx-list");
  loadList();
};
```

---

❌ `listForm` 非 `Ref`/`Reactive` 直接传原始对象
```ts
QueryPersistService.loadQuery("xxx", listForm); // ❌ listForm 为 ref 时传 .value 才正确
```

✅ 传 `.value`（`Ref<T>` 场景）或直接传 `reactive`
```ts
QueryPersistService.loadQuery("xxx", listForm.value); // Ref 场景
QueryPersistService.loadQuery("xxx", listForm);       // reactive 场景（直接传）
```

---

## 6. 源码索引

| 文件 | 职责 |
| --- | --- |
| `src/main/resources/web-ui/src/commons/service/QueryPersistService.ts` | 持久化核心服务：`persistQuery` / `loadQuery` / `clearQuery` |
| `src/main/resources/web-ui/src/soa/std-series/StdIndicatorQueryPersist.vue` | 查询区左上角持久化指示器图标（`showPersistTip` 驱动） |
| `src/main/resources/web-ui/src/soa/std-series/StdListAreaQuery.vue` | 查询区容器，承载 `showPersistTip` prop |
| `src/main/resources/web-ui/src/views/core/service/UserManagerService.ts` | 接入示例：`ref` + 三方法 |
| `src/main/resources/web-ui/src/views/qt/service/QtTaskService.ts` | 接入示例：`ref` + 三方法 |
| `src/main/resources/web-ui/src/views/core/UserManager.vue` | 模板示例：`show-persist-tip` |
| `src/main/resources/web-ui/src/views/qt/QtTask.vue` | 模板示例：`show-persist-tip` |
