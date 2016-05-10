/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class SE_Expense_BP_Project extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strBPartnerId = vars.getStringParameter("inpcBpartnerId");
      String strProjectId = vars.getStringParameter("inpcProjectId");
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strBPartnerId, strProjectId, strChanged, strTabId, strWindowId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strBPartnerId, String strProjectId, String strChanged, String strTabId,
      String strWindowId) throws IOException, ServletException {
    String localStrProjectId = strProjectId;
    String localStrBPartnerId = strBPartnerId;
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_Expense_BP_Project';\n\n");
    resultado.append("var respuesta = new Array(");

    if (strChanged.equals("inpcProjectId")) {
      // Reset Project Phase and Project Task fields
      resultado.append("new Array(\"inpcProjectphaseId\", \"\"),\n");
      resultado.append("new Array(\"inpcProjecttaskId\", \"\")\n");
      // If project changed, select project's business partner (if any).
      if (localStrProjectId != null && !localStrProjectId.equals("")) {
        String strBPartnerName = "";
        String strBPartner = SEExpenseBPProjectData.selectBPId(this, localStrProjectId);
        if (strBPartner != null && !strBPartner.equals("")) {
          localStrBPartnerId = strBPartner;
          strBPartnerName = SEExpenseBPProjectData.selectBPName(this, localStrProjectId);
          resultado.append(", new Array(\"inpcBpartnerId\", \"" + localStrBPartnerId + "\")\n");
          resultado.append(", new Array(\"inpcBpartnerId_R\", \"" + strBPartnerName + "\")\n");
        }
      }
    } else if (strChanged.equals("inpcBpartnerId")) {
      // If business partner changed...
      String strReset = "0";
      if (localStrBPartnerId != null && !localStrBPartnerId.equals("")) {
        String strProject = "";
        if (localStrProjectId != null && !localStrProjectId.equals("")) {
          // ...if project is not null, check if it corresponds with
          // the business partner
          String strBPartnerProject = SEExpenseBPProjectData.selectBPProject(this,
              localStrBPartnerId, localStrProjectId);
          // ...if there is no relationship between project and
          // business partner, take the last project of that business
          // partner (if any).
          if (strBPartnerProject == null || strBPartnerProject.equals("")) {
            // strReset = "1";
            strProject = SEExpenseBPProjectData.selectProjectId(this, localStrBPartnerId);
            if (strProject != null && !strProject.equals("")) {
              localStrProjectId = strProject;
              resultado.append("new Array(\"inpcProjectId\", \"" + localStrProjectId + "\")\n");
            } else {
              localStrProjectId = "";
            }
          }
        } else {
          // ...if project is null, take the last project of that
          // business partner (if any).
          strReset = "1";
          strProject = SEExpenseBPProjectData.selectProjectId(this, localStrBPartnerId);
          resultado.append("new Array(\"inpcProjectId\", \"" + strProject + "\"),\n");
        }
        if (strReset.equals("1")) {
          // Reset Project Phase and Project Task fields
          resultado.append("new Array(\"inpcProjectphaseId\", \"\"),\n");
          resultado.append("new Array(\"inpcProjecttaskId\", \"\")\n");
        }
      }
    }

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
