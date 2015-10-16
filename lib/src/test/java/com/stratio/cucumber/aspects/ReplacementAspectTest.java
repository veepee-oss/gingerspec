package com.stratio.cucumber.aspects;

import static org.assertj.core.api.Assertions.assertThat;
import org.testng.annotations.Test;
import com.stratio.tests.utils.ThreadProperty;

public class ReplacementAspectTest {

    @Test
    public void replaceEmptyPlaceholdersTest() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        assertThat(repAspect.replacePlaceholders("")).as("Replacing an empty placeholded string should not modify it").isEqualTo("");
    }

    @Test
    public void replaceSinglePlaceholdersTest() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        System.setProperty("STRATIOBDD_ENV1", "33");
        System.setProperty("STRATIOBDD_ENV2", "aa");

        assertThat(repAspect.replacePlaceholders("${STRATIOBDD_ENV1}"))
        	.as("Unexpected replacement").isEqualTo("33");
        assertThat(repAspect.replacePlaceholders("${STRATIOBDD_ENV1}${STRATIOBDD_ENV2}"))
        	.as("Unexpected replacement").isEqualTo("33aa");
        assertThat(repAspect.replacePlaceholders("${STRATIOBDD_ENV1}:${STRATIOBDD_ENV2}"))
        	.as("Unexpected replacement").isEqualTo("33:aa");
        assertThat(repAspect.replacePlaceholders("|${STRATIOBDD_ENV1}|:|${STRATIOBDD_ENV2}|"))
        	.as("Unexpected replacement").isEqualTo("|33|:|aa|");
        assertThat(repAspect.replacePlaceholders("|${STRATIOBDD_ENV}|:|${STRATIOBDD_ENV2}|"))
        	.as("Unexpected replacement").isEqualTo("||:|aa|");
    }

    @Test
    public void replaceSinglePlaceholderCaseTest() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        System.setProperty("STRATIOBDD_ENV1", "33");
        System.setProperty("STRATIOBDD_ENV2", "aA");

        assertThat(repAspect.replacePlaceholders("${STRATIOBDD_ENV1.toUpper}")).as("Unexpected replacement").isEqualTo("33");
        assertThat(repAspect.replacePlaceholders("${STRATIOBDD_ENV1.toLower}")).as("Unexpected replacement").isEqualTo("33");
        assertThat(repAspect.replacePlaceholders("${STRATIOBDD_ENV2.toUpper}")).as("Unexpected replacement").isEqualTo("AA");
        assertThat(repAspect.replacePlaceholders("${STRATIOBDD_ENV2.toLower}")).as("Unexpected replacement").isEqualTo("aa");
        assertThat(repAspect.replacePlaceholders("${STRATIOBDD_ENV1}${STRATIOBDD_ENV2.toLower}")).as("Unexpected replacement").isEqualTo("33aa");
        assertThat(repAspect.replacePlaceholders("${STRATIOBDD_ENV1}:${STRATIOBDD_ENV2.toUpper}")).as("Unexpected replacement").isEqualTo("33:AA");
        assertThat(repAspect.replacePlaceholders("|${STRATIOBDD_ENV.toUpper}|:|${STRATIOBDD_ENV2}|")).as("Unexpected replacement").isEqualTo("||:|aA|");
        assertThat(repAspect.replacePlaceholders("|${STRATIOBDD_ENV2}.toUpper")).as("Unexpected replacement").isEqualTo("|aA.toUpper");
    }
}