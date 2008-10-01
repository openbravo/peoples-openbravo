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

package org.openbravo.dal.security;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.base.structure.OrganisationEnabled;
import org.openbravo.dal.core.DALUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;

/**
 * Centralizes checking of security for data objects.
 * 
 * @author mtaal
 */

public class SecurityChecker {
  
  private static SecurityChecker instance = new SecurityChecker();
  
  public static SecurityChecker getInstance() {
    return instance;
  }
  
  public static void setInstance(SecurityChecker instance) {
    SecurityChecker.instance = instance;
  }
  
  public void checkDeleteAllowed(Object o) {
    if (!OBContext.getOBContext().isAdministrator() && o instanceof BaseOBObject) {
      final BaseOBObject bob = (BaseOBObject) o;
      final Entity entity = ModelProvider.getInstance().getEntity(bob.getEntityName());
      if (!entity.isDeletable()) {
        throw new OBSecurityException("Entity " + entity.getName() + " is not deletable");
      }
    }
    checkWriteAccess(o);
  }
  
  public void checkWriteAccess(Object o) {
    
    // check that the client id and organisation id are resp. in the list of
    // user_client and user_org
    // TODO: throw specific and translated exception, for more info:
    // Utility.translateError(this, vars, vars.getLanguage(),
    // Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()))
    final OBContext obContext = OBContext.getOBContext();
    
    String clientId = "";
    if (o instanceof ClientEnabled) {
      clientId = (String) DALUtil.getId(((ClientEnabled) o).getClient());
    }
    String orgId = "";
    if (o instanceof OrganisationEnabled) {
      orgId = (String) DALUtil.getId(((OrganisationEnabled) o).getOrg());
    }
    
    if (!obContext.isAdministrator() && o instanceof ClientEnabled) {
      if (!obContext.getCurrentClient().getId().equals(clientId)) {
        // TODO: maybe move rollback to exception throwing
        SessionHandler.getInstance().setDoRollback(true);
        throw new OBSecurityException("Client (" + clientId + ") of object (" + o + ") is not present  in ClientList " + obContext.getCurrentClient().getId());
      }
    }
    
    // todo can be improved by only checking if the client or organisation
    // actually changed...
    final Entity entity = ((BaseOBObject) o).getModel();
    obContext.getEntityAccessChecker().checkWritable(entity);
    
    if (!obContext.isAdministrator() && o instanceof OrganisationEnabled) {
      // todo as only the id is required this can be made much more efficient by
      // not loading the hibernate proxy
      if (!obContext.getWritableOrganisations().contains(orgId)) {
        // TODO: maybe move rollback to exception throwing
        SessionHandler.getInstance().setDoRollback(true);
        throw new OBSecurityException("Organisation " + orgId + " of object (" + o + ") is not present  in OrganisationList " + obContext.getWritableOrganisations());
      }
    }
    
    // accesslevel check must also be done for administrators
    entity.checkAccessLevel(clientId, orgId);
  }
}