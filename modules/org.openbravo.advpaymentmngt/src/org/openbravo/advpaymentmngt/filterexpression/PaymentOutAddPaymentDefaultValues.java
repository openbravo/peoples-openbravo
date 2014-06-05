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
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.utility.APRMConstants;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

@ComponentProvider.Qualifier(APRMConstants.PAYMENT_OUT_WINDOW_ID)
public class PaymentOutAddPaymentDefaultValues extends AddPaymentDefaultValuesHandler {

  private static final long SEQUENCE = 100l;

  protected long getSeq() {
    return SEQUENCE;
  }

  @Override
  String getDefaultExpectedAmount(Map<String, String> requestMap) throws JSONException {
    // Expected amount is the amount on the editing payment
    BigDecimal pendingAmt = getPayment(requestMap).getAmount();
    return pendingAmt.toPlainString();
  }

  @Override
  String getDefaultActualAmount(Map<String, String> requestMap) throws JSONException {
    // Actual amount is the amount on the editing payment
    BigDecimal pendingAmt = getPayment(requestMap).getAmount();
    return pendingAmt.toPlainString();
  }

  @Override
  String getDefaultGeneratedCredit(Map<String, String> requestMap) throws JSONException {
    BigDecimal generateCredit = getPayment(requestMap).getGeneratedCredit();
    if (generateCredit == null) {
      return (BigDecimal.ZERO).toPlainString();
    } else {
      return generateCredit.toPlainString();
    }

  }

  @Override
  String getDefaultIsSOTrx(Map<String, String> requestMap) {
    return "N";
  }

  @Override
  String getDefaultTransactionType(Map<String, String> requestMap) {
    return "I";
  }

  @Override
  String getDefaultPaymentType(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    return context.getString("inpfinPaymentId");
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
  String getDefaultDocumentNo(Map<String, String> requestMap) throws JSONException {
    FIN_Payment payment = getPayment(requestMap);

    return payment.getDocumentNo();
  }

  private FIN_Payment getPayment(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    String strFinPaymentId = context.getString("inpfinPaymentId");
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, strFinPaymentId);
    return payment;
  }

  @Override
  String getDefaultConversionRate(Map<String, String> requestMap) throws JSONException {
    // Conversion Rate of the current Payment
    FIN_Payment payment = getPayment(requestMap);
    return payment.getFinancialTransactionConvertRate().toPlainString();
  }

  @Override
  String getDefaultConvertedAmount(Map<String, String> requestMap) throws JSONException {
    // Converted Amount of the current Payment
    FIN_Payment payment = getPayment(requestMap);
    return payment.getFinancialTransactionAmount().toPlainString();
  }

}
