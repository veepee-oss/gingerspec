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

package com.privalia.qa.cucumber.reporter;

import com.privalia.qa.utils.ThreadProperty;
import cucumber.api.*;
import cucumber.api.Scenario;
import cucumber.api.event.*;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.NiceAppendable;
import cucumber.runtime.formatter.Format;
import cucumber.runtime.formatter.Formats;
import cucumber.util.FixJava;
import cucumber.util.Mapper;
import gherkin.ast.*;
import gherkin.pickles.PickleTag;
import io.cucumber.stepexpression.DataTableArgument;
import io.cucumber.stepexpression.DocStringArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;


/**
 * This class implements a set of eventListeners for the tests suite. This methods are fired before/after
 * specific events during tests execution. Since the library uses testng and not junit to run the test
 * {@link ConcurrentEventListener} should be used instead of {@link EventListener}
 *
 * This class is responsible for printing the steps/features/scenarios to console. This is a custom
 * implementation of the cucumber PrettyFormatter that allow more flexibility with colors and other
 * formatting options. Error messages generated from other classes are printed using the standard log4j.
 *
 * Unlike regular {@link cucumber.runtime.formatter.PrettyFormatter}, this formatter prints steps BEFORE
 * they are executed, giving real-time feedback to the user about the test progress
 *
 * @author Jose Fernandez
 */
public class TestNGPrettyFormatter implements ConcurrentEventListener, ColorAware {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    private static final String SCENARIO_INDENT = "  ";

    private static final String STEP_INDENT = "    ";

    private static final String EXAMPLES_INDENT = "    ";

    private static final String TABLES_INDENT = "     ";

    private final TestSourcesModel testSources = new TestSourcesModel();

    private final NiceAppendable out;

    private Formats formats;

    private String currentFeatureFile;

    private TestCase currentTestCase;

    private ScenarioOutline currentScenarioOutline;

    private Examples currentExamples;

    private int locationIndentation;

    private Mapper<Tag, String> tagNameMapper = new Mapper<Tag, String>() {
        @Override
        public String map(Tag tag) {
            return tag.getName();
        }
    };

    private Mapper<PickleTag, String> pickleTagNameMapper = new Mapper<PickleTag, String>() {
        @Override
        public String map(PickleTag pickleTag) {
            return pickleTag.getName();
        }
    };

    private EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
        @Override
        public void receive(TestSourceRead event) {
            handleTestSourceRead(event);
        }
    };

    private EventHandler<TestCaseStarted> caseStartedHandler = new EventHandler<TestCaseStarted>() {
        @Override
        public void receive(TestCaseStarted event) {
            handleTestCaseStarted(event);
        }
    };

    private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted(event);
        }
    };

    private EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            handleTestStepFinished(event);
        }
    };

    private EventHandler<WriteEvent> writeEventhandler = new EventHandler<WriteEvent>() {
        @Override
        public void receive(WriteEvent event) {
            handleWrite(event);
        }
    };

    private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            finishReport();
        }
    };

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public TestNGPrettyFormatter() {
        this.out = new NiceAppendable(new PrintStream(System.out));
        this.formats = new AnsiFormats();
    }

    /**
     * Events are mapped to the appropriate handler method
     * @param publisher
     */
    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(WriteEvent.class, writeEventhandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        if (monochrome) {
            formats = new MonochromeFormats();
        } else {
            formats = new AnsiFormats();
        }
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.uri, event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        handleStartOfFeature(event);
        handleScenarioOutline(event);
        if (testSources.hasBackground(currentFeatureFile, event.testCase.getLine())) {
            printBackground(event.testCase);
            currentTestCase = event.testCase;
        } else {
            printScenarioDefinition(event.testCase);
        }

        /*dataSet is used to correctly create the folder name under target/executions when screenshot is taken*/
        /*Why here you may wonder?.... why not?*/
        try {
            String feature = testSources.getFeature(event.getTestCase().getUri()).getName();
            String scenario = event.testCase.getName();
            ThreadProperty.set("dataSet", feature + "." + scenario);
            ThreadProperty.set("feature", feature);
            ThreadProperty.set("scenario", scenario);
        } catch (Exception e) {
            ThreadProperty.set("dataSet", "");
        }
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (event.testStep instanceof PickleStepTestStep) {
            if (isFirstStepAfterBackground((PickleStepTestStep) event.testStep)) {
                printScenarioDefinition(currentTestCase);
                currentTestCase = null;
            }

            printComments((PickleStepTestStep) event.testStep, event.getTestCase());

            /*
            * This formatter prints the steps before they are executed, so the result of the step cannot be known beforehand
            * (PASSED, FAILED, etc). All steps are printed in green color regardless of the future result by default
            * */
            printStep((PickleStepTestStep) event.testStep, new Result(Result.Type.PASSED, (long) 0, null));

        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
//        if (event.testStep instanceof PickleStepTestStep) {
//            printStep((PickleStepTestStep) event.testStep, event.result);
//        }
//        printError(event.result);
    }

    private void handleWrite(WriteEvent event) {
        out.println(event.text);
    }

    private void finishReport() {
        out.close();
    }

    private void handleStartOfFeature(TestCaseStarted event) {
        if (currentFeatureFile == null || !currentFeatureFile.equals(event.testCase.getUri())) {
            if (currentFeatureFile != null) {
                out.println();
            }
            currentFeatureFile = event.testCase.getUri();
            printFeature(currentFeatureFile);
        }
    }

    private void handleScenarioOutline(TestCaseStarted event) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, event.testCase.getLine());
        if (TestSourcesModel.isScenarioOutlineScenario(astNode)) {
            ScenarioOutline scenarioOutline = (ScenarioOutline) TestSourcesModel.getScenarioDefinition(astNode);
            if (currentScenarioOutline == null || !currentScenarioOutline.equals(scenarioOutline)) {
                currentScenarioOutline = scenarioOutline;
                printScenarioOutline(currentScenarioOutline);
            }
            if (currentExamples == null || !currentExamples.equals(astNode.parent.node)) {
                currentExamples = (Examples) astNode.parent.node;
                printExamples(currentExamples);
            }
        } else {
            currentScenarioOutline = null;
            currentExamples = null;
        }
    }

    private void printScenarioOutline(ScenarioOutline scenarioOutline) {
        out.println();
        printTags(scenarioOutline.getTags(), SCENARIO_INDENT);
        out.println(SCENARIO_INDENT + getScenarioDefinitionText(scenarioOutline) + " " + getLocationText(currentFeatureFile, scenarioOutline.getLocation().getLine()));
        printDescription(scenarioOutline.getDescription());
        for (Step step : scenarioOutline.getSteps()) {
            out.println(STEP_INDENT + formats.get("skipped").text(step.getKeyword() + step.getText()));
        }
    }

    private void printExamples(Examples examples) {
        out.println();
        printTags(examples.getTags(), EXAMPLES_INDENT);
        out.println(EXAMPLES_INDENT + examples.getKeyword() + ": " + examples.getName());
        printDescription(examples.getDescription());
    }

    private void printComments(PickleStepTestStep testStep, TestCase testCase) {
        String comment = testSources.getRawLineString(testCase, testStep.getStepLine() - 2);

        try {
            if (comment.substring(0, 4).toLowerCase().matches("#log")) {
                StringBuilder formattedComment = formatComment(comment);
                out.println(STEP_INDENT + formattedComment);
            }
        } catch (Exception e) { }
    }

    private StringBuilder formatComment(String comment) {
        Format format = formats.get("output");
        StringBuilder result = new StringBuilder(format.text(comment));
        return result;
    }

    private void printStep(PickleStepTestStep testStep, Result result) {
        String keyword = getStepKeyword(testStep);
        String stepText = testStep.getStepText();
        String locationPadding = createPaddingToLocation(STEP_INDENT, keyword + stepText);
        String formattedStepText = formatStepText(keyword, stepText, formats.get(result.getStatus().lowerCaseName()), formats.get(result.getStatus().lowerCaseName() + "_arg"), testStep.getDefinitionArgument());
        out.println(STEP_INDENT + formattedStepText + locationPadding + getLocationText(testStep.getCodeLocation()));
        printStepExtraArguments(formats.get("tag"), testStep);
    }

    /**
     * This method was specifically created to print Docstrings and datatables
     *
     * Reflection had to be used since only doing {@link PickleStepTestStep#getStepArgument()} would return
     * the arguments without the necessary replacements done by {@link com.privalia.qa.aspects.ReplacementAspect}
     *
     * @param format    Format to apply
     * @param testStep  PickleStepTestStep object where to get the elements
     */
    private void printStepExtraArguments(Format format, PickleStepTestStep testStep) {

        if (testStep.getStepArgument().size() > 0) {

            try {
                Field definitionMatchField = testStep.getClass().getDeclaredField("definitionMatch");
                definitionMatchField.setAccessible(true);
                Object pickleStepDefinitionMatch = definitionMatchField.get(testStep);
                Field argumentsField = pickleStepDefinitionMatch.getClass().getSuperclass().getDeclaredField("arguments");
                argumentsField.setAccessible(true);
                List<io.cucumber.stepexpression.Argument> arguments = (List<io.cucumber.stepexpression.Argument>) argumentsField.get(pickleStepDefinitionMatch);

                for (io.cucumber.stepexpression.Argument argument: arguments) {

                    if (argument instanceof DocStringArgument) {
                        out.println(TABLES_INDENT +  format.text("\"\"\""));
                        out.println(TABLES_INDENT +  format.text(argument.getValue().toString()));
                        out.println(TABLES_INDENT +  format.text("\"\"\""));
                    }

                    if (argument instanceof DataTableArgument) {
                        String[] rows = argument.getValue().toString().split("\n");
                        for (String row : rows) {
                            out.println(TABLES_INDENT +  format.text(row.trim()));
                        }
                    }

                }

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    String formatStepText(String keyword, String stepText, Format textFormat, Format argFormat, List<Argument> arguments) {

        int beginIndex = 0;
        StringBuilder result = new StringBuilder(textFormat.text(keyword));
        for (Argument argument : arguments) {
            // can be null if the argument is missing.
            if (argument.getValue() != null) {
                int argumentOffset = argument.getStart();
                // a nested argument starts before the enclosing argument ends; ignore it when formatting
                if (argumentOffset < beginIndex) {
                    continue;
                }
                String text = stepText.substring(beginIndex, argumentOffset);
                result.append(textFormat.text(text));
            }
            // val can be null if the argument isn't there, for example @And("(it )?has something")
            if (argument.getValue() != null) {
                //String text = stepText.substring(argument.getStart(), argument.getEnd());
                result.append(argFormat.text(argument.getValue())); //Value of argument should have been correctly replaced by ReplacementAspect at this point
                // set beginIndex to end of argument
                beginIndex = argument.getEnd();
            }
        }
        if (beginIndex != stepText.length()) {
            String text = stepText.substring(beginIndex, stepText.length());
            result.append(textFormat.text(text));
        }
        return result.toString();
    }

    private String getScenarioDefinitionText(ScenarioDefinition definition) {
        return definition.getKeyword() + ": " + definition.getName();
    }

    private String getLocationText(String file, int line) {
        return getLocationText(file + ":" + line);
    }

    private String getLocationText(String location) {
        return formats.get("comment").text("# " + location);
    }

    private StringBuffer stepText(PickleStepTestStep testStep) {
        String keyword = getStepKeyword(testStep);
        return new StringBuffer(keyword + testStep.getStepText());
    }

    private String getStepKeyword(PickleStepTestStep testStep) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            Step step = (Step) astNode.node;
            return step.getKeyword();
        } else {
            return "";
        }
    }

    private boolean isFirstStepAfterBackground(PickleStepTestStep testStep) {
        return currentTestCase != null && !isBackgroundStep(testStep);
    }

    private boolean isBackgroundStep(PickleStepTestStep testStep) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            return TestSourcesModel.isBackgroundStep(astNode);
        }
        return false;
    }

    private void printFeature(String path) {
        Feature feature = testSources.getFeature(path);
        printTags(feature.getTags());
        out.println(feature.getKeyword() + ": " + feature.getName());
        printDescription(feature.getDescription());
    }

    private void printTags(List<Tag> tags) {
        printTags(tags, "");
    }

    private void printTags(List<Tag> tags, String indent) {
        if (!tags.isEmpty()) {
            Format format = formats.get("pending_arg");
            StringBuilder result = new StringBuilder(format.text(FixJava.join(FixJava.map(tags, tagNameMapper), " ")));
            out.println(indent + result);
        }
    }

    private void printPickleTags(List<PickleTag> tags, String indent) {
        if (!tags.isEmpty()) {
            Format format = formats.get("pending_arg");
            StringBuilder result = new StringBuilder(format.text(FixJava.join(FixJava.map(tags, pickleTagNameMapper), " ")));
            out.println(indent + result);
        }
    }

    private void printDescription(String description) {
        if (description != null) {
            out.println(description);
        }
    }

    private void printBackground(TestCase testCase) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
        if (astNode != null) {
            Background background = TestSourcesModel.getBackgroundForTestCase(astNode);
            String backgroundText = getScenarioDefinitionText(background);
            boolean useBackgroundSteps = true;
            calculateLocationIndentation(SCENARIO_INDENT + backgroundText, testCase.getTestSteps(), useBackgroundSteps);
            String locationPadding = createPaddingToLocation(SCENARIO_INDENT, backgroundText);
            out.println();
            out.println(SCENARIO_INDENT + backgroundText + locationPadding + getLocationText(currentFeatureFile, background.getLocation().getLine()));
            printDescription(background.getDescription());
        }
    }

    private void printScenarioDefinition(TestCase testCase) {
        ScenarioDefinition scenarioDefinition = testSources.getScenarioDefinition(currentFeatureFile, testCase.getLine());
        String definitionText = scenarioDefinition.getKeyword() + ": " + testCase.getName();
        calculateLocationIndentation(SCENARIO_INDENT + definitionText, testCase.getTestSteps());
        String locationPadding = createPaddingToLocation(SCENARIO_INDENT, definitionText);
        out.println();
        printPickleTags(testCase.getTags(), SCENARIO_INDENT);
        out.println(SCENARIO_INDENT + definitionText + locationPadding + getLocationText(currentFeatureFile, testCase.getLine()));
        printDescription(scenarioDefinition.getDescription());
    }

    private void printError(Result result) {
        if (result.getError() != null) {
            out.println("      " + formats.get(result.getStatus().lowerCaseName()).text(result.getErrorMessage()));
        }
    }

    private void calculateLocationIndentation(String definitionText, List<TestStep> testSteps) {
        boolean useBackgroundSteps = false;
        calculateLocationIndentation(definitionText, testSteps, useBackgroundSteps);
    }

    private void calculateLocationIndentation(String definitionText, List<TestStep> testSteps, boolean useBackgroundSteps) {
        int maxTextLength = definitionText.length();
        for (TestStep step : testSteps) {
            if (step instanceof PickleStepTestStep) {
                PickleStepTestStep testStep = (PickleStepTestStep) step;
                if (isBackgroundStep(testStep) == useBackgroundSteps) {
                    StringBuffer stepText = stepText(testStep);
                    maxTextLength = Math.max(maxTextLength, STEP_INDENT.length() + stepText.length());
                }
            }
        }
        locationIndentation = maxTextLength + 1;
    }

    private String createPaddingToLocation(String indent, String text) {
        StringBuffer padding = new StringBuffer();
        for (int i = indent.length() + text.length(); i < locationIndentation; ++i) {
            padding.append(' ');
        }
        return padding.toString();
    }
}