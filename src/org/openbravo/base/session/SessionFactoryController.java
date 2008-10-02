/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.base.session;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cache.HashtableCacheProvider;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.PostgreSQLDialect;
import org.openbravo.base.exception.OBException;

/**
 * Initializes and provides the session factory.
 * 
 * @author mtaal
 */

public abstract class SessionFactoryController {
  private static final Logger log = Logger.getLogger(SessionFactoryController.class);
  
  private static SessionFactoryController instance = null;
  
  public static SessionFactoryController getInstance() {
    return instance;
  }
  
  public static void setInstance(SessionFactoryController sfc) {
    log.debug("Setting instance of " + sfc.getClass().getName() + " as session factory controller");
    instance = sfc;
  }
  
  private SessionFactory sessionFactory = null;
  private Configuration configuration = null;
  
  public SessionFactory getSessionFactory() {
    initialize();
    return sessionFactory;
  }
  
  public Configuration getConfiguration() {
    initialize();
    return configuration;
  }
  
  public void reInitialize() {
    if (sessionFactory != null) {
      sessionFactory.close();
      sessionFactory = null;
    }
    initialize();
  }
  
  public void initialize() {
    if (sessionFactory != null)
      return;
    
    log.debug("Initializing session factory");
    
    // TODO: mapping is automatically generated and should depend on the
    // modules/tables
    // which are actually used
    // NOTE: reads the hibernate.properties in the root of the classpath
    try {
      configuration = new Configuration();
      mapModel(configuration);
      setInterceptor(configuration);
      
      configuration.addProperties(getOpenbravoProperties());
      
      // add a default second level cache
      if (configuration.getProperties().get(Environment.CACHE_PROVIDER) == null) {
        configuration.getProperties().setProperty(Environment.CACHE_PROVIDER, HashtableCacheProvider.class.getName());
      }
      
      sessionFactory = configuration.buildSessionFactory();
      
      log.debug("Session Factory initialized");
    } catch (Throwable t) {
      // this is done to get better visibility of the exceptions
      t.printStackTrace(System.err);
      throw new OBException(t);
    }
  }
  
  protected abstract void mapModel(Configuration configuration);
  
  private Properties getOpenbravoProperties() {
    final Properties props = new Properties();
    final Properties obProps = OBPropertiesProvider.getInstance().getOpenbravoProperties();    
    if (obProps == null) {
      return new Properties();
    }
    
    if (obProps.getProperty("bbdd.rdbms") != null) {
      if (obProps.getProperty("bbdd.rdbms").equals("POSTGRE")) {
        return getPostgresHbProps(obProps);
      } else {
        return getOracleHbProps(obProps);
      }
    }
    return props;
  }
  
  private Properties getPostgresHbProps(Properties obProps) {
    final Properties props = new Properties();
    props.setProperty(Environment.DIALECT, PostgreSQLDialect.class.getName());
    props.setProperty(Environment.DRIVER, "org.postgresql.Driver");
    props.setProperty(Environment.URL, obProps.getProperty("bbdd.url") + "/" + obProps.getProperty("bbdd.sid"));
    props.setProperty(Environment.USER, obProps.getProperty("bbdd.user"));
    props.setProperty(Environment.PASS, obProps.getProperty("bbdd.password"));
    return props;
  }
  
  private Properties getOracleHbProps(Properties obProps) {
    final Properties props = new Properties();
    props.setProperty(Environment.DIALECT, OBOracle10gDialect.class.getName());
    props.setProperty(Environment.DRIVER, "oracle.jdbc.driver.OracleDriver");
    props.setProperty(Environment.URL, obProps.getProperty("bbdd.url"));
    props.setProperty(Environment.USER, obProps.getProperty("bbdd.user"));
    props.setProperty(Environment.PASS, obProps.getProperty("bbdd.password"));
    return props;
  }
  
  protected void setInterceptor(Configuration configuration) {
  }
}
