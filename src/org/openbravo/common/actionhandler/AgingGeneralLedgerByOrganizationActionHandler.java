/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */ 

package org.openbravo.common.actionhandler;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBLedgerUtils;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;

public class AgingGeneralLedgerByOrganizationActionHandler extends BaseActionHandler {

  private static final Logger log = Logger
      .getLogger(AgingGeneralLedgerByOrganizationActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject resultMessage = new JSONObject();
    JSONObject errorMessage = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      JSONObject jsonContent = new JSONObject(content);
      JSONObject data = new JSONObject(content);
      String generalLedgerId = OBLedgerUtils.getOrgLedger(jsonContent.getString("organization"));
      data.put("value", generalLedgerId);

      AcctSchema generalLedger = OBDal.getInstance().get(AcctSchema.class, generalLedgerId);

      data.put("identifier", generalLedger.getName());
      resultMessage.put("response", data);
    } catch (JSONException e) {
      
      log.error("Error creating JSON Object ", e);
      e.printStackTrace();
      try {
        errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("title", "Error");
        errorMessage.put("text", e.getMessage());
        resultMessage.put("message", errorMessage);
      } catch (JSONException e1) {
        log.error("Error creating JSON Object ", e);
      }
      
    } finally {
      OBContext.restorePreviousMode();
    }
    
    return resultMessage;
  }

}
