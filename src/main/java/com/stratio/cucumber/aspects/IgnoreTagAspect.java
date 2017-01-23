package com.stratio.cucumber.aspects;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;

@Aspect
public class IgnoreTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Pointcut("execution (* cucumber.runtime.model.CucumberScenario.run(..)) && "
            + "args (formatter, reporter, runtime)")
    protected void addIgnoreTagPointcutScenario(Formatter formatter, Reporter reporter, Runtime runtime) {
    }

    /**
     * @param pjp
     * @param formatter
     * @param reporter
     * @param runtime
     * @throws Throwable
     */
    @Around(value = "addIgnoreTagPointcutScenario(formatter, reporter, runtime)")
    public void aroundAddIgnoreTagPointcut(ProceedingJoinPoint pjp, Formatter formatter, Reporter reporter,
                                           Runtime runtime) throws Throwable {
        logger.debug("Executing pointcut CucumberScenario run method");

        CucumberScenario scen = (CucumberScenario) pjp.getThis();
        Scenario scenario = (Scenario) scen.getGherkinModel();

        Class<?> sc = scen.getClass();
        Method tt = sc.getSuperclass().getDeclaredMethod("tagsAndInheritedTags");
        tt.setAccessible(true);
        Set<Tag> tags = (Set<Tag>) tt.invoke(scen);

        Boolean ignore = false;
        Boolean ignoreReason = false;

        for (Tag tag : tags) {
            if (tag.getName().equals("@ignore")) {
                ignore = true;
                for (Tag tagNs : tags) {
                    //@tillFixed
                    if ((tagNs.getName()).matches("@tillfixed\\(\\w+-\\d+\\)")) {
                        String issueNumb = tagNs.getName().substring(tagNs.getName().lastIndexOf('(') + 1);
                        logger.warn("Scenario '" + scenario.getName() + "' ignored because of Issue: " + issueNumb.subSequence(0, issueNumb.length() - 1) + ".");
                        ignoreReason = true;
                        break;
                    }
                    //@unimplemented
                    if (tagNs.getName().matches("@unimplemented")) {
                        logger.warn("Scenario '" + scenario.getName() + "' ignored because it is not yet implemented.");
                        ignoreReason = true;
                        break;
                    }
                    //@manual
                    if (tagNs.getName().matches("@manual")) {
                        logger.warn("Scenario '" + scenario.getName() + "' ignored because it is marked as manual test.");
                        ignoreReason = true;
                        break;
                    }
                    //@toocomplex
                    if (tagNs.getName().matches("@toocomplex")) {
                        logger.warn("Scenario '" + scenario.getName() + "' ignored because the test is too complex.");
                        ignoreReason = true;
                        break;
                    }
                }
            }
        }

        if (ignore && !ignoreReason) {
            logger.error("Scenario '" + scenario.getName() + "' failed due to wrong use of the @ignore tag. ");
        }

        if (ignore) {
            runtime.buildBackendWorlds(reporter, tags, scenario.getName());
            formatter.startOfScenarioLifeCycle(scenario);
            formatter.endOfScenarioLifeCycle(scenario);
            runtime.disposeBackendWorlds();
        } else {
            pjp.proceed();
        }
    }
}
