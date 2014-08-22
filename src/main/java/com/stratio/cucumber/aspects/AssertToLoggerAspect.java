package com.stratio.cucumber.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class AssertToLoggerAspect {

	final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getCanonicalName());

	@Pointcut("call(static public void org.hamcrest.MatcherAssert.assertThat(String, Object, org.hamcrest.Matcher))"
			+ " && args(reason, actual, matcher)")
	protected void logAssertFailurePointcut(String reason, Object actual,
			Matcher<?> matcher) {
	}

	@Around(value = "logAssertFailurePointcut(reason, actual, matcher)")
	public void aroundLogAssertFailurePointcut(ProceedingJoinPoint pjp,
			String reason, Object actual, Matcher<?> matcher) throws Throwable {
		try {
			pjp.proceed();
		} catch (AssertionError e) {
			logger.error("Assertion failed: {}", reason);
			throw e;
		}
	}
}