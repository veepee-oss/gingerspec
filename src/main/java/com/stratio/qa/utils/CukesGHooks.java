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
package com.stratio.qa.utils;

import com.stratio.qa.cucumber.testng.ICucumberFormatter;
import com.stratio.qa.cucumber.testng.ICucumberReporter;
import com.stratio.qa.specs.BaseGSpec;
import gherkin.formatter.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CukesGHooks extends BaseGSpec implements ICucumberReporter, ICucumberFormatter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    Feature feature;
    Scenario scenario;

    public CukesGHooks() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    @Override
    public void uri(String uri) {
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
    }

    @Override
    public void done() {
    }

    @Override
    public void close() {
    }

    @Override
    public void eof() {
    }

    @Override
    public void background(Background background) {
        logger.info("Background: {}", background.getName());
    }

    @Override
    public void feature(Feature feature) {
        this.feature = feature;
        ThreadProperty.set("feature", feature.getName());
    }

    @Override
    public void scenario(Scenario scenario) {
        this.scenario = scenario;
        logger.info("Feature/Scenario: {}/{} ", feature.getName(), scenario.getName());
        ThreadProperty.set("scenario", scenario.getName());
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    @Override
    public void step(Step step) {
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        logger.info(""); //empty line to split scenarios
    }

    @Override
    public void before(Match match, Result result) {
    }

    @Override
    public void result(Result result) {
    }

    @Override
    public void after(Match match, Result result) {
    }

    @Override
    public void match(Match match) {
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
    }

    @Override
    public void write(String text) {
    }

}