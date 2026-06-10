---
name: ids-diff
description: 编辑关联表时计算多对多关系的增删差异。当需要"更新关联/绑定关系/差量新增删除/批量关联/多对多增减/IdsDiff/关联表"时使用本 Skill，避免手写 set 差集或全删全插。
---

# IdsDiff 关联表差量更新指南

## 1. 选型

| 业务场景 | 方案 | 关键区别 |
| --- | --- | --- |
| 编辑时更新多对多关联（如用户组↔部门、包↔菜单） | `new IdsDiff(existIds, inputIds)` → 差量 add / remove | 只插增量、只删多余，不改不动已有项 |
| 新增时绑定首批关联 | 直接 `saveAll` 新建关联 Po | 无已存在数据，无需 Diff |
| 删除时清空所有关联 | `deleteAll` 或 `@Modifying DELETE` | 不关心差量，全清 |

不适用场景：关联表有余量字段（seq 等）需逐条维护，Diff 无法感知余量变化。

---

## 2. 快速接入

1. Repository 新增 `getXxxIdsByYyyId` 查询已存在 ID 列表，返回 `List<Long>`
2. Repository 新增 `removeByYyyIdAndXxxIds` 按父 ID + 子 ID 列表批量删除，`@Modifying` + `clearAutomatically`
3. Service 编辑方法中 `new IdsDiff(repository.getXxxIdsByYyyId(parentId), dto.getXxxIds())`
4. `if (idsDiff.hasAdd())` → 构建关联 Po 列表 → `saveAll`
5. `if (idsDiff.hasRemove())` → 调用 Repository 的 remove 方法

---

## 3. 参数契约

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `existsIds` | `Collection<Long>` | 是 | - | 库里已存在的子 ID 集合，允许 null（视为空集） |
| `inputIds` | `Collection<Long>` | 是 | - | 前端传入的子 ID 集合，允许 null（视为空集） |
| `getAddIds()` | `List<Long>` | - | - | 输入有但库里没有的，视为"需新增" |
| `getRemoveIds()` | `List<Long>` | - | - | 库里有但输入没有的，视为"需删除" |
| `hasAdd()` | `boolean` | - | - | 是否有需新增的 ID |
| `hasRemove()` | `boolean` | - | - | 是否有需删除的 ID |

---

## 4. 模板（Templates）

### Repository：查询已关联 ID 列表

```java
/**
 * 根据用户组ID获取已关联的组织机构ID列表
 *
 * @param groupId 用户组ID
 * @return 组织机构ID列表
 */
@Query("""
        SELECT u.deptId FROM GroupDeptPo u WHERE u.groupId = :groupId
        """)
List<Long> getDeptIdsByGroupId(@Param("groupId") Long groupId);
```

方法名固定格式：`get<Child>IdsBy<Parent>Id`，如 `getDidsByGid`、`getPidsByGid`、`getMidsByPid`。

### Repository：批量删除关联（按父 ID + 子 ID 列表）

```java
/**
 * 根据用户组ID和组织机构ID列表删除关联
 *
 * @param groupId 用户组ID
 * @param deptIds 组织机构ID列表
 */
@Modifying(clearAutomatically = true)
@Query("""
        DELETE FROM GroupDeptPo u WHERE u.groupId = :groupId AND u.deptId IN :deptIds
        """)
void removeByGidAndDids(@Param("groupId") Long groupId, @Param("deptIds") List<Long> deptIds);
```

`@Modifying(clearAutomatically = true)` 确保同一事务后续查询能读到最新数据。

### Service：差量更新关联

```java
@Transactional(rollbackFor = Exception.class)
public void editGroup(EditGroupDto dto) throws BizException {
    GroupPo g = groupRepository.findById(dto.getId())
            .orElseThrow(() -> new BizException("用户组不存在或无权限访问."));

    assign(dto, g);

    var gdIdsDiff = new IdsDiff(gdRepository.getDidsByGid(g.getId()), dto.getDeptIds());

    if (gdIdsDiff.hasAdd()) {
        var gdPos = gdIdsDiff.getAddIds().stream()
                .map(deptId -> new GroupDeptPo(g.getId(), deptId)).toList();
        gdRepository.saveAll(gdPos);
    }

    if (gdIdsDiff.hasRemove()) {
        gdRepository.removeByGidAndDids(g.getId(), gdIdsDiff.getRemoveIds());
    }
}
```

### 完整 edit 模板（含关联表 Diff）

```java
@Transactional(rollbackFor = Exception.class)
public void editXxx(EditXxxDto dto) throws BizException {
    XxxPo po = repository.findById(dto.getId())
            .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

    assign(dto, po);
    repository.save(po);

    var idsDiff = new IdsDiff(relRepository.getRelIdsByParentId(dto.getId()), dto.getRelIds());

    if (idsDiff.hasAdd()) {
        var relPos = idsDiff.getAddIds().stream()
                .map(relId -> new RelPo(dto.getId(), relId)).toList();
        relRepository.saveAll(relPos);
    }

    if (idsDiff.hasRemove()) {
        relRepository.removeByParentIdAndRelIds(dto.getId(), idsDiff.getRemoveIds());
    }
}
```

---

## 5. 陷阱（Traps）

```java
// ❌ 全删全插——多一次 DELETE + INSERT 全部，浪费且可能触发行锁/序列问题
relRepository.removeByParentId(dto.getId());
relRepository.saveAll(newRelPos);
```

```java
// ✅ 差量更新——只删真正移除的，只插真正新增的
var diff = new IdsDiff(existIds, inputIds);
if (diff.hasAdd()) { relRepository.saveAll(toAdd); }
if (diff.hasRemove()) { relRepository.removeByParentIdAndRelIds(pid, diff.getRemoveIds()); }
```

```java
// ❌ 手写 Set 差集——容易漏判 null 或空集合
Set<Long> addIds = new HashSet<>(inputIds);
addIds.removeAll(existIds);
```

```java
// ❌ @Modifying 不带 clearAutomatically——同一事务后续查关联可能读到旧数据
@Modifying
@Query("DELETE FROM RelPo u WHERE u.parentId = :pid AND u.childId IN :ids")
void removeByPidAndCids(@Param("pid") Long pid, @Param("ids") List<Long> ids);
```

```java
// ✅ 带 clearAutomatically——清除持久化上下文，后续查询走 DB
@Modifying(clearAutomatically = true)
```

```java
// ❌ inputIds 为 null 时未处理——调用方忘记判空，IdsDiff 构造函数 NPE
// IdsDiff 内部已处理：null 场景转为空 Set，无需外部判空
```

---

## 6. 源码索引

- `src/main/java/com/ksptool/bio/biz/core/common/IdsDiff.java` — IdsDiff 源码
- `src/main/java/com/ksptool/bio/biz/auth/service/GroupService.java` — `editGroup` 中 `IdsDiff` 处理组↔部门、组↔权限、组↔菜单三组关联
- `src/main/java/com/ksptool/bio/biz/core/service/CoreRootService.java` — `IdsDiff` 处理租户↔包关联
- `src/main/java/com/ksptool/bio/biz/core/service/PackService.java` — `IdsDiff` 处理包↔菜单关联
- `src/main/java/com/ksptool/bio/biz/auth/repository/GroupDeptRepository.java` — `getDidsByGid` / `removeByGidAndDids` 示例
