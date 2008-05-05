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
package org.openbravo.erpCommon.ad_actionButton;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.math.BigDecimal;

// imports for transactions
import java.sql.Connection;


public class EditCCPMeasureValues extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  static final BigDecimal ZERO = new BigDecimal(0.0);
  

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      vars.getRequestGlobalVariable("inpmaMeasureShiftId", "EditCCPMeasureValues|inpmaMeasureShiftId");
      vars.getGlobalVariable("inpwindowId", "EditCCPMeasureValues|windowId", "");
      vars.getGlobalVariable("inpTabId", "EditCCPMeasureValues|adTabId", "");
      vars.getRequestGlobalVariable("inpmeasuredate", "EditCCPMeasureValues|inpmeasuredate");
      vars.getRequestGlobalVariable("inpshift", "EditCCPMeasureValues|inpshift");

      vars.setSessionValue("EditCCPMeasureValues|adProcessId", "800062");

      printPage_FS(response, vars);
    } else if (vars.commandIn("FRAME2")) {
      printPage_F2(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strWindowId = vars.getGlobalVariable("inpWindowId", "EditCCPMeasureValues|windowId");
      String strKey = vars.getGlobalVariable("inpmaMeasureShiftId", "EditCCPMeasureValues|inpmaMeasureShiftId");
      String strTabId = vars.getGlobalVariable("inpTabId", "EditCCPMeasureValues|adTabId");
      String strProcessId = vars.getGlobalVariable("inpadProcessId", "EditCCPMeasureValues|adProcessId");
      String strMeasureDate = vars.getGlobalVariable("inpmeasuredate", "EditCCPMeasureValues|inpmeasuredate", "");
      String strShift = vars.getGlobalVariable("inpshift", "EditCCPMeasureValues|inpshift", "");
      vars.removeSessionValue("EditCCPMeasureValues|windowId");
      vars.removeSessionValue("EditCCPMeasureValues|adTabId");
      vars.removeSessionValue("EditCCPMeasureValues|adProcessId");

      printPageDataSheet(response, vars, strKey, strWindowId, strTabId, strProcessId, strMeasureDate, strShift);
    } else if (vars.commandIn("SAVE")) {
      String strKey = vars.getStringParameter("inpmaMeasureShiftId");
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strTabId = vars.getStringParameter("inpTabId");
      updateValues(response, request, vars, strKey, strWindowId, strTabId);
    } else pageErrorPopUp(response);
  }


  void printPage_FS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: FrameSet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/EditCCPMeasureValues_FS").createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
    if (log4j.isDebugEnabled()) log4j.debug("Output: FrameSet - out");
  }

  void printPage_F2(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame2");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/EditCCPMeasureValues_F2").createXmlDocument();
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame2 - Out");
  }

  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strKey, String strWindowId, String strTabId, String strProcessId, String strMeasureDate, String strShift) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: values ");
    String[] discard = {""};
    EditCCPMeasureValuesData[] data = null;
    data = EditCCPMeasureValuesData.select(this, strKey);

    String shift = "";

    if (vars.getLanguage().equals("en_US")) shift = EditCCPMeasureValuesData.selectShift(this, strShift);
    else shift = EditCCPMeasureValuesData.selectShiftTrl(this, strShift, vars.getLanguage());
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/EditCCPMeasureValues_F1", discard).createXmlDocument();
    
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);

    xmlDocument.setParameter("measureDate", strMeasureDate);
    xmlDocument.setParameter("shift", shift);
    

    if (log4j.isDebugEnabled()) log4j.debug("Param: " + vars.getLanguage() + " " + strReplaceWith + " " + strKey + " " + strWindowId + " " + strTabId + " " + strMeasureDate + " shift: " + shift + " " + strShift);
    EditCCPMeasureValuesHoursData[][] dataHours = new EditCCPMeasureValuesHoursData[data.length][10];
    EditCCPMeasureValuesValuesData[][] dataValues = new EditCCPMeasureValuesValuesData[data.length][];

    for (int i=0; i<data.length; i++){
      dataHours[i] = EditCCPMeasureValuesHoursData.select(this, data[i].groupid);
      if (dataHours[i] == null || dataHours[i].length == 0) dataHours[i] = new EditCCPMeasureValuesHoursData[0];
      
      dataValues[i] = EditCCPMeasureValuesValuesData.select(this, data[i].groupid);
      if (dataValues[i] == null || dataValues[i].length == 0) dataValues[i] = new EditCCPMeasureValuesValuesData[0];
    }

    OBError myMessage = vars.getMessage("EditCCPMeasureValues");
    vars.removeMessage("EditCCPMeasureValues");
    if (myMessage!=null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    xmlDocument.setData("structure1", data);
    xmlDocument.setDataArray("reportHours", "structureHours",dataHours);
    xmlDocument.setDataArray("reportValues", "structureValues",dataValues);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
    if (log4j.isDebugEnabled()) log4j.debug("Output: values - out");
  }

  void updateValues(HttpServletResponse response, HttpServletRequest request, VariablesSecureApp vars, String strKey, String strWindowId, String strTabId) throws IOException, ServletException {
    String strMessage="";
    if (log4j.isDebugEnabled()) log4j.debug("Update: values");

    String[] strValueId = request.getParameterValues("strKey");
    String[] strGroupId = request.getParameterValues("strGroup");
    OBError myError = null;
    int total = 0;
    Boolean  error = false;

    if (log4j.isDebugEnabled()) log4j.debug("Update: values after strValueID");
    if (strValueId != null && strValueId.length > 0) {
      Connection conn = null;
      try {
        conn = this.getTransactionConnection();
        for (int i=0; i<strValueId.length; i++) {
          String numeric = "";
          String text = "";
          String check = "N";
          if (log4j.isDebugEnabled()) log4j.debug("*****strValueId[i]=" + strValueId[i]);
          if (!strValueId[i].equals("0")) {
            String type = EditCCPMeasureValuesData.selectType(this, strValueId[i]);
            if (type.equals("N")) numeric = vars.getStringParameter("strValue"+strValueId[i]);
            else if (type.equals("T")) text = vars.getStringParameter("strValue"+strValueId[i]);
            else check = vars.getStringParameter("strValue"+strValueId[i]);
            if (log4j.isDebugEnabled()) log4j.debug("Values to update: " + strValueId[i] + ", " + numeric + ", " + text + ", " + check);
            total = EditCCPMeasureValuesData.update(conn, this, numeric, text, check.equals("Y")?"Y":"N", strValueId[i]);
            if (total == 0) error =true;
          }
        }
        releaseCommitConnection(conn);
      } catch (Exception e) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
        try {
          releaseRollbackConnection(conn);
        } catch (Exception ignored) {}
        e.printStackTrace();
        log4j.warn("Rollback in transaction");
        if (!myError.isConnectionAvailable()) {
          bdErrorConnection(response);
          return;
        }
      }
    }
    if (error){
      myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=DBExecuteError");
    } else if (myError == null) {
      myError = new OBError();
      myError.setType("Info");
      myError.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
      myError.setTitle("");
    }
    vars.setMessage(strTabId, myError);
    ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(this, strTabId);
    String strWindowPath="", strTabName="";
    if (tab!=null && tab.length!=0) {
      strTabName = FormatUtilities.replace(tab[0].name);
      strWindowPath = "../" + FormatUtilities.replace(tab[0].description) + "/" + strTabName + "_Relation.html";
    } else strWindowPath = strDefaultServlet;
    if (!strMessage.equals("")) vars.setSessionValue(strWindowId + "|" + strTabName + ".message", strMessage);
    printPageClosePopUp(response, vars, strWindowPath);
  }


  public String getServletInfo() {
    return "Servlet that presents the button of Create From Multiple";
  } // end of getServletInfo() method
}
