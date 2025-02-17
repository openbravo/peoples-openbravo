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
 * All portions are Copyright (C) 2008-2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.dal;

import static org.junit.Assert.fail;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.type.Type;
import org.junit.Test;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalSessionFactoryController;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.test.base.OBBaseTest;

/**
 * Test for updates which can happen behind the scenes (but should not happen) if properties are
 * accidentally changed. An example of this is that a boolean field has a null in the database, then
 * when hibernate reads it from the database as a default false is set in that field. Then when
 * checking for dirty the object has changed as the boolean value has changed from null to false.
 * 
 * @author mtaal
 */

public class HiddenUpdateTest extends OBBaseTest {

  private static final Logger log = LogManager.getLogger();

  /**
   * Tests for hidden updates. Hidden updates can occur when a load/read of an entity also changes
   * the state, or that hibernate detects dirty in another way. Use the Hibernate Interceptor
   * concept.
   */
  @Test
  public void testHiddenUpdates() {

    setSystemAdministratorContext();

    final SessionFactoryController currentSFC = SessionFactoryController.getInstance();
    try {
      // close session created with previous SessionFactoryController
      SessionHandler.getInstance().commitAndClose();

      final SessionFactoryController newSFC = new LocalSessionFactoryController();
      SessionFactoryController.setInstance(newSFC);
      SessionFactoryController.getInstance().reInitialize();

      Metadata metadata = LocalMetadataExtractorIntegrator.getInstance().getMetadata();

      log.info("Checking hidden updates...");
      for (PersistentClass pc : metadata.getEntityBindings()) {
        final String entityName = pc.getEntityName();

        /*
         * Issue 50879: db_checksum column (only) on oracle instances contains invalid (for xml)
         * characters Since jdk19 xml parser rejects those so skip that specific table
         */
        if (entityName.equals("ADSystemInformation")) {
          continue;
        }

        Entity entity = ModelProvider.getInstance().getEntity(entityName);
        // can also ignore views as they will result in errors anyway and they are mapped as not
        // updateable
        if (entity.isHQLBased() || entity.isDataSourceBased() || entity.isView()) {
          continue;
        }

        log.info("Checking entity {}", entityName);
        // read max 5 records from each type, should be enough to limit runtime of the test and
        // still give good results.
        final OBCriteria<BaseOBObject> criteria = OBDal.getInstance().createCriteria(entityName);
        criteria.setMaxResults(5);
        for (final Object o : criteria.list()) {
          if (o == null) {
            // can occur when reading views which have nullable columns in a multi-column pk
            continue;
          }
          EntityXMLConverter.newInstance().toXML((BaseOBObject) o);
        }
        SessionHandler.getInstance().commitAndClose();
      }
    } finally {
      SessionFactoryController.setInstance(currentSFC);
      SessionFactoryController.getInstance().reInitialize();
    }

  }

  private class LocalSessionFactoryController extends DalSessionFactoryController {
    @Override
    public Configuration buildConfiguration() {
      return new Configuration(new BootstrapServiceRegistryBuilder()
          .applyIntegrator(LocalMetadataExtractorIntegrator.getInstance())
          .build());
    }

    @Override
    protected void setInterceptor(Configuration configuration) {
      configuration.setInterceptor(new LocalInterceptor());
    }
  }

  private class LocalInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames,
        Type[] types) {
      return false;
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames,
        Type[] types) {
      fail();
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
        Object[] previousState, String[] propertyNames, Type[] types) {
      fail();
      return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
        Type[] types) {
      fail();
      return false;
    }

    @Override
    public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
      fail();
    }

    @Override
    public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
      fail();
    }

    @Override
    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
      fail();
    }
  }

  private static class LocalMetadataExtractorIntegrator implements Integrator {

    private static LocalMetadataExtractorIntegrator instance = new LocalMetadataExtractorIntegrator();

    private Metadata metadata;

    public static LocalMetadataExtractorIntegrator getInstance() {
      return instance;
    }

    public Metadata getMetadata() {
      return metadata;
    }

    @Override
    public void integrate(Metadata md, SessionFactoryImplementor sessionFactory,
        SessionFactoryServiceRegistry serviceRegistry) {
      if (this.metadata != null) {
        return;
      }
      this.metadata = md;
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory,
        SessionFactoryServiceRegistry serviceRegistry) {
      // Do not require to implement
    }

  }
}
