/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;

/**
 * Classes implementing this interface can execute logic after Business Partner Location has been
 * created/modified in CustomerAddrLoader class
 *
 */
public interface CustomerAddrAfterCreationHook {

  public abstract void exec(JSONObject jsonCustomerAddr, BusinessPartner customer,
      Location location) throws Exception;

}
