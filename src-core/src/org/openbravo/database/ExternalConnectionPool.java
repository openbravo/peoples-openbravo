package org.openbravo.database;

import java.sql.Connection;

import org.apache.log4j.Logger;

public abstract class ExternalConnectionPool {

  static Logger log = Logger.getLogger(ExternalConnectionPool.class);

  private static ExternalConnectionPool instance;

  public synchronized static ExternalConnectionPool getInstance(
      String externalConnectionPoolClassName) throws InstantiationException,
      IllegalAccessException, ClassNotFoundException {
    if (instance == null) {
      instance = (ExternalConnectionPool) Class.forName(externalConnectionPoolClassName)
          .newInstance();
    }
    return instance;
  }

  public abstract Connection getConnection();

  public abstract Connection getConnection(String type);

}
