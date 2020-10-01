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
 * All portions are Copyright (C) 2020 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;

/**
 * Allows to retrieve information about the dependencies for a given module.
 */
public class ModuleDependencyResolver {

  private static final ModuleDependencyResolver instance = new ModuleDependencyResolver();

  private ModuleDependencyResolver() {
  }

  /**
   * @return the ModuleDependencyResolver singleton instance
   */
  public static ModuleDependencyResolver getInstance() {
    return instance;
  }

  /**
   * Retrieve all the modules which has a dependency relation with the given module. This includes
   * the modules that depend on the provided module and the modules that depends on those
   * (descendants) and all the modules the provided module depends on and the all the dependencies
   * of those (ancestors).
   * 
   * @param modulePackage
   *          the package of the module
   * 
   * @return the full list of dependencies for the given module
   */
  public List<Module> getDependenciesForModulePackage(String modulePackage) {
    Module module = KernelUtils.getInstance().getModule(modulePackage);
    return getDependenciesForModule(module);
  }

  /**
   * Retrieve all the modules which has a dependency relation with the given module. This includes
   * the modules that depend on the provided module and the modules that depends on those
   * (descendants) and all the modules the provided module depends on and the all the dependencies
   * of those (ancestors).
   * 
   * @param moduleId
   *          the module ID
   * 
   * @return the full list of dependencies for the given module
   */
  public List<Module> getDependenciesForModuleId(String moduleId) {
    Module module = OBDal.getInstance().get(Module.class, moduleId);
    return getDependenciesForModule(module);
  }

  private List<Module> getDependenciesForModule(Module module) {
    Set<String> moduleDependencyTree = getDescendantsDependencyTree(module);
    moduleDependencyTree.addAll(getAncestorsDependencyTree(module));

    return KernelUtils.getInstance()
        .getModulesOrderedByDependency()
        .stream()
        .filter(m -> moduleDependencyTree.contains(m.getId()))
        .collect(Collectors.toList());
  }

  private Set<String> getDescendantsDependencyTree(Module module) {
    Set<String> tree = new HashSet<>();
    tree.add(module.getId());
    for (ModuleDependency dep : module.getModuleDependencyList()) {
      Module depMod = dep.getDependentModule();
      if (!tree.contains(depMod.getId())) {
        tree.add(depMod.getId());
        tree.addAll(getDescendantsDependencyTree(depMod));
      }
    }
    return tree;
  }

  private Set<String> getAncestorsDependencyTree(Module module) {
    Set<String> tree = new HashSet<>();

    for (Module m : KernelUtils.getInstance().getModulesOrderedByDependency()) {
      boolean isParent = m.getModuleDependencyList()
          .stream()
          .map(ModuleDependency::getDependentModule)
          .anyMatch(dep -> dep.getId().equals(module.getId()) && !tree.contains(dep.getId()));
      if (isParent) {
        tree.addAll(getAncestorsDependencyTree(m));
      }
    }

    tree.add(module.getId());
    return tree;
  }
}
