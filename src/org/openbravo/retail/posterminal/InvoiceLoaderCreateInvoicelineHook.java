/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.invoice.InvoiceLine;

/**
 * Classes implementing this interface will be executed before create invoice lines in InvoiceLoader
 * process. They can modify the jsoninvoice parameter.
 * 
 */
public interface InvoiceLoaderCreateInvoicelineHook {
  public void exec(JSONObject jsoninvoiceLine, InvoiceLine invoiceLine) throws Exception;
}
