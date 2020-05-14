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
import org.openbravo.erpCommon.utility.Utility;

public class ResetSimplifiedReturnInvoiceDocumentSequence extends SimpleCallout {

  @Override
  protected void execute(final CalloutInfo info) throws ServletException {
    info.addResult("WARNING",
        Utility.messageBD(this, "OBPOS_ResetDocumentSequence", info.vars.getLanguage()));
    info.addResult("inpsimpretinvlastassignednum", 0L);
  }
}
