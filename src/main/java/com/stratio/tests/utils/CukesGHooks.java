package com.stratio.tests.utils;

import com.stratio.specs.BaseGSpec;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stratio.cucumber.testng.ICucumberFormatter;
import com.stratio.cucumber.testng.ICucumberReporter;

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