package com.privalia.qa.utils;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An small utility for posting messages in slack
 * @author José Fernández
 */
public class SlackConnector {

    public static final String SLACK_PROPERTIES_FILE = "slack.properties";

    final StringSubstitutor interpolator = StringSubstitutor.createInterpolator();

    /**
     * Reads the given key from the properties file. The system will try to locate the key first in
     * the maven variables (System.getProperty) and if not found will look for it in the properties file.
     * If the value is still not found it will return the default value (if provided) or an exception
     * @param property      key
     * @param defaultValue  defaultValue
     * @return              value
     */
    private String getProperty(String property, String defaultValue) {

        if (System.getProperty(property) != null) {
            return System.getProperty(property);
        }

        interpolator.setEnableUndefinedVariableException(true);

        if (defaultValue != null) {
            property = property + ":-" + defaultValue;
        }

        return interpolator.replace("${properties:src/test/resources/" + SLACK_PROPERTIES_FILE + "::" + property + "}");
    }

    /**
     * Parses the provided set of tags and returns all the channels referenced by the
     * slack tag.
     * @param tags  List of tags (i.e @ignore, @slack(#channel1,#channel2,#channel3))
     * @return      A list of all the channels contained in the slack tag (i.e {#channel1,#channel2,#channel3})
     */
    public List<String> getChannels(List<String> tags) {

        String pattern = "@slack[\\(\\[](.*)[\\)\\]]";
        Pattern r = Pattern.compile(pattern);
        List<String> channels = new LinkedList<>();

        for (String tag: tags) {
            Matcher m = r.matcher(tag);
            if (m.find()) {
                String[] channelsString = m.group(1).split(",");

                for (String c : channelsString) {
                    if (!c.matches("")) {
                        channels.add(c);
                    }
                }
                return channels;
            }
        }
        return channels;
    }


    /**
     * Sends the given message to the specified list of channels. For more information and settings, you
     * can check.
     * @see <a href="https://api.slack.com/methods/chat.postMessage</a>
     * @param channels              List of channels
     * @param message               Text to send
     * @return                      List of {@link ChatPostMessageResponse}
     * @throws SlackApiException    SlackApiException
     * @throws IOException          IOException
     */
    public List<ChatPostMessageResponse> sendMessageToChannels(List<String> channels, String message) throws SlackApiException, IOException {

        List<ChatPostMessageResponse> responses = new LinkedList<>();

        for (String channel: channels) {

            Slack slack = Slack.getInstance();
            String token = this.getProperty("slack.token", null);

            ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
                    .channel(channel)
                    .iconEmoji(this.getProperty("slack.iconEmoji", ":ko:"))
                    .linkNames(true)
                    .username(this.getProperty("slack.username", "Test report bot"))
                    .text(message));
            response.setChannel(channel);
            responses.add(response);
        }
        return responses;
    }
}
