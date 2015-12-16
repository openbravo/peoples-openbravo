/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.service.json.JsonConstants;

public class CheckTerminalAuth extends JSONProcessSimple {

  HashMap<String, DocumentType> paymentDocTypes = new HashMap<String, DocumentType>();
  String paymentDescription = null;
  private static final Logger log = Logger.getLogger(CheckTerminalAuth.class);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    String value = new String();
    JSONObject respArray = new JSONObject();
    JSONObject result = new JSONObject();
    result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    try {
      String terminalName = jsonsent.getString("terminalName");
      String terminalKeyIdentifier = jsonsent.getString("terminalKeyIdentifier");
      String terminalAuthentication = jsonsent.getString("terminalAuthentication");

      OBCriteria<OBPOSApplications> qApp = OBDal.getInstance().createCriteria(
          OBPOSApplications.class);
      qApp.add(Restrictions.eq(OBPOSApplications.PROPERTY_SEARCHKEY, terminalName));
      List<OBPOSApplications> apps = qApp.list();
      if (apps.size() == 1) {
        OBPOSApplications terminal = ((OBPOSApplications) apps.get(0));
        if (!terminal.isLinked() || !terminal.getTerminalKey().equals(terminalKeyIdentifier)) {
          respArray.put("isLinked", false);
        }
        try {
          value = Preferences.getPreferenceValue("OBPOS_TerminalAuthentication", true, null, null,
              null, null, (String) null);
        } catch (PropertyException e) {
          if (!terminalAuthentication.equals("N")) {
            respArray.put("terminalAuthentication", terminalAuthentication);
            result.put(JsonConstants.RESPONSE_DATA, respArray);
            return result;
          }
          if (!terminalAuthentication.equals(value)) {
            respArray.put("terminalAuthentication", terminalAuthentication);
            result.put(JsonConstants.RESPONSE_DATA, respArray);
            return result;
          }
        }
      }
    } catch (Exception e) {
      log.error("There was an error Checking Terminal Authentication: ", e);
    }
    result.put(JsonConstants.RESPONSE_DATA, respArray);
    return result;
  }

  @Override
  protected String getProperty() {
    return "OBPOS_receipt.voidLayaway";
  }
}
