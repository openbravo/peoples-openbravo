/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.List;

import javax.servlet.ServletException;

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
      String hqlCashup = "select c.id, c.netsales as netSales, c.grosssales as grossSales, "
          + "c.netreturns as netReturns, c.grossreturns as grossReturns, c.totalretailtransactions as totalRetailTransactions,"
          + "c.creationDate as createdDate, c.createdBy.id as userId, c.isbeingprocessed, c.isProcessed, c.pOSTerminal.id as posterminal "
          + "from OBPOS_App_Cashup c where c.isProcessed=:isprocessed and c.pOSTerminal.id= :terminal order by c.creationDate desc";

      SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(hqlCashup, OBContext.getOBContext()
          .getCurrentClient().getId(), OBContext.getOBContext().getCurrentOrganization().getId(),
          null);

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
        cashupJSON.put("createdDate", cashup[6]);
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
      paymentMethodJSON.put("amountToKeep", paymentMethodJSON.get("amounttokeep"));
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
}
