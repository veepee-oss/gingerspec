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

import com.privalia.qa.utils.ThreadProperty;

import gherkin.ast.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Allows conditional scenario execution using @skipOnEnv and @runOnEnv tags
 *
 * @author Jose Fernandez
 */
@Aspect
public class RunOnTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());


    @Pointcut("execution (gherkin.ast.Scenario.new(..)) && args(tags, location, keyword, name, description, steps)")
    protected void replacementScenarios(List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps) {
    }

    @Pointcut("execution (gherkin.ast.ScenarioOutline.new(..)) && args(tags, location, keyword, name, description, steps, examples)")
    protected void replacementScenariosOutline(List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps, List<Examples> examples) {
    }

    @Around("replacementScenariosOutline(tags, location, keyword, name, description, steps, examples)")
    public void aroundScenariosOutlineDefinition(ProceedingJoinPoint pjp, List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps, List<Examples> examples) throws Throwable {
        this.aroundScenarios(pjp, tags, location, keyword, name, description, steps, examples);
    }

    @Around("replacementScenarios(tags, location, keyword, name, description, steps)")
    public void aroundScenariosDefinition(ProceedingJoinPoint pjp, List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps) throws Throwable {
        this.aroundScenarios(pjp, tags, location, keyword, name, description, steps, null);
    }

    /**
     * Allows conditional scenario execution.
     * If the scenario contains the following tag:
     * <dl>
     *    <dt>\@runOnEnv(param)</dt>
     *<dd>The scenario will only be executed if the param is defined when test is launched. Configuration map object.
     * More than one param can be passed in the tag. To do so, the params must be comma separated:
     * \@runOnEnv(param): The scenario will only be executed if the param is defined when test is launched.
     * \@runOnEnv(param1,param2,param3): The scenario will only be executed if ALL the params are defined.
     * </dd>
     * </dl>
     * Additionally, if the scenario contains the following tag:
     * <dl>
     *    <dt>\@skipOnEnv(param)</dt>
     *<dd>The scenario will be omitted if the param is defined when test is launched.
     * More than one param can be passed in the tag. To do so, the params must be comma separated.
     * The scenario will omitted if ANY of params are defined. (OR)</dd>
     *
     *<dd>Or in separated lines to force ALL of the params to be defined in order to omit the execution</dd>
     *    <dt>  \@skipOnEnv(param1)
     *          \@skipOnEnv(param2)
     *          \@skipOnEnv(param3)</dt>
     *<dd>The scenario will omitted if ALL of params are defined. (AND)</dd>
     *</dl>
     *
     * @param pjp
     * @param tags
     * @param location
     * @param keyword
     * @param name
     * @param description
     * @param steps
     * @param examples
     * @throws Throwable
     */
    public void aroundScenarios(ProceedingJoinPoint pjp, List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps, List<Examples> examples) throws Throwable {

        Boolean exit = tagsIteration(tags, location);

        if (exit) {
            ThreadProperty.set("skippedOnParams" + pjp.getArgs()[3].toString() + location.getLine(), "true");
        }

        pjp.proceed();
    }


    public boolean tagsIteration(List<Tag> tags, Location location) throws Exception {
        for (Tag tag : tags) {
            if (tag.getName().contains("@runOnEnv")) {
                if (!checkParams(getParams(tag.getName()))) {
                    tags.add(new Tag(location, "@ignore"));
                    tags.add(new Tag(location, "@envCondition"));
                    return true;
                }
            } else if (tag.getName().contains("@skipOnEnv")) {
                if (checkParams(getParams(tag.getName()))) {
                    tags.add(new Tag(location, "@ignore"));
                    tags.add(new Tag(location, "@envCondition"));
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
            throw new Exception ("Error while parsing params. Format is: \"runOnEnv(PARAM)\", but found: " + s);
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
