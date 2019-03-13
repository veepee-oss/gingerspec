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

package com.privalia.qa.aspects;

import com.privalia.qa.specs.BaseGSpec;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * This Aspect verifies if the maven variable FORCE_BROWSER exists when executing selenium related tests
 * and forces the BrowsersDataProvider class to return only that browser
 *
 * @author Jose Fernandez
 */
@Aspect
public class BrowsersDataProviderAspect extends BaseGSpec {

    private final Logger logger = LoggerFactory.getLogger(this.getClass()
            .getCanonicalName());

    @Pointcut("execution (public static * com.privalia.qa.data.BrowsersDataProvider.available*(..))  &&"
            + "args (context, testConstructor)")
    protected void availableBrowsersCallPointcut(ITestContext context, Constructor<?> testConstructor) {
    }

    /**
     * If a System property with FORCE_BROWSER exists then Methods in
     * BrowsersDataProvider will return its value.
     *
     * @param pjp ProceedingJoinPoint
     * @return Object
     * @throws Throwable exception
     */
    @Around(value = "availableBrowsersCallPointcut(context, testConstructor)")
    public Object availableBrowsersCalls(ProceedingJoinPoint pjp, ITestContext context, Constructor<?> testConstructor)
            throws Throwable {

        if (pjp.getArgs().length > 0) {
            if (pjp.getArgs()[0] instanceof ITestContext) {
                if (Arrays.asList(((ITestContext) pjp.getArgs()[0]).getIncludedGroups()).contains("mobile")) {
                    return pjp.proceed();
                }
            }
        }

        if (!"".equals(System.getProperty("FORCE_BROWSER", ""))) {
            logger.info("FORCE_BROWSER variable detected. Using browser: '{}'", System.getProperty("FORCE_BROWSER"));
            return new Object[][] {{System.getProperty("FORCE_BROWSER")}};
        }
        return pjp.proceed();
    }
}

