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
import org.openbravo.model.common.invoice.Invoice;

@ComponentProvider.Qualifier(APRMConstants.PURCHASE_INVOICE_WINDOW_ID)
public class PurchaseInvoiceAddPaymentDefaultValues extends AddPaymentDefaultValuesHandler {

  @Override
  public String getDefaultExpectedAmount(Map<String, String> requestMap) throws JSONException {
    BigDecimal pendingAmt = getPendingAmount(requestMap);
    return pendingAmt.toPlainString();
  }

  @Override
  String getDefaultActualAmount(Map<String, String> requestMap) throws JSONException {
    BigDecimal pendingAmt = getPendingAmount(requestMap);
    return pendingAmt.toPlainString();
  }

  private BigDecimal getPendingAmount(Map<String, String> requestMap) throws JSONException {
    Invoice invoice = OBDal.getInstance().get(Invoice.class, getDefaultInvoiceType(requestMap));
    BigDecimal pendingAmt = getPendingAmt(invoice.getFINPaymentScheduleList());
    return pendingAmt;
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
  String getDefaultPaymentType(Map<String, String> requestMap) {
    return "";
  }

  @Override
  String getDefaultOrderType(Map<String, String> requestMap) {
    return "";
  }

  @Override
  String getDefaultInvoiceType(Map<String, String> requestMap) throws JSONException {
    JSONObject context = new JSONObject(requestMap.get("context"));
    return context.getString("inpcInvoiceId");
  }

  @Override
  String getDefaultConversionRate(Map<String, String> requestMap) throws JSONException {
    return "";
  }

  @Override
  String getDefaultConvertedAmount(Map<String, String> requestMap) throws JSONException {
    return "";
  }

}
