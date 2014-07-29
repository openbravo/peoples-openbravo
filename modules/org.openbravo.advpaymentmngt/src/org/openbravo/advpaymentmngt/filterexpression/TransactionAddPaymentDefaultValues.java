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

@ComponentProvider.Qualifier(APRMConstants.TRANSACTION_WINDOW_ID)
public class TransactionAddPaymentDefaultValues extends AddPaymentDefaultValuesHandler {

  private static final long SEQUENCE = 100l;

  protected long getSeq() {
    return SEQUENCE;
  }

  @Override
  String getDefaultExpectedAmount(Map<String, String> requestMap) throws JSONException {
    return BigDecimal.ZERO.toPlainString();
  }

  @Override
  String getDefaultActualAmount(Map<String, String> requestMap) throws JSONException {
    return BigDecimal.ZERO.toPlainString();
  }

  @Override
  String getDefaultIsSOTrx(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    if (context.has("inptrxtype") && context.get("inptrxtype") != JSONObject.NULL
        && StringUtils.isNotEmpty(context.getString("inptrxtype"))) {
      String document = context.getString("inptrxtype");
      if ("BPD".equals(document)) {
        return "Y";
      } else if ("BPW".equals(document)) {
        return "N";
      } else {
        return "";
      }
    }
    return "";
  }

  @Override
  String getDefaultTransactionType(Map<String, String> requestMap) {
    return "I";
  }

  @Override
  String getDefaultPaymentType(Map<String, String> requestMap) throws JSONException {
    return "";
  }

  @Override
  String getDefaultOrderType(Map<String, String> requestMap) throws JSONException {
    return "";
  }

  @Override
  String getDefaultInvoiceType(Map<String, String> requestMap) throws JSONException {
    return "";
  }

  @Override
  String getDefaultConversionRate(Map<String, String> requestMap) throws JSONException {
    return BigDecimal.ONE.toPlainString();
  }

  @Override
  String getDefaultConvertedAmount(Map<String, String> requestMap) throws JSONException {
    return BigDecimal.ZERO.toPlainString();
  }

  @Override
  String getDefaultReceivedFrom(Map<String, String> requestMap) throws JSONException {
    return "";
  }

  @Override
  String getDefaultStandardPrecision(Map<String, String> requestMap) throws JSONException {
    return getFinancialAccount(requestMap).getCurrency().getStandardPrecision().toString();
  }

  @Override
  String getDefaultCurrency(Map<String, String> requestMap) throws JSONException {
    return getFinancialAccount(requestMap).getCurrency().getId().toString();
  }

  @Override
  String getOrganization(Map<String, String> requestMap) throws JSONException {
    // Organization of the current Payment
    return getFinancialAccount(requestMap).getOrganization().getId();
  }

  @Override
  String getDefaultPaymentMethod(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    boolean isReceipt = true;
    if (context.has("IsSOTrx")) {
      isReceipt = "Y".equals(context.get("IsSOTrx")) ? true : false;
    }

    FinAccPaymentMethod anyFinAccPaymentMethod = null;
    for (FinAccPaymentMethod finAccPaymentMethod : getFinancialAccount(requestMap)
        .getFinancialMgmtFinAccPaymentMethodList()) {
      if (finAccPaymentMethod.isDefault()) {
        if ((isReceipt && finAccPaymentMethod.isPayinAllow())
            || (!isReceipt && finAccPaymentMethod.isPayoutAllow())) {
          return finAccPaymentMethod.getPaymentMethod().getId();
        }
      }
      if ((isReceipt && finAccPaymentMethod.isPayinAllow())
          || (!isReceipt && finAccPaymentMethod.isPayoutAllow())) {
        anyFinAccPaymentMethod = finAccPaymentMethod;
      }
    }
    return anyFinAccPaymentMethod != null ? anyFinAccPaymentMethod.getPaymentMethod().getId() : "";
  }

  @Override
  String getDefaultDocument(Map<String, String> requestMap) throws JSONException {
    // Document Type
    JSONObject context = new JSONObject(requestMap.get("context"));
    if (context.has("inptrxtype") && context.get("inptrxtype") != JSONObject.NULL
        && StringUtils.isNotEmpty(context.getString("inptrxtype"))) {
      String document = context.getString("inptrxtype");
      if ("BPD".equals(document)) {
        return "RCIN";
      } else if ("BPW".equals(document)) {
        return "PDOUT";
      } else {
        return "";
      }
    }
    return "";
  }

  @Override
  String getDefaultPaymentDate(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    if (context.has("inpstatementdate") && !context.isNull("inpstatementdate")
        && !"".equals(context.getString("inpstatementdate"))) {
      try {
        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(context.getString("inpstatementdate"));
        return OBDateUtils.formatDate(date);
      } catch (ParseException e) {
        return OBDateUtils.formatDate(new Date());
      }
    } else {
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
