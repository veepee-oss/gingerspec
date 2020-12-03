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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Map;

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

    ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("execution (public static * com.privalia.qa.data.BrowsersDataProvider.available*(..))  &&"
            + "args (context, testConstructor)")
    protected void availableBrowsersCallPointcut(ITestContext context, Constructor<?> testConstructor) {
    }

    /**
     * If a System property with FORCE_BROWSER exists then Methods in
     * BrowsersDataProvider will return its value.
     *
     * The expected format of the FORCE_BROWSER variable is:
     *  browserName_version
     *
     * browserName: browserName capability of the connected node
     * version: version capability of the connected node
     *
     * Example: firefox_60.0
     *
     * @param pjp             ProceedingJoinPoint
     * @param context         the context
     * @param testConstructor the test constructor
     * @return Object object
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

            Map<String, String> nodeDetailsMap = new HashMap<String, String>();
            nodeDetailsMap.put("browserName", System.getProperty("FORCE_BROWSER").split("_")[0]);
            nodeDetailsMap.put("version", System.getProperty("FORCE_BROWSER").split("_")[1]);
            return new Object[][] {{objectMapper.writeValueAsString(nodeDetailsMap)}};
        }
        return pjp.proceed();
    }
}

