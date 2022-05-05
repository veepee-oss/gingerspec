package com.privalia.qa.cucumber.reporter.templates;

import io.cucumber.plugin.event.TestSourceRead;
import org.apache.commons.lang3.RegExUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class featureDoc {

    String featureName;

    Map<String, Map> featureRules;

    String[] ruleSet;

    TestSourceRead event;

    public featureDoc(TestSourceRead event) {
        this.featureName = this.getPattern(event.getSource(), "^Feature: (.*?)[\\n\\r]").get(0);
        this.featureRules = this.parseFeatureRules(event);
    }

    public String getFeatureName() {
        return this.featureName;
    }

    public Map<String, Map> getFeatureRules() {
        return featureRules;
    }

    public Map<String, Map> parseFeatureRules(TestSourceRead event) {

        String featureBody = event.getSource();

        Map<String, Map> featureRules = new LinkedHashMap<>();

        this.featureName = this.getPattern(featureBody, "^Feature: (.*?)[\\n\\r]").get(0);

        String[] rules = featureBody.split("(?=Rule:)");

        for (int i = 0; i <= rules.length - 1; i++) {

            List<String> matches = this.getPattern(rules[i],"^Rule: (.*?)[\\n\\r]");

            if (!matches.isEmpty()) {

                String ruleName = matches.get(0);

                Map<String, String> ruleScenarios = new HashMap<>();

                String ruleBody = RegExUtils.removePattern(rules[i], "^Rule: (.*?)[\\n\\r]");

                String[] scenarios = ruleBody.split("(?=Scenario:|Scenario Outline:|@)");
                for (String scenarioBody: scenarios) {

                    if (scenarioBody.contains("Scenario: ")) {
                        String scenarioName = this.getPattern(scenarioBody, "^Scenario: (.*?)[\\n\\r]").get(0);
                        ruleScenarios.put(scenarioName, scenarioBody);
                    }

                    if (scenarioBody.contains("Scenario Outline: ")) {
                        String scenarioName = this.getPattern(scenarioBody, "^Scenario Outline: (.*?)[\\n\\r]").get(0);
                        ruleScenarios.put(scenarioName, scenarioBody);
                    }
                }
                featureRules.put(ruleName, ruleScenarios);
            }
        }

        return featureRules;

    }

    public List<String> getRuleSet() {
        List<String> ruleList = new ArrayList<>();
        for (String ruleName : this.featureRules.keySet()) {
            ruleList.add(ruleName);
        }
        return ruleList;
    }

    public List<String> getPattern(String source, String regex) {
        List<String> groups = new LinkedList<String>();
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(source);

        while (matcher.find()) {

            for (int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(matcher.group(i));
            }
        }
        return groups;
    }
}
