/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.ad_callouts;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;

/**
 * Callout used to uncheck the Count Payment in Cash Up when payment method is rounding to properly
 * hide this payment method in the Cashup process
 *
 * @author collazoandy4
 *
 */
public class PaymentRoundingCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String rounding = info.getStringParameter("inpisrounding");
    if (StringUtils.equals(rounding, "Y")) {
      info.addResult("inpcountpaymentincashup", false);
      info.addResult("MESSAGE", OBMessageUtils.messageBD("OBPOS_TouchpointTypeRoundingChecked"));
    }
  }
}
