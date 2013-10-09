/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;

public class ProcessCashClose extends JSONProcessSimple {

  private static final Logger log = Logger.getLogger(ProcessCashClose.class);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(false);
    JSONObject jsonResponse = new JSONObject();
    JSONObject jsonData = new JSONObject();
    try {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put("result", "0");
      OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          jsonsent.getString("terminalId"));
      OBPOSAppCashup cashUp = OBDal.getInstance().get(OBPOSAppCashup.class,
          jsonsent.getString("cashUpId"));
      
      // check if there is a reconciliation in draft status
      for (OBPOSAppPayment payment: posTerminal.getOBPOSAppPaymentList()){    	  
    	  final OBCriteria<FIN_Reconciliation> recconciliations = OBDal.getInstance().createCriteria(
    			  FIN_Reconciliation.class);
    	  recconciliations.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_DOCUMENTSTATUS, "DR"));
    	  recconciliations.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT , payment.getFinancialAccount()));
    	  for (final FIN_Reconciliation r : recconciliations.list()) {
    	      log.error("Error processing cash close: the reconciliation "+ r.getDocumentNo() +" ("+ r.getAccount().getName()+") is in draft status");
    	      jsonData.put("error", "1");
    	      jsonData.put("errorMessage", "OBPOS_LblCashupWithReconciliationDraft");
    	      jsonData.put("errorDetail",  payment.getCommercialName());
    	      jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
    	      return jsonResponse;
    	  }
      }   
      
      if (cashUp == null
          && RequestContext.get().getSessionAttribute(
              "cashupTerminalId|" + jsonsent.getString("terminalId")) == null) {
        RequestContext.get().setSessionAttribute(
            "cashupTerminalId|" + jsonsent.getString("terminalId"), true);
        new OrderGroupingProcessor().groupOrders(posTerminal);
        posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
            jsonsent.getString("terminalId"));
        JSONArray arrayCashCloseInfo = jsonsent.getJSONArray("cashCloseInfo");
        new CashCloseProcessor().processCashClose(posTerminal, jsonsent.getString("cashUpId"),
            arrayCashCloseInfo);
      }
      jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
      return jsonResponse;
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error("Error processing cash close", e);
      jsonData.put("error", "1");
      jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
      return jsonResponse;
    } finally {
      RequestContext.get().removeSessionAttribute(
          "cashupTerminalId|" + jsonsent.getString("terminalId"));
      OBDal.getInstance().rollbackAndClose();
      OBContext.restorePreviousMode();
      if (TriggerHandler.getInstance().isDisabled()) {
        TriggerHandler.getInstance().enable();
      }
    }
  }

  @Override
  protected String getProperty() {
    return "OBPOS_retail.cashup";
  }
}
