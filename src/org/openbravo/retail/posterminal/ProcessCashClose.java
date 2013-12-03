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
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.service.json.JsonConstants;

public class ProcessCashClose extends JSONProcessSimple {

  private static final Logger log = Logger.getLogger(ProcessCashClose.class);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(false);
    JSONArray jsonCashups = jsonsent.getJSONArray("cashups");
    JSONObject jsonResponse = new JSONObject();
    for (int i = 0; i < jsonCashups.length(); i++) {
      JSONObject jsonCashup = jsonCashups.getJSONObject(i);
      JSONObject jsonData = new JSONObject();
      String cashUpId = jsonCashup.getString("cashUpId");
      try {
        jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
        jsonResponse.put("result", "0");
        OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
            jsonCashup.getString("terminalId"));
        OBPOSAppCashup cashUp = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);

        // check if there is a reconciliation in draft status
        for (OBPOSAppPayment payment : posTerminal.getOBPOSAppPaymentList()) {
          final OBCriteria<FIN_Reconciliation> recconciliations = OBDal.getInstance()
              .createCriteria(FIN_Reconciliation.class);
          recconciliations.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_DOCUMENTSTATUS, "DR"));
          recconciliations.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT,
              payment.getFinancialAccount()));
          for (final FIN_Reconciliation r : recconciliations.list()) {
            log.error("Error processing cash close: the reconciliation " + r.getDocumentNo() + " ("
                + r.getAccount().getName() + ") is in draft status");
            jsonData.put("error", "1");
            jsonData.put("errorMessage", "OBPOS_LblCashupWithReconciliationDraft");
            jsonData.put("errorDetail", payment.getCommercialName());
            jsonData.put("errorNoNavigateToInitialScreen", "true");
            jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
            break;
          }
        }

        if (cashUp == null
            && RequestContext.get().getSessionAttribute(
                "cashupTerminalId|" + jsonCashup.getString("terminalId")) == null) {
          RequestContext.get().setSessionAttribute(
              "cashupTerminalId|" + jsonCashup.getString("terminalId"), true);
          new OrderGroupingProcessor().groupOrders(posTerminal, cashUpId);
          posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
              jsonCashup.getString("terminalId"));
          JSONArray arrayCashCloseInfo = jsonCashup.getJSONArray("cashCloseInfo");

          CashCloseProcessor processor = WeldUtils
              .getInstanceFromStaticBeanManager(CashCloseProcessor.class);
          JSONObject result = processor.processCashClose(posTerminal,
              jsonCashup.getString("cashUpId"), arrayCashCloseInfo);

          // add the messages returned by processCashClose...
          jsonData.put("messages", result.opt("messages"));
          jsonData.put("next", result.opt("next"));
        }
        jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
      } catch (Exception e) {
        OBDal.getInstance().rollbackAndClose();
        log.error("Error processing cash close", e);
        jsonData.put("error", "1");
        jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
        break;
      } finally {
        RequestContext.get().removeSessionAttribute(
            "cashupTerminalId|" + jsonCashup.getString("terminalId"));
        OBDal.getInstance().rollbackAndClose();
        OBContext.restorePreviousMode();
        if (TriggerHandler.getInstance().isDisabled()) {
          TriggerHandler.getInstance().enable();
        }
      }
    }

    return jsonResponse;
  }

  @Override
  protected String getProperty() {
    return "OBPOS_retail.cashup";
  }
}
