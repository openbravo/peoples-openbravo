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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.messageclient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageClientManager {
  private static final Logger log = LogManager.getLogger();
  MessageRegistry messageRegistry;
  MessageClientRegistry messageClientRegistry;
  boolean threadsStarted = false;
  boolean isShutDown = false;
  MessageClientManagerThread managerThread;
  ExecutorService executorService;

  public synchronized void start() {
    // TODO: maybe we don't want to have WebSocket system always on, preference to disable
    // message client manager entirely
    if (threadsStarted) {
      return;
    }
    threadsStarted = true;

    log.debug("Starting Message Client Manager");

    executorService = Executors.newCachedThreadPool();

    // create, start the manager thread
    managerThread = new MessageClientManagerThread(this);
    messageRegistry = MessageRegistry.getInstance();
    messageClientRegistry = MessageClientRegistry.getInstance();
    executorService.submit(managerThread);
    isShutDown = false;
  }

  public void shutdown() {
    if (!threadsStarted) {
      return;
    }
    log.debug("Shutting down Message Client Manager");

    isShutDown = true;

    if (executorService != null) {
      executorService.shutdownNow();
    }

    executorService = null;
    threadsStarted = false;
    managerThread = null;
    messageRegistry = null;
    messageClientRegistry = null;
  }

  static class MessageClientManagerThread implements Runnable {

    MessageClientManager manager;

    MessageClientManagerThread(MessageClientManager manager) {
      this.manager = manager;
    }

    @Override
    public void run() {
      if (manager.isShutDown) {
        return;
      }
      while (true) {
        if (manager.isShutDown) {
          return;
        }
        List<MessageClientMsg> pendingMessages = manager.messageRegistry.getPendingMessages();
        List<MessageClient> messageClients = manager.messageClientRegistry.getAllClients();
        if (!pendingMessages.isEmpty() && !messageClients.isEmpty()) {
          System.out.println("[MSG_CLIENT] Msgs to be sent have been detected. "
              + pendingMessages.size() + " msgs pending to be sent");
          pendingMessages.forEach(message -> {
            if (message.getPayload() != null) {
              try {
                // TODO: Reimplement this to properly handle only pending messages and call the
                // corresponding MessageHandler
                MessageClientBroadcaster.send(message, messageClients);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          });
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
