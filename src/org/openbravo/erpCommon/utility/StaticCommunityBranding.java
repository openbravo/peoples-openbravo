/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class StaticCommunityBranding extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strIsOPS = vars.getStringParameter("isOPS");
      String strVersion = vars.getStringParameter("version");
      printPage(response, strIsOPS, strVersion);
    } else
      pageError(response);

  }

  private void printPage(HttpServletResponse response, String strIsOPS, String strVersion)
      throws IOException {
    log4j.debug("Output: dataSheet");
    String strXmlTemplate = "";
    if (strVersion.startsWith("3")) {
      if ("Y".equals(strIsOPS)) {
        strXmlTemplate = "org/openbravo/erpCommon/utility/StaticCommunityBranding-3.0-OPS";
      } else {
        strXmlTemplate = "org/openbravo/erpCommon/utility/StaticCommunityBranding-3.0-Comm";
      }
    } else {
      if ("Y".equals(strIsOPS)) {
        strXmlTemplate = "org/openbravo/erpCommon/utility/StaticCommunityBranding-2.50-OPS";
      } else {
        strXmlTemplate = "org/openbravo/erpCommon/utility/StaticCommunityBranding-2.50-Comm";
      }
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(strXmlTemplate).createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
