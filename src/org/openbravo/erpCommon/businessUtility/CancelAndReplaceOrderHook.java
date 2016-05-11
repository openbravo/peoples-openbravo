/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.erpCommon.businessUtility;

import org.openbravo.model.common.order.Order;

public abstract class CancelAndReplaceOrderHook {

  public abstract void exec(Boolean triggersDisabled, Order oldOrder, Order newOrder)
      throws Exception;
}
