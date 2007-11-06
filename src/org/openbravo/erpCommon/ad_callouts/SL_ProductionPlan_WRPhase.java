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
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;

public class SL_ProductionPlan_WRPhase extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  static final BigDecimal ZERO = new BigDecimal(0.0);

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }


  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled()) log4j.debug("CHANGED: " + strChanged);
        String strProduction = vars.getStringParameter("inpmProductionId");
        String strWRPhase = vars.getStringParameter("inpmaWrphaseId");
      try {
        printPage(response, vars, strProduction, strWRPhase);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strProduction, String strWRPhase) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLProductionPlanWRPhaseData[] data = SLProductionPlanWRPhaseData.select(this, strProduction, strWRPhase);
    if (data == null || data.length == 0) data = SLProductionPlanWRPhaseData.set();
    String strNeededQuantity = data[0].neededqty;

    String strOutsourced = SLProductionPlanWRPhaseData.selectOutsourced(this, strWRPhase);
    if (strOutsourced == null || strOutsourced.equals("")) strOutsourced = "N";
   

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_ProductionPlan_WRPhase';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpneededquantity\", \"" + strNeededQuantity + "\"),\n"); 
    resultado.append("new Array(\"inpsecondaryunit\", \"" + FormatUtilities.replaceJS(data[0].secondaryunit) + "\"),\n"); 
    resultado.append("new Array(\"inpconversionrate\", \"" + data[0].conversionrate + "\"),\n");
    resultado.append("new Array(\"inpmaCostcenterVersionId\", \"" + data[0].maCostcenterVersionId + "\"), \n");
    resultado.append("new Array(\"inpoutsourced\", \"" + strOutsourced + "\")\n");
    resultado.append(");\n");
    xmlDocument.setParameter("array", resultado.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
