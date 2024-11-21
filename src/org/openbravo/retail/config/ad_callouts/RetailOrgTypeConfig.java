/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class RetailOrgTypeConfig extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String retailOrgType = info.getStringParameter("inpemObretcoRetailorgtype", null);
    if ("".equals(retailOrgType)) {
      info.addResult("inpemObretcoProductlistId", "");
      info.addResult("inpemObretcoCBpartnerId", "");
      info.addResult("inpemObretcoCBpLocationId", "");
      info.addResult("inpemObretcoMWarehouseId", "");
      info.addResult("inpemObretcoPricelistId", "");
    }
  }

}
