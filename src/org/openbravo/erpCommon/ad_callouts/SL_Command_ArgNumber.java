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
 * All portions are Copyright (C) 2001-2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Command_ArgNumber extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strCommand = vars.getStringParameter("inpatCommandId");

      try {
        printPage(response, vars, strCommand);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strCommand)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLCommandArgNumberData[] data = SLCommandArgNumberData.select(this, strCommand);
    String strArgNo = data[0].argno;
    String strHelp1 = data[0].help1;
    String strHelp2 = data[0].help2;
    String strHelp3 = data[0].help3;

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Command_ArgNumber';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpargno\", " + strArgNo + "),\n");
    resultado.append("new Array(\"inparghelp1\", \"" + FormatUtilities.replaceJS(strHelp1)
        + "\"),\n");
    resultado.append("new Array(\"inparghelp2\", \"" + FormatUtilities.replaceJS(strHelp2)
        + "\"),\n");
    resultado.append("new Array(\"inparghelp3\", \"" + FormatUtilities.replaceJS(strHelp3)
        + "\"),\n");
    resultado.append("new Array(\"EXECUTE\", \"displayLogic();\")\n");
    resultado.append(");");

    xmlDocument.setParameter("array", resultado.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
