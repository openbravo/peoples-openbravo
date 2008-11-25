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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.UniqueConstraint;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.security.SecurityChecker;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * The OBDal class offers the external access to the Dal layer.
 * 
 * TODO: add methods to return a sorted list based on the identifier of an
 * object
 * 
 * TODO: re-check singleton pattern when a new factory/dependency injection
 * approach is implemented.
 * 
 * @author mtaal
 */

public class OBDal implements OBSingleton {
    private static final Logger log = Logger.getLogger(OBDal.class);

    private static OBDal instance;

    public static OBDal getInstance() {
        if (instance == null) {
            instance = OBProvider.getInstance().get(OBDal.class);
        }
        return instance;
    }

    // commits the transaction and closes the session
    public void commitAndClose() {
        SessionHandler.getInstance().commitAndClose();
    }

    // rolls back the transaction and closes the session
    public void rollbackAndClose() {
        SessionHandler.getInstance().rollback();
    }

    // flush the current state to the db
    public void flush() {
        SessionHandler.getInstance().getSession().flush();
    }

    public void save(Object o) {
        // set client organisation has to be done here before checking write
        // access
        // not the most nice to do
        // TODO: add checking if setClientOrganisation is really necessary
        // TODO: log using entityName
        log.debug("Saving object " + o.getClass().getName());
        setClientOrganisation(o);
        if (o instanceof BaseOBObject) {
            OBContext.getOBContext().getEntityAccessChecker().checkWritable(
                    ((BaseOBObject) o).getEntity());
        }
        SecurityChecker.getInstance().checkWriteAccess(o);
        SessionHandler.getInstance().save(o);
    }

    public void remove(Object o) {
        // TODO: add checking if setClientOrganisation is really necessary
        // TODO: log using entityName
        log.debug("Removing object " + o.getClass().getName());

        if (o instanceof BaseOBObject) {
            final Entity entity = ((BaseOBObject) o).getEntity();
            SecurityChecker.getInstance().checkDeleteAllowed(o);
            OBContext.getOBContext().getEntityAccessChecker().checkWritable(
                    entity);
        }
        SecurityChecker.getInstance().checkWriteAccess(o);
        SessionHandler.getInstance().delete(o);
    }

    public <T extends Object> T get(Class<T> clazz, Object id) {
        checkReadAccess(clazz);
        return SessionHandler.getInstance().find(clazz, id);
    }

    public boolean exists(String entityName, Object id) {
        return null != SessionHandler.getInstance().find(entityName, id);
    }

    public BaseOBObject get(String entityName, Object id) {
        checkReadAccess(entityName);
        return SessionHandler.getInstance().find(entityName, id);
    }

    public <T extends BaseOBObject> OBQuery<T> createQuery(Class<T> fromClz,
            String whereOrderByClause) {
        return createQuery(fromClz, whereOrderByClause, new ArrayList<Object>());
    }

    public <T extends BaseOBObject> OBQuery<T> createQuery(Class<T> fromClz,
            String whereOrderByClause, List<Object> parameters) {
        checkReadAccess(fromClz);
        final OBQuery<T> obQuery = new OBQuery<T>();
        obQuery.setWhereAndOrderBy(whereOrderByClause);
        obQuery.setEntity(ModelProvider.getInstance().getEntity(fromClz));
        obQuery.setParameters(parameters);
        return obQuery;
    }

    public OBQuery<BaseOBObject> createQuery(String entityName,
            String whereOrderByClause) {
        return createQuery(entityName, whereOrderByClause,
                new ArrayList<Object>());
    }

    public OBQuery<BaseOBObject> createQuery(String entityName,
            String whereOrderByClause, List<Object> parameters) {
        checkReadAccess(entityName);
        final OBQuery<BaseOBObject> obQuery = new OBQuery<BaseOBObject>();
        obQuery.setWhereAndOrderBy(whereOrderByClause);
        obQuery.setEntity(ModelProvider.getInstance().getEntity(entityName));
        obQuery.setParameters(parameters);
        return obQuery;
    }

    public <T extends BaseOBObject> OBCriteria<T> createCriteria(Class<T> clz) {
        checkReadAccess(clz);
        final OBCriteria<T> obCriteria = new OBCriteria<T>();
        obCriteria.setCriteria(SessionHandler.getInstance().getSession()
                .createCriteria(clz));
        obCriteria.setEntity(ModelProvider.getInstance().getEntity(clz));
        return obCriteria;
    }

    public <T extends BaseOBObject> OBCriteria<T> createCriteria(
            String entityName) {
        checkReadAccess(entityName);
        final OBCriteria<T> obCriteria = new OBCriteria<T>();
        obCriteria.setCriteria(SessionHandler.getInstance().getSession()
                .createCriteria(entityName));
        obCriteria.setEntity(ModelProvider.getInstance().getEntity(entityName));
        return obCriteria;
    }

    /**
     * Retrieves a list of baseOBObjects using the uniqueconstraints defined for
     * the entity. The passed BaseOBObject and the uniqueconstraints are used to
     * construct a query searching for matching objects in the db.
     */
    public List<BaseOBObject> findUniqueConstrainedObjects(BaseOBObject obObject) {
        final Entity entity = obObject.getEntity();
        final List<BaseOBObject> result = new ArrayList<BaseOBObject>();
        final Object id = obObject.getId();
        for (final UniqueConstraint uc : entity.getUniqueConstraints()) {
            final OBCriteria<BaseOBObject> criteria = createCriteria(entity
                    .getName());
            if (id != null) {
                criteria.add(Expression.ne("id", id));
            }
            for (final Property p : uc.getProperties()) {
                final Object value = obObject.getValue(p.getName());
                criteria.add(Expression.eq(p.getName(), value));
            }
            final List<BaseOBObject> queryResult = criteria.list();
            // this is not fast, but the list should be small normally
            // if performance becomes a problem then a hashset should
            // be used.
            for (final BaseOBObject queriedObject : queryResult) {
                if (!result.contains(queriedObject)) {
                    result.add(queriedObject);
                }
            }
        }

        return result;
    }

    // TODO: this is maybe not the best location for this functionality??
    protected void setClientOrganisation(Object o) {
        final OBContext obContext = OBContext.getOBContext();
        if (o instanceof ClientEnabled) {
            final ClientEnabled ce = (ClientEnabled) o;
            // reread the client
            if (ce.getClient() == null) {
                final Client client = SessionHandler.getInstance().find(
                        Client.class, obContext.getCurrentClient().getId());
                ce.setClient(client);
            }
        }
        if (o instanceof OrganizationEnabled) {
            final OrganizationEnabled oe = (OrganizationEnabled) o;
            // reread the client and organisation
            if (oe.getOrganization() == null) {
                final Organization org = SessionHandler.getInstance().find(
                        Organization.class,
                        obContext.getCurrentOrganisation().getId());
                oe.setOrganization(org);
            }
        }
    }

    private void checkReadAccess(Class<?> clz) {
        checkReadAccess(DalUtil.getEntityName(clz));
    }

    private void checkReadAccess(String entityName) {
        final Entity e = ModelProvider.getInstance().getEntity(entityName);
        OBContext.getOBContext().getEntityAccessChecker().checkReadable(e);
    }
}