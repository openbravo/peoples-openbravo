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
 * All portions are Copyright (C) 2014-2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
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

/**
 * This interceptor allows to act whenever a connection is requested from the pool and whenever any
 * operation is invoked on a connection provided by Apache JDBC Connection Pool.
 */
public class ConnectionInitializerInterceptor extends JdbcInterceptor implements
    PoolInterceptorProvider {

  private static final String SESSION_CONFIG_APPLIED = "OB_INITIALIZED";
  private static final String SESSION_INFO_APPLIED = "SESSION_INFO_INITIALIZED";

  String rbdms = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
      .get("bbdd.rdbms");

  /**
   * This method is called each time the connection is borrowed from the pool and it is used to
   * initialize prepareStatement.
   */
  @Override
  public void reset(ConnectionPool parent, PooledConnection con) {
    if (con != null) {
      HashMap<Object, Object> attributes = con.getAttributes();
      Boolean sessionInfoApplied = (Boolean) attributes.get(SESSION_CONFIG_APPLIED);
      if (sessionInfoApplied == null || !sessionInfoApplied) {
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
        attributes.put(SESSION_CONFIG_APPLIED, true);
      }

      Boolean sessionInfoInitialized = (Boolean) attributes.get(SESSION_INFO_APPLIED);
      if (sessionInfoInitialized == null || !sessionInfoInitialized) {
        boolean initialized = false;
        if (isReadOnlyPool(parent)) {
          initialized = true;
        } else {
          // SessionInfo will be initialized when the SessionListener ServletContextListener is
          // invoked. That listener will check if there are audited tables and notify SessionInfo,
          // from them on SessionInfo will know whether the ad_context_info table should be created
          if (SessionInfo.isInitialized()) {
            SessionInfo.initDB(con.getConnection(), rbdms);
            initialized = true;
          }
        }
        attributes.put(SESSION_INFO_APPLIED, initialized);
      }
    }
  }

  private boolean isReadOnlyPool(ConnectionPool connectionPool) {
    if (connectionPool == null || connectionPool.getPoolProperties().isDefaultReadOnly() == null) {
      return false;
    }
    return connectionPool.getPoolProperties().isDefaultReadOnly();
  }

  @Override
  public String getPoolInterceptorsClassNames() {
    String fullClassName = this.getClass().getName();
    return fullClassName + ";";
  }
}
