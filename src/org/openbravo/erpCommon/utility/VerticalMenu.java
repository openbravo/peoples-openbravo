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
 * All portions are Copyright (C) 2001-2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.utility;

import org.openbravo.base.secureApp.*;
import org.openbravo.erpCommon.ad_background.PeriodicHeartbeatData;
import org.openbravo.erpCommon.ad_process.RegisterData;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.erpCommon.security.AccessData;



public class VerticalMenu extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  String target = "frameAplicacion";
  
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      printPageDataSheet(response, vars, "0", false);
    } else if (vars.commandIn("ALL")) {
      printPageDataSheet(response, vars, "0", true);
    } else if (vars.commandIn("ALERT")) {
      printPageAlert(response, vars);
    } else if (vars.commandIn("LOADING")) {
      printPageLoadingMenu(response, vars);
    } else throw new ServletException();
  }
  
  void printPageAlert(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    
    Integer alertCount = 0;
    
    VerticalMenuData[] data = VerticalMenuData.selectAlertRules(this, vars.getUser(), vars.getRole());
    if (data!=null && data.length!=0) {
      for (int i=0; i<data.length; i++) {
        String strWhere = new UsedByLink().getWhereClause(vars,"",data[i].filterclause);
        try {
          Integer count = new Integer(VerticalMenuData.selectCountActiveAlerts(this, Utility.getContext(this, vars, "#User_Client", ""), Utility.getContext(this, vars, "#User_Org", ""),data[i].adAlertruleId,strWhere)).intValue();
          alertCount += count;
        } catch (Exception ex) {
          log4j.error("Error in Alert Query, alertRule="+data[i].adAlertruleId+" error:"+ex.toString());
        }       
      }
    }
  
    response.setContentType("text/plain; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(alertCount.toString());
    out.close();
  }
  
  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strCliente, boolean open) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Vertical Menu's screen");
    String[] discard = new String[1];
    if (open) discard[0] = new String("buttonExpand");
    else discard[0] = new String("buttonCollapse");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/VerticalMenu", discard).createXmlDocument();
    
    
    MenuData[] data = MenuData.selectIdentificacion(this, strCliente);
    MenuData[] dataMenu;
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    dataMenu = MenuData.select(this, vars.getLanguage(), vars.getRole(), data[0].parentId);
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    StringBuffer menu = new StringBuffer();
    menu.append(generarMenuVertical(dataMenu, strDireccion, "0", open));
    menu.append("<tr>\n");
    menu.append("  <td>\n");
    menu.append("    <table cellspacing=\"0\" cellpadding=\"0\" onmouseover=\"window.status='");
    menu.append(Utility.messageBD(this, "Information", vars.getLanguage()));
    menu.append("';return true;\"");
    menu.append(" onmouseout=\"window.status='';return true;\"");
    menu.append(" id=\"folderInformation\">\n");
    menu.append("      <tr class=\"Normal ");
    if (!open) menu.append("NOT_");
    menu.append("Opened NOT_Hover NOT_Selected NOT_Pressed NOT_Focused");
    menu.append("\" id=\"childInformation\" onmouseover=\"setMouseOver(this);return true;\" onmouseout=\"setMouseOut(this); return true;\"");
    menu.append(" onmousedown=\"setMouseDown(this);return true;\" onmouseup=\"setMouseUp(this);return true;\">\n");
    menu.append("        <td width=\"5px\" id=\"folderCell1_Information\"><img src=\"").append(strReplaceWith).append("/images/blank.gif\" class=\"Menu_Client_Button_BigIcon Menu_Client_Button_BigIcon_folder");
    menu.append((open?"Opened":"Closed"));
    menu.append("\" id=\"folderImgInformation\"></td>\n");
    menu.append("        <td nowrap=\"\" id=\"folderCell2_Information\">");
    menu.append(Utility.messageBD(this, "Information", vars.getLanguage()));
    menu.append("        </td>\n");
    menu.append("      </tr>\n");
    menu.append("    </table>\n");
    menu.append("  </td>\n");
    menu.append("</tr>\n");
    menu.append("<tr>\n");
    menu.append("  <td");
    menu.append(" style=\"").append((!open?"display: none;":"")).append("\" id=\"parentInformation\">\n");
    menu.append("    <table cellspacing=\"0\" cellpadding=\"0\">\n");
    menu.append(generarMenuSearchs(vars, strDireccion, open));
    menu.append("    </table>\n");
    menu.append("  </td>\n");
    menu.append("</tr>\n");
    xmlDocument.setParameter("menu", menu.toString());
    xmlDocument.setParameter("userName", MenuData.getUserName(this, vars.getUser()));

    decidePopups(xmlDocument, vars);
    
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public void printPageLoadingMenu(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/VerticalMenuLoading").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String tipoVentana(String tipo) {
    if (tipo.equals("W")) return "Windows";
    else if (tipo.equals("X")) return "Forms";
    else if (tipo.equals("P")) return "Processes";
    else if (tipo.equals("T")) return "Tasks";
    else if (tipo.equals("R")) return "Reports";
    else if (tipo.equals("F")) return "WorkFlow";
    else if (tipo.equals("B")) return "WorkBench";
    else if (tipo.equals("L")) return "ExternalLink";
    else if (tipo.equals("I")) return "InternalLink";
    else return "";
  }

  public String tipoVentanaNico(String tipo) {
    if (tipo.equals("W")) return "window";
    else if (tipo.equals("X")) return "form";
    else if (tipo.equals("P")) return "process";
    else if (tipo.equals("T")) return "task";
    else if (tipo.equals("R")) return "report";
    else if (tipo.equals("F")) return "wf";
    else if (tipo.equals("B")) return "wb";
    else if (tipo.equals("L")) return "el";
    else if (tipo.equals("I")) return "il";
    else return "";
  }

  public String generarMenuVertical(MenuData[] menuData, String strDireccion, String indice, boolean open) {
    if (menuData==null || menuData.length==0) return "";
    if (indice == null) indice="0";
//    boolean haveData=false;
    StringBuffer strText = new StringBuffer();
    for (int i=0;i<menuData.length;i++) {
      if (menuData[i].parentId.equals(indice)) {
//        haveData=true;
        String strHijos = generarMenuVertical(menuData, strDireccion, menuData[i].nodeId, open);
        String strID = "";
        if (!strHijos.equals("") || menuData[i].issummary.equals("N")) {
          strText.append("<tr>\n");
          strText.append("  <td>\n");
          strText.append("    <table cellspacing=\"0\" cellpadding=\"0\"");
          if (menuData[i].issummary.equals("N")) {
            strText.append(" id=\"").append(tipoVentanaNico(menuData[i].action)).append(menuData[i].nodeId).append("\"");
            strID = tipoVentanaNico(menuData[i].action) + menuData[i].nodeId;
          } else
            strText.append(" id=\"folder").append(menuData[i].nodeId).append("\"");
          strText.append(" onmouseover=\"window.status='");
          strText.append(FormatUtilities.replaceJS(menuData[i].description));
          strText.append("';return true;\"");
          strText.append(" onmouseout=\"window.status='';return true;\">\n");
          strText.append("      <tr");
          strText.append(" class=\"Normal ");
          if (!open || !menuData[i].issummary.equals("Y")) strText.append("NOT_");
          strText.append("Opened NOT_Hover NOT_Selected NOT_Pressed NOT_Focused");
          strText.append("\"");
          if (menuData[i].issummary.equals("N")) {
            strText.append(" id=\"child").append(strID).append("\"");
            strText.append(" onclick=\"checkSelected('child").append(strID).append("');openLink('");
            if (menuData[i].action.equals("L") || menuData[i].action.equals("I")) strText.append(menuData[i].url);
            else {
              strText.append(getUrlString(strDireccion, menuData[i].name, menuData[i].action, menuData[i].classname, menuData[i].mappingname, menuData[i].adWorkflowId, menuData[i].adTaskId, menuData[i].adProcessId));
            }
            strText.append("', '");
            if (menuData[i].action.equals("F") || menuData[i].action.equals("T") || (menuData[i].action.equals("P") && menuData[i].mappingname.equals(""))) strText.append("frameOculto");
            else if (menuData[i].action.equals("L")) strText.append("_blank");
            else strText.append(target);
            strText.append("'");
            if (menuData[i].action.equals("F")) strText.append(", 600, 600");
            strText.append(");return false;\"");
          } else {
            strText.append(" id=\"child").append(menuData[i].nodeId).append("\"");
          }
          strText.append(" onmouseover=\"setMouseOver(this);return true;\" onmouseout=\"setMouseOut(this); return true;\"");
          strText.append(" onmousedown=\"setMouseDown(this);return true;\" onmouseup=\"setMouseUp(this);return true;\">\n");
          strText.append("        <td width=\"5px\"");
          if (menuData[i].issummary.equals("Y")) strText.append(" id=\"folderCell1_").append(menuData[i].nodeId).append("\"");
          strText.append(">");
          strText.append("<img src=\"").append(strReplaceWith).append("/images/blank.gif\" class=\"Menu_Client_Button_").append((indice.equals("0")?"Big":"")).append("Icon");
          if (menuData[i].issummary.equals("N")) {
            if (menuData[i].action.equals("F")) strText.append(" Menu_Client_Button_Icon_childWorkFlow");
            else if (menuData[i].action.equals("T")) strText.append(" Menu_Client_Button_Icon_childTasks");
            else if (menuData[i].action.equals("B")) strText.append(" Menu_Client_Button_Icon_childWorkBench");
            else if (menuData[i].action.equals("P")) strText.append(" Menu_Client_Button_Icon_childProcesses");
            else if (menuData[i].action.equals("R")) strText.append(" Menu_Client_Button_Icon_childReports");
            else if (menuData[i].action.equals("X")) strText.append(" Menu_Client_Button_Icon_childForms");
            else if (menuData[i].action.equals("L") || menuData[i].action.equals("I")) strText.append(" Menu_Client_Button_Icon_childExternalLink");
            else strText.append(" Menu_Client_Button_Icon_childWindows");
          } else {
            strText.append(" Menu_Client_Button_");
            if (indice.equals("0")) strText.append("Big");
            strText.append("Icon_folder");
            strText.append((open?"Opened":"Closed"));
          }
          strText.append("\"");
          if (menuData[i].issummary.equals("Y")) strText.append(" id=\"folderImg").append(menuData[i].nodeId).append("\"");
          strText.append(">");
          strText.append("</td>\n");
          strText.append("        <td nowrap=\"\"");
          if (menuData[i].issummary.equals("Y")) strText.append(" id=\"folderCell2_").append(menuData[i].nodeId).append("\"");
          strText.append(">");
          strText.append(menuData[i].name);
          strText.append("</td>\n");
          strText.append("      </tr>\n");
          strText.append("    </table>\n");
          strText.append("  </td>\n");
          strText.append("</tr>\n");
          strText.append("<tr>\n");
          strText.append("  <td");
          if (strHijos.equals("")) {
            strText.append(" style=\"").append("display: none;").append("\" id=\"parent").append(menuData[i].nodeId).append("\">\n");
          } else {
            strText.append(" style=\"").append((!open?"display: none;":"")).append("\" id=\"parent").append(menuData[i].nodeId).append("\">\n");
          }
          strText.append("    <table cellspacing=\"0\" cellpadding=\"0\" class=\"Menu_Client_child_bg\">\n");
          strText.append(strHijos);
          strText.append("    </table>\n");
          strText.append("  </td>\n");
          strText.append("</tr>\n");
        }
      }
    }
    return (strText.toString());
  }

  public static String getUrlString(String strDireccionBase, String name, String action, String classname, String mappingname, String adWorkflowId, String adTaskId, String adProcessId) {
    StringBuffer strResultado = new StringBuffer();
    strResultado.append(strDireccionBase);
    if (mappingname.equals("")) {
      if (action.equals("F")) {
        strResultado.append("/ad_workflow/WorkflowControl.html?inpadWorkflowId=").append(adWorkflowId);
      } else if (action.equals("T")) {
        strResultado.append("/utility/ExecuteTask.html?inpadTaskId=").append(adTaskId);
      } else if (action.equals("P")) {
        strResultado.append("/ad_actionButton/ActionButton_Responser.html?inpadProcessId=").append(adProcessId);
      } else if (action.equals("X")) {
        strResultado.append("/ad_forms/").append(FormatUtilities.replace(name)).append(".html");
      } else if (action.equals("R")) {
        strResultado.append("ad_reports/").append(FormatUtilities.replace(name)).append(".html");
      }
    } else {
      strResultado.append(mappingname);
    }
    return strResultado.toString();
  }

  public String generarMenuSearchs(VariablesSecureApp vars, String direccion, boolean open) throws ServletException {
    StringBuffer result = new StringBuffer();
    MenuData[] data = MenuData.selectSearchs(this, vars.getLanguage());
    if (data==null || data.length==0) return "";
    for (int i=0;i<data.length;i++) {
      if (!AccessData.selectAccessSearch(this, vars.getRole(), data[i].nodeId).equals("0")) {
        result.append("<tr>\n");
        result.append("  <td>\n");
        result.append("    <table cellspacing=\"0\" cellpadding=\"0\" onmouseover=\"window.status='");
        result.append(FormatUtilities.replaceJS(data[i].description));
        result.append("';return true;\"");
        result.append(" onmouseout=\"window.status='';return true;\"");
        result.append(" id=\"info").append(FormatUtilities.replace(data[i].name)).append("\"");
        result.append(">\n");
        result.append("      <tr");
        result.append(" class=\"Normal NOT_Opened NOT_Hover NOT_Selected NOT_Pressed NOT_Focused\"");
        result.append(" id=\"childinfo").append(FormatUtilities.replace(data[i].name)).append("\"");
        result.append(" onclick=\"checkSelected('childinfo").append(FormatUtilities.replace(data[i].name)).append("');openSearch(null, null, '");
        String javaClassName = data[i].classname.trim();
        if (data[i].nodeId.equals("800011")) javaClassName = "/info/ProductComplete_FS.html";
        else if (data[i].nodeId.equals("21")) javaClassName = "/info/Location_FS.html";
        else if (data[i].nodeId.equals("25")) javaClassName = "/info/Account_FS.html";
        else if (data[i].nodeId.equals("800013")) javaClassName = "/info/Locator_Detail_FS.html";
        else if (data[i].nodeId.equals("31")) javaClassName = "/info/Locator_FS.html";
        result.append(direccion).append(javaClassName);
        result.append("', null, false);return false;\" onmouseover=\"setMouseOver(this);return true;\" onmouseout=\"setMouseOut(this); return true;\"");
        result.append(" onmousedown=\"setMouseDown(this);return true;\" onmouseup=\"setMouseUp(this);return true;\">\n");
        result.append("        <td width=\"5px\"><img src=\"").append(strReplaceWith).append("/images/blank.gif\" class=\"Menu_Client_Button_Icon Menu_Client_Button_Icon_childInfo\"></td>\n");
        result.append("        <td nowrap=\"\">");
        result.append(data[i].name);
        result.append("        </td>\n");
        result.append("      </tr>\n");
        result.append("    </table>\n");
        result.append("  </td>\n");
        result.append("</tr>\n");
      }
    }
    return result.toString();
  }

void decidePopups(XmlDocument xmlDocument, VariablesSecureApp vars) throws ServletException {
    
    // Check if the heartbeat popup needs to be displayed
    PeriodicHeartbeatData[] hbData = PeriodicHeartbeatData.selectSystemProperties(myPool);
    if (hbData.length > 0) {
      String isheartbeatactive = hbData[0].isheartbeatactive;
      String postponeDate = hbData[0].postponeDate;
      if (isheartbeatactive == null || isheartbeatactive.equals("")) {
        if (postponeDate == null || postponeDate.equals("")) {
          xmlDocument.setParameter("popup", "openHeartbeat();");
          return;
        } else {
          Date date = null;
          try {
            date = new SimpleDateFormat("dd-MM-yyyy").parse(postponeDate);
            if (date.before(new Date())) {
              xmlDocument.setParameter("popup", "openHeartbeat();");
              return;
            }
          } catch (ParseException e) {
            e.printStackTrace();
          }
        }
      } 
    }
    
    // If the heartbeat doesn't need to be displayed, check the registration popup
    RegisterData[] rData = RegisterData.select(myPool);
    if (rData.length > 0) {
      String isregistrationactive = rData[0].isregistrationactive;
      String rPostponeDate = rData[0].postponeDate;
      if (isregistrationactive == null || isregistrationactive.equals("")) {
        if (rPostponeDate == null || rPostponeDate.equals("")) {
          xmlDocument.setParameter("popup", "openRegistration();");
          return;
        } else {
          Date date = null;
          try {
            date = new SimpleDateFormat("dd-MM-yyyy").parse(rPostponeDate);
            if (date.before(new Date())) {
              xmlDocument.setParameter("popup", "openRegistration();");
              return;
            }
          } catch (ParseException e) {
            e.printStackTrace();
          }
        }
      } 
    }
    xmlDocument.setParameter("popup", "");
  }
  
  public String getServletInfo() {
    return "Servlet that presents application's vertical menu";
  } // end of getServletInfo() method
}
