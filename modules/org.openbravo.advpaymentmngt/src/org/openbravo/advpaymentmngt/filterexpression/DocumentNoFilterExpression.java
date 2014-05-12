/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.advpaymentmngt.filterexpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;

public class DocumentNoFilterExpression implements FilterExpression {

  private static final Logger log = Logger.getLogger(DocumentNoFilterExpression.class);

  @Override
  public String getExpression(Map<String, String> requestMap) {
    try {
      OBContext.setAdminMode(true);
      ConnectionProvider conn = new DalConnectionProvider();
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      JSONObject context = new JSONObject((String) requestMap.get("context"));

      // get DocumentNo
      final List<Object> parameters = new ArrayList<Object>();
      parameters.add(vars.getClient());
      parameters.add(context.get("inpadOrgId"));
      parameters.add("Y".equals(context.get("inpissotrx")) ? "ARR" : "APP");
      String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
          parameters, null);

      // Last parameter false to not to update database yet, in action handler should be true
      String strDocNo = Utility.getDocumentNo(conn, vars, "AddPaymentFromInvoice", "FIN_Payment",
          strDocTypeId, strDocTypeId, false, false);

      return "<" + strDocNo + ">";
    } catch (JSONException e) {
      log.error(
          "Error trying to get Payment DocumentNo on DocumentNoFilterExpression class: "
              + e.getMessage(), e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }

  }
}
