/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class ResetGenerateAggregatedInvoices extends SimpleCallout {

  @Override
  protected void execute(final CalloutInfo info) throws ServletException {
    final boolean isGenerateAggregatedInvoicesActive = info.getStringParameter("inpgroupingorders")
        .equals("Y");
    info.addResult("inpgenerateinvoice", !isGenerateAggregatedInvoicesActive);
  }
}
