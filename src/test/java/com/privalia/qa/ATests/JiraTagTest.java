package com.privalia.qa.ATests;

import com.privalia.qa.utils.JiraConnector;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JiraTagTest {

    JiraConnector jc = new JiraConnector();

    @BeforeTest
    public void setUp() throws Exception {
        this.jc.transitionEntityToGivenStatus("QMS-990", "Done");
        String statusName = this.jc.getEntityStatus("QMS-990");
        assertThat(statusName).isEqualToIgnoringCase("Done");
    }

    @Test
    public void shouldGetDataFromPropertiesFile() {
        String jiraServerURL = this.jc.getProperty("jira.server.url");
        assertThat(jiraServerURL).isEqualTo("https://jira.vptech.eu");
    }

    @Test
    public void shouldUseSystemVariableIfPresent() {
        System.setProperty("jira.server.url", "http://dummy-server.com/");
        String jiraServerURL = this.jc.getProperty("jira.server.url");
        assertThat(jiraServerURL).isEqualTo("http://dummy-server.com/");
    }

    @Test
    public void shouldUseDefaultValueIfProvided() {
        String jiraServerURL = this.jc.getProperty("not.real.key:-test");
        assertThat(jiraServerURL).isEqualTo("test");
    }

    @Test
    public void shouldThrowExceptionIfVariableNotFound() {
        assertThatThrownBy(() -> {
            this.jc.getProperty("not.real.property.key");
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot resolve variable");
    }


    @Test(enabled = false)
    public void shouldRetrieveCorrectEntityStatus() throws Exception {
        String statusName = this.jc.getEntityStatus("QMS-990");
        assertThat(statusName).isEqualToIgnoringCase("Done");
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
    public void shouldReturnAllPossibleTransitionsForEntity() throws Exception {
        int transitionID = this.jc.getTransitionIDForEntityByName("QMS-990", "In Progress");
        assertThat(transitionID).isEqualTo(31);

        transitionID = this.jc.getTransitionIDForEntityByName("QMS-990", "Backlog");
        assertThat(transitionID).isEqualTo(11);

        transitionID = this.jc.getTransitionIDForEntityByName("QMS-990", "Done");
        assertThat(transitionID).isEqualTo(41);
    }


    @Test(enabled = false)
    public void shouldTransitionEntityToGivenStatus() throws Exception {
        this.jc.transitionEntityToGivenStatus("QMS-990", "In Progress");
        String statusName = this.jc.getEntityStatus("QMS-990");
        assertThat(statusName).isEqualToIgnoringCase("In Progress");
    }

    @Test(enabled = false)
    public void shouldTransitionEntity() throws Exception {
        this.jc.transitionEntity("QMS-990");
        String statusName = this.jc.getEntityStatus("QMS-990");
        assertThat(statusName).isEqualToIgnoringCase("In Progress");
    }

    @Test(enabled = false)
    public void shouldAddANewCommentToEntity() throws Exception {
        this.jc.postCommentToEntity("QMS-990", "This is a test message");
    }
}
