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
package org.openbravo.erpReports;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import java.io.*;
import java.util.HashMap;
import javax.servlet.*;
import javax.servlet.http.*;

public class RptC_Order extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcOrderId = vars.getSessionValue("RptC_Order.inpcOrderId_R");
      
      if (strcOrderId.equals("")) strcOrderId = vars.getSessionValue("RptC_Order.inpcOrderId");
      if (log4j.isDebugEnabled()) log4j.debug("strcOrderId: "+ strcOrderId);
      printPagePartePDF(response, vars, strcOrderId);
    } else pageError(response);
  }


   void printPagePartePDF(HttpServletResponse response, VariablesSecureApp vars, String strcOrderId) throws IOException,ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: RptC_Order - pdf");    
    RptCOrderHeaderData[] data = RptCOrderHeaderData.select(this, strcOrderId);
    if (log4j.isDebugEnabled()) log4j.debug("data: "+(data==null?"null":"not null"));
    if (data == null || data.length == 0) data = RptCOrderHeaderData.set();
    String strReportName = "@basedesign@/org/openbravo/erpReports/C_OrderJR.jrxml";
    response.setHeader("Content-disposition", "inline; filename=SalesOrderJR.pdf");
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    renderJR(vars, response, strReportName, "pdf", parameters, data, null );
    
  }

  public String getServletInfo() {
    return "Servlet that presents the RptCOrders seeker";
  } // End of getServletInfo() method
}
