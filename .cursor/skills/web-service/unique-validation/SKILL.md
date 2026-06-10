---
name: unique-validation
description: 新增/编辑时校验字段唯一性（如 code、name 不可重复）。当需要"唯一性校验/重复校验/code 不能重复/名称已存在/字段去重"时使用本 Skill，避免新增和编辑各写一套校验逻辑。
---

# 字段唯一性校验接入指南

## 1. 选型

| 业务场景 | 方案 | 关键区别 |
| --- | --- | --- |
| 新增时校验字段唯一 | `countByXxxExcludeId(xxx, null)` | id 传 null，SpEL 短路排除条件，等价全量 COUNT |
| 编辑时校验字段唯一（排除自身） | `countByXxxExcludeId(xxx, dto.getId())` | id 传自身，SpEL 追加 `t.id != :id`，跳过自己 |

统一使用一个 Repository 方法，通过 SpEL `:#{#id} IS NULL` 处理 id 为 null 的场景，新增和编辑复用同一入口。

---

## 2. 快速接入

1. Repository 新增 `countByXxxExcludeId` 方法，`@Query` 内 SpEL `:#{#id} IS NULL OR t.id != :id`
2. Service `addXxx` 调用 `repository.countByXxxExcludeId(dto.getXxx(), null) > 0` 早退抛 BizException
3. Service `editXxx` 调用 `repository.countByXxxExcludeId(dto.getXxx(), dto.getId()) > 0` 早退抛 BizException
4. 校验放在 Po 查询之前，先快速失败再查库

---

## 3. 参数契约

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `xxx` | `String` / `Long` | 是 | - | 待校验的字段值（如 code、name） |
| `id` | `Long` | 否 | `null` | 排除自身的 ID；null 时不排除任何记录 |
| 返回值 | `Long` / `int` | - | - | 匹配记录数，`> 0` 表示存在重复 |

方法名格式：`countBy<FieldName>ExcludeId`，如 `countByCodeExcludeId`、`countByNameExcludeId`。

---

## 4. 模板（Templates）

### Repository：SpEL 动态排除（以 code 字段为例）

```java
/**
 * 根据编码统计用户组数量，排除指定ID（id为null时不排除）
 *
 * @param code 编码
 * @param id   排除的ID，可为null
 * @return 数量
 */
@Query("""
        SELECT COUNT(t) FROM GroupPo t
        WHERE t.code = :code AND (:#{#id} IS NULL OR t.id != :id)
        """)
int countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);
```

关键点：`:#{#id} IS NULL` 用 SpEL 在 JPQL 编译时判断参数是否为 null，为 null 时短路 `OR` 右侧，不生成排除条件。

### Service：新增传 null

```java
@Transactional(rollbackFor = Exception.class)
public void addXxx(AddXxxDto dto) throws BizException {
    if (repository.countByCodeExcludeId(dto.getCode(), null) > 0) {
        throw new BizException("唯一编码已存在,请更换后重试.");
    }
    XxxPo insertPo = as(dto, XxxPo.class);
    repository.save(insertPo);
}
```

### Service：编辑传自身 ID（校验前置）

```java
@Transactional(rollbackFor = Exception.class)
public void editXxx(EditXxxDto dto) throws BizException {
    if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) {
        throw new BizException("唯一编码已存在,请更换后重试.");
    }
    XxxPo updatePo = repository.findById(dto.getId())
            .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));
    assign(dto, updatePo);
    repository.save(updatePo);
}
```

### Repository：name 字段唯一性校验示例

```java
/**
 * 根据名称统计任务分组数量，排除指定ID（id为null时不排除）
 *
 * @param name 名称
 * @param id   排除的ID，可为null
 * @return 数量
 */
@Query("""
        SELECT COUNT(t) FROM QtTaskGroupPo t
        WHERE t.name = :name AND (:#{#id} IS NULL OR t.id != :id)
        """)
Long countByNameExcludeId(@Param("name") String name, @Param("id") Long id);
```

---

## 5. 陷阱（Traps）

```java
// ❌ 新增用 findByCode 查实体，编辑再单独写 countByNameExcludeId——两套逻辑
XxxPo existing = repository.findByCode(dto.getCode());
if (existing != null) { throw ...; }
```

```java
// ✅ 新增和编辑统一用 countByCodeExcludeId，id 传 null 或自身
if (repository.countByCodeExcludeId(dto.getCode(), null) > 0) { throw ...; }
if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) { throw ...; }
```

```java
// ❌ 编辑时用 != 拼接 JPQL，id 非 null 时生成无效 SQL
@Query("SELECT COUNT(t) FROM XxxPo t WHERE t.code = :code AND t.id != :id")
// id 为 null 时 WHERE t.id != null 永远 false，新增永远返回 0
```

```java
// ✅ SpEL :#{#id} IS NULL OR t.id != :id——null 时短路，非 null 时才追加排除
@Query("""
        SELECT COUNT(t) FROM XxxPo t
        WHERE t.code = :code AND (:#{#id} IS NULL OR t.id != :id)
        """)
```

```java
// ❌ 校验放在 findById 之后——先查库拿实体，再校验，多余一次 IO
XxxPo updatePo = repository.findById(dto.getId()).orElseThrow(...);
if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) { throw ...; }
```

```java
// ✅ 校验放在 findById 之前——快速失败，不浪费查询
if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) { throw ...; }
XxxPo updatePo = repository.findById(dto.getId()).orElseThrow(...);
```

---

## 6. 源码索引

- `src/main/java/com/ksptool/bio/biz/auth/repository/GroupRepository.java` — `countByCodeExcludeId`（SpEL 统一排除，code 字段）
- `src/main/java/com/ksptool/bio/biz/auth/service/GroupService.java` — `addGroup` / `editGroup` 统一调用 `countByCodeExcludeId`
- `src/main/java/com/ksptool/bio/biz/qf/repository/QfModelRepository.java` — `countByCodeExcludeId`（SpEL 统一排除，code 字段）
- `src/main/java/com/ksptool/bio/biz/qf/service/QfModelService.java` — `addQfModel` 调用 `countByCodeExcludeId` 示例
- `src/main/java/com/ksptool/bio/biz/core/repository/PostRepository.java` — `countByCodeExcludeId`（SpEL 统一排除，code 字段）
- `src/main/java/com/ksptool/bio/biz/qt/repository/QtTaskGroupRepository.java` — `countByNameExcludeId`（name 字段示例）
- `src/main/java/com/ksptool/bio/biz/qt/service/QtTaskGroupService.java` — `addQtTaskGroup` / `editQtTaskGroup` 唯一性校验示例
