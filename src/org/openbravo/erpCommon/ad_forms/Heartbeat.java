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
 * portions are Copyright (C) 2001-2008 Openbravo SL All Rights Reserved.
 * Contributor(s): ______________________________________.
 */

package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_process.HeartbeatProcessData;
import org.openbravo.erpCommon.ad_process.RegisterData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class Heartbeat extends HttpSecureAppServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        final VariablesSecureApp vars = new VariablesSecureApp(request);

        removeFromPageHistory(request);

        final HeartbeatProcessData[] data = HeartbeatProcessData
                .selectSystemProperties(this);
        if (data.length > 0) {
            String servletContainer = data[0].servletContainer;
            String servletContainerVersion = data[0].servletContainerVersion;
            if ((servletContainer == null || servletContainer.equals(""))
                    && (servletContainerVersion == null || servletContainerVersion
                            .equals(""))) {
                final String serverInfo = request.getSession()
                        .getServletContext().getServerInfo();
                if (serverInfo != null && serverInfo.contains("/")) {
                    servletContainer = serverInfo.split("/")[0];
                    servletContainerVersion = serverInfo.split("/")[1];

                    HeartbeatProcessData.updateServletContainer(this,
                            servletContainer, servletContainerVersion);
                }
            }
        }

        if (vars.commandIn("DEFAULT")) {
            printPageDataSheet(response, vars);
        } else if (vars.commandIn("DISABLE")) {
            HeartbeatProcessData.disableHeartbeat(myPool);
        } else if (vars.commandIn("POSTPONE")) {
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 2);
            final String date = new SimpleDateFormat(vars.getJavaDateFormat())
                    .format(cal.getTime());
            HeartbeatProcessData.postpone(myPool, date);
        } else
            pageError(response);
    }

    /**
     * Removes the Heartbeat pop-up from the page history so when Openbravo 
     * back arrow is pressed, Heartbeat window has no chance of being shown.
     * 
     * @param request the HttpServletRequest object
     */
    public void removeFromPageHistory(HttpServletRequest request) {
        final Variables variables = new Variables(request);
        final String sufix = variables.getCurrentHistoryIndex();
        variables.removeSessionValue("reqHistory.servlet" + sufix);
        variables.removeSessionValue("reqHistory.path" + sufix);
        variables.removeSessionValue("reqHistory.command" + sufix);
        variables.downCurrentHistoryIndex();
    }

    void printPageDataSheet(HttpServletResponse response,
            VariablesSecureApp vars) throws IOException, ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: dataSheet");
        response.setContentType("text/html; charset=UTF-8");
        final PrintWriter out = response.getWriter();

        XmlDocument xmlDocument = null;
        xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/ad_forms/Heartbeat")
                .createXmlDocument();

        xmlDocument.setParameter("directory", "var baseDirectory = \""
                + strReplaceWith + "/\";\n");
        xmlDocument.setParameter("language", "defaultLang=\""
                + vars.getLanguage() + "\";");
        xmlDocument.setParameter("theme", vars.getTheme());
        xmlDocument.setParameter("welcome", Utility.messageBD(this,
                "HB_WELCOME", vars.getLanguage()));

        final RegisterData[] rData = RegisterData.select(this);
        if (rData.length > 0) {
            final String isregistrationactive = rData[0].isregistrationactive;
            final String rPostponeDate = rData[0].postponeDate;
            if (isregistrationactive == null || isregistrationactive.equals("")) {
                Date date = null;
                try {
                    if (!rPostponeDate.equals("")) {
                        date = new SimpleDateFormat(vars.getJavaDateFormat())
                                .parse(rPostponeDate);
                    }
                } catch (final ParseException e) {
                    e.printStackTrace();
                }
                final Date today = new Date();
                if ((rPostponeDate == null || rPostponeDate.equals(""))
                        || date.before(today)) {
                    final String openRegistrationString = "\n function openRegistration() { "
                            + "\n var w = window.opener; "
                            + "\n if(w) { "
                            + "\n w.openRegistration(); "
                            + "\n } "
                            + "\n return true; \n }";
                    xmlDocument.setParameter("registration",
                            openRegistrationString);
                } else {
                    xmlDocument
                            .setParameter("registration",
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
