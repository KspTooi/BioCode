---
name: com-seq-fixer
description: 在列表表格列中接入排序快捷修改组件 ComSeqFixer（悬浮弹窗改 seq/排序号/上移下移）。当需要"改排序/修改排序/列表内调序/seq 字段"时使用本 Skill，避免手写弹窗或 editApi 传参错误。
---

# ComSeqFixer 排序快捷修改接入指南

## 1. 选型

| 业务场景 | 方案 | 关键区别 |
| --- | --- | --- |
| 列表行内快速改单个数值排序字段 | `ComSeqFixer` | 悬浮 Popover + 数字输入 + 上/下微调，打开时拉详情、提交时整单回写 |
| 表单弹窗内编辑排序 | 业务模态框 `modalForm.seq` | 不走 ComSeqFixer，与列表快捷改序分离 |
| 批量重排 / 拖拽排序 | 业务自定义 | ComSeqFixer 仅支持单条数值修改 |

统一使用 `ComSeqFixer`，无替代 SOA 组件。

---

## 2. 快速接入

1. `import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue"`
2. 在 `el-table-column` 的 `#default="scope"` 中放置组件
3. 传入 `:id="scope.row.id"`（`string`）
4. 传入 `seq-field`（详情 Vo 与编辑 Dto 中的排序字段名，通常为 `"seq"`）
5. 实现 `get-detail-api`、`edit-api`，`:on-success="loadList"` 刷新列表

---

## 3. 参数契约

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `string` | 是 | - | 当前行数据 ID |
| `seqField` | `string` | 是 | - | 排序字段名（如 `"seq"`），用于从详情读取并写回 Dto |
| `getDetailApi` | `(id: string) => Promise<any>` | 是 | - | 获取详情，返回对象须含 `seqField` 对应字段 |
| `editApi` | `(id: string, dto: any) => Promise<any>` | 是 | - | 提交编辑；`dto` 为详情全量展开后仅覆盖排序字段 |
| `displayValue` | `number \| string` | 否 | - | 按钮展示文案，未传显示「修改排序」 |
| `onSuccess` | `() => void` | 否 | - | 修改成功回调，通常传 `loadList` |

组件无 `v-model`。排序取值范围 `0`～`655350`（与 `el-input-number` 一致）。

---

## 4. 模板（Templates）

### 列表列内联 Api（最简）

`getDetailApi` / `editApi` 直接内联模块 Api；`onSuccess` 绑定列表 `loadList`。

```vue
<el-table-column prop="seq" label="排序" width="90" align="center">
  <template #default="scope">
    <ComSeqFixer
      :id="String(scope.row.id)"
      seq-field="seq"
      :get-detail-api="(id) => XxxApi.getXxxDetails({ id })"
      :edit-api="(id, dto) => XxxApi.editXxx({ id, ...dto })"
      :display-value="scope.row.seq"
      :on-success="loadList"
    />
  </template>
</el-table-column>

<script setup lang="ts">
import ComSeqFixer from "@/soa/com-series/ComSeqFixer.vue";
import XxxApi from "@/views/xxx/api/XxxApi.ts";
// loadList 来自 useXxxList()
</script>
```

### 列表列 + 独立包装函数（编辑 Dto 需裁剪字段）

`editApi` 须显式映射 `EditXxxDto`，禁止把未知字段透传给后端。

```vue
<ComSeqFixer
  :id="String(scope.row.id)"
  seq-field="seq"
  :get-detail-api="getXxxDetailForSeq"
  :edit-api="editXxxSeq"
  :display-value="scope.row.seq"
  :on-success="loadList"
/>

<script setup lang="ts">
import type { GetXxxDetailsVo, EditXxxDto } from "@/views/xxx/api/XxxApi.ts";
import XxxApi from "@/views/xxx/api/XxxApi.ts";

const getXxxDetailForSeq = async (id: string): Promise<GetXxxDetailsVo> => {
  return await XxxApi.getXxxDetails({ id });
};

const editXxxSeq = async (id: string, dto: GetXxxDetailsVo): Promise<void> => {
  const editDto: EditXxxDto = {
    id: dto.id,
    name: dto.name,
    seq: dto.seq,
    // 仅列出 EditXxxDto 要求的字段
  };
  await XxxApi.editXxx(editDto);
};
</script>
```

### 条件禁用（组件无 disabled Prop）

按行状态控制是否渲染，禁止传不存在的 `disabled`。

```vue
<ComSeqFixer
  v-if="scope.row.status === 0"
  :id="String(scope.row.id)"
  seq-field="seq"
  :get-detail-api="(id) => XxxApi.getXxxDetails({ id })"
  :edit-api="(id, dto) => XxxApi.editXxx({ id, name: dto.name, seq: dto.seq })"
  :display-value="scope.row.seq"
  :on-success="loadList"
/>
<span v-else>{{ scope.row.seq }}</span>
```

---

## 5. 陷阱（Traps）

```vue
<!-- ❌ 传 disabled（组件未声明该 Prop） -->
<ComSeqFixer :disabled="scope.row.status !== 0" ... />

<!-- ✅ 用 v-if 或外层 v-show 控制 -->
<ComSeqFixer v-if="scope.row.status === 0" ... />
```

```ts
// ❌ editApi 只传 seq，丢失详情其余必填字段
:edit-api="(id, dto) => XxxApi.editXxx({ id, seq: dto.seq })"

// ✅ 先 getDetailApi 拿全量，组件合并后 editApi 映射完整 EditDto
:edit-api="(id, dto) => XxxApi.editXxx({ id, ...dto })"
// 或显式构造 EditXxxDto（见 §4 第二模板）
```

```vue
<!-- ❌ id 用 number，与 Prop string 不一致 -->
<ComSeqFixer :id="scope.row.id" ... />

<!-- ✅ 统一 String -->
<ComSeqFixer :id="String(scope.row.id)" ... />
```

```ts
// ❌ getDetailApi 返回 Result 包装对象，未解包
return await XxxApi.getXxxDetails({ id }); // 若 Api 返回 Result，须取 .data

// ✅ 返回含 seqField 的 plain 详情对象
const res = await XxxApi.getXxxDetails({ id });
return res; // 与项目 Api 层约定一致（多数 getDetails 已直接返回 Vo）
```

```ts
// ❌ seqField 与后端字段名不一致
seq-field="sort"

// ✅ 与 GetXxxDetailsVo / EditXxxDto 字段名一致
seq-field="seq"
```

---

## 6. 源码索引

- `src/main/resources/web-ui/src/soa/com-series/ComSeqFixer.vue` — 排序快捷修改 UI（Popover + 数字输入 + 确认）
- `src/main/resources/web-ui/src/soa/com-series/service/ComSeqFixerService.ts` — `useSeqQuickPopover`：拉详情、合并 Dto、调 editApi
- `src/main/resources/web-ui/src/views/auth/GroupManager.vue` — 独立 `getGroupDetailForSeq` / `editGroupSeq` 示例
- `src/main/resources/web-ui/src/views/core/Post.vue` — 内联 `getPostDetails` / `editPost` 示例
- `src/main/resources/web-ui/src/views/qf/QfModel.vue` — 内联 Api + 字段裁剪 `editQfModel` 示例
