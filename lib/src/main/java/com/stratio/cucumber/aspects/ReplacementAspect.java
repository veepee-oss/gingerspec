package com.stratio.cucumber.aspects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stratio.specs.CommonG;

import cucumber.api.DataTable;

@Aspect
public class ReplacementAspect {

	private final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getCanonicalName());

	@Pointcut("execution(* com.stratio.specs.GivenGSpec..*(..))"
		+ " || execution(* com.stratio.specs.WhenGSpec..*(..))"
		+ " || execution(* com.stratio.specs.HookGSpec..*(..))"
		+ " || execution(* com.stratio.specs.BaseGSpec..*(..))"
		+ " || execution(* com.stratio.specs.ThenGSpec..*(..))")
	protected void replacementCallPointcut() {
	}

	/**
	 * When performing calls to methods in com.stratio.specs, we replace the system
	 * and stored variables with the actual value
	 * 
	 * @param pjp
	 * @return Object
	 * @throws Throwable 
	 */
	@Around(value = "replacementCallPointcut()")
	public void aroundReplacementCalls(ProceedingJoinPoint pjp) throws Throwable {
	    Object[] args = pjp.getArgs();
	    if (args.length > 0) {
		for (int i = 0; i < args.length; i++) {
		    if (args[i] != null) {
			if (args[i] instanceof DataTable) {
			   DataTable dataTable = (DataTable)args[i];
			   List<List<String>> listOfLists = new ArrayList<List<String>>();
			   for (int j = 0; j < dataTable.raw().size(); j ++) {
			       List<String> list = new ArrayList<String>();
			       for (int k = 0; k < dataTable.raw().get(j).size(); k ++) {
				   String res = dataTable.raw().get(j).get(k);
				   if (res.contains("${")) {
				       res = replacePlaceholders(res);
				   }
				   if (res.contains("!{")) {
				       res = replaceReflectionPlaceholders(res);
				   }
				   list.add(res);
			       }
			       listOfLists.add(list);
			   }
			   args[i] = DataTable.create(listOfLists);  
			} else {
			    String res = args[i].toString();
			    if (res.contains("${")) {
				res = replacePlaceholders(res);
			    }
			    if (res.contains("!{")) {
		    		res = replaceReflectionPlaceholders(res);
			    }
			    
			    switch(args[i].getClass().getName()) {
			    	case "java.lang.Integer":
			    	    args[i] = Integer.parseInt(res);
			    	    break;
			    	case "java.lang.Double":
			    	    args[i] = Double.parseDouble(res);
			    	    break;
			    	case "java.lang.Float":
			    	    args[i] = Float.parseFloat(res);
			    	    break;
			    	case "java.lang.Long":
			    	    args[i] = Long.parseLong(res);
			    	    break;
			    	default:
			    	    args[i] = res;
			    	    break;
			    }			    
			}
		    }
		}
		pjp.proceed(args);
	    } else {
		pjp.proceed();
	    }
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
	protected String replaceReflectionPlaceholders(String element) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String newVal = element;
		while (newVal.contains("!{")) {
			String placeholder = newVal.substring(newVal.indexOf("!{"),
					newVal.indexOf("}") + 1);
			String attribute = placeholder.substring(2, placeholder.length() - 1);
			
			// we want to use value previously saved
			Reflections reflections = new Reflections("com.stratio");    
			Set classes = reflections.getSubTypesOf(CommonG.class);
			    
			Object pp = (classes.toArray())[0];
			String qq = (pp.toString().split(" "))[1];
			Class<?> c = Class.forName(qq.toString());
			
			Field ff = c.getDeclaredField(attribute);
			ff.setAccessible(true);
			String prop = (String)ff.get(null);
			
			newVal = newVal.replace(placeholder, prop);
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
	protected String replacePlaceholders(String element) {
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