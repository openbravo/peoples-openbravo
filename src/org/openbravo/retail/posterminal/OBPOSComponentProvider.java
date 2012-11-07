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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource.ComponentResourceType;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;

/**
 * @author iperdomo
 * 
 */
@ApplicationScoped
@ComponentProvider.Qualifier(OBPOSComponentProvider.QUALIFIER)
public class OBPOSComponentProvider extends BaseComponentProvider {

  public static final String QUALIFIER = "OBPOS_Main";
  public static final String APP_CACHE_COMPONENT = "AppCacheManifest";
  public static final String CLIENT_MODEL_COMPONENT = "ClientModel";
  public static final String LABELS_COMPONENT = "Labels";

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    if (componentId.equals(APP_CACHE_COMPONENT)) {
      final ApplicationCacheComponent component = getComponent(ApplicationCacheComponent.class);
      component.setId(APP_CACHE_COMPONENT);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(CLIENT_MODEL_COMPONENT)) {
      final ClientModelComponent component = getComponent(ClientModelComponent.class);
      component.setId(CLIENT_MODEL_COMPONENT);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(LABELS_COMPONENT)) {
      final LabelsComponent component = getComponent(LabelsComponent.class);
      component.setId(LABELS_COMPONENT);
      component.setParameters(parameters);
      return component;
    } else if (componentId.equals(KernelConstants.RESOURCE_COMPONENT_ID)) {
      final OBPOSStaticResorcesComponent component = getComponent(OBPOSStaticResorcesComponent.class);
      component.setId(KernelConstants.RESOURCE_COMPONENT_ID);
      component.setParameters(parameters);
      return component;
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {

    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    final String prefix = "web/" + POSUtils.MODULE_JAVA_PACKAGE + "/js/";

    final String[] resourceDependency = {
        // Common components
        "datasource",
        "data/dal",
        "utilities",
        "utilitiesui",
        "arithmetic",
        "i18n",
        "data/windowmodel",
        "components/clock",
        "model/order",
        "model/terminal",
        "model/user",
        "model/changedbusinesspartners",
        "model/executor",
        "components/commonbuttons",
        "components/table",
        "components/scrollabletable",
        "components/terminal",
        "components/modalprofile",
        "components/modallogout",
        "components/modalonline",
        "components/modalcancel",
        "components/subwindow",
        "components/leftsubwindow",
        "components/windowview",
        "components/modalreceiptproperties",
        "components/modalreceiptlineproperties",
        "components/modalnoteditableorder",
        "main",
        // Web POS window
        "components/keypadbasic",
        "components/keyboard",
        "components/keypadcoins",
        "data/dataordersave",
        "data/dataordertaxes",
        "data/datacustomersave",
        "data/dataorderdiscount",
        "components/modalreceipts",
        "components/rendercategory",
        "components/renderproduct",
        "components/searchproducts",
        "components/total",
        "components/modalpayment",
        "components/renderorderline",
        "components/order",
        "components/orderdetails",
        "components/businesspartner",
        "components/listreceiptscounter",
        "components/product-browser",
        "components/menu",
        // Old Tickets
        "components/modalpaidreceipts",
        // Quotations
        "components/modalcreateorderfromquotation",
        "components/modalreactivatequotation",

        // Point of sale
        "pointofsale/model/pointofsale-print",
        "pointofsale/model/pointofsale-model",
        "pointofsale/model/localstock",
        "pointofsale/model/otherstoresstock",
        "pointofsale/view/pointofsale",
        "pointofsale/view/ps-receiptview",
        "pointofsale/view/ps-productdetailsview",
        "pointofsale/view/toolbar-left",
        "pointofsale/view/toolbar-right",
        "pointofsale/view/scan",
        "pointofsale/view/editline",
        "pointofsale/view/payment",
        "pointofsale/view/keyboard-toolbars",
        "pointofsale/view/keyboardorder",
        // Point of sale subwindows
        "pointofsale/view/subwindows/customers/components/sharedcomponents",
        "pointofsale/view/subwindows/customers/customersadvancedsearch",
        "pointofsale/view/subwindows/customers/editcreatecustomerform",
        "pointofsale/view/subwindows/customers/customerdetailview",
        // Point of sale modals
        "pointofsale/view/modals/modalstockinstore",
        "pointofsale/view/modals/modalstockinotherstore",
        "pointofsale/view/modals/modalconfirmclosepaidticket",
        "pointofsale/view/modals/modalproductcannotbegroup",
        "pointofsale/view/modals/modalwarehousesrequired",

        // Cash Management window
        "cashmgmt/model/cashmgmt-print", "cashmgmt/model/cashmgmt-model",

        "cashmgmt/view/cashmgmtkeyboard",
        "cashmgmt/view/listevents",
        "cashmgmt/view/cashmgmtinfo",
        "cashmgmt/view/listdepositsdrops",
        "cashmgmt/view/cashmgmt",

        "cashmgmt/components/cashmgmt-modals",

        // Cash Up window
        "closecash/model/cashup-print", "closecash/model/cashup-model", "closecash/view/closecash",
        "closecash/view/closekeyboard", "closecash/view/closeinfo",
        "closecash/view/tabpendingreceipts", "closecash/view/tabcountcash",
        "closecash/view/tabcashtokeep", "closecash/view/tabpostprintclose",
        "closecash/components/cashup-modals",

        "closecash/model/daycash",
        // Core resources
        "../../org.openbravo.client.application/js/utilities/ob-utilities-number",
        "../../org.openbravo.client.application/js/utilities/ob-utilities-date",

        // Payment providers
        "components/mockpayments",

        // Discounts
        "model/discounts" };

    for (String resource : resourceDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Static, prefix + resource
          + ".js", POSUtils.APP_NAME));
    }

    globalResources.add(createComponentResource(ComponentResourceType.Static, prefix
        + "components/errors.js", ComponentResource.APP_OB3));

    return globalResources;
  }
}
