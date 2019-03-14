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
package com.privalia.qa.aspects;

import com.privalia.qa.exceptions.NonReplaceableException;
import com.privalia.qa.utils.ThreadProperty;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

public class ReplacementAspectTest {

    @Test
    public void replaceEmptyPlaceholdersTest() throws NonReplaceableException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;
        assertThat(repAspect.replaceEnvironmentPlaceholders("", pjp)).as("Replacing an empty placeholded string should not modify it").isEqualTo("");
    }

    @Test
    public void replaceSinglePlaceholdersTest() throws NonReplaceableException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;
        System.setProperty("DUMMYBDD_ENV1", "33");
        System.setProperty("DUMMYBDD_ENV2", "aa");

        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}", pjp))
                .as("Unexpected replacement").isEqualTo("33");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2}", pjp))
                .as("Unexpected replacement").isEqualTo("33aa");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}:${DUMMYBDD_ENV2}", pjp))
                .as("Unexpected replacement").isEqualTo("33:aa");
        assertThat(repAspect.replaceEnvironmentPlaceholders("|${DUMMYBDD_ENV1}|:|${DUMMYBDD_ENV2}|", pjp))
                .as("Unexpected replacement").isEqualTo("|33|:|aa|");
    }

    @Test
    public void replaceSinglePlaceholderCaseTest() throws NonReplaceableException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;
        System.setProperty("DUMMYBDD_ENV1", "33");
        System.setProperty("DUMMYBDD_ENV2", "aA");

        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1.toUpper}", pjp)).as("Unexpected replacement").isEqualTo("33");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1.toLower}", pjp)).as("Unexpected replacement").isEqualTo("33");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV2.toUpper}", pjp)).as("Unexpected replacement").isEqualTo("AA");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV2.toLower}", pjp)).as("Unexpected replacement").isEqualTo("aa");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2.toLower}", pjp)).as("Unexpected replacement").isEqualTo("33aa");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}:${DUMMYBDD_ENV2.toUpper}", pjp)).as("Unexpected replacement").isEqualTo("33:AA");
        assertThat(repAspect.replaceEnvironmentPlaceholders("|${DUMMYBDD_ENV2}.toUpper", pjp)).as("Unexpected replacement").isEqualTo("|aA.toUpper");
    }

    @Test
    public void replaceElementPlaceholderCaseTest() throws NonReplaceableException, FileNotFoundException, ConfigurationException, URISyntaxException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;
        System.setProperty("DUMMYBDD_ENV4", "33");
        System.setProperty("DUMMYBDD_ENV5", "aA");

        assertThat(repAspect.replacedElement("${DUMMYBDD_ENV4}", pjp)).isEqualTo("33");
        assertThat(repAspect.replacedElement("${DUMMYBDD_ENV5.toLower}", pjp)).isEqualTo("aa");
        assertThat(repAspect.replacedElement("${DUMMYBDD_ENV5.toUpper}", pjp)).isEqualTo("AA");
        assertThat(repAspect.replacedElement("${DUMMYBDD_ENV5}", pjp)).isEqualTo("aA");
        assertThat(repAspect.replacedElement("${DUMMYBDD_ENV4}${DUMMYBDD_ENV5}", pjp)).isEqualTo("33aA");
        assertThat(repAspect.replacedElement("${DUMMYBDD_ENV4}:${DUMMYBDD_ENV5}", pjp)).isEqualTo("33:aA");
    }
    @Test
    public void replaceReflectionPlaceholderCaseTest() throws NonReplaceableException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;

        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repAspect.replaceReflectionPlaceholders("!{NO_VAL}", pjp));
    }

    @Test
    public void replaceCodePlaceholderCaseTest() throws NonReplaceableException {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ReplacementAspect repAspect = new ReplacementAspect();
        ProceedingJoinPoint pjp = null;

        assertThat(repAspect.replaceCodePlaceholders("@{schemas/simple1.json}", pjp)).isEqualTo("");
        assertThat(repAspect.replaceCodePlaceholders("@{JSON.schemas/simple1.json}", pjp)).isEqualTo("{\"a\":true}");
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repAspect.replaceCodePlaceholders("@{IP.10.10.10.10}", pjp));
    }

    @Test
    public void replaceMixedPlaceholdersTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ThreadProperty.set("DUMMYBDD_LOCAL1", "LOCAL");
        ProceedingJoinPoint pjp = null;
        ReplacementAspect repAspect = new ReplacementAspect();
        System.setProperty("DUMMYBDD_ENV2", "aa");

        assertThat(repAspect.replaceReflectionPlaceholders(repAspect.replaceEnvironmentPlaceholders("!{DUMMYBDD_LOCAL1}:${DUMMYBDD_ENV2}", pjp), pjp))
                .as("Unexpected replacement").isEqualTo("LOCAL:aa");
        assertThat(repAspect.replaceReflectionPlaceholders(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV2}:!{DUMMYBDD_LOCAL1}", pjp), pjp))
                .as("Unexpected replacement").isEqualTo("aa:LOCAL");
    }

    @Test
    public void replaceDefaultValue() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        ProceedingJoinPoint pjp = null;
        ReplacementAspect repAspect = new ReplacementAspect();
        System.setProperty("DUMMYBDD_ENV1", "aa");
        System.setProperty("DUMMYBDD_ENV3", "cc");

        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1:-bb}", pjp)).as("Unexpected replacement").isEqualTo("aa");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV2:-bb}", pjp)).as("Unexpected replacement").isEqualTo("bb");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV4:-dd}", pjp)).as("Unexpected replacement").isEqualTo("bbdd");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV1}", pjp)).as("Unexpected replacement").isEqualTo("bbaa");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}", pjp)).as("Unexpected replacement").isEqualTo("aabb");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV3}", pjp)).as("Unexpected replacement").isEqualTo("aabbcc");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1.toUpper}${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV3}", pjp)).as("Unexpected replacement").isEqualTo("AAbbcc");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2.toUpper:-bb}${DUMMYBDD_ENV3}", pjp)).as("Unexpected replacement").isEqualTo("aaBBcc");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV3:-aa}", pjp)).as("Unexpected replacement").isEqualTo("aabbcc");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV3.toUpper:-aa}", pjp)).as("Unexpected replacement").isEqualTo("aabbCC");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb.bb}${DUMMYBDD_ENV3:-aa}", pjp)).as("Unexpected replacement").isEqualTo("aabb.bbcc");
        assertThat(repAspect.replaceEnvironmentPlaceholders("${DUMMYBDD_ENV1}${DUMMYBDD_ENV2:-bb}${DUMMYBDD_ENV3:-aa.aa}", pjp)).as("Unexpected replacement").isEqualTo("aabbcc");
    }
}