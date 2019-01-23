/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.ad_callouts;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.retail.posterminal.OBPOSAppPayment;

public class PaymentMethodsVerification extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    Boolean paymentMethodStatus = info.getStringParameter("inpisactive").equals("Y") ? true : false;
    String paymentTypeId = info.getStringParameter("inpobposAppPaymentTypeId");
    int noDifferentPayments = getNoDifferentPayments(paymentTypeId, paymentMethodStatus);
    if (noDifferentPayments > 0) {
      Map<String, String> map = new HashMap<String, String>();
      map.put("noPayments", Integer.toString(noDifferentPayments));
      info.addResult("WARNING", OBMessageUtils
          .parseTranslation(OBMessageUtils.messageBD("OBPOS_CALLOUT_PAYMENTMETHODS"), map));
    }

  }

  /*
   * Search for those payments with an active property different from the POS Terminal Type
   */
  private int getNoDifferentPayments(String paymentType, Boolean status) {
    OBQuery<OBPOSAppPayment> terminalPaymentQuery = OBDal.getInstance()
        .createQuery(OBPOSAppPayment.class,
            " as obap where obap.active = :status and obap.paymentMethod.id = :paymentType");
    terminalPaymentQuery.setFilterOnActive(false);
    terminalPaymentQuery.setNamedParameter("status", status ? false : true);
    terminalPaymentQuery.setNamedParameter("paymentType", paymentType);
    return terminalPaymentQuery.count();
  }

}
