/*
 * 
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SL All
 * portions are Copyright (C) 2001-2009 Openbravo SL All Rights Reserved.
 * Contributor(s): ______________________________________.
 */

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.RegistrationData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class Heartbeat extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    final org.openbravo.erpCommon.businessUtility.HeartbeatData[] data = org.openbravo.erpCommon.businessUtility.HeartbeatData
        .selectSystemProperties(this);
    if (data.length > 0) {
      String servletContainer = data[0].servletContainer;
      String servletContainerVersion = data[0].servletContainerVersion;
      if ((servletContainer == null || servletContainer.equals(""))
          && (servletContainerVersion == null || servletContainerVersion.equals(""))) {
        final String serverInfo = request.getSession().getServletContext().getServerInfo();
        if (serverInfo != null && serverInfo.contains("/")) {
          servletContainer = serverInfo.split("/")[0];
          servletContainerVersion = serverInfo.split("/")[1];

          HeartbeatData.updateServletContainer(this, servletContainer, servletContainerVersion);
        }
      }
    }

    if (vars.commandIn("DEFAULT", "DEFAULT_MODULE", "UPDATE_MODULE")) {
      printPageDataSheet(response, vars);
    } else if (vars.commandIn("CONFIGURE", "CONFIGURE_MODULE")) {
      response.sendRedirect(strDireccion + "/ad_process/TestHeartbeat.html?Command="
          + vars.getCommand() + "&inpcRecordId="
          + vars.getStringParameter("inpcRecordId", IsIDFilter.instance));
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/Heartbeat")
        .createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    String msgCode = vars.getCommand().equals("DEFAULT_MODULE") ? "HB_WELCOME_MODULE"
        : "HB_WELCOME";
    xmlDocument.setParameter("welcome", Utility.formatMessageBDToHtml(Utility.messageBD(this,
        msgCode, vars.getLanguage())));

    xmlDocument.setParameter("recordId", vars.getStringParameter("inpcRecordId",
        IsIDFilter.instance));

    String jsCommand = "var cmd='";
    if (vars.commandIn("DEFAULT")) {
      jsCommand += "CONFIGURE";
    } else {
      jsCommand += "CONFIGURE_MODULE";
    }
    jsCommand += "';";
    xmlDocument.setParameter("cmd", jsCommand);

    final RegistrationData[] rData = RegistrationData.select(this);
    if (rData.length > 0) {
      final String isregistrationactive = rData[0].isregistrationactive;
      final String rPostponeDate = rData[0].postponeDate;
      if (isregistrationactive == null || isregistrationactive.equals("")) {
        Date date = null;
        try {
          if (!rPostponeDate.equals("")) {
            date = new SimpleDateFormat(vars.getJavaDateFormat()).parse(rPostponeDate);
          }
        } catch (final ParseException e) {
          e.printStackTrace();
        }
        final Date today = new Date();
        if ((rPostponeDate == null || rPostponeDate.equals("")) || date.before(today)) {
          final String openRegistrationString = "\n function openRegistration() { "
              + "\n var w = window.opener; " + "\n if(w) { " + "\n w.setTimeout(\"openRegistration();\",100); "
              + "\n } " + "\n return true; \n }";
          xmlDocument.setParameter("registration", openRegistrationString);
        } else {
          xmlDocument.setParameter("registration",
              "\n function openRegistration() { \n return true; \n }");
        }
      }
    }

    out.println(xmlDocument.print());
    out.close();
  }

  @Override
  public String getServletInfo() {
    return "Heartbeat pop-up form servlet.";
  } // end of getServletInfo() method
}
