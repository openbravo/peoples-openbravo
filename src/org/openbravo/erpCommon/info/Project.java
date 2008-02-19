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
package org.openbravo.erpCommon.info;

import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.ToolBar;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.Replace;



public class Project extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      vars.getRequestGlobalVariable("WindowID", "Project.windowId");
      vars.getRequestGlobalVariable("inpBpartnerId", "Project.bpartner");
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "Project.name");
      vars.removeSessionValue("Project.key");
      if (!strNameValue.equals("")) {
        int guion = strNameValue.indexOf(" - ");
        if (guion!=-1) {
          String strKey = strNameValue.substring(0, guion).trim();
          strNameValue = strNameValue.substring(guion+3).trim();
          vars.setSessionValue("Project.key", strKey);
        }
        vars.setSessionValue("Project.name", strNameValue + "%");
      }
      printPageFS(response, vars);
    } else if (vars.commandIn("KEY")) {
      vars.getRequestGlobalVariable("WindowID", "Project.windowId");
      String strBparnter = vars.getRequestGlobalVariable("inpBpartnerId", "Project.bpartner");
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "Project.key");
      vars.removeSessionValue("Project.name");
      vars.setSessionValue("Project.key", strKeyValue + "%");
      ProjectData[] data = ProjectData.selectKey(this, Utility.getContext(this, vars, "#User_Client", "Project"), Utility.getContext(this, vars, "#User_Org", "Project"), strBparnter, strKeyValue + "%");
      if (data!=null && data.length==1) {
        printPageKey(response, vars, data);
      } else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strWindowId = vars.getGlobalVariable("WindowID", "Project.windowId", "Project");
      String strKeyValue = vars.getGlobalVariable("inpKey", "Project.key", "");
      String strNameValue = vars.getGlobalVariable("inpName", "Project.name", "");
      String strBpartners = vars.getGlobalVariable("inpBpartnerId", "Project.bpartner", "");
      printPageFrame1(response, vars, strKeyValue, strNameValue, strBpartners, strWindowId);
    } else if (vars.commandIn("FRAME2")) {
      String strWindowId = vars.getGlobalVariable("inpWindowID", "Project.windowId", "Project");
      String strKey = vars.getGlobalVariable("inpKey", "Project.key", "");
      String strName = vars.getGlobalVariable("inpName", "Project.name", "");
      String strBpartners = vars.getGlobalVariable("inpBpartnerId", "Project.bpartner", "");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      printPageFrame2(response, vars, strKey, strName, strBpartners, strIsSOTrx);
    } else if (vars.commandIn("FIND")) {
      String strWindowId = vars.getRequestGlobalVariable("inpWindowId", "Project.windowId");
      String strKey = vars.getRequestGlobalVariable("inpKey", "Project.key");
      String strName = vars.getRequestGlobalVariable("inpName", "Project.name");
      String strBpartners = vars.getRequestGlobalVariable("inpBpartnerId", "Project.bpartner");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);

      vars.setSessionValue("Project.initRecordNumber", "0");

      printPageFrame2(response, vars, strKey, strName, strBpartners, strIsSOTrx);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("Project.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Project");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("Project.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("Project.initRecordNumber", strInitRecord);
      }

      request.getRequestDispatcher(request.getServletPath() + "?Command=FRAME2").forward(request, response);
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("Project.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Project");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("Project.initRecordNumber", strInitRecord);

      request.getRequestDispatcher(request.getServletPath() + "?Command=FRAME2").forward(request, response);
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Project seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Project_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, ProjectData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Project seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(ProjectData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    
    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].cProjectId + "\";\n");
    html.append("var texto = \"" + Replace.replace((data[0].value + " - " + data[0].name), "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strKeyValue, String strNameValue, String strBpartners, String strWindow) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the projects seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Project_F1").createXmlDocument();
    if (strKeyValue.equals("") && strNameValue.equals("")) {
      xmlDocument.setParameter("key", "%");
    } else {
      xmlDocument.setParameter("key", strKeyValue);
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strWindow);
    xmlDocument.setParameter("name", strNameValue);
    xmlDocument.setParameter("claveTercero", strBpartners);
    xmlDocument.setParameter("tercero", ProjectData.selectTercero(this, strBpartners));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strKey, String strName, String strBpartners, String strIsSOTrx) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the projects seeker");
    XmlDocument xmlDocument;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Project");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("Project.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
   // if (!strIsSOTrx.equals("Y")) strBpartners="";
    boolean hasPrevious=false, hasNext=false;
    if (strKey.equals("") && strName.equals("") && strBpartners.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Project_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", ProjectData.set());
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      ProjectData[] data = ProjectData.select(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", "Project"), Utility.getContext(this, vars, "#User_Org", "Project"), strKey, strName, strBpartners, initRecordNumber, intRecordRange);
      if (data==null || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Project_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", data);
    }

    /*
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "Project_F2", false, "document.frmMain.inpClave", "", "", false, "info", strReplaceWith, false, true);
    toolbar.prepareInfoTemplate(hasPrevious, hasNext, vars.getSessionValue("#ShowTest", "N").equals("Y"));
    xmlDocument.setParameter("toolbar", toolbar.toString());
    */
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the projects seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Project_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the project seeker";
  } // end of getServletInfo() method
}
