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

package com.privalia.qa.utils;

import com.privalia.qa.cucumber.testng.CucumberOptionsImpl;
import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * This is a custom implementation of {@link AbstractTestNGCucumberTests} for adding special configuration
 * of GingerSpec to the {@link CucumberOptions} annotation of the class
 *
 * Test classes must extend this class in order to be executed with TestNG and use the Gingerspec steps
 * and other functionality
 *
 * @author Jose Fernandez
 */
abstract public class BaseGTest extends AbstractTestNGCucumberTests {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    protected String browser = "";

    /**
     * Method executed before a suite.
     *
     * Before the test is executed, the library modifies the {@link CucumberOptions} annotation
     * in the runner class  to include some special configuration for GingerSpec.
     *
     * @param context                       context
     * @throws NoSuchMethodException        NoSuchMethodException
     * @throws InvocationTargetException    InvocationTargetException
     * @throws IllegalAccessException       IllegalAccessException
     * @throws NoSuchFieldException         NoSuchFieldException
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeGSuite(ITestContext context) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {

        CucumberOptionsImpl newAnnotation = new CucumberOptionsImpl(this.getClass());

        Method method = Class.class.getDeclaredMethod("annotationData", null);
        method.setAccessible(true);
        //Since AnnotationData is a private class we cannot create a direct reference to it. We will have to
        //manage with just Object
        Object annotationData = method.invoke(this.getClass());
        //We now look for the map called "annotations" within AnnotationData object.
        Field annotations = annotationData.getClass().getDeclaredField("annotations");
        annotations.setAccessible(true);
        Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) annotations.get(annotationData);
        map.put(CucumberOptions.class, newAnnotation);
    }

    /**
     * Method executed after a suite.
     *
     * @param context the context
     */
    @AfterSuite(alwaysRun = true)
    public void afterGSuite(ITestContext context) {
        logger.info("Done executing this test-run.");
    }

    /**
     * Method executed before a test class.
     *
     * @param context the context
     */
    @BeforeClass(alwaysRun = true)
    public void beforeGClass(ITestContext context) {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
    }

    /**
     * Method executed after a test method.
     *
     * @param method the method
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeGMethod(Method method) {
        ThreadProperty.set("browser", this.browser);
    }

    /**
     * Method executed before method.
     *
     * @param method the method
     */
    @AfterMethod(alwaysRun = true)
    public void afterGMethod(Method method) {
    }

    /**
     * Method executed before a class.
     */
    @AfterClass()
    public void afterGClass() {
    }

}
