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

import com.google.common.collect.Lists;
import com.stratio.qa.specs.BaseGSpec;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;

import java.util.Arrays;
import java.util.List;

@Aspect
public class BrowsersDataProviderAspect extends BaseGSpec {

    private final Logger logger = LoggerFactory.getLogger(this.getClass()
            .getCanonicalName());

    @Pointcut("execution (static * BrowsersDataProvider.available*(..)) ")
    protected void availableBrowsersCallPointcut() {
    }

    /**
     * If a System property with FORCE_BROWSER exists then Methods in
     * BrowsersDataProvider will return its value.
     *
     * @param pjp ProceedingJoinPoint
     * @return Object
     * @throws Throwable exception
     */
    @Around(value = "availableBrowsersCallPointcut()")
    public Object availableBrowsersCalls(ProceedingJoinPoint pjp)
            throws Throwable {

        if (pjp.getArgs().length > 0) {
            if (pjp.getArgs()[0] instanceof ITestContext) {
                if (Arrays.asList(((ITestContext) pjp.getArgs()[0]).getIncludedGroups()).contains("mobile")) {
                    return pjp.proceed();
                }
            }
        }

        if (!"".equals(System.getProperty("FORCE_BROWSER", ""))) {
            List<String[]> lData = Lists.newArrayList();
            lData.add(new String[]{System.getProperty("FORCE_BROWSER")});
            logger.debug("Forcing browser to {}",
                    System.getProperty("FORCE_BROWSER"));
            return lData.iterator();
        }
        return pjp.proceed();
    }
}

