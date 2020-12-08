package com.privalia.qa.cucumber.reporter;

import com.privalia.qa.utils.ThreadProperty;
import io.cucumber.core.exception.CucumberException;

import io.cucumber.core.stepexpression.ExpressionArgument;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static com.privalia.qa.cucumber.reporter.Formats.ansi;

import static com.privalia.qa.cucumber.reporter.Formats.monochrome;

import static io.cucumber.core.exception.ExceptionUtils.printStackTrace;

import static java.lang.Math.max;

import static java.util.Locale.ROOT;

/**
 * This class implements a set of eventListeners for the tests suite. This methods are fired before/after
 * specific events during tests execution. Since the library uses testng and not junit to run the test
 * {@link ConcurrentEventListener} should be used instead of {@link EventListener}
 * <p>
 * This class is responsible for printing the steps/features/scenarios to console. This is a custom
 * implementation of the cucumber PrettyFormatter that allow more flexibility with colors and other
 * formatting options. Error messages generated from other classes are printed using the standard log4j.
 * <p>
 * There are several reason to implement a custom formatter in Gingerspec:
 * * Unlike regular PrettyFormatter, this formatter prints steps BEFORE
 * they are executed, giving real-time feedback to the user about the test progress
 * * Unlike PrettyFormatter, this formatter also prints things like Docstrings and datatables
 * * Instead of printing the whole stacktrace of the error when an assertion fails, only the main message
 * of the assertion is printed, which makes tests easier to read
 * * Last but not least, this formatter also makes variable replacement, so it will correctly
 * print Gingerspec custon variables (!{VAR}, #{VAR}, ${VAR} and @{VAR})
 *
 * @author Jose Fernandez
 */
public class TestNGPrettyFormatter implements ConcurrentEventListener, ColorAware {

    private static final String SCENARIO_INDENT = "";

    private static final String STEP_INDENT = "  ";

    private static final String STEP_SCENARIO_INDENT = "    ";

    private static final String LOCATION_INDENT = "         ";

    private final Map<UUID, Integer> commentStartIndex = new HashMap<>();

    private final NiceAppendable out;

    private Formats formats = ansi();

    private String currentFeatureFile;

    private final TestSourcesModel testSources = new TestSourcesModel();

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    public TestNGPrettyFormatter(OutputStream out) {
        this.out = new NiceAppendable(new UTF8OutputStreamWriter(out));
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(WriteEvent.class, this::handleWrite);
        publisher.registerHandlerFor(EmbedEvent.class, this::handleEmbed);
        publisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.getUri(), event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        handleStartOfFeature(event);
        out.println();
        preCalculateLocationIndent(event);
        printTags(event);
        printScenarioDefinition(event);

        /*dataSet is used to correctly create the folder name under target/executions when screenshot is taken*/
        /*Why here you may wonder?.... why not?*/
        try {
            String feature = testSources.getFeature(event.getTestCase().getUri()).getName();
            String scenario = event.getTestCase().getName();
            ThreadProperty.set("dataSet", feature + "." + scenario);
            ThreadProperty.set("feature", feature);
            ThreadProperty.set("scenario", scenario);
        } catch (Exception e) {
            ThreadProperty.set("dataSet", "");
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        printComments(event);
        printStep(event);
        printError(event);
    }

    private void handleWrite(WriteEvent event) {
        out.println();
        printText(event);
        out.println();

    }

    private void handleEmbed(EmbedEvent event) {
        out.println();
        printEmbedding(event);
        out.println();

    }

    private void handleTestRunFinished(TestRunFinished event) {
        out.close();
    }

    private void handleStartOfFeature(TestCaseStarted event) {
        if (currentFeatureFile == null || !currentFeatureFile.equals(event.getTestCase().getUri().toString())) {
            if (currentFeatureFile != null) {
                out.println();
            }
            currentFeatureFile = event.getTestCase().getUri().toString();
            printFeature(currentFeatureFile);
        }
    }

    /**
     * Prints the beginning of the feature file, from the Feature tags to the
     * description
     * @param path      Path of the feature file
     */
    private void printFeature(String path) {
        Messages.GherkinDocument.Feature feature = null;
        try {
            feature = testSources.getFeature(new URI(path));
        } catch (URISyntaxException e) {
            logger.error("Error getting Feature document from " + path + ". " + e.getMessage());
        }

        List<Messages.GherkinDocument.Feature.Tag> tags = feature.getTagsList();
        String tagsList = "";
        for (Messages.GherkinDocument.Feature.Tag tag: tags) {
            tagsList = tag.getName() + " ";
        }
        Format format = formats.get("pending_arg");
        out.println(tagsList);

        out.println(feature.getKeyword() + ": " + feature.getName());
        if (feature.getDescription() != null) {
            out.println(feature.getDescription());
        }
    }

    private void printComments(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            String comment = testSources.getRawLineString(testStep.getUri(), testStep.getStepLine() - 2);

            StringBuilder formattedComment;
            try {
                if (comment.substring(0, 4).toLowerCase().matches("#log")) {
                    formattedComment = new StringBuilder(formats.get("output").text(comment));
                    out.println(STEP_INDENT + formattedComment);
                }
            } catch (Exception e) {
                formattedComment = new StringBuilder(formats.get("failed").text(e.getMessage()));
                out.println(STEP_INDENT + formattedComment);
            }
        }
    }

    private void preCalculateLocationIndent(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        Integer longestStep = testCase.getTestSteps().stream()
                .filter(PickleStepTestStep.class::isInstance)
                .map(PickleStepTestStep.class::cast)
                .map(PickleStepTestStep::getStep)
                .map(step -> formatPlainStep(step.getKeyword(), step.getText()).length())
                .max(Comparator.naturalOrder())
                .orElse(0);

        int scenarioLength = formatScenarioDefinition(testCase).length();
        commentStartIndex.put(testCase.getId(), max(longestStep, scenarioLength) + 1);
    }

    private void printTags(TestCaseStarted event) {
        List<String> tags = event.getTestCase().getTags();
        if (!tags.isEmpty()) {
            out.println(TestNGPrettyFormatter.SCENARIO_INDENT + String.join(" ", tags));
        }
    }

    private void printScenarioDefinition(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        String definitionText = formatScenarioDefinition(testCase);
        String path = relativize(testCase.getUri()).getSchemeSpecificPart();
        String locationIndent = calculateLocationIndent(event.getTestCase(), SCENARIO_INDENT + definitionText);
        out.println(SCENARIO_INDENT + definitionText + locationIndent
                + formatLocation(path + ":" + testCase.getLocation().getLine()));
    }

    private void printStep(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            String keyword = testStep.getStep().getKeyword();
            String stepText = testStep.getStep().getText();
            String status = event.getResult().getStatus().name().toLowerCase(ROOT);
            String formattedStepText = formatStepText(keyword, stepText, formats.get(status),
                    formats.get(status + "_arg"), testStep.getDefinitionArgument());
            String locationIndent = calculateLocationIndent(event.getTestCase(), formatPlainStep(keyword, stepText));

            if (System.getProperty("SHOW_STACK_INFO") != null) {
                out.println(STEP_INDENT + formattedStepText + locationIndent);
                this.printStepStackInformation(event.getTestStep());
            } else {
                out.println(STEP_INDENT + formattedStepText + locationIndent);
            }
        }
    }

    /**
     * Shows information about the underlying test step definition function, its location and the arguments used
     * @param testStep  PickleStepTestStep instance
     */
    private void printStepStackInformation(TestStep testStep) {

        out.println(LOCATION_INDENT +  getLocationText(testStep.getCodeLocation()));

        int argumentIndex = 0;

        try {

            for (io.cucumber.core.stepexpression.Argument argument: this.getArguments(testStep)) {

                if (argument instanceof ExpressionArgument) {

                    io.cucumber.cucumberexpressions.Group group = (io.cucumber.cucumberexpressions.Group) ((ExpressionArgument) argument).getGroup();

                    if (group.getChildren().size() > 0) {
                        out.println(LOCATION_INDENT +  getLocationText("Argument " + argumentIndex + ": " + group.getChildren().get(0).getValue()));
                    } else {
                        out.println(LOCATION_INDENT +  getLocationText("Argument " + argumentIndex + ": " + group.getValue()));
                    }
                }

                if (argument instanceof DocStringArgument) {
                    out.println(LOCATION_INDENT +  getLocationText("Argument " + argumentIndex + ": " + argument.getValue().toString()));
                }

                if (argument instanceof DataTableArgument) {
                    Field argumentField = argument.getClass().getDeclaredField("argument");
                    argumentField.setAccessible(true);
                    List<List<String>> finalList = (List<List<String>>) argumentField.get(argument);

                    out.println(LOCATION_INDENT +  getLocationText("Argument " + argumentIndex + ": " + Arrays.toString(finalList.toArray())));
                }

                argumentIndex += 1;
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private String getLocationText(String location) {
        return formats.get("comment").text("# " + location);
    }

    /**
     * Returns the list of arguments for the given step
     * @param testStep                  PickleStepTestStep instance
     * @return                          list of Arguments
     * @throws NoSuchFieldException     NoSuchFieldException
     * @throws IllegalAccessException   IllegalAccessException
     */
    private List<io.cucumber.core.stepexpression.Argument> getArguments(TestStep testStep) throws NoSuchFieldException, IllegalAccessException {
        Field definitionMatchField = testStep.getClass().getDeclaredField("definitionMatch");
        definitionMatchField.setAccessible(true);
        Object pickleStepDefinitionMatch = definitionMatchField.get(testStep);
        Field argumentsField = pickleStepDefinitionMatch.getClass().getSuperclass().getDeclaredField("arguments");
        argumentsField.setAccessible(true);
        List<io.cucumber.core.stepexpression.Argument> arguments = (List<io.cucumber.core.stepexpression.Argument>) argumentsField.get(pickleStepDefinitionMatch);
        return arguments;
    }

    private void printError(TestStepFinished event) {
        Result result = event.getResult();
        Throwable error = result.getError();
        if (error != null) {
            try {
                String name = result.getStatus().name().toLowerCase(ROOT);
                out.println(STEP_INDENT + formats.get(name).text(event.getResult().getError().getMessage().replace("\n", " ")));
            } catch (Exception e) {
                out.println(STEP_INDENT + formats.get("failed").text("Unexpected exception retrieving error message: " + e.getMessage() + ". Check the stacktrace for more details"));
            }
        }
    }

    private void printText(WriteEvent event) {
        try (BufferedReader lines = new BufferedReader(new StringReader(event.getText()))) {
            String line;
            while ((line = lines.readLine()) != null) {
                out.println(STEP_SCENARIO_INDENT + line);
            }
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    private void printEmbedding(EmbedEvent event) {
        String line = "Embedding " + event.getName() + " [" + event.getMediaType() + " " + event.getData().length
                + " bytes]";
        out.println(STEP_SCENARIO_INDENT + line);
    }

    private String formatPlainStep(String keyword, String stepText) {
        return STEP_INDENT + keyword + stepText;
    }

    private String formatScenarioDefinition(TestCase testCase) {
        return testCase.getKeyword() + ": " + testCase.getName();
    }

    static URI relativize(URI uri) {
        if (!"file".equals(uri.getScheme())) {
            return uri;
        }
        if (!uri.isAbsolute()) {
            return uri;
        }

        try {
            URI root = new File("").toURI();
            URI relative = root.relativize(uri);
            // Scheme is lost by relativize
            return new URI("file", relative.getSchemeSpecificPart(), relative.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private String calculateLocationIndent(TestCase testStep, String prefix) {
        Integer commentStartAt = commentStartIndex.getOrDefault(testStep.getId(), 0);
        int padding = commentStartAt - prefix.length();

        if (padding < 0) {
            return " ";
        }

        StringBuilder builder = new StringBuilder(padding);
        for (int i = 0; i < padding; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    private String formatLocation(String location) {
        return formats.get("comment").text("# " + location);
    }

    String formatStepText(
            String keyword, String stepText, Format textFormat, Format argFormat, List<Argument> arguments
    ) {
        int beginIndex = 0;
        StringBuilder result = new StringBuilder(textFormat.text(keyword));
        for (Argument argument : arguments) {
            // can be null if the argument is missing.
            if (argument.getValue() != null) {
                int argumentOffset = argument.getStart();
                // a nested argument starts before the enclosing argument ends;
                // ignore it when formatting
                if (argumentOffset < beginIndex) {
                    continue;
                }
                String text = stepText.substring(beginIndex, argumentOffset);
                result.append(textFormat.text(text));
            }
            // val can be null if the argument isn't there, for example
            // @And("(it )?has something")
            if (argument.getValue() != null) {
                String text = stepText.substring(argument.getStart(), argument.getEnd());
                result.append(argFormat.text(text));
                // set beginIndex to end of argument
                beginIndex = argument.getEnd();
            }
        }
        if (beginIndex != stepText.length()) {
            String text = stepText.substring(beginIndex);
            result.append(textFormat.text(text));
        }
        return result.toString();
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        formats = monochrome ? monochrome() : Formats.ansi();
    }
}
