package org.openbravo.service.importqueue.impl;

import javax.enterprise.context.ApplicationScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.importqueue.MessageDispatcher;
import org.openbravo.service.importqueue.QueueException;
import org.openbravo.service.importqueue.QueuePublication;
import org.openbravo.service.importqueue.QueueSubscription;

@ApplicationScoped
public class NoQueue implements QueuePublication, QueueSubscription {
  private static final Logger log = LogManager.getLogger();

  private MessageDispatcher messagedispatcher;

  @Override
  public void publish(JSONObject message) {
    try {
      messagedispatcher.dispatchMessage(message);
    } catch (QueueException | JSONException e) {
      log.error("Error processing import entry. ", e);
    }
  }

  @Override
  public void subscribe(MessageDispatcher dispatcher) {
    messagedispatcher = dispatcher;
  }
}
