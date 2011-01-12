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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;
import org.openbravo.model.ad.ui.Tab;

/**
 * Contains several utility methods used in the kernel.
 * 
 * @author mtaal
 */
public class KernelUtils {
  private static final Logger log = Logger.getLogger(KernelUtils.class);

  private static KernelUtils instance = new KernelUtils();

  public static synchronized KernelUtils getInstance() {
    if (instance == null) {
      instance = new KernelUtils();
    }
    return instance;
  }

  public static synchronized void setInstance(KernelUtils instance) {
    KernelUtils.instance = instance;
  }

  private List<Module> sortedModules = null;

  public Property getPropertyFromColumn(Column column) {
    final Entity entity = ModelProvider.getInstance().getEntity(column.getTable().getName());
    for (Property property : entity.getProperties()) {
      if (property.getColumnId().equals(column.getId())) {
        return property;
      }
    }
    throw new IllegalArgumentException("Column " + column
        + " does not have a corresponding property in the model");
  }

  /**
   * Creates a javascript string which reports an exception to the client.
   */
  public String createErrorJavaScript(Exception e) {
    log.error(e.getMessage(), e);

    final StringBuilder sb = new StringBuilder();
    if (e instanceof OBUserException) {
      OBUserException ex = (OBUserException) e;
      sb.append("OB.KernelUtilities.handleUserException('"
          + StringEscapeUtils.escapeJavaScript(ex.getMessage()) + "', " + ex.getJavaScriptParams()
          + ");");
    } else {
      sb.append("OB.KernelUtilities.handleSystemException('"
          + StringEscapeUtils.escapeJavaScript(e.getMessage()) + "');");
    }
    return sb.toString();
  }

  public JSONObject createErrorJSON(Exception e) {
    log.error(e.getMessage(), e);
    JSONObject error = new JSONObject();
    try {
      error.put("message", e.getMessage());
      if (e instanceof OBUserException) {
        error.put("type", "user");
        error.put("params", ((OBUserException) e).getJavaScriptParams());
      } else {
        error.put("type", "system");
      }
    } catch (JSONException e1) {
      log.error("Error creating json error", e1);
    }
    return error;
  }

  /**
   * Computes parameters to add to a link of a resource. The parameters include the version and
   * language of the user.
   * 
   * @param module
   *          Module to get the version from (if not in developers mode)
   * @return the version parameter string, a concatenation of the version and language with
   *         parameter names
   * @see KernelConstants#RESOURCE_VERSION_PARAMETER
   * @see KernelConstants#RESOURCE_LANGUAGE_PARAMETER
   */
  public String getVersionParameters(Module module) {

    return KernelConstants.RESOURCE_VERSION_PARAMETER + "=" + module.getVersion() + "&"
        + KernelConstants.RESOURCE_LANGUAGE_PARAMETER + "="
        + OBContext.getOBContext().getLanguage().getId();
  }

  /**
   * Get a module using its java package, the module is read from the internal cache.
   * 
   * @param javaPackage
   *          the java package used to read the module
   * @return a Module
   * @throws OBException
   *           if the module can not be found.
   */
  public Module getModule(String javaPackage) {
    for (Module module : getModulesOrderedByDependency()) {
      if (module.getJavaPackage().equals(javaPackage)) {
        return module;
      }
    }
    throw new OBException("No module found for java package " + javaPackage);
  }

  /**
   * Note the result of this method is cached. If module dependencies change then a system restart
   * is required to refresh this cache.
   * 
   * @return the modules in order of their dependencies, so core will be the first module etc.
   */
  public List<Module> getModulesOrderedByDependency() {
    if (sortedModules != null) {
      return sortedModules;
    }

    final List<ModuleWithLowLevelCode> moduleLowLevelCodes = new ArrayList<ModuleWithLowLevelCode>();
    OBContext.setAdminMode();
    try {
      final OBCriteria<Module> modules = OBDal.getInstance().createCriteria(Module.class);
      for (Module module : modules.list()) {
        final ModuleWithLowLevelCode moduleLowLevelCode = new ModuleWithLowLevelCode();
        moduleLowLevelCode.setModule(module);
        moduleLowLevelCode.setLowLevelCode(computeLowLevelCode(module, new ArrayList<Module>()));
        moduleLowLevelCodes.add(moduleLowLevelCode);
      }
      Collections.sort(moduleLowLevelCodes);
      final List<Module> result = new ArrayList<Module>();
      for (ModuleWithLowLevelCode moduleLowLevelCode : moduleLowLevelCodes) {
        result.add(moduleLowLevelCode.getModule());
      }
      sortedModules = result;
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private int computeLowLevelCode(Module module, List<Module> modules) {
    if (module.getId().equals("0")) {
      return 0;
    }
    // have been here, go away, with a high number
    // infinite loop
    if (modules.contains(module)) {
      log.warn("Cyclic relation in module dependencies of module " + module);
      return 100;
    }
    modules.add(module);
    int currentLevel = 0;
    for (ModuleDependency dependency : module.getModuleDependencyList()) {
      final int computedLevel = 1 + computeLowLevelCode(dependency.getDependentModule(), modules);
      if (computedLevel > currentLevel) {
        currentLevel = computedLevel;
      }
    }
    return currentLevel;
  }

  private static class ModuleWithLowLevelCode implements Comparable<ModuleWithLowLevelCode> {
    private Module module;
    private int lowLevelCode;

    @Override
    public int compareTo(ModuleWithLowLevelCode other) {
      return lowLevelCode - other.getLowLevelCode();
    }

    public Module getModule() {
      return module;
    }

    public void setModule(Module module) {
      this.module = module;
    }

    public int getLowLevelCode() {
      return lowLevelCode;
    }

    public void setLowLevelCode(int lowLevelCode) {
      this.lowLevelCode = lowLevelCode;
    }

  }

  /**
   * Returns the parent object for a specified combination of DAL object and tab. For example,
   * returns the corresponding Sales Order header for a Sales Order Line Returns null if there is no
   * parent object.
   * 
   * @param object
   *          The object whose parent should be returned
   * @param tab
   *          The tab the object belongs to
   * @return The BaseOBObject of the parent record
   */
  public BaseOBObject getParentRecord(BaseOBObject object, Tab tab) {
    List<Tab> tabsOfWindow = tab.getWindow().getADTabList();
    ArrayList<Entity> entities = new ArrayList<Entity>();
    for (Tab aTab : tabsOfWindow) {
      Entity entity = ModelProvider.getInstance().getEntityByTableName(
          aTab.getTable().getDBTableName());
      entities.add(entity);
    }
    Property fkProp = null;
    for (Property property : object.getEntity().getProperties()) {
      if (property.isParent()) {
        if (property.getTargetEntity() != null && entities.contains(property.getTargetEntity())) {
          fkProp = property;
        }
      }
    }
    if (fkProp != null) {
      return (BaseOBObject) object.get(fkProp.getName());
    } else {
      return null;
    }
  }

  /**
   * Returns the parent tab of a BaseOBObject which belongs to a given tab
   * 
   * @param tab
   *          The tab the object belongs to
   * @return The parent tab of the given tab
   */
  public Tab getParentTab(Tab tab) {
    List<Tab> tabsOfWindow = tab.getWindow().getADTabList();
    ArrayList<Entity> entities = new ArrayList<Entity>();
    HashMap<Entity, Tab> tabOfEntity = new HashMap<Entity, Tab>();
    Entity theEntity = ModelProvider.getInstance().getEntityByTableName(
        tab.getTable().getDBTableName());
    for (Tab aTab : tabsOfWindow) {
      Entity entity = ModelProvider.getInstance().getEntityByTableName(
          aTab.getTable().getDBTableName());
      entities.add(entity);
      tabOfEntity.put(entity, aTab);
    }
    Tab targetTab = null;
    for (Property property : theEntity.getProperties()) {
      if (property.isParent()) {
        if (property.getTargetEntity() != null && entities.contains(property.getTargetEntity())) {
          targetTab = tabOfEntity.get(property.getTargetEntity());
        }
      }
    }
    return targetTab;
  }
}
