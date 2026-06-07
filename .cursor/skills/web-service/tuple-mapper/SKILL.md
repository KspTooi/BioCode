---
name: tuple-mapper
description: TupleMapper/tupleAs/Tuple映射/Tuple投影/JPA投影/Tuple转VO/Tuple转DTO。用于将JPA查询返回的Tuple结果转换为VO或DTO对象。
---

# TupleMapper 投影映射指南

> 本 Skill 指导在 JPA 多表关联查询或自定义投影场景下，如何使用 `TupleMapper` 将查询返回的 `Tuple` 结果映射为目标 VO 或 DTO 对象。

## 1. 选型

| 业务场景 | 对应组件/方案 | 关键区别 |
| --- | --- | --- |
| 单实体或关联实体 Po 直接映射为 Vo/Dto | `Entities.as` / `Entities.assign` | 适用于属性名称完全一致，且不需要多表关联投影的简单映射场景。 |
| JPA 多表关联查询或自定义投影（Tuple）转换为 Vo/Dto | `TupleMapper.tupleAs` | 适用于 JPQL/SQL 多表关联查询返回 `Tuple`，需要按 `AS 驼峰别名` 映射到目标 Vo/Dto 字段的场景。支持基础类型转换、日期时间转换、数字互转。 |

## 2. 快速接入

1. 在 Repository 层编写 JPQL/SQL 查询，使用 `AS 驼峰别名`，使别名与目标 Vo/Dto 字段名完全一致，返回 `Page<Tuple>` 或 `List<Tuple>`。
2. 在 Service 层引入 `import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;`。
3. 在 Service 层调用 `tupleAs(page, GetXxxListVo.class)` 或 `tupleAs(list, GetXxxListVo.class)` 将 Tuple 转换为目标对象。

## 3. 参数契约

| 方法/参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `tupleAs(tuple, targetClass)` | `Tuple`, `Class<T>` | 是 | — | 将单个 `Tuple` 映射为目标类型实例。目标类必须提供无参构造函数。 |
| `tupleAs(tuples, targetClass)` | `List<Tuple>`, `Class<T>` | 是 | — | 将 `Tuple` 列表映射为目标类型列表。如果列表为空，返回空 `ArrayList`。 |
| `tupleAs(page, targetClass)` | `Page<Tuple>`, `Class<T>` | 是 | — | 将 `Page<Tuple>` 映射为 `Page<T>`，保留分页信息与总数。如果 `page` 为 `null`，返回空 `PageImpl`。 |

## 4. 模板（Templates）

### 场景一：多表关联分页查询与映射

在 Repository 层进行多表关联查询并返回 `Page<Tuple>`，在 Service 层转换为 Vo 分页列表。

#### 1. Repository 层定义

```java
package com.ksptool.bio.biz.xxx.repository;

import com.ksptool.bio.biz.xxx.model.xxx.dto.GetXxxListDto;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ksptool.bio.biz.xxx.model.xxx.XxxPo;

@Repository
public interface XxxRepository extends JpaRepository<XxxPo, Long> {

    @Query("""
            SELECT u.id AS id, o.name AS orgName, u.title AS title, u.createTime AS createTime
            FROM XxxPo u
            LEFT JOIN OrgPo o ON u.orgId = o.id
            WHERE
            (:#{#po.title} IS NULL OR u.title LIKE CONCAT('%', :#{#po.title}, '%'))
            ORDER BY u.createTime DESC
            """)
    Page<Tuple> getXxxList(@Param("po") GetXxxListDto po, Pageable pageable);
}
```

#### 2. Service 层调用

```java
package com.ksptool.bio.biz.xxx.service;

import com.ksptool.bio.biz.xxx.model.xxx.dto.GetXxxListDto;
import com.ksptool.bio.biz.xxx.model.xxx.vo.GetXxxListVo;
import com.ksptool.bio.biz.xxx.repository.XxxRepository;
import com.ksptool.bio.biz.core.common.PageResult;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;

@Service
public class XxxService {

    @Autowired
    private XxxRepository repository;

    public PageResult<GetXxxListVo> getXxxList(GetXxxListDto dto) {
        Page<Tuple> page = repository.getXxxList(dto, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetXxxListVo> vos = tupleAs(page.getContent(), GetXxxListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }
}
```

#### 3. 目标 VO 类定义

```java
package com.ksptool.bio.biz.xxx.model.xxx.vo;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class GetXxxListVo {
    private Long id;
    private String orgName;
    private String title;
    private LocalDateTime createTime;
}
```

## 5. 陷阱（Traps）

```java
// ❌ 错误：JPQL 查询中未使用 AS 别名，或者别名与目标类字段名不一致（大小写或拼写错误）
@Query("SELECT u.id, u.create_time FROM XxxPo u")
Page<Tuple> getXxxList(Pageable pageable);
```

```java
// ✅ 正确：使用 AS 驼峰别名，且别名与目标类字段名完全一致
@Query("SELECT u.id AS id, u.createTime AS createTime FROM XxxPo u")
Page<Tuple> getXxxList(Pageable pageable);
```

```java
// ❌ 错误：目标类缺少无参构造函数（例如只定义了有参构造函数）
public class GetXxxListVo {
    public GetXxxListVo(Long id) { this.id = id; }
}
```

```java
// ✅ 正确：提供无参构造函数（或使用 Lombok 的 @NoArgsConstructor）
@NoArgsConstructor
public class GetXxxListVo {
    private Long id;
}
```

```java
// ❌ 错误：目标类字段被声明为 final 或 static，导致反射无法写入
public class GetXxxListVo {
    private final String name = "";
}
```

```java
// ✅ 正确：字段为非 final、非 static 的普通成员变量
public class GetXxxListVo {
    private String name;
}
```

```java
// ❌ 错误：自定义 @Query 查询方法在 Repository 中使用 find 前缀
@Query("SELECT u.id AS id FROM XxxPo u")
Page<Tuple> findXxxList(Pageable pageable);
```

```java
// ✅ 正确：手写 @Query 查询方法统一使用 get 前缀
@Query("SELECT u.id AS id FROM XxxPo u")
Page<Tuple> getXxxList(Pageable pageable);
```

## 6. 源码索引

- `src/main/java/com/ksptool/bio/biz/core/common/TupleMapper.java` — `TupleMapper` 映射工具核心实现类
- `src/main/java/com/ksptool/bio/biz/dbsec/repository/SnapshotRepository.java` — 使用 `Tuple` 投影查询的 Repository 示例
- `src/main/java/com/ksptool/bio/biz/dbsec/service/SnapshotService.java` — 调用 `TupleMapper.tupleAs` 进行分页映射的 Service 示例
