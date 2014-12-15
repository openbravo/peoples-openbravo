/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Date;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.mobile.core.process.PropertyByType;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonToDataConverter;

@DataSynchronization(entity = "OBPOS_App_Cashup")
public class ProcessCashClose extends POSDataSynchronizationProcess {

  private static final Logger log = Logger.getLogger(ProcessCashClose.class);
  JSONObject jsonResponse = new JSONObject();

  public JSONObject saveRecord(JSONObject jsonCashup) throws Exception {
    JSONObject jsonData = new JSONObject();
    String cashUpId = jsonCashup.getString("id");
    Date cashUpDate = new Date();

    try {
      if (jsonCashup.has("cashUpDate")) {
        String strCashUpDate = (String) jsonCashup.getString("cashUpDate");
        cashUpDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(PropertyByType.DATETIME,
            ((String) strCashUpDate).subSequence(0, ((String) strCashUpDate).lastIndexOf(".")));
      } else {
        log.error("Error processing cash close: error retrieving cashUp date. Using current date");
      }
    } catch (Exception e) {
      log.error("Error processing cash close: error retrieving cashUp date. Using current date");
    }

    OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
        jsonCashup.getString("posTerminal"));
    OBContext.setOBContext(jsonCashup.getString("userId"), OBContext.getOBContext().getRole()
        .getId(), OBContext.getOBContext().getCurrentClient().getId(), posTerminal
        .getOrganization().getId());
    OBPOSAppCashup cashUp = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);

    reassignTicketsFromLostCashups(cashUpId, posTerminal);

    // check if there is a reconciliation in draft status
    for (OBPOSAppPayment payment : posTerminal.getOBPOSAppPaymentList()) {
      final OBCriteria<FIN_Reconciliation> recconciliations = OBDal.getInstance().createCriteria(
          FIN_Reconciliation.class);
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
        return jsonResponse;
      }
    }

    OBCriteria<OBPOSErrors> errorsQuery = OBDal.getInstance().createCriteria(OBPOSErrors.class);
    errorsQuery.add(Restrictions.ne(OBPOSErrors.PROPERTY_TYPEOFDATA, "OBPOS_App_Cashup"));
    errorsQuery.add(Restrictions.eq(OBPOSErrors.PROPERTY_ORDERSTATUS, "N"));
    errorsQuery.add(Restrictions.eq(OBPOSErrors.PROPERTY_OBPOSAPPLICATIONS, posTerminal));
    if (errorsQuery.count() > 0) {
      throw new OBException(
          "There are errors related to non-created customers, orders, or cash management movements pending to be processed. Process them before processing the cash ups");
    }
    if (cashUp == null) {
      TriggerHandler.getInstance().disable();
      try {
        TriggerHandler.getInstance().disable();

        new OrderGroupingProcessor().groupOrders(posTerminal, cashUpId, cashUpDate);
        posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
            jsonCashup.getString("posTerminal"));

        CashCloseProcessor processor = WeldUtils
            .getInstanceFromStaticBeanManager(CashCloseProcessor.class);
        JSONArray cashMgmtIds = jsonCashup.getJSONArray("cashMgmtIds");
        JSONObject result = processor.processCashClose(posTerminal, jsonCashup, cashMgmtIds,
            cashUpDate);

        // add the messages returned by processCashClose...
        jsonData.put("messages", result.opt("messages"));
        jsonData.put("next", result.opt("next"));
        jsonData.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      } finally {
        TriggerHandler.getInstance().enable();
      }
    } else {
      // This cashup has already been saved. Nothing needs to be done
      jsonData.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    }
    return jsonData;
  }

  @Override
  protected String getProperty() {
    return "OBPOS_retail.cashup";
  }

  /**
   * Looks for tickets created before the first ticket in this cashup, and which have a non-existant
   * cash up id, and reallocates them to this cashup
   * 
   * @param cashUpId
   * @param posTerminal
   */
  private void reassignTicketsFromLostCashups(String cashUpId, OBPOSApplications posTerminal) {
    String hqllastDate = "select min(creationDate) from Order as o where o.obposApplications.id=? and o.obposAppCashup=?";
    Query lastDateQuery = OBDal.getInstance().getSession().createQuery(hqllastDate);
    lastDateQuery.setString(0, posTerminal.getId());
    lastDateQuery.setString(1, cashUpId);
    Date lastDate = (Date) lastDateQuery.uniqueResult();
    if (lastDate == null) {
      return;
    }
    String hqlFirstDate = "select max(creationDate) from OBPOS_App_Cashup as cashup where cashup.pOSTerminal.id=?";
    Query firstDateQuery = OBDal.getInstance().getSession().createQuery(hqlFirstDate);
    firstDateQuery.setString(0, posTerminal.getId());
    Date firstDate = (Date) firstDateQuery.uniqueResult();
    if (firstDate == null) {
      return;
    }

    String hqlReassignableOrderScroll = " as o where o.obposApplications=:pos and o.creationDate>=:firstDate and o.creationDate<=:lastDate"
        + " and not exists (select 1 from OBPOS_App_Cashup as c where c.id=o.obposAppCashup) and o.obposAppCashup<>:currentcashup";
    OBQuery<Order> reassignableTicketsQuery = OBDal.getInstance().createQuery(Order.class,
        hqlReassignableOrderScroll);
    reassignableTicketsQuery.setNamedParameter("pos", posTerminal);
    reassignableTicketsQuery.setNamedParameter("firstDate", firstDate);
    reassignableTicketsQuery.setNamedParameter("lastDate", lastDate);
    reassignableTicketsQuery.setNamedParameter("currentcashup", cashUpId);

    ScrollableResults reassignableOrderScroll = reassignableTicketsQuery
        .scroll(ScrollMode.FORWARD_ONLY);
    while (reassignableOrderScroll.next()) {
      Order order = (Order) reassignableOrderScroll.get()[0];
      log.info("Reassigned ticket " + order.getDocumentNo() + " (id: " + order.getId()
          + ") to cash up id: " + cashUpId);
      order.setObposAppCashup(cashUpId);
    }
    reassignableOrderScroll.close();
    OBDal.getInstance().flush();
  }
}
