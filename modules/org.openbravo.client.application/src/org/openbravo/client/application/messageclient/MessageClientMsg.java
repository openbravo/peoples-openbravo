package org.openbravo.client.application.messageclient;

import java.util.Date;
import java.util.Map;

public class MessageClientMsg {
  String id;
  String type;
  Map<String, String> context;
  String payload;
  Date expirationDate;

  public MessageClientMsg(String id, String type, Map<String, String> context, String payload,
      Date expirationTime) {
    this.id = id;
    this.type = type;
    this.context = context;
    this.payload = payload;
    this.expirationDate = expirationTime;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public Map<String, String> getContext() {
    return context;
  }

  public String getPayload() {
    return payload;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }
}
