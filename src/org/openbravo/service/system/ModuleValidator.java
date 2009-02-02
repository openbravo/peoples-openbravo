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
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.system;

import java.io.File;

import org.hibernate.criterion.Expression;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;
import org.openbravo.model.ad.ui.Element;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Form;
import org.openbravo.model.ad.ui.Menu;
import org.openbravo.model.ad.ui.Message;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.TextInterface;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.ad.ui.Workflow;
import org.openbravo.service.system.SystemValidationResult.SystemValidationType;

/**
 * Validates modules, their dependencies and licenses
 * 
 * @author mtaal
 */
public class ModuleValidator implements SystemValidator {

  private Module validateModule;

  public String getCategory() {
    return "Module";
  }

  /**
   * Validates all modules and returns the {@link SystemValidationResult}.
   */
  public SystemValidationResult validate() {
    final SystemValidationResult result = new SystemValidationResult();
    if (getValidateModule() != null) {
      validate(getValidateModule(), result);
    } else {
      final OBCriteria<Module> modules = OBDal.getInstance().createCriteria(Module.class);
      for (Module module : modules.list()) {
        if (module.isInDevelopment()) {
          validate(module, result);
        }
      }
    }
    return result;
  }

  public void validate(Module module, SystemValidationResult result) {
    if (module.getId().equals("0")) {
      return;
    }
    final String sourcePath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path");
    final File modulesDir = new File(sourcePath, "modules");

    final File moduleDir = new File(modulesDir, module.getJavaPackage());
    if (!moduleDir.exists()) {
      result.addError(SystemValidationType.MODULE_ERROR,

      "Module directory (" + moduleDir.getAbsolutePath()
          + ") not found, has the module been installed?");
      return;
    }

    // check dependency on core
    checkDepencyOnCore(module, result);

    checkJavaPath(module, moduleDir, module.getJavaPackage(), result);

    checkJavaPackages(module, result);

    checkHasUIArtifact(module, result);

    if (module.getLicense() == null || module.getLicenseType() == null) {
      result.addError(SystemValidationType.MODULE_ERROR,

      "The license and/or the licenseType of the Module " + module.getName()
          + " are not set, before exporting these " + "fields should be set");
    }

    // industry template
    if (module.getType().equals("T")) {
      boolean found = false;
      for (ModuleDependency md : module.getModuleDependencyList()) {
        if (md.getModule().getId().equals("0") && md.isIncluded()) {
          found = true;
          break;
        }
      }
      if (!found) {
        result.addError(SystemValidationType.MODULE_ERROR, "Module " + module.getName()
            + " is an Industry Template must depend " + "on Core and the dependency relation "
            + "must have isIncluded set to true");
      }
    }
  }

  private void checkJavaPath(Module module, File moduleDir, String javaPackage,
      SystemValidationResult result) {

    File curDir = new File(moduleDir, "src");
    if (!curDir.exists()) {
      return;
    }
    final String[] paths = javaPackage.split("\\.");
    for (String part : paths) {
      final File partDir = new File(curDir, part);
      if (!partDir.exists()) {
        result.addError(SystemValidationType.MODULE_ERROR, "The source directory of the Module "
            + module.getName() + " is invalid, it should follow the "
            + "javaPackage of the module: " + javaPackage);
      }
      if (curDir.listFiles().length > 1) {
        result.addError(SystemValidationType.MODULE_ERROR, "The source directory of the Module "
            + module.getName() + " is invalid, it contains directories "
            + "which are not part of the javaPackage of the module: " + javaPackage);

      }
      curDir = partDir;
    }
  }

  private void checkHasUIArtifact(Module module, SystemValidationResult result) {
    if (module.isTranslationRequired()) {
      return;
    }
    final boolean reportError = hasArtifact(Window.class, module) || hasArtifact(Tab.class, module)
        || hasArtifact(Field.class, module) || hasArtifact(Element.class, module)
        || hasArtifact(TextInterface.class, module) || hasArtifact(Message.class, module)
        || hasArtifact(Form.class, module) || hasArtifact(Menu.class, module)
        || hasArtifact(Workflow.class, module);
    if (reportError) {
      result.addError(SystemValidationType.MODULE_ERROR, "Module " + module.getName()
          + " has UI Artifacts, " + "translation required should be set to 'Y', it is now 'N'.");
    }
  }

  private <T extends BaseOBObject> boolean hasArtifact(Class<T> clz, Module module) {
    final OBCriteria<T> obc = OBDal.getInstance().createCriteria(clz);
    obc.add(Expression.eq("module", module));
    return obc.count() > 0;
  }

  private void checkDepencyOnCore(Module module, SystemValidationResult result) {
    boolean coreModuleFound = false;
    for (ModuleDependency md : module.getModuleDependencyList()) {
      final Module dependentModule = findCoreModule(md.getDependentModule(), module.getId());
      if (dependentModule != null) {
        if (dependentModule.getId().equals(module.getId())) {
          result.addError(SystemValidationType.MODULE_ERROR,
              "Cycle in module dependencies with module " + module.getName());
          coreModuleFound = true; // prevents additional message
          break;
        }
        if (dependentModule.getId().equals("0")) {
          coreModuleFound = true;
          break;
        }
      }
    }
    if (!coreModuleFound) {
      result.addError(SystemValidationType.MODULE_ERROR, "Module " + module.getName()
          + " or any of its ancestors " + "does not depend on the Core module.");
    }

  }

  private void checkJavaPackages(Module module, SystemValidationResult result) {
    for (org.openbravo.model.ad.module.DataPackage pckg : module.getDataPackageList()) {
      if (!pckg.getJavaPackage().startsWith(module.getJavaPackage())) {
        result.addError(SystemValidationType.MODULE_ERROR, "Data package " + pckg.getName()
            + " has a java package which is not within the java package of its module "
            + module.getName());
      }
    }
  }

  // find the core module
  private Module findCoreModule(Module module, String originalModuleId) {
    if (module.getId().equals(originalModuleId)) {
      return module;
    }
    if (module.getId().equals("0")) {
      return module;
    }

    for (ModuleDependency md : module.getModuleDependencyList()) {
      final Module depModule = findCoreModule(md.getDependentModule(), originalModuleId);
      if (depModule != null) {
        return depModule;
      }
    }
    return null;
  }

  public Module getValidateModule() {
    return validateModule;
  }

  public void setValidateModule(Module validateModule) {
    this.validateModule = validateModule;
  }
}
