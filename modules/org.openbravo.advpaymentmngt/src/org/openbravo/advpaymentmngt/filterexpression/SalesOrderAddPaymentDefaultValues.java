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

import java.util.Map;

import org.openbravo.advpaymentmngt.utility.APRMConstants;
import org.openbravo.client.kernel.ComponentProvider;

@ComponentProvider.Qualifier(APRMConstants.SALES_ORDER_WINDOW_ID)
public class SalesOrderAddPaymentDefaultValues extends AddPaymentDefaultValuesHandler {

  @Override
  public String getDefaultExpectedAmount(Map<String, String> requestMap) {
    // Expected amount is the amount pending to pay on the Sales Order
    return null;
  }

  @Override
  String getDefaultActualPaymentAmount(Map<String, String> requestMap) {
    // TODO Auto-generated method stub
    return null;
  }

}
