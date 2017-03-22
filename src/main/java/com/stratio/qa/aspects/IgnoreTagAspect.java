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

import com.stratio.qa.utils.ThreadProperty;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Aspect
public class IgnoreTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Pointcut("execution (* cucumber.runtime.model.CucumberScenario.run(..)) && "
            + "args (formatter, reporter, runtime)")
    protected void addIgnoreTagPointcutScenario(Formatter formatter, Reporter reporter, Runtime runtime) {
    }

    /**
     * @param pjp
     * @param formatter
     * @param reporter
     * @param runtime
     * @throws Throwable
     */
    @Around(value = "addIgnoreTagPointcutScenario(formatter, reporter, runtime)")
    public void aroundAddIgnoreTagPointcut(ProceedingJoinPoint pjp, Formatter formatter, Reporter reporter,
                                           Runtime runtime) throws Throwable {

        CucumberScenario scen = (CucumberScenario) pjp.getThis();
        Scenario scenario = (Scenario) scen.getGherkinModel();

        Class<?> sc = scen.getClass();
        Method tt = sc.getSuperclass().getDeclaredMethod("tagsAndInheritedTags");
        tt.setAccessible(true);
        Set<Tag> tags = (Set<Tag>) tt.invoke(scen);

        Boolean ignore = false;
        Boolean ignoreReason = false;

        List<String> tagList = new ArrayList<>();
        tagList = tags.stream().map(Tag::getName).collect(Collectors.toList());


        for (String tag: tagList) {
            if (tag.contains("@ignore")) {
                ignore = true;
                if (tag.contains("runOnEnvs")) {
                    ignoreReason = true;
                }
                Pattern pattern = Pattern.compile("@tillfixed\\((.*?)\\)");
                Matcher matcher = pattern.matcher(tag);
                if (matcher.find()) {
                    String ticket = matcher.group(1);
                    logger.warn("Scenario '" + scenario.getName() + "' ignored because of ticket: " + ticket);
                    ignoreReason = true;
                    break;
                }
            }
            if (tag.contains("@unimplemented")) {
                logger.warn("Scenario '" + scenario.getName() + "' ignored because it is not yet implemented.");
                ignoreReason = true;
            }
            if (tag.contains("@manual")) {
                logger.warn("Scenario '" + scenario.getName() + "' ignored because it is marked as manual test.");
                ignoreReason = true;
            }
            if (tag.contains("@toocomplex")) {
                logger.warn("Scenario '" + scenario.getName() + "' ignored because the test is too complex.");
                ignoreReason = true;
            }
        }

        if (ignore && !ignoreReason) {
            logger.error("Scenario '" + scenario.getName() + "' failed due to wrong use of the @ignore tag. ");
        }

        if (ignore) {
            runtime.buildBackendWorlds(reporter, tags, scenario.getName());
            formatter.startOfScenarioLifeCycle(scenario);
            formatter.endOfScenarioLifeCycle(scenario);
            runtime.disposeBackendWorlds();
        } else {
            pjp.proceed();
        }
    }
}