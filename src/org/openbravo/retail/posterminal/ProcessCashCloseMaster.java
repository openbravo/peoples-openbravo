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
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;

public class ProcessCashCloseMaster extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    String masterterminal = jsonsent.getString("masterterminal");
    String cashUpId = jsonsent.getString("cashUpId");

    OBPOSAppCashup appCashup = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);
    UpdateCashup.associateMasterSlave(appCashup,
        OBDal.getInstance().get(OBPOSApplications.class, masterterminal));
    OBDal.getInstance().flush();

    OBDal.getInstance().getSession().evict(appCashup);
    appCashup = OBDal.getInstance().get(OBPOSAppCashup.class, cashUpId);

    OBCriteria<OBPOSApplications> obCriteria = OBDal.getInstance().createCriteria(
        OBPOSApplications.class);
    obCriteria.add(Restrictions.eq(OBPOSApplications.PROPERTY_MASTERTERMINAL + ".id",
        masterterminal));
    obCriteria.addOrderBy(OBPOSApplications.PROPERTY_SEARCHKEY, true);
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
      int noOfTransactions = (terminalCashUp != null ? getnoOfTransactions(terminalCashUp.getId())
          : 0);
      terminal.put("noOfTransactions", noOfTransactions);
      terminal.put("cashUpId", terminalCashUp != null ? terminalCashUp.getId() : null);
      if (!finish && noOfTransactions > 0) {
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
        if (terminal.getBoolean("finish")) {
          cashUpIds.add(terminal.getString("cashUpId"));
        }
      }
      if (cashUpIds.size() > 0) {
        addPaymentmethodCashup(payments, cashUpIds);
      }
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
   */
  private OBPOSAppCashup getTerminalCashUp(String posterminal, String parentCashUp) {
    OBCriteria<OBPOSAppCashup> obCriteria = OBDal.getInstance()
        .createCriteria(OBPOSAppCashup.class);
    obCriteria.add(Restrictions.eq(OBPOSAppCashup.PROPERTY_POSTERMINAL + ".id", posterminal));
    obCriteria
        .add(Restrictions.eq(OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + ".id", parentCashUp));
    List<OBPOSAppCashup> cashUp = obCriteria.list();
    return cashUp.size() > 0 ? cashUp.get(0) : null;
  }

  /**
   * Check If CashUp has Transactions associated
   * 
   * @param cashUp
   *          Cash up id.
   * @return 1 if has transactions, 0 if no transactions.
   */
  private int getnoOfTransactions(String cashUp) {
    OBCriteria<FIN_FinaccTransaction> obCriteria = OBDal.getInstance().createCriteria(
        FIN_FinaccTransaction.class);
    obCriteria.add(Restrictions.eq(FIN_FinaccTransaction.PROPERTY_OBPOSAPPCASHUP + ".id", cashUp));
    obCriteria.setMaxResults(1);
    List<FIN_FinaccTransaction> cashUpTransactions = obCriteria.list();
    return cashUpTransactions.size();
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
        + OBPOSPaymentMethodCashup.PROPERTY_AMOUNTTOKEEP + ")"
        + " from OBPOS_Paymentmethodcashup" //
        + " where cashUp.id in (:cashUpIds) and paymentType.paymentMethod.isshared = 'Y'"
        + " group by " + OBPOSPaymentMethodCashup.PROPERTY_SEARCHKEY;
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
