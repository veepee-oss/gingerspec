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

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Allows conditional scenario execution using @skipOnEnv and @runOnEnv tags
 * <pre>
 * {@code
 *      \@runOnEnv(param1,param2,param3,..): The scenario will only be executed if ALL the params are defined.
 *      \@skipOnEnv(param1,param2,param3,..) The scenario will be omitted if ANY of params are defined.
 * }</pre>
 *
 * @author Jose Fernandez
 */
@Aspect
public class RunOnTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    /**
     * Pointcut is executed for {@link io.cucumber.testng.AbstractTestNGCucumberTests#runScenario(PickleWrapper, FeatureWrapper)}
     * @param pickleWrapper     the pickleWrapper
     * @param featureWrapper    the featureWrapper
     */
    @Pointcut("execution (void *.runScenario(..)) && args(pickleWrapper, featureWrapper)")
    protected void addIgnoreTagPointcutScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
    }


    @Around(value = "addIgnoreTagPointcutScenario(pickleWrapper, featureWrapper)")
    public void aroundAddIgnoreTagPointcut(ProceedingJoinPoint pjp, PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) throws Throwable {

        List<String> tags = pickleWrapper.getPickle().getTags();
        String scenarioName = pickleWrapper.getPickle().getName();
        Boolean exit = tagsIteration(tags);

        if (exit) {
            logger.warn("Scenario '" + scenarioName + "' ignored by execution tag.");
            return;
        }

        pjp.proceed();
    }

    public boolean tagsIteration(List<String> tags) throws Exception {
        for (String tag : tags) {
            if (tag.contains("@runOnEnv")) {
                if (!checkParams(getParams(tag))) {
                    return true;
                }
            } else if (tag.contains("@skipOnEnv")) {
                if (checkParams(getParams(tag))) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Returns a string array of params
     */
    public String[] getParams(String s) throws Exception {
        String[] val = s.substring((s.lastIndexOf("(") + 1), (s.length()) - 1).split(",");
        if (val[0].startsWith("@")) {
            throw new Exception("Error while parsing params. Format is: \"runOnEnv(PARAM)\", but found: " + s);
        }
        return val;
    }

    /*
     * Checks if every param in the array of strings is defined
     */
    public boolean checkParams(String[] params) throws Exception {
        if ("".equals(params[0])) {
            throw new Exception("Error while parsing params. Params must be at least one");
        }
        for (int i = 0; i < params.length; i++) {
            if (System.getProperty(params[i], "").isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
