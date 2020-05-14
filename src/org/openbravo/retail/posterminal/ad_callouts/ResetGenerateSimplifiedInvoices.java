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

public class ResetGenerateSimplifiedInvoices extends SimpleCallout {

  @Override
  protected void execute(final CalloutInfo info) throws ServletException {
    final boolean isGenerateSimplifiedInvoicesActive = info.getStringParameter("inpgenerateinvoice")
        .equals("Y");
    info.addResult("inpgroupingorders", !isGenerateSimplifiedInvoicesActive);
    info.addResult("inpseparateinvoiceforreturns",
        false);
  }
}
