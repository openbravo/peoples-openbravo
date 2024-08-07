package org.openbravo.client.application.messageclient;

public class MessageClientMsg {
  String id;
  String type;
  String context;
  String payload;
  String expirationTime;

  public MessageClientMsg(String id, String type, String context, String payload,
      String expirationTime) {
    this.id = id;
    this.type = type;
    this.context = context;
    this.payload = payload;
    this.expirationTime = expirationTime;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getContext() {
    return context;
  }

  public String getPayload() {
    return payload;
  }

  public String getExpirationTime() {
    return expirationTime;
  }
}
