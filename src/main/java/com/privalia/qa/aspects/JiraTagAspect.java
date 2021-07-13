package com.privalia.qa.aspects;

import com.privalia.qa.utils.JiraConnector;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aspect for managing the @jira() tag on a feature/scenario
 */
@Aspect
public class JiraTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    JiraConnector jc = new JiraConnector();

    /**
     * Pointcut is executed for {@link io.cucumber.testng.AbstractTestNGCucumberTests#runScenario(PickleWrapper, FeatureWrapper)}
     * @param pickleWrapper         the pickleWrapper
     * @param featureWrapper        the featureWrapper
     */
    @Pointcut("execution (void *.runScenario(..)) && args(pickleWrapper, featureWrapper)")
    protected void jiraTagPointcutScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
    }

    @Around(value = "jiraTagPointcutScenario(pickleWrapper, featureWrapper)")
    public void aroundJiraTagPointcut(ProceedingJoinPoint pjp, PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) throws Throwable {

        List<String> tags = pickleWrapper.getPickle().getTags();
        String scenarioName = pickleWrapper.getPickle().getName();

        String ticket = this.jc.getFirstTicketReference(tags);

        if (ticket != null) {
            try {
                if (!jc.entityShouldRun(ticket)) {
                    logger.warn("Scenario '" + scenarioName + "' was ignored, it is in a non runnable status in Jira.");
                    return;
                } else {
                    pjp.proceed();
                }
            } catch (Exception e) {
                logger.warn("Could not retrieve info of ticket " + ticket + " from jira: " + e.getMessage() + ". Proceeding with execution...");
                pjp.proceed();
            }

        } else {
            pjp.proceed();
        }
    }

}
