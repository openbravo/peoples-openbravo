/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.CancelLayawayPaymentsHook;
import org.openbravo.model.common.order.Order;

@ApplicationScoped
public class CancelLayawayPaymentsHookExtension implements CancelLayawayPaymentsHook {

  /**
   * Hook to execute the handlePayments public method that creates the payments sent from the Web
   * POS during the Cancel Layaway process. These payments are added to the inverse order created
   * during the Cancel Layaway process.
   */
  @Override
  public void exec(JSONObject jsonorder, Order inverseOrder) throws Exception {
    OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
        jsonorder.getString("posTerminal"));
    inverseOrder.setObposApplications(posTerminal);
    OrderLoader orderLoader = WeldUtils.getInstanceFromStaticBeanManager(OrderLoader.class);
    orderLoader.initializeVariables(jsonorder);
    orderLoader.handlePayments(jsonorder, inverseOrder, null, false, false);
  }

}
