package com.stratio.cucumber.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class AssertJAspect {

    final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Pointcut("execution(* org.assertj.core.internal.Failures.failure(..))")
    protected void logAssertJFailurePointcut() {
    }

    @Around("logAssertJFailurePointcut()")
    public AssertionError aroundLogAssertJFailurePointcut(ProceedingJoinPoint pjp) throws Throwable {

        AssertionError ae = (AssertionError) pjp.proceed();
        logger.error("Assertion failed: {}", ae.getMessage());
        return ae;

    }
}