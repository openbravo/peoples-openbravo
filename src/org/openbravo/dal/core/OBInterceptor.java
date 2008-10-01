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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.ad.Client;
import org.openbravo.base.model.ad.Org;
import org.openbravo.base.model.ad.User;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganisationEnabled;
import org.openbravo.base.structure.Traceable;
import org.openbravo.dal.security.SecurityChecker;

/**
 * This event listener catches save or update events to set the client and
 * organisation and the updated/created fields. In addition security checks are
 * done.
 * 
 * TODO: also the delete event should be caught for security checking
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
  public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
    SecurityChecker.getInstance().checkDeleteAllowed(entity);
  }
  
  @Override
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
    
    if (entity instanceof BaseOBObject) {
      ((BaseOBObject) entity).validate();
    }
    
    doEvent(entity, currentState, propertyNames);
    
    checkReferencedOrganisations(entity, currentState, previousState, propertyNames);
    
    if (entity instanceof Traceable) {
      return true;
    }
    return false;
  }
  
  @Override
  public boolean onSave(Object entity, Serializable id, Object[] currentState, String[] propertyNames, Type[] types) {
    if (entity instanceof BaseOBObject) {
      ((BaseOBObject) entity).validate();
    }
    
    doEvent(entity, currentState, propertyNames);
    
    if (entity instanceof Traceable || entity instanceof ClientEnabled || entity instanceof OrganisationEnabled) {
      return true;
    }
    return false;
  }
  
  private void checkReferencedOrganisations(Object entity, Object[] currentState, Object[] previousState, String[] propertyNames) {
    if (!(entity instanceof OrganisationEnabled)) {
      return;
    }
    final Org o1 = ((OrganisationEnabled) entity).getOrg();
    final OBContext obContext = OBContext.getOBContext();
    final BaseOBObject bob = (BaseOBObject) entity;
    boolean isNew = bob.getId() == null || bob.isNewOBObject();
    
    // check if the organisation of the current object has changed, if so then
    // check all references
    if (!isNew) {
      for (int i = 0; i < currentState.length; i++) {
        if (propertyNames[i].equals("org")) {
          if (currentState[i] != previousState[i]) {
            isNew = true;
            break;
          }
        }
      }
    }
    
    for (int i = 0; i < currentState.length; i++) {
      // TODO maybe use equals
      if ((isNew || currentState[i] != previousState[i]) && !(currentState[i] instanceof Org) && (currentState[i] instanceof BaseOBObject || currentState[i] instanceof HibernateProxy) && currentState[i] instanceof OrganisationEnabled) {
        // get the organisation from the current state
        final OrganisationEnabled oe = (OrganisationEnabled) currentState[i];
        final Org o2 = oe.getOrg();
        if (!obContext.getOrganisationStructureProvider().isInNaturalTree(o1, o2)) {
          throw new OBSecurityException("Entity " + bob.getIdentifier() + " (" + bob.getEntityName() + ") with organisation " + o1.getIdentifier() + " references an entity " + ((BaseOBObject) currentState[i]).getIdentifier() + " through its property " + propertyNames[i] + " but this referenced entity " + " belongs to an organisation " + o2.getIdentifier() + " which is not part of the natural tree of " + o1.getIdentifier());
        }
      }
    }
  }
  
  // general event handler does new and update
  protected void doEvent(Object o, Object[] currentState, String[] propertyNames) {
    try {
      // not traceable but still do the security check
      if (!(o instanceof Traceable)) {
        // do a check for writeaccess
        // TODO: the question is if this is the correct
        // location as because of hibernate cascade many things are written.
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
  private void onNew(Traceable t, String[] propertyNames, Object[] currentState) {
    
    final OBContext obContext = OBContext.getOBContext();
    final User currentUser = obContext.getUser();
    log.debug("OBEvent for new object " + t.getClass().getName() + " user " + currentUser.getName());
    
    Client client = null;
    Org org = null;
    if (t instanceof ClientEnabled || t instanceof OrganisationEnabled) {
      // reread the client and organisation
      client = SessionHandler.getInstance().find(Client.class, obContext.getCurrentClient().getId());
      org = SessionHandler.getInstance().find(Org.class, obContext.getCurrentOrganisation().getId());
    }
    for (int i = 0; i < propertyNames.length; i++) {
      if ("created".equals(propertyNames[i])) {
        currentState[i] = new Date();
      }
      if ("createdby".equals(propertyNames[i])) {
        currentState[i] = currentUser;
      }
      if ("updated".equals(propertyNames[i])) {
        currentState[i] = new Date();
      }
      if ("updatedby".equals(propertyNames[i])) {
        currentState[i] = currentUser;
      }
      if ("client".equals(propertyNames[i])) {
        currentState[i] = client;
      }
      if ("organisation".equals(propertyNames[i])) {
        currentState[i] = org;
      }
    }
  }
  
  // Sets the updated/updatedby
  // TODO: can the client/organisation change?
  protected void onUpdate(Traceable t, String[] propertyNames, Object[] currentState) {
    final User currentUser = OBContext.getOBContext().getUser();
    log.debug("OBEvent for updated object " + t.getClass().getName() + " user " + currentUser.getName());
    for (int i = 0; i < propertyNames.length; i++) {
      if ("updated".equals(propertyNames[i])) {
        currentState[i] = new Date();
      }
      if ("updatedby".equals(propertyNames[i])) {
        currentState[i] = currentUser;
      }
    }
  }
}