package org.openbravo.service.importqueue.impl;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@ApplicationScoped
public class RabbitConnection {

  private static final Logger log = LogManager.getLogger();

  private final String host = "localhost";

  private Connection connection;

  public Connection getConnection() {
    return connection;
  }

  @PostConstruct
  private void init() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    try {
      connection = factory.newConnection();
      log.debug(() -> "RABBITMQ connected. " + connection.toString());
    } catch (IOException | TimeoutException e) {
      // TODO PROTOTYPE:
      connection = null;
      throw new RuntimeException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @PreDestroy
  private void close() {
    try {
      connection.close();
      log.debug(() -> "RABBITMQ disconnected.");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
