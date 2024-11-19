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
