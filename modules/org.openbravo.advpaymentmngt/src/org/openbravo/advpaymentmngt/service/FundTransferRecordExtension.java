/*
 ************************************************************************************
 * Copyright (C) 2012-2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.advpaymentmngt.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.retail.posterminal.JSONProcessSimple;

public class FundTransferRecordExtension extends JSONProcessSimple {
  private static final Logger log = LogManager.getLogger();

  @Override
  public JSONObject exec(JSONObject jsonsent) {

    JSONObject result = new JSONObject();

    try {
      result.put("date", jsonsent.getString("date"));
      result.put("accountFrom", jsonsent.getString("accountFrom"));
      result.put("accountTo", jsonsent.getString("accountTo"));
      result.put("glItem", jsonsent.getString("glItem"));
      result.put("amount", jsonsent.getString("amount"));
      result.put("description", jsonsent.getString("description"));
    } catch (Exception e) {
      log.error("Unexpected exception creating the fund transfer record", e);
      throw new OBException("Unexpected exception creating the fund transfer record", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return isErrorJson(result) ? result : createSuccessResponse(jsonsent, result);

  }

  @Override
  public String getImportEntryId() {
    return null;
  }

  @Override
  public void setImportEntryId(String importEntryId) {
    // We don't want to create any import entry in these transactions.
  }

  @Override
  protected String getProperty() {
    return "GCNV_PaymentGiftCard";
  }
}
