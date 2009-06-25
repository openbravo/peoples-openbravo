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
 * All portions are Copyright (C) 2001-2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class DebtPaymentUnapply extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      printPageDataSheet(response, vars, strWindow);
    } else if (vars.commandIn("PROCESS")) {
      String strCDebtPaymentId = vars.getInStringParameter("inpDebtPayment", IsIDFilter.instance);
      OBError myMessage = updateSelection(vars, strCDebtPaymentId);
      vars.setMessage("DebtPaymentUnapply", myMessage);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else
      pageError(response);
  }

  private OBError updateSelection(VariablesSecureApp vars, String strCDebtPaymentId) {
    OBError myMessage = new OBError();

    if (strCDebtPaymentId.equals("")) {
      myMessage.setType("Info");
      myMessage.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
      myMessage.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
          "No records selected"));
      return myMessage;
    }

    try {
      DebtPaymentUnapplyData[] data = DebtPaymentUnapplyData.selectRecord(this, strCDebtPaymentId);
      int i = 0;
      for (i = 0; i < data.length; i++) {
        if (data[i].iscancel.equals("N")) {
          DebtPaymentUnapplyData.updateGenerate(this, data[i].cDebtPaymentId);
        } else {
          DebtPaymentUnapplyData.updateCancel(this, data[i].cDebtPaymentId);
        }
      }

      myMessage.setType("Success");
      myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      myMessage.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), "Updated = "
          + Integer.toString(i)));
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    return myMessage;
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strWindow) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "sectionDetail" };
    XmlDocument xmlDocument = null;
    DebtPaymentUnapplyData[] data = null;
    data = DebtPaymentUnapplyData.select(this, vars.getLanguage(), Utility.getContext(this, vars,
        "#User_Org", strWindow), Utility.getContext(this, vars, "#User_Client", strWindow));
    if (data == null || data.length == 0) {
      xmlDocument = xmlEngine.readXmlTemplate(
          "org/openbravo/erpCommon/ad_forms/DebtPaymentUnapply", discard).createXmlDocument();
      data = DebtPaymentUnapplyData.set();
    } else {
      xmlDocument = xmlEngine
          .readXmlTemplate("org/openbravo/erpCommon/ad_forms/DebtPaymentUnapply")
          .createXmlDocument();
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "DebtPaymentUnapply", false, "", "",
        "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_forms.DebtPaymentUnapply");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "DebtPaymentUnapply.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "DebtPaymentUnapply.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("DebtPaymentUnapply");
      vars.removeMessage("DebtPaymentUnapply");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet DebtPaymentUnapply. This Servlet was made by Eduardo Argal";
  } // end of getServletInfo() method
}
