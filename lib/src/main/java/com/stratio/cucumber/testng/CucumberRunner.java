package com.stratio.cucumber.testng;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import cucumber.api.CucumberOptions;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;


public class CucumberRunner {

    private final cucumber.runtime.Runtime runtime;

    /**
     * Default constructor for cucumber Runner.
     * 
     * @param clazz
     * @param feature
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unused")
    public CucumberRunner(Class<?> clazz, String... feature) throws IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz,
                new Class[] { CucumberOptions.class });
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        boolean aux = new File("target/executions/").mkdirs();
        CucumberReporter reporterTestNG;

        if ((feature.length == 0)) {
            reporterTestNG = new CucumberReporter("target/executions/", clazz.getCanonicalName(), "");
        } else {
            List<String> features = new ArrayList<String>();
            String fPath = "src/test/resources/features/" + feature[0] + ".feature";
            features.add(fPath);
            runtimeOptions.getFeaturePaths().addAll(features);
            reporterTestNG = new CucumberReporter("target/executions/", clazz.getCanonicalName(), feature[0]);
        }

        List<String> uniqueGlue = new ArrayList<String>();
        uniqueGlue.add("classpath:com/stratio/specs");
	uniqueGlue.add("classpath:com/stratio/sparkta/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/crossdata/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/streaming/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/ingestion/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/datavis/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/connectors/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/admin/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/manager/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/viewer/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/decision/testsAT/specs");
        runtimeOptions.getGlue().clear();
        runtimeOptions.getGlue().addAll(uniqueGlue);

        runtimeOptions.addFormatter(reporterTestNG);
        Set<Class<? extends ICucumberFormatter>> implementers = new Reflections("com.stratio.tests.utils")
                .getSubTypesOf(ICucumberFormatter.class);

        for (Class<? extends ICucumberFormatter> implementerClazz : implementers) {
            Constructor<?> ctor = implementerClazz.getConstructor();
            ctor.setAccessible(true);
            runtimeOptions.addFormatter((ICucumberFormatter) ctor.newInstance());
        }

        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        runtime = new cucumber.runtime.Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
    }

    /**
     * Run the testclases(Features).
     * 
     * @throws IOException
     */
    public void runCukes() throws IOException {
        runtime.run();
    }
}
