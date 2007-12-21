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
 * All portions are Copyright (C) 2007 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.utility;

import org.openbravo.base.secureApp.VariablesSecureApp;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.data.Sqlc;
import org.openbravo.utils.Replace;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class PrintJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;	
  private JasperReport jasperReport;
  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    
    String strProcessId = vars.getRequiredStringParameter("inpadProcessId");
    String strOutputType = vars.getStringParameter("inpoutputtype", "html");
    if (!hasGeneralAccess(vars, "P", strProcessId)) {
      bdError(response, "AccessTableNoView", vars.getLanguage());
      return;
    }
    String strReportName = PrintJRData.getReportName(this, strProcessId);
    HashMap<String, Object> parameters = createParameters(vars, strProcessId);

    renderJR(vars, response, strReportName, strOutputType, parameters, null, null);
  }
  
  HashMap<String, Object> createParameters(VariablesSecureApp vars, String strProcessId) throws ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("JR: Get Parameters");
    String strParamname;
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    PrintJRData[] processparams = PrintJRData.getProcessParams(this, strProcessId);
    if (processparams != null && processparams.length>0) {
      String strReportName = PrintJRData.getReportName(this, strProcessId);
      String strAttach = strFTPDirectory + "/284-" +classInfo.id;
      String strLanguage = vars.getLanguage();
      if (strBaseDesignPath.endsWith("/")) strDefaultDesignPath = strDefaultDesignPath.substring(0, strDefaultDesignPath.length()-1);
      String strNewAddBase = strDefaultDesignPath;
      String strFinal = strBaseDesignPath;
      if (!strLanguage.equals("") && !strLanguage.equals("en_US")) strNewAddBase = strLanguage;
      if (!strFinal.endsWith("/" + strNewAddBase)) strFinal += "/" + strNewAddBase;
      String strBaseDesign = prefix + "/" + strFinal;
      
      strReportName = Replace.replace(Replace.replace(strReportName,"@basedesign@",strBaseDesign),"@attach@",strAttach);
       
      try {
        JasperDesign jasperDesign= JRXmlLoader.load(strReportName);
        jasperReport= JasperCompileManager.compileReport(jasperDesign);
      } catch (JRException e) {
        if (log4j.isDebugEnabled()) log4j.debug("JR: Error: " + e);
        e.printStackTrace();
        throw new ServletException(e.getMessage());
      } catch (Exception e) {
        throw new ServletException(e.getMessage());
      }
    
    }
    for (int i=0; i<processparams.length;i++) {
      strParamname = Sqlc.TransformaNombreColumna(processparams[i].paramname);
      if (log4j.isDebugEnabled()) log4j.debug("JR: -----parameter: " + strParamname + " " + vars.getStringParameter("inp"+strParamname));
      if (!vars.getStringParameter("inp"+strParamname).equals(""))
        parameters.put(processparams[i].paramname, formatParameter(vars, processparams[i].paramname, vars.getStringParameter("inp"+strParamname), processparams[i].reference));
    }
    return parameters;  
  }
  Object formatParameter(VariablesSecureApp vars, String strParamName, String strParamValue, String reference) throws ServletException{
    String strObjectClass = "";
    Object object ;
    JRParameter[] jrparams = jasperReport.getParameters();
    for (int i=0; i<jrparams.length;i++){
      if ( jrparams[i].getName().equals(strParamName)) strObjectClass = jrparams[i].getValueClassName();
    }
    if (log4j.isDebugEnabled()) log4j.debug("ClassType: " + strObjectClass);
    if (strObjectClass.equals("java.lang.String")) {
      object = new String(strParamValue);
    } else if (strObjectClass.equals("java.util.Date")){
      String strDateFormat;
      strDateFormat = vars.getJavaDateFormat();
      SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
      try {
        object = dateFormat.parse(strParamValue);
      } catch (Exception e) {
        throw new ServletException(e.getMessage());
      }
    } else {
      object = new String(strParamValue);
    }
    return object;
  }
  public String getServletInfo() {
    return "Servlet that generates the output of a JasperReports report.";
  } // end of getServletInfo() method
}
