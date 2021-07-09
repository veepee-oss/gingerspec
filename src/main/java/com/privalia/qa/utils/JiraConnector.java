package com.privalia.qa.utils;

import com.jayway.jsonpath.JsonPath;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.apache.commons.text.StringSubstitutor;
import net.minidev.json.JSONArray;
import java.util.concurrent.Future;

public class JiraConnector {

    public static final String JIRA_PROPERTIES_FILE = "jira.properties";

    final StringSubstitutor interpolator = StringSubstitutor.createInterpolator();

    AsyncHttpClient client = new AsyncHttpClient();

    public String getProperty(String property) {

        interpolator.setEnableUndefinedVariableException(true);

        if (System.getProperty(property) != null) {
            return System.getProperty(property);
        }

        return interpolator.replace("${properties:src/test/resources/" + JIRA_PROPERTIES_FILE + "::" + property + "}");
    }

    public String getEntityStatus(String entity) throws Exception {

        String jiraURL = this.getProperty("jira.server.url");
        String jiraToken = this.getProperty("jira.personal.access.token");

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

        return JsonPath.read(r.getResponseBody(),"$.fields.status.name").toString().toUpperCase();
    }


    public Boolean entityShouldRun(String entity) throws Exception {

        String[] valid_statuses = this.getProperty("jira.valid.runnable.statuses").split(",");
        String entity_current_status = this.getEntityStatus(entity).toUpperCase();

        for (String status: valid_statuses) {
            if (entity_current_status.matches(status.toUpperCase())) {
                return true;
            }
        }

        return false;
    }

    public void transitionEntityToGivenStatus(String entity, String new_status) throws Exception {

        int targetTransition = this.getTransitionIDForEntityByName(entity, new_status);

        String jiraURL = this.getProperty("jira.server.url");
        String jiraToken = this.getProperty("jira.personal.access.token");

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

    public int getTransitionIDForEntityByName(String entity, String transitionName) throws Exception {

        String jiraURL = this.getProperty("jira.server.url");
        String jiraToken = this.getProperty("jira.personal.access.token");

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

        Object transitionStrings = JsonPath.read(r.getResponseBody(),"$.transitions[?(@.name=='" + transitionName + "')].id");
        JSONArray ja = (JSONArray) transitionStrings;

        if (ja.isEmpty()) {
            throw new IndexOutOfBoundsException("Could not find the transition '" + transitionName + "' in the list of valid transitions for entity '" + entity + "'");
        } else {
            return Integer.valueOf(ja.get(0).toString());
        }

    }

    public void postCommentToEntity(String entity, String message) throws Exception {

        String jiraURL = this.getProperty("jira.server.url");
        String jiraToken = this.getProperty("jira.personal.access.token");

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

    public void transitionEntity(String entity) throws Exception {

        String jiraTransitionIfFail = this.getProperty("jira.transition.if.fail");
        this.transitionEntityToGivenStatus(entity, jiraTransitionIfFail);

    }
}
