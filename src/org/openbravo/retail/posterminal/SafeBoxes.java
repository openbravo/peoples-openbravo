package org.openbravo.retail.posterminal;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.query.Query;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.service.json.JsonConstants;

public class SafeBoxes extends JSONProcessSimple {
  public static final String safeBoxesPropertyExtension = "SafeBoxesExtension";
  public static final String safeBoxesPaymentMethodsPropertyExtension = "SafeBoxesExtensionPaymentMethods";
  public static final String safeBoxesPaymentMethodsTransactionPropertyExtension = "SafeBoxesPaymentMethodsExtensionTransaction";
  public static final String safeBoxesPaymentMethodsAccountPropertyExtension = "SafeBoxesPaymentMethodsExtensionAccount";

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
      String optSafeBoxSearchKey = null;
      if (jsonsent.has("safeBoxSearchKey")) {
        optSafeBoxSearchKey = jsonsent.getString("safeBoxSearchKey");
      }
      // get the safe boxes
      HQLPropertyList hqlPropertiesSafeBox = ModelExtensionUtils.getPropertyExtensions(extensions);
      String hqlSafeBoxes = "SELECT " + hqlPropertiesSafeBox.getHqlSelect()
          + " FROM OBPOS_SafeBox AS sf" //
          + " LEFT JOIN sf.user AS u " //
          + " WHERE 1 = 1";

      if (optSafeBoxSearchKey != null) {
        hqlSafeBoxes += " AND sf.searchKey = :safeBoxSearchKey";
      }

      // Set always the same order
      hqlSafeBoxes += " ORDER BY sf.searchKey ";

      @SuppressWarnings("rawtypes")
      Query safeBoxesQuery = OBDal.getInstance().getSession().createQuery(hqlSafeBoxes);

      if (optSafeBoxSearchKey != null) {
        safeBoxesQuery.setParameter("safeBoxSearchKey", optSafeBoxSearchKey);
      }

      JSONArray safeBoxes = hqlPropertiesSafeBox.getJSONArray(safeBoxesQuery);

      for (int countSafeBox = 0; countSafeBox < safeBoxes.length(); countSafeBox++) {
        JSONObject safeBox = safeBoxes.getJSONObject(countSafeBox);

        JSONArray safeBoxPaymentMethods = new JSONArray();
        // get the details of each payment method
        HQLPropertyList hqlPropertiesPaymentMethods = ModelExtensionUtils
            .getPropertyExtensions(extensionsPaymentMethods);
        String hqlSafeBoxPaymentMethods = "SELECT " + hqlPropertiesPaymentMethods.getHqlSelect() //
            + " FROM OBPOS_SafeBox_PaymentMethod AS sfpm" //
            + " WHERE sfpm.obposSafebox.id = :safeBoxId"; //

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

}