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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aspect for managing the @ignore tag on a feature/scenario
 */
@Aspect
public class IgnoreTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    public enum ignoreReasons { ENVCONDITION, UNIMPLEMENTED, MANUAL, TOOCOMPLEX, JIRATICKET, NOREASON }

    /**
     * Pointcut is executed for {@link io.cucumber.testng.AbstractTestNGCucumberTests#runScenario(PickleWrapper, FeatureWrapper)}
     * @param pickleWrapper         the pickleWrapper
     * @param featureWrapper        the featureWrapper
     */
    @Pointcut("execution (void *.runScenario(..)) && args(pickleWrapper, featureWrapper)")
    protected void addIgnoreTagPointcutScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
    }


    @Around(value = "addIgnoreTagPointcutScenario(pickleWrapper, featureWrapper)")
    public void aroundAddIgnoreTagPointcut(ProceedingJoinPoint pjp, PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) throws Throwable {

        List<String> tags = pickleWrapper.getPickle().getTags();
        String scenarioName = pickleWrapper.getPickle().getName();

        if (tags.contains("@ignore")) {
            ignoreReasons exitReason = manageTags(tags, scenarioName);

            if (exitReason.equals(ignoreReasons.NOREASON)) {
                logger.warn("Scenario '" + scenarioName + "' ignored, no reason specified.");
            }

            return;

        } else {
            pjp.proceed();
        }
    }

    public ignoreReasons manageTags(List<String> tagList, String scenarioName) {

        ignoreReasons exit = ignoreReasons.NOREASON;

        for (String tag : tagList) {
            Pattern pattern = Pattern.compile("@tillfixed\\((.*?)\\)");
            Matcher matcher = pattern.matcher(tag);
            if (matcher.find()) {
                String ticket = matcher.group(1);
                logger.warn("Scenario '" + scenarioName + "' ignored because of ticket: " + ticket);
                exit = ignoreReasons.JIRATICKET;
            }
        }
        if (tagList.contains("@envCondition")) {
            exit = ignoreReasons.ENVCONDITION;
        }
        if (tagList.contains("@unimplemented")) {
            logger.warn("Scenario '" + scenarioName + "' ignored because it is not yet implemented.");
            exit = ignoreReasons.UNIMPLEMENTED;
        }
        if (tagList.contains("@manual")) {
            logger.warn("Scenario '" + scenarioName + "' ignored because it is marked as manual test.");
            exit = ignoreReasons.MANUAL;
        }
        if (tagList.contains("@toocomplex")) {
            logger.warn("Scenario '" + scenarioName + "' ignored because the test is too complex.");
            exit = ignoreReasons.TOOCOMPLEX;
        }

        return exit;
    }


}