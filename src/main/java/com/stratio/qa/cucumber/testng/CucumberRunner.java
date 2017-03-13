/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.qa.cucumber.testng;

import com.stratio.qa.utils.CukesGHooks;
import cucumber.api.CucumberOptions;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import gherkin.formatter.Formatter;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class CucumberRunner {

    private final cucumber.runtime.Runtime runtime;
    private ClassLoader classLoader;
    private RuntimeOptions runtimeOptions;

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
        classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz,
                new Class[]{CucumberOptions.class});
        runtimeOptions = runtimeOptionsFactory.create();
        String testSuffix = System.getProperty("TESTSUFFIX");
        String targetExecutionsPath = "target/executions/";
        if (testSuffix != null) {
            targetExecutionsPath = targetExecutionsPath + testSuffix + "/";
        }
        boolean aux = new File(targetExecutionsPath).mkdirs();
        CucumberReporter reporterTestNG;

        if ((feature.length == 0)) {
            reporterTestNG = new CucumberReporter(targetExecutionsPath, clazz.getCanonicalName(), "");
        } else {
            List<String> features = new ArrayList<String>();
            String fPath = "src/test/resources/features/" + feature[0] + ".feature";
            features.add(fPath);
            runtimeOptions.getFeaturePaths().addAll(features);
            reporterTestNG = new CucumberReporter(targetExecutionsPath, clazz.getCanonicalName(), feature[0]);
        }

        List<String> uniqueGlue = new ArrayList<String>();
        uniqueGlue.add("classpath:com/stratio/qa/specs");
        uniqueGlue.add("classpath:com/stratio/sparta/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/gosecsso/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/dcos/crossdata/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/crossdata/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/streaming/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/ingestion/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/datavis/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/connectors/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/admin/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/explorer/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/manager/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/viewer/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/decision/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/paas/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/cassandra/lucene/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/analytic/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/exhibitor/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/intelligence/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/postgresbd/testsAT/specs");
        uniqueGlue.add("classpath:com/stratio/universe/testsAT/specs");
        runtimeOptions.getGlue().clear();
        runtimeOptions.getGlue().addAll(uniqueGlue);

        runtimeOptions.addFormatter(reporterTestNG);
        Set<Class<? extends ICucumberFormatter>> implementers = new Reflections("com.stratio.qa.utils")
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
    public void runCukes() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        try {
            runtime.run();
        } catch (Exception e){

            Class<?> runtimeOptionsClass = runtimeOptions.getClass();
            Method tt = runtimeOptionsClass.getDeclaredMethod("getFormatters");
            tt.setAccessible(true);
            List<Formatter> formaterList = (List<Formatter>) tt.invoke(runtimeOptions);

            for (Object elem : formaterList) {
                if (elem instanceof CukesGHooks) {
                    Formatter formatter = runtimeOptions.formatter(classLoader);
                    formatter.endOfScenarioLifeCycle(((CukesGHooks) elem).scenario);
                    formatter.done();
                    formatter.close();
                }
            }
            throw e;
        } finally {
            if (!runtime.getErrors().isEmpty()) {
                throw new CucumberException(runtime.getErrors().get(0));
            }
        }
    }
}
