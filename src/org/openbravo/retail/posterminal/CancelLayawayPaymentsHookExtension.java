/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.erpCommon.businessUtility.CancelLayawayPaymentsHook;
import org.openbravo.model.common.order.Order;

@ApplicationScoped
public class CancelLayawayPaymentsHookExtension implements CancelLayawayPaymentsHook {

  @Override
  public void exec(JSONObject jsonorder, Order inverseOrder) throws Exception {
    OrderLoader orderLoader = WeldUtils.getInstanceFromStaticBeanManager(OrderLoader.class);
    orderLoader.initializeVariables(jsonorder);
    orderLoader.handlePayments(jsonorder, inverseOrder, null, false);
  }

}
