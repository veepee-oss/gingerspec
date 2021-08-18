package com.privalia.qa.ATests;

import com.privalia.qa.utils.SlackConnector;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SlackConnectorTest {

    SlackConnector sc = new SlackConnector();

    @Test
    public void shouldNotFindValidChannels() {

        List<String> tags = new ArrayList<>();
        tags.add("@rest");
        tags.add("@jira(QMS-123)");
        tags.add("@slack");
        tags.add("@slack()");

        List<String> channels = this.sc.getChannels(tags);
        Assert.assertTrue(channels.isEmpty());

    }

    @Test
    public void shouldReturnOneChannel() {
        List<String> tags = new ArrayList<>();
        tags.add("@rest");
        tags.add("@jira(QMS-123)");
        tags.add("@slack");
        tags.add("@slack(#channel1)");

        List<String> channels = this.sc.getChannels(tags);
        Assert.assertEquals(channels.size(), 1);
    }

    @Test
    public void shouldReturnFourChannel() {
        List<String> tags = new ArrayList<>();
        tags.add("@rest");
        tags.add("@jira(QMS-123)");
        tags.add("@slack");
        tags.add("@slack(#channel1,#channel2,#channel3,@jose.fernandez)");

        List<String> channels = this.sc.getChannels(tags);
        Assert.assertEquals(channels.size(), 4);
    }

    @Test(enabled = false)
    public void shouldSendMessageToAllChannelsWithPropertiesConfiguration() throws SlackApiException, IOException {

        String message = "This is a test message";
        List<String> tags = new ArrayList<>();
        tags.add("@rest");
        tags.add("@jira(QMS-123)");
        tags.add("@slack");
        tags.add("@slack(#qms-notifications)");

        List<String> channels = this.sc.getChannels(tags);
        List<ChatPostMessageResponse> responses = this.sc.sendMessageToChannels(channels, message);
        Assert.assertEquals(responses.size(), 1);
        Assert.assertTrue(responses.get(0).isOk());
    }
}