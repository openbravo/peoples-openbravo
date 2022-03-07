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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.importqueue.MessageDispatcher;
import org.openbravo.service.importqueue.QueueException;
import org.openbravo.service.importqueue.QueueSubscription;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

public class RabbitSubscription implements QueueSubscription {
  private static final Logger log = LogManager.getLogger();

  private final String queueName = "OB_QUEUE_PROCESS";
  private final boolean autoack = true;

  @Inject
  private RabbitConnection rabbit;

  private MessageDispatcher messagedispatcher;

  private Channel channel;

  @PostConstruct
  private void init() {
    try {
      channel = rabbit.getConnection().createChannel();
    } catch (IOException e) {
      channel = null;
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  @PreDestroy
  private void close() {
    try {
      channel.close();
    } catch (IOException | TimeoutException e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void subscribe(MessageDispatcher dispatcher) {
    messagedispatcher = dispatcher;
    Thread t = new Thread(() -> {
      try {
        channel.basicConsume(queueName, autoack, this::deliverCallback, consumerTag -> {
        });
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    t.setDaemon(true);
    t.start();
  }

  private void deliverCallback(String consumerTag, Delivery delivery) {
    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

    try {
      JSONObject jsonmessage = new JSONObject(message);
      messagedispatcher.dispatchMessage(jsonmessage);
      if (!autoack) {
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      }
    } catch (QueueException | JSONException e) {
      log.warn("Error processing Import Entry", e);
    } catch (Exception e) {
      log.warn("Error processing Import Entry", e);
    }
  }
}
