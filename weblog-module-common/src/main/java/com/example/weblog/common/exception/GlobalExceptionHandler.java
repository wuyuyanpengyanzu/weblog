package com.example.weblog.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.example.weblog.common.enums.ResponseCodeEnum;
import com.example.weblog.common.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public Response<?> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage());
        return Response.fail(e.getErrorCode(), e.getErrorMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Response<?> handleValidationException(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(error ->
                sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; "));
        return Response.fail(ResponseCodeEnum.PARAM_ERROR.getErrorCode(), sb.toString());
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseBody
    public Response<?> handleNotLoginException(NotLoginException e) {
        return Response.fail(ResponseCodeEnum.UNAUTHORIZED.getErrorCode(),
                ResponseCodeEnum.UNAUTHORIZED.getErrorMessage());
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseBody
    public Response<?> handleNotRoleException(NotRoleException e) {
        return Response.fail(ResponseCodeEnum.FORBIDDEN.getErrorCode(),
                ResponseCodeEnum.FORBIDDEN.getErrorMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Response<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Response.fail(ResponseCodeEnum.SYSTEM_ERROR.getErrorCode(),
                ResponseCodeEnum.SYSTEM_ERROR.getErrorMessage());
    }
}
