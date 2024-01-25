/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.erpReports;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

public class RptM_RefInventoryPrintSeqLabel extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strHandlingUnitId = vars
          .getSessionValue("RptM_RefInventoryPrintSeqLabel.inpmRefinventoryId_R");
      if (strHandlingUnitId.equals("")) {
        strHandlingUnitId = vars
            .getSessionValue("RptM_RefInventoryPrintSeqLabel.inpmRefinventoryId");
      }
      printPageDataPDF(request, response, vars, strHandlingUnitId);
    }
  }

  private void printPageDataPDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strHandlingUnitId) throws IOException, ServletException {
    log4j.debug("Output: PDF report");
    final HashMap<String, Object> parameters = new HashMap<>(1);
    parameters.put("M_REFINVENTORY_ID", strHandlingUnitId);
    renderJR(vars, response, null, "pdf", parameters, null, null);
  }
}
