package org.openbravo.service.centralrepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
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
    REGISTER_MODULE("register"), SEARCH_MODULES("search");

    private String endpoint;

    private Service(String endpoint) {
      this.endpoint = endpoint;
    }
  }

  public static JSONObject post(Service service, JSONObject content) {
    StringEntity requestEntity = new StringEntity(content.toString(), ContentType.APPLICATION_JSON);

    HttpPost postMethod = new HttpPost(BUTLER_API_URL + service.endpoint);
    postMethod.setEntity(requestEntity);
    try (CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse rawResponse = httpclient.execute(postMethod)) {

      String result = new BufferedReader(
          new InputStreamReader(rawResponse.getEntity().getContent())).lines()
              .collect(Collectors.joining("\n"));
      JSONObject r = new JSONObject(result);
      JSONObject msg = new JSONObject();
      msg.put("success", rawResponse.getStatusLine().getStatusCode() == 200);
      msg.put("responseCode", rawResponse.getStatusLine().getStatusCode());
      msg.put("response", r);

      System.out.println(msg.toString(1));
      return msg;
    } catch (Exception e) {
      log.error("Error communicating with Central Repository service {}", service);
      log.debug("Failed content sent to CR {}", content);
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
