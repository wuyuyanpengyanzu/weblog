package com.example.weblog.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApiOperationLogAspect {

    @Pointcut("@annotation(com.example.weblog.common.aspect.ApiOperationLog)")
    public void apiOperationLog() {}

    @Before("apiOperationLog()")
    public void before() {
        log.info("API 请求");
    }

    @Around("apiOperationLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - start;

        log.info("API 响应, 耗时: {}ms", elapsed);

        return result;
    }
}