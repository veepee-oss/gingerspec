package com.privalia.qa.utils;

import com.jayway.jsonpath.JsonPath;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
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

    public String getEntityStatus(String entity) throws ExecutionException, InterruptedException, IOException {

        String jiraURL = this.getProperty("jira.server.url");
        String jiraToken = this.getProperty("jira.personal.access.token");

        Request getRequest = new RequestBuilder()
                .setMethod("GET")
                .setUrl(jiraURL + "/rest/api/2/issue/" + entity)
                .addHeader("Authorization", "Bearer " + jiraToken)
                .build();

        Future  f = this.client.executeRequest(getRequest);
        Response r = (Response) f.get();

        return JsonPath.read(r.getResponseBody(),"$.fields.status.name").toString().toUpperCase();
    }


    public Boolean entityShouldRun(String entity) throws IOException, ExecutionException, InterruptedException {

        String[] valid_statuses = this.getProperty("jira.valid.runnable.statuses").split(",");
        String entity_current_status = this.getEntityStatus(entity).toUpperCase();

        for (String status: valid_statuses) {
            if (entity_current_status.matches(status.toUpperCase())) {
                return true;
            }
        }

        return false;
    }
}
