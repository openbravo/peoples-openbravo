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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.businessUtility;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.utils.FormatUtilities;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;



public class PrinterReports extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strDirectPrint = vars.getStringParameter("inpdirectprint", "N");
      String strPDFPath = vars.getStringParameter("inppdfpath");
      String strHiddenKey = vars.getStringParameter("inphiddenkey");
      String strHiddenValue = vars.getStringParameter("inphiddenvalue");
      printPage(response, vars, strDirectPrint, strPDFPath, strHiddenKey, strHiddenValue);
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDirectPrint, String strPDFPath, String strHiddenKey, String strHiddenValue) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    String[] discard = {"isPrintPreview"};
    if (strDirectPrint.equals("N")) discard[0] = new String("isDirectPrint");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/businessUtility/PrinterReports", discard).createXmlDocument();
    String mapping = "";
    if (strPDFPath.startsWith("..")) {
      strPDFPath = strPDFPath.substring(2);
      mapping = strPDFPath;
      strPDFPath = FormatUtilities.replace(PrinterReportsData.select(this, strPDFPath));
    } else mapping = PrinterReportsData.selectMapping(this, strPDFPath);

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("pdfPath", mapping);
    xmlDocument.setParameter("directPrint", strDirectPrint);
    //if (strPDFPath.startsWith("..")) strPDFPath = strPDFPath.substring(2);

    //String mapping = FormatUtilities.replace(PrinterReportsData.select(this, strPDFPath));
    strPDFPath = FormatUtilities.replace(strPDFPath);

    vars.setSessionValue(strPDFPath + "." + strHiddenKey, "(" + strHiddenValue + ")");
    if (!strHiddenValue.equals("")) vars.setSessionValue(strPDFPath + "." + strHiddenKey, "(" + strHiddenValue + ")");
    else vars.getRequestInGlobalVariable(strHiddenKey, strPDFPath + "." + strHiddenKey);

    //vars.getRequestInGlobalVariable(strHiddenKey + "_R", mapping + "." + strHiddenKey + "_R");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
