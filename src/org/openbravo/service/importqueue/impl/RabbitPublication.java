package org.openbravo.service.importqueue.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.importqueue.QueuePublication;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

public class RabbitPublication implements QueuePublication {

  private static final Logger log = LogManager.getLogger();

  private final String exchangeName = "OB_EXCHANGE_ORDERS";
  private final String exchangeRoute = "";

  @Inject
  private RabbitConnection rabbit;

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
  public void publish(JSONObject message) {
    try {
      channel.basicPublish(exchangeName, exchangeRoute, MessageProperties.PERSISTENT_TEXT_PLAIN,
          message.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      log.error("Cannot publish import entry.", e);
    }
  }
}
