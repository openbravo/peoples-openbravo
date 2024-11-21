/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;

@ApplicationScoped
public class RetailConfigurationProvider extends BaseComponentProvider {

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    return null;
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<>();
    globalResources.add(
        createStaticResource("web/org.openbravo.retail.config/js/ob-clone-productlist.js", false));
    globalResources.add(createStaticResource(
        "web/org.openbravo.retail.config/js/specialScheduleValidation.js", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/Default"
            + "/org.openbravo.retail.config/ob-clone-styles.css",
        false));
    return globalResources;
  }

}
