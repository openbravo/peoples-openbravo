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
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
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

  public enum Service {
    REGISTER_MODULE("register"),
    SEARCH_MODULES("search"),
    MODULE_INFO("module"),
    MATURITY_LEVEL("maturityLevel"),
    SCAN("scan"),
    CHECK_CONSISTENCY("checkConsistency"),
    VERSION_INFO("versionInfo");

    private String endpoint;

    private Service(String endpoint) {
      this.endpoint = endpoint;
    }
  }

  public static JSONObject post(Service service, JSONObject content) {
    StringEntity requestEntity = new StringEntity(content.toString(), ContentType.APPLICATION_JSON);

    HttpPost postMethod = new HttpPost(BUTLER_API_URL + service.endpoint);
    postMethod.setEntity(requestEntity);
    return executeRequest(postMethod, service, content);
  }

  public static JSONObject get(Service service) {
    return get(service, Collections.emptyList());
  }

  public static JSONObject get(Service service, List<String> fragments) {
    try {

      String path = service.endpoint;
      path += "/" + fragments.stream().collect(Collectors.joining("/"));
      URIBuilder b = new URIBuilder(BUTLER_API_URL + path);
      HttpGet getMethod = new HttpGet(b.build());

      return executeRequest(getMethod, service, null);
    } catch (URISyntaxException e) {
      throw new OBException(e);
    }

  }

  private static JSONObject executeRequest(HttpRequestBase request, Service service,
      JSONObject content) {
    long t = System.currentTimeMillis();
    request.setConfig(TIMEOUT_CONFIG);
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
      JSONObject r = new JSONObject();
      try {
        r.put("msg", e.getMessage());
        JSONObject msg = new JSONObject();
        msg.put("sucess", false);
        msg.put("responseCode", 500);
        msg.put("response", r);
        return msg;
      } catch (JSONException e1) {
        throw new OBException(e1);
      }
    }
  }
}
