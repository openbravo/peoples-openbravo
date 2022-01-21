/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.core.TriggerHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.DataSynchronizationImportProcess;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.service.json.JsonConstants;

@DataSynchronization(entity = "OBPOS_SafeBox")
public class ProcessCountSafeBox extends POSDataSynchronizationProcess
    implements DataSynchronizationImportProcess {

  @Inject
  @Any
  private Instance<ProcessCountSafeBoxHook> countSafeBoxProcesses;

  JSONObject jsonResponse = new JSONObject();
  private static final Logger log = LogManager.getLogger();

  @Override
  protected String getImportQualifier() {
    return "OBPOS_SafeBox";
  }

  @Override
  public JSONObject saveRecord(JSONObject countSafeBox) throws Exception {
    JSONObject jsonData = new JSONObject();
    JSONObject jsonCountSafeBox = new JSONObject(countSafeBox.getString("objToSend"));
    Date countSafeBoxDate = new Date();
    OBPOSApplications posTerminal = OBDal.getInstance()
        .get(OBPOSApplications.class,
            jsonCountSafeBox.has("posTerminal") ? jsonCountSafeBox.getString("posTerminal")
                : jsonCountSafeBox.getString("posterminal"));

    // get and prepare the countSafeBox Date
    if (jsonCountSafeBox.has("countSafeBoxDate") && jsonCountSafeBox.get("countSafeBoxDate") != null
        && StringUtils.isNotEmpty(jsonCountSafeBox.getString("countSafeBoxDate"))) {
      final String strCashUpDate = jsonCountSafeBox.getString("countSafeBoxDate");
      if (!strCashUpDate.substring(strCashUpDate.length() - 1).equals("Z")) {
        log.error(String.format(
            "The countSafeBox date must be provided in ISO 8601 format and be an UTC date (value: '%s')",
            strCashUpDate));
      }
      // get the timezoneOffset
      final long timezoneOffset;
      if (jsonCountSafeBox.has("timezoneOffset") && jsonCountSafeBox.get("timezoneOffset") != null
          && StringUtils.isNotEmpty(jsonCountSafeBox.getString("timezoneOffset"))) {
        timezoneOffset = Long.parseLong(jsonCountSafeBox.getString("timezoneOffset"));
      } else {
        timezoneOffset = -((Calendar.getInstance().get(Calendar.ZONE_OFFSET)
            + Calendar.getInstance().get(Calendar.DST_OFFSET)) / (60 * 1000));
        log.error(
            "Error processing count safe box (1): error retrieving the timezoneOffset. Using the current timezoneOffset");
      }
      countSafeBoxDate = OBMOBCUtils.calculateServerDatetime(strCashUpDate, timezoneOffset);
    } else {
      log.debug(
          "Error processing count safe box (2): error retrieving countSafeBox date. Using current server date");
    }
    OBCriteria<OBPOSSafeBox> safeBoxCriteria = OBDal.getInstance()
        .createCriteria(OBPOSSafeBox.class);
    safeBoxCriteria.add(
        Restrictions.eq(OBPOSSafeBox.PROPERTY_SEARCHKEY, jsonCountSafeBox.getString("safeBox")));
    safeBoxCriteria
        .add(Restrictions.eq(OBPOSSafeBox.PROPERTY_ORGANIZATION, posTerminal.getOrganization()));

    OBPOSSafeBox safebox = (OBPOSSafeBox) safeBoxCriteria.uniqueResult();

    doReconciliation(safebox, jsonCountSafeBox, jsonData, countSafeBoxDate);

    jsonData.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

    return jsonData;
  }

  private void doReconciliation(OBPOSSafeBox safebox, JSONObject jsonCountSafeBox,
      JSONObject jsonData, Date countSafeBoxDate) throws Exception {
    // check if there is a reconciliation in draft status
    for (OBPOSSafeBoxPaymentMethod payment : safebox.getOBPOSSafeBoxPaymentMethodList()) {
      if (payment.getFINFinancialaccount() == null) {
        continue;
      }
      final OBCriteria<FIN_Reconciliation> recconciliations = OBDal.getInstance()
          .createCriteria(FIN_Reconciliation.class);
      recconciliations.add(Restrictions.eq(FIN_Reconciliation.PROPERTY_DOCUMENTSTATUS, "DR"));
      recconciliations.add(
          Restrictions.eq(FIN_Reconciliation.PROPERTY_ACCOUNT, payment.getFINFinancialaccount()));
      for (final FIN_Reconciliation r : recconciliations.list()) {
        // will end up in the pos error window
        throw new OBException(
            "Count safe box can not proceed, there is a reconciliation in draft status, payment method: "
                + payment.getIdentifier() + ", reconcilliation: " + r.getDocumentNo() + " ("
                + r.getAccount().getName() + ")");
      }
    }

    // This cashup is a closed box
    TriggerHandler.getInstance().disable();
    try {
      CountSafeBoxProcessor processor = getCountSafeBoxProcessor();
      JSONObject result = processor.processCountSafeBox(safebox, jsonCountSafeBox, countSafeBoxDate,
          this);
      // add the messages returned by processCashClose...
      jsonData.put("messages", result.opt("messages"));
      jsonData.put("next", result.opt("next"));
      jsonData.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } finally {
      // enable triggers contains a flush in getConnection method
      TriggerHandler.getInstance().enable();
    }
  }

  protected CountSafeBoxProcessor getCountSafeBoxProcessor() {
    return WeldUtils.getInstanceFromStaticBeanManager(CountSafeBoxProcessor.class);
  }

  // We do not have to check if the role has access because now, we update cashup with every order.
  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected String getProperty() {
    return "OBPOS_retail.cashup";
  }

  protected void executeHooks(Instance<? extends Object> hooks, JSONObject jsonCountSafeBox,
      JSONObject jsonCurrentSafeBox, OBPOSSafeBox safeBox, OBPOSSafeBoxPaymentMethod paymentMethod,
      FIN_FinaccTransaction finAccPaymentTransaction,
      FIN_FinaccTransaction finAccDepositTransaction) throws Exception {

    for (Iterator<? extends Object> procIter = hooks.iterator(); procIter.hasNext();) {
      Object proc = procIter.next();
      ((ProcessCountSafeBoxHook) proc).exec(jsonCountSafeBox, jsonCurrentSafeBox, safeBox,
          paymentMethod, finAccPaymentTransaction, finAccDepositTransaction);
    }
  }

  public void executeHooksCountSafeBoxProcesses(JSONObject jsonCountSafeBox,
      JSONObject jsonCurrentSafeBox, OBPOSSafeBox safeBox, OBPOSSafeBoxPaymentMethod paymentMethod,
      FIN_FinaccTransaction finAccPaymentTransaction,
      FIN_FinaccTransaction finAccDepositTransaction) throws Exception {
    executeHooks(countSafeBoxProcesses, jsonCountSafeBox, jsonCurrentSafeBox, safeBox,
        paymentMethod, finAccPaymentTransaction, finAccDepositTransaction);
  }

}
