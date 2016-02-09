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
 * All portions are Copyright (C) 2008-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.Table;
import org.openbravo.base.provider.OBNotSingleton;
import org.openbravo.client.application.Parameter;
import org.openbravo.client.application.Process;
import org.openbravo.client.application.ProcessAccess;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.TableAccess;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.userinterface.selector.Selector;

/**
 * This class is responsible for determining the allowed read/write access for a combination of user
 * and Entity. It uses the window-role access information and the window-table relation to determine
 * which tables are readable and writable for a user. If the user has readWrite access to a Window
 * then also the related Table/Entity is writable.
 * <p/>
 * In addition this class implements the concept of derived readable. Any entity refered to from a
 * readable/writable entity is a derived readable. A user may read (but not write) the following
 * properties from a deriver readable entity: id and identifier properties. Access to any other
 * property or changing a property on a derived readable entity results in a OBSecurityException.
 * Derived readable checks are done when a value is retrieved of an object (@see
 * BaseOBObject#get(String)).
 * <p/>
 * This class is used from the {@link SecurityChecker} which combines all entity security checks.
 * 
 * @see Entity
 * @see Property
 * @see SecurityChecker
 * @author mtaal
 */

public class EntityAccessChecker implements OBNotSingleton {
  private static final Logger log = Logger.getLogger(EntityAccessChecker.class);

  private static final Object BUTTON_REFERENCE = "28";
  private static final Object SELECTOR_REFERENCE = "95E2A8B50A254B2AAE6774B8C2F28120";

  // Table Access Level:
  // "6";"System/Client"
  // "1";"Organization"
  // "3";"Client/Organization"
  // "4";"System only"
  // "7";"All"

  // User level:
  // "S";"System"
  // " C";"Client"
  // "  O";"Organization"
  // " CO";"Client+Organization"

  private String roleId;

  private Set<Entity> writableEntities = new HashSet<Entity>();

  private Set<Entity> readableEntities = new HashSet<Entity>();
  // the derived readable entities only contains the entities which are
  // derived
  // readable
  // the completely readable entities are present in the readableEntities
  private Set<Entity> derivedReadableEntities = new HashSet<Entity>();
  // the derived entities from process only contains the entities which are
  // derived from process definition
  private Set<Entity> derivedEntitiesFromProcess = new HashSet<Entity>();
  private Set<Entity> nonReadableEntities = new HashSet<Entity>();
  private boolean isInitialized = false;

  private OBContext obContext;

  /**
   * Reads the windows from the database using the current role of the user. Then it iterates
   * through the windows and tabs to determine which entities are readable/writable for that user.
   * In addition non-readable and derived-readable entities are computed. Besides derived entities
   * from process definition are being computed too.
   * 
   * @see ModelProvider
   */
  public synchronized void initialize() {
    OBContext.setAdminMode();
    try {
      final ModelProvider mp = ModelProvider.getInstance();
      final String userLevel = obContext.getUserLevel();

      // Don't use dal because otherwise we can end up in infinite loops
      // there is always only one windowaccess per role due to unique constraints
      final String qryStr = "select t.table.id, wa.editableField from " + Tab.class.getName()
          + " t left join t.window w left join w.aDWindowAccessList wa"
          + " where wa.role.id= :roleId";
      final Query qry = SessionHandler.getInstance().createQuery(qryStr);
      qry.setParameter("roleId", getRoleId());
      @SuppressWarnings("unchecked")
      final List<Object> tabData = qry.list();
      for (final Object o : tabData) {
        final Object[] os = (Object[]) o;

        final String tableId = (String) os[0];
        final Entity e = mp.getEntityByTableId(tableId);
        if (e == null) { // happens for AD_Client_Info and views
          continue;
        }

        final int accessLevel = e.getAccessLevel().getDbValue();
        if (!hasCorrectAccessLevel(userLevel, accessLevel)) {
          continue;
        }

        final boolean writeAccess = (Boolean) os[1];
        if (writeAccess) {
          writableEntities.add(e);
          readableEntities.add(e);
        } else {
          readableEntities.add(e);
        }
      }

      // and take into account table access
      final String tafQryStr = "select ta from " + TableAccess.class.getName()
          + " ta where role.id='" + getRoleId() + "'";
      @SuppressWarnings("unchecked")
      final List<TableAccess> tas = SessionHandler.getInstance().createQuery(tafQryStr).list();
      for (final TableAccess ta : tas) {
        final String tableName = ta.getTable().getName();
        final Entity e = mp.getEntity(tableName);

        if (ta.isExclude()) {
          readableEntities.remove(e);
          writableEntities.remove(e);
          nonReadableEntities.add(e);
        } else if (ta.isReadOnly()) {
          writableEntities.remove(e);
          readableEntities.add(e);
          nonReadableEntities.remove(e);
        } else {
          if (!writableEntities.contains(e)) {
            writableEntities.add(e);
          }
          if (!readableEntities.contains(e)) {
            readableEntities.add(e);
          }
          nonReadableEntities.remove(e);
        }
      }

      // and compute the derived readable
      for (final Entity e : new ArrayList<Entity>(readableEntities)) {
        for (final Property p : e.getProperties()) {
          if (p.getTargetEntity() != null && !readableEntities.contains(p.getTargetEntity())) {
            derivedReadableEntities.add(p.getTargetEntity());
            addDerivedReadableIdentifierProperties(p.getTargetEntity());
          }
        }
      }

      // and take into account derived entities from process definition
      // union of writableEntities and readableEntities
      List<Entity> processEntities = new ArrayList<Entity>(writableEntities);
      for (final Entity readableEntity : readableEntities) {
        if (!processEntities.contains(readableEntity)) {
          processEntities.add(readableEntity);
        }
      }
      for (final Entity entity : processEntities) {
        Table table = mp.getTableWithoutCheck(entity.getTableName());
        if (table == null) {
          continue;
        }
        // Processes invoked from selectors
        for (org.openbravo.base.model.Column col : table.getColumns()) {
          if (SELECTOR_REFERENCE.equals(col.getReference().getId())) {
            Reference ref = OBDal.getInstance().get(Reference.class,
                col.getReferenceValue().getId());
            if (ref != null) {
              // max one defined selector per reference
              Selector selector = ref.getOBUISELSelectorList().get(0);
              if (selector != null) {
                Process process = selector.getProcessDefintion();
                if (process != null) {
                  addDerivedEtityFromProcess(process);
                }
              }
            }
            // Processes invoked from buttons
          } else if (BUTTON_REFERENCE.equals(col.getReference().getId())) {
            Process process = OBDal.getInstance().get(Column.class, col.getId())
                .getOBUIAPPProcess();
            if (process == null) {
              continue;
            }
            addDerivedEtityFromProcess(process);
          }
        }
      }
      // and take into account explicit process access
      final String processAccessQryStr = "select p from " + ProcessAccess.class.getName()
          + " p where p.role.id='" + getRoleId() + "'";
      @SuppressWarnings("unchecked")
      final List<ProcessAccess> processAccessQuery = SessionHandler.getInstance()
          .createQuery(processAccessQryStr).list();
      for (final ProcessAccess processAccess : processAccessQuery) {
        addDerivedEtityFromProcess(processAccess.getObuiappProcess());
      }

      isInitialized = true;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Checks if a certain user access level and a certain data access level match. Meaning that with
   * a certain user access level it is allowed to view something with a certain data access level.
   * 
   * @param userLevel
   *          the user level as defined in the role of the user
   * @param accessLevel
   *          the data access level defined in the table
   * @return true if access is allowed, false otherwise
   */
  private boolean hasCorrectAccessLevel(String userLevel, int accessLevel) {
    // copied from HttpSecureAppServlet.
    if (accessLevel == 4 && userLevel.indexOf("S") == -1) {
      return false;
    } else if (accessLevel == 1 && userLevel.indexOf("O") == -1) {
      return false;
    } else if (accessLevel == 3
        && (!(userLevel.indexOf("C") != -1 || userLevel.indexOf("O") != -1))) {
      return false;
    } else if (accessLevel == 6
        && (!(userLevel.indexOf("S") != -1 || userLevel.indexOf("C") != -1))) {
      return false;
    }
    return true;
  }

  /**
   * Dumps the readable, writable, derived readable entities to the System.err outputstream. For
   * debugging purposes.
   */
  public void dump() {
    log.info("");
    log.info(">>> Readable entities: ");
    log.info("");
    dumpSorted(readableEntities);

    log.info("");
    log.info(">>> Derived Readable entities: ");
    log.info("");
    dumpSorted(derivedReadableEntities);

    log.info("");
    log.info(">>> Derived entities from process: ");
    log.info("");
    dumpSorted(derivedEntitiesFromProcess);

    log.info("");
    log.info(">>> Writable entities: ");
    log.info("");
    dumpSorted(writableEntities);
    log.info("");
    log.info("");

    final Set<Entity> readableNotWritable = new HashSet<Entity>(readableEntities);
    readableNotWritable.removeAll(writableEntities);

    log.info("");
    log.info(">>> Readable Not-Writable entities: ");
    log.info("");
    dumpSorted(readableNotWritable);
    log.info("");
    log.info("");

  }

  private void dumpSorted(Set<Entity> set) {
    final List<String> names = new ArrayList<String>();
    for (final Entity e : set) {
      names.add(e.getName());
    }
    Collections.sort(names);
    for (final String n : names) {
      log.info(n);
    }
  }

  // a special case whereby an identifier property is again a reference to
  // another entity, then this other entity is also derived readable, etc.
  private void addDerivedReadableIdentifierProperties(Entity entity) {
    for (final Property p : entity.getProperties()) {
      if (p.isIdentifier() && p.getTargetEntity() != null
          && !readableEntities.contains(p.getTargetEntity())
          && !derivedReadableEntities.contains(p.getTargetEntity())) {
        derivedReadableEntities.add(p.getTargetEntity());
        addDerivedReadableIdentifierProperties(p.getTargetEntity());
      }
    }
  }

  /**
   * @param entity
   *          the entity to check
   * @return true if the entity is derived readable for this user, otherwise false is returned.
   */
  public boolean isDerivedReadable(Entity entity) {
    // prevent infinite looping
    if (!isInitialized) {
      return false;
    }

    // false is the allow read reply
    if (obContext.isInAdministratorMode()) {
      return false;
    }
    return derivedReadableEntities.contains(entity);
  }

  /**
   * @param entity
   *          the entity to check
   * @return true if the entity is writable for this user, otherwise false is returned.
   */
  public boolean isWritable(Entity entity) {
    // prevent infinite looping
    if (!isInitialized) {
      return true;
    }

    if (obContext.isInAdministratorMode()) {
      return true;
    }
    return isWritableWithoutAdminMode(entity);
  }

  /**
   * Checks if an entity is writable for this user. If not then a OBSecurityException is thrown.
   * 
   * @param entity
   *          the entity to check
   * @throws OBSecurityException
   */
  public void checkWritable(Entity entity) {
    if (!isWritable(entity)) {
      throw new OBSecurityException("Entity " + entity + " is not writable by this user");
    }
  }

  /**
   * Checks if an entity is readable for this user. If not then a OBSecurityException is thrown.
   * 
   * @param entity
   *          the entity to check
   * @throws OBSecurityException
   */
  public void checkReadable(Entity entity) {
    // prevent infinite looping
    if (!isInitialized) {
      return;
    }

    if (obContext.isInAdministratorMode()) {
      return;
    }

    if (nonReadableEntities.contains(entity)) {
      throw new OBSecurityException("Entity " + entity + " is not readable by this user");
    }

    if (derivedReadableEntities.contains(entity)) {
      return;
    }

    if (!readableEntities.contains(entity)) {
      throw new OBSecurityException("Entity " + entity + " is not readable by the user "
          + obContext.getUser().getId());
    }
  }

  /**
   * Checks if an entity is readable for current user. It is not take into account admin mode.
   * 
   * @param entity
   *          the entity to check
   */
  public void checkReadableAccess(Entity entity) {
    if (!isReadableWithoutAdminMode(entity)) {
      throw new OBSecurityException("Entity " + entity + " is not accessible by this role/user: "
          + obContext.getRole().getName() + "/" + obContext.getUser().getName());
    }
  }

  /**
   * Checks if an entity is derived for current user. It is not take into account admin mode.
   * 
   * @param entity
   *          the entity to check
   */
  public void checkDerivedAccess(Entity entity) {
    if (!isDerivedWithoutAdminMode(entity)) {
      throw new OBSecurityException("Entity " + entity + " is not accessible by this role/user: "
          + obContext.getRole().getName() + "/" + obContext.getUser().getName());
    }
  }

  /**
   * Checks if an entity is writable for current user. It is not take into account admin mode.
   * 
   * @param entity
   *          the entity to check
   */
  public void checkWritableAccess(Entity entity) {
    if (!isWritableWithoutAdminMode(entity)) {
      throw new OBSecurityException("Entity " + entity + " is not writable by this role/user: "
          + obContext.getRole().getName() + "/" + obContext.getUser().getName());
    }
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  public OBContext getObContext() {
    return obContext;
  }

  public void setObContext(OBContext obContext) {
    this.obContext = obContext;
  }

  public Set<Entity> getReadableEntities() {
    return readableEntities;
  }

  public Set<Entity> getWritableEntities() {
    return writableEntities;
  }

  private boolean isReadableWithoutAdminMode(Entity entity) {
    // prevent infinite looping
    if (!isInitialized) {
      return false;
    }

    if (readableEntities.contains(entity)) {
      return true;
    }

    return false;
  }

  private boolean isDerivedWithoutAdminMode(Entity entity) {
    // prevent infinite looping
    if (!isInitialized) {
      return false;
    }

    if (readableEntities.contains(entity)) {
      return true;
    }

    if (derivedReadableEntities.contains(entity)) {
      return true;
    }

    if (derivedEntitiesFromProcess.contains(entity)) {
      return true;
    }

    return false;
  }

  private boolean isWritableWithoutAdminMode(Entity entity) {
    // prevent infinite looping
    if (!isInitialized) {
      return false;
    }
    return writableEntities.contains(entity);
  }

  private void addDerivedEtityFromProcess(Process process) {
    final ModelProvider mp = ModelProvider.getInstance();
    // any selector in a process definition is checked
    for (Parameter param : process.getOBUIAPPParameterList()) {
      Reference ref = param.getReferenceSearchKey();
      if (ref != null) {
        for (Selector sel : ref.getOBUISELSelectorList()) {
          // obtain entity from selector and added to derivedReadableEntities to take into
          // account as a derived entity.
          final String tableNameSelector = sel.getTable().getName();
          final Entity derivedEntity = mp.getEntity(tableNameSelector);
          if (!writableEntities.contains(derivedEntity)
              && !readableEntities.contains(derivedEntity)
              && !derivedReadableEntities.contains(derivedEntity)) {
            derivedEntitiesFromProcess.add(derivedEntity);
          }
        }
      }
    }
  }
}
