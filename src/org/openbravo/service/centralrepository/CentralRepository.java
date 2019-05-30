package org.openbravo.service.centralrepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
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

  public enum Service {
    REGISTER_MODULE("register"),
    SEARCH_MODULES("search"),
    MODULE_INFO("module"),
    MATURITY_LEVEL("maturityLevel");

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

  private static JSONObject executeRequest(HttpUriRequest request, Service service,
      JSONObject content) {
    try (CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse rawResponse = httpclient.execute(request)) {

      String result = new BufferedReader(
          new InputStreamReader(rawResponse.getEntity().getContent())).lines()
              .collect(Collectors.joining("\n"));
      JSONObject r = new JSONObject(result);
      JSONObject msg = new JSONObject();
      msg.put("success", rawResponse.getStatusLine().getStatusCode() == 200);
      msg.put("responseCode", rawResponse.getStatusLine().getStatusCode());
      msg.put("response", r);
      return msg;
    } catch (Exception e) {
      log.error("Error communicating with Central Repository service {}", service);
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
