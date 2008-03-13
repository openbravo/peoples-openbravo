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

package org.openbravo.erpCommon.ad_forms;

import org.openbravo.erpCommon.utility.ToolBar;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class DebtPaymentUnapply extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      printPageDataSheet(response, vars);
    } else if (vars.commandIn("PROCESS")) {
      String strCDebtPaymentId = vars.getInStringParameter("inpDebtPayment");
      if (strCDebtPaymentId.equals("")) strCDebtPaymentId = "('0')";
      int i = updateSelection(strCDebtPaymentId);
      vars.setSessionValue("DebtPaymentUnapply|message", "Updated = " + Integer.toString(i));
      response.sendRedirect(strDireccion + request.getServletPath());
     } else pageError(response);
  }

  int updateSelection(String strCDebtPaymentId)
    throws IOException, ServletException {
    DebtPaymentUnapplyData [] data = DebtPaymentUnapplyData.selectRecord(this, strCDebtPaymentId);
    int i=0;
    for (i=0;i<data.length;i++) {
      if (data[i].iscancel.equals("N")){
        DebtPaymentUnapplyData.updateGenerate(this, data[i].cDebtPaymentId);
      }else{
        DebtPaymentUnapplyData.updateCancel(this, data[i].cDebtPaymentId);
      }
    }
    return i;
  }


  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[]={"sectionDetail"};
    XmlDocument xmlDocument=null;
    DebtPaymentUnapplyData[] data=null;
    data = DebtPaymentUnapplyData.select(this, vars.getLanguage());
    if (data==null || data.length == 0) {
     xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/DebtPaymentUnapply", discard).createXmlDocument();
     data = DebtPaymentUnapplyData.set();
    } else {
     xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/DebtPaymentUnapply").createXmlDocument();
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "DebtPaymentUnapply", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

	try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.DebtPaymentUnapply");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "DebtPaymentUnapply.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "DebtPaymentUnapply.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("DebtPaymentUnapply");
      vars.removeMessage("DebtPaymentUnapply");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet DebtPaymentUnapply. This Servlet was made by Eduardo Argal";
  } // end of getServletInfo() method
}

