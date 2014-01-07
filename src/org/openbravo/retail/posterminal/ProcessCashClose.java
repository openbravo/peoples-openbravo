/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.sql.SQLException;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
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

  public JSONObject saveCashUp(JSONObject jsonCashup) throws Exception {

    JSONObject jsonData = new JSONObject();
    String cashUpId = jsonCashup.getString("cashUpId");
    OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
        jsonCashup.getString("terminalId"));
    OBContext.setOBContext(jsonCashup.getString("userId"), OBContext.getOBContext().getRole()
        .getId(), OBContext.getOBContext().getCurrentClient().getId(), posTerminal
        .getOrganization().getId());
    OBPOSAppCashup cashUp = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);

    // check if there is a reconciliation in draft status
    for (OBPOSAppPayment payment : posTerminal.getOBPOSAppPaymentList()) {
      final OBCriteria<FIN_Reconciliation> recconciliations = OBDal.getInstance().createCriteria(
          FIN_Reconciliation.class);
      recconciliations.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_DOCUMENTSTATUS, "DR"));
      recconciliations.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT,
          payment.getFinancialAccount()));
      for (final FIN_Reconciliation r : recconciliations.list()) {
        throw new OBException("Error processing cash close: the reconciliation "
            + r.getDocumentNo() + " (" + r.getAccount().getName() + ") is in draft status");

      }
    }

    OBCriteria<OBPOSErrors> errorsQuery = OBDal.getInstance().createCriteria(OBPOSErrors.class);
    errorsQuery.add(Restrictions.ne(OBPOSErrors.PROPERTY_TYPEOFDATA, "CU"));
    errorsQuery.add(Restrictions.eq(OBPOSErrors.PROPERTY_ORDERSTATUS, "N"));
    if (errorsQuery.count() > 0) {
      throw new OBException(
          "There are errors related to non-created customers, orders, or cash management movements pending to be processed. Process them before processing the cash ups");
    }

    if (cashUp == null
        && RequestContext.get().getSessionAttribute(
            "cashupTerminalId|" + jsonCashup.getString("terminalId")) == null) {
      RequestContext.get().setSessionAttribute(
          "cashupTerminalId|" + jsonCashup.getString("terminalId"), true);
      new OrderGroupingProcessor().groupOrders(posTerminal, cashUpId);
      posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          jsonCashup.getString("terminalId"));

      CashCloseProcessor processor = WeldUtils
          .getInstanceFromStaticBeanManager(CashCloseProcessor.class);
      JSONArray cashMgmtIds = jsonCashup.getJSONArray("cashMgmtIds");
      JSONObject result = processor.processCashClose(posTerminal, jsonCashup, cashMgmtIds);

      // add the messages returned by processCashClose...
      jsonData.put("messages", result.opt("messages"));
      jsonData.put("next", result.opt("next"));
    }
    return jsonData;
  }

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(false);
    JSONArray jsonCashups = jsonsent.getJSONArray("cashups");
    JSONObject jsonResponse = new JSONObject();
    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");
    for (int i = 0; i < jsonCashups.length(); i++) {
      JSONObject jsonCashup = jsonCashups.getJSONObject(i);
      try {
        JSONObject jsonData = saveCashUp(jsonCashup);

        jsonResponse.put(JsonConstants.RESPONSE_DATA, jsonData);
        if (i % 1 == 0) {
          OBDal.getInstance().getConnection(false).commit();
          OBDal.getInstance().getSession().clear();
        }
        if (TriggerHandler.getInstance().isDisabled()) {
          TriggerHandler.getInstance().enable();
        }
      } catch (Exception e) {
        OBDal.getInstance().rollbackAndClose();
        if (TriggerHandler.getInstance().isDisabled()) {
          TriggerHandler.getInstance().enable();
        }
        OBPOSErrors errorEntry = OBProvider.getInstance().get(OBPOSErrors.class);
        errorEntry.setError(OrderLoader.getErrorMessage(e));
        errorEntry.setOrderstatus("N");
        errorEntry.setJsoninfo(jsonCashup.toString());
        errorEntry.setTypeofdata("CU");
        errorEntry.setObposApplications(OBDal.getInstance().get(OBPOSApplications.class,
            jsonCashup.getString("terminalId")));
        OBDal.getInstance().save(errorEntry);
        OBDal.getInstance().flush();
        log.error("Error processing cash close", e);
        try {
          OBDal.getInstance().getConnection().commit();
        } catch (SQLException e1) {
          // this won't happen
        }
      } finally {
        RequestContext.get().removeSessionAttribute(
            "cashupTerminalId|" + jsonCashup.getString("terminalId"));
      }
    }

    return jsonResponse;
  }

  @Override
  protected String getProperty() {
    return "OBPOS_retail.cashup";
  }
}
