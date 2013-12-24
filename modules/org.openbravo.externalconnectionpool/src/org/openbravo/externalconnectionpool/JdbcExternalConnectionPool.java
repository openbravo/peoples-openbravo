package org.openbravo.externalconnectionpool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.database.ExternalConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JdbcExternalConnectionPool extends ExternalConnectionPool {

  final static private Logger log = LoggerFactory.getLogger(JdbcExternalConnectionPool.class);

  private static JdbcExternalConnectionPool instance;

  private final static String PATH_FROM_OB_ROOT = "modules/org.openbravo.externalconnectionpool/config/connectionPool.properties";

  private DataSource dataSource = null;

  public synchronized static ExternalConnectionPool getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(JdbcExternalConnectionPool.class);
    }
    return instance;
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
      showPoolStats();
    } catch (Exception e) {
      log.error("Error while retrieving connection: ", e);
    }
    return connection;
  }

  @Override
  public Connection getConnection(String type) {
    return getConnection();
  }

  public void showPoolStats() {
    if (dataSource != null) {
      System.out.println("getConnection(). Active: " + dataSource.getActive() + ", idle: "
          + dataSource.getIdle());
    }
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

    PoolProperties poolProperties = new PoolProperties();
    poolProperties.setUrl(obUrl + "/" + sid);
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
        + "org.openbravo.externalconnectionpool.TestInterceptor;");
    return poolProperties;
  }

  private boolean getBooleanProperty(Properties properties, String propertyName, String defaultValue) {
    return ("true".equals(properties.getProperty(propertyName, defaultValue)));
  }

  private int getIntProperty(Properties properties, String propertyName, String defaultValue) {
    return Integer.parseInt(properties.getProperty(propertyName, defaultValue));
  }

  private File getPropertiesFile() {
    // get the location of the current class file
    final URL url = this.getClass().getResource(getClass().getSimpleName() + ".class");
    File f = new File(url.getPath());
    File propertiesFile = null;
    while (f.getParentFile() != null && f.getParentFile().exists()) {
      f = f.getParentFile();
      final File configDirectory = new File(f, "config");
      if (configDirectory.exists()) {
        propertiesFile = new File(f, PATH_FROM_OB_ROOT);
        if (propertiesFile.exists()) {
          // found it and break
          break;
        }
      }
    }
    return propertiesFile;
  }
}
