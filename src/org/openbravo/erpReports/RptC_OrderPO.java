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

import org.openbravo.base.secureApp.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class RptC_OrderPO extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcOrderId = vars.getSessionValue("RptC_OrderPO.inpcOrderId_R");
      if (strcOrderId.equals("")) strcOrderId = vars.getSessionValue("RptC_OrderPO.inpcOrderId");
      if (log4j.isDebugEnabled()) log4j.debug("+***********************: " + strcOrderId);
      printPagePartePDF(response, vars, strcOrderId);
    } else pageError(response);
  }


   void printPagePartePDF(HttpServletResponse response, VariablesSecureApp vars, String strcOrderId) throws IOException,ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: pdf");
    String strLanguage = vars.getLanguage();
    if (strBaseDesignPath.endsWith("/")) strDefaultDesignPath = strDefaultDesignPath.substring(0, strDefaultDesignPath.length()-1);
    log4j.info("*********************Base path: " + strBaseDesignPath);
    String strNewAddBase = strDefaultDesignPath;
    String strFinal = strBaseDesignPath;
    if (!strLanguage.equals("") && !strLanguage.equals("en_US")) strNewAddBase = strLanguage;
    if (!strFinal.endsWith("/" + strNewAddBase)) strFinal += "/" + strNewAddBase;
    log4j.info("*********************Base path: " + strFinal);
    String strBaseDesign = prefix + "/" + strFinal;
    
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    JasperReport jasperReportLines;
    try { 
      JasperDesign jasperDesignLines = JRXmlLoader.load(strBaseDesign+"/org/openbravo/erpReports/RptC_OrderPO_Lines.jrxml");
      jasperReportLines = JasperCompileManager.compileReport(jasperDesignLines);
    } catch (JRException e){
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    }
    parameters.put("SR_LINES", jasperReportLines);
    parameters.put("ORDER_ID", strcOrderId);
    renderJR(vars, response, null, "pdf", parameters, null, null);
  }

  public String getServletInfo() {
    return "Servlet that presents the RptCOrders seeker";
  } // End of getServletInfo() method
}
