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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
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
import cucumber.runtime.io.URLOutputStream;
import cucumber.runtime.io.UTF8OutputStreamWriter;

class CucumberReporter implements Formatter, Reporter {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	private final Writer writer;
	private final Document document;
	private final Element results;
	private final Element suite;
	private final Element test;
	private Element clazz;
	private Element root;
	private TestMethod testMethod;
	private Feature feat;
	private Examples tmpExamples;
	private List<Result> tmpHooks = new ArrayList<Result>();
	private List<Step> tmpSteps = new ArrayList<Step>();
	private Integer iteration = 0;
	private Integer position = 0;
	private String callerClass;

	public CucumberReporter(URL url, String cClass) throws IOException {
		this.writer = new UTF8OutputStreamWriter(new URLOutputStream(url));
		TestMethod.treatSkippedAsFailure = false;
		try {
			document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
			results = document.createElement("testng-results");
			suite = document.createElement("suite");
			test = document.createElement("test");
			callerClass = cClass;
			suite.appendChild(test);
			results.appendChild(suite);
			document.appendChild(results);
		} catch (ParserConfigurationException e) {
			throw new CucumberException("Error initializing DocumentBuilder.",
					e);
		}
	}

	@Override
	public void syntaxError(String state, String event,
			List<String> legalEvents, String uri, Integer line) {
	}

	@Override
	public void uri(String uri) {
	}

	@Override
	public void feature(Feature feature) {
		feat = feature;
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
		clazz.appendChild(root);
		testMethod = new TestMethod(scenario);
		testMethod.feature = feat;
		testMethod.hooks = tmpHooks;
		if (tmpExamples == null) {
			testMethod.steps = tmpSteps;
		} else {
			testMethod.steps = new ArrayList<Step>();
		}
		testMethod.examplesData = tmpExamples;
		testMethod.start(root, iteration);
		iteration++;
	}

	@Override
	public void background(Background background) {
	}

	@Override
	public void step(Step step) {
		if (step.getClass().toString().contains("ExampleStep")) {
			testMethod.steps.add(step);
		} else {
			tmpSteps.add(step);
		}
	}

	@Override
	public void endOfScenarioLifeCycle(Scenario scenario) {
		testMethod.finish(document, root, position);
		position++;
		if ((tmpExamples != null)
				&& (iteration >= tmpExamples.getRows().size())) {
			tmpExamples = null;
		}
		tmpHooks.clear();
		tmpSteps.clear();
		testMethod = null;
	}

	@Override
	public void eof() {
	}

	@Override
	public void done() {
		try {
			results.setAttribute("total",
					String.valueOf(getElementsCountByAttribute(suite, "status",
							".*")));
			results.setAttribute("passed", String
					.valueOf(getElementsCountByAttribute(suite, "status",
							"PASS")));
			results.setAttribute("failed", String
					.valueOf(getElementsCountByAttribute(suite, "status",
							"FAIL")));
			results.setAttribute("skipped", String
					.valueOf(getElementsCountByAttribute(suite, "status",
							"SKIP")));
			suite.setAttribute("name", CucumberReporter.class.getName());
			suite.setAttribute("duration-ms",
					getTotalDuration(suite.getElementsByTagName("test-method")));
			test.setAttribute("name", CucumberReporter.class.getName());
			test.setAttribute("duration-ms",
					getTotalDuration(suite.getElementsByTagName("test-method")));

			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult streamResult = new StreamResult(writer);
			DOMSource domSource = new DOMSource(document);
			transformer.transform(domSource, streamResult);
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

	private int getElementsCountByAttribute(Node node, String attributeName,
			String attributeValue) {
		int count = 0;

		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			count += getElementsCountByAttribute(node.getChildNodes().item(i),
					attributeName, attributeValue);
		}

		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			Node namedItem = attributes.getNamedItem(attributeName);
			if (namedItem != null
					&& namedItem.getNodeValue().matches(attributeValue)) {
				count++;
			}
		}
		return count;
	}

	private String getTotalDuration(NodeList testCaseNodes) {
		long totalDuration = 0;
		for (int i = 0; i < testCaseNodes.getLength(); i++) {
			try {
				String duration = "0";
				Node durationms = testCaseNodes.item(i).getAttributes()
						.getNamedItem("duration-ms");
				if (durationms != null) {
					duration = durationms.getNodeValue();
				}
				totalDuration += Long.parseLong(duration);
			} catch (NumberFormatException e) {
				throw new CucumberException(e);
			} catch (NullPointerException e) {
				throw new CucumberException(e);
			}
		}
		return String.valueOf(totalDuration);
	}

	private static class TestMethod {

		Feature feature = null;
		Scenario scenario = null;
		Examples examplesData;
		static boolean treatSkippedAsFailure = false;
		List<Step> steps;
		final List<Result> results = new ArrayList<Result>();
		List<Result> hooks;
		Integer iteration = 1;

		private TestMethod(Scenario scenario) {
			this.scenario = scenario;
		}

		private void start(Element element, Integer iteration) {
			this.iteration = iteration;
			if ((examplesData == null)
					|| (this.iteration >= examplesData.getRows().size())) {
				ThreadProperty.set("dataSet", "");
				element.setAttribute("name", scenario.getName());
			} else {
				ThreadProperty.set("dataSet",
						examplesData.getRows().get(iteration).getCells()
								.toString());

				element.setAttribute("name", scenario.getName()
						+ " "
						+ examplesData.getRows().get(iteration).getCells()
								.toString());
			}
			element.setAttribute("started-at", DATE_FORMAT.format(new Date()));
		}

		public void finish(Document doc, Element element, Integer position) {

			element.setAttribute("duration-ms", calculateTotalDurationString());
			element.setAttribute("finished-at", DATE_FORMAT.format(new Date()));
			StringBuilder stringBuilder = new StringBuilder();

			addStepAndResultListing(stringBuilder);
			Result skipped = null;
			Result failed = null;

			for (Result result : results) {
				if ("failed".equals(result.getStatus())) {
					failed = result;
				} else if ("undefined".equals(result.getStatus())
						|| "pending".equals(result.getStatus())) {
					skipped = result;
				}
			}
			for (Result result : hooks) {
				if (failed == null && "failed".equals(result.getStatus())) {
					failed = result;
				}
			}
			if (failed != null) {
				element.setAttribute("status", "FAIL");
				StringWriter stringWriter = new StringWriter();
				failed.getError()
						.printStackTrace(new PrintWriter(stringWriter));
				Element exception = createException(doc, failed.getError()
						.getClass().getName(), stringBuilder.toString(),
						stringWriter.toString());
				element.appendChild(exception);

			} else if (skipped != null) {
				if (treatSkippedAsFailure) {
					element.setAttribute("status", "FAIL");
					Element exception = createException(doc,
							"The scenario has pending or undefined step(s)",
							stringBuilder.toString(),
							"The scenario has pending or undefined step(s)");
					element.appendChild(exception);
				} else {
					element.setAttribute("status", "SKIP");
				}
			} else {
				element.setAttribute("status", "PASS");
			}
		}

		private String calculateTotalDurationString() {
			long totalDurationNanos = 0;
			for (Result r : results) {
				totalDurationNanos += r.getDuration() == null ? 0 : r
						.getDuration();
			}
			for (Result r : hooks) {
				totalDurationNanos += r.getDuration() == null ? 0 : r
						.getDuration();
			}
			return String.valueOf(totalDurationNanos / 1000000);
		}

		private void addStepAndResultListing(StringBuilder sb) {

			for (int i = 0; i < steps.size(); i++) {				
				String resultStatus = "not executed";
				String resultStatusWarn = "*";
				if (i < results.size()) {
					resultStatus = results.get(i).getStatus();
					resultStatusWarn = ((results.get(i).getError() != null) && (results
							.get(i).getStatus().equals("passed"))) ? "(W)" : "";
				}
				sb.append(steps.get(i).getKeyword());
				sb.append(steps.get(i).getName());
				int len = 0;
				len = steps.get(i).getKeyword().length() + steps.get(i).getName().length();
				if (steps.get(i).getRows() != null) {					
					for (DataTableRow row : steps.get(i).getRows()) {
						String strrow = "| ";
						for (String cell : row.getCells()) {
							strrow += cell + " | ";
						}
						len = strrow.length() + 11;
						sb.append("\n           " + strrow);
					}
				}
				for (int j = 0 ; j + len < 140; j++) {
					sb.append(".");
				}
				sb.append(resultStatus + resultStatusWarn);
				sb.append("\n");
			}
		}

		private Element createException(Document doc, String clazz,
				String message, String stacktrace) {
			Element exceptionElement = doc.createElement("exception");
			exceptionElement.setAttribute("class", clazz);

			if (message != null) {
				Element messageElement = doc.createElement("message");
				message = "\r\n<pre>\r\n" + message + "\r\n</pre>\r\n";
				messageElement.appendChild(doc.createCDATASection(message));
				exceptionElement.appendChild(messageElement);
			}

			Element stacktraceElement = doc.createElement("full-stacktrace");
			stacktraceElement.appendChild(doc.createCDATASection(stacktrace));
			exceptionElement.appendChild(stacktraceElement);

			return exceptionElement;
		}
	}
}