/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;

public class ErrorsComponentProvider extends BaseComponentProvider {
  final static String COMPONENT_TYPE = "OBPOS_ClearError";

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources.add(createStaticResource(
        "web/org.openbravo.retail.posterminal/js/components/errors.js", false));
    return globalResources;
  }

}
