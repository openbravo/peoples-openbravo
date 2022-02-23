package org.openbravo.service.importqueue;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class MessageDispatcher {

  private static final Logger log = LogManager.getLogger();

  @Inject
  @Any
  private Instance<MessageProcessor> queueProcessorsList;

  public void dispatchMessage(JSONObject message) throws QueueException, JSONException {

    queueProcessorsList.forEach((MessageProcessor p) -> {
      // Returns success or failure if processed
      // Returns null otherwise
      try {
        p.processMessage(message);
      } catch (QueueException | JSONException e) {
        log.error("Cannot process message.", e);
      }
    });
  }
}
