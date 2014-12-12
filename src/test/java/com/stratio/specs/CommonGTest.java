package com.stratio.specs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import com.stratio.tests.utils.ThreadProperty;

public class CommonGTest {

    @Test
    public void replaceEmptyPlaceholdersTest() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        assertThat("Replacing an empty placeholded string should not modify it", commong.replacePlaceholders(""),
                equalTo(""));
    }

    @Test
    public void replaceSinglePlaceholdersTest() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        System.setProperty("STRATIOBDD_ENV1", "33");
        System.setProperty("STRATIOBDD_ENV2", "aa");

        assertThat("Unexpected replacement", commong.replacePlaceholders("${STRATIOBDD_ENV1}"), equalTo("33"));
        assertThat("Unexpected replacement", commong.replacePlaceholders("${STRATIOBDD_ENV1}${STRATIOBDD_ENV2}"),
                equalTo("33aa"));
        assertThat("Unexpected replacement", commong.replacePlaceholders("${STRATIOBDD_ENV1}:${STRATIOBDD_ENV2}"),
                equalTo("33:aa"));
        assertThat("Unexpected replacement", commong.replacePlaceholders("|${STRATIOBDD_ENV1}|:|${STRATIOBDD_ENV2}|"),
                equalTo("|33|:|aa|"));
        assertThat("Unexpected replacement", commong.replacePlaceholders("|${STRATIOBDD_ENV}|:|${STRATIOBDD_ENV2}|"),
                equalTo("||:|aa|"));
    }

    @Test
    public void replaceSinglePlaceholderCaseTest() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        System.setProperty("STRATIOBDD_ENV1", "33");
        System.setProperty("STRATIOBDD_ENV2", "aA");

        assertThat("Unexpected replacement", commong.replacePlaceholders("${STRATIOBDD_ENV1.toUpper}"), equalTo("33"));
        assertThat("Unexpected replacement", commong.replacePlaceholders("${STRATIOBDD_ENV1.toLower}"), equalTo("33"));

        assertThat("Unexpected replacement", commong.replacePlaceholders("${STRATIOBDD_ENV2.toUpper}"), equalTo("AA"));
        assertThat("Unexpected replacement", commong.replacePlaceholders("${STRATIOBDD_ENV2.toLower}"), equalTo("aa"));

        assertThat("Unexpected replacement",
                commong.replacePlaceholders("${STRATIOBDD_ENV1}${STRATIOBDD_ENV2.toLower}"), equalTo("33aa"));
        assertThat("Unexpected replacement",
                commong.replacePlaceholders("${STRATIOBDD_ENV1}:${STRATIOBDD_ENV2.toUpper}"), equalTo("33:AA"));

        assertThat("Unexpected replacement",
                commong.replacePlaceholders("|${STRATIOBDD_ENV.toUpper}|:|${STRATIOBDD_ENV2}|"), equalTo("||:|aA|"));

        assertThat("Unexpected replacement", commong.replacePlaceholders("|${STRATIOBDD_ENV2}.toUpper"),
                equalTo("|aA.toUpper"));
    }
}
