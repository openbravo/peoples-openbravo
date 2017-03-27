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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class generates the session dynamic resources. This kind of resources are loaded before the
 * javascript content used in the application and they are provided by those classes extending the
 * {@link SessionDynamicTemplateComponent} class.
 */
public class SessionDynamicResourceComponent extends BaseComponent {

  private static final Logger log = LoggerFactory.getLogger(SessionDynamicResourceComponent.class);

  @Inject
  @Any
  private Instance<SessionDynamicTemplateComponent> components;

  @Override
  public String generate() {
    StringBuilder result = new StringBuilder();
    for (SessionDynamicTemplateComponent component : getSortedComponentList()) {
      log.debug("Generating session dynamic resource {}", component.getId());
      component.setParameters(getParameters());
      long t = System.currentTimeMillis();
      result.append(component.generate());
      log.debug("Resource generation took {} ms", System.currentTimeMillis() - t);
    }
    return result.toString();
  }

  @Override
  public Object getData() {
    return this;
  }

  @Override
  public String getETag() {
    // session dynamic resources are never cached
    return OBContext.getOBContext().getUser().getId() + "_"
        + OBContext.getOBContext().getLanguage().getId() + "_" + System.currentTimeMillis();
  }

  public List<SessionDynamicTemplateComponent> getSortedComponentList() {
    List<SessionDynamicTemplateComponent> componentList = new ArrayList<>();
    final List<Module> modules = KernelUtils.getInstance().getModulesOrderedByDependency();
    for (Module module : modules) {
      for (SessionDynamicTemplateComponent component : components) {
        if (!component.getModule().getId().equals(module.getId())) {
          continue;
        }
        componentList.add(component);
      }
    }
    return componentList;
  }
}
