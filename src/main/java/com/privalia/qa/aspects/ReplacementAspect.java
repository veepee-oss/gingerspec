/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
*/

package com.privalia.qa.aspects;


import com.privalia.qa.exceptions.NonReplaceableException;
import com.privalia.qa.lookups.*;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.stepexpression.DataTableArgument;
import io.cucumber.core.stepexpression.DocStringArgument;
import io.cucumber.core.stepexpression.ExpressionArgument;
import io.cucumber.cucumberexpressions.Group;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            put("faker", new FakerLookUp());
            put("math", new MathLookup());
        }
    };

    public static StringSubstitutor getInterpolator() {
        return interpolator;
    }

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
                        String replacedValue = replacePlaceholders(currentTextValue, true);

                        Group group = textArgument.getGroup();
                        Field valueField = group.getClass().getDeclaredField("value");
                        valueField.setAccessible(true);
                        valueField.set(group, replacedValue);

                        List<Group> children = group.getChildren();
                        for (Group child : children) {
                            Field valueFieldChild = child.getClass().getDeclaredField("value");
                            String valuechild = child.getValue();
                            if (valuechild != null) {
                                String replacedValueChild = replacePlaceholders(valuechild, true);
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
                            row.set(i, replacePlaceholders(row.get(i), true));
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
                    String replacedDocStringValue = replacePlaceholders(docStringValue, true);
                    docstringField.set(docStringArgument, replacedDocStringValue);
                }
            }
        }
    }

    /**
     * Replaces every placeholder element, enclosed in ${} with the
     * corresponding value
     *
     * @param element element to be replaced
     * @param setEnableUndefinedVariableException      whether an exception should be thrown is a variable could not be replaced
     * @return String
     * @throws NonReplaceableException exception
     */
    public static String replacePlaceholders(String element, boolean setEnableUndefinedVariableException) throws NonReplaceableException {

        if (element == null) {
            return null;
        }

        try {
            interpolator.setEnableUndefinedVariableException(setEnableUndefinedVariableException);
            return interpolator.replace(element);
        } catch (Exception e) {
            if (!setEnableUndefinedVariableException) {
                return element;
            }
            Assertions.fail(e.getMessage());
        }

        return null;
    }


}

