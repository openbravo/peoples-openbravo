package org.openbravo.scheduling.quartz;

import java.sql.Connection;
import java.sql.SQLException;

import org.openbravo.base.ConnectionProviderContextListener;
import org.openbravo.exception.NoConnectionAvailableException;
import org.quartz.utils.ConnectionProvider;

public class QuartzConnectionProvider implements ConnectionProvider {

  @Override
  public Connection getConnection() throws SQLException {
    Connection connection;
    try {
      connection = ConnectionProviderContextListener.getPool().getConnection(); 
    } catch (NoConnectionAvailableException ex) {
      throw new SQLException(ex);
    }
    return connection;
  }

  @Override
  public void initialize() throws SQLException {
    // The Openbravo connection provider is initialized by the Servlet Context listener
  }

  @Override
  public void shutdown() throws SQLException {
    // The Openbravo connection provider is shutdown by the Servlet Context listener
  }

}
