/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2009 Openbravo SLU
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
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.xmlEngine.XmlDocument;

public class Register extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;
  private static final String URL = "http://www.openbravo.com/embedreg/form/";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/Home")
        .createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    // Getting system id
    SystemInformation sysInfo = OBDal.getInstance().get(SystemInformation.class, "0");

    if (sysInfo != null) {
      // Generating JavaScript code to open registration form
      String sysId = sysInfo.getSystemIdentifier() != null
          || sysInfo.getSystemIdentifier().equals("") ? sysInfo.getSystemIdentifier() : "";

      String formURL = URL;

      if (!sysId.equals("")) {
        // Append system identifier
        formURL += "?system_id=" + sysId;
      }

      String jsCode = "(function(){window.open('" + formURL + "');})();";

      // Setting jsCode
      xmlDocument.setParameter("customJS", jsCode);
    }
    LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "Home.html", strReplaceWith);
    xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  @Override
  public String getServletInfo() {
    return "Register servlet. Opens a new window with Openbravo's website registration form";
  }
}
