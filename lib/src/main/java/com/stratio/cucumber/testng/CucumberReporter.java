package com.stratio.cucumber.testng;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.stratio.tests.utils.ThreadProperty;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;
import cucumber.runtime.io.URLOutputStream;
import cucumber.runtime.io.UTF8OutputStreamWriter;

public class CucumberReporter implements Formatter, Reporter {
    public static final int DURATION_STRING = 1000000;
    public static final int DEFAULT_LENGTH = 11;
    public static final int DEFAULT_MAX_LENGTH = 140;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final Writer writer, writerJunit;
    private final Document document, jUnitDocument;
    private final Element results, jUnitResults;
    private final Element suite, jUnitSuite;
    private final Element test, jUnitTest;
    private Element clazz;
    private Element root, jUnitRoot;
    private TestMethod testMethod;
    private Examples tmpExamples;
    private List<Result> tmpHooks = new ArrayList<Result>();
    private List<Step> tmpSteps = new ArrayList<Step>();
    private List<Step> tmpStepsBG = new ArrayList<Step>();
    private Integer iteration = 0;
    private Integer position = 0;
    private String callerClass;
    private Background background;
    private static final String STATUS = "status";
    long time_start, time_end;
    String featureName;

    /**
     * Constructor of cucumberReporter.
     * 
     * @param url
     * @param cClass
     * @throws IOException
     */
    public CucumberReporter(String url, String cClass, String additional) throws IOException {
        this.writer = new UTF8OutputStreamWriter(new URLOutputStream(Utils.toURL(url + cClass + additional
                + "TESTNG.xml")));
        this.writerJunit = new UTF8OutputStreamWriter(new URLOutputStream(Utils.toURL(url + cClass + additional
                + "JUNIT.xml")));
        TestMethod.treatSkippedAsFailure = false;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            results = document.createElement("testng-results");
            suite = document.createElement("suite");
            test = document.createElement("test");
            callerClass = cClass;
            suite.appendChild(test);
            results.appendChild(suite);
            document.appendChild(results);
            jUnitDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            jUnitResults = jUnitDocument.createElement("testsuites");
            jUnitSuite = jUnitDocument.createElement("testsuite");
            jUnitTest = jUnitDocument.createElement("testcase");
            jUnitDocument.appendChild(jUnitResults);
            jUnitResults.appendChild(jUnitSuite);
        } catch (ParserConfigurationException e) {
            throw new CucumberException("Error initializing DocumentBuilder.", e);
        }
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    @Override
    public void uri(String uri) {
    }

    @Override
    public void feature(Feature feature) {
        featureName = feature.getName();
        clazz = document.createElement("class");
        clazz.setAttribute("name", callerClass);
        test.appendChild(clazz);
    }

    @Override
    public void scenario(Scenario scenario) {
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        iteration = 1;
    }

    @Override
    public void examples(Examples examples) {
        tmpExamples = examples;
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        root = document.createElement("test-method");
        jUnitRoot = jUnitDocument.createElement("testcase");
        jUnitSuite.appendChild(jUnitRoot);
        clazz.appendChild(root);
        testMethod = new TestMethod(scenario);
        testMethod.hooks = tmpHooks;
        tmpStepsBG.clear();
        if (tmpExamples == null) {
            testMethod.steps = tmpSteps;
            testMethod.stepsbg = null;
        } else {
            testMethod.steps = new ArrayList<Step>();
            testMethod.stepsbg = tmpStepsBG;
        }
        testMethod.examplesData = tmpExamples;
        testMethod.start(root, iteration, jUnitRoot);
        iteration++;
    }

    @Override
    public void background(Background background) {
        this.background = background;
    }

    @Override
    public void step(Step step) {
        boolean bgstep = false;
        if (background != null && (background.getLineRange().getLast() <= step.getLine())
                && (step.getLine() >= background.getLineRange().getFirst())) {
            tmpStepsBG.add(step);
            bgstep = true;
        }

        if (step.getClass().toString().contains("ExampleStep") && !bgstep) {
            testMethod.steps.add(step);
        } else {
            tmpSteps.add(step);
        }
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        Boolean ignored = false;
        for (Tag tag : scenario.getTags()) {
            if ("@ignore".equals(tag.getName())) {
                ignored = true;
            }
        }

        testMethod.finish(document, root, this.position, ignored, jUnitDocument, jUnitRoot);
        this.position++;
        if ((tmpExamples != null) && (iteration >= tmpExamples.getRows().size())) {
            tmpExamples = null;
        }
        tmpHooks.clear();
        tmpSteps.clear();
        tmpStepsBG.clear();
        testMethod = null;
        jUnitRoot.setAttribute("classname", callerClass);
    }

    @Override
    public void eof() {
    }

    @Override
    public void done() {
        try {
            results.setAttribute("total", String.valueOf(getElementsCountByAttribute(suite, STATUS, ".*")));
            results.setAttribute("passed", String.valueOf(getElementsCountByAttribute(suite, STATUS, "PASS")));
            results.setAttribute("failed", String.valueOf(getElementsCountByAttribute(suite, STATUS, "FAIL")));
            results.setAttribute("skipped", String.valueOf(getElementsCountByAttribute(suite, STATUS, "SKIP")));
            suite.setAttribute("name", CucumberReporter.class.getName());
            suite.setAttribute("duration-ms",
                    String.valueOf(getTotalDuration(suite.getElementsByTagName("test-method"))));
            test.setAttribute("name", CucumberReporter.class.getName());
            test.setAttribute("duration-ms",
                    String.valueOf(getTotalDuration(suite.getElementsByTagName("test-method"))));

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult streamResult = new StreamResult(writer);
            DOMSource domSource = new DOMSource(document);
            transformer.transform(domSource, streamResult);
            jUnitSuite.setAttribute("name", callerClass + "." + featureName);
            jUnitSuite.setAttribute("tests", String.valueOf(getElementsCountByAttribute(suite, STATUS, ".*")));
            jUnitSuite.setAttribute("failures", String.valueOf(getElementsCountByAttribute(suite, STATUS, "FAIL")));
            jUnitSuite.setAttribute("skipped", String.valueOf(getElementsCountByAttribute(suite, STATUS, "SKIP")));
            jUnitSuite.setAttribute("timestamp", new java.util.Date().toString());
            jUnitSuite.setAttribute("time",
                    String.valueOf(getTotalDurationMs(suite.getElementsByTagName("test-method"))));
            Transformer transformerJunit = TransformerFactory.newInstance().newTransformer();
            transformerJunit.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult streamResultJunit = new StreamResult(writerJunit);
            DOMSource domSourceJunit = new DOMSource(jUnitDocument);
            transformerJunit.transform(domSourceJunit, streamResultJunit);

        } catch (TransformerException e) {
            throw new CucumberException("Error transforming report.", e);
        }
    }

    @Override
    public void close() {
    }

    // Reporter methods
    @Override
    public void before(Match match, Result result) {
        tmpHooks.add(result);
    }

    @Override
    public void match(Match match) {
    }

    @Override
    public void result(Result result) {
        testMethod.results.add(result);
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
    }

    @Override
    public void write(String text) {
    }

    @Override
    public void after(Match match, Result result) {
        testMethod.hooks.add(result);
    }

    private int getElementsCountByAttribute(Node node, String attributeName, String attributeValue) {
        int count = 0;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            count += getElementsCountByAttribute(node.getChildNodes().item(i), attributeName, attributeValue);
        }

        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Node namedItem = attributes.getNamedItem(attributeName);
            if (namedItem != null && namedItem.getNodeValue().matches(attributeValue)) {
                count++;
            }
        }
        return count;
    }

    private double getTotalDuration(NodeList testCaseNodes) {
        double totalDuration = 0;
        for (int i = 0; i < testCaseNodes.getLength(); i++) {
            try {
                String duration = "0";
                Node durationms = testCaseNodes.item(i).getAttributes().getNamedItem("duration-ms");
                if (durationms != null) {
                    duration = durationms.getNodeValue();
                }
                totalDuration += Double.parseDouble(duration);
            } catch (NumberFormatException e) {
                throw new CucumberException(e);
            }
        }
        return totalDuration;
    }

    private double getTotalDurationMs(NodeList testCaseNodes) {
        return getTotalDuration(testCaseNodes) / 1000;
    }

    private static final class TestMethod {

        private Scenario scenario = null;
        private Examples examplesData;
        private static boolean treatSkippedAsFailure = false;
        private List<Step> steps;
        private List<Step> stepsbg;
        private final List<Result> results = new ArrayList<Result>();
        private List<Result> hooks;
        private Integer iteration = 1;

        private TestMethod(Scenario scenario) {
            this.scenario = scenario;
        }

        private void start(Element element, Integer iteration, Element JunitElement) {
            this.iteration = iteration;
            if ((examplesData == null) || (this.iteration >= examplesData.getRows().size())) {
                element.setAttribute("name", scenario.getName());
                JunitElement.setAttribute("name", scenario.getName());
                ThreadProperty.set("dataSet", "");
            } else {
                String data = examplesData.getRows().get(iteration).getCells().toString();
                data = data.replaceAll("\"", "¨");
                element.setAttribute("name", scenario.getName() + " " + data);
                JunitElement.setAttribute("name", scenario.getName() + " " + data);
                ThreadProperty.set("dataSet", data);
            }
            element.setAttribute("started-at", DATE_FORMAT.format(new Date()));
        }

        /**
         * Finish.
         * 
         * @param doc
         * @param element
         * @param position
         * @param ignored
         */
        public void finish(Document doc, Element element, Integer position, Boolean ignored, Document docJunit,
                Element Junit) {

            Junit.setAttribute("time", String.valueOf(calculateTotalDurationString() / 1000));

            element.setAttribute("duration-ms", String.valueOf(calculateTotalDurationString()));
            element.setAttribute("finished-at", DATE_FORMAT.format(new Date()));

            StringBuilder stringBuilder = new StringBuilder();

            addStepAndResultListing(stringBuilder);
            Result skipped = null;
            Result failed = null;
            if (ignored) {
                element.setAttribute(STATUS, "SKIP");
                Element exception = createException(doc, "SkippedDueTagException",
                        "This scenario was skipped due the use of the @ignore tag", " ");
                element.appendChild(exception);
                Element skippedElementJunit = docJunit.createElement("skipped");
                Junit.appendChild(skippedElementJunit);
                Element systemOut = systemOutPrintJunit(docJunit,
                        "This scenario was skipped due the use of the @ignore tag");
                Junit.appendChild(systemOut);
            } else {
                for (Result result : results) {
                    if ("failed".equals(result.getStatus())) {
                        failed = result;
                    } else if ("undefined".equals(result.getStatus()) || "pending".equals(result.getStatus())) {
                        skipped = result;
                    }
                }
                for (Result result : hooks) {
                    if (failed == null && "failed".equals(result.getStatus())) {
                        failed = result;
                    }
                }
                if (failed != null) {
                    element.setAttribute(STATUS, "FAIL");
                    StringWriter stringWriter = new StringWriter();
                    failed.getError().printStackTrace(new PrintWriter(stringWriter));
                    Element exception = createException(doc, failed.getError().getClass().getName(),
                            stringBuilder.toString(), stringWriter.toString());
                    element.appendChild(exception);
                    Element exceptionJunit = createExceptionJunit(docJunit, failed.getError().getClass().getName(),
                            stringBuilder.toString(), stringWriter.toString());
                    Junit.appendChild(exceptionJunit);
                } else if (skipped != null) {
                    if (treatSkippedAsFailure) {
                        element.setAttribute(STATUS, "FAIL");
                        Element exception = createException(doc, "The scenario has pending or undefined step(s)",
                                stringBuilder.toString(), "The scenario has pending or undefined step(s)");
                        element.appendChild(exception);
                        Element exceptionJunit = createExceptionJunit(docJunit,
                                "The scenario has pending or undefined step(s)", stringBuilder.toString(),
                                "The scenario has pending or undefined step(s)");
                        Junit.appendChild(exceptionJunit);
                    } else {
                        element.setAttribute(STATUS, "SKIP");
                        Element skippedElementJunit = docJunit.createElement("skipped");
                        Junit.appendChild(skippedElementJunit);
                        Element systemOut = systemOutPrintJunit(docJunit, stringBuilder.toString());
                        Junit.appendChild(systemOut);
                    }

                } else {
                    element.setAttribute(STATUS, "PASS");
                    Element exception = createException(doc, "NonRealException", stringBuilder.toString(), " ");
                    element.appendChild(exception);
                    Element systemOut = systemOutPrintJunit(docJunit, stringBuilder.toString());
                    Junit.appendChild(systemOut);
                }
            }
        }

        private double calculateTotalDurationString() {
            double totalDurationNanos = 0;
            for (Result r : results) {
                totalDurationNanos += r.getDuration() == null ? 0 : r.getDuration();
            }
            for (Result r : hooks) {
                totalDurationNanos += r.getDuration() == null ? 0 : r.getDuration();
            }
            return totalDurationNanos / DURATION_STRING;
        }

        private void addStepAndResultListing(StringBuilder sb) {

            if (stepsbg != null) {
                List<Step> mergedsteps = new ArrayList<Step>();
                mergedsteps.addAll(stepsbg);
                mergedsteps.addAll(steps);
                steps.clear();
                steps.addAll(mergedsteps);
            }

            for (int i = 0; i < steps.size(); i++) {
                String resultStatus = "not executed";
                String resultStatusWarn = "*";
                if (i < results.size()) {
                    resultStatus = results.get(i).getStatus();
                    resultStatusWarn = ((results.get(i).getError() != null) && (results.get(i).getStatus()
                            .equals("passed"))) ? "(W)" : "";
                }
                sb.append(steps.get(i).getKeyword());
                sb.append(steps.get(i).getName());
                int len = 0;
                len = steps.get(i).getKeyword().length() + steps.get(i).getName().length();
                if (steps.get(i).getRows() != null) {
                    for (DataTableRow row : steps.get(i).getRows()) {
                        StringBuilder strrowBuilder = new StringBuilder();
                        strrowBuilder.append("| ");
                        for (String cell : row.getCells()) {
                            strrowBuilder.append(cell).append(" | ");
                        }
                        String strrow = strrowBuilder.toString();
                        len = strrow.length() + DEFAULT_LENGTH;
                        sb.append("\n           ");
                        sb.append(strrow);
                    }
                }
                for (int j = 0; j + len < DEFAULT_MAX_LENGTH; j++) {
                    sb.append(".");
                }
                sb.append(resultStatus);
                sb.append(resultStatusWarn);
                sb.append("\n");
            }
        }

        private Element createException(Document doc, String clazz, String message, String stacktrace) {
            Element exceptionElement = doc.createElement("exception");
            exceptionElement.setAttribute("class", clazz);

            if (message != null) {
                Element messageElement = doc.createElement("message");
                messageElement.appendChild(doc.createCDATASection("\r\n<pre>\r\n" + message + "\r\n</pre>\r\n"));
                exceptionElement.appendChild(messageElement);
            }

            Element stacktraceElement = doc.createElement("full-stacktrace");
            stacktraceElement.appendChild(doc.createCDATASection(stacktrace));
            exceptionElement.appendChild(stacktraceElement);

            return exceptionElement;
        }

        private Element createExceptionJunit(Document doc, String clazz, String message, String stacktrace) {
            Element exceptionElement = doc.createElement("failure");
            if (message != null) {
                exceptionElement.setAttribute("message", "\r\n" + message + "\r\n");
            }
            exceptionElement.appendChild(doc.createCDATASection(stacktrace));
            return exceptionElement;
        }

        private Element systemOutPrintJunit(Document doc, String message) {
            Element systemOut = doc.createElement("system-out");
            systemOut.appendChild(doc.createCDATASection("\r\n" + message + "\r\n"));
            return systemOut;
        }
    }
}