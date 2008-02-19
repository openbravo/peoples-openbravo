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
import org.openbravo.erpCommon.utility.ComboTableData;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class ProductMultiple extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "ProductMultiple.name");
      vars.getRequestGlobalVariable("inpProductCategory", "ProductMultiple.productCategory");
      if (!strNameValue.equals("")) vars.setSessionValue("ProductMultiple.name", strNameValue + "%");
      printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strKeyValue = vars.getGlobalVariable("inpKey", "ProductMultiple.key", "");
      String strNameValue = vars.getGlobalVariable("inpName", "ProductMultiple.name", "");
      printPageFrame1(response, vars, strKeyValue, strNameValue);
    } else if (vars.commandIn("FRAME2")) {
      String strKey = vars.getGlobalVariable("inpKey", "ProductMultiple.key", "");
      String strName = vars.getGlobalVariable("inpName", "ProductMultiple.name", "");
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory", "ProductMultiple.productCategory");
      printPageFrame2(response, vars, strKey, strName, strProductCategory);
    } else if (vars.commandIn("FIND")) {
      String strKey = vars.getRequestGlobalVariable("inpKey", "ProductMultiple.key");
      String strName = vars.getRequestGlobalVariable("inpName", "ProductMultiple.name");
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory", "ProductMultiple.productCategory");
      vars.setSessionValue("ProductMultiple.initRecordNumber", "0");

      printPageFrame2(response, vars, strKey, strName, strProductCategory);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("ProductMultiple.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ProductMultiple");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("ProductMultiple.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("ProductMultiple.initRecordNumber", strInitRecord);
      }

      request.getRequestDispatcher(request.getServletPath() + "?Command=FRAME2").forward(request, response);
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("ProductMultiple.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ProductMultiple");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("ProductMultiple.initRecordNumber", strInitRecord);

      request.getRequestDispatcher(request.getServletPath() + "?Command=FRAME2").forward(request, response);
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Multiple products seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductMultiple_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }


  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strKeyValue, String strNameValue) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the multiple products seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductMultiple_F1").createXmlDocument();
    if (strKeyValue.equals("") && strNameValue.equals("")) {
      xmlDocument.setParameter("key", "%");
    } else {
      xmlDocument.setParameter("key", strKeyValue);
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("name", strNameValue);
	try {
		ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Product_Category_ID", "", "", Utility.getContext(this, vars, "#User_Org", "ProductMultiple"), Utility.getContext(this, vars, "#User_Client", "ProductMultiple"), 0);
		Utility.fillSQLParameters(this, vars, null, comboTableData, "ProductMultiple", "");
		xmlDocument.setData("reportM_Product_Category_ID","liststructure", comboTableData.select(false));
		comboTableData = null;
	} catch (Exception ex) {
		throw new ServletException(ex);
	}

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strKey, String strName, String strProductCategory) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the multuple images seeker");
    XmlDocument xmlDocument;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ProductMultiple");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ProductMultiple.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strKey.equals("") && strName.equals("") && strProductCategory.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductMultiple_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", BusinessPartnerData.set());
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      ProductMultipleData[] data = ProductMultipleData.select(this, strKey, strName, strProductCategory, Utility.getContext(this, vars, "#User_Client", "ProductMultiple"), Utility.getContext(this, vars, "#User_Org", "ProductMultiple"), initRecordNumber, intRecordRange);
      if (data==null || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductMultiple_F2", discard).createXmlDocument();
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
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the multiple products seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ProductMultiple_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the multiple products seeker";
  } // end of getServletInfo() method
}
