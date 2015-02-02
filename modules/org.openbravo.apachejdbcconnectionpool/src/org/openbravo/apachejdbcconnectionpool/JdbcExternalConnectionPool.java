/*
 ***********************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.apachejdbcconnectionpool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.database.ExternalConnectionPool;
import org.openbravo.database.PoolInterceptorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcExternalConnectionPool extends ExternalConnectionPool {

  final static private Logger log = LoggerFactory.getLogger(JdbcExternalConnectionPool.class);

  private final static String CONFIG_FILE_NAME = "connectionPool.properties";
  private final static String PATH_FROM_OB_ROOT = "modules/org.openbravo.apachejdbcconnectionpool/config/";

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

    File propertiesURL = getPropertiesFile();
    Properties poolPropertiesConfig = new Properties();
    try {
      FileInputStream fis = new FileInputStream(propertiesURL);
      poolPropertiesConfig.load(fis);
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
      poolProperties.setLogAbandoned(getBooleanProperty(poolPropertiesConfig, "logAbandoned",
          "false"));
      poolProperties.setJmxEnabled(getBooleanProperty(poolPropertiesConfig, "jmxEnabled", "true"));
      poolProperties.setTestWhileIdle(getBooleanProperty(poolPropertiesConfig, "testWhileIdle",
          "false"));
      poolProperties.setTestOnBorrow(getBooleanProperty(poolPropertiesConfig, "testOnBorrow",
          "true"));
      poolProperties.setTestOnReturn(getBooleanProperty(poolPropertiesConfig, "testOnReturn",
          "false"));
      poolProperties.setValidationInterval(getIntProperty(poolPropertiesConfig,
          "validationInterval", "30000"));
      poolProperties.setValidationQuery(poolPropertiesConfig.getProperty("validationQuery",
          "SELECT 1 FROM DUAL"));
    } catch (IOException e) {
      log.error("Error while loading connection pool properties", e);
    }
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

  private File getPropertiesFile() {
    File propertiesFile = null;

    // get config from Servlet context
    ServletContext ctx = OBConfigFileProvider.getInstance().getServletContext();
    if (ctx != null) {
      propertiesFile = new File(ctx.getRealPath("/WEB-INF/" + CONFIG_FILE_NAME));
      if (propertiesFile.exists()) {
        return propertiesFile;
      }
    }

    // get the location of the current class file
    final URL url = this.getClass().getResource(getClass().getSimpleName() + ".class");
    File f = new File(url.getPath());
    while (f.getParentFile() != null && f.getParentFile().exists()) {
      f = f.getParentFile();
      final File configDirectory = new File(f, "config");
      if (configDirectory.exists()) {
        propertiesFile = new File(f, PATH_FROM_OB_ROOT + CONFIG_FILE_NAME);
        if (propertiesFile.exists()) {
          // found it and break
          break;
        }
      }
    }
    return propertiesFile;
  }
}
