package com.stratio.cucumber.aspects;

import com.stratio.exceptions.NonReplaceableException;
import com.stratio.tests.utils.ThreadProperty;
import cucumber.runtime.*;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.DataTableRow;
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
import java.util.*;

@Aspect
public class ReplacementAspect {

	private final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getCanonicalName());

	@Pointcut("call(cucumber.runtime.StepDefinitionMatch.new(..)) && "
			+ "args (arguments, stepDefinition, featurePath, step, localizedXStreams)")
	protected void replacementCallPointcut(List<Argument> arguments, StepDefinition stepDefinition, String featurePath, Step step, LocalizedXStreams localizedXStreams) {
	}

	@Pointcut("execution (public void com.stratio.cucumber.testng.CucumberReporter.TestMethod.addStepAndResultListing(..)) && "
			+ "args (sb, mergedsteps)")
	protected void replacementTestngCallPointcut(StringBuilder sb, List<Step> mergedsteps) {
	}

	@Pointcut("execution (public String com.stratio.cucumber.testng.CucumberReporter.TestMethod.obtainOutlineScenariosExamples(..)) && "
			+ "args (examplesData)")
	protected void replacementOutlineScenariosCallPointcut(String examplesData) {
	}

	@Pointcut("call(void cucumber.runtime.Runtime.runStep(..)) && "
			+ "args (featurePath, step, reporter, i18n)")
	protected void replacementDataError(String featurePath, Step step, Reporter reporter, I18n i18n) {
	}

	@Around(value = "replacementDataError(featurePath, step, reporter, i18n)")
	public void aroundReplacementException(ProceedingJoinPoint pjp, String featurePath, Step step, Reporter reporter, I18n i18n) throws Throwable {
		try {
			pjp.proceed();
		} catch (NonReplaceableException e) {}
	}

	@Around(value = "replacementOutlineScenariosCallPointcut(examplesData)")
	public Object aroundReplacementOutlineScenariosCalls(ProceedingJoinPoint pjp, String examplesData) throws Throwable {
		String newExamplesData = examplesData;

		if (newExamplesData.contains("${")) {
			newExamplesData = replaceEnvironmentPlaceholders(newExamplesData);
		}
		if (newExamplesData.contains("!{")) {
			newExamplesData = replaceReflectionPlaceholders(newExamplesData);
		}
		if (newExamplesData.contains("@{")) {
			newExamplesData = replaceCodePlaceholders(newExamplesData);
		}

		Object[] myArray = {newExamplesData};
		return pjp.proceed(myArray);
	}



	@Around(value = "replacementTestngCallPointcut(sb, mergedsteps)")
	public Object aroundReplacementTestngCalls(ProceedingJoinPoint pjp, StringBuilder sb, List<Step> mergedsteps) throws Throwable {
		List<Step> newMergedsteps = new ArrayList<Step>();
		for (Step step: mergedsteps) {
			newMergedsteps.add(modifyStep(step));
		}

		Object[] myArray = {sb, newMergedsteps};
		return pjp.proceed(myArray);
	}


	@Around(value = "replacementCallPointcut(arguments, stepDefinition, featurePath, step, localizedXStreams)")
	public Object aroundReplacementCalls(ProceedingJoinPoint pjp, List<Argument> arguments, StepDefinition stepDefinition, String featurePath, Step step, LocalizedXStreams localizedXStreams) throws Throwable {
		if (arguments.size() > 0) {
			List<Argument> myArguments = new ArrayList<Argument>();
			if (arguments != null) {
				for (Argument arg : arguments) {
					if (arg.getVal() != null) {
						String value = arg.getVal();
						if (value.contains("${")) {
							value = replaceEnvironmentPlaceholders(value);
						}
						if (value.contains("!{")) {
							value = replaceReflectionPlaceholders(value);
						}
						if (value.contains("@{")) {
							value = replaceCodePlaceholders(value);
						}
						Argument myArg = new Argument(arg.getOffset(), value);
						myArguments.add(myArg);
					} else {
						myArguments.add(arg);
					}
				}
				arguments = myArguments;
			}
		}
	    // Proceed with new modified params
		Object[] myArray = {arguments, stepDefinition, featurePath, modifyStep(step), localizedXStreams};
		return pjp.proceed(myArray);
	}


	protected Step modifyStep(Step step) throws Exception {
		Step newStep = step;

		if (step != null) {
		    // Modify line
		    String stepName = step.getName();

		    if (stepName.contains("${")) {
			stepName = replaceEnvironmentPlaceholders(stepName);
		    }
		    if (stepName.contains("!{")) {
				try {
					stepName = replaceReflectionPlaceholders(stepName);
				} catch (NonReplaceableException e) {
					logger.error("Unreplaceable elements at {}", stepName);
				}
		    }
			if (stepName.contains("@{")) {
			stepName = replaceCodePlaceholders(stepName);
		    }

		    // Modify datatable
		    List<DataTableRow> stepRows = step.getRows();

		    List<DataTableRow> myRows = null;
		    if (stepRows != null) {
				DataTableRow myRow;
				myRows =  new ArrayList<DataTableRow>();
				for (DataTableRow row: stepRows) {
			    	List<String> cells = row.getCells();
			    	List<String> myCells = new ArrayList<String>();
			    	for (String cell: cells) {
						if (cell.contains("${")) {
				    		cell = replaceEnvironmentPlaceholders(cell);
						}
						if (cell.contains("!{")) {
							try {
								cell = replaceReflectionPlaceholders(cell);
							} catch (NonReplaceableException e) {
								logger.error("Unreplaceable elements at {}", cell);
							}
						}
						if (cell.contains("@{")) {
				    		cell = replaceCodePlaceholders(cell);
						}
						myCells.add(cell);
			    	}
			    	myRow = new DataTableRow(row.getComments(), myCells, row.getLine());
			    	myRows.add(myRow);
				}
		    }

		    // Redefine step
		    newStep = new Step(step.getComments(), step.getKeyword(), stepName, step.getLine(), myRows, step.getDocString());
		}

		return newStep;
	}

	/**
	 * Replaces every placeholded element, enclosed in @{} with the
	 * corresponding attribute value in local Common class
	 *
	 * If the element starts with:
	 * 	- IP: We expect it to be followed by '.' + interface name (i.e. IP.eth0). It can contain other replacements.
	 *
	 * @param element
	 *
	 * @return String
	 *
	 * @throws Exception
	 */
	protected String replaceCodePlaceholders(String element) throws Exception {
		String newVal = element;
		while (newVal.contains("@{")) {
			String placeholder = newVal.substring(newVal.indexOf("@{"),
					newVal.indexOf("}") + 1);
			String property = placeholder.substring(2, placeholder.length() - 1);
			String subproperty = "";
			if (placeholder.contains(".")) {
				property = placeholder.substring(2, placeholder.indexOf("."));
				subproperty = placeholder.substring(placeholder.indexOf(".") + 1, placeholder.length() - 1);
			} else {
				throw new Exception("Interface not defined");
			}

			switch (property) {
				case "IP":
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
						throw new Exception("Interface " + subproperty + " not available" );
					}
					break;
				default:
					throw new Exception("Property not defined");
			}
		}
		return newVal;
	}


	/**
	 * Replaces every placeholded element, enclosed in !{} with the
	 * corresponding attribute value in local Common class
	 * 
	 * @param element
	 * 
	 * @return String
	 * 
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	protected String replaceReflectionPlaceholders(String element) throws Exception {
		String newVal = element;
		while (newVal.contains("!{")) {
			String placeholder = newVal.substring(newVal.indexOf("!{"),
					newVal.indexOf("}") + 1);
			String attribute = placeholder.substring(2, placeholder.length() - 1);

			// we want to use value previously saved
			String prop = ThreadProperty.get(attribute);

			if (prop == null) {
				logger.error("Element: " + attribute + " has not been saved correctly previously.");
				newVal = "ERR: UNREPLACEABLE_PLACEHOLDER";
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
	 * 
	 * @return String
	 */
	protected String replaceEnvironmentPlaceholders(String element) {
		String newVal = element;
		while (newVal.contains("${")) {
			String placeholder = newVal.substring(newVal.indexOf("${"),
					newVal.indexOf("}") + 1);
			String modifier = "";
			String sysProp = "";
			if (placeholder.contains(".")) {
				sysProp = placeholder.substring(2, placeholder.indexOf("."));
				modifier = placeholder.substring(placeholder.indexOf(".") + 1,
						placeholder.length() - 1);
			} else {
				sysProp = placeholder.substring(2, placeholder.length() - 1);
			}

			String prop = "";
			if ("toLower".equals(modifier)) {
				prop = System.getProperty(sysProp, "").toLowerCase();
			} else if ("toUpper".equals(modifier)) {
				prop = System.getProperty(sysProp, "").toUpperCase();
			} else {
				prop = System.getProperty(sysProp, "");
			}
			newVal = newVal.replace(placeholder, prop);
		}

		return newVal;
	}	
}
