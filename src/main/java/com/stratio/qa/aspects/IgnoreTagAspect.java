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
import java.util.Set;

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
        logger.debug("Executing pointcut CucumberScenario run method");

        CucumberScenario scen = (CucumberScenario) pjp.getThis();
        Scenario scenario = (Scenario) scen.getGherkinModel();

        Class<?> sc = scen.getClass();
        Method tt = sc.getSuperclass().getDeclaredMethod("tagsAndInheritedTags");
        tt.setAccessible(true);
        Set<Tag> tags = (Set<Tag>) tt.invoke(scen);

        Boolean ignore = false;
        Boolean ignoreReason = false;

        for (Tag tag : tags) {
            if (tag.getName().contains("@ignore")) {
                ignore = true;
                for (Tag tagNs : tags) {
                    //@runOnEnv
                    //@skipOnEnv
                    if ((tagNs.getName()).contains("runOnEnvs")) {
                        ignoreReason = true;
                        break;
                    }
                    //@tillFixed
                    if ((tagNs.getName()).matches("@tillfixed\\(\\w+-\\d+\\)")) {
                        String issueNumb = tagNs.getName().substring(tagNs.getName().lastIndexOf('(') + 1);
                        logger.warn("Scenario '" + scenario.getName() + "' ignored because of Issue: " + issueNumb.subSequence(0, issueNumb.length() - 1) + ".");
                        ignoreReason = true;
                        break;
                    }
                    //@unimplemented
                    if (tagNs.getName().matches("@unimplemented")) {
                        logger.warn("Scenario '" + scenario.getName() + "' ignored because it is not yet implemented.");
                        ignoreReason = true;
                        break;
                    }
                    //@manual
                    if (tagNs.getName().matches("@manual")) {
                        logger.warn("Scenario '" + scenario.getName() + "' ignored because it is marked as manual test.");
                        ignoreReason = true;
                        break;
                    }
                    //@toocomplex
                    if (tagNs.getName().matches("@toocomplex")) {
                        logger.warn("Scenario '" + scenario.getName() + "' ignored because the test is too complex.");
                        ignoreReason = true;
                        break;
                    }
                }
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