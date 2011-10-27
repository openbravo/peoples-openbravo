package org.openbravo.examples.pne;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.process.BaseProcessActionHandler;

public class CopyLinesActionHandler extends BaseProcessActionHandler {

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
