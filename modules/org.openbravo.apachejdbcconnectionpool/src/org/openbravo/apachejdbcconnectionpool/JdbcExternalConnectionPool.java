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
 * All portions are Copyright (C) 2014-2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.apachejdbcconnectionpool;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.database.ExternalConnectionPool;
import org.openbravo.database.PoolInterceptorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcExternalConnectionPool extends ExternalConnectionPool {

  final static private Logger log = LoggerFactory.getLogger(JdbcExternalConnectionPool.class);

  private DataSource dataSource = null;

  @Override
  public void loadInterceptors(List<PoolInterceptorProvider> interceptors) {
    String currentInterceptors = this.getDataSource().getJdbcInterceptors();
    for (PoolInterceptorProvider interceptor : interceptors) {
      currentInterceptors += interceptor.getPoolInterceptorsClassNames();
    }
    this.getDataSource().setJdbcInterceptors(currentInterceptors);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public Connection getConnection() {
    if (dataSource == null) {
      initPool();
    }
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      // All connections are setting autoCommit to true. DAL is taking into account his logical and
      // DAL is setting autoCommint to false to maintain transactional way of working.
      connection.setAutoCommit(true);
    } catch (Exception e) {
      log.error("Error while retrieving connection: ", e);
      throw new OBException(e);
    }
    return connection;
  }

  private void initPool() {
    dataSource = new DataSource();
    dataSource.setPoolProperties(getPoolProperties());
  }

  private PoolProperties getPoolProperties() {
    String obUrl = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .get("bbdd.url");
    String sid = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .get("bbdd.sid");
    String driver = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .get("bbdd.driver");
    String username = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .get("bbdd.user");
    String password = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .get("bbdd.password");
    String rbdms = (String) OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .get("bbdd.rdbms");

    PoolProperties poolProperties = new PoolProperties();
    if ("POSTGRE".equals(rbdms)) {
      poolProperties.setUrl(obUrl + "/" + sid);
    } else {
      poolProperties.setUrl(obUrl);
    }
    poolProperties.setDriverClassName(driver);
    poolProperties.setUsername(username);
    poolProperties.setPassword(password);

    Properties poolPropertiesConfig = new Properties();
    poolPropertiesConfig = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    poolProperties.setInitialSize(getIntProperty(poolPropertiesConfig, "db.pool.initialSize", "1"));
    poolProperties.setMaxActive(getIntProperty(poolPropertiesConfig, "db.pool.maxActive", "10000"));
    poolProperties.setMinIdle(getIntProperty(poolPropertiesConfig, "db.pool.minIdle", "5"));
    poolProperties.setTimeBetweenEvictionRunsMillis(getIntProperty(poolPropertiesConfig,
        "db.pool.timeBetweenEvictionRunsMillis", "60000"));
    poolProperties.setMinEvictableIdleTimeMillis(getIntProperty(poolPropertiesConfig,
        "db.pool.minEvictableIdleTimeMillis", "120000"));
    poolProperties.setRemoveAbandoned(getBooleanProperty(poolPropertiesConfig,
        "db.pool.removeAbandoned", "false"));

    poolProperties.setTestWhileIdle(getBooleanProperty(poolPropertiesConfig,
        "db.pool.testWhileIdle", "false"));
    poolProperties.setTestOnBorrow(getBooleanProperty(poolPropertiesConfig, "db.pool.testOnBorrow",
        "true"));
    poolProperties.setTestOnReturn(getBooleanProperty(poolPropertiesConfig, "db.pool.testOnReturn",
        "false"));
    poolProperties.setValidationInterval(getIntProperty(poolPropertiesConfig,
        "db.pool.validationInterval", "30000"));
    poolProperties.setValidationQuery(poolPropertiesConfig.getProperty("db.pool.validationQuery",
        "SELECT 1 FROM DUAL"));
    poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
        + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
        + "org.openbravo.apachejdbcconnectionpool.ConnectionInitializerInterceptor;");
    return poolProperties;
  }

  private boolean getBooleanProperty(Properties properties, String propertyName, String defaultValue) {
    return ("true".equals(properties.getProperty(propertyName, defaultValue)));
  }

  private int getIntProperty(Properties properties, String propertyName, String defaultValue) {
    return Integer.parseInt(properties.getProperty(propertyName, defaultValue).trim());
  }

  @Override
  public void closePool() {
    DataSource ds = getDataSource();
    if (ds != null) {
      // Closes the pool and all idle connections. true parameter is for close the active
      // connections too.
      ds.close(true);
    }
    super.closePool();
  }
}
