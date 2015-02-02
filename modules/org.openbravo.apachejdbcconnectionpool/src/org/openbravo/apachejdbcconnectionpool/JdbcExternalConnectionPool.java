/*
 ***********************************************************************************
 * Copyright (C) 2014-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.apachejdbcconnectionpool;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
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
      connection.setAutoCommit(false);
    } catch (Exception e) {
      log.error("Error while retrieving connection: ", e);
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
    poolProperties.setInitialSize(getIntProperty(poolPropertiesConfig, "initialSize", "5"));
    poolProperties.setMaxActive(getIntProperty(poolPropertiesConfig, "maxActive", "30"));
    poolProperties.setMaxIdle(getIntProperty(poolPropertiesConfig, "maxIdle", "30"));
    poolProperties.setMinIdle(getIntProperty(poolPropertiesConfig, "minIdle", "5"));
    poolProperties.setMaxWait(getIntProperty(poolPropertiesConfig, "maxWait", "30"));
    poolProperties.setTimeBetweenEvictionRunsMillis(getIntProperty(poolPropertiesConfig,
        "timeBetweenEvictionRunsMillis", "30000"));
    poolProperties.setMinEvictableIdleTimeMillis(getIntProperty(poolPropertiesConfig,
        "minEvictableIdleTimeMillis", "30000"));
    poolProperties.setRemoveAbandoned(getBooleanProperty(poolPropertiesConfig, "removeAbandoned",
        "true"));
    poolProperties.setRemoveAbandonedTimeout(getIntProperty(poolPropertiesConfig,
        "removeAbandonedTimeout", "60"));
    poolProperties
        .setLogAbandoned(getBooleanProperty(poolPropertiesConfig, "logAbandoned", "false"));
    poolProperties.setJmxEnabled(getBooleanProperty(poolPropertiesConfig, "jmxEnabled", "true"));
    poolProperties.setTestWhileIdle(getBooleanProperty(poolPropertiesConfig, "testWhileIdle",
        "false"));
    poolProperties
        .setTestOnBorrow(getBooleanProperty(poolPropertiesConfig, "testOnBorrow", "true"));
    poolProperties
        .setTestOnReturn(getBooleanProperty(poolPropertiesConfig, "testOnReturn", "false"));
    poolProperties.setValidationInterval(getIntProperty(poolPropertiesConfig, "validationInterval",
        "30000"));
    poolProperties.setValidationQuery(poolPropertiesConfig.getProperty("validationQuery",
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
    return Integer.parseInt(properties.getProperty(propertyName, defaultValue));
  }
}
