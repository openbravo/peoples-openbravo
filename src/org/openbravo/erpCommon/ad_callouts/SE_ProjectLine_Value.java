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
package org.openbravo.erpCommon.ad_callouts;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.utils.FormatUtilities;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;



public class SE_ProjectLine_Value extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strmProductId = vars.getStringParameter("inpmProductId");
      String strTabId = vars.getStringParameter("inpTabId");
      try {
        printPage(response, vars, strmProductId, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strmProductId, String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SEProjectLineValueData[] data = null;
    if (strmProductId != null && !strmProductId.equals("")){
       data = SEProjectLineValueData.select(this, strmProductId);
    } else{
      data = SEProjectLineValueData.set();
    }
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_ProjectLine_Value';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpproductValue\", \"" + data[0].value + "\"),\n");
    resultado.append("new Array(\"inpproductName\", \"" + FormatUtilities.replaceJS(data[0].name) + "\"),\n");
    resultado.append("new Array(\"inpproductDescription\", \"" + FormatUtilities.replaceJS(data[0].description) + "\")");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "frameAplicacion");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
