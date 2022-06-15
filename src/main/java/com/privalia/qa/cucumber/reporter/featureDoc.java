package com.privalia.qa.cucumber.reporter;

import io.cucumber.plugin.event.TestSourceRead;
import org.apache.commons.lang3.RegExUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A java class that contains a representation of the feature file. It is used by the
 * {@link gingerHtmlFormatter} class to extract the elements of the feature and build the HTML
 * page
 *
 * @author Jose Fernandez
 */
public class featureDoc {

    String featureName;

    String featureDescription;

    Map<String, Map> featureRules;

    String[] ruleSet;

    public featureDoc(String featureBody) {
        this.featureName = this.getPattern(featureBody, "^Feature: (.*?)[\\n\\r]").get(0);
        this.featureRules = this.parseFeatureRules(featureBody);
        this.featureDescription = this.parseFeatureDescription(featureBody);
    }

    public String getFeatureName() {
        return this.featureName;
    }

    public String getFeatureDescription() { return featureDescription; }

    public Map<String, Map> getFeatureRules() {
        return featureRules;
    }

    public Map<String, Map> parseFeatureRules(String featureBody) {

        Map<String, Map> featureRules = new LinkedHashMap<>();

        String[] rules = featureBody.split("(?=Rule:)");

        for (int i = 0; i <= rules.length - 1; i++) {

            List<String> matches = this.getPattern(rules[i], "^Rule: (.*?)[\\n\\r]");

            if (!matches.isEmpty()) {

                String ruleName = matches.get(0);

                Map<String, String> ruleScenarios = new HashMap<>();

                String ruleBody = RegExUtils.removePattern(rules[i], "^Rule: (.*?)[\\n\\r]");

                List<String> scenarios = this.parseScenariosFromRuleBody(ruleBody);
                for (String scenarioBody: scenarios) {

                    if (scenarioBody.contains("Scenario: ")) {
                        String scenarioName = this.getPattern(scenarioBody, "Scenario: (.*?)[\\n\\r]").get(0);
                        ruleScenarios.put(scenarioName, scenarioBody);
                    }

                    if (scenarioBody.contains("Scenario Outline: ")) {
                        String scenarioName = this.getPattern(scenarioBody, "Scenario Outline: (.*?)[\\n\\r]").get(0);
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

    public String parseFeatureDescription(String featureBody) {
        String description = "";
        boolean takeline = false;
        String[] lines = featureBody.split("\n");
        for (String line: lines) {
            if (line.trim().startsWith("Feature:")) {
                takeline = true;
            } else if (takeline && (line.trim().startsWith("Rule:") || line.trim().startsWith("Scenario:") || line.trim().startsWith("@") || line.trim().startsWith("#"))) {
                takeline = false;
                break;
            } else {
                if (takeline) {
                    description = description + line + "\n";
                }
            }
        }
        return description;
    }

    private List<String> parseScenariosFromRuleBody(String ruleBody) {

        String[] scenarios = ruleBody.split("(?m)^\\s*$");
        List<String> finalScenarioList = new LinkedList<>();

        for (int i = 0; i <= scenarios.length - 1; i++) {
            if (scenarios[i].contains("Scenario") && !scenarios[i].contains("Scenario Outline")) {
                finalScenarioList.add(scenarios[i].replaceAll("([\\n\\r]+\\s*)*$", ""));
            }

            if (scenarios[i].contains("Scenario Outline") && scenarios[i+1].contains("Example")) {
                finalScenarioList.add(scenarios[i] + scenarios[i+1].replaceAll("([\\n\\r]+\\s*)*$", ""));
            }
        }

        return finalScenarioList;

    }
}
