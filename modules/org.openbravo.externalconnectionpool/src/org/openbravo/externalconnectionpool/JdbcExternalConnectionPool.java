package org.openbravo.externalconnectionpool;

import java.io.File;
import java.net.URL;
import java.sql.Connection;

import javax.enterprise.context.ApplicationScoped;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.database.ExternalConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JdbcExternalConnectionPool extends ExternalConnectionPool {

  final static private Logger log = LoggerFactory.getLogger(JdbcExternalConnectionPool.class);

  private static JdbcExternalConnectionPool instance;

  private final static String CONFIG_FILE_NAME = "connectionPool.properties";

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

    String classPathLocation = OBConfigFileProvider.getInstance().getClassPathLocation();

    File propertiesURL = new File(classPathLocation, CONFIG_FILE_NAME);
    PoolProperties poolProperties = new PoolProperties();
    poolProperties.setUrl(obUrl + "/" + sid);
    poolProperties.setDriverClassName(driver);
    poolProperties.setUsername(username);
    poolProperties.setPassword(password);

    // Properties poolPropertiesConfig = new Properties();
    // try {
    // FileInputStream fis = new FileInputStream(propertiesURL);
    // poolPropertiesConfig.load(fis);
    // } catch (IOException e) {
    // log.error("Error while loading connection pool properties", e);
    // }

    poolProperties.setJmxEnabled(true);
    poolProperties.setTestWhileIdle(false);
    poolProperties.setTestOnBorrow(true);
    poolProperties.setValidationQuery("SELECT 1");
    poolProperties.setTestOnReturn(false);
    poolProperties.setValidationInterval(30000);
    poolProperties.setTimeBetweenEvictionRunsMillis(30000);
    poolProperties.setInitialSize(5);
    poolProperties.setMaxActive(30);
    poolProperties.setMaxIdle(30);
    poolProperties.setMaxWait(10000);
    poolProperties.setMinEvictableIdleTimeMillis(30000);
    poolProperties.setMinIdle(10);
    poolProperties.setLogAbandoned(false);
    poolProperties.setRemoveAbandoned(true);
    poolProperties.setRemoveAbandonedTimeout(60);
    poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
        + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
        + "org.openbravo.externalconnectionpool.TestInterceptor;");
    return poolProperties;
  }

  private File getFileFromDevelopmentPath(String fileName) {
    // get the location of the current class file
    final URL url = this.getClass().getResource(getClass().getSimpleName() + ".java");
    File f = new File(url.getPath());
    File propertiesFile = null;
    while (f.getParentFile() != null && f.getParentFile().exists()) {
      f = f.getParentFile();
      final File configDirectory = new File(f, "config");
      if (configDirectory.exists()) {
        propertiesFile = new File(configDirectory, fileName);
        if (propertiesFile.exists()) {
          // found it and break
          break;
        }
      }
    }
    return propertiesFile;
  }
}
