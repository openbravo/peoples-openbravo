/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.businesspartner.BusinessPartner;

public interface CustomerLoaderHook {

  public void exec(JSONObject jsonCustomer, BusinessPartner customer) throws Exception;
}

// Example of a hook:
// import javax.enterprise.context.ApplicationScoped;
//
// import org.codehaus.jettison.json.JSONObject;
// import org.openbravo.model.common.businesspartner.BusinessPartner;
// import org.openbravo.retail.posterminal.CustomerLoaderHook;
//
// @ApplicationScoped
// public class CustomerLoaderHookTest implements CustomerLoaderHook {
//
// @Override
// public void exec(JSONObject jsonCustomer, BusinessPartner customer)
// throws Exception {
// // TODO Auto-generated method stub
// System.out.println("somebody is calling me");
// }
//
// }