package com.privalia.qa.utils;

import com.jayway.jsonpath.JsonPath;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.privalia.qa.lookups.DefaultLookUp;
import org.apache.commons.text.StringSubstitutor;
import net.minidev.json.JSONArray;
import org.apache.commons.text.lookup.StringLookupFactory;

import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An small utility for interacting with entities in Jira
 * @author José Fernández
 */
public class JiraConnector {

    public static final String JIRA_PROPERTIES_FILE = "jira.properties";

    final StringSubstitutor interpolator = StringSubstitutor.createInterpolator();

    AsyncHttpClient client = new AsyncHttpClient();

    /**
     * Reads the given key from the properties file. The system will try to locate the key first in
     * the maven variables (System.getProperty) and if not found will look for it in the properties file.
     * If the value is still not found it will return the default value (if provided) or an exception
     * @param property  key
     * @return          value
     */
    private String getProperty(String property, String defaultValue) {

        if (System.getProperty(property) != null) {
            return System.getProperty(property);
        }

        interpolator.setEnableUndefinedVariableException(true);

        if (defaultValue != null) {
            property = property + ":-" + defaultValue;
        }

        return interpolator.replace("${properties:src/test/resources/" + JIRA_PROPERTIES_FILE + "::" + property + "}");
    }

    /**
     * Retrieves the current entity status
     * @param entity        Entity identifier (i.e QMS-123)
     * @return              Status as string (i.e 'In Progress')
     * @throws Exception    Exception
     */
    private String getEntityStatus(String entity) throws Exception {

        String jiraURL = this.getProperty("jira.server.url", null);
        String jiraToken = this.getProperty("jira.personal.access.token", null);

        Request getRequest = new RequestBuilder()
                .setMethod("GET")
                .setUrl(jiraURL + "/rest/api/2/issue/" + entity)
                .addHeader("Authorization", "Bearer " + jiraToken)
                .build();

        Future  f = this.client.executeRequest(getRequest);
        Response r = (Response) f.get();

        if (r.getStatusCode() != 200) {
            throw new Exception("Unexpected status code response:" + r.getStatusCode() + ". Body: '" + r.getResponseBody() + "'");
        }

        return JsonPath.read(r.getResponseBody(), "$.fields.status.name").toString().toUpperCase();
    }

    /**
     * Determines if the entity status matches any of the expected statuses
     * @param entity        Entity identifier (i.e QMS-123)
     * @return              True if the entity status is within the expected statuses
     * @throws Exception    Exception
     */
    public Boolean entityShouldRun(String entity) throws Exception {

        String[] valid_statuses = this.getProperty("jira.valid.runnable.statuses", "Done,Deployed").split(",");
        String entity_current_status = this.getEntityStatus(entity).toUpperCase();

        for (String status: valid_statuses) {
            if (entity_current_status.matches(status.toUpperCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Change the status of an entity to the Given Status by name. The status name should match exactly a valid
     * status for that entity
     * @param entity        Entity identifier (i.e QMS-123)
     * @param new_status    New status (i.e 'Done')
     * @throws Exception    Exception
     */
    private void transitionEntityToGivenStatus(String entity, String new_status) throws Exception {

        int targetTransition = this.getTransitionIDForEntityByName(entity, new_status);

        String jiraURL = this.getProperty("jira.server.url", "");
        String jiraToken = this.getProperty("jira.personal.access.token", "");

        Request postRequest = new RequestBuilder()
                .setMethod("POST")
                .setUrl(jiraURL + "/rest/api/2/issue/" + entity + "/transitions")
                .addHeader("Authorization", "Bearer " + jiraToken)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"transition\": {\"id\": " + targetTransition + " }}")
                .build();

        Future  f = this.client.executeRequest(postRequest);
        Response r = (Response) f.get();

        if (r.getStatusCode() != 204) {
            throw new Exception("Unexpected status code response:" + r.getStatusCode() + ". Body: '" + r.getResponseBody() + "'");
        }

    }

    /**
     * Gets the id of the transition by the given name
     * @param entity            Entity identifier (i.e QMS-123)
     * @param transitionName    Transition name (i.e 'In Progress')
     * @return                  Id of the transition for that name
     * @throws Exception        Exception
     */
    private int getTransitionIDForEntityByName(String entity, String transitionName) throws Exception {

        String jiraURL = this.getProperty("jira.server.url", "");
        String jiraToken = this.getProperty("jira.personal.access.token", "");

        Request getRequest = new RequestBuilder()
                .setMethod("GET")
                .setUrl(jiraURL + "/rest/api/2/issue/" + entity + "/transitions")
                .addHeader("Authorization", "Bearer " + jiraToken)
                .build();

        Future  f = this.client.executeRequest(getRequest);
        Response r = (Response) f.get();

        if (r.getStatusCode() != 200) {
            throw new Exception("Unexpected status code response:" + r.getStatusCode() + ". Body: '" + r.getResponseBody() + "'");
        }

        Object transitionStrings = JsonPath.read(r.getResponseBody(), "$.transitions[?(@.name=='" + transitionName + "')].id");
        JSONArray ja = (JSONArray) transitionStrings;

        if (ja.isEmpty()) {
            throw new IndexOutOfBoundsException("Could not find the transition '" + transitionName + "' in the list of valid transitions for entity '" + entity + "'");
        } else {
            return Integer.valueOf(ja.get(0).toString());
        }

    }

    /**
     * Adds a new comment to the entity
     * @param entity        Entity identifier (i.e QMS-123)
     * @param message       Message to post
     * @throws Exception    Exception
     */
    public void postCommentToEntity(String entity, String message) throws Exception {

        String jiraURL = this.getProperty("jira.server.url", "");
        String jiraToken = this.getProperty("jira.personal.access.token", "");

        Request postRequest = new RequestBuilder()
                .setMethod("POST")
                .setUrl(jiraURL + "/rest/api/2/issue/" + entity + "/comment")
                .addHeader("Authorization", "Bearer " + jiraToken)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"body\": \"" + message + "\"}")
                .build();

        Future  f = this.client.executeRequest(postRequest);
        Response r = (Response) f.get();

        if (r.getStatusCode() != 201) {
            throw new Exception("Unexpected status code response:" + r.getStatusCode() + ". Body: '" + r.getResponseBody() + "'");
        }
    }

    /**
     * Transition (change status) of the entity to the value provided in the properties file. Will
     * default to "In Progress" is the value is not found
     * @param entity        Entity identifier (i.e QMS-123)
     * @throws Exception    Exception
     */
    public void transitionEntity(String entity) throws Exception {

        String jiraTransitionToStatus = this.getProperty("jira.transition.if.fail.status", "TO REVIEW");
        Boolean jiraTransition = Boolean.valueOf(this.getProperty("jira.transition.if.fail", "true"));

        if (jiraTransition) {
            this.transitionEntityToGivenStatus(entity, jiraTransitionToStatus);
        }

    }

    /**
     * Returns the first reference to the jira ticket from the tag reference
     * @param tags      List of thats (i.e @ignore, @jira(QMS-123))
     * @return          The first ticket reference (i.e QMS-123)
     */
    public String getFirstTicketReference(List<String> tags) {
        String pattern = "@jira\\((.*)\\)";
        Pattern r = Pattern.compile(pattern);

        for (String tag: tags) {
            Matcher m = r.matcher(tag);
            if (m.find()) {
                return m.group(1);
            }
        }

        return null;
    }
}
