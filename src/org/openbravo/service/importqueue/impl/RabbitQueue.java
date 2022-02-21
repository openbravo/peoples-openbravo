package org.openbravo.service.importqueue.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.service.importqueue.ImportEntryProcessor;
import org.openbravo.service.importqueue.QueueException;
import org.openbravo.service.importqueue.QueueImplementation;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Delivery;

public class RabbitQueue implements QueueImplementation {

  private static final Logger log = LogManager.getLogger();

  private final String host = "localhost";
  private final String exchangeName = "OB_EXCHANGE_ORDERS";
  private final String queueName = "OB_QUEUE_ORDERS";

  private ImportEntryProcessor processor;

  private Connection connection;
  private Channel exchangeChannel;
  private Channel queueChannel;

  @Override
  public void start(ImportEntryProcessor p) {
    this.processor = p;

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    try {
      connection = factory.newConnection();
      exchangeChannel = connection.createChannel();
      queueChannel = connection.createChannel();

    } catch (IOException | TimeoutException e) {
      // TODO PROTOTYPE:
      exchangeChannel = null;
      queueChannel = null;
      connection = null;
      throw new RuntimeException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    Thread t = new Thread(() -> {
      try {
        queueChannel.basicConsume(queueName, true, this::deliverCallback, consumerTag -> {
        });
      } catch (IOException e) {
        // TODO PROTOTYPE:
        throw new RuntimeException(e);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    });
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void close() {

    try {
      queueChannel.close();
      exchangeChannel.close();
      connection.close();
    } catch (IOException | TimeoutException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void deliverCallback(String consumerTag, Delivery delivery) {
    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

    try {
      JSONObject jsonmessage = new JSONObject(message);
      processor.processImportEntry(jsonmessage.getString("qualifier"),
          jsonmessage.getString("data"));
    } catch (QueueException | JSONException e) {
      // TODO PROTOTYPE:
      log.warn("Error processing Import Entry", e);
    } catch (Exception e) {
      // TODO PROTOTYPE:
      log.warn("Error processing Import Entry", e);
    }
  }

  @Override
  public void publish(String qualifier, String data) {
    try {
      String message = new JSONObject().put("qualifier", qualifier).put("data", data).toString();
      // Publishs to the DEFAULT EXCHANGE, ROUTING KEY is the QUEUE NAME
      exchangeChannel.basicPublish(exchangeName, "", null,
          message.getBytes(StandardCharsets.UTF_8));
    } catch (IOException | JSONException e) {
      // TODO PROTOTYPE:
      log.warn("Cannot publish message", e);
    }
  }

}
