/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;

public interface CustomerAddrCreationHook {

  public void exec(JSONObject jsonCustomerAddr, BusinessPartner customer, Location location)
      throws Exception;

}

// Example of a hook:
// import javax.enterprise.context.ApplicationScoped;
//
// import org.codehaus.jettison.json.JSONObject;
// import org.openbravo.model.common.businesspartner.BusinessPartner;
// import org.openbravo.model.common.businesspartner.Location;
// import org.openbravo.retail.posterminal.CustomerLoaderHook;
//
// @ApplicationScoped
// public class CustomerAddrCreationHookTest implements CustomerAddrCreationHook {
//
// @Override
// public void exec(JSONObject jsonCustomerAddr, BusinessPartner customer, Location location)
// throws Exception {
// // TODO Auto-generated method stub
// System.out.println("somebody is calling me");
// }
//
// }