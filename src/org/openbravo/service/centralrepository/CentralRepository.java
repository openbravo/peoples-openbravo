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
 * All portions are Copyright (C) 2019 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):
 ************************************************************************
 */

package org.openbravo.service.centralrepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

public class CentralRepository {
  private static final String BUTLER_API_URL = "https://butler.openbravo.com/openbravo/api/";

  private static final Logger log = LogManager.getLogger();

  private static final int TIMEOUT = 10_000;
  private static final RequestConfig TIMEOUT_CONFIG = RequestConfig.custom()
      .setConnectionRequestTimeout(TIMEOUT)
      .setConnectTimeout(TIMEOUT)
      .setSocketTimeout(TIMEOUT)
      .build();

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

  public static JSONObject executeRequest(Service service) {
    return executeRequest(service, Collections.emptyList(), null);
  }

  public static JSONObject executeRequest(Service service, JSONObject payload) {
    return executeRequest(service, Collections.emptyList(), payload);
  }

  public static JSONObject executeRequest(Service service, List<String> path) {
    return executeRequest(service, path, null);
  }

  private static JSONObject executeRequest(Service service, List<String> path, JSONObject content) {
    long t = System.currentTimeMillis();
    HttpRequestBase request = getServiceRequest(service, path);

    if (content != null && (request instanceof HttpPost)) {
      StringEntity requestEntity = new StringEntity(content.toString(),
          ContentType.APPLICATION_JSON);
      ((HttpPost) request).setEntity(requestEntity);
    }

    try (CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse rawResponse = httpclient.execute(request)) {

      log.trace("Sending request [{}] payload: {}", request.getURI(), content);

      String result = new BufferedReader(
          new InputStreamReader(rawResponse.getEntity().getContent())).lines()
              .collect(Collectors.joining("\n"));

      log.debug("Processed to Central Repository {} with status {} in {} ms", request.getURI(),
          rawResponse.getStatusLine().getStatusCode(), System.currentTimeMillis() - t);
      log.trace("Response to request [{}]: {}", request.getURI(), result);

      JSONObject msg = new JSONObject();
      boolean success = 200 >= rawResponse.getStatusLine().getStatusCode()
          && rawResponse.getStatusLine().getStatusCode() < 300;
      msg.put("success", success);
      msg.put("responseCode", rawResponse.getStatusLine().getStatusCode());
      JSONObject r;
      try {
        r = new JSONObject(result);
      } catch (JSONException e) {
        log.debug("Didn't receive a valid JSON response: {}", result, e);
        r = new JSONObject();
        if (!success) {
          // try to get something meaningful from the status info
          r.put("msg", rawResponse.getStatusLine().getReasonPhrase());
        }
      }

      msg.put("response", r);

      return msg;
    } catch (Exception e) {
      log.error("Error communicating with Central Repository service {}", service, e);
      if (content != null) {
        log.debug("Failed content sent to CR {}", content);
      }

      try {
        JSONObject msg = new JSONObject();
        msg.put("sucess", false);
        msg.put("responseCode", 500);

        JSONObject r = new JSONObject();
        r.put("msg", e.getMessage());
        msg.put("response", r);
        return msg;
      } catch (JSONException e1) {
        throw new OBException(e1);
      }
    }
  }

  private static HttpRequestBase getServiceRequest(Service service, List<String> path) {
    HttpRequestBase req;
    switch (service.method) {
      case GET:
        req = new HttpGet();
        break;
      default: // POST
        req = new HttpPost();
    }
    String uri = BUTLER_API_URL + service.endpoint + "/"
        + path.stream().collect(Collectors.joining("/"));
    try {
      req.setURI(new URI(uri));
    } catch (URISyntaxException e) {
      throw new OBException(e);
    }
    req.setConfig(TIMEOUT_CONFIG);
    return req;
  }
}
