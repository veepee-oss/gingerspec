package com.privalia.qa.ATests;

import com.privalia.qa.utils.JiraConnector;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JiraTagIT {

    JiraConnector jc = new JiraConnector();

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
    public void shouldThrowExceptionIfVariableNotFound() {
        assertThatThrownBy(() -> {
            this.jc.getProperty("not.real.property.key");
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot resolve variable");
    }


    @Test
    public void shouldRetrieveCorrectEntityStatus() throws ExecutionException, InterruptedException, IOException {
        String statusName = this.jc.getEntityStatus("QMS-990");
        assertThat(statusName).isEqualToIgnoringCase("Done");
    }

    @Test
    public void shouldReturnTrueIfEntitiyStatusMatchesRunnableStatuses() throws ExecutionException, InterruptedException, IOException {
        Boolean shouldRun = this.jc.entityShouldRun("QMS-990");
        assertThat(shouldRun).isTrue();
    }

    @Test
    public void shouldReturnFalseIfEntitiyStatusDifferentFronRunnableStatuses() throws ExecutionException, InterruptedException, IOException {
        System.setProperty("jira.valid.runnable.statuses", "READY,QA READY,DEPLOYED");
        Boolean shouldRun = this.jc.entityShouldRun("QMS-990");
        assertThat(shouldRun).isFalse();
        System.clearProperty("jira.valid.runnable.statuses");
    }


    @Test
    public void shouldTransitionEntityIfTestFails() {




    }
}
