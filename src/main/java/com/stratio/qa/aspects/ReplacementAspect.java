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

import com.stratio.qa.cucumber.testng.CucumberReporter;
import com.stratio.qa.exceptions.NonReplaceableException;
import com.stratio.qa.specs.CommonG;
import com.stratio.qa.utils.ThreadProperty;
import gherkin.I18n;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

@Aspect
public class ReplacementAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass()
            .getCanonicalName());
    private String lastEchoedStep = "";


    @Pointcut("(execution (gherkin.formatter.model.Scenario.new(..)) ||  execution (gherkin.formatter.model.ScenarioOutline.new(..))) && "
            + "args (comments, tags, keyword, name, description, line, id) ")
    protected void replacementScenarios(List<Comment> comments, List<Tag> tags, String keyword, String name, String description, Integer line, String id) {
    }

    @After(value = "replacementScenarios(comments, tags, keyword, name, description, line, id)")
    public void aroundScenarios(JoinPoint jp, List<Comment> comments, List<Tag> tags, String keyword, String name, String description, Integer line, String id) throws Throwable {

        BasicStatement scenario = (BasicStatement) jp.getThis();
        String scenarioName = scenario.getName();
        String newScenarioName = replacedElement(scenarioName , jp);

        if (!scenarioName.equals(newScenarioName)) {
            Field field = null;
            Class current = scenario.getClass();
            do {
                try {
                    field = current.getDeclaredField("name");
                } catch(Exception e) {}
            } while((current = current.getSuperclass()) != null);

            field.setAccessible(true);
            field.set(scenario, replacedElement(name, jp));
        }
    }

    @Pointcut("execution (public void cucumber.runtime.Runtime.runStep(..)) && "
            + "args (featurePath, step, reporter, i18n)")
    protected void replacementStar(String featurePath, Step step, Reporter reporter, I18n i18n) {
    }

    @Before(value = "replacementStar(featurePath, step, reporter, i18n)")
    public void aroundReplacementStar(JoinPoint jp, String featurePath, Step step, Reporter reporter, I18n i18n) throws Throwable{
        DocString docString = step.getDocString();
        List<DataTableRow> rows= step.getRows();
        if (docString != null) {
            String value = replacedElement(docString.getValue(), jp);
            Field field = docString.getClass().getField("value");
            field.set(field, value);
        }
        if (rows != null) {
            for (int r = 0; r < rows.size(); r++) {
                List<String> cells = rows.get(r).getCells();
                for (int c = 0; c < cells.size(); c++) {
                    cells.set(c, replacedElement(cells.get(c), jp));
                }
            }
        }

        String stepName = step.getName();
        String newName = replacedElement(stepName , jp);
        if (!stepName.equals(newName)) {
            //field up to BasicStatement, from Step and ExampleStep
            Field field = null;
            Class current = step.getClass();
            do {
                try {
                    field = current.getDeclaredField("name");
                } catch(Exception e) {}
            } while((current = current.getSuperclass()) != null);

            field.setAccessible(true);
            field.set(step, newName);
        }

        lastEchoedStep = step.getName();
        logger.info("  {}{}", step.getKeyword(), step.getName());
    }

    private String replacedElement(String el, JoinPoint jp) throws NonReplaceableException {
        if (el.contains("${")) {
            el = replaceEnvironmentPlaceholders(el, jp);
        }
        if (el.contains("!{")) {
            el = replaceReflectionPlaceholders(el, jp);
        }
        if (el.contains("@{")) {
            el = replaceCodePlaceholders(el, jp);
        }
        return el;
    }

    /**
     * Replaces every placeholded element, enclosed in @{} with the
     * corresponding attribute value in local Common class
     * <p>
     * If the element starts with:
     * - IP: We expect it to be followed by '.' + interface name (i.e. IP.eth0). It can contain other replacements.
     * <p>
     * If the element starts with:
     * - JSON: We expect it to be followed by '.' + path_to_json_file (relative to src/test/resources or
     * target/test-classes). The json is read and its content is returned as a string
     * <p>
     * If the element starts with:
     * - FILE: We expect it to be followed by '.' + path_to_file (relative to src/test/resources or
     * target/test-classes). The file is read and its content is returned as a string
     *
     * @param element
     * @return String
     * @throws Exception
     */
    protected String replaceCodePlaceholders(String element, JoinPoint pjp) throws NonReplaceableException {
        String newVal = element;
        while (newVal.contains("@{")) {
            String placeholder = newVal.substring(newVal.indexOf("@{"), newVal.indexOf("}", newVal.indexOf("@{")) + 1);
            String property = placeholder.substring(2, placeholder.length() - 1).toLowerCase();
            String subproperty = "";
            CommonG commonJson;
            if (placeholder.contains(".")) {
                property = placeholder.substring(2, placeholder.indexOf(".")).toLowerCase();
                subproperty = placeholder.substring(placeholder.indexOf(".") + 1, placeholder.length() - 1);
            } else {
                if (pjp.getThis() instanceof CucumberReporter.TestMethod) {
                    return newVal;
                } else {
                    logger.error("{} -> {} placeholded element has not been replaced previously.", element, property);
                    throw new NonReplaceableException("Unreplaceable placeholder: " + placeholder);
                }
            }

            switch (property) {
                case "ip":
                    boolean found = false;
                    if (!subproperty.isEmpty()) {
                        Enumeration<InetAddress> ifs = null;
                        try {
                            ifs = NetworkInterface.getByName(subproperty).getInetAddresses();
                        } catch (SocketException e) {
                            this.logger.error(e.getMessage());
                        }
                        while (ifs.hasMoreElements() && !found) {
                            InetAddress itf = ifs.nextElement();
                            if (itf instanceof Inet4Address) {
                                String ip = itf.getHostAddress();
                                newVal = newVal.replace(placeholder, ip);
                                found = true;
                            }
                        }
                    }
                    if (!found) {
                        throw new NonReplaceableException("Interface " + subproperty + " not available");
                    }
                    break;
                case "json":
                case "file":
                    commonJson = new CommonG();
                    newVal = newVal.replace(placeholder, commonJson.retrieveData(subproperty, property));
                    break;
                default:
                    commonJson = new CommonG();
                    commonJson.getLogger().error("Replacement with an undefined option ({})", property);
                    newVal = newVal.replace(placeholder, "");
            }
        }
        return newVal;
    }


    /**
     * Replaces every placeholded element, enclosed in !{} with the
     * corresponding attribute value in local Common class
     *
     * @param element
     * @return String
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    protected String replaceReflectionPlaceholders(String element, JoinPoint pjp) throws NonReplaceableException {
        String newVal = element;
        while (newVal.contains("!{")) {
            String placeholder = newVal.substring(newVal.indexOf("!{"),
                    newVal.indexOf("}", newVal.indexOf("!{")) + 1);
            String attribute = placeholder.substring(2, placeholder.length() - 1);
            // we want to use value previously saved
            String prop = ThreadProperty.get(attribute);

            if (prop == null && (pjp.getThis() instanceof CucumberReporter.TestMethod)) {
                return element;
            } else if (prop == null) {
                logger.error("{} -> {} local var has not been saved correctly previously.", element, attribute);
                throw new NonReplaceableException("Unreplaceable placeholder: " + placeholder);
            } else {
                newVal = newVal.replace(placeholder, prop);
            }
        }
        return newVal;
    }


    /**
     * Replaces every placeholded element, enclosed in ${} with the
     * corresponding java property
     *
     * @param element
     * @return String
     */
    protected String replaceEnvironmentPlaceholders(String element, JoinPoint jp) throws NonReplaceableException {
        String newVal = element;
        while (newVal.contains("${")) {
            String placeholder = newVal.substring(newVal.indexOf("${"),
                    newVal.indexOf("}", newVal.indexOf("${")) + 1);
            String modifier = "";
            String sysProp;
            if (placeholder.contains(".")) {
                sysProp = placeholder.substring(2, placeholder.indexOf("."));
                modifier = placeholder.substring(placeholder.indexOf(".") + 1,
                        placeholder.length() - 1);
            } else {
                sysProp = placeholder.substring(2, placeholder.length() - 1);
            }

            String prop = System.getProperty(sysProp);

            if (prop == null && (jp.getThis() instanceof CucumberReporter.TestMethod)) {
                return element;
            } else if (prop == null) {
                logger.error("{} -> {} env var has not been defined.", element, sysProp);
                throw new NonReplaceableException("Unreplaceable placeholder: " + placeholder);
            }

            if ("toLower".equals(modifier)) {
                prop = prop.toLowerCase();
            } else if ("toUpper".equals(modifier)) {
                prop = prop.toUpperCase();
            }
            newVal = newVal.replace(placeholder, prop);
        }

        return newVal;
    }
}

