package com.ksptool.bio.commons.utils;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Bean 循环依赖错误中文翻译
 * <p>
 * 必须实现 Ordered 并取最高优先级，否则内置的 BeanCurrentlyInCreationFailureAnalyzer 会先产出英文分析结果
 *
 * @author KspTool
 * @since 1.7.5(E).1
 */
public class CircularDependencyFailureAnalyzer
        extends AbstractFailureAnalyzer<BeanCurrentlyInCreationException> implements Ordered {

    @Override
    protected FailureAnalysis analyze(
            @NonNull Throwable rootFailure, BeanCurrentlyInCreationException cause) {

        //沿异常链收集循环路径上的 bean 名称
        List<String> beans = new ArrayList<>();
        Throwable current = rootFailure;
        while (current != null) {
            if (current instanceof BeanCreationException bce
                    && StringUtils.isNotBlank(bce.getBeanName())
                    && !beans.contains(bce.getBeanName())) {
                beans.add(bce.getBeanName());
            }
            current = current.getCause();
        }

        String chain = String.join(" → ", beans);
        if (StringUtils.isNotBlank(chain) && !beans.isEmpty()) {
            chain = chain + " → " + beans.get(0);
        }

        String description = """
                【循环依赖检测】
                以下 Bean 之间形成了依赖环，Spring 默认禁止循环引用:
                %s
                原始报错: %s
                """.formatted(chain, cause.getMessage());

        String action = """
                👉 解决方案:
                1. 在其中一方的注入点上加 @Lazy 注解打破循环依赖链
                2. 将公共依赖提取到独立的 @Configuration 类中
                3. 重构代码结构，消除双向依赖
                4. 作为临时方案: spring.main.allow-circular-references=true
                """;

        return new FailureAnalysis(description, action, cause);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}