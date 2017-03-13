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
import cucumber.runtime.model.CucumberScenarioOutline;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

@Aspect
public class ReplacementAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass()
            .getCanonicalName());
    private String lastEchoedStep = "";

    @Pointcut("execution (public String CucumberReporter.TestMethod.obtainOutlineScenariosExamples(..)) && "
            + "args (examplesData)")
    protected void replacementOutlineScenariosCallPointcut(String examplesData) {
    }

    @Around(value = "replacementOutlineScenariosCallPointcut(examplesData)")
    public Object aroundReplacementOutlineScenariosCalls(ProceedingJoinPoint pjp, String examplesData) throws Throwable {
        if (isSkippedOnParams(pjp)) {
            return null;
        }

        if (CucumberScenarioOutline.class.isAssignableFrom(pjp.getSourceLocation().getWithinType())) {
            String name = (String) pjp.proceed();
            return name;
        }

        String newExamplesData = examplesData;

        if (newExamplesData.contains("${")) {
            newExamplesData = replaceEnvironmentPlaceholders(newExamplesData, pjp);
        }
        if (newExamplesData.contains("!{")) {
            newExamplesData = replaceReflectionPlaceholders(newExamplesData, pjp);
        }
        if (newExamplesData.contains("@{")) {
            newExamplesData = replaceCodePlaceholders(newExamplesData, pjp);
        }

        Object[] myArray = {newExamplesData};
        return pjp.proceed(myArray);
    }


    @Pointcut("execution (public * gherkin.formatter.model.Step.getRows())")
    protected void replacementDataTableStatementName() {
    }

    @Around(value = "replacementDataTableStatementName()")
    public List<DataTableRow> aroundReplacementDataTableStatementName(ProceedingJoinPoint pjp) throws Throwable {
        if (isSkippedOnParams(pjp)) {
            return null;
        }
        List<DataTableRow> dataTableOld = (List<DataTableRow>) pjp.proceed();
        if (dataTableOld != null) {
            for (int i = 0; i < dataTableOld.size(); i++) {
                List<String> row = dataTableOld.get(i).getCells();
                for (int x = 0; x < row.size(); x++) {
                    String value = row.get(x);
                    if (value.contains("${")) {
                        value = replaceEnvironmentPlaceholders(value,pjp);
                    }
                    if (value.contains("!{")) {
                        value = replaceReflectionPlaceholders(value,pjp);
                    }
                    if (value.contains("@{")) {
                        value = replaceCodePlaceholders(value,pjp);
                    }
                    dataTableOld.get(i).getCells().set(x, value);
                }
            }
        }
        return dataTableOld;
    }

    @Pointcut("call(String gherkin.formatter.model.BasicStatement.getName())")
    protected void replacementBasicStatementName() {
    }

    @Around(value = "replacementBasicStatementName()")
    public String aroundReplacementBasicStatementName(ProceedingJoinPoint pjp) throws Throwable {
        if (isSkippedOnParams(pjp)) {
            return "Omitted scenario";
        }
        String newBasicStmt = (String) pjp.proceed();

        if (CucumberScenarioOutline.class.isAssignableFrom(pjp.getSourceLocation().getWithinType())) {
            String name = (String) pjp.proceed();
            return name;
        }

        if (newBasicStmt.contains("${")) {
            newBasicStmt = replaceEnvironmentPlaceholders(newBasicStmt, pjp);
        }
        if (newBasicStmt.contains("!{")) {
            newBasicStmt = replaceReflectionPlaceholders(newBasicStmt, pjp);
        }
        if (newBasicStmt.contains("@{")) {
            newBasicStmt = replaceCodePlaceholders(newBasicStmt, pjp);
        }

        if ((pjp.getTarget() instanceof Step) &&
                !(lastEchoedStep.equals(newBasicStmt)) &&
                !(newBasicStmt.contains("'<"))) {
            lastEchoedStep = newBasicStmt;
            logger.debug("  {}{}", ((Step) pjp.getTarget()).getKeyword(), newBasicStmt);
        }
        return newBasicStmt;
    }

    private boolean isSkippedOnParams (ProceedingJoinPoint pjp) {


        if (pjp.getTarget() instanceof Scenario) {
            try {
                Scenario linescn = (Scenario) pjp.getTarget();
                return ("true".equals(ThreadProperty.get("skippedOnParams" + pjp.proceed() + linescn.getLine())));
            } catch (Throwable throwable) {
                return false;
            }
        } else {
            return false;
        }
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
    protected String replaceCodePlaceholders(String element, ProceedingJoinPoint pjp) throws Exception {
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
                        Enumeration<InetAddress> ifs = NetworkInterface.getByName(subproperty).getInetAddresses();
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
                        throw new Exception("Interface " + subproperty + " not available");
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
    protected String replaceReflectionPlaceholders(String element, ProceedingJoinPoint pjp) throws Exception {
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
    protected String replaceEnvironmentPlaceholders(String element, ProceedingJoinPoint pjp) throws NonReplaceableException {
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

            if (prop == null && (pjp.getThis() instanceof CucumberReporter.TestMethod)) {
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

