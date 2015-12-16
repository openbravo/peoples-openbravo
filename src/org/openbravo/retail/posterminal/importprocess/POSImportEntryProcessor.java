/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.importprocess;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryPreProcessor;
import org.openbravo.service.importprocess.ImportProcessUtils;

/**
 * Sets the pos terminal in the {@link ImportEntry}.
 * 
 * @author mtaal
 *
 */
public class POSImportEntryProcessor extends ImportEntryPreProcessor {

  public void beforeCreate(ImportEntry importEntry) {
    try {
      if (POStypeofdata(importEntry)) {
        JSONObject jsonObject = new JSONObject(importEntry.getJsonInfo());

        // TODO: using 2 different ways of writing posTerminal is just not nice...
        String posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "posterminal");
        if (posTerminalId == null) {
          posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "posTerminal");
        }
        if (posTerminalId == null) {
          posTerminalId = ImportProcessUtils.getJSONProperty(jsonObject, "pos");
        }
        if (posTerminalId != null) {
          OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
              posTerminalId);
          importEntry.setOBPOSPOSTerminal(posTerminal);
          importEntry.setOrganization(posTerminal.getOrganization());
        }
      }
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

  private boolean POStypeofdata(ImportEntry importEntry) {
    List<String> posTypeOfData = Arrays.asList("Order", "BusinessPartner",
        "BusinessPartnerLocation", "OBPOS_App_Cashup", "FIN_Finacc_Transaction");
    return posTypeOfData.contains(importEntry.getTypeofdata());
  }
}
