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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;

/**
 * Class that manages a MessageClientManagerThread that regularly checks for pending messages and
 * sends them to their corresponding recipients
 */
public class MessageClientManager {
  private static final Logger log = LogManager.getLogger();
  private static final long WAITING_TIME_FOR_POLLING = getOBProperty("messageclient.wait.time",
      10000, 5000);

  private static MessageClientManager instance;

  private MessageRegistry messageRegistry;
  private boolean threadsStarted = false;
  private boolean isShutDown = false;
  private MessageClientManagerThread managerThread;
  private ExecutorService executorService;

  @Inject
  @Any
  private Instance<MessageHandler> messageHandlers;

  public static MessageClientManager getInstance() {
    if (instance == null) {
      instance = WeldUtils.getInstanceFromStaticBeanManager(MessageClientManager.class);
    }
    return instance;
  }

  /**
   * Starts the MessageClientManager thread and its corresponding message registry
   */
  public synchronized void start() {
    if (threadsStarted) {
      return;
    }

    if (!isMessageClientManagerEnabled()) {
      return;
    }

    threadsStarted = true;

    log.info("Starting Message Client Manager");

    executorService = Executors.newCachedThreadPool();

    // create, start the manager thread
    managerThread = new MessageClientManagerThread(this);
    messageRegistry = MessageRegistry.getInstance();
    executorService.submit(managerThread);
    isShutDown = false;
  }

  /**
   * Stops the message client manager and clears no longer required instance variables
   */
  public void shutdown() {
    if (!threadsStarted) {
      return;
    }
    log.info("Shutting down Message Client Manager");

    isShutDown = true;

    if (executorService != null) {
      executorService.shutdownNow();
    }

    executorService = null;
    threadsStarted = false;
    managerThread = null;
    messageRegistry = null;
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
        if (!pendingMessages.isEmpty()) {
          log.debug(
              "[Message Client] There are " + pendingMessages.size() + " pending to be sent.");
          pendingMessages.forEach(message -> {
            MessageClientBroadcaster.send(message, getMessageRecipients(message));
          });
        }
        try {
          log.debug(
              "{} messages have been handled. Wait {} ms, and try again to capture new messages which have been added.",
              pendingMessages.size(), WAITING_TIME_FOR_POLLING);
          Thread.sleep(WAITING_TIME_FOR_POLLING);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }

    List<MessageClient> getMessageRecipients(MessageClientMsg messageClientMsg) {
      Instance<MessageHandler> messageHandler = manager.messageHandlers
          .select(new MessageHandler.Selector(messageClientMsg.getTopic()));
      if (messageHandler.isUnsatisfied()) {
        log.warn("No available message handler for type: " + messageClientMsg.getTopic());
        return Collections.emptyList();
      }
      return messageHandler.get().getRecipients(messageClientMsg);
    }
  }

  private static boolean isMessageClientManagerEnabled() {
    try {
      OBContext.setAdminMode();
      String enableMessageManagerPreference = Preferences.getPreferenceValue(
          "OBUIAPP_Enable_Message_Manager", true, OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null);
      if (!Preferences.YES.equals(enableMessageManagerPreference)) {
        return false;
      }
    } catch (PropertyException e) {
      log.error(
          "Preference OBUIAPP_Enable_Message_Manager is not properly set, Message Client Manager will not be started.");
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }
    return true;
  }

  /**
   * Returns an integer property or its min/default value if not set
   * 
   * @param property
   *          Property to be retrieved
   * @param defaultValue
   *          Default value if not set
   * @param minValue
   *          Minimum value, to avoid user setting it too low
   * @return value of the property
   */
  private static int getOBProperty(String property, int defaultValue, int minValue) {
    int value = getOpenbravoProperty(property, defaultValue);
    if (value < minValue) {
      log.warn("Value of property " + property + " is set too low (" + value
          + "), using valid minValue instead " + minValue);
      return minValue;
    }
    return value;
  }

  /**
   * Retrieves an integer property from OBProperties Provider, if it is not properly set, it will
   * fallback to a provided default value
   * 
   * @param propName
   *          Property name to be retrieved
   * @param defaultValue
   *          Default value in case it is not properly set
   * @return value of the property
   */
  private static int getOpenbravoProperty(String propName, int defaultValue) {
    final String val = OBPropertiesProvider.getInstance()
        .getOpenbravoProperties()
        .getProperty(propName);
    if (val == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException ignore) {
      return defaultValue;
    }
  }
}
