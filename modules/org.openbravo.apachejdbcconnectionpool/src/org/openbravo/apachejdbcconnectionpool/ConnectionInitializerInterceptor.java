/*
 ***********************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.apachejdbcconnectionpool;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.database.PoolInterceptorProvider;
import org.openbravo.database.SessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionInitializerInterceptor extends JdbcInterceptor implements
    PoolInterceptorProvider {
  private static Logger log = LoggerFactory.getLogger(ConnectionInitializerInterceptor.class);

  String rbdms = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
      .get("bbdd.rdbms");

  public void reset(ConnectionPool parent, PooledConnection con) {
    if (con != null) {
      HashMap<Object, Object> attributes = con.getAttributes();
      Boolean connectionInitialized = (Boolean) attributes.get("OB_INITIALIZED");
      if (connectionInitialized == null || connectionInitialized == false) {
        SessionInfo.setDBSessionInfo(con.getConnection(), rbdms);
        PreparedStatement pstmt = null;
        try {
          final Properties props = OBPropertiesProvider.getInstance().getOpenbravoProperties();
          final String dbSessionConfig = props.getProperty("bbdd.sessionConfig");
          pstmt = con.getConnection().prepareStatement(dbSessionConfig);
          pstmt.executeQuery();
        } catch (Exception e) {
          throw new IllegalStateException(e);
        } finally {
          try {
            if (pstmt != null && !pstmt.isClosed()) {
              pstmt.close();
            }
          } catch (SQLException e) {
            throw new OBException(e);
          }
        }
        attributes.put("OB_INITIALIZED", true);
      }
    }
  }

  @Override
  public String getPoolInterceptorsClassNames() {
    String fullClassName = this.getClass().getName();
    return fullClassName + ";";
  }
}
