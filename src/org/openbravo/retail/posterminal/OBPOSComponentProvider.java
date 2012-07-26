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
    }
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {

    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    final String prefix = "web/" + POSUtils.MODULE_JAVA_PACKAGE + "/js/";

    final String[] resourceDependency = {
        // Common components
        "builder",
        "datasource",
        "data/dal",
        "utilities",
        "utilitiesui",
        "arithmetic",
        "i18n",
        "components/clock",
        "model/order",
        "model/terminal",
        "components/commonbuttons",
        "components/hwmanager",
        "components/table",
        "components/terminal",
        "components/modalprofile",
        "components/modallogout",
        "components/modalcancel",
        "main",
        // Web POS window
        "components/keypadbasic", "components/keyboard", "components/toolbarpayment",
        "components/toolbarscan", "components/keypadcoins", "components/keyboardorder",
        "windows/login", "windows/posbuttons", "data/datamaster", "data/dataordersave",
        "data/dataordertaxes", "data/dataorderdiscount", "components/renderorder",
        "components/listreceipts",
        "components/modalreceipts",
        "components/renderbusinesspartner",
        "components/searchbps",
        "components/modalbps",
        "components/scan",
        "components/tabscan",
        "components/rendercategory",
        "components/listcategories",
        "components/renderproduct",
        "components/listproducts",
        "components/tabbrowse",
        "components/searchproducts",
        "components/tabsearch",
        "components/editline",
        "components/tabeditline",
        "components/total",
        "components/modalpayment",
        "components/payment",
        "components/tabpayment",
        "components/renderorderline",
        "components/order",
        "components/orderdetails",
        "components/businesspartner",
        "components/listreceiptscounter",
        "windows/pointofsale",
        // Cash Management window
        "cashmgmt/data/datadropdepsave",
        "cashmgmt/components/cashmgmtbuttons",
        "cashmgmt/components/cashmgmtkeyboard",
        "cashmgmt/components/renderdropdepdestinations",
        "cashmgmt/components/searchdepositevents",
        "cashmgmt/components/searchdropevents",
        "cashmgmt/components/modaldepositevents",
        "cashmgmt/components/modaldropevents",
        "cashmgmt/components/toolbarcashmgmt",
        "cashmgmt/data/datamaster",
        "cashmgmt/windows/cashmgmtinfo",
        "cashmgmt/components/renderdepositsdrops",
        "cashmgmt/windows/listdepositsdrops",

        "cashmgmt/windows/cashmgmt",
        // Cash Up window
        "closecash/components/closebuttons", "closecash/components/closekeyboard",
        "closecash/components/listpaymentmethod", "closecash/components/renderpayments",
        "closecash/components/renderpaymentlines", "closecash/components/renderretailtransactions",
        "closecash/components/renderpendingreceipt", "closecash/components/listpendingreceipts",
        "closecash/components/searchretailtransactions", "closecash/components/toolbarcountcash",
        "closecash/components/modalfinishclose", "closecash/components/modalprocessreceipts",
        "closecash/data/datamaster", "closecash/model/daycash", "closecash/data/dataclosecash",
        "closecash/windows/closeinfo", "closecash/windows/tabcountcash",
        "closecash/windows/tabcashtokeep", "closecash/windows/tabpendingreceipts",
        "closecash/windows/tabpostprintclose", "closecash/windows/closecash",
        // Core resources
        "../../org.openbravo.client.application/js/utilities/ob-utilities-number",
        "../../org.openbravo.client.application/js/utilities/ob-utilities-date",
        // Payment providers
        "components/mockpayments" };

    for (String resource : resourceDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Static, prefix + resource
          + ".js", POSUtils.APP_NAME));
    }

    globalResources.add(createComponentResource(ComponentResourceType.Static, prefix
        + "components/errors.js", ComponentResource.APP_OB3));

    return globalResources;
  }
}
