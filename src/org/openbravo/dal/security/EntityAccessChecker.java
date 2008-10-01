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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.ad.Tab;
import org.openbravo.base.model.ad.TableAccess;
import org.openbravo.base.model.ad.Window;
import org.openbravo.base.model.ad.WindowAccess;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;

/**
 * Uses window/tab access information to determine which entities are writable
 * and readable by a user.
 * 
 * @author mtaal
 */

public class EntityAccessChecker {
  
  private String roleId;
  
  private Set<String> writableEntities = new HashSet<String>();
  private Set<String> readableEntities = new HashSet<String>();
  private Set<String> nonReadableEntities = new HashSet<String>();
  
  public void initialize() {
    
    final ModelProvider mp = ModelProvider.getInstance();
    // Don't use dal because otherwise we can end up in infinite loops
    final String qryStr = "select wa from " + WindowAccess.class.getName() + " wa where role.id='" + getRoleId() + "'";
    final Query qry = SessionHandler.getInstance().createQuery(qryStr);
    @SuppressWarnings("unchecked")
    final List<WindowAccess> was = qry.list();
    for (WindowAccess wa : was) {
      final Window w = wa.getWindow();
      final boolean writeAccess = wa.isReadwrite();
      // get the ttabs
      final String tfQryStr = "select t from " + Tab.class.getName() + " t where window.id='" + w.getId() + "'";
      @SuppressWarnings("unchecked")
      final List<Tab> ts = SessionHandler.getInstance().createQuery(tfQryStr).list();
      for (Tab t : ts) {
        final String tableName = t.getTable().getName();
        final Entity e = mp.getEntityByTableName(tableName);
        if (e == null) { // happens for AD_Client_Info
          continue;
        }
        if (writeAccess) {
          writableEntities.add(e.getName());
          readableEntities.add(e.getName());
        } else {
          readableEntities.add(e.getName());
        }
      }
    }
    
    // and take into account table access
    final String tafQryStr = "select ta from " + TableAccess.class.getName() + " ta where role.id='" + getRoleId() + "'";
    @SuppressWarnings("unchecked")
    final List<TableAccess> tas = SessionHandler.getInstance().createQuery(tafQryStr).list();
    for (TableAccess ta : tas) {
      final String tableName = ta.getTable().getName();
      final Entity e = mp.getEntityByTableName(tableName);
      
      if (ta.isExclude()) {
        readableEntities.remove(e.getName());
        writableEntities.remove(e.getName());
        nonReadableEntities.add(e.getName());
      } else if (ta.isReadonly()) {
        writableEntities.remove(e.getName());
      }
    }
  }
  
  public void checkWritable(Entity entity) {
    if (OBContext.getOBContext().isAdministrator() || OBContext.getOBContext().isInAdministratorMode()) {
      return;
    }
    
    if (!writableEntities.contains(entity.getName())) {
      throw new OBSecurityException("Entity " + entity + " is not writable by this user");
    }
  }
  
  public void checkReadable(Entity entity) {
    if (OBContext.getOBContext().isAdministrator() || OBContext.getOBContext().isInAdministratorMode()) {
      return;
    }
    
    if (nonReadableEntities.contains(entity.getName())) {
      throw new OBSecurityException("Entity " + entity + " is not readable by this user");
    }
    
    if (false && !readableEntities.contains(entity.getName())) {
      throw new OBSecurityException("Entity " + entity + " is not readable by this user");
    }
  }
  
  public String getRoleId() {
    return roleId;
  }
  
  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }
  
}