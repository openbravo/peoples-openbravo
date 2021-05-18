/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.List;

public abstract class PaidReceiptsPaymentsTypeTerminalHook {

  public abstract void exec(List<Object[]> paymentTypes, String paymentId, String terminalId)
      throws Exception;
}
