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
import com.privalia.qa.lookups.DefaultLookUp;
import com.privalia.qa.lookups.EnvPropertyLookup;
import com.privalia.qa.lookups.LowerCaseLookup;
import com.privalia.qa.lookups.UpperCaseLookUp;
import com.privalia.qa.specs.CommonG;
import com.privalia.qa.utils.ThreadProperty;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.resource.Resource;
import io.cucumber.core.stepexpression.DataTableArgument;
import io.cucumber.core.stepexpression.DocStringArgument;
import io.cucumber.core.stepexpression.ExpressionArgument;
import io.cucumber.cucumberexpressions.Group;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;

/**
 * Aspect to replace variables used in the feature files
 *
 * @author Jose Fernandez
 */
@Aspect
public final class ReplacementAspect {

    private static Logger logger = LoggerFactory.getLogger(ReplacementAspect.class.getCanonicalName());

    private static Map<String, StringLookup> stringLookupMap = new HashMap<String, StringLookup>() {
        {
            put("envProperties", new EnvPropertyLookup());
            put("toUpperCase", new UpperCaseLookUp());
            put("toLowerCase", new LowerCaseLookup());
        }
    };

    private static StringSubstitutor interpolator = new StringSubstitutor(StringLookupFactory.INSTANCE.interpolatorStringLookup(stringLookupMap, new DefaultLookUp(), true))
            .setEnableSubstitutionInVariables(true);


    @Pointcut("execution (* io.cucumber.core.runner.PickleStepDefinitionMatch.runStep(..)) && args(state)")
    protected void replacementArguments(TestCaseState state) {
    }

    /**
     * When a step is about to be executed, the Match#getArguments method is called. this function retrieves the the arguments that
     * are going to be used when executing the glue method.
     * <p>
     * This method captures this event and replaces the variables with their appropriate value using reflection
     *
     * @param jp    the jp
     * @param state the state
     * @throws NoSuchFieldException    the no such field exception
     * @throws IllegalAccessException  the illegal access exception
     * @throws FileNotFoundException   the file not found exception
     * @throws NonReplaceableException the non replaceable exception
     * @throws ConfigurationException  the configuration exception
     * @throws URISyntaxException      the uri syntax exception
     */
    @Before(value = "replacementArguments(state)")
    public void aroundReplacementArguments(JoinPoint jp, TestCaseState state) throws NoSuchFieldException, IllegalAccessException, FileNotFoundException, NonReplaceableException, ConfigurationException, URISyntaxException {

        Object pickleStepDefinitionMatch = jp.getThis();

        if (pickleStepDefinitionMatch.getClass().getName().matches("io.cucumber.core.runner.PickleStepDefinitionMatch")) {
            Field argumentsField = pickleStepDefinitionMatch.getClass().getSuperclass().getDeclaredField("arguments");
            argumentsField.setAccessible(true);
            List<io.cucumber.core.stepexpression.Argument> arguments = (List<io.cucumber.core.stepexpression.Argument>) argumentsField.get(pickleStepDefinitionMatch);

            for (io.cucumber.core.stepexpression.Argument argument : arguments) {

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
                        for (Group child : children) {
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
                    Field docstringField = docStringArgument.getClass().getDeclaredField("content");
                    docstringField.setAccessible(true);
                    String docStringValue = (String) docstringField.get(docStringArgument);
                    String replacedDocStringValue = replacedElement(docStringValue, jp);
                    docstringField.set(docStringArgument, replacedDocStringValue);
                }
            }
        }
    }


    public static String replacedElement(String el, JoinPoint jp) throws NonReplaceableException, ConfigurationException, URISyntaxException, FileNotFoundException {
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

    private static File getfile(String environment) throws URISyntaxException, FileNotFoundException {

        URL url = ReplacementAspect.class.getClassLoader().getResource("configuration/" + environment + ".properties");

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
     * @deprecated Deprecated. This same function can now be perform via {@link StringSubstitutor} with the {@link EnvPropertyLookup}
     * (i.e. #{my.key} can be represented as ${envProperties:my.key})
     */
    @Deprecated
    protected static String replacePropertyPlaceholders(String element, JoinPoint pjp) throws ConfigurationException, URISyntaxException, NonReplaceableException, FileNotFoundException {

        String newVal = element;
        Parameters params = new Parameters();
        CombinedConfiguration config = new CombinedConfiguration(new OverrideCombiner());

        /*If environment specific file is required, search it by its name and add it as a source of properties*/
        String environment = System.getProperty("env", null);
        if (environment != null) {
            FileBasedConfigurationBuilder<FileBasedConfiguration> config2 = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                    PropertiesConfiguration.class).configure(params.properties().setFile(getfile(environment)));
            config.addConfiguration(config2.getConfiguration());
        }

        /*Add the file common.properties as a source of properties*/
        FileBasedConfigurationBuilder<FileBasedConfiguration> config1 = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                PropertiesConfiguration.class)
                .configure(params.properties().setFile(getfile("common")));

        config.addConfiguration(config1.getConfiguration());


        while (newVal.contains("#{")) {
            String placeholder = newVal.substring(newVal.indexOf("#{"), newVal.indexOf("}", newVal.indexOf("#{")) + 1);
            String property = placeholder.substring(2, placeholder.length() - 1);

            String prop = config.getString(property);
            if (prop != null) {
                newVal = newVal.replace(placeholder, prop);
            } else {
                Assertions.fail("Could not find property %s in included files", property);
                //logger.error("Could not find property {} in included files", property);
                //throw new NonReplaceableException("Unreplaceable placeholder: " + placeholder);
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
     * @deprecated Deprecated. @{} functions can now be performed using {@link StringSubstitutor} with the ${}
     * variable placeholder
     */
    @Deprecated
    protected static String replaceCodePlaceholders(String element, JoinPoint pjp) throws NonReplaceableException {
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
                if (pjp.getThis().getClass().getName().matches("io.cucumber.core.gherkin.messages.GherkinMessagesPickle")) {
                    return newVal;
                } else {
                    Assertions.fail("%s -> %s placeholded element has not been replaced previously.", element, property);
                    //logger.error("{} -> {} placeholded element has not been replaced previously.", element, property);
                    //throw new NonReplaceableException("Unreplaceable placeholder: " + placeholder);
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
                            logger.error(e.getMessage());
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
     * @deprecated Deprecated. ${} placeholder can also be used as placeholder for Thread variables
     */
    @Deprecated
    protected static String replaceReflectionPlaceholders(String element, JoinPoint pjp) {
        String newVal = element;
        while (newVal.contains("!{")) {
            String placeholder = newVal.substring(newVal.indexOf("!{"),
                    newVal.indexOf("}", newVal.indexOf("!{")) + 1);
            String attribute = placeholder.substring(2, placeholder.length() - 1);
            // we want to use value previously saved
            String prop = ThreadProperty.get(attribute);

            if (prop == null && (pjp.getThis().getClass().getName().matches("io.cucumber.core.gherkin.messages.GherkinMessagesPickle"))) {
                return element;
            } else if (prop == null) {
                newVal = newVal.replace(placeholder, "NULL");
                Assertions.fail("%s -> %s local variable has not been saved correctly previously.", element, attribute);
                //logger.warn("{} -> {} local var has not been saved correctly previously.", element, attribute);
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
    protected static String replaceEnvironmentPlaceholders(String element, JoinPoint jp) throws NonReplaceableException {

        boolean canFail = false;

        if (jp != null) {
            if (!(jp.getThis().getClass().getName().matches("io.cucumber.core.gherkin.messages.GherkinMessagesPickle"))) {
                canFail = true;
            }
        }

        try {
            interpolator.setEnableUndefinedVariableException(canFail);
            return interpolator.replace(element);
        } catch (Exception e) {
            if (!canFail) {
                return element;
            }
            Assertions.fail(e.getMessage());
        }

        return null;
    }

    private static Properties mergeProperties(Properties... properties) {
        Properties mergedProperties = new Properties();
        for (Properties property : properties) {
            mergedProperties.putAll(property);
        }
        return mergedProperties;
    }


}

