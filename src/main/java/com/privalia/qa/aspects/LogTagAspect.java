//package com.privalia.qa.aspects;
//
//import com.privalia.qa.cucumber.reporter.TestSourcesModel;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Adds the possibility of printing the comments from the feature files as info level messages when executing.
// * Using #log {@literal <message>} in a feature file, will print the message in the CLI when executing the test
// *
// * The log message is captured right before is printed on console and altered using the {@link ReplacementAspect}
// *
// * @author José Fernández
// */
//@Aspect
//public class LogTagAspect {
//
//    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
//
//    private TestSourcesModel testSources;
//
//
//    @Pointcut("execution (* com.privalia.qa.cucumber.reporter.TestNGPrettyFormatter.formatComment(..)) && args(comment)")
//    protected void logStep(String comment) {
//    }
//
//    @Around(value = "logStep(comment)")
//    public StringBuilder beforeLogStep(ProceedingJoinPoint pjp, String comment) throws Throwable {
//
//        String replacedValue;
//        try {
//            ReplacementAspect replace = new ReplacementAspect();
//            replacedValue = replace.replacedElement(comment, pjp);
//        } catch (Exception e) {
//            replacedValue = comment;
//        }
//
//        return (StringBuilder) pjp.proceed(new Object[] {replacedValue});
//
//    }
//
//}
