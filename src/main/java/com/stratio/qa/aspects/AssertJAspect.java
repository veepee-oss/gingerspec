/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.qa.aspects;

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
     * @param pjp ProceedingJoinPoint
     * @return AssertionError
     * @throws Throwable exception
     */
    @Around("logAssertJFailurePointcut()")
    public AssertionError aroundLogAssertJFailurePointcut(
            ProceedingJoinPoint pjp) throws Throwable {

        AssertionError ae = (AssertionError) pjp.proceed();
        if (ae.getStackTrace()[2].getMethodName().equals("assertCommandExistsOnTimeOut") ||
                ae.getStackTrace()[2].getMethodName().equals("assertSeleniumNElementExistsOnTimeOut") ||
                ae.getStackTrace()[2].getMethodName().equals("sendRequestTimeout")) {
            logger.warn("Assertion failed: {}", ae.getMessage());
        } else {
            logger.error("Assertion failed: {}", ae.getMessage());
        }
        return ae;

    }
}