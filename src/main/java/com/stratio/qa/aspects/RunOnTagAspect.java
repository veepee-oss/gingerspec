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
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.util.parsing.combinator.testing.Str;

import javax.swing.tree.ExpandVetoException;
import java.util.List;

@Aspect
public class RunOnTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Pointcut("execution (gherkin.formatter.model.Scenario.new(..)) && " +
              "args(comments, tags, keyword, name, description, line, id)")
    protected void AddRunOnTagPointcutScenario(List<Comment> comments, List<Tag> tags, String keyword, String name,
                                               String description, Integer line, String id) {
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
     * @param pjp ProceedingJoinPoint
     * @param comments comments
     * @param tags tags of scenario
     * @param keyword keyword
     * @param name name
     * @param description description
     * @param line line
     * @param id id
     * @throws Throwable exception
     */
    @Around(value = "AddRunOnTagPointcutScenario(comments, tags, keyword, name, description, line, id)")
    public void aroundAddRunOnTagPointcut(ProceedingJoinPoint pjp, List<Comment> comments, List<Tag> tags,
                                                  String keyword, String name, String description, Integer line, String id) throws Throwable {

        Scenario linescn = (Scenario) pjp.getTarget();
        Boolean exit = tagsIteration(tags, line);

        if (exit) {
            ThreadProperty.set("skippedOnParams" + pjp.getArgs()[3].toString() + linescn.getLine(), "true");
        }
    }

    public boolean tagsIteration(List<Tag> tags, Integer line) throws Exception {
        for (Tag tag : tags) {
            if (tag.getName().contains("@runOnEnv")) {
                if (!checkParams(getParams(tag.getName()))) {
                    tags.add(new Tag("@ignore", line));
                    tags.add(new Tag("@envCondition", line));
                    return true;
                }
            } else if (tag.getName().contains("@skipOnEnv")) {
                if (checkParams(getParams(tag.getName()))) {
                    tags.add(new Tag("@ignore", line));
                    tags.add(new Tag("@envCondition", line));
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
