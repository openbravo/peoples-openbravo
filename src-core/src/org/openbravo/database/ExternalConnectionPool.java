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
      Class<ExternalConnectionPool> cpClass = null;
      try {
        System.out.println("Inside - Try!");
        cpClass = (Class<ExternalConnectionPool>) Class.forName(externalConnectionPoolClassName);
      } catch (Exception e) {
        System.out.println("Inside - Catch!");
      }
      instance = (ExternalConnectionPool) cpClass.newInstance();
    }
    return instance;
  }

  public abstract Connection getConnection();

  public abstract Connection getConnection(String type);

}
