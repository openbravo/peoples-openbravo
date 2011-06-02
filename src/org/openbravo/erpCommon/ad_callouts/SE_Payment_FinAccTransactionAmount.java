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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  Enterprise Intelligence Systems (http://www.eintel.com.au).
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.util.Convert;
import org.openbravo.erpCommon.dao.CurrencyDao;

/**
 * Update exchange rate
 * 
 * @author eintelau (ben.sommerville@eintel.com.au)
 */
public class SE_Payment_FinAccTransactionAmount extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String strFinaccTxnAmount = vars.getStringParameter("inpfinaccTxnAmount");
    BigDecimal finaccTxnAmount = Convert.toBigDecimal(strFinaccTxnAmount);

    String strAmount = vars.getNumericParameter("inpamount");
    BigDecimal amount = Convert.toAmount(strAmount);

    if (finaccTxnAmount != null && amount != null && amount.compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal convertRate = finaccTxnAmount.divide(amount, MathContext.DECIMAL32);
      String strConvertRate = Convert.toStringWithPrecision(convertRate,
          CurrencyDao.CONVERSION_RATE_PRECISION);
      info.addResult("inpfinaccTxnConvertRate", strConvertRate);
    }

  }

}
