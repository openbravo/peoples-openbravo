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

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;

public class PaymentMethodMulticurrencyActionHandler extends BaseActionHandler {

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    try {
      final JSONObject jsonData = new JSONObject(data);
      final String paymentMethodId = jsonData.getString("paymentMethodId");
      final String financialAccountId = jsonData.getString("financialAccountId");

      final FIN_PaymentMethod paymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
          paymentMethodId);

      JSONObject result = new JSONObject();
      result.put("isPayinIsMulticurrency", paymentMethod.isPayinIsMulticurrency());
      if (!isValidFinancialAccount(paymentMethodId, financialAccountId)) {
        result.put("isWrongFinancialAccount", true);
      } else {
        result.put("isWrongFinancialAccount", false);
      }
      return result;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  private boolean isValidFinancialAccount(String paymentMethodId, String financialAccountId) {
    OBCriteria<FinAccPaymentMethod> obc = OBDal.getInstance().createCriteria(
        FinAccPaymentMethod.class);
    obc.setFilterOnReadableOrganization(false);
    obc.setMaxResults(1);
    obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT,
        OBDal.getInstance().get(FIN_FinancialAccount.class, financialAccountId)));
    obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
        OBDal.getInstance().get(FIN_PaymentMethod.class, paymentMethodId)));
    return obc.uniqueResult() != null;
  }
}