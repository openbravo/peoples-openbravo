/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.SimpleQueryBuilder;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.retail.posterminal.OBPOSAppCashup;
import org.openbravo.retail.posterminal.OBPOSPaymentMethodCashup;
import org.openbravo.retail.posterminal.OBPOSTaxCashup;
import org.openbravo.retail.posterminal.ProcessCashClose;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;

public class Cashup extends JSONProcessSimple {
  private static final Logger log = Logger.getLogger(ProcessCashClose.class);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);

    try {

      JSONArray respArray = new JSONArray();
      String posId = RequestContext.get().getSessionAttribute("POSTerminal").toString();
      String isprocessed = jsonsent.getString("isprocessed");
      String isprocessedbo = "";
      if (jsonsent.has("isprocessedbo")) {
        isprocessedbo = " and c.isprocessedbo = "
            + jsonsent.getString("isprocessedbo").equalsIgnoreCase("Y");
      }

      String hqlCashup = "select c.id, c.netsales as netSales, c.grosssales as grossSales, "
          + "c.netreturns as netReturns, c.grossreturns as grossReturns, c.totalretailtransactions as totalRetailTransactions,"
          + "c.creationDate as creationDate, c.createdBy.id as userId, c.isbeingprocessed, c.isProcessed, c.pOSTerminal.id as posterminal "
          + "from OBPOS_App_Cashup c where c.isProcessed=:isprocessed and c.pOSTerminal.id= :terminal "
          + isprocessedbo + " order by c.creationDate desc";

      SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(hqlCashup, OBContext.getOBContext()
          .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization().getId(),
          null, null, null);

      final Session session = OBDal.getInstance().getSession();
      final Query cashupquery = session.createQuery(querybuilder.getHQLQuery());
      cashupquery.setParameter("isprocessed", isprocessed.equalsIgnoreCase("Y"));
      cashupquery.setParameter("terminal", posId);
      @SuppressWarnings("unchecked")
      List<Object[]> cashupList = cashupquery.list();
      DataToJsonConverter converter = new DataToJsonConverter();

      if (!cashupList.isEmpty()) {
        Object[] cashup = cashupList.get(0);
        JSONObject cashupJSON = new JSONObject();
        cashupJSON.put("id", cashup[0]);
        cashupJSON.put("netSales", cashup[1]);
        cashupJSON.put("grossSales", cashup[2]);
        cashupJSON.put("netReturns", cashup[3]);
        cashupJSON.put("grossReturns", cashup[4]);
        cashupJSON.put("totalRetailTransactions", cashup[5]);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(cashup[6]);
        cashupJSON.put("creationDate", nowAsISO);
        cashupJSON.put("userId", cashup[7]);
        cashupJSON.put("isbeingprocessed", ((Boolean) cashup[8]) ? "Y" : "N");
        cashupJSON.put("isprocessed", ((Boolean) cashup[9]) ? "Y" : "N");
        cashupJSON.put("posterminal", cashup[10]);

        // Get Payments
        JSONArray cashPaymentMethodInfo = getPayments((String) cashup[0], converter);
        cashupJSON.put("cashPaymentMethodInfo", cashPaymentMethodInfo);
        // Get Taxes
        JSONArray cashTaxInfo = getTaxes((String) cashup[0], converter);
        cashupJSON.put("cashTaxInfo", cashTaxInfo);

        // Get CashManagement
        JSONArray cashMgmtInfo = getCashMgmt((String) cashup[0], converter);
        cashupJSON.put("cashMgmInfo", cashMgmtInfo);

        respArray.put(cashupJSON);
      }
      result.put(JsonConstants.RESPONSE_DATA, respArray);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      return result;

    } catch (Exception e) {
      log.error(e);
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  public JSONArray getPayments(String cashupId, DataToJsonConverter converter) throws JSONException {
    JSONArray respArray = new JSONArray();
    OBPOSAppCashup cashupObj = OBDal.getInstance().get(OBPOSAppCashup.class, cashupId);
    OBCriteria<OBPOSPaymentMethodCashup> paymentMethodCashupCriteria = OBDal.getInstance()
        .createCriteria(OBPOSPaymentMethodCashup.class);
    paymentMethodCashupCriteria.add(Restrictions.eq(OBPOSPaymentMethodCashup.PROPERTY_CASHUP,
        cashupObj));
    List<OBPOSPaymentMethodCashup> paymentMethodList = paymentMethodCashupCriteria.list();
    for (BaseOBObject paymentMethod : paymentMethodList) {
      JSONObject paymentMethodJSON = converter.toJsonObject(paymentMethod, DataResolvingMode.FULL);
      paymentMethodJSON.put("cashup_id", paymentMethodJSON.get("cashUp"));
      paymentMethodJSON.put("searchKey", paymentMethodJSON.get("searchkey"));
      paymentMethodJSON.put("paymentmethod_id", paymentMethodJSON.get("paymentType"));
      paymentMethodJSON.put("startingCash", paymentMethodJSON.get("startingcash"));
      paymentMethodJSON.put("totalSales", paymentMethodJSON.get("totalsales"));
      paymentMethodJSON.put("totalReturns", paymentMethodJSON.get("totalreturns"));
      respArray.put(paymentMethodJSON);
    }

    return respArray;
  }

  public JSONArray getTaxes(String cashupId, DataToJsonConverter converter) throws JSONException {
    JSONArray respArray = new JSONArray();
    OBPOSAppCashup cashupObj = OBDal.getInstance().get(OBPOSAppCashup.class, cashupId);
    OBCriteria<OBPOSTaxCashup> taxCashupCriteria = OBDal.getInstance().createCriteria(
        OBPOSTaxCashup.class);
    taxCashupCriteria.add(Restrictions.eq(OBPOSTaxCashup.PROPERTY_CASHUP, cashupObj));
    List<OBPOSTaxCashup> taxesList = taxCashupCriteria.list();
    for (BaseOBObject tax : taxesList) {
      JSONObject taxJSON = converter.toJsonObject(tax, DataResolvingMode.FULL);
      JSONObject result = new JSONObject();
      result.put("cashup_id", taxJSON.get("cashup"));
      result.put("orderType", taxJSON.get("ordertype"));
      result.put("id", taxJSON.get("id"));
      result.put("name", taxJSON.get("name"));
      result.put("amount", taxJSON.get("amount"));
      respArray.put(result);
    }

    return respArray;
  }

  public JSONArray getCashMgmt(String cashupId, DataToJsonConverter converter) throws JSONException {
    JSONArray respArray = new JSONArray();
    String posId = RequestContext.get().getSessionAttribute("POSTerminal").toString();

    // Get GL Items associated to the payment methods of this terminal
    String[] paymentTypes = { "fmgi.oBPOSAppPaymentTypeCGlitemDropdepIDList",
        "fmgi.oBPOSAppPaymentTypeCGlitemWriteoffIDList",
        "fmgi.oBPOSAppPaymentTypeCashDifferencesList",
        "fmgi.oBPOSAppPaymentTypeGLItemForDepositsList",
        "fmgi.oBPOSAppPaymentTypeGLItemForDropsList" };
    List<GLItem> glItemList = new ArrayList<GLItem>();
    for (int i = 0; i < paymentTypes.length; i++) {
      String hqlglItem = "select distinct fmgi from FinancialMgmtGLItem fmgi join "
          + paymentTypes[i] + " as oapt " + "where oapt.id in (select oap.paymentMethod.id "
          + "from OBPOS_App_Payment oap where oap.obposApplications.id = :terminal)";
      SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(hqlglItem, OBContext.getOBContext()
          .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization().getId(),
          null, null, null);

      final Session session = OBDal.getInstance().getSession();
      final Query glitemquery = session.createQuery(querybuilder.getHQLQuery());
      glitemquery.setParameter("terminal", posId);

      @SuppressWarnings("unchecked")
      List<GLItem> glList = glitemquery.list();
      for (GLItem glItem : glList) {
        glItemList.add(glItem);
      }
    }

    // Get Financial Accounts
    String hqlFinanAcct = "from FIN_Financial_Account ffa where ffa.id in (select oap.financialAccount.id from OBPOS_App_Payment oap where oap.obposApplications.id = :terminal)";
    SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(hqlFinanAcct, OBContext.getOBContext()
        .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization().getId(),
        null, null, null);

    final Session session = OBDal.getInstance().getSession();
    final Query finacctquery = session.createQuery(querybuilder.getHQLQuery());
    finacctquery.setParameter("terminal", posId);
    @SuppressWarnings("unchecked")
    List<FIN_FinancialAccount> finAcctList = finacctquery.list();

    // Get Transactions from that cashupId and for the GL Items of the actual organization
    OBPOSAppCashup cashupObj = OBDal.getInstance().get(OBPOSAppCashup.class, cashupId);
    OBCriteria<FIN_FinaccTransaction> cashMgmTransCriteria = OBDal.getInstance().createCriteria(
        FIN_FinaccTransaction.class);
    cashMgmTransCriteria.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_OBPOSAPPCASHUP,
        cashupObj));
    cashMgmTransCriteria.add(Restrictions.in(FIN_FinaccTransaction.PROPERTY_GLITEM,
        glItemList.toArray()));
    cashMgmTransCriteria.add(Restrictions.in(FIN_FinaccTransaction.PROPERTY_ACCOUNT,
        finAcctList.toArray()));

    List<FIN_FinaccTransaction> cashMgmtList = cashMgmTransCriteria.list();
    for (BaseOBObject cashMgmt : cashMgmtList) {
      JSONObject cashMgmtJSON = converter.toJsonObject(cashMgmt, DataResolvingMode.FULL);
      JSONObject result = new JSONObject();
      Float totalamt = Float.parseFloat(cashMgmtJSON.get("paymentAmount").toString())
          + Float.parseFloat(cashMgmtJSON.get("depositAmount").toString());
      Calendar c = DatatypeConverter.parseDateTime(cashMgmtJSON.get("creationDate").toString());
      int hour = c.get(Calendar.HOUR_OF_DAY);
      int minute = c.get(Calendar.MINUTE);
      String creationDate = hour + ":" + minute;

      // Get Payment Method ID and Reason ID
      String financialacct = cashMgmtJSON.get("account").toString();
      String hqlPaymentMethod = "select oap.id as id, oap.obretcoCmevents.id as reason from OBPOS_App_Payment oap where oap.financialAccount.id = :financialacct and oap.obposApplications.id = :terminal";
      SimpleQueryBuilder paymentMethodbuilder = new SimpleQueryBuilder(hqlPaymentMethod, OBContext
          .getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
          .getCurrentOrganization().getId(), null, null, null);

      final Session paymentsession = OBDal.getInstance().getSession();
      final Query paymentfinacctquery = paymentsession.createQuery(paymentMethodbuilder
          .getHQLQuery());
      paymentfinacctquery.setParameter("terminal", posId);
      paymentfinacctquery.setParameter("financialacct", financialacct);
      Object[] paymentmethod = (Object[]) paymentfinacctquery.uniqueResult();
      String paymentmethodId = (String) paymentmethod[0];
      String reasonId = (String) paymentmethod[1];

      // Set the cashManagement data
      result.put("id", cashMgmtJSON.get("id"));
      result.put("description", cashMgmtJSON.get("description"));
      result.put("amount", totalamt.toString());
      result.put("origAmount", totalamt.toString());
      result.put("type", cashMgmtJSON.get("paymentAmount").toString().equals("0") ? "deposit"
          : "drop");
      result.put("reasonId", reasonId);
      result.put("paymentMethodId", paymentmethodId);
      result.put("time", creationDate);
      result.put("userId", cashMgmtJSON.get("createdBy"));
      result.put("user", cashMgmtJSON.get("createdBy$_identifier"));
      result.put("isocode", cashMgmtJSON.get("currency$_identifier"));
      result.put("cashup_id", cashMgmtJSON.get("obposAppCashup"));
      result.put("glItem", cashMgmtJSON.get("gLItem"));
      result.put("isbeingprocessed", cashMgmtJSON.get("aprmProcessed").equals("P") ? "Y" : "N");
      result.put("_idx", "");
      respArray.put(result);
    }
    return respArray;

  }
}
