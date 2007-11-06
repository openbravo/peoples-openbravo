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
package org.openbravo.erpCommon.info;

import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.utility.Utility;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.Replace;



public class Locator extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "Locator.name");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId", "Locator.mWarehouseId");
      vars.setSessionValue("Locator.warehousename", LocatorData.selectname(this, strWarehouse));
      if (!strNameValue.equals("")) vars.setSessionValue("Locator.name", strNameValue + "%");
      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "Locator.name");
      String strWarehouse = vars.getRequestGlobalVariable("inpmWarehouseId", "Locator.mWarehouseId");
      vars.setSessionValue("Locator.name", strKeyValue + "%");
      vars.setSessionValue("Locator.warehousename", LocatorData.selectname(this, strWarehouse));
      LocatorData[] data = LocatorData.selectKey(this, Utility.getContext(this, vars, "#User_Client", "Locator"), Utility.getContext(this, vars, "#User_Org", "Locator"), LocatorData.selectname(this, strWarehouse), strKeyValue + "%");
      if (data!=null && data.length==1) {
        printPageKey(response, vars, data);
      } else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strNameValue = vars.getGlobalVariable("inpName", "Locator.name", "");
      String strWarehousename = vars.getGlobalVariable("inpWarehouse", "Locator.warehousename", "");
      printPageFrame1(response, vars, strNameValue, strWarehousename);
    } else if (vars.commandIn("FRAME2")) {
      String strName = vars.getGlobalVariable("inpKey", "Locator.name", "");
      String strWarehousename = vars.getGlobalVariable("inpWarehouse", "Locator.warehousename", "");
      String strAisle = vars.getStringParameter("inpAisle");
      String strBin = vars.getStringParameter("inpBin");
      String strLevel = vars.getStringParameter("inpLevel");
      printPageFrame2(response, vars, strName, strWarehousename, strAisle, strBin, strLevel);
    } else if (vars.commandIn("FIND")) {
      String strName = vars.getGlobalVariable("inpKey", "Locator.name", "");
      String strWarehousename = vars.getGlobalVariable("inpWarehouse", "Locator.warehousename", "");
      String strAisle = vars.getStringParameter("inpAisle");
      String strBin = vars.getStringParameter("inpBin");
      String strLevel = vars.getStringParameter("inpLevel");

      vars.setSessionValue("Locator.initRecordNumber", "0");

      printPageFrame2(response, vars, strName, strWarehousename, strAisle, strBin, strLevel);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("Locator.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Locator");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("Locator.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("Locator.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("Locator.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Locator");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("Locator.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Locators seeker Frame");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Locator_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, LocatorData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Locators seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(LocatorData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    
    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].mLocatorId + "\";\n");
    html.append("var texto = \"" + Replace.replace(data[0].value, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strNameValue, String strWarehousename) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of Locators seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Locator_F1").createXmlDocument();
    if (strNameValue.equals("")) {
      xmlDocument.setParameter("key", "%");
    } else {
      xmlDocument.setParameter("key", strNameValue);
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("warehouse", strWarehousename);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strName, String strWarehousename, String strAisle, String strBin, String strLevel) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of Locators seeker");
    XmlDocument xmlDocument;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Locator");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("Locator.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strName.equals("") && strWarehousename.equals("") && strAisle.equals("") && strBin.equals("") && strLevel.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Locator_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", LocatorData.set());
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      LocatorData[] data = LocatorData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "Locator"), Utility.getContext(this, vars, "#User_Org", "Locator"), strName, strWarehousename,  strAisle, strBin, strLevel, initRecordNumber, intRecordRange);
      if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Locator_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", data);
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the Locators seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Locator_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the Locators seeker";
  } // end of getServletInfo() method
}
