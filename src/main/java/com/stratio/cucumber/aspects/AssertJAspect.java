package com.stratio.cucumber.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class AssertJAspect {

	private final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getCanonicalName());

	@Pointcut("execution(* org.assertj.core.internal.Failures.failure(..))")
	protected void logAssertJFailurePointcut() {
	}

	/**
	 * 
	 * @param pjp
	 * @return AssertionError
	 * @throws Throwable
	 */
	@Around("logAssertJFailurePointcut()")
	public AssertionError aroundLogAssertJFailurePointcut(
			ProceedingJoinPoint pjp) throws Throwable {

		AssertionError ae = (AssertionError) pjp.proceed();
        if(ae.getStackTrace()[2].getMethodName().equals("assertCommandExistsOnTimeOut") ||
                ae.getStackTrace()[2].getMethodName().equals("assertSeleniumNElementExistsOnTimeOut") ||
                ae.getStackTrace()[2].getMethodName().equals("sendRequestTimeout")){
            logger.warn("Assertion failed: {}", ae.getMessage());
        } else{
            logger.error("Assertion failed: {}", ae.getMessage());
        }
		return ae;

	}
}