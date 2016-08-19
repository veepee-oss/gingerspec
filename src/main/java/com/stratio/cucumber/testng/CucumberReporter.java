package com.stratio.cucumber.testng;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.ning.http.client.*;
import com.ning.http.client.cookie.Cookie;
import com.stratio.specs.CommonG;
import com.stratio.specs.GivenGSpec;
import com.stratio.tests.utils.HttpResponse;
import com.stratio.tests.utils.ThreadProperty;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;
import cucumber.runtime.io.URLOutputStream;
import cucumber.runtime.io.UTF8OutputStreamWriter;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.hjson.JsonValue;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.stratio.specs.*;

import org.apache.commons.codec.binary.Base64;

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
        testMethod = new TestMethod(featureName, scenario);
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

        List<Tag> tags = scenario.getTags();

        try {
            testMethod.finish(document, root, this.position, tags, jUnitDocument, jUnitRoot);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        private String featureName;
        private Examples examplesData;
        private static boolean treatSkippedAsFailure = false;
        private List<Step> steps;
        private List<Step> stepsbg;
        private final List<Result> results = new ArrayList<Result>();
        private List<Result> hooks;
        private Integer iteration = 1;

        private TestMethod(String feature, Scenario scenario) {
            this.featureName = feature;
            this.scenario = scenario;
        }

        private void start(Element element, Integer iteration, Element JunitElement) {
            this.iteration = iteration;
            String testSuffix = System.getProperty("TESTSUFFIX");
            String name = scenario.getName();
            if (testSuffix != null) {
                name = name + " [" +testSuffix + "]";
            }
            if ((examplesData == null) || (this.iteration >= examplesData.getRows().size())) {
                element.setAttribute("name", name);
                JunitElement.setAttribute("name", name);
                ThreadProperty.set("dataSet", "");
            } else {
                String data = obtainOutlineScenariosExamples(examplesData.getRows().get(iteration).getCells().toString());
                element.setAttribute("name", name + " " + data);
                JunitElement.setAttribute("name", name + " " + data);
                ThreadProperty.set("dataSet", data);
            }
            element.setAttribute("started-at", DATE_FORMAT.format(new Date()));
        }

        public String obtainOutlineScenariosExamples(String examplesData) {
            String data = examplesData.replaceAll("\"", "¨");
            return data;
        }

        /**
         * Finish.
         *
         * @param doc
         * @param element
         * @param position
         * @param tags
         */
        public void finish(Document doc, Element element, Integer position, List<Tag> tags, Document docJunit,
                Element Junit) throws ExecutionException, InterruptedException, IOException {

            Junit.setAttribute("time", String.valueOf(calculateTotalDurationString() / 1000));

            element.setAttribute("duration-ms", String.valueOf(calculateTotalDurationString()));
            element.setAttribute("finished-at", DATE_FORMAT.format(new Date()));

            StringBuilder stringBuilder = new StringBuilder();

            List<Step> mergedsteps = new ArrayList<Step>();
            if (stepsbg != null) {
                mergedsteps.addAll(stepsbg);
                mergedsteps.addAll(steps);
            } else {
                mergedsteps.addAll(steps);
            }
            addStepAndResultListing(stringBuilder, mergedsteps);
            Result skipped = null;
            Result failed = null;
            Boolean ignored = false;
            Boolean ignoreReason = false;
            String exceptionmsg = "Failed";

            AsyncHttpClient client = new AsyncHttpClient();
            Future<Response> response = null;
            Boolean isJiraTicketDone = false;
            Boolean isWrongTicket = false;
            CommonG comm = new CommonG();
            String userJira = System.getProperty("usernamejira");
            String passJira = System.getProperty("passwordjira");

            Logger logger = LoggerFactory.getLogger(ThreadProperty.get("class"));
            String value = "";
            String issue = "";

            for (Tag tag : tags) {
                if ("@ignore".equals(tag.getName())) {
                    ignored = true;
                    for (Tag tagNs : tags) {
                        if (!(tagNs.getName().equals("@ignore"))) {
                            String tillFix = tagNs.getName();
                            if (tillFix.startsWith("@tillfixed")) {
                                Pattern pattern = Pattern.compile("@(.*?)\\((.*?)\\)");
                                Matcher matcher = pattern.matcher(tillFix);
                                if (matcher.find()) {
                                    issue = matcher.group(2);
                                } else {
                                    isWrongTicket = true;
                                }
                                if (((userJira != null) || (passJira != null)) && (issue != "")) {
                                    comm.setRestProtocol("https://");
                                    comm.setRestHost("stratio.atlassian.net");
                                    comm.setRestPort("");
                                    comm.setClient(client);
                                    String endpoint = "/rest/api/2/issue/" + issue;
                                    try {
                                        response = comm.generateRequest("GET", true, userJira, passJira, endpoint, "", "json");
                                        comm.setResponse(endpoint, response.get());
                                    } catch (Exception e) {
                                        logger.error("Rest API Jira connection error " + String.valueOf(comm.getResponse().getStatusCode()));
                                    }

                                    String json = comm.getResponse().getResponse();
                                    try {
                                        value = JsonPath.read(json, "$.fields.status.name");
                                    } catch (PathNotFoundException pe) {
                                        logger.error("Json Path $.fields.status.name not found\r");
                                        logger.error(json);
                                    }

                                    if (value.equals("")) {
                                        isWrongTicket = true;
                                    } else if ("done".equals(value.toLowerCase()) || "finalizado".equals(value.toLowerCase())) {
                                        isJiraTicketDone = true;
                                    }

                                }
                                exceptionmsg = "This scenario was skipped because of https://stratio.atlassian.net/browse/" + issue;
                                ignoreReason = true;
                                break;
                            }

                            //@unimplemented
                            if (tagNs.getName().matches("@unimplemented")) {
                                exceptionmsg = "This scenario was skipped because of it is not yet implemented";
                                ignoreReason = true;
                                break;
                            }

                            //@manual
                            if (tagNs.getName().matches("@manual")) {
                                ignoreReason = true;
                                exceptionmsg = "This scenario was skipped because it is marked as manual.";
                                break;
                            }

                            //@toocomplex
                            if (tagNs.getName().matches("@toocomplex")) {
                                exceptionmsg = "This scenario was skipped because of being too complex to test";
                                ignoreReason = true;
                                break;
                            }
                        }
                    }

                }
            }

            String msg1 = null;
            String msg2 = null;

            if (ignored && (!ignoreReason || (ignoreReason && isJiraTicketDone) || (ignoreReason && isWrongTicket))) {
                element.setAttribute(STATUS, "FAIL");
                if (isJiraTicketDone) {
                    msg1 = "The scenario was ignored due an already done ticket. " + "https://stratio.atlassian.net/browse/" + issue;;
                    msg2 =  " ";
                } else if (isWrongTicket) {
                    msg1 = "The scenario was ignored due to unexistant ticket " +  issue;
                    msg2 = " ";
                } else {
                    msg1 = "The scenario has no valid reason for being ignored. <p>Valid values: @tillfixed(ISSUE-007) @unimplemented @manual @toocomplex</p>";
                    msg2 = " ";
                }

                Element exception = createException(doc,  msg1, msg1, msg2);
                element.appendChild(exception);
                Element systemOut = createExceptionJunit(docJunit, msg1, msg1, msg2);
                Junit.appendChild(systemOut);

            } else if (ignored && ignoreReason) {
                element.setAttribute(STATUS, "SKIP");
                Element exception = createException(doc, "skipped",
                        exceptionmsg, " ");
                element.appendChild(exception);
                Element skippedElementJunit = docJunit.createElement("skipped");
                Junit.appendChild(skippedElementJunit);
                Element systemOut = systemOutPrintJunit(docJunit, exceptionmsg);
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

        public void addStepAndResultListing(StringBuilder sb, List<Step> mergedsteps) {

            for (int i = 0; i < mergedsteps.size(); i++) {
                String resultStatus = "not executed";
                String resultStatusWarn = "*";
                if (i < results.size()) {
                    resultStatus = results.get(i).getStatus();
                    resultStatusWarn = ((results.get(i).getError() != null) && (results.get(i).getStatus()
                            .equals("passed"))) ? "(W)" : "";
                }
                sb.append(mergedsteps.get(i).getKeyword());
                sb.append(mergedsteps.get(i).getName());
                int len = 0;
                len = mergedsteps.get(i).getKeyword().length() + mergedsteps.get(i).getName().length();
                if (mergedsteps.get(i).getRows() != null) {
                    for (DataTableRow row : mergedsteps.get(i).getRows()) {
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
            String cap = "";
            if (!("".equals(cap = hasCapture(featureName, scenario.getName())))) {
                sb.append("evidence @ " + System.getProperty("BUILD_URL", "") + "/artifact/testsAT/"+ cap.replaceAll("",""));
            }
        }

        private String hasCapture(String feat, String scen) {
            String testSuffix = System.getProperty("TESTSUFFIX");
            File dir;
            if (testSuffix != null) {
                dir = new File("./target/executions/" + testSuffix + "/");
            } else {
                dir = new File("./target/executions/");
            }
            final String[] imgext = {"png"};
            Collection<File> files = FileUtils.listFiles(dir, imgext, true);

            for (File file: files) {
                if (file.getPath().contains(featureName.replaceAll(" ", "_") + "." + scenario.getName().replaceAll(" ", "_")) &&
                        file.getName().contains("assert")) {
                    return file.toString();
                }
            }
            return "";
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
