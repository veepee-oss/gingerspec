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

import cucumber.api.TestCase;
import cucumber.runner.EventBus;
import gherkin.pickles.PickleTag;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.privalia.qa.aspects.IgnoreTagAspect.ignoreReasons.NOREASON;
import static com.privalia.qa.aspects.IgnoreTagAspect.ignoreReasons.NOTIGNORED;

/**
 * Aspect for managing the @ignore annotation on a feature/scenario
 */
@Aspect
public class IgnoreTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Pointcut("execution (void cucumber.runner.TestCase.run(..)) && args(bus)")
    protected void addIgnoreTagPointcutScenario(EventBus bus) {
    }


    @Around(value = "addIgnoreTagPointcutScenario(bus)")
    public void aroundAddIgnoreTagPointcut(ProceedingJoinPoint pjp, EventBus bus) throws Throwable {

        TestCase testCase = (TestCase) pjp.getThis();
        List<PickleTag> tags = testCase.getTags();

        String scenarioName = testCase.getName();

        List<String> tagList = new ArrayList<>();
        tagList = tags.stream().map(PickleTag::getName).collect(Collectors.toList());


        ignoreReasons exitReason = manageTags(tagList, scenarioName);
        if (exitReason.equals(NOREASON)) {
            logger.error("Scenario '" + scenarioName + "' failed due to wrong use of the @ignore tag. ");
        }

        if ((!(exitReason.equals(NOTIGNORED))) && (!(exitReason.equals(NOREASON)))) {
            /*
            runtime.buildBackendWorlds(reporter, tags, scenario);
            formatter.startOfScenarioLifeCycle(scenario);
            formatter.endOfScenarioLifeCycle(scenario);
            runtime.disposeBackendWorlds();
            */
        } else {
            pjp.proceed();
        }

    }

    public ignoreReasons manageTags(List<String> tagList, String scenarioName) {
        ignoreReasons exit = NOTIGNORED;
        if (tagList.contains("@ignore")) {
            exit = NOREASON;
            for (String tag: tagList) {
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
        }
        return exit;
    }

    public enum ignoreReasons { NOTIGNORED, ENVCONDITION, UNIMPLEMENTED, MANUAL, TOOCOMPLEX, JIRATICKET, NOREASON }
}