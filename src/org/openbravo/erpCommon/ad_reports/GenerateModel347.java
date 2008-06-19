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
package org.openbravo.erpCommon.ad_reports;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.erpCommon.ad_combos.OrganizationComboData;

import org.openbravo.erpCommon.utility.DateTimeData;

public class GenerateModel347 extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strType = vars.getStringParameter("inpReportType", "New");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpDateTo");
      printPageDataSheet(response, vars, strType, strDateFrom, strDateTo);
    } else if (vars.commandIn("FIND")){
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strType = vars.getStringParameter("inpReportType");
      String strOrg = vars.getStringParameter("inpOrg", "0");
      String strComplementar = vars.getStringParameter("inpComplementar");
      printPageGenerate(response, vars, strDateFrom, strDateTo, strType, strComplementar, strOrg);
    }else pageError(response);
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strType, String strDateFrom, String strDateTo)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument=null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/GenerateModel347").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GenerateModel347", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString()); 

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.GenerateModel347");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "GenerateModel347.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ABCbPartner.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("GenerateModel347");
      vars.removeMessage("GenerateModel347");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("newType", strType);
    xmlDocument.setParameter("complementaryType", strType);
    xmlDocument.setParameter("sustitutiveType", strType);
    xmlDocument.setData("reportAD_ORGID", "liststructure", OrganizationComboData.selectCombo(this, vars.getRole()));
    out.println(xmlDocument.print());
    out.close();
  }


  void printPageGenerate(HttpServletResponse response, VariablesSecureApp vars, String strDateFrom, String strDateTo, String strType, String strComplementar, String strOrg) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: pageFind");
    GenerateModel347Data[] data = GenerateModel347Data.select(this, strType, strComplementar, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg));
    if (data.length>0)
    {
      response.setContentType("application/rtf");
      response.setHeader("Content-Disposition","attachment; filename=MODEL347.DAT" );
      PrintWriter out = response.getWriter();
      StringBuffer strBuf = new StringBuffer();

      String strLinea = "";
      String strCabecera = "";
      strCabecera = data[0].constant1 + data[0].model + data[0].ejercicio + data[0].nifDeclarante + data[0].nombreDeclarante + data[0].soporte + data[0].persona + data[0].numeroJustif + data[0].tipoDeclaracion + data[0].tipoDeclaracion2 + data[0].numeroDec + data[0].numeroPersonas + data[0].importe + data[0].totalInmuebles + data[0].importeTotalInmuebles + data[0].blancos;
      strBuf = strBuf.append(strCabecera);

      GenerateModel347Data[] dataLines = GenerateModel347Data.selectType2(this, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,"1"), Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg));

      for (int i=0; i<dataLines.length; i++){
        strLinea = dataLines[i].constant1 + dataLines[i].model + dataLines[i].ejercicio + dataLines[i].nifDeclarante + dataLines[i].nifDeclarado + dataLines[i].nifRepresentante + dataLines[i].nombreSocial + dataLines[i].tipoDeclaracion + dataLines[i].codigoProvincia + dataLines[i].codigoPais + dataLines[i].claveCodigo + dataLines[i].importe + dataLines[i].operacionSeguro + dataLines[i].arrendamiento + dataLines[i].blancos;
        strBuf = strBuf.append("\r\n").append(strLinea);
      }
      out.print(strBuf.toString());
      out.close();
    }
    
  }


  public String getServletInfo() {
    return "Servlet ReportInvoices. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}

