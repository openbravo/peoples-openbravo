/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.mobile.core.process.DataSynchronizationProcess.DataSynchronization;

@DataSynchronization(entity = "OBPOS_CancelLayaway")
public class CancelLayawayLoader extends OrderLoader {

  @Override
  public JSONObject saveRecord(JSONObject json) throws Exception {
    final JSONObject jsonResponse = super.saveRecord(json);
    return jsonResponse;
  }

  @Override
  protected String getImportQualifier() {
    return "OBPOS_CancelLayaway";
  }

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.cancelLayaway";
  }
}
