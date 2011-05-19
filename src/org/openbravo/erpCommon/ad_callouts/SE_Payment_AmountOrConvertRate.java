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

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.util.Convert;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;

import javax.servlet.ServletException;
import java.math.BigDecimal;

/**
 * Update converted amount
 *
 * @author eintelau (ben.sommerville@eintel.com.au)
 */
public class SE_Payment_AmountOrConvertRate extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String strConvertRate = vars.getStringParameter("inpfinaccTxnConvertRate");
    BigDecimal convertRate = Convert.toBigDecimal(strConvertRate);

    String strAmount = vars.getNumericParameter("inpamount");
    BigDecimal amount = Convert.toAmount(strAmount);

    String financialAccountId = vars.getStringParameter("inpfinFinancialAccountId");
    final OBDal dal = OBDal.getInstance();
    FIN_FinancialAccount financialAccount= dal.get(FIN_FinancialAccount.class, financialAccountId);

    String finAccTxnAmount = "";
    if( convertRate != null && amount != null) {
      BigDecimal converted = amount.multiply(convertRate);
      finAccTxnAmount = Convert.toStringWithPrecision(converted,
          financialAccount.getCurrency().getStandardPrecision());
      info.addResult("inpfinaccTxnAmount",finAccTxnAmount);
    }

  }
}
