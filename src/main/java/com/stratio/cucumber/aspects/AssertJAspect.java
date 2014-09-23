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

    @Pointcut("execution(void org.assertj.core.api.Assert+.*(..))")
    protected void logAssertJFailurePointcut() { // Object actual) {
    }

    @Around("logAssertJFailurePointcut()")
    public void aroundLogAssertJFailurePointcut(ProceedingJoinPoint pjp) throws Throwable {
        try {
            pjp.proceed();
        } catch (AssertionError e) {
            logger.error("Assertion failed: {}", e.getMessage());
            throw e;
        }
    }
}