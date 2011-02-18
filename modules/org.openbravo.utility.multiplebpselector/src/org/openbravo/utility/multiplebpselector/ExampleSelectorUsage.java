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
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.utility.multiplebpselector;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.xmlEngine.XmlDocument;

public class ExampleSelectorUsage extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPage_W1(response, request, vars, null);
    } else if (vars.commandIn("WINDOW2")) {
      printPage_W2(response, request, vars);
    } else {
      pageError(response);
    }
  }

  void printPage_W1(HttpServletResponse response, HttpServletRequest request,
      VariablesSecureApp vars, OBError obError) throws IOException, ServletException {
    String className = this.getClass().getName().toString();
    String xmlTemplateName = "org/openbravo/utility/multiplebpselector/ExampleSelectorUsage_W1";
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(xmlTemplateName).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ExampleSelectorUsage_W1", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars, className);
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ExampleSelectorUsage_W1.html", classInfo.id, classInfo.type, strReplaceWith, tabs
              .breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ExampleSelectorUsage_W1.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    if (obError != null) {
      xmlDocument.setParameter("messageType", obError.getType());
      xmlDocument.setParameter("messageTitle", obError.getTitle());
      xmlDocument.setParameter("messageMessage", obError.getMessage());
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";");

    // +++++++++ MULTIBPSelector +++++++++++++++++++
    // put the selected business partners back in the select
    final String[] bps = getSelectedBPs(request);
    if (bps != null) {
      final StringBuilder sb = new StringBuilder();
      for (String bpId : bps) {
        final BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bpId);
        sb.append("<option value='" + bpId + "'>" + bp.getIdentifier() + "</option>\n");
      }
      xmlDocument.setParameter("sectionBusinessPartners", sb.toString());
    }
    // +++++++++ MULTIBPSelector +++++++++++++++++++

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPage_W2(HttpServletResponse response, HttpServletRequest request,
      VariablesSecureApp vars) throws IOException, ServletException {
    String className = this.getClass().getName().toString();
    String xmlTemplateName = "org/openbravo/utility/multiplebpselector/ExampleSelectorUsage_W2";
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(xmlTemplateName).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ExampleSelectorUsage_W2", false, "",
        "", "", false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars, className);
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
          "ExampleSelectorUsage_W2.html", classInfo.id, classInfo.type, strReplaceWith, tabs
              .breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ExampleSelectorUsage_W2.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    OBError myMessage = vars.getMessage("ExampleSelectorUsage_W2");
    vars.removeMessage("ExampleSelectorUsage_W2");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";");

    // +++++++++ MULTIBPSelector +++++++++++++++++++
    // set the selected bp's in a hidden field for when moving
    // back to page 1
    final StringBuilder sb = new StringBuilder();
    if (request.getParameter("bps") != null) {
      sb.append(getHiddenField("bps", request));
    } else if (request.getParameterValues("inpcBPartnerId_IN") != null) {
      final StringBuilder bpIds = new StringBuilder();
      for (String bpId : request.getParameterValues("inpcBPartnerId_IN")) {
        if (bpIds.length() > 0) {
          bpIds.append(",");
        }
        bpIds.append(bpId);
      }
      sb.append("<input type='hidden' name='bps' id='bps' value='" + bpIds + "'/>\n");
    }

    // display the selected bp's
    final String[] bpIds = getSelectedBPs(request);
    sb
        .append("\n<div style='padding-left:100px'><b>Selected Business Partners:</b><br/><br/><table>");
    for (String bpId : bpIds) {
      final BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, bpId);
      sb.append("\n<tr><td>" + bp.getIdentifier() + "</td><tr>");
    }
    sb.append("\n</table></div>");
    xmlDocument.setParameter("sectionBusinessPartners", sb.toString());

    // +++++++++ MULTIBPSelector +++++++++++++++++++

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  // +++++++++ MULTIBPSelector +++++++++++++++++++
  // Some utility methods used in this example
  private String[] getSelectedBPs(HttpServletRequest request) {
    if (request.getParameter("bps") != null) {
      return request.getParameter("bps").split(",");
    }
    return request.getParameterValues("inpcBPartnerId_IN");
  }

  private String getHiddenField(String name, HttpServletRequest request) {
    return "<input type='hidden' name='" + name + "' id='" + name + "' value='"
        + request.getParameter(name) + "'/>\n";
  }
  // +++++++++ MULTIBPSelector +++++++++++++++++++

}
