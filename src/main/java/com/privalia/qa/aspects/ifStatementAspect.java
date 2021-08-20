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

import com.privalia.qa.lookups.DefaultLookUp;
import com.privalia.qa.lookups.EnvPropertyLookup;
import com.privalia.qa.lookups.LowerCaseLookup;
import com.privalia.qa.lookups.UpperCaseLookUp;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aspect to control the conditional execution of steps based on a given expression.
 *
 * @author Jose Fernandez
 * @see com.privalia.qa.specs.UtilsGSpec#ifStamenetBeginBlock(String)
 */
@Aspect
public class ifStatementAspect {

    private static Boolean step_should_run = true;

    private static String step_text = "";

    private static Map<String, StringLookup> stringLookupMap = new HashMap<String, StringLookup>() {
        {
            put("envProperties", new EnvPropertyLookup());
            put("toUpperCase", new UpperCaseLookUp());
            put("toLowerCase", new LowerCaseLookup());
        }
    };

    private static StringSubstitutor interpolator = new StringSubstitutor(StringLookupFactory.INSTANCE.interpolatorStringLookup(stringLookupMap, new DefaultLookUp(), true))
            .setEnableSubstitutionInVariables(true);

    /**
     * This pointcut is triggered right before the step is executed
     *
     * @param state state
     */
    @Pointcut("execution (* io.cucumber.core.runner.PickleStepDefinitionMatch.runStep(..)) && args(state)")
    protected void replacementArguments(TestCaseState state) {
    }

    @Around(value = "replacementArguments(state)")
    public void aroundReplacementArguments(ProceedingJoinPoint pjp, TestCaseState state) throws Throwable {

        Object pickleStepDefinitionMatch = pjp.getThis();

        if (pickleStepDefinitionMatch.getClass().getName().matches("io.cucumber.core.runner.PickleStepDefinitionMatch")) {

            Field stepField = pickleStepDefinitionMatch.getClass().getDeclaredField("step");
            stepField.setAccessible(true);
            io.cucumber.core.gherkin.Step step = (io.cucumber.core.gherkin.Step) stepField.get(pickleStepDefinitionMatch);

            step_text = step.getText();

            if (step.getText().startsWith("if (")) {
                step_should_run = this.validateStatement(step.getText());
            }

            if (step_should_run) {
                pjp.proceed();
            }

            if (step.getText().matches("}")) {
                step_should_run = true;
            }
        }
    }

    /**
     * This pointcut is triggered right after the creation of the TestStepFinished event. If the step was not executed
     * the status of the result is changed to "SKIPPED", so it is colored in a different way in the console and in reports
     *
     * @param timeInstant timeInstant
     * @param testCase    testCase
     * @param testStep    testStep
     * @param result      result
     */
    @Pointcut("execution (io.cucumber.plugin.event.TestStepFinished.new(..)) && args(timeInstant, testCase, testStep, result)")
    protected void setResult(Instant timeInstant, TestCase testCase, TestStep testStep, Result result) {
    }

    @After(value = "setResult(timeInstant, testCase, testStep, result)")
    public void aroundSetResult(JoinPoint jp, Instant timeInstant, TestCase testCase, TestStep testStep, Result result) throws NoSuchFieldException, IllegalAccessException {

        Object testStepFinished = jp.getThis();
        Field currentResult = testStepFinished.getClass().getDeclaredField("result");
        currentResult.setAccessible(true);
        io.cucumber.plugin.event.Result currentResultObject = (io.cucumber.plugin.event.Result) currentResult.get(testStepFinished);
        io.cucumber.plugin.event.Result skippedResult = new io.cucumber.plugin.event.Result(Status.SKIPPED, currentResultObject.getDuration(), currentResultObject.getError());

        if (step_text.startsWith("if (") || step_text.matches("}")) {
            return;
        }

        if (!step_should_run) {
            currentResult.set(testStepFinished, skippedResult);
        }

    }

    /**
     * Captures the given javascript statement and determines if it resolves
     * to true or false.
     *
     * @param text Step text
     * @return True if the expression can be resolved to true, false otherwise
     */
    private Boolean validateStatement(String text) {

        String pattern = "if \\((.*)\\) \\{";
        Pattern r = Pattern.compile(pattern);
        String statement = "";

        Matcher m = r.matcher(text);
        if (m.find()) {
            statement = m.group(1);
        }

        String result = interpolator.replace("${script:javascript:" + statement + "}");
        return Boolean.parseBoolean(result.toLowerCase());
    }
}
