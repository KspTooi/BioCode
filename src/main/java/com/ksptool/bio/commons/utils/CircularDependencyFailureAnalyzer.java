package com.ksptool.bio.commons.utils;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Bean 循环依赖错误中文翻译，附带 ASCII 依赖环图
 * <p>
 * 遍历异常链提取所有涉及的 Bean 名称与依赖描述，绘制依赖环并给出中文解决方案
 *
 * @author KspTool
 * @since 1.7.5(E).1
 */
public class CircularDependencyFailureAnalyzer
        extends AbstractFailureAnalyzer<BeanCurrentlyInCreationException> implements Ordered {

    @Override
    protected FailureAnalysis analyze(
            @NonNull Throwable rootFailure, BeanCurrentlyInCreationException cause) {

        List<BeanInCycle> beans = new ArrayList<>();
        int cycleStart = -1;

        Throwable current = rootFailure;
        while (current != null) {
            if (current instanceof BeanCreationException bce
                    && StringUtils.isNotBlank(bce.getBeanName())) {
                BeanInCycle bic = new BeanInCycle(
                        bce.getBeanName(), buildDescription(bce));

                int idx = indexOf(beans, bce.getBeanName());
                if (idx == -1) {
                    beans.add(bic);
                }
                if (cycleStart == -1) {
                    cycleStart = idx;
                }
            }
            current = current.getCause();
        }

        if (beans.isEmpty() || cycleStart == -1) {
            return null;
        }

        String description = """
                【循环依赖检测】
                以下 Bean 之间形成了依赖环，Spring 默认禁止循环引用:

                %s
                """.formatted(buildDiagram(beans, cycleStart));

        String action = """
                👉 解决方案:
                1. 在其中一方的注入点上加 @Lazy 注解打破循环依赖链
                2. 将公共依赖提取到独立的 @Configuration 类中
                3. 重构代码结构，消除双向依赖
                4. 作为临时方案: spring.main.allow-circular-references=true
                """;

        return new FailureAnalysis(description, action, cause);
    }

    /**
     * 生成 Bean 节点的描述文本，优先取注入字段信息
     */
    private String buildDescription(BeanCreationException bce) {
        if (bce instanceof UnsatisfiedDependencyException ude) {
            InjectionPoint ip = ude.getInjectionPoint();
            if (ip != null && ip.getField() != null) {
                return " (field " + ip.getField() + ")";
            }
        }
        if (StringUtils.isNotBlank(bce.getResourceDescription())) {
            return " defined in " + bce.getResourceDescription();
        }
        return "";
    }

    /**
     * 按 bean 名称查找列表中首次出现的位置
     */
    private int indexOf(List<BeanInCycle> list, String name) {
        for (int i = 0; i < list.size(); i++) {
            if (name.equals(list.get(i).name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 绘制 ASCII 依赖环图，格式对齐 Spring Boot 内置样式
     */
    private String buildDiagram(List<BeanInCycle> beans, int cycleStart) {
        StringBuilder sb = new StringBuilder();
        boolean single = beans.size() == 1;

        for (int i = 0; i < beans.size(); i++) {
            BeanInCycle bean = beans.get(i);

            if (i == cycleStart) {
                sb.append(single ? "↑─────↑" : "┌─────┐").append("\n");
            }
            if (i > 0 && i < cycleStart) {
                sb.append("│     ↓\n");
            }
            if (i >= cycleStart && i > cycleStart) {
                sb.append("↑     ↓\n");
            }
            String left = (i < cycleStart) ? " " : "|";
            sb.append(String.format("%s  %s%s\n", left, bean.name, bean.description));
        }

        sb.append(single ? "↓─────↓" : "└─────┘");
        return sb.toString();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static class BeanInCycle {
        final String name;
        final String description;

        BeanInCycle(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}
