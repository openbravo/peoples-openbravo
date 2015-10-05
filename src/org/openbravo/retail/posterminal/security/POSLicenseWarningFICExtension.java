/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.security;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.application.window.FICExtension;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes warning message to be displayed if number of terminals has exceeded the warning
 * threshold
 *
 */
@ApplicationScoped
public class POSLicenseWarningFICExtension implements FICExtension {
  private static final Logger log = LoggerFactory.getLogger(POSLicenseRestrictions.class);

  @Inject
  private POSLicenseRestrictions restrictions;

  @Override
  public void execute(String mode, Tab tab, Map<String, JSONObject> columnValues, BaseOBObject row,
      List<String> changeEventCols, List<JSONObject> calloutMessages, List<JSONObject> attachments,
      List<String> jsExcuteCode, Map<String, Object> hiddenInputs, int noteCount,
      List<String> overwrittenAuxiliaryInputs) {
    if (!isValidEvent(mode, tab)) {
      return;
    }
    restrictions.resetNumberOfTerminals();
    try {
      JSONObject msg = new JSONObject();
      msg.put(
          "text",
          restrictions.getPOSTerminalsWarningMsg(ActivationKey.getInstance(), OBContext
              .getOBContext().getLanguage().getLanguage()));
      msg.put("severity", "TYPE_WARNING");
      calloutMessages.add(msg);
    } catch (Exception e) {
      log.error("Error when generating warn message for POS Terminals", e);
    }
  }

  private boolean isValidEvent(String mode, Tab tab) {
    return ModelProvider.getInstance().getEntityByTableId((String) DalUtil.getId(tab.getTable()))
        .getMappingClass() == OBPOSApplications.class
        && (mode.equals("EDIT") || mode.equals("NEW"));
  }
}
