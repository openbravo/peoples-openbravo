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
package org.openbravo.erpCommon.utility;

import org.openbravo.base.secureApp.*;
import org.openbravo.data.Sqlc;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.utils.FormatUtilities;

import java.util.Vector;

public class UsedByLink extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }
  
  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strKeyColumn = vars.getRequiredStringParameter("inpkeyColumnId");
      String strTableId = vars.getRequiredStringParameter("inpTableId");
      String strKeyId = vars.getRequiredStringParameter("inp" + Sqlc.TransformaNombreColumna(strKeyColumn));
      printPage(response, vars, strWindow, strTabId, strKeyColumn, strKeyId, strTableId);
    } else if (vars.commandIn("LINKS")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTabId = vars.getRequiredStringParameter("inpTabId");
      String strKeyColumn = vars.getRequiredStringParameter("inpkeyColumnId");
      String strKeyId = vars.getRequiredStringParameter("inp" + Sqlc.TransformaNombreColumna(strKeyColumn));
      String strAD_TAB_ID = vars.getRequiredStringParameter("inpadTabIdKey");
      String strTABLENAME = vars.getRequiredStringParameter("inptablename");
      String strCOLUMNNAME = vars.getRequiredStringParameter("inpcolumnname");
      String strTableId = vars.getRequiredStringParameter("inpTableId");
      printPageDetail(response, vars, strWindow, strTabId, strKeyColumn, strKeyId, strAD_TAB_ID, strTABLENAME, strCOLUMNNAME, strTableId);
    } else throw new ServletException();
  }


  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strWindow, String TabId, String keyColumn, String keyId, String tableId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: UsedBy links for tab: " + TabId);
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/UsedByLink").createXmlDocument();
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("tabID", TabId);
    xmlDocument.setParameter("windowID", strWindow);
    xmlDocument.setParameter("keyColumn", keyColumn);
    xmlDocument.setParameter("tableId", tableId);
    xmlDocument.setParameter("keyName", "inp" + Sqlc.TransformaNombreColumna(keyColumn));
    xmlDocument.setParameter("keyId", keyId);
    xmlDocument.setParameter("recordIdentifier", UsedByLinkData.selectIdentifier(this, keyId, vars.getLanguage(), tableId));

    UsedByLinkData[] data = null;

    if (vars.getLanguage().equals("en_US")) data = UsedByLinkData.select(this, vars.getClient(), vars.getLanguage(), keyColumn);
    else data = UsedByLinkData.selectLanguage(this, vars.getClient(), vars.getLanguage(), keyColumn);

    if (data!=null && data.length>0) {
      Vector<Object> vecTotal = new Vector<Object>();
      for (int i=0;i<data.length;i++) {
        if (log4j.isDebugEnabled()) log4j.debug("***Referenced tab: " + data[i].adTabId);
        UsedByLinkData[] dataRef = UsedByLinkData.windowRef(this, data[i].adTabId);
        if (dataRef==null || dataRef.length==0) continue;
        String strWhereClause = getWhereClause(vars, strWindow, dataRef[0].whereclause);
        if (log4j.isDebugEnabled()) log4j.debug("***   Referenced where clause (1): " + strWhereClause);
        strWhereClause += getAditionalWhereClause(vars, strWindow, data[i].adTabId, data[i].tablename, keyColumn, data[i].columnname, UsedByLinkData.getTabTableName(this, tableId));
        if (log4j.isDebugEnabled()) log4j.debug("***   Referenced where clause (2): " + strWhereClause);
        strWhereClause += " AND AD_ORG_ID IN (" + vars.getUserOrg() + ") AND AD_CLIENT_ID IN (" + vars.getUserClient() + ")";
        int total = Integer.valueOf(UsedByLinkData.countLinks(this, data[i].tablename, data[i].columnname, keyId, strWhereClause)).intValue();
        if (log4j.isDebugEnabled()) log4j.debug("***   Count: " + total);
        data[i].total = Integer.toString(total);
        if (total>0) {
          vecTotal.addElement(data[i]);
        }
      }
      data = new UsedByLinkData[vecTotal.size()];
      vecTotal.copyInto(data);
    }

    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageDetail(HttpServletResponse response, VariablesSecureApp vars, String strWindow, String TabId, String keyColumn, String keyId, String strAD_TAB_ID, String strTABLENAME, String strCOLUMNNAME, String adTableId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: UsedBy links for tab: " + TabId);
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/UsedByLink_Detail").createXmlDocument();
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("tabID", TabId);
    xmlDocument.setParameter("windowID", strWindow);
    xmlDocument.setParameter("keyColumn", keyColumn);
    xmlDocument.setParameter("keyName", "inp" + Sqlc.TransformaNombreColumna(keyColumn));
    xmlDocument.setParameter("keyId", keyId);
    xmlDocument.setParameter("adTabId", strAD_TAB_ID);
    xmlDocument.setParameter("tableName", strTABLENAME);
    xmlDocument.setParameter("columnName", strCOLUMNNAME);
    xmlDocument.setParameter("tableId", adTableId);
    xmlDocument.setParameter("recordIdentifier", UsedByLinkData.selectIdentifier(this, keyId, vars.getLanguage(), adTableId));
    if (vars.getLanguage().equals("en_US")) {
      xmlDocument.setParameter("paramName", UsedByLinkData.tabName(this, strAD_TAB_ID));
    } else {
      xmlDocument.setParameter("paramName", UsedByLinkData.tabNameLanguage(this, vars.getLanguage(), strAD_TAB_ID));
    }

    UsedByLinkData[] data = UsedByLinkData.keyColumns(this, strAD_TAB_ID);
    if (data==null || data.length==0) {
      bdError(response, "RecordError", vars.getLanguage());
      return;
    }
    StringBuffer strScript = new StringBuffer();
    StringBuffer strHiddens = new StringBuffer();
    StringBuffer strSQL = new StringBuffer();
    strScript.append("function windowSelect() {\n");
    strScript.append("var frm = document.forms[0];\n");
    for (int i=0;i<data.length;i++) {
      if (i>0) {
        strSQL.append(" || ', ' || ");
      }
      strScript.append("frm.inp").append(Sqlc.TransformaNombreColumna(data[i].name)).append(".value = arguments[").append(i).append("];\n");
      strSQL.append("'''' || ").append(data[i].name).append(" || ''''");
      strHiddens.append("<input type=\"hidden\" name=\"inp").append(Sqlc.TransformaNombreColumna(data[i].name)).append("\">\n");
    }
    UsedByLinkData[] dataRef = UsedByLinkData.windowRef(this, strAD_TAB_ID);
    if (dataRef==null || dataRef.length==0) {
      bdError(response, "RecordError", vars.getLanguage());
      return;
    }
    String windowRef= FormatUtilities.replace(dataRef[0].windowname) + "/" + FormatUtilities.replace(dataRef[0].tabname) + "_Edition.html";
    strScript.append("top.opener.submitFormGetParams('DIRECT', '../").append(windowRef).append("', getParamsScript(document.forms[0]));\n");
    strScript.append("top.close();\n");
    strScript.append("return true;\n");
    strScript.append("}\n");

    xmlDocument.setParameter("hiddens", strHiddens.toString());
    xmlDocument.setParameter("script", strScript.toString());

    xmlDocument.setData("structure1", UsedByLinkData.selectLinks(this, strSQL.toString(), strTABLENAME, data[0].name, vars.getLanguage(), strCOLUMNNAME, keyId, (getWhereClause(vars, strWindow, dataRef[0].whereclause) + " AND AD_ORG_ID IN (" + vars.getUserOrg() + ") AND AD_CLIENT_ID IN (" + vars.getUserClient() + ")")));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getWhereClause(VariablesSecureApp vars, String window, String strWhereClause) throws ServletException {
    String strWhere = strWhereClause;
    if (strWhere.equals("") || strWhere.indexOf("@")==-1) return ((strWhere.equals("")?"":" AND ") + strWhere);
    if (log4j.isDebugEnabled()) log4j.debug("WHERE CLAUSE: " + strWhere);
    StringBuffer where = new StringBuffer();
    String token="", fin="";
    int i=0;
    i = strWhere.indexOf("@");
    while (i!=-1) {
      where.append(strWhere.substring(0,i));
      if (log4j.isDebugEnabled()) log4j.debug("WHERE ACTUAL: " + where.toString());
      strWhere = strWhere.substring(i+1);
      if (log4j.isDebugEnabled()) log4j.debug("WHERE COMPARATION: " + strWhere);
      if (strWhere.startsWith("SQL")) {
        fin += ")";
        strWhere.substring(4);
        where.append("(");
      } else {
        i = strWhere.indexOf("@");
        if (i==-1) {
          log4j.error("Unable to parse the following string: " + strWhereClause + "\nNow parsing: " + where.toString());
          throw new ServletException("Unable zo parse the following string: " + strWhereClause + "\nNow parsing: " + where.toString());
        }
        token = strWhere.substring(0, i);
        strWhere = (i==strWhere.length())?"":strWhere.substring(i+1);
        if (log4j.isDebugEnabled()) log4j.debug("TOKEN: " + token);
        String tokenResult = Utility.getContext(this, vars, token, window);
        if (log4j.isDebugEnabled()) log4j.debug("TOKEN PARSED: " + tokenResult);
        if (tokenResult.equalsIgnoreCase(token)) {
          log4j.error("Unable to parse the String " + strWhereClause + "\nNow parsing: " + where.toString());
          throw new ServletException("Unable to parse the string: " + strWhereClause + "\nNow parsing: " + where.toString());
        }
        where.append(tokenResult);
      }
      i = strWhere.indexOf("@");
    };
    where.append(strWhere);
    return " AND " + where.toString();
  }

  public String getAditionalWhereClause(VariablesSecureApp vars, String strWindow, String adTabId, String tableName, String keyColumn, String columnName, String parentTableName) throws ServletException {
    String result = "";
    if (log4j.isDebugEnabled()) log4j.debug("getAditionalWhereClause - ad_Tab_ID: " + adTabId);
    UsedByLinkData[] data = UsedByLinkData.parentTabTableName(this, adTabId);
    if (data!=null && data.length>0) {
      if (log4j.isDebugEnabled()) log4j.debug("getAditionalWhereClause - parent tab: " + data[0].adTabId);
      UsedByLinkData[] dataColumn = UsedByLinkData.parentsColumnName(this, adTabId, data[0].adTabId);
      if (dataColumn==null || dataColumn.length==0) {
        if (log4j.isDebugEnabled()) log4j.debug("getAditionalWhereClause - searching parent Columns Real");
        dataColumn = UsedByLinkData.parentsColumnReal(this, adTabId, data[0].adTabId);
      }
      if (dataColumn==null || dataColumn.length==0) {
        if (log4j.isDebugEnabled()) log4j.debug("getAditionalWhereClause - no parent columns found");
        return result;
      }
      result += " AND EXISTS (SELECT 1 FROM " + data[0].tablename + " WHERE " + data[0].tablename + "." + ((!dataColumn[0].name.equals(""))?dataColumn[0].name:keyColumn) + " = " + tableName + "." + ((!dataColumn[0].name.equals(""))?dataColumn[0].name:columnName);
      UsedByLinkData[] dataRef = UsedByLinkData.windowRef(this, data[0].adTabId);
      String strAux = "";
      if (dataRef!=null && dataRef.length>0) strAux = getWhereClause(vars, strWindow, dataRef[0].whereclause);
      result += strAux;
      if (!data[0].tablename.equalsIgnoreCase(parentTableName)) {
        result += getAditionalWhereClause(vars, strWindow, data[0].adTabId, data[0].tablename, "", "", parentTableName);
      }
      result += ")";
    }
    if (log4j.isDebugEnabled()) log4j.debug("getAditionalWhereClause - result: " + result);
    return result;
  }

  public String getServletInfo() {
    return "Servlet that presents the usedBy links";
  } // end of getServletInfo() method
}
