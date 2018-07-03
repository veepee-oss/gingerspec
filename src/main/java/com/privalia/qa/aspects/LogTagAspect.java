package com.privalia.qa.aspects;

import gherkin.I18n;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
