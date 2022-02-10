package org.openbravo.test.http;

import static org.junit.Assert.assertThat;
import static org.openbravo.test.matchers.json.JSONMatchers.matchesObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

public class HttpRequestTest extends BaseHttpTest {

  @Test
  public void postRequest() throws InterruptedException, JSONException, ExecutionException {
    JSONObject response = new JSONObject(Map.of("result", "ok"));
    enqueueResponse(Collections.emptyMap(), 200, response.toString());

    HttpClient httpClient = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
    JSONObject requestData = new JSONObject(Map.of("userId", "100"));
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(getServerURL()))
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString(requestData.toString()))
        .build();

    JSONObject body = new JSONObject(
        httpClient.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).get());
    assertThat(body, matchesObject(response));
  }
}
