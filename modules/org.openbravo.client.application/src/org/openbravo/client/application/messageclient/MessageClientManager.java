package org.openbravo.client.application.messageclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.weld.WeldUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
