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


import com.privalia.qa.exceptions.NonReplaceableException;
import com.privalia.qa.specs.CommonG;
import com.privalia.qa.utils.ThreadProperty;
import cucumber.runtime.StepDefinitionMatch;
import gherkin.ast.*;
import io.cucumber.cucumberexpressions.Group;
import io.cucumber.stepexpression.DataTableArgument;
import io.cucumber.stepexpression.DocStringArgument;
import io.cucumber.stepexpression.ExpressionArgument;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * Aspect to replace variables used in the feature files
 *
 * @author Jose Fernandez
 */
@Aspect
public class ReplacementAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    private StepDefinitionMatch lastEchoedStep;

    @Pointcut("execution (gherkin.ast.Scenario.new(..)) && args(tags, location, keyword, name, description, steps)")
    protected void replacementScenarios(List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps) {
    }

    @Pointcut("execution (gherkin.ast.ScenarioOutline.new(..)) && args(tags, location, keyword, name, description, steps, examples)")
    protected void replacementScenariosOutline(List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps, List<Examples> examples) {
    }

    @Before("replacementScenariosOutline(tags, location, keyword, name, description, steps, examples)")
    public void aroundScenariosOutlineDefinition(JoinPoint jp, List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps, List<Examples> examples) throws Throwable {
        this.aroundScenarios(jp, tags, location, keyword, name, description, steps, examples);
    }

    @Before("replacementScenarios(tags, location, keyword, name, description, steps)")
    public void aroundScenariosDefinition(JoinPoint jp, List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps) throws Throwable {
        this.aroundScenarios(jp, tags, location, keyword, name, description, steps, null);
    }

    /**
     * replaces any variable placeholder in the scenario/Scenario Outline title.
     *
     * @param jp          the jp
     * @param tags        the tags
     * @param location    the location
     * @param keyword     the keyword
     * @param name        the name
     * @param description the description
     * @param steps       the steps
     * @param examples    the examples
     * @throws Throwable the throwable
     */
    public void aroundScenarios(JoinPoint jp, List<Tag> tags, Location location, String keyword, String name, String description, List<Step> steps, List<Examples> examples) throws Throwable {

        ScenarioDefinition scenario = (ScenarioDefinition) jp.getThis();
        String scenarioName = scenario.getName();
        String newScenarioName = replacedElement(scenarioName, jp);

        if (!scenarioName.equals(newScenarioName)) {
            Field field = null;
            Class current = scenario.getClass();
            do {
                try {
                    field = current.getDeclaredField("name");
                } catch (Exception e) {
                }
            } while ((current = current.getSuperclass()) != null);

            field.setAccessible(true);
            field.set(scenario, replacedElement(name, jp));
        }
    }


    @Pointcut("execution (* cucumber.runner.Match.getArguments(..)) && args()")
    protected void replacementArguments() {
    }

    /**
     * When a step is about to be executed, the Match#getArguments method is called. this function retrieves the the arguments that
     * are going to be used when executing the glue method.
     * <p>
     * This method captures this event and replaces the variables with their appropriate value using reflection
     *
     * @param jp the jp
     * @throws NoSuchFieldException    the no such field exception
     * @throws IllegalAccessException  the illegal access exception
     * @throws FileNotFoundException   the file not found exception
     * @throws NonReplaceableException the non replaceable exception
     * @throws ConfigurationException  the configuration exception
     * @throws URISyntaxException      the uri syntax exception
     */
    @Before(value = "replacementArguments()")
    public void aroundReplacementArguments(JoinPoint jp) throws NoSuchFieldException, IllegalAccessException, FileNotFoundException, NonReplaceableException, ConfigurationException, URISyntaxException {

        Object match = jp.getThis();

        /*To avoid executing replacement on the same step several times*/
        if (match.equals(lastEchoedStep)) {
            return;
        } else {
            lastEchoedStep = (StepDefinitionMatch) match;
        }

        if (match.getClass().getName().matches("cucumber.runner.PickleStepDefinitionMatch")) {

            Field argumentsField = match.getClass().getSuperclass().getDeclaredField("arguments");
            argumentsField.setAccessible(true);
            List<io.cucumber.stepexpression.Argument> arguments = (List<io.cucumber.stepexpression.Argument>) argumentsField.get(match);

            for (io.cucumber.stepexpression.Argument argument : arguments) {

                //if (argument.getValue() != null) {

                //If is a normal expression argument
                if (argument instanceof ExpressionArgument) {
                    ExpressionArgument expressionArgument = (ExpressionArgument) argument;
                    Field textField = expressionArgument.getClass().getDeclaredField("argument");
                    textField.setAccessible(true);
                    io.cucumber.cucumberexpressions.Argument textArgument = (io.cucumber.cucumberexpressions.Argument) textField.get(expressionArgument);
                    String currentTextValue = textArgument.getGroup().getValue();

                    /*In steps with optional params, the argument could be null*/
                    if (currentTextValue != null) {
                        String replacedValue = replacedElement(currentTextValue, jp);

                        Group group = textArgument.getGroup();
                        Field valueField = group.getClass().getDeclaredField("value");
                        valueField.setAccessible(true);
                        valueField.set(group, replacedValue);

                        List<Group> children = group.getChildren();
                        for (Group child: children) {
                            Field valueFieldChild = child.getClass().getDeclaredField("value");
                            String valuechild = child.getValue();
                            if (valuechild != null) {
                                String replacedValueChild = replacedElement(valuechild, jp);
                                valueFieldChild.setAccessible(true);
                                valueFieldChild.set(child, replacedValueChild);
                            }
                        }

                    }
                }

                //If is a datatable argument
                if (argument instanceof DataTableArgument) {
                    DataTableArgument dataTabeArgument = (DataTableArgument) argument;
                    Field listField = dataTabeArgument.getClass().getDeclaredField("argument");
                    listField.setAccessible(true);
                    List<List<String>> rows = (List<List<String>>) listField.get(dataTabeArgument);

                    for (List<String> row : rows) {
                        for (int i = 0; i <= row.size() - 1; i++) {
                            row.set(i, replacedElement(row.get(i), jp));
                        }
                    }

                    listField.set(dataTabeArgument, rows);
                }

                //If is a Docstring argument
                if (argument instanceof DocStringArgument) {
                    DocStringArgument docStringArgument = (DocStringArgument) argument;
                    Field docstringField = docStringArgument.getClass().getDeclaredField("argument");
                    docstringField.setAccessible(true);
                    String docStringValue = (String) docstringField.get(docStringArgument);
                    String replacedDocStringValue = replacedElement(docStringValue, jp);
                    docstringField.set(docStringArgument, replacedDocStringValue);
                }
                //}
            }

        } else {
            logger.error("Incorrect step definition match in Feature. Could not apply replacements");
        }
    }


    protected String replacedElement(String el, JoinPoint jp) throws NonReplaceableException, ConfigurationException, URISyntaxException, FileNotFoundException {
        if (el.contains("${")) {
            el = replaceEnvironmentPlaceholders(el, jp);
        }
        if (el.contains("!{")) {
            el = replaceReflectionPlaceholders(el, jp);
        }
        if (el.contains("@{")) {
            el = replaceCodePlaceholders(el, jp);
        }
        if (el.contains("#{")) {
            el = replacePropertyPlaceholders(el, jp);
        }
        return el;
    }

    private File getfile(String environment) throws URISyntaxException, FileNotFoundException {

        URL url = getClass().getClassLoader().getResource("configuration/" + environment + ".properties");

        if (url != null) {
            return new File(url.toURI());
        } else {
            logger.error("The configuration file {}.properties was not found", environment);
            throw new FileNotFoundException("The configuration file " + environment + ".properties was not found");
        }

    }

    /**
     * Replaces every placeholded element, enclosed in #{} with the
     * corresponding value in a properties file.
     * <p>
     * The file that contains all common configuration for the project (environment
     * independent configuration) must be located in /resources/configuration/common.properties
     * <p>
     * Environment-specific configuration can be located in a separated file. This configuration can
     * override the configuration from the common file. All environment specific configuration files
     * can be included at runtime via maven variable, setting the 'env' to the name of the file.
     * <p>
     * for example, to use properties from the file pre.properties located in
     * /resources/configuration/pre.properties, just pass -Denv=pre when
     * running your tests
     *
     * @param element element to be replaced
     * @param pjp     JoinPoint
     * @return resulting string
     * @throws ConfigurationException  ConfigurationException
     * @throws URISyntaxException      URISyntaxException
     * @throws NonReplaceableException NonReplaceableException
     * @throws FileNotFoundException   FileNotFoundException
     */
    protected String replacePropertyPlaceholders(String element, JoinPoint pjp) throws ConfigurationException, URISyntaxException, NonReplaceableException, FileNotFoundException {

        String newVal = element;
        Parameters params = new Parameters();
        CombinedConfiguration config = new CombinedConfiguration(new OverrideCombiner());

        /*If environment specific file is required, search it by its name and add it as a source of properties*/
        String environment = System.getProperty("env", null);
        if (environment != null) {
            FileBasedConfigurationBuilder<FileBasedConfiguration> config2 = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                    PropertiesConfiguration.class).configure(params.properties().setFile(this.getfile(environment)));
            config.addConfiguration(config2.getConfiguration());
        }

        /*Add the file common.properties as a source of properties*/
        FileBasedConfigurationBuilder<FileBasedConfiguration> config1 = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                PropertiesConfiguration.class)
                .configure(params.properties().setFile(this.getfile("common")));

        config.addConfiguration(config1.getConfiguration());


        while (newVal.contains("#{")) {
            String placeholder = newVal.substring(newVal.indexOf("#{"), newVal.indexOf("}", newVal.indexOf("#{")) + 1);
            String property = placeholder.substring(2, placeholder.length() - 1);

            String prop = config.getString(property);
            if (prop != null) {
                newVal = newVal.replace(placeholder, prop);
            } else {
                logger.error("Could not find property {} in included files", property);
                throw new NonReplaceableException("Unreplaceable placeholder: " + placeholder);
            }
        }

        return newVal;
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
     * @param element element to be replaced
     * @param pjp     JoinPoint
     * @return String
     * @throws NonReplaceableException exception
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
                if (pjp.getThis() instanceof ScenarioDefinition) {
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
     * @param element element to be replaced
     * @param pjp     JoinPoint
     * @return String string
     */
    protected String replaceReflectionPlaceholders(String element, JoinPoint pjp) {
        String newVal = element;
        while (newVal.contains("!{")) {
            String placeholder = newVal.substring(newVal.indexOf("!{"),
                    newVal.indexOf("}", newVal.indexOf("!{")) + 1);
            String attribute = placeholder.substring(2, placeholder.length() - 1);
            // we want to use value previously saved
            String prop = ThreadProperty.get(attribute);

            if (prop == null && (pjp.getThis() instanceof ScenarioDefinition)) {
                return element;
            } else if (prop == null) {
                logger.warn("{} -> {} local var has not been saved correctly previously.", element, attribute);
                newVal = newVal.replace(placeholder, "NULL");
                //throw new NonReplaceableException("Unreplaceable placeholder: " + placeholder);
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
     * @param element element to be replaced
     * @param jp      JoinPoint
     * @return String
     * @throws NonReplaceableException exception
     */
    protected String replaceEnvironmentPlaceholders(String element, JoinPoint jp) throws NonReplaceableException {
        String newVal = element;
        while (newVal.contains("${")) {
            String placeholder = newVal.substring(newVal.indexOf("${"),
                    newVal.indexOf("}", newVal.indexOf("${")) + 1);
            String modifier = "";
            String sysProp;
            String defaultValue = "";
            String prop;
            String placeholderAux = "";

            if (placeholder.contains(":-")) {
                defaultValue = placeholder.substring(placeholder.indexOf(":-") + 2, placeholder.length() - 1);
                placeholderAux = placeholder.substring(0, placeholder.indexOf(":-")) + "}";
            }

            if (placeholderAux.contains(".")) {
                if (placeholder.contains(":-")) {
                    sysProp = placeholderAux.substring(2, placeholderAux.indexOf("."));
                    modifier = placeholderAux.substring(placeholderAux.indexOf(".") + 1, placeholderAux.length() - 1);
                } else {
                    sysProp = placeholder.substring(2, placeholder.indexOf("."));
                    modifier = placeholder.substring(placeholder.indexOf(".") + 1, placeholder.length() - 1);
                }
            } else {
                if (defaultValue.isEmpty()) {
                    if (placeholder.contains(".")) {
                        modifier = placeholder.substring(placeholder.indexOf(".") + 1, placeholder.length() - 1);
                        sysProp = placeholder.substring(2, placeholder.indexOf("."));
                    } else {
                        sysProp = placeholder.substring(2, placeholder.length() - 1);
                    }
                } else {
                    sysProp = placeholder.substring(2, placeholder.indexOf(":-"));
                }
            }

            if (defaultValue.isEmpty()) {
                prop = System.getProperty(sysProp);
            } else {
                prop = System.getProperty(sysProp, defaultValue);
            }

            if (prop == null && (jp.getThis() instanceof ScenarioDefinition)) {
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

