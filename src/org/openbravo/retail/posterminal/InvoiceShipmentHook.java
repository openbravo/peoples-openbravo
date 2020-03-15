/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;

/**
 * Classes implementing this interface will be executed after creating invoices from shipments. They can modify
 * invoice parameter.
 * 
 */
public interface InvoiceShipmentHook {

  public void exec(JSONObject jsonorder, ShipmentInOut shipment, Invoice invoice)
      throws Exception;
}
