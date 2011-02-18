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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;

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

  private String moduleVersion = null;
  private List<Module> sortedModules = null;

  /**
   * Creates a javascript string which reports an exception to the client.
   */
  public String createErrorJavaScript(Exception e) {

    log.error(e.getMessage(), e);

    final StringBuilder sb = new StringBuilder();
    sb.append("isc.warn('Error occured: " + e.getMessage() + "');");
    return sb.toString();
  }

  /**
   * Computes parameters to add to a link of a resource. The parameters include the version and
   * language of the user.
   * 
   * The version computation logic depends on if the module is in development (
   * {@link Module#isInDevelopment()}. If in developers mode then the
   * {@link System#currentTimeMillis()} is used. If not in developers mode then the
   * {@link Module#getVersion()} is used. These values are prepended with the language id of the
   * user. This makes it possible to generate language specific components on the server.
   * 
   * @param module
   *          Module to get the version from (if not in developers mode)
   * @return the version parameter string, a concatenation of the version and language with
   *         parameter names
   * @see KernelConstants#RESOURCE_VERSION_PARAMETER
   * @see KernelConstants#RESOURCE_LANGUAGE_PARAMETER
   */
  public String getVersionParameters(Module module) {
    final String useAsVersion;

    if (module.isInDevelopment()) {
      useAsVersion = "" + System.currentTimeMillis();
    } else {
      useAsVersion = module.getVersion();
    }
    return KernelConstants.RESOURCE_VERSION_PARAMETER + "=" + useAsVersion + "&"
        + KernelConstants.RESOURCE_LANGUAGE_PARAMETER + "="
        + OBContext.getOBContext().getLanguage().getId();
  }

  /**
   * @return true if there is at least one module in development
   * @see Module#isInDevelopment()
   */
  public boolean inDevelopersMode() {
    if (moduleVersion != null) {
      return false;
    }
    OBContext.setAdminMode();
    try {
      final OBCriteria<Module> modules = OBDal.getInstance().createCriteria(Module.class);
      modules.add(Expression.eq(Module.PROPERTY_INDEVELOPMENT, true));
      return modules.count() > 0;
    } finally {
      OBContext.restorePreviousMode();
    }
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
        moduleLowLevelCode.setLowLevelCode(computeLowLevelCode(module));
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

  private int computeLowLevelCode(Module module) {
    if (module.getId().equals("0")) {
      return 0;
    }
    int currentLevel = 0;
    for (ModuleDependency dependency : module.getModuleDependencyList()) {
      final int computedLevel = 1 + computeLowLevelCode(dependency.getDependentModule());
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
}
