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

package org.openbravo.dal.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.ad.Client;
import org.openbravo.base.model.ad.Org;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganisationEnabled;
import org.openbravo.base.util.ModelUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.security.SecurityChecker;

/**
 * The OBDal class offers the external access to the Dal layer.
 * 
 * TODO: extend interface, possibly add security, add automatic filtering on
 * organisation/client add methods to return a sorted list based on the
 * identifier of an object
 * 
 * TODO: re-check singleton pattern when a new factory/dependency injection
 * approach is implemented.
 * 
 * @author mtaal
 */

public class OBDal {
  private static final Logger log = Logger.getLogger(OBDal.class);
  
  private static OBDal instance = new OBDal();
  
  public static OBDal getInstance() {
    return instance;
  }
  
  public static void setInstance(OBDal instance) {
    OBDal.instance = instance;
  }
  
  public void save(Object o) {
    // set client organisation has to be done here before checking write access
    // not the most nice to do
    // TODO: add checking if setClientOrganisation is really necessary
    // TODO: log using entityName
    log.debug("Saving object " + o.getClass().getName());
    setClientOrganisation(o);
    SecurityChecker.getInstance().checkWriteAccess(o);
    if (o instanceof BaseOBObject) {
      OBContext.getOBContext().getEntityAccessChecker().checkWritable(((BaseOBObject) o).getModel());
    }
    SessionHandler.getInstance().save(o);
  }
  
  public void remove(Object o) {
    // TODO: add checking if setClientOrganisation is really necessary
    // TODO: log using entityName
    log.debug("Removing object " + o.getClass().getName());
    setClientOrganisation(o);
    SecurityChecker.getInstance().checkWriteAccess(o);
    if (o instanceof BaseOBObject) {
      OBContext.getOBContext().getEntityAccessChecker().checkWritable(((BaseOBObject) o).getModel());
    }
    SessionHandler.getInstance().delete(o);
  }
  
  public <T extends Object> T get(Class<T> clazz, Object id) {
    checkReadAccess(clazz);
    return SessionHandler.getInstance().find(clazz, id);
  }
  
  public BaseOBObject get(String entityName, Object id) {
    checkReadAccess(entityName);
    return SessionHandler.getInstance().find(entityName, id);
  }
  
  public <T extends Object> List<T> list(Class<T> clz, OBFilter filter) {
    checkReadAccess(clz);
    final OBFilterQuery fq = new OBFilterQuery();
    fq.setSession(SessionHandler.getInstance().getSession());
    fq.setFilter(filter);
    
    log.debug("Querying for class: " + clz.getName());
    log.debug("Using filter " + filter.toString());
    return fq.list(clz);
  }
  
  public <T extends Object> int count(Class<T> clz, OBFilter filter) {
    checkReadAccess(clz);
    final OBFilterQuery fq = new OBFilterQuery();
    fq.setSession(SessionHandler.getInstance().getSession());
    fq.setFilter(filter);
    log.debug("Counting class: " + clz.getName());
    log.debug("Using filter " + filter.toString());
    return fq.count(clz);
  }
  
  public List<BaseOBObject> list(String entityName, OBFilter filter) {
    checkReadAccess(entityName);
    final OBFilterQuery fq = new OBFilterQuery();
    fq.setSession(SessionHandler.getInstance().getSession());
    fq.setFilter(filter);
    log.debug("Querying for entity: " + entityName);
    log.debug("Using filter " + filter.toString());
    return fq.list(entityName);
  }
  
  public int count(String entityName, OBFilter filter) {
    checkReadAccess(entityName);
    final OBFilterQuery fq = new OBFilterQuery();
    fq.setSession(SessionHandler.getInstance().getSession());
    fq.setFilter(filter);
    log.debug("Counting entity: " + entityName);
    log.debug("Using filter " + filter.toString());
    return fq.count(entityName);
  }
  
  // TODO: this is maybe not the best location for this functionality??
  protected void setClientOrganisation(Object o) {
    final OBContext obContext = OBContext.getOBContext();
    if (o instanceof ClientEnabled) {
      final ClientEnabled ce = (ClientEnabled) o;
      // reread the client
      if (ce.getClient() == null) {
        final Client client = SessionHandler.getInstance().find(Client.class, obContext.getCurrentClient().getId());
        ce.setClient(client);
      }
    }
    if (o instanceof OrganisationEnabled) {
      final OrganisationEnabled oe = (OrganisationEnabled) o;
      // reread the client and organisation
      if (oe.getOrg() == null) {
        final Org org = SessionHandler.getInstance().find(Org.class, obContext.getCurrentOrganisation().getId());
        oe.setOrg(org);
      }
    }
  }
  
  private void checkReadAccess(Class<?> clz) {
    checkReadAccess(ModelUtil.getEntityName(clz));
  }
  
  private void checkReadAccess(String entityName) {
    final Entity e = ModelProvider.getInstance().getEntity(entityName);
    OBContext.getOBContext().getEntityAccessChecker().checkReadable(e);
  }
}