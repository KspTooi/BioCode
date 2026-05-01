package com.ksptool.bio.commons.aop;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.audit.service.AuditErrorRcdService;
import com.ksptool.bio.biz.auth.common.exception.RootBindingException;
import com.ksptool.bio.commons.web.ResultCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

import static com.ksptool.bio.biz.auth.service.SessionService.sessionWithNullable;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 是否记录系统错误记录
     */
    @Value("${audit.record-error-rcd:true}")
    private boolean recordErrorRcd;

    @Autowired
    private AuditErrorRcdService auditErrorRcdService;

    /**
     * 处理 @Valid 注解校验失败抛出的异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        String errorMessage = String.join(", ", errors);
        // log.error("参数校验失败: {}", errorMessage);
        printEnhancedRequestLog(ex);

        if (recordErrorRcd) {
            // 审计模块记录系统错误记录（后台异步记录，不影响前端提示）
            var session = sessionWithNullable();
            var userId = 0L;
            var userName = "无法获取";
            var errorCode = auditErrorRcdService.nextErrorCode("PARAM");

            if (session != null) {
                userId = session.getUserId();
            }

            auditErrorRcdService.addAuditErrorRcdAsync(errorCode, request.getRequestURI(), userId, userName, ex);
        }

        return Result.error(errorMessage);
    }

    /**
     * 处理 @Valid 表单绑定校验失败抛出的异常（非JSON请求体）
     */
    @ExceptionHandler(BindException.class)
    public Result<Object> handleBindException(BindException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        String errorMessage = String.join(", ", errors);
        printEnhancedRequestLog(ex);

        if (recordErrorRcd) {
            var session = sessionWithNullable();
            var userId = 0L;
            var userName = "无法获取";
            var errorCode = auditErrorRcdService.nextErrorCode("PARAM");
            if (session != null) {
                userId = session.getUserId();
            }
            auditErrorRcdService.addAuditErrorRcdAsync(errorCode, request.getRequestURI(), userId, userName, ex);
        }

        return Result.error(errorMessage);
    }

    /**
     * 处理 @Validated 方法参数校验失败抛出的异常（如 @RequestParam 上的约束）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        String errorMessage = String.join(", ", errors);
        printEnhancedRequestLog(ex);

        if (recordErrorRcd) {
            var session = sessionWithNullable();
            var userId = 0L;
            var userName = "无法获取";
            var errorCode = auditErrorRcdService.nextErrorCode("PARAM");
            if (session != null) {
                userId = session.getUserId();
            }
            auditErrorRcdService.addAuditErrorRcdAsync(errorCode, request.getRequestURI(), userId, userName, ex);
        }

        return Result.error(errorMessage);
    }

    /**
     * 处理缺少必要请求参数的异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String errorMessage = "请求参数不完整，请检查后重试";
        log.warn("缺少必要的请求参数: {}", ex.getParameterName());
        printEnhancedRequestLog(ex);

        if (recordErrorRcd) {
            var session = sessionWithNullable();
            var userId = 0L;
            var userName = "无法获取";
            var errorCode = auditErrorRcdService.nextErrorCode("PARAM");
            if (session != null) {
                userId = session.getUserId();
            }
            auditErrorRcdService.addAuditErrorRcdAsync(errorCode, request.getRequestURI(), userId, userName, ex);
        }

        return Result.error(errorMessage);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    public Result<Object> handleBizException(BizException ex) {
        log.error(ex.getMessage(), ex);
        return Result.error(ex);
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);
        printEnhancedRequestLog(ex);

        if (recordErrorRcd) {
            // 审计模块记录系统错误记录
            var session = sessionWithNullable();
            var userId = 0L;
            var userName = "无法获取";
            var errorCode = auditErrorRcdService.nextErrorCode("INTERNAL");

            if (session != null) {
                userId = session.getUserId();
            }

            auditErrorRcdService.addAuditErrorRcdAsync(errorCode, request.getRequestURI(), userId, userName, ex);
            return Result.error("在处理您的请求时发生了一个内部错误，该错误已被审计系统记录，错误代码: " + errorCode);
        }

        return Result.internalError("系统内部错误", ex.getMessage());
    }

    /**
     * 处理Spring Security的权限不足异常
     * com.ksptool.bio.auth.common.JsonAuthEntryPoint已经能够处理Filter层的异常,但当异常发生在Controller或方法级时,需要在这里处理。
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public Result<Object> handleAuthorizationDeniedException(AuthorizationDeniedException ex,HttpServletResponse hsrp) {
        hsrp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return Result.error(403, "权限不足");
    }


    /**
     * 打印增强型请求日志
     *
     * @param ex 异常
     */
    public void printEnhancedRequestLog(Exception ex) {
        var requestInfo = RequestLogFilter.getRequestInfo();
        if (requestInfo != null) {
            var appendMsg = "";
            if (StringUtils.isNotBlank(requestInfo.getGatewayRequestId())) {
                appendMsg = " 网关请求ID:" + requestInfo.getGatewayRequestId() + " ";
            }
            log.error("请求处理失败=>{}[{}]{}载荷:{} 原因:{}", requestInfo.getUri(), requestInfo.getMethod(), appendMsg,
                    requestInfo.getBodySupplier().get(), ex.getMessage(), ex);
        }
    }

    /**
     * 处理用户未绑定租户异常
     */
    @ExceptionHandler(RootBindingException.class)
    public Result<Object> handleRootBindingException(RootBindingException ex) {
        return Result.error(ResultCode.REQUIRE_ROOT.getCode(), ResultCode.REQUIRE_ROOT.getMessage());
    }


}
