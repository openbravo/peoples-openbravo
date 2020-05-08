/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource.ComponentResourceType;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;

/**
 * Component provider for the Taxes Engine.
 */
@ApplicationScoped
@ComponentProvider.Qualifier(TaxesEngineAppComponentProvider.QUALIFIER)
public class TaxesEngineAppComponentProvider extends BaseComponentProvider {
  public static final String QUALIFIER = "OBPOS_TaxesComponent";
  public static final String TAXES_APP = "TaxesEngine";

  private static final String JS_BASE = "web/org.openbravo.retail.posterminal/app/model/businessLogic/taxesengine/";

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {

    List<String> commonDependencies = Arrays.asList( //
        JS_BASE + "interface/tax-posinterface", //
        JS_BASE + "engine/tax-engine", //
        JS_BASE + "engine/tax-rules", //
        JS_BASE + "engine/rules/priceincluding-tax", //
        JS_BASE + "engine/rules/priceexcluding-tax");

    List<ComponentResource> globalResources = new ArrayList<>();
    globalResources.addAll(getComponents(commonDependencies, TAXES_APP));

    return globalResources;
  }

  private List<ComponentResource> getComponents(List<String> resources, String appName) {
    return resources.stream()
        .map(r -> createComponentResource(ComponentResourceType.Static, r + ".js", appName))
        .collect(Collectors.toList());
  }
}
