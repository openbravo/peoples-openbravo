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
import org.openbravo.erpCommon.utility.Utility;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;
import org.openbravo.utils.FormatUtilities;


public class SL_AlertRule_SQL extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strSQL = vars.getStringParameter("inpsql");
      try {
        printPage(response, vars, strSQL);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else pageError(response);
  }

  boolean existsColumn(ResultSetMetaData rmeta, String col) {
    try {
      for (int i=1; i<=rmeta.getColumnCount(); i++) {
        if (rmeta.getColumnName(i).equalsIgnoreCase(col)) return true;
      }
    } catch (Exception ex){}
    return false;
  }
  
  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strSQL) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    String msg="";
    
    if(!strSQL.equals("")) {
 
	    try {
	      PreparedStatement st = this.getPreparedStatement(strSQL);
	      ResultSet result;
	      result = st.executeQuery();
	      ResultSetMetaData rmeta=result.getMetaData();
	      if (!existsColumn(rmeta,"AD_CLIENT_ID"))    msg = "AD_CLIENT_ID ";
	      if (!existsColumn(rmeta,"AD_ORG_ID"))       msg += "AD_ORG_ID ";
	      if (!existsColumn(rmeta,"CREATED"))         msg += "CREATED ";
	      if (!existsColumn(rmeta,"CREATEDBY"))       msg += "CREATEDBY ";
	      if (!existsColumn(rmeta,"UPDATED"))         msg += "UPDATED ";
	      if (!existsColumn(rmeta,"UPDATEDBY"))       msg += "UPDATEDBY ";
	      if (!existsColumn(rmeta,"ISACTIVE"))        msg += "ISACTIVE ";
	      if (!existsColumn(rmeta,"AD_USER_ID"))      msg += "AD_USER_ID ";
	      if (!existsColumn(rmeta,"AD_ROLE_ID"))      msg += "AD_ROLE_ID ";
	      if (!existsColumn(rmeta,"RECORD_ID"))       msg += "RECORD_ID ";
	      if (!existsColumn(rmeta,"DESCRIPTION"))     msg += "DESCRIPTION ";
	      if (!existsColumn(rmeta,"REFERENCEKEY_ID")) msg += "REFERENCEKEY_ID";
	      if (!msg.equals("")) msg = Utility.messageBD(this,"notColumnInQuery",vars.getLanguage()) + msg;
	    } catch (Exception ex) {
	      msg = "error in query: " + FormatUtilities.replaceJS(ex.toString());
	    }
    }
    
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_AlertRule_SQL';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"MESSAGE\", \"" + msg + "\")");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
