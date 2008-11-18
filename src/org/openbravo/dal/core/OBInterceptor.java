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

package org.openbravo.dal.core;

import static org.openbravo.model.ad.system.Client.PROPERTY_ORGANIZATION;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_CLIENT;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_CREATED;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_CREATEDBY;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_UPDATED;
import static org.openbravo.model.common.enterprise.Organization.PROPERTY_UPDATEDBY;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganizationEnabled;
import org.openbravo.base.structure.Traceable;
import org.openbravo.dal.security.SecurityChecker;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

/**
 * This event listener catches save or update events to set the client and
 * organisation and the updated/created fields. In addition security checks are
 * done.
 * 
 * @author mtaal
 */

public class OBInterceptor extends EmptyInterceptor {
    private static final Logger log = Logger.getLogger(OBInterceptor.class);

    private static final long serialVersionUID = 1L;

    @Override
    public Boolean isTransient(Object entity) {
        // special case, if the id is set but it was explicitly
        // set to new then return new
        if (entity instanceof BaseOBObject) {
            final BaseOBObject bob = (BaseOBObject) entity;
            if (bob.getId() != null && bob.isNewOBObject()) {
                return new Boolean(true);
            }
        }
        // let hibernate do the rest
        return null;
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) {
        SecurityChecker.getInstance().checkDeleteAllowed(entity);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id,
            Object[] currentState, Object[] previousState,
            String[] propertyNames, Type[] types) {

        // disabled for now, checks are all done when a property is set
        // if (entity instanceof BaseOBObject) {
        // ((BaseOBObject) entity).validate();
        // }

        doEvent(entity, currentState, propertyNames);

        checkReferencedOrganisations(entity, currentState, previousState,
                propertyNames);

        if (entity instanceof Traceable) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onSave(Object entity, Serializable id,
            Object[] currentState, String[] propertyNames, Type[] types) {
        // disabled for now, checks are all done when a property is set
        // if (entity instanceof BaseOBObject) {
        // ((BaseOBObject) entity).validate();
        // }

        doEvent(entity, currentState, propertyNames);

        if (entity instanceof Traceable || entity instanceof ClientEnabled
                || entity instanceof OrganizationEnabled) {
            return true;
        }
        return false;
    }

    private void checkReferencedOrganisations(Object entity,
            Object[] currentState, Object[] previousState,
            String[] propertyNames) {
        if (!(entity instanceof OrganizationEnabled)) {
            return;
        }
        final Organization o1 = ((OrganizationEnabled) entity)
                .getOrganization();
        final OBContext obContext = OBContext.getOBContext();
        final BaseOBObject bob = (BaseOBObject) entity;
        boolean isNew = bob.getId() == null || bob.isNewOBObject();

        // check if the organisation of the current object has changed, if so
        // then
        // check all references
        if (!isNew) {
            for (int i = 0; i < currentState.length; i++) {
                if (propertyNames[i].equals(PROPERTY_ORGANIZATION)) {
                    if (currentState[i] != previousState[i]) {
                        isNew = true;
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < currentState.length; i++) {
            // TODO maybe use equals
            if ((isNew || currentState[i] != previousState[i])
                    && !(currentState[i] instanceof Organization)
                    && (currentState[i] instanceof BaseOBObject || currentState[i] instanceof HibernateProxy)
                    && currentState[i] instanceof OrganizationEnabled) {
                // get the organisation from the current state
                final OrganizationEnabled oe = (OrganizationEnabled) currentState[i];
                final Organization o2 = oe.getOrganization();
                if (!obContext.getOrganisationStructureProvider(
                        o1.getClient().getId()).isInNaturalTree(o1, o2)) {
                    throw new OBSecurityException("Entity "
                            + bob.getIdentifier() + " (" + bob.getEntityName()
                            + ") with organisation " + o1.getIdentifier()
                            + " references an entity "
                            + ((BaseOBObject) currentState[i]).getIdentifier()
                            + " through its property " + propertyNames[i]
                            + " but this referenced entity"
                            + " belongs to an organisation "
                            + o2.getIdentifier()
                            + " which is not part of the natural tree of "
                            + o1.getIdentifier());
                }
            }
        }
    }

    // general event handler does new and update
    protected void doEvent(Object o, Object[] currentState,
            String[] propertyNames) {
        try {
            // not traceable but still do the security check
            if (!(o instanceof Traceable)) {
                // do a check for writeaccess
                // TODO: the question is if this is the correct
                // location as because of hibernate cascade many things are
                // written.
                SecurityChecker.getInstance().checkWriteAccess(o);
                return;
            }

            final Traceable t = (Traceable) o;
            if (t.getCreated() == null) { // new
                onNew(t, propertyNames, currentState);
            } else {
                onUpdate(t, propertyNames, currentState);
            }
        } catch (Exception e) {
            final Exception originalException = e;
            e.printStackTrace(System.err);
            while (e instanceof SQLException) {
                e = ((SQLException) e).getNextException();
                e.printStackTrace(System.err);
            }
            throw new OBException(originalException);
        }

        // do a check for writeaccess
        // TODO: the question is if this is the correct
        // location as because of hibernate cascade many things are written.
        SecurityChecker.getInstance().checkWriteAccess(o);
    }

    // set created/createdby and the client and organisation
    private void onNew(Traceable t, String[] propertyNames,
            Object[] currentState) {
        final OBContext obContext = OBContext.getOBContext();
        final User currentUser = obContext.getUser();
        log.debug("OBEvent for new object " + t.getClass().getName() + " user "
                + currentUser.getName());

        Client client = null;
        Organization org = null;
        if (t instanceof ClientEnabled || t instanceof OrganizationEnabled) {
            // reread the client and organisation
            client = SessionHandler.getInstance().find(Client.class,
                    obContext.getCurrentClient().getId());
            org = SessionHandler.getInstance().find(Organization.class,
                    obContext.getCurrentOrganisation().getId());
        }
        for (int i = 0; i < propertyNames.length; i++) {
            if ("".equals(propertyNames[i])) {
                currentState[i] = new Date();
            }
            if (PROPERTY_UPDATED.equals(propertyNames[i])) {
                currentState[i] = new Date();
            }
            if (PROPERTY_UPDATEDBY.equals(propertyNames[i])) {
                currentState[i] = currentUser;
            }
            if (PROPERTY_CREATED.equals(propertyNames[i])) {
                currentState[i] = new Date();
            }
            if (PROPERTY_CREATEDBY.equals(propertyNames[i])) {
                currentState[i] = currentUser;
            }
            if (PROPERTY_CLIENT.equals(propertyNames[i])
                    && currentState[i] == null) {
                currentState[i] = client;
            }
            if (PROPERTY_ORGANIZATION.equals(propertyNames[i])
                    && currentState[i] == null) {
                currentState[i] = org;
            }
        }
    }

    // Sets the updated/updatedby
    // TODO: can the client/organisation change?
    protected void onUpdate(Traceable t, String[] propertyNames,
            Object[] currentState) {
        final User currentUser = OBContext.getOBContext().getUser();
        log.debug("OBEvent for updated object " + t.getClass().getName()
                + " user " + currentUser.getName());
        for (int i = 0; i < propertyNames.length; i++) {
            if (PROPERTY_UPDATED.equals(propertyNames[i])) {
                currentState[i] = new Date();
            }
            if (PROPERTY_UPDATEDBY.equals(propertyNames[i])) {
                currentState[i] = currentUser;
            }
        }
    }
}