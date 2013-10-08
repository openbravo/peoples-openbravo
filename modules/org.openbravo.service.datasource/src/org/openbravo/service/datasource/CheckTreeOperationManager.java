package org.openbravo.service.datasource;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

public abstract class CheckTreeOperationManager {

  public abstract ActionResponse checkNewNode(JSONObject bobProperties);

  public abstract ActionResponse checkNodeRemoval(Map<String, String> parameters);

  public abstract ActionResponse checkNodeMovement(Map<String, String> parameters, String nodeId,
      String newParentId, String prevNodeId, String nextNodeId);

  protected class ActionResponse {
    private boolean success;
    private String messageType;
    private String message;

    public ActionResponse(boolean success) {
      this.success = success;
    }

    public ActionResponse(boolean success, String messageType, String message) {
      this.success = success;
      this.messageType = messageType;
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public String getMessageType() {
      return messageType;
    }

    public void setMessageType(String messageType) {
      this.messageType = messageType;
    }

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }
  }
}
