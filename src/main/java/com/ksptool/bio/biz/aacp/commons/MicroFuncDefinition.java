package com.ksptool.bio.biz.aacp.commons;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * 微函数元信息：@MicroFunc 注解元数据 + 反射调用句柄（对应 QT 的 QMetaMethod）
 */
@Getter
@Schema(description = "微函数元信息")
public class MicroFuncDefinition {

    @Schema(description = "微函数唯一标识")
    private final String code;

    @Schema(description = "微函数名称")
    private final String name;

    @Schema(description = "微函数描述")
    private final String description;

    @Schema(description = "所属 Spring Bean 实例")
    private final Object bean;

    @Schema(description = "目标方法")
    private final Method method;

    @Schema(description = "方法参数类型数组")
    private final Class<?>[] parameterTypes;

    public MicroFuncDefinition(String code, String name, String description, Object bean, Method method) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.bean = bean;
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
    }

    /**
     * 反射调用目标方法
     *
     * @param args 已由 MicroFuncService 完成 DTO 反序列化注入的参数数组
     * @return 方法返回值
     */
    public Object invoke(Object... args) throws Exception {
        return method.invoke(bean, args);
    }
}
