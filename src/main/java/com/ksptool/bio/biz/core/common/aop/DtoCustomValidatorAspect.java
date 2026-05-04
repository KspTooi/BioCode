package com.ksptool.bio.biz.core.common.aop;

import com.ksptool.assembly.entity.exception.BizException;
import jakarta.validation.Valid;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * DTO自定义验证切面
 * 用于在DTO验证时调用自定义验证方法
 * <p>
 * 这个切面需要配合 @Valid 注解和 DtoCustomValidator 接口一起使用
 * <p>
 * 使用方法：
 * 1.在控制器入参中使用 @Valid 注解
 * 2.在DTO类中实现 DtoCustomValidator 接口
 * 3.编写自定义验证方法，实现 DtoCustomValidator 接口中的 validate 方法
 *
 * @author KspTooi
 * @since 1.6.14(N).68
 */
@Aspect
@Component
public class DtoCustomValidatorAspect {

    /**
     * 拦截所有 Controller 层方法，对标注了 @Valid 且实现了 DtoCustomValidator 的参数执行自定义验证
     */
    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void validateDtoArgs(JoinPoint joinPoint) throws BizException {

        //获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        //获取参数注解
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < args.length; i++) {
            if (!hasValidAnnotation(paramAnnotations[i])) {
                continue;
            }

            //判断参数是否实现了 DtoCustomValidator 接口
            if (!(args[i] instanceof DtoCustomValidator validator)) {
                continue;
            }

            //调用自定义验证方法
            String error = validator.validate();

            if (error == null) {
                continue;
            }

            throw new BizException(error);
        }
    }

    /**
     * 判断参数注解数组中是否含有 @Valid
     */
    private boolean hasValidAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Valid) {
                return true;
            }
        }
        return false;
    }

}
