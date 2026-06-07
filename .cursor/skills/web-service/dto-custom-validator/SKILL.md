---
name: dto-custom-validator
description: DTO自定义校验/DtoCustomValidator/validate/DTO多字段联合校验/自定义验证
---

# DTO 自定义校验指南

> 本 Skill 指导在自定义 DTO 校验时，如何实现 `DtoCustomValidator` 接口进行多字段联合校验或复杂规则校验。

## 1. 选型

| 业务诉求 | 方案 | 说明 |
| --- | --- | --- |
| 单字段基础校验（如非空、长度、范围、正则） | Jakarta Validation 注解（`@NotBlank`, `@NotNull`, `@Range` 等） | 直接在 Dto 字段上声明，简单高效 |
| 多字段联合校验、多字段间依赖关系校验、复杂业务规则校验 | 实现 `DtoCustomValidator` 接口并重写 `validate()` 方法 | 自定义校验逻辑，返回错误信息字符串（通过返回 `null`） |

## 2. 快速接入

1. DTO 类实现 `com.ksptool.bio.biz.core.common.aop.DtoCustomValidator` 接口
2. 重写 `validate()` 方法，编写自定义校验逻辑
3. 使用 `if-return` 早退守卫，校验失败时直接返回错误提示字符串
4. 校验全部通过时，方法末尾返回 `null`
5. Controller 层入参配合 `@Valid` 注解（如 `@RequestBody @Valid AddXxxDto dto`）

## 3. 参数契约

对 `DtoCustomValidator` 接口的 `validate()` 方法契约：

| 方法 | 返回类型 | 必填/实现 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `validate()` | `String` | 是 | — | 自定义校验逻辑。验证通过返回 `null`，验证不通过返回具体错误信息字符串。 |

## 4. 模板（Templates）

### 场景一：多字段联合校验/依赖关系校验

当字段 `kind` 满足特定值时，对其他字段进行必填或禁止填写的校验。

```java
package com.ksptool.bio.biz.xxx.model.xxx.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class AddXxxDto implements DtoCustomValidator {

    @NotNull(message = "类型不能为空")
    @Schema(description = "类型 0:目录 1:菜单")
    private Integer kind;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "图标")
    private String icon;

    /**
     * 自定义DTO验证方法
     *
     * @return 验证结果 如果验证通过则返回null 否则返回错误信息
     */
    @Override
    public String validate() {
        if (kind == 0) {
            if (StringUtils.isNotBlank(path)) {
                return "目录不支持填写路径";
            }
            if (StringUtils.isBlank(icon)) {
                return "目录图标不能为空";
            }
        }

        if (kind == 1) {
            if (StringUtils.isBlank(path)) {
                return "菜单路径不能为空";
            }
            if (StringUtils.isBlank(icon)) {
                return "菜单图标不能为空";
            }
        }

        return null;
    }
}
```

### 场景二：集合大小与内容校验

校验集合字段的长度上限，并对集合内的元素进行黑白名单或格式校验。

```java
package com.ksptool.bio.biz.xxx.model.xxx.dto;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class AddYyyDto implements DtoCustomValidator {

    @Schema(description = "权限码列表")
    private Set<String> permissionCodes;

    /**
     * 自定义DTO验证方法
     *
     * @return 验证结果 如果验证通过则返回null 否则返回错误信息
     */
    @Override
    public String validate() {
        if (permissionCodes == null) {
            return null;
        }

        if (permissionCodes.size() > 10) {
            return "一个菜单最多只能增加10个权限";
        }

        for (String code : permissionCodes) {
            if ("ADMIN".equalsIgnoreCase(code)) {
                return "权限码[ADMIN]不允许在此处使用！";
            }
        }

        return null;
    }
}
```

## 5. 陷阱（Traps）

```java
// ❌ 错误：使用 switch 或 else 分支
@Override
public String validate() {
    switch (kind) {
        case 0:
            if (path != null) return "目录不支持路径";
            else return null;
        default:
            return null;
    }
}
```

```java
// ✅ 正确：短路优先，平铺 if-return，不使用 switch/else
@Override
public String validate() {
    if (kind == 0) {
        if (StringUtils.isNotBlank(path)) {
            return "目录不支持路径";
        }
    }
    return null;
}
```

```java
// ❌ 错误：验证通过时返回空字符串 "" 或其他非 null 值
@Override
public String validate() {
    if (kind == 0 && StringUtils.isBlank(icon)) {
        return "图标不能为空";
    }
    return "";
}
```

```java
// ✅ 正确：验证通过时必须返回 null
@Override
public String validate() {
    if (kind == 0 && StringUtils.isBlank(icon)) {
        return "图标不能为空";
    }
    return null;
}
```

## 6. 源码索引

- `src/main/java/com/ksptool/bio/biz/core/common/aop/DtoCustomValidator.java` — 自定义DTO验证接口定义
- `src/main/java/com/ksptool/bio/biz/core/model/menu/dto/AddMenuDto.java` — 菜单新增DTO，实现了 `DtoCustomValidator` 的标准示例
