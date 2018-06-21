package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public interface ApprovalCheckHook {
  public void exec(String userName, String password, String terminal, JSONArray approvalType,
      JSONObject attributes) throws Exception;
}
