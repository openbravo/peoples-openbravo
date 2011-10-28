package org.openbravo.erpCommon.actionhandler;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.process.BaseProcessActionHandler;

/**
 * 
 * @author gorkaion
 * 
 */
@ApplicationScoped
public class SRMOPickEditLines extends BaseProcessActionHandler {

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    try {
      jsonRequest = new JSONObject(content);
      System.err.println(jsonRequest);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jsonRequest;
  }

}
