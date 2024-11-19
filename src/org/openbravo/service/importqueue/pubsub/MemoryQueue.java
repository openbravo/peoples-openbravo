/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.importqueue.pubsub;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
public class MemoryQueue implements QueuePublication, QueueSubscription {
  private static final Logger log = LogManager.getLogger();

  private MessageDispatcher messagedispatcher;
  private BlockingQueue<JSONObject> queue = new LinkedBlockingQueue<>();

  @Override
  public void publish(JSONObject message) {
    try {
      queue.offer(message, 5000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      log.error("Error processing import entry. ", e);
    }
  }

  @Override
  public void subscribe(MessageDispatcher dispatcher) {
    messagedispatcher = dispatcher;

    Thread t = new Thread(() -> {
      for (;;) {
        try {
          messagedispatcher.dispatchMessage(queue.take());
        } catch (QueueException | JSONException | InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });
    t.setDaemon(true);
    t.start();
  }
}
