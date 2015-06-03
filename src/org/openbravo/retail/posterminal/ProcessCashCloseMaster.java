/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

public class ProcessCashCloseMaster extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    String masterterminal = jsonsent.getString("masterterminal");
    String cashUpId = jsonsent.getString("cashUpId");
    OBCriteria<OBPOSApplications> obCriteria = OBDal.getInstance().createCriteria(
        OBPOSApplications.class);
    obCriteria.add(Restrictions.eq(OBPOSApplications.PROPERTY_MASTERTERMINAL + ".id",
        masterterminal));
    List<OBPOSApplications> applications = obCriteria.list();
    JSONArray terminals = new JSONArray();
    boolean finishAll = true;
    for (OBPOSApplications application : applications) {
      JSONObject terminal = new JSONObject();
      terminal.put("id", application.getId());
      terminal.put("searchKey", application.getSearchKey());
      terminal.put("name", application.getName());
      OBPOSAppCashup terminalCashUp = getTerminalCashUp(application.getId(), cashUpId);
      boolean finish = terminalCashUp != null && terminalCashUp.isProcessed()
          && terminalCashUp.isProcessedbo();
      terminal.put("finish", finish);
      terminal.put("cashUpId", terminalCashUp != null ? terminalCashUp.getId() : null);
      if (!finish) {
        finishAll = false;
      }
      terminals.put(terminal);
    }
    JSONObject data = new JSONObject();
    data.put("terminals", terminals);
    data.put("finishAll", finishAll);
    if (finishAll) {
      JSONArray payments = new JSONArray();
      data.put("payments", payments);
      List<String> cashUpIds = new ArrayList<String>();
      for (int i = 0; i < terminals.length(); i++) {
        JSONObject terminal = terminals.getJSONObject(i);
        cashUpIds.add(terminal.getString("cashUpId"));
      }
      addPaymentmethodCashup(payments, cashUpIds);
    }
    result.put("data", data);
    result.put("status", 0);
    return result;
  }

  /**
   * Get cash up for terminal and parent cash up
   * 
   * @param posterminal
   *          Terminal id.
   * @param parentCashUp
   *          Parent cash up id.
   * @return
   */
  public static OBPOSAppCashup getTerminalCashUp(String posterminal, String parentCashUp) {
    OBCriteria<OBPOSAppCashup> obCriteria = OBDal.getInstance()
        .createCriteria(OBPOSAppCashup.class);
    obCriteria.add(Restrictions.eq(OBPOSAppCashup.PROPERTY_POSTERMINAL + ".id", posterminal));
    obCriteria
        .add(Restrictions.eq(OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + ".id", parentCashUp));
    List<OBPOSAppCashup> cashUp = obCriteria.list();
    return cashUp.size() > 0 ? cashUp.get(0) : null;
  }

  /**
   * Accumulate share payment methods
   * 
   * @param payments
   *          Payments list
   * @param cashUpIds
   *          Cash up ids. with IN format
   * @throws JSONException
   */
  public static void addPaymentmethodCashup(JSONArray payments, List<String> cashUpIds)
      throws JSONException {
    String query = "select " + OBPOSPaymentMethodCashup.PROPERTY_SEARCHKEY + ", sum("
        + OBPOSPaymentMethodCashup.PROPERTY_STARTINGCASH + "), sum("
        + OBPOSPaymentMethodCashup.PROPERTY_TOTALDEPOSITS + "), sum("
        + OBPOSPaymentMethodCashup.PROPERTY_TOTALDROPS + "), sum("
        + OBPOSPaymentMethodCashup.PROPERTY_TOTALRETURNS + "), sum("
        + OBPOSPaymentMethodCashup.PROPERTY_TOTALSALES + "), sum( "
        + OBPOSPaymentMethodCashup.PROPERTY_AMOUNTTOKEEP + ") " + "from OBPOS_Paymentmethodcashup "
        + "where cashUp.id in :cashUpIds and paymentType.paymentMethod.isshared = 'Y'"
        + "group by 1";
    final Session session = OBDal.getInstance().getSession();
    final Query paymentQuery = session.createQuery(query);
    paymentQuery.setParameterList("cashUpIds", cashUpIds);
    List<?> paymentList = paymentQuery.list();
    for (int i = 0; i < paymentList.size(); i++) {
      Object[] item = (Object[]) paymentList.get(i);
      JSONObject paymentCashup = new JSONObject();
      paymentCashup.put("searchKey", item[0]);
      paymentCashup.put("startingCash", item[1]);
      paymentCashup.put("totalDeposits", item[2]);
      paymentCashup.put("totalDrops", item[3]);
      paymentCashup.put("totalReturns", item[4]);
      paymentCashup.put("totalSales", item[5]);
      paymentCashup.put("amountToKeep", item[6]);
      payments.put(paymentCashup);
    }
  }

}
