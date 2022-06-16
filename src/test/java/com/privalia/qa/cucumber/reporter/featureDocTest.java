package com.privalia.qa.cucumber.reporter;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class featureDocTest {

    String featureBody;

    @BeforeMethod
    public void setUp() throws IOException {
        this.featureBody = FileUtils.readFileToString(new File("src/test/resources/testFeature.feature"), StandardCharsets.UTF_8);
        Assert.assertNotNull(this.featureBody);
    }


    @Test
    public void shouldCorrectlyReadTheNameOfTheFeature() {
        featureDoc featureDoc = new featureDoc(this.featureBody);
        String featureName = featureDoc.getFeatureName();
        Assert.assertEquals(featureName, "test feature");
    }

    @Test
    public void shouldGetFeatureDescription() {
        featureDoc featureDoc = new featureDoc(this.featureBody);
        String featureDescription = featureDoc.getFeatureDescription();
        Assert.assertTrue(featureDescription.contains("First line"));
        Assert.assertTrue(featureDescription.contains("Second line"));
    }

    @Test
    public void shouldReturnCorrectRuleCount() {
        featureDoc featureDoc = new featureDoc(this.featureBody);
        Map<String, Map> featureRules = featureDoc.getFeatureRules();
        Assert.assertEquals(featureRules.size(), 2);
    }

    @Test
    public void shouldReturnCorrectNumberOfScenariosPerRule() {
        featureDoc featureDoc = new featureDoc(this.featureBody);
        Map<String, Map> featureRules = featureDoc.getFeatureRules();
        Assert.assertEquals(featureRules.get("Specifying Request Data").size(), 5);
        Assert.assertEquals(featureRules.get("Verifying Response Data").size(), 2);
    }

    @Test
    public void shouldCorrectlyReturnScenarioWithDocString() {
        featureDoc featureDoc = new featureDoc(this.featureBody);
        Map<String, Map> featureRules = featureDoc.getFeatureRules();
        Map<String, String> scenarios = featureRules.get("Specifying Request Data");
        String scenarioDocString = scenarios.get("Replacements in a DocString");
        Assert.assertTrue(scenarioDocString.contains("Test1"));
        Assert.assertTrue(scenarioDocString.contains("Test2"));
        Assert.assertTrue(scenarioDocString.contains("Test3"));
    }

    @Test
    public void shouldCorrectlyReturnScenarioOutline() {
        featureDoc featureDoc = new featureDoc(this.featureBody);
        Map<String, Map> featureRules = featureDoc.getFeatureRules();
        Map<String, String> scenarios = featureRules.get("Specifying Request Data");
        String scenarioDocString = scenarios.get("With scenarios outlines in examples table");
        Assert.assertTrue(scenarioDocString.contains("Example"));
    }

    @Test
    public void shouldReturnAllExpectedDataFromScenarios() {
        featureDoc featureDoc = new featureDoc(this.featureBody);
        Map<String, Map> featureRules = featureDoc.getFeatureRules();
        Map<String, String> firstRuleScenarios = featureRules.get("Specifying Request Data");
        String scenario1 = firstRuleScenarios.get("Invoking HTTP resources (GET, POST, DELETE, PATCH, UPDATE)");
        String scenario2 = firstRuleScenarios.get("Adding cookies");
        String scenario3 = firstRuleScenarios.get("With scenarios outlines in examples table");
        Assert.assertTrue(scenario1.contains("@rest"));
        Assert.assertTrue(scenario2.contains("@ignore"));
        Assert.assertTrue(scenario3.contains("Examples"));

        Map<String, String> secondRuleScenarios = featureRules.get("Verifying Response Data");
        scenario1 = secondRuleScenarios.get("Verify the response body contains specific text");
        Assert.assertTrue(scenario1.contains("#comment"));
        Assert.assertTrue(scenario1.contains("#comment2"));
        Assert.assertTrue(scenario1.contains("@rest"));

        scenario2 = secondRuleScenarios.get("Verify response status code");
        Assert.assertTrue(scenario2.contains("@rest"));
        Assert.assertTrue(scenario2.contains("@ignore"));
    }
}