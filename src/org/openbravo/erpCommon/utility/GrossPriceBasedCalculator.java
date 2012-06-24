package org.openbravo.erpCommon.utility;

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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.openbravo.service.db.CallStoredProcedure;

public class GrossPriceBasedCalculator {
  public static BigDecimal calculateNetFromGross(String strTaxId, BigDecimal grossAmount,
      int pricePrecision, BigDecimal alternateAmount, BigDecimal orderedQty) {
    if (grossAmount.compareTo(BigDecimal.ZERO) == 0)
      return BigDecimal.ZERO;
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(strTaxId);
    parameters.add(grossAmount);
    // TODO: Alternate Base Amount
    parameters.add(alternateAmount);
    parameters.add(pricePrecision);
    parameters.add(orderedQty);

    final String procedureName = "C_GET_NET_PRICE_FROM_GROSS";
    final BigDecimal lineNetAmount = (BigDecimal) CallStoredProcedure.getInstance().call(
        procedureName, parameters, null);
    return lineNetAmount;
  }
}
