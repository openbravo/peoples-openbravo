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
import org.openbravo.erpCommon.utility.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.math.BigDecimal;

public class SL_Requisition_Product extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled()) log4j.debug("CHANGED: " + strChanged);
      String strQty = vars.getStringParameter("inpqty");

      String strMProductID = vars.getStringParameter("inpmProductId");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strTabId = vars.getStringParameter("inpTabId");
      
      try {
        printPage(response, vars, strMProductID, strWindowId, strIsSOTrx, strTabId, strQty, strChanged);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strMProductID, String strWindowId, String strIsSOTrx, String strTabId, String strQty, String strChanged) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    
    String strPriceList = vars.getStringParameter("inpmProductId_PLIST");
    String strPriceStd = vars.getStringParameter("inpmProductId_PSTD");
    String strPriceLimit = vars.getStringParameter("inpmProductId_PLIM");

    if (strMProductID.equals("")) {
      strPriceList = strPriceLimit = strPriceStd = "";
    }
    if (!strChanged.equalsIgnoreCase("inpmProductId")) {
      strPriceList = vars.getStringParameter("inppricelist");
      strPriceStd = vars.getStringParameter("inppricestd");
      strPriceLimit = vars.getStringParameter("inppricelimit");
    }
    StringBuffer resultado = new StringBuffer();
    
    //Discount...
    if (strPriceList.startsWith("\"")) strPriceList = strPriceList.substring(1, strPriceList.length() - 1);
    if (strPriceStd.startsWith("\"")) strPriceStd = strPriceStd.substring(1, strPriceStd.length() - 1);
    BigDecimal priceStd = (strPriceStd.equals("")?new BigDecimal(0.0):new BigDecimal(strPriceStd));
    BigDecimal qty = (strQty.equals("")?new BigDecimal(0.0):new BigDecimal(strQty));
    BigDecimal lineNet = new BigDecimal(0.0);
    lineNet = new BigDecimal (priceStd.doubleValue() * qty.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);

    resultado.append("var calloutName='SL_Requisition_Product';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inppricelist\", " + (strPriceList.equals("")?"\"0\"":strPriceList) + "),");
    resultado.append("new Array(\"inppricelimit\", " + (strPriceLimit.equals("")?"\"0\"":strPriceLimit) + "),");
    resultado.append("new Array(\"inppricestd\", " + (strPriceStd.equals("")?"\"0\"":strPriceStd) + "),");
    resultado.append("new Array(\"inplinenetamt\", " + lineNet.toString() + "),");
    resultado.append("new Array(\"EXECUTE\", \"displayLogic();\"),\n");
    //To set the cursor focus in the amount field
    resultado.append("new Array(\"CURSOR_FIELD\", \"inpqty\")\n");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "frameAplicacion");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
