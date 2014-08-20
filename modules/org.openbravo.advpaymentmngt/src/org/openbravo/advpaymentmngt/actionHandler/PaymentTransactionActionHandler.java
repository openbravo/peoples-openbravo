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

package org.openbravo.advpaymentmngt.actionHandler;

import java.math.BigDecimal;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public class PaymentTransactionActionHandler extends BaseActionHandler {

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    try {
      final JSONObject jsonData = new JSONObject(data);
      final String strPaymentId = jsonData.getString("strPaymentId");
      String description = "";
      JSONObject result = new JSONObject();
      if ("".equals(strPaymentId)) {
        result.put("description", "");
        result.put("inpdepositamt", BigDecimal.ZERO);
        result.put("inppaymentamt", BigDecimal.ZERO);
      }
      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, strPaymentId);
      if ((payment.isReceipt() && payment.getAmount().compareTo(BigDecimal.ZERO) > 0)
          || (!payment.isReceipt() && payment.getAmount().compareTo(BigDecimal.ZERO) < 0)) {
        result.put("inpdepositamt", payment.getFinancialTransactionAmount().abs());
        result.put("inppaymentamt", BigDecimal.ZERO);
      } else {
        result.put("inpdepositamt", BigDecimal.ZERO);
        result.put("inppaymentamt", payment.getFinancialTransactionAmount().abs());
      }
      if (payment.getBusinessPartner() != null) {
        result.put("inpcBpartnerId", payment.getBusinessPartner().getId());
      }
      if (payment.getDescription() != null) {
        result.put("description", payment.getDescription());
      }

      return result;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

}