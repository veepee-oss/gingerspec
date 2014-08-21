package com.stratio.cucumber.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class IgnoreTagAspect {

	private final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getCanonicalName());

	@Pointcut("execution(* cucumber.api.CucumberOptions+.tags())")
	protected void addIgnoreTagPointcut() {
	}

	@Around(value = "addIgnoreTagPointcut()")
	public String[] aroundAddIgnoreTagPointcut(ProceedingJoinPoint pjp) throws Throwable {
		logger.debug("Executing pointcut around CucumberOptions tag array");
		String[] response = new String[1];
		response[0] = "~@ignore";
		return response;
	}
}