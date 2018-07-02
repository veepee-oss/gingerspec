package com.privalia.qa.aspects;

import com.privalia.qa.cucumber.testng.CucumberReporter;
import com.privalia.qa.exceptions.NonReplaceableException;
import com.privalia.qa.specs.CommonG;
import com.privalia.qa.utils.ThreadProperty;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;
import gherkin.I18n;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.privalia.qa.aspects.IgnoreTagAspect.ignoreReasons.NOREASON;
import static com.privalia.qa.aspects.IgnoreTagAspect.ignoreReasons.NOTIGNORED;

/**
 * Adds the possibility of printing the comments from the feature files as info level messages when executing
 * via CLI
 * @author José Fernández
 */
@Aspect
public class LogTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());


    @Pointcut("execution (public void cucumber.runtime.Runtime.runStep(..)) && "
            + "args (featurePath, step, reporter, i18n)")
    protected void logStep(String featurePath, Step step, Reporter reporter, I18n i18n) {
    }

    @Before(value = "logStep(featurePath, step, reporter, i18n)")
    public void beforeLogStep(JoinPoint jp, String featurePath, Step step, Reporter reporter, I18n i18n) throws Throwable {

        List<Comment> comments = step.getComments();

        for (Comment comment: comments) {

            String value = comment.getValue();

            if (value.toLowerCase().startsWith("#trace")) {
                logger.trace(value.replace("#trace ", ""));
            }
            if (value.toLowerCase().startsWith("#debug")) {
                logger.debug(value.replace("#debug ", ""));
            }
            if (value.toLowerCase().startsWith("#info")) {
                logger.info(value.replace("#info ", ""));
            }
            if (value.toLowerCase().startsWith("#warn")) {
                logger.warn(value.replace("#warn ", ""));
            }
            if (value.toLowerCase().startsWith("#error")) {
                logger.error(value.replace("#error ", ""));
            }
        }

    }


}
