package com.privalia.qa.cucumber.testng;

import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.api.testng.*;
import cucumber.runner.*;
import cucumber.runtime.*;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import gherkin.events.PickleEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a custom implementation of {@link TestNGCucumberRunner} for running feature files
 * using TestNG. This implementation automatically adds glue files, feature files and
 * proper formatter options to all tests
 *
 * @author Jose Fernandez
 */
public class CucumberRunner {

    private final EventBus bus;

    private final Filters filters;

    private final FeaturePathFeatureSupplier featureSupplier;

    private final ThreadLocalRunnerSupplier runnerSupplier;

    private final RuntimeOptions runtimeOptions;


    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the cucumber.api.CucumberOptions and org.testng.annotations.Test annotations
     */
    public CucumberRunner(Class clazz) throws NoSuchFieldException {

        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        runtimeOptions = runtimeOptionsFactory.create();

        /* Automatically adds glue files of library if not present in CucumberOptions annotation */
        if (!runtimeOptions.getGlue().contains("com.privalia.qa.specs")) {
            List<String> uniqueGlue = new ArrayList<String>();
            uniqueGlue.add("com.privalia.qa.specs");
            runtimeOptions.getGlue().addAll(uniqueGlue);
        }

        /* Calculate route where to store reports */
        String testSuffix = System.getProperty("TESTSUFFIX");
        String targetExecutionsPath = "target/executions/";
        if (testSuffix != null) {
            targetExecutionsPath = targetExecutionsPath + testSuffix + "/";
        }
        boolean aux = new File(targetExecutionsPath).mkdirs();


        /* Include custom reporter*/
        List<String> reporters = new ArrayList<String>();
        reporters.add("com.privalia.qa.cucumber.reporter.TestNGPrettyFormatter");

        /* Include TestNG reporter (store TestNG reports under /target/executions/com.mypackage.myClass.xml) */
        //reporters.add("pretty");
        //reporters.add("testng:" + targetExecutionsPath + clazz.getName() + ".xml");

        /* include abode reporters to the "Plugins" of cucumber options*/
        Field pluginFormatterNamesField = runtimeOptions.getClass().getDeclaredField("pluginFormatterNames");
        pluginFormatterNamesField.setAccessible(true);
        try {
            pluginFormatterNamesField.set(runtimeOptions, reporters);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendModuleBackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        bus = new TimeServiceEventBus(TimeService.SYSTEM);
        new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        filters = new Filters(runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
        featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);

    }

    public void runScenario(PickleEvent pickle) throws Throwable {
        //Possibly invoked in a multi-threaded context
        Runner runner = runnerSupplier.get();
        TestCaseResultListener testCaseResultListener = new TestCaseResultListener(runner.getBus(), runtimeOptions.isStrict());
        runner.runPickle(pickle);
        testCaseResultListener.finishExecutionUnit();

        if (!testCaseResultListener.isPassed()) {
            throw testCaseResultListener.getError();
        }
    }

    public void finish() {
        bus.send(new TestRunFinished(bus.getTime()));
    }

    /**
     * @return returns the cucumber scenarios as a two dimensional array of {@link PickleEventWrapper}
     * scenarios combined with their {@link CucumberFeatureWrapper} feature.
     */
    public Object[][] provideScenarios() {
        try {
            List<Object[]> scenarios = new ArrayList<Object[]>();
            List<CucumberFeature> features = getFeatures();
            for (CucumberFeature feature : features) {
                for (PickleEvent pickle : feature.getPickles()) {
                    if (filters.matchesFilters(pickle)) {
                        scenarios.add(new Object[]{new PickleEventWrapperImpl(pickle), new CucumberFeatureWrapperImpl(feature)});
                    }
                }
            }
            return scenarios.toArray(new Object[][]{});
        } catch (CucumberException e) {
            return new Object[][]{new Object[]{new CucumberExceptionWrapper(e), null}};
        }
    }

    List<CucumberFeature> getFeatures() {

        List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }
        return features;
    }
}
