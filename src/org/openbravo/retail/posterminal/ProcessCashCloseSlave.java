/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

public class ProcessCashCloseSlave extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(true);
    try {
      OBPOSAppCashup appCashup = OBDal.getInstance().get(OBPOSAppCashup.class,
          jsonsent.getString("cashUpId"));

      UpdateCashup.associateMasterSlave(appCashup, appCashup.getPOSTerminal());
      OBDal.getInstance().flush();

      OBDal.getInstance().getSession().evict(appCashup);
      appCashup = OBDal.getInstance().get(OBPOSAppCashup.class, jsonsent.getString("cashUpId"));

      JSONObject result = new JSONObject();
      JSONObject data = new JSONObject();
      boolean hasMaster = appCashup != null && appCashup.getObposParentCashup() != null;
      data.put("hasMaster", hasMaster);
      result.put("data", data);
      result.put("status", 0);
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected String getProperty() {
    return "OBPOS_retail.cashup";
  }

}
