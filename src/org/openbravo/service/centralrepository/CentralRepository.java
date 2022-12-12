/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2019-2020 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):
 ************************************************************************
 */

package org.openbravo.service.centralrepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

/** Handles communication with Central Repository Web Services. */
public class CentralRepository {
  private static final String BUTLER_API_URL = "https://butler.openbravo.com/openbravo/api/";

  private static final Logger log = LogManager.getLogger();

  private static final int TIMEOUT = 10_000;

  private enum Method {
    GET, POST
  }

  /** Defines available services in Central Repository */
  public enum Service {
    REGISTER_MODULE("register", Method.POST),
    SEARCH_MODULES("search", Method.POST),
    MODULE_INFO("module", Method.GET),
    MATURITY_LEVEL("maturityLevel", Method.GET),
    SCAN("scan", Method.POST),
    CHECK_CONSISTENCY("checkConsistency", Method.POST),
    VERSION_INFO("versionInfo", Method.GET);

    private String endpoint;
    private Method method;

    private Service(String endpoint, Method method) {
      this.endpoint = endpoint;
      this.method = method;
    }
  }

  private CentralRepository() {
    throw new IllegalStateException("No instantiable class");
  }

  /** @see #executeRequest(Service, List, JSONObject) */
  public static JSONObject executeRequest(Service service) {
    return executeRequest(service, Collections.emptyList(), null);
  }

  /** @see #executeRequest(Service, List, JSONObject) */
  public static JSONObject executeRequest(Service service, JSONObject payload) {
    return executeRequest(service, Collections.emptyList(), payload);
  }

  /** @see #executeRequest(Service, List, JSONObject) */
  public static JSONObject executeRequest(Service service, List<String> path) {
    return executeRequest(service, path, null);
  }

  /**
   * Performs a request to Central Repository for a given {@link Service} returning its response as
   * a {@link JSONObject}.
   *
   * @param service
   *          Central Repository service that will be invoked.
   * @param path
   *          Additional path parts that the service requires to be invoked.
   * @param payload
   *          JSON with additional information the request requires.
   * @return A {@link JSONObject} with the service's response, this JSON contains the following
   *         fields:
   *         <ul>
   *         <li>{@code success}: {@code boolean} indicating whether the response was successful or
   *         not.
   *         <li>{@code responseCode}: {@code int} HTTP status code.
   *         <li>{@code response}: {@code JSONObject} with the complete json as it was returned from
   *         the service. In case of a unsuccessful response, it contains a {@code msg} field with a
   *         textual description of the failure reason.
   *         </ul>
   */
  private static JSONObject executeRequest(Service service, List<String> path, JSONObject payload) {
    long t = System.currentTimeMillis();
    HttpRequest request = getServiceRequest(service, path, payload);

    HttpClient client = HttpClient.newBuilder()
        .version(Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    try {
      log.trace("Sending request [{}] payload: {}", request.uri(), payload);
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

      int status = response.statusCode();
      String responseBody = response.body();
      log.debug("Processed to Central Repository {} with status {} in {} ms", request.uri(), status,
          System.currentTimeMillis() - t);
      log.trace("Response to request [{}]: {}", request.uri(), responseBody);

      JSONObject msg = new JSONObject();
      boolean success = 200 >= status && status < 300;
      msg.put("success", success);
      msg.put("responseCode", status);
      JSONObject r;
      try {
        r = new JSONObject(responseBody);
      } catch (JSONException e) {
        log.debug("Didn't receive a valid JSON response: {}", responseBody, e);
        r = new JSONObject();
      }

      if (!success && !r.has("msg")) {
        // try to get something meaningful from the status info
        r.put("msg", responseBody); // TODO: check if correct, it was:
                                    // getStatusLine().getReasonPhrase()
      }

      msg.put("response", r);

      return msg;
    } catch (Exception e) {
      log.error("Error communicating with Central Repository service {}", service, e);
      if (payload != null) {
        log.debug("Failed content sent to CR {}", payload);
      }

      try {
        JSONObject msg = new JSONObject();
        msg.put("success", false);
        msg.put("responseCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject r = new JSONObject();
        r.put("msg", e.getMessage());
        msg.put("response", r);
        return msg;
      } catch (JSONException e1) {
        throw new OBException(e1);
      }
    }
  }

  private static HttpRequest getServiceRequest(Service service, List<String> path,
      JSONObject payload) {
    String uri = BUTLER_API_URL + service.endpoint + "/"
        + path.stream().collect(Collectors.joining("/"));
    var requestBuilder = HttpRequest.newBuilder()
        .uri(URI.create(uri))
        .timeout(Duration.ofMillis(TIMEOUT));

    switch (service.method) {
      case GET:
        requestBuilder.GET();
        break;
      default: // POST
        requestBuilder.header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(payload.toString()));
    }

    return requestBuilder.build();
  }
}
