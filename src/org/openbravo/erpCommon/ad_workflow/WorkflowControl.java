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

package org.openbravo.erpCommon.ad_workflow;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;



public class WorkflowControl extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  static final int increment = 30;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    String strAD_Workflow_ID = vars.getGlobalVariable("inpadWorkflowId", "WorkflowControl|adWorkflowId");

    if (!vars.commandIn("DEFAULT") && !hasGeneralAccess(vars, "F", strAD_Workflow_ID)) {
      bdError(response, "AccessTableNoView",vars.getLanguage());
      return;
    }

    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars, strAD_Workflow_ID);
    } else if (vars.commandIn("WORKFLOW")) {
      printPageDataSheet(response, vars, strAD_Workflow_ID);
    } else if (vars.commandIn("WORKFLOW_ACTION")) {
      String strAction = vars.getRequiredStringParameter("inpAction");
      String strClave = vars.getRequiredStringParameter("inpClave");
      String strPath = getUrlPath(vars.getLanguage(), strAction, strClave);
      
      printPageRedirect(response, vars, strPath);
    } else pageError(response);
  }

  void printPageRedirect(HttpServletResponse response, VariablesSecureApp vars, String strPath) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: print page redirect");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_workflow/WorkflowControl_Redirect").createXmlDocument();

    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("href", strPath);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String windowIcon(String action) {
    String strIcon="";
    if (action.equals("W")) strIcon = "Popup_Workflow_Button_Icon Menu_Client_Button_Icon_childWindows";//Window
    else if (action.equals("X")) strIcon = "Popup_Workflow_Button_Icon Menu_Client_Button_Icon_childForms";//Form
    else if (action.equals("P")) strIcon = "Popup_Workflow_Button_Icon Menu_Client_Button_Icon_childProcesses";//Process
    else if (action.equals("T")) strIcon = "Popup_Workflow_Button_Icon Menu_Client_Button_Icon_childTasks";//Task
    else if (action.equals("R")) strIcon = "Popup_Workflow_Button_Icon Menu_Client_Button_Icon_childProcesses";//Process
    else if (action.equals("F")) strIcon = "Popup_Workflow_Button_Icon Menu_Client_Button_Icon_childWorkflows";//WorkFlow
    else strIcon = "Popup_Workflow_Button_Icon Menu_Client_Button_Icon_childWindows";//Windows
    return strIcon;
  }

  public String getUrlPath(String language, String action, String clave) throws ServletException {
    String strWindow="", strForm="", strProcess="", strTask="", strWorkflow="";
    if (action.equals("W")) strWindow = clave;
    else if (action.equals("X")) strForm = clave;
    else if (action.equals("P")) strProcess = clave;
    else if (action.equals("T")) strTask = clave;
    else if (action.equals("R")) strProcess = clave;
    else if (action.equals("F")) strWorkflow = clave;
    else return "";

    MenuData[] menuData = MenuData.selectData(this, language, strWindow, strProcess, strForm, strTask, strWorkflow);
    if (menuData==null || menuData.length==0) throw new ServletException("WorkflowControl.getUrlPath() - Error while getting data");

    return VerticalMenu.getUrlString(strDireccion, menuData[0].name, menuData[0].action, menuData[0].classname, menuData[0].mappingname, menuData[0].adWorkflowId, menuData[0].adTaskId, menuData[0].adProcessId, "N", "");
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strAD_Workflow_ID) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: print page");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_workflow/WorkflowControl_Response").createXmlDocument();

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");

    xmlDocument.setParameter("workflow", strAD_Workflow_ID);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strAD_Workflow_ID) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    WorkflowControlData[] workflowName = null;
    if (vars.getLanguage().equals("en_US")) {
      workflowName = WorkflowControlData.selectWorkflowName(this, strAD_Workflow_ID);
    } else {
      workflowName = WorkflowControlData.selectWorkflowNameTrl(this, vars.getLanguage(), strAD_Workflow_ID);
    }
    String[] discard = {""};
    if (workflowName==null || workflowName.length==0 || workflowName[0].help.equals("")) discard[0] = new String("fieldWorkflowHelp");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_workflow/WorkflowControl", discard).createXmlDocument();

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    if (workflowName!=null && workflowName.length>0) {
      xmlDocument.setParameter("workflowName", workflowName[0].name);
      xmlDocument.setParameter("workflowHelp", workflowName[0].help);
    }
    xmlDocument.setParameter("detail", buildHtml(vars, strAD_Workflow_ID));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String buildHtml(VariablesSecureApp vars, String strAD_Workflow_ID) throws ServletException {
    String firstNode = WorkflowControlData.selectFirstNode(this, strAD_Workflow_ID);
    if (firstNode.equals("")) {
      log4j.warn("WorkflowControl.buildHtml() - There're no first node defined for workflow: " + strAD_Workflow_ID);
      return "";
    }
    StringBuffer sb = new StringBuffer();
    WorkflowControlData[] name = null;
    if (vars.getLanguage().equals("en_US")) name = WorkflowControlData.selectFirstNodeData(this, firstNode);
    else name = WorkflowControlData.selectFirstNodeDataTrl(this, vars.getLanguage(), firstNode);
    sb.append(buildButton(vars, name[0])).append("\n");
    sb.append(buildLevel(vars, firstNode));

    return sb.toString();
  }

  String buildLevel(VariablesSecureApp vars, String node) throws ServletException {
    WorkflowControlData[] data = null;
    if (vars.getLanguage().equals("en_US")) data = WorkflowControlData.select(this, Utility.getContext(this, vars, "#User_Client", "WorkflowControl"), Utility.getContext(this, vars, "#User_Org", "WorkflowControl"), node);
    else data = WorkflowControlData.selectTrl(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "WorkflowControl"), Utility.getContext(this, vars, "#User_Org", "WorkflowControl"), node);
    if (data==null || data.length==0) return "";
    StringBuffer sb = new StringBuffer();
    if (data.length>1) {
      sb.append("<TR><TD colspan=\"2\">\n");
      sb.append("  <TABLE cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><TR>");
    }
    for (int i=0;i<data.length;i++) {
      if (data.length>1) {
        sb.append("<TD valign=\"top\"><TABLE cellspacing=\"0\" cellpadding=\"0\" class=\"Popup_Client_TableWorkflow\">\n");
        sb.append("  <TR>\n");
        for (int j=0;j<2;j++) sb.append("<td class=\"TableEdition_OneCell_width\"></td>\n");
        sb.append("  </TR>\n");
      }
      sb.append(line());
      sb.append(buildButton(vars, data[i])).append("\n");
      sb.append(buildLevel(vars, data[i].adWfNodeId));
      if (data.length>1) {
        sb.append("</TABLE></TD>\n");
      }
    }
    if (data.length>1) {
      sb.append("</TR></TABLE>\n");
      sb.append("</TD></TR>\n");
    }
    return sb.toString();
  }

  public String claveWindow(WorkflowControlData data) {
    if (data.action.equals("W")) return data.adWindowId;
    else if (data.action.equals("X")) return data.adFormId;
    else if (data.action.equals("P")) return data.adProcessId;
    else if (data.action.equals("T")) return data.adTaskId;
    else if (data.action.equals("R")) return data.adProcessId;
    else if (data.action.equals("F")) return data.workflowId;
    else return "";
  }

  String buildButton(VariablesSecureApp vars, WorkflowControlData data) throws ServletException {
    StringBuffer html = new StringBuffer();
    String strClave = claveWindow(data);
    html.append("<TR>\n");
    html.append("  <TD class=\"Popup_Workflow_Button_ContentCell\">\n");
    html.append("    <a href=\"#\" class=\"Popup_Workflow_Button\" onmouseout=\"window.status='';return true;\" onmouseover=\"'");
    html.append(data.name).append("';return true;\" onblur=\"this.hideFocus=false\" ");
    html.append(" onClick=\"this.hideFocus=true;callServlet('").append(data.action).append("', '").append(strClave).append("');return false;\">\n");
    html.append("    <img src=\"").append(strReplaceWith).append("/images/blank.gif\" class=\"").append(windowIcon(data.action)).append("\" border=\"0\" title=\"");
    html.append(data.name).append("\"></img></a>\n");
    html.append("</TD>\n");
    html.append("<TD class=\"Popup_Workflow_text_ContentCell\">\n");
    html.append("  <a href=\"#\" onclick=\"callServlet('").append(data.action).append("', '").append(strClave).append("');");
    html.append("return false;\" onmouseover=\"window.status='").append(data.name).append("';return true;\" ");
    html.append("onmouseout=\"window.status='';return true;\" class=\"Popup_Workflow_text\">");
    html.append(data.name).append("</a>\n");
    html.append("</TD>\n");
    html.append("</TR>\n");
    return html.toString();
  }

  String line() {
    StringBuffer html = new StringBuffer();
    html.append("<tr>");
    html.append("<td class=\"Popup_Workflow_arrow_ContentCell\"><img class=\"Popup_Workflow_arrow\" src=\"").append(strReplaceWith).append("/images/blank.gif\" border=\"0\"></img></td>");
    html.append("<td></td></tr>");
    return html.toString();
  }

  public String getServletInfo() {
    return "Servlet WorkflowControl";
  } // end of getServletInfo() method
}

