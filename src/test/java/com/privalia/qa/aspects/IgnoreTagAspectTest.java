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

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IgnoreTagAspectTest {

    public IgnoreTagAspect ignoretag = new IgnoreTagAspect();

    @Test
    public void testJiraTicket() throws Exception {
        List<String> tagList = new ArrayList<>();
        tagList.add(0, "@ignore");
        tagList.add(1, "@tillfixed(XXX-123)");
        String scnName = "Jira ticket ignore";
        IgnoreTagAspect.ignoreReasons exit = IgnoreTagAspect.ignoreReasons.JIRATICKET;

        assertThat(exit).as("Scenario 'Jira ticket ignore' ignored because of ticket: XXX-123").isEqualTo(ignoretag.manageTags(tagList,scnName));
    }

    @Test
    public void testManual() throws Exception {
        List<String> tagList = new ArrayList<>();
        tagList.add(0, "@ignore");
        tagList.add(1, "@manual");
        String scnName = "Manual ignore";
        IgnoreTagAspect.ignoreReasons exit = IgnoreTagAspect.ignoreReasons.MANUAL;

        assertThat(exit).as("Scenario 'Manual ignore' ignored because it is marked as manual test.").isEqualTo(ignoretag.manageTags(tagList,scnName));
    }

    @Test
    public void testTooComplex() throws Exception {
        List<String> tagList = new ArrayList<>();
        tagList.add(0, "@ignore");
        tagList.add(1, "@toocomplex");
        String scnName = "Too complex ignore";
        IgnoreTagAspect.ignoreReasons exit = IgnoreTagAspect.ignoreReasons.TOOCOMPLEX;

        assertThat(exit).as("Scenario 'Too complex ignore' ignored because the test is too complex.").isEqualTo(ignoretag.manageTags(tagList,scnName));
    }

    @Test
    public void testRunOnEnvs() throws Exception {
        List<String> tagList = new ArrayList<>();
        tagList.add(0, "@ignore");
        tagList.add(1, "@envCondition");
        String scnName = "Condition ignore";
        IgnoreTagAspect.ignoreReasons exit = IgnoreTagAspect.ignoreReasons.ENVCONDITION;

        assertThat(exit).isEqualTo(ignoretag.manageTags(tagList,scnName));
    }

    @Test
    public void testUnimplemented() throws Exception {
        List<String> tagList = new ArrayList<>();
        tagList.add(0, "@ignore");
        tagList.add(1, "@unimplemented");
        String scnName = "Unimplemented ignore";
        IgnoreTagAspect.ignoreReasons exit = IgnoreTagAspect.ignoreReasons.UNIMPLEMENTED;

        assertThat(exit).as("Scenario 'Unimplemented ignore' ignored because it is not yet implemented.").isEqualTo(ignoretag.manageTags(tagList,scnName));
    }

    @Test
    public void testNotKnownReason() throws Exception {
        List<String> tagList = new ArrayList<>();
        tagList.add(0, "@ignore");
        tagList.add(1, "@hellomyfriend");
        String scnName = "Not known reason ignore";
        IgnoreTagAspect.ignoreReasons exit = IgnoreTagAspect.ignoreReasons.NOREASON;

        assertThat(exit).as("Scenario 'test ignore in scenario' ignored because of ticket: XXX-123").isEqualTo(ignoretag.manageTags(tagList,scnName));
    }

}
