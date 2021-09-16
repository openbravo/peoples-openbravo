/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.service.json.JsonConstants;

public class SafeBoxes extends JSONProcessSimple {
  public static final String safeBoxesPropertyExtension = "SafeBoxesExtension";
  public static final String safeBoxesPaymentMethodsPropertyExtension = "SafeBoxesExtensionPaymentMethods";
  public static final String safeBoxesPaymentMethodsTransactionPropertyExtension = "SafeBoxesPaymentMethodsExtensionTransaction";
  public static final String safeBoxesPaymentMethodsAccountPropertyExtension = "SafeBoxesPaymentMethodsExtensionAccount";
  public static final String LAST_TERMINAL_IN = "lastIn";
  public static final String LAST_TERMINAL_OUT = "lastOut";
  public static final String LAST_TERMINAL_SEARCHKEY = "searchKey";
  private static final String SAFE_BOX_CASHIER_NAME = "safeBoxCashierName";
  private static final String SAFE_BOX_CASHIER_USER_NAME = "safeBoxCashierUserName";

  @Inject
  @Any
  @Qualifier(safeBoxesPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Inject
  @Any
  @Qualifier(safeBoxesPaymentMethodsPropertyExtension)
  private Instance<ModelExtension> extensionsPaymentMethods;

  @Inject
  @Any
  @Qualifier(safeBoxesPaymentMethodsTransactionPropertyExtension)
  private Instance<ModelExtension> extensionsPMTransaction;

  @Inject
  @Any
  @Qualifier(safeBoxesPaymentMethodsAccountPropertyExtension)
  private Instance<ModelExtension> extensionsPMAccount;

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      String orgId = jsonsent.getString("organization");
      String optSafeBoxSearchKey = null;
      if (jsonsent.has("safeBoxSearchKey")) {
        optSafeBoxSearchKey = jsonsent.getString("safeBoxSearchKey");
      }
      // get the safe boxes
      HQLPropertyList hqlPropertiesSafeBox = ModelExtensionUtils.getPropertyExtensions(extensions);
      String hqlSafeBoxes = "SELECT " + hqlPropertiesSafeBox.getHqlSelect()
          + " FROM OBPOS_SafeBox AS sf" //
          + " LEFT JOIN sf.user AS u " //
          + " WHERE 1 = 1" + " AND sf.organization.id = :organization";

      if (optSafeBoxSearchKey != null) {
        hqlSafeBoxes += " AND sf.searchKey = :safeBoxSearchKey";
      }

      // Set always the same order
      hqlSafeBoxes += " ORDER BY sf.searchKey ";

      @SuppressWarnings("rawtypes")
      Query safeBoxesQuery = OBDal.getInstance().getSession().createQuery(hqlSafeBoxes);

      safeBoxesQuery.setParameter("organization", orgId);

      if (optSafeBoxSearchKey != null) {
        safeBoxesQuery.setParameter("safeBoxSearchKey", optSafeBoxSearchKey);
      }

      JSONArray safeBoxes = hqlPropertiesSafeBox.getJSONArray(safeBoxesQuery);

      for (int countSafeBox = 0; countSafeBox < safeBoxes.length(); countSafeBox++) {
        JSONObject safeBox = safeBoxes.getJSONObject(countSafeBox);

        OBPOSSafeBox safeBoxObject = OBDal.getInstance()
            .get(OBPOSSafeBox.class, safeBox.getString("safeBoxId"));

        JSONObject lastHistoryRecord = getLastHistoryRecord(safeBoxObject);

        safeBox.putOpt("lastTouchpoint", lastHistoryRecord);

        if (lastHistoryRecord != null && lastHistoryRecord.has(SAFE_BOX_CASHIER_NAME)) {
          safeBox.put(SAFE_BOX_CASHIER_NAME, lastHistoryRecord.get(SAFE_BOX_CASHIER_NAME));
          safeBox.put(SAFE_BOX_CASHIER_USER_NAME,
              lastHistoryRecord.get(SAFE_BOX_CASHIER_USER_NAME));
        }

        JSONArray safeBoxPaymentMethods = new JSONArray();
        // get the details of each payment method
        HQLPropertyList hqlPropertiesPaymentMethods = ModelExtensionUtils
            .getPropertyExtensions(extensionsPaymentMethods);
        String hqlSafeBoxPaymentMethods = "SELECT " + hqlPropertiesPaymentMethods.getHqlSelect() //
            + " FROM OBPOS_SafeBox_PaymentMethod AS sfpm" //
            + " WHERE sfpm.obposSafebox.id = :safeBoxId order by sfpm.paymentMethod.name, sfpm.fINFinancialaccount.name"; //

        @SuppressWarnings("rawtypes")
        Query safeBoxPaymentMethodsQuery = OBDal.getInstance()
            .getSession()
            .createQuery(hqlSafeBoxPaymentMethods);
        safeBoxPaymentMethodsQuery.setParameter("safeBoxId", safeBox.getString("safeBoxId"));

        safeBoxPaymentMethods = hqlPropertiesPaymentMethods
            .getJSONArray(safeBoxPaymentMethodsQuery);

        for (int countPaymentMethod = 0; countPaymentMethod < safeBoxPaymentMethods
            .length(); countPaymentMethod++) {
          JSONObject safeBoxPaymentMethod = safeBoxPaymentMethods.getJSONObject(countPaymentMethod);

          JSONObject safeBoxPaymentMethodCounting = new JSONObject();

          // Get deposits and withdrawals transactions balance not reconciled
          HQLPropertyList hqlPropertiesTransaction = ModelExtensionUtils
              .getPropertyExtensions(extensionsPMTransaction);
          String hqlSafeBoxPaymentMethodTransaction = " SELECT "
              + hqlPropertiesTransaction.getHqlSelect() //
              + " FROM FIN_Finacc_Transaction AS fft" //
              + " WHERE fft.account.id = :financialAccountId" //
              + " AND fft.reconciliation IS NULL"; //

          @SuppressWarnings("rawtypes")
          Query safeBoxPaymentMethodTransactionQuery = OBDal.getInstance()
              .getSession()
              .createQuery(hqlSafeBoxPaymentMethodTransaction);
          safeBoxPaymentMethodTransactionQuery.setParameter("financialAccountId",
              safeBoxPaymentMethod.getString("financialAccountId"));

          JSONObject safeBoxPaymentMethodTransaction = hqlPropertiesTransaction
              .getJSONArray(safeBoxPaymentMethodTransactionQuery)
              .getJSONObject(0);

          // Get ending balance of the last reconciliation or the initial balance of the account
          HQLPropertyList hqlPropertiesAccount = ModelExtensionUtils
              .getPropertyExtensions(extensionsPMAccount);
          String hqlSafeBoxPaymentMethodAccount = " SELECT " + hqlPropertiesAccount.getHqlSelect() //
              + " FROM FIN_Financial_Account as ffa" //
              + " WHERE ffa.id = :financialAccountId"; //

          @SuppressWarnings("rawtypes")
          Query safeBoxPaymentMethodAccountQuery = OBDal.getInstance()
              .getSession()
              .createQuery(hqlSafeBoxPaymentMethodAccount);
          safeBoxPaymentMethodAccountQuery.setParameter("financialAccountId",
              safeBoxPaymentMethod.getString("financialAccountId"));

          JSONObject safeBoxPaymentMethodAccount = hqlPropertiesAccount
              .getJSONArray(safeBoxPaymentMethodAccountQuery)
              .getJSONObject(0);

          // Save values in result JSON
          safeBoxPaymentMethodCounting.put("depositBalance",
              safeBoxPaymentMethodTransaction.getDouble("depositBalance"));
          safeBoxPaymentMethodCounting.put("paymentBalance",
              safeBoxPaymentMethodTransaction.getDouble("paymentBalance"));
          safeBoxPaymentMethodCounting.put("initialBalance",
              safeBoxPaymentMethodAccount.getDouble("initialBalance"));

          safeBoxPaymentMethod.put("safeBoxCounting", safeBoxPaymentMethodCounting);

          if (safeBoxPaymentMethod.optBoolean("countPerAmount")) {
            safeBoxPaymentMethod.put("countPerAmountEntries",
                getCountPerAmountEntries(safeBoxObject, safeBoxPaymentMethod));
          }
        }
        safeBox.put("paymentMethods", safeBoxPaymentMethods);
      }

      result.put(JsonConstants.RESPONSE_DATA, safeBoxes);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  public static JSONObject getCountPerAmountEntries(OBPOSSafeBox safebox,
      JSONObject safeBoxPaymentMethodJson) throws JSONException {
    List<OBPOSSafeboxTouchpoint> uncountedCashups = getUncountedCashups(safebox);

    JSONObject countPerAmountEntries = new JSONObject();
    for (OBPOSSafeboxTouchpoint obposSafeboxTouchpoint : uncountedCashups) {
      OBPOSAppCashup cashUp = obposSafeboxTouchpoint.getCashUp();
      OBPOSPaymentMethodCashup paymentMethodCashup = getPaymentMethodCashup(cashUp,
          safeBoxPaymentMethodJson);
      if (paymentMethodCashup == null) {
        continue;
      }
      List<OBPOS_PaymentMethodCashCountPerAmount> countPerAmount = paymentMethodCashup
          .getObposPmcashupAmntcntList();
      for (OBPOS_PaymentMethodCashCountPerAmount countPerAmountEntry : countPerAmount) {
        String key = countPerAmountEntry.getAmount().toString();
        Long cashupExpected = countPerAmountEntry.getExpectedAmount();
        long currentExpected = countPerAmountEntries.optLong(key, 0);
        countPerAmountEntries.put(key, currentExpected + cashupExpected);
      }
    }
    return countPerAmountEntries;
  }

  private static List<OBPOSSafeboxTouchpoint> getUncountedCashups(OBPOSSafeBox safebox) {
    //@formatter:off
    String hql = 
            "as sfc" +
            " where sfc.obposSafebox.id = :safeboxId" +
            " and sfc.cashUp is not null" +
            " and sfc.iscounted = false";
    //@formatter:on

    List<OBPOSSafeboxTouchpoint> uncountedCashups = OBDal.getInstance()
        .createQuery(OBPOSSafeboxTouchpoint.class, hql)
        .setNamedParameter("safeboxId", safebox.getId())
        .list();
    return uncountedCashups;
  }

  private static OBPOSPaymentMethodCashup getPaymentMethodCashup(OBPOSAppCashup cashUp,
      JSONObject safeBoxPaymentMethodJson) throws JSONException {
    //@formatter:off
    String hql = 
            "as pmc" +
            " where pmc.cashUp.id = :cashUpId" +
            " and pmc.paymentType.paymentMethod.paymentMethod.id = :paymentMethodId" +
            " and pmc.paymentType.paymentMethod.currency.id = :currencyId";
    //@formatter:on

    return OBDal.getInstance()
        .createQuery(OBPOSPaymentMethodCashup.class, hql)
        .setNamedParameter("cashUpId", cashUp.getId())
        .setNamedParameter("paymentMethodId", safeBoxPaymentMethodJson.get("paymentMethodId"))
        .setNamedParameter("currencyId", safeBoxPaymentMethodJson.get("currency"))
        .uniqueResult();
  }

  /**
   * @name getLastHistoryRecord
   * @param safeBox
   *          The reference of a safeBox to get the last history record
   * @return OBPOSSafeboxTouchpoint the last history record of a given safeBox
   * @throws JSONException
   */
  public JSONObject getLastHistoryRecord(OBPOSSafeBox safeBox) throws JSONException {
    if (safeBox == null) {
      return null;
    }

    OBCriteria<OBPOSSafeboxTouchpoint> criteria = OBDal.getInstance()
        .createCriteria(OBPOSSafeboxTouchpoint.class);
    criteria.add(Restrictions.eq(OBPOSSafeboxTouchpoint.PROPERTY_OBPOSSAFEBOX, safeBox));
    criteria.addOrder(Order.desc(OBPOSSafeboxTouchpoint.PROPERTY_UPDATED));
    criteria.setMaxResults(1);
    OBPOSSafeboxTouchpoint lastTouchpoint = (OBPOSSafeboxTouchpoint) criteria.uniqueResult();

    if (lastTouchpoint == null) {
      return null;
    }

    JSONObject result = new JSONObject();
    result.put(LAST_TERMINAL_IN, lastTouchpoint.getDateIn());
    result.put(LAST_TERMINAL_OUT, lastTouchpoint.getDateOut());
    result.put(LAST_TERMINAL_SEARCHKEY, lastTouchpoint.getTouchpoint().getSearchKey());

    User safeboxUser = lastTouchpoint.getSafeboxUser();
    if (safeboxUser != null) {
      result.put(SAFE_BOX_CASHIER_NAME, safeboxUser.getName());
      result.put(SAFE_BOX_CASHIER_USER_NAME, safeboxUser.getUsername());
    }

    return result;
  }

}
