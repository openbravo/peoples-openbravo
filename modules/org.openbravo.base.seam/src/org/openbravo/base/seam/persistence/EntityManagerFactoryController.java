/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.seam.persistence;

import java.io.ByteArrayInputStream;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.EntityManagerFactoryImpl;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.dal.core.DalMappingGenerator;
import org.openbravo.dal.core.OBInterceptor;

/**
 * Initializes the Openbravo Data Access Layer and provides an entity manager factory to be used in
 * a Seam environment.
 * 
 * Openbravo uses a SessionFactory (encapsulated by the EntityManagerFactory). Seam and Openbravo
 * use the same Session/EntityManager. The Openbravo transaction handling detects that its session
 * is running in a Seam context and will leave transaction handling to Seam.
 * 
 * @author mtaal
 */
@Name("entityManagerFactoryController")
@Startup
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.APPLICATION)
public class EntityManagerFactoryController extends SessionFactoryController implements OBSingleton {

  private static EntityManagerFactoryController instance;

  /**
   * Retrieves the PersistenceManagerFactoryController from the Seam context and returns it. Caches
   * it as a static as the component is application scoped.
   */
  public static EntityManagerFactoryController getInstance() {
    if (instance == null) {
      instance = (EntityManagerFactoryController) Component
          .getInstance("entityManagerFactoryController");
    }
    return instance;
  }

  @Logger
  private Log log;

  private EntityManagerFactoryImpl entityManagerFactory;

  private Ejb3Configuration ejb3Configuration;

  protected void mapModel(Ejb3Configuration configuration) {
    final String mapping = DalMappingGenerator.getInstance().generateMapping();
    // log.debug("Generated mapping: ");
    // log.debug(mapping);
    try {
      final ByteArrayInputStream is = new ByteArrayInputStream(mapping.getBytes("UTF-8"));
      configuration.addInputStream(is);
      is.close();
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  protected void mapModel(Configuration configuration) {
    throw new UnsupportedOperationException("This method should not have been called");
  }

  protected void setInterceptor(Ejb3Configuration configuration) {
    ejb3Configuration.setInterceptor(new OBInterceptor());
  }

  public SessionFactory getSessionFactory() {
    initialize();
    return entityManagerFactory.getSessionFactory();
  }

  public Configuration getConfiguration() {
    initialize();
    return ejb3Configuration.getHibernateConfiguration();
  }

  /**
   * Resets and initializes the SessionFactory. If there is a SessionFactory then this one is first
   * closed before a new one is created.
   */
  public void reInitialize() {
    if (entityManagerFactory != null) {
      entityManagerFactory.close();
      entityManagerFactory = null;
    }
    initialize();
  }

  /**
   * Creates a new Hibernate Configuration, generates a mapping and initializes the SessionFactory.
   */
  public void initialize() {
    if (entityManagerFactory != null) {
      return;
    }

    log.debug("Initializing entityManager factory");

    // TODO: mapping is automatically generated and should depend on the
    // modules/tables
    // which are actually used
    // NOTE: reads the hibernate.properties in the root of the classpath
    try {
      ejb3Configuration = new Ejb3Configuration();
      mapModel(ejb3Configuration);
      setInterceptor(ejb3Configuration);

      ejb3Configuration.addProperties(getOpenbravoProperties());

      // second-level caching is disabled for now because not all data
      // access and updates go through hibernate.
      ejb3Configuration.getProperties().setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
      ejb3Configuration.getProperties().setProperty(Environment.USE_QUERY_CACHE, "false");

      entityManagerFactory = (EntityManagerFactoryImpl) ejb3Configuration
          .buildEntityManagerFactory();

      log.debug("Entity Manager Factory initialized");
    } catch (final Throwable t) {
      // this is done to get better visibility of the exceptions
      t.printStackTrace(System.err);
      throw new OBException(t);
    }
  }

  @Override
  public boolean isInitialized() {
    return entityManagerFactory != null;
  }

  public EntityManagerFactory getEntityManagerFactory() {
    return entityManagerFactory;
  }

}
