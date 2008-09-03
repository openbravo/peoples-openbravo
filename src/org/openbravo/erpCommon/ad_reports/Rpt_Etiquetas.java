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
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.utils.CommPortsOpen;

public class Rpt_Etiquetas extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      vars.getGlobalVariable("inpwindowId", "Rpt_Etiquetas|windowID","");
      String strcBpartnerId = vars.getGlobalVariable("inpcBpartnerId", "Rpt_Etiquetas|cBpartnerId", "");
      String strmProductId = vars.getGlobalVariable("inpmProductId", "Rpt_Etiquetas|mProductId", "");
      String strmAttributesetinstanceId = vars.getGlobalVariable("inpmAttributesetinstanceId", "Rpt_Etiquetas|mAttributesetinstanceId", "");
      String strqty = vars.getGlobalVariable("inpqty", "Rpt_Etiquetas|qty", "");
      printPage(response, vars, strcBpartnerId, strmProductId, strmAttributesetinstanceId, strqty);
    } else if (vars.commandIn("PRINT_LABEL")) {
      String strWindow = vars.getGlobalVariable("inpwindowId", "Rpt_Etiquetas|windowID","");
      String strBpartner = vars.getRequestGlobalVariable("inpBpartnerId", "Rpt_Etiquetas|cBpartnerId");
      String strProduct = vars.getRequestGlobalVariable("inpmProductId", "Rpt_Etiquetas|mProductId");
      String strAttribute = vars.getRequestGlobalVariable("inpmAttributesetinstanceId", "Rpt_Etiquetas|mAttributesetinstanceId");
      String strPageNumber = vars.getRequestGlobalVariable("inpNumberPages", "Rpt_Etiquetas|qty");
      String label = compoundLabel(vars, strBpartner, strProduct, strAttribute, strPageNumber);
      try {
        CommPortsOpen comm = new CommPortsOpen();
        comm.transmit(label);
        comm.close();
        if (strWindow.equals("")) bdError(response, "ProcessOK", vars.getLanguage());
        else printPageClosePopUpWindow(response, vars);
      } catch(Exception e) {
        log4j.error(e);
        advise(response, "Error", e.toString());
      }
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strcBpartnerId, String strmProductId, String strmAttributesetinstanceId, String strqty) throws IOException,ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/Rpt_Etiquetas_Config").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("product", strmProductId);
    xmlDocument.setParameter("productName", RptEtiquetasData.selectProduct(this, strmProductId));
    xmlDocument.setParameter("bpartner", strcBpartnerId);
    xmlDocument.setParameter("bpartnerName", RptEtiquetasData.selectBPartner(this, strcBpartnerId));
    xmlDocument.setParameter("attribute", strmAttributesetinstanceId);
    xmlDocument.setParameter("attributeName", RptEtiquetasData.selectAttribute(this, strmAttributesetinstanceId));
    xmlDocument.setParameter("numberPages", strqty);

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "RptEtiquetas", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());  
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.Rpt_Etiquetas");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "Rpt_Etiquetas_Config.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "Rpt_Etiquetas_Config.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("Rpt_Etiquetas");
      vars.removeMessage("Rpt_Etiquetas");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    out.println(xmlDocument.print());
    out.close();
  }

  String compoundLabel(VariablesSecureApp vars, String strBpartner, String strProduct, String strAttribute, String strPageNumber) throws IOException,ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/Rpt_Etiquetas").createXmlDocument();
    RptEtiquetasData[] data = RptEtiquetasData.select(this, strBpartner, strProduct);

    xmlDocument.setParameter("bPartnerName", RptEtiquetasData.selectBPartner(this, strBpartner));
    if (data!=null && data.length>0) {
      xmlDocument.setParameter("productName", data[0].name);
      xmlDocument.setParameter("productDescription", data[0].description);
      xmlDocument.setParameter("productEAN", data[0].upc);
    }
    RptEtiquetasData[] data2 = RptEtiquetasData.selectAttributeComplete(this, strAttribute);
    if (data2!=null && data2.length>0) {
      xmlDocument.setParameter("productAttribute", data2[0].lot);
      xmlDocument.setParameter("productAttributeCad", data2[0].guaranteedate);
    }

    xmlDocument.setParameter("numberPages", strPageNumber);

    return(xmlDocument.print());
  }


  public String getServletInfo() {
    return "Servlet Etiquetas. This Servlet was made by Firi";
  } // end of the getServletInfo() method
}

