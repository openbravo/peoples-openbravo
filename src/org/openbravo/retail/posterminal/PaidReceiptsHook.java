/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;

public interface PaidReceiptsHook {

  /**
   * Executes the hook logic on the Paid Receipts process. If property obposExtendProductModel is
   * returned in a line of paidReceipt object it will be injected in product model in the frontend
   */
  public abstract void exec(final String orderId, final JSONObject paidReceipt) throws Exception;

  /**
   * @return an integer representing the priority of this Java component. The one with the lowest
   *         priority will be processed first.
   */
  public default int getPriority() {
    return 100;
  }

}
