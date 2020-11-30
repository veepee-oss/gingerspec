package com.privalia.qa.aspects;

import gherkin.pickles.PickleTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Controls if a scenario should run depending on its tags. Currently, the only two tags that could alter
 * the execution of a scenario are the @ignore, @runOnEnv and @skipOnEnv
 *
 * @author Jose Fernandez
 */
public class executionTags {

    private List<String> tags;

    private String scenarioName;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    public enum ignoreReasons { NOTIGNORED, ENVCONDITION, UNIMPLEMENTED, MANUAL, TOOCOMPLEX, JIRATICKET, NOREASON }

    public executionTags() {
    }

    public executionTags(List<PickleTag> tags, String scenarioName) {
        this.tags = tags.stream().map(PickleTag::getName).collect(Collectors.toList());
        this.scenarioName = scenarioName;
    }

    /**
     * Verifies if the given tag is present in the {@link PickleTag} array
     *
     * @param tags    The {@link PickleTag} array array
     * @param tagName The tag to search in string (i.e @mytag)
     * @return true if theres at least one {@link PickleTag} in the array with that name
     */
    private boolean containsTag(List<PickleTag> tags, String tagName) {
        for (PickleTag pickleTag : tags) {
            if (pickleTag.getName().toLowerCase().matches(tagName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Controls if the scenario should be executed or ignored depending on its tags
     *
     * @return true if the scenario should be executed, false otherwise
     */
    public boolean shouldRun() {

        try {

            if (tags.contains("@ignore")) {
                ignoreReasons ir = this.printIgnoreReason(this.tags);
                if (ir == ignoreReasons.NOREASON) {
                    logger.warn("Scenario '" + scenarioName + "' ignored. No reason specified");
                }
                return false;
            }


        } catch (Exception e) {
            logger.error("Scenario '" + this.scenarioName + "' skipped because of an internal error:" + e.getMessage());
            return false;
        }

        return true;

    }

    public ignoreReasons printIgnoreReason(List<String> tags) {

        for (String tag : tags) {
            Pattern pattern = Pattern.compile("@tillfixed\\((.*?)\\)");
            Matcher matcher = pattern.matcher(tag.toLowerCase());
            if (matcher.find()) {
                String ticket = matcher.group(1);
                logger.warn("Scenario '" + scenarioName + "' ignored because of ticket: " + ticket);
                return ignoreReasons.JIRATICKET;
            }
        }

        if (tags.contains("@envCondition")) {
            return ignoreReasons.ENVCONDITION;

        }
        if (tags.contains("@unimplemented")) {
            logger.warn("Scenario '" + scenarioName + "' ignored because it is not yet implemented.");
            return ignoreReasons.UNIMPLEMENTED;

        }
        if (tags.contains("@manual")) {
            logger.warn("Scenario '" + scenarioName + "' ignored because it is marked as manual test.");
            return ignoreReasons.MANUAL;

        }
        if (tags.contains("@toocomplex")) {
            logger.warn("Scenario '" + scenarioName + "' ignored because the test is too complex.");
            return ignoreReasons.TOOCOMPLEX;
        }

        return ignoreReasons.NOREASON;
    }

}
