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

package com.privalia.qa.ATests;

import com.privalia.qa.utils.JiraConnector;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class JiraTagTest {

    JiraConnector jc = new JiraConnector();

    @BeforeTest(enabled = false)
    public void setUp() throws Exception {
        System.setProperty("jira.transition.if.fail.status", "Done");
        this.jc.transitionEntity("QMS-990");
        System.clearProperty("jira.transition.if.fail.status");
    }

    @Test(enabled = false)
    public void shouldReturnTrueIfEntitiyStatusMatchesRunnableStatuses() throws Exception {
        Boolean shouldRun = this.jc.entityShouldRun("QMS-990");
        assertThat(shouldRun).isTrue();
    }

    @Test(enabled = false)
    public void shouldReturnFalseIfEntitiyStatusDifferentFronRunnableStatuses() throws Exception {
        System.setProperty("jira.valid.runnable.statuses", "READY,QA READY,DEPLOYED");
        Boolean shouldRun = this.jc.entityShouldRun("QMS-990");
        assertThat(shouldRun).isFalse();
        System.clearProperty("jira.valid.runnable.statuses");
    }

    @Test(enabled = false)
    public void shouldAddANewCommentToEntity() throws Exception {
        this.jc.postCommentToEntity("QMS-990", "This is a test message");
    }

    @Test(enabled = false)
    public void shouldReturnExceptionIkFeyVariablesNotFound() {
        assertThatThrownBy(() -> {
            System.setProperty("jira.server.url", null);
            this.jc.transitionEntity("QMS-990");
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldReturnTheTicketFromTheTag() throws Exception {
        List<String> tags = Arrays.asList("@jira(QMS-990)", "@ignore", "@jira(QMS-123)");
        assertThat("QMS-990").isEqualToIgnoringCase(this.jc.getFirstTicketReference(tags));

        tags = Arrays.asList("@jira[QMS-990]", "@ignore", "@jira(QMS-123)");
        assertThat("QMS-990").isEqualToIgnoringCase(this.jc.getFirstTicketReference(tags));

        tags = Arrays.asList("@jira[QMS-123]", "@ignore", "@jira(QMS-990)");
        assertThat("QMS-123").isEqualToIgnoringCase(this.jc.getFirstTicketReference(tags));

        tags = Arrays.asList("@ignore", "@jira(QMS-123)");
        assertThat("QMS-123").isEqualToIgnoringCase(this.jc.getFirstTicketReference(tags));

        tags = Arrays.asList("@ignore");
        assertThat(this.jc.getFirstTicketReference(tags)).isNull();
    }
}
