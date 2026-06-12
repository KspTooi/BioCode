package com.ksptool.bio.support.failureanalysis;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.core.Ordered;

/**
 * Spring 非唯一 Bean 定义错误中文翻译
 * <p>
 * 当同一类型存在多个 Bean 且未指定 @Primary / @Qualifier 时触发，
 * 翻译英文报告并提供中文解决方案
 *
 * @author KspTool
 * @since 1.7.5(E).1
 */
public class NoUniqueBeanDefinitionFailureAnalyzer
        extends AbstractFailureAnalyzer<NoUniqueBeanDefinitionException> implements Ordered {

    @Override
    protected FailureAnalysis analyze(
            @NonNull Throwable rootFailure, NoUniqueBeanDefinitionException cause) {

        String[] beanNames = extractBeanNames(cause);
        if (beanNames == null) {
            return null;
        }

        String injection = findInjectionTarget(rootFailure);
        String target = StringUtils.isNotBlank(injection) ? injection : "组件";

        Class<?> beanType = cause.getBeanType();
        String typeName = beanType != null ? beanType.getSimpleName() : "未知类型";

        StringBuilder desc = new StringBuilder();
        desc.append("【Bean 类型冲突】\n");
        desc.append("%s 需要类型 %s 的单个 Bean，但找到了 %d 个:\n\n"
                .formatted(target, typeName, beanNames.length));
        for (String name : beanNames) {
            desc.append("\t- %s\n".formatted(name));
        }

        StringBuilder action = new StringBuilder();
        action.append("👉 解决方案:\n");
        action.append("1. 将其中一个 Bean 标记为 @Primary，使其成为首选注入\n");
        if (StringUtils.isNotBlank(typeName) && !"未知类型".equals(typeName)) {
            action.append("2. 修改使用者，改为接受多个 Bean（如 List<%s>）\n".formatted(typeName));
        }
        action.append("3. 使用 @Qualifier 精确指定需要注入的 Bean 名称\n\n");
        action.append("💡 如果注入点位于方法参数且参数名与 Bean 名不匹配，\n");
        action.append("   请在编译设置中启用 -parameters 标志");

        return new FailureAnalysis(desc.toString(), action.toString(), cause);
    }

    /**
     * 从异常链中查找注入点的目标类型名称
     */
    private String findInjectionTarget(Throwable rootFailure) {
        Throwable current = rootFailure;
        while (current != null) {
            if (current instanceof UnsatisfiedDependencyException ude) {
                var ip = ude.getInjectionPoint();
                if (ip != null && ip.getMember() != null) {
                    return ip.getMember().getDeclaringClass().getSimpleName();
                }
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * 从异常消息中提取重复的 Bean 名称列表
     */
    private String[] extractBeanNames(NoUniqueBeanDefinitionException cause) {
        String msg = cause.getMessage();
        if (msg == null || !msg.contains("but found")) {
            return null;
        }
        String names = msg.substring(msg.lastIndexOf(':') + 1).trim();
        return names.split("\\s*,\\s*");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
