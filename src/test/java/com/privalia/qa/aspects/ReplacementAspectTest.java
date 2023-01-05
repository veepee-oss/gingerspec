/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
*/

package com.privalia.qa.aspects;

import com.privalia.qa.exceptions.NonReplaceableException;
import com.privalia.qa.utils.ThreadProperty;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReplacementAspectTest {

    @Test
    public void replaceEmptyPlaceholdersTest() throws NonReplaceableException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;
        assertThat(repAspect.replacePlaceholders("", true)).as("Replacing an empty placeholded string should not modify it").isEqualTo("");
    }

    @Test
    public void replaceSinglePlaceholdersTest() throws NonReplaceableException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;
        System.setProperty("DUMMYBDD_ENV1", "33");
        System.setProperty("DUMMYBDD_ENV2", "aa");

        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}", true))
                .as("Unexpected replacement").isEqualTo("33");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2}", true))
                .as("Unexpected replacement").isEqualTo("33aa");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}:${DUMMYBDD_ENV2}", true))
                .as("Unexpected replacement").isEqualTo("33:aa");
        assertThat(repAspect.replacePlaceholders("|${DUMMYBDD_ENV1}|:|${DUMMYBDD_ENV2}|", true))
                .as("Unexpected replacement").isEqualTo("|33|:|aa|");
    }

    @Test
    public void replaceSinglePlaceholderCaseTest() throws NonReplaceableException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;
        System.setProperty("DUMMYBDD_ENV1", "33");
        System.setProperty("DUMMYBDD_ENV2", "aA");

        assertThat(repAspect.replacePlaceholders("${toUpperCase:${DUMMYBDD_ENV1}}", true)).as("Unexpected replacement").isEqualTo("33");
        assertThat(repAspect.replacePlaceholders("${toLowerCase:${DUMMYBDD_ENV1}}", true)).as("Unexpected replacement").isEqualTo("33");
        assertThat(repAspect.replacePlaceholders("${toUpperCase:${DUMMYBDD_ENV2}}", true)).as("Unexpected replacement").isEqualTo("AA");
        assertThat(repAspect.replacePlaceholders("${toLowerCase:${DUMMYBDD_ENV2}}", true)).as("Unexpected replacement").isEqualTo("aa");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}${toLowerCase:${DUMMYBDD_ENV2}}", true)).as("Unexpected replacement").isEqualTo("33aa");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}:${toUpperCase:${DUMMYBDD_ENV2}}", true)).as("Unexpected replacement").isEqualTo("33:AA");
        assertThat(repAspect.replacePlaceholders("|${toUpperCase:${DUMMYBDD_ENV2}}", true)).as("Unexpected replacement").isEqualTo("|AA");
    }

    @Test
    public void replaceElementPlaceholderCaseTest() throws NonReplaceableException, FileNotFoundException, ConfigurationException, URISyntaxException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;
        System.setProperty("DUMMYBDD_ENV4", "33");
        System.setProperty("DUMMYBDD_ENV5", "aA");

        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV4}", true)).isEqualTo("33");
        assertThat(repAspect.replacePlaceholders("${toLowerCase:${DUMMYBDD_ENV5}}", true)).isEqualTo("aa");
        assertThat(repAspect.replacePlaceholders("${toUpperCase:${DUMMYBDD_ENV5}}", true)).isEqualTo("AA");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV5}", true)).isEqualTo("aA");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV4}${DUMMYBDD_ENV5}", true)).isEqualTo("33aA");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV4}:${DUMMYBDD_ENV5}", true)).isEqualTo("33:aA");
    }

    @Test
    public void replaceDefaultValue() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ProceedingJoinPoint pjp = null;
        ReplacementAspect repAspect = new ReplacementAspect();
        System.setProperty("DUMMYBDD_ENV1", "aa");
        System.setProperty("DUMMYBDD_ENV3", "cc");

        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1:-bb}", true)).as("Unexpected replacement").isEqualTo("aa");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV2:-bb}", true)).as("Unexpected replacement").isEqualTo("bb");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV4:-dd}", true)).as("Unexpected replacement").isEqualTo("bbdd");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV1}", true)).as("Unexpected replacement").isEqualTo("bbaa");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}", true)).as("Unexpected replacement").isEqualTo("aabb");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV3}", true)).as("Unexpected replacement").isEqualTo("aabbcc");
        assertThat(repAspect.replacePlaceholders("${toUpperCase:${DUMMYBDD_ENV1}}${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV3}", true)).as("Unexpected replacement").isEqualTo("AAbbcc");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}${toUpperCase:${DUMMYBDD_ENV2:-bb}}${DUMMYBDD_ENV3}", true)).as("Unexpected replacement").isEqualTo("aaBBcc");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV3:-aa}", true)).as("Unexpected replacement").isEqualTo("aabbcc");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}${toUpperCase:${DUMMYBDD_ENV3:-aa}}", true)).as("Unexpected replacement").isEqualTo("aabbCC");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb.bb}${DUMMYBDD_ENV3:-aa}", true)).as("Unexpected replacement").isEqualTo("aabb.bbcc");
        assertThat(repAspect.replacePlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV3:-aa.aa}", true)).as("Unexpected replacement").isEqualTo("aabbcc");
    }

    @Test
    public void replaceEnvProperty() throws NonReplaceableException {
        ProceedingJoinPoint pjp = null;
        ReplacementAspect repAspect = new ReplacementAspect();

        assertThat(repAspect.replacePlaceholders("${envProperties:wait.time}", true)).as("Unexpected replacement").isEqualTo("1");
        assertThatThrownBy(() -> { repAspect.replacePlaceholders("${envProperties:invalid.property}", true); })
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Cannot resolve variable 'envProperties:invalid.property");

        System.setProperty("env", "pre");
        assertThat(repAspect.replacePlaceholders("${envProperties:wait.time}", true)).as("Unexpected replacement").isEqualTo("2");

    }
}