---
name: std-date-time
description: 在表单/查询区接入日期时间范围选择器 StdDateTime。当需要"日期范围/时间范围/开始时间/结束时间/日期选择/时间选择器"时使用本 Skill，避免直接使用 el-date-picker 并手动拆分开始/结束字段。
---

# 日期时间范围选择器接入指南（StdDateTime）

日期/时间范围选择一律走 `StdDateTime`，禁止直接使用 `el-date-picker` 并手动处理 `[start, end]` 数组拆分。

## 1. 选型

| 业务场景 | type 值 | 宽度 | 说明 |
| --- | --- | --- | --- |
| 精确到秒的时间区间 | `"datetimerange"` | 355px（自动） | 含日期 + 时分秒 |
| 仅日期区间（不含时间） | `"daterange"` | 260px（自动） | 年月日 |
| 不传 type | `undefined` | `auto` | 宽度不固定，慎用 |

## 2. 快速接入

1. Import 组件：`import StdDateTime from "@/soa/std-series/StdDateTime.vue"`
2. Service 中声明两个字段：`startTime: ""` 和 `endTime: ""`
3. 模板中绑定 `v-model:start-time` 和 `v-model:end-time`
4. 传入 `type` 决定日期精度（`"datetimerange"` 或 `"daterange"`）
5. 用 `dateFormat` 控制提交给后端的格式字符串

## 3. 参数契约

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `startTime` | `string` | 否 | `undefined` | v-model:start-time，开始时间字符串 |
| `endTime` | `string` | 否 | `undefined` | v-model:end-time，结束时间字符串 |
| `type` | `"datetimerange" \| "daterange"` | 否 | `undefined` | 选择器类型，决定显示精度和自动宽度 |
| `format` | `string` | 否 | `undefined` | 面板显示格式，如 `"YYYY-MM-DD HH:mm:ss"` |
| `dateFormat` | `string` | 否 | `undefined` | 提交值格式（value-format），如 `"YYYY-MM-DD HH:mm:ss"` |
| `startPlaceholder` | `string` | 否 | `"开始时间"` | 开始输入框占位文本 |
| `endPlaceholder` | `string` | 否 | `"结束时间"` | 结束输入框占位文本 |
| `rangeSeparator` | `string` | 否 | `"-"` | 两个输入框之间的分隔符 |

> 其余 `el-date-picker` 原生属性通过 `$attrs` 透传，直接写在组件标签上即可。

## 4. 模板（Templates）

### 查询区：精确到秒的时间范围

```vue
<template>
  <StdDateTime
    v-model:start-time="query.startTime"
    v-model:end-time="query.endTime"
    type="datetimerange"
    date-format="YYYY-MM-DD HH:mm:ss"
  />
</template>

<script setup lang="ts">
import { reactive } from "vue";
import StdDateTime from "@/soa/std-series/StdDateTime.vue";

const query = reactive({
  startTime: "",
  endTime: "",
});
</script>
```

### 查询区：仅日期范围

```vue
<template>
  <StdDateTime
    v-model:start-time="query.startTime"
    v-model:end-time="query.endTime"
    type="daterange"
    date-format="YYYY-MM-DD"
  />
</template>

<script setup lang="ts">
import { reactive } from "vue";
import StdDateTime from "@/soa/std-series/StdDateTime.vue";

const query = reactive({
  startTime: "",
  endTime: "",
});
</script>
```

### 清空时的处理（Service 层）

```ts
// 清空时两个字段都会被设为 ""，后端判空逻辑按 "" 处理即可
function resetQuery(): void {
  query.startTime = "";
  query.endTime = "";
}
```

## 5. 陷阱（Traps）

❌ 直接用 el-date-picker 并手动处理数组：
```vue
<el-date-picker v-model="timeRange" type="datetimerange" @change="onRangeChange" />
```

✅ 改用 StdDateTime，startTime/endTime 自动拆分：
```vue
<StdDateTime v-model:start-time="query.startTime" v-model:end-time="query.endTime" type="datetimerange" />
```

---

❌ 清空后判断 `null`：
```ts
if (query.startTime === null) { ... }
```

✅ 清空后值为 `""`，判断空字符串：
```ts
if (!query.startTime) { ... }
```

---

❌ 绑定 `v-model`（默认）：
```vue
<StdDateTime v-model="timeRange" />
```

✅ 组件无默认 v-model，必须分开绑定：
```vue
<StdDateTime v-model:start-time="xxx" v-model:end-time="xxx" />
```

## 6. 源码索引

- `src/main/resources/web-ui/src/soa/std-series/StdDateTime.vue` — 组件本体，封装 el-date-picker 的双向 startTime/endTime 拆分逻辑
