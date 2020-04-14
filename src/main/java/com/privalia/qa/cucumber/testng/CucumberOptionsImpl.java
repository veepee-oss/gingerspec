package com.privalia.qa.cucumber.testng;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a custom implementation of {@link CucumberOptions} annotation. This implementation
 * copies the current parameters of the annotation used in the runner class and adds special
 * configuration parameters needed for GingerSpec:
 *
 * * Automatically adds reference of {@link com.privalia.qa.specs} to the glue
 * * Automatically adds reference of {@link com.privalia.qa.cucumber.reporter.TestNGPrettyFormatter}
 * to the plugins
 * * Automatically include path to store TestNG reports
 *
 * @author Jose Fernandez
 */
public class CucumberOptionsImpl implements CucumberOptions {

    private CucumberOptions cucumberOptionsAnnotation;

    private String className;

    public CucumberOptionsImpl(Class clazz) {
        this.cucumberOptionsAnnotation = (CucumberOptions) clazz.getAnnotation(CucumberOptions.class);
        this.className = clazz.getName();
    }

    @Override
    public boolean dryRun() {
        return cucumberOptionsAnnotation.dryRun();
    }

    @Override
    public boolean strict() {
        return cucumberOptionsAnnotation.strict();
    }

    @Override
    public String[] features() {
        return cucumberOptionsAnnotation.features();
    }

    /**
     * Automatically adds {@link com.privalia.qa.specs} to the glue if not present
     * to have access to all GingerSpec steps definitions
     * @return  Array with reference path to classes/packages that contain steps definitions
     */
    @Override
    public String[] glue() {
        List<String> glue  = new ArrayList<String>(Arrays.asList(cucumberOptionsAnnotation.glue()));
        if (!glue.contains("com.privalia.qa.specs")) {
            glue.add("com.privalia.qa.specs");
            return (String[]) glue.toArray(cucumberOptionsAnnotation.glue());
        }
        return cucumberOptionsAnnotation.glue();
    }

    @Override
    public String[] extraGlue() {
        return cucumberOptionsAnnotation.extraGlue();
    }

    @Override
    public String[] tags() {
        return cucumberOptionsAnnotation.tags();
    }

    /**
     * Automatically adds reference of {@link com.privalia.qa.cucumber.reporter.TestNGPrettyFormatter}
     * to the plugins and includes path to store TestNG reports
     * @return  Array with reference path to the plugins
     */
    @Override
    public String[] plugin() {
        List<String> plugin  = new ArrayList<String>(Arrays.asList(cucumberOptionsAnnotation.plugin()));

        /* Calculate route where to store reports */
        String testSuffix = System.getProperty("TESTSUFFIX");
        String targetExecutionsPath = "target/executions/";
        if (testSuffix != null) {
            targetExecutionsPath = targetExecutionsPath + testSuffix + "/";
        }

        /* Include TestNG reporter (store TestNG reports under /target/executions/com.mypackage.myClass.xml) */
        plugin.add("testng:" + targetExecutionsPath + this.className + ".xml");

        /*Include custom reporter*/
        plugin.add("com.privalia.qa.cucumber.reporter.TestNGPrettyFormatter");

        return (String[]) plugin.toArray(cucumberOptionsAnnotation.plugin());

    }

    @Override
    public boolean monochrome() {
        return cucumberOptionsAnnotation.monochrome();
    }

    @Override
    public String[] name() {
        return cucumberOptionsAnnotation.name();
    }

    @Override
    public SnippetType snippets() {
        return cucumberOptionsAnnotation.snippets();
    }

    @Override
    public String[] junit() {
        return cucumberOptionsAnnotation.junit();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
