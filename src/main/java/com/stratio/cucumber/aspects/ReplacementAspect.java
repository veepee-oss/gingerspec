package com.stratio.cucumber.aspects;

import com.stratio.exceptions.NonReplaceableException;
import com.stratio.tests.utils.ThreadProperty;
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

	private String lastEchoedStep = "";

	private final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getCanonicalName());



	@Pointcut("execution (public String com.stratio.cucumber.testng.CucumberReporter.TestMethod.obtainOutlineScenariosExamples(..)) && "
			+ "args (examplesData)")
	protected void replacementOutlineScenariosCallPointcut(String examplesData) {
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

	@Pointcut("call(String gherkin.formatter.model.BasicStatement.getName())")
	protected void replacementBasicStatementName() {
	}

	@Around(value = "replacementBasicStatementName()")
	public String aroundReplacementBasicStatementName(ProceedingJoinPoint pjp) throws Throwable {

		String oldVal = (String) pjp.proceed();
		String newBasicStmt = oldVal;


		if (oldVal.contains("${")) {
			newBasicStmt = replaceEnvironmentPlaceholders(oldVal);
		}
		if (oldVal.contains("!{")) {
			newBasicStmt = replaceReflectionPlaceholders(oldVal);
		}
		if (oldVal.contains("@{")) {
			newBasicStmt = replaceCodePlaceholders(oldVal);
		}

		if ((pjp.getTarget() instanceof Step) &&
				!(lastEchoedStep.equals(newBasicStmt)) &&
				!(newBasicStmt.contains("'<"))) {
			lastEchoedStep = newBasicStmt;
			logger.info("  {}{}", ((Step)pjp.getTarget()).getKeyword(),newBasicStmt);
		}
		return newBasicStmt;
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
					newVal.indexOf("}", newVal.indexOf("@{")) + 1);
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
					newVal.indexOf("}", newVal.indexOf("!{")) + 1);
			String attribute = placeholder.substring(2, placeholder.length() - 1);
			// we want to use value previously saved
			String prop = ThreadProperty.get(attribute);
			if (prop == null) {
				logger.error("{} -> {} local var has not been saved correctly previously.", element, attribute);
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
	protected String replaceEnvironmentPlaceholders(String element) throws NonReplaceableException {
		String newVal = element;
		while (newVal.contains("${")) {
			String placeholder = newVal.substring(newVal.indexOf("${"),
					newVal.indexOf("}", newVal.indexOf("${")) + 1);
			String modifier = "";
			String sysProp;
			if (placeholder.contains(".")) {
				sysProp = placeholder.substring(2, placeholder.indexOf("."));
				modifier = placeholder.substring(placeholder.indexOf(".") + 1,
						placeholder.length() - 1);
			} else {
				sysProp = placeholder.substring(2, placeholder.length() - 1);
			}

			String prop = System.getProperty(sysProp);

			if (prop == null) {
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

