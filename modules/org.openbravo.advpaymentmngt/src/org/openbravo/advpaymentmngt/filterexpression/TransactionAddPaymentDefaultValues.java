/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.advpaymentmngt.filterexpression;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.utility.APRMConstants;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.service.json.JsonUtils;

@ComponentProvider.Qualifier(APRMConstants.TRANSACTION_WINDOW_ID)
public class TransactionAddPaymentDefaultValues extends AddPaymentDefaultValuesHandler {

  private static final long SEQUENCE = 100l;

  protected long getSeq() {
    return SEQUENCE;
  }

  @Override
  public String getDefaultExpectedAmount(Map<String, String> requestMap) throws JSONException {
    return BigDecimal.ZERO.toPlainString();
  }

  @Override
  public String getDefaultActualAmount(Map<String, String> requestMap) throws JSONException {
    return BigDecimal.ZERO.toPlainString();
  }

  @Override
  public String getDefaultIsSOTrx(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    String document = null;
    if (context.has("trxtype") && context.get("trxtype") != JSONObject.NULL
        && StringUtils.isNotEmpty(context.getString("trxtype"))) {
      document = context.getString("trxtype");
    } else if (context.has("inptrxtype") && context.get("inptrxtype") != JSONObject.NULL
        && StringUtils.isNotEmpty(context.getString("inptrxtype"))) {
      document = context.getString("inptrxtype");
    }
    if ("BPD".equals(document)) {
      return "Y";
    } else if ("BPW".equals(document)) {
      return "N";
    } else {
      return "";
    }
  }

  @Override
  public String getDefaultTransactionType(Map<String, String> requestMap) {
    return "I";
  }

  @Override
  public String getDefaultPaymentType(Map<String, String> requestMap) throws JSONException {
    return "";
  }

  @Override
  public String getDefaultOrderType(Map<String, String> requestMap) throws JSONException {
    return "";
  }

  @Override
  public String getDefaultInvoiceType(Map<String, String> requestMap) throws JSONException {
    return "";
  }

  @Override
  public String getDefaultConversionRate(Map<String, String> requestMap) throws JSONException {
    return BigDecimal.ONE.toPlainString();
  }

  @Override
  public String getDefaultConvertedAmount(Map<String, String> requestMap) throws JSONException {
    return BigDecimal.ZERO.toPlainString();
  }

  @Override
  public String getDefaultReceivedFrom(Map<String, String> requestMap) throws JSONException {
    return "";
  }

  @Override
  public String getDefaultStandardPrecision(Map<String, String> requestMap) throws JSONException {
    return getFinancialAccount(requestMap).getCurrency().getStandardPrecision().toString();
  }

  @Override
  public String getDefaultCurrency(Map<String, String> requestMap) throws JSONException {
    return getFinancialAccount(requestMap).getCurrency().getId().toString();
  }

  @Override
  public String getOrganization(Map<String, String> requestMap) throws JSONException {
    // Organization of the current Payment
    return getFinancialAccount(requestMap).getOrganization().getId();
  }

  @Override
  public String getDefaultPaymentMethod(Map<String, String> requestMap) throws JSONException {
    boolean isReceipt = "Y".equals(getDefaultIsSOTrx(requestMap));

    FinAccPaymentMethod anyFinAccPaymentMethod = null;
    for (FinAccPaymentMethod finAccPaymentMethod : getFinancialAccount(requestMap)
        .getFinancialMgmtFinAccPaymentMethodList()) {
      if (finAccPaymentMethod.isDefault()) {
        if ((isReceipt && finAccPaymentMethod.isPayinAllow() && !finAccPaymentMethod
            .isAutomaticDeposit())
            || (!isReceipt && finAccPaymentMethod.isPayoutAllow() && !finAccPaymentMethod
                .isAutomaticWithdrawn())) {
          return finAccPaymentMethod.getPaymentMethod().getId();
        }
      }
      if ((isReceipt && finAccPaymentMethod.isPayinAllow() && !finAccPaymentMethod
          .isAutomaticDeposit())
          || (!isReceipt && finAccPaymentMethod.isPayoutAllow() && !finAccPaymentMethod
              .isAutomaticWithdrawn())) {
        anyFinAccPaymentMethod = finAccPaymentMethod;
      }
    }
    return anyFinAccPaymentMethod != null ? anyFinAccPaymentMethod.getPaymentMethod().getId() : "";
  }

  @Override
  public String getDefaultDocument(Map<String, String> requestMap) throws JSONException {
    // Document Type
    JSONObject context = new JSONObject(requestMap.get("context"));
    String document = null;
    if (context.has("trxtype") && context.get("trxtype") != JSONObject.NULL
        && StringUtils.isNotEmpty(context.getString("trxtype"))) {
      document = context.getString("trxtype");
    } else if (context.has("inptrxtype") && context.get("inptrxtype") != JSONObject.NULL
        && StringUtils.isNotEmpty(context.getString("inptrxtype"))) {
      document = context.getString("inptrxtype");
    }
    if ("BPD".equals(document)) {
      return "RCIN";
    } else if ("BPW".equals(document)) {
      return "PDOUT";
    } else {
      return "";
    }
  }

  @Override
  public String getDefaultPaymentDate(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    String strTransactionDate = null;
    try {
      if (context.has("trxdate") && !context.isNull("trxdate")
          && !"".equals(context.getString("trxdate"))) {
        strTransactionDate = context.getString("trxdate");
        Date transactionDate = JsonUtils.createDateFormat().parse(strTransactionDate);
        return OBDateUtils.formatDate(transactionDate);
      } else if (context.has("inpstatementdate") && !context.isNull("inpstatementdate")
          && !"".equals(context.getString("inpstatementdate"))) {
        strTransactionDate = context.getString("inpstatementdate");
      }
      if (strTransactionDate != null) {

        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(strTransactionDate);
        return OBDateUtils.formatDate(date);
      } else {
        return OBDateUtils.formatDate(new Date());
      }
    } catch (ParseException e) {
      return OBDateUtils.formatDate(new Date());
    }

  }

  private FIN_FinancialAccount getFinancialAccount(Map<String, String> requestMap)
      throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    if (context.has("inpfinFinancialAccountId") && !context.isNull("inpfinFinancialAccountId")
        && !"".equals(context.getString("inpfinFinancialAccountId"))) {
      return OBDal.getInstance().get(FIN_FinancialAccount.class,
          context.get("inpfinFinancialAccountId"));
    }
    return null;
  }
}
