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

//import com.sun.mail.smtp.SMTPMessage;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class DropCloseFactAcct extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strCloseFactAcctGroupId = vars.getStringParameter("inpCloseFactAcctGroupId", "");
      String strKey = vars.getRequiredGlobalVariable("inpcYearId", strWindow + "|C_Year_ID");
      printPage(response, vars, strKey, strCloseFactAcctGroupId, strWindow, strTab, strProcessId);
    } else if (vars.commandIn("SAVE")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strCloseFactAcctGroupId = vars.getStringParameter("inpCloseFactAcctGroupId", "");
      vars.getRequiredGlobalVariable("inpcYearId", strWindow + "|C_Year_ID");
      String strTab = vars.getStringParameter("inpTabId");
      ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(this, strTab);
      String strWindowPath="", strTabName="";
      if (tab!=null && tab.length!=0) {
        strTabName = FormatUtilities.replace(tab[0].name);
        strWindowPath = "../" + FormatUtilities.replace(tab[0].description) + "/" + strTabName + "_Relation.html";
      } else strWindowPath = strDefaultServlet;
      String messageResult = processButton(vars, strCloseFactAcctGroupId, strWindow);
      vars.setSessionValue(strWindow + "|" + strTabName + ".message", messageResult);
      printPageClosePopUp(response, vars, strWindowPath);
    } else pageErrorPopUp(response);
  }


  String processButton(VariablesSecureApp vars, String strCloseFactAcctGroupId, String windowId) {
    try {
      DropCloseFactAcctData.updatePeriods(this, strCloseFactAcctGroupId);
      DropCloseFactAcctData.deleteFactAcct(this, strCloseFactAcctGroupId);
      return Utility.messageBD(this, "ProcessOK", vars.getLanguage());
    } catch (ServletException e) {
      log4j.warn(e);
      return Utility.messageBD(this, "ProcessRunError", vars.getLanguage());
    }
  }


  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey, String strCloseFactAcctGroupId, String windowId, String strTab, String strProcessId) throws IOException, ServletException {
      if (log4j.isDebugEnabled()) log4j.debug("Output: Button process Create Close Fact Acct");

      ActionButtonDefaultData[] data = null;
      String strHelp="", strDescription="";
      if (vars.getLanguage().equals("en_US")) data = ActionButtonDefaultData.select(this, strProcessId);
      else data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);

      if (data!=null && data.length!=0) {
        strDescription = data[0].description;
        strHelp = data[0].help;
      }
      String[] discard = {""};
      if (strHelp.equals("")) discard[0] = new String("helpDiscard");
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/DropCloseFactAcct", discard).createXmlDocument();
      xmlDocument.setParameter("key", strKey);
      xmlDocument.setParameter("window", windowId);
      xmlDocument.setParameter("tab", strTab);
      xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("question", Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
      xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("description", strDescription);
      xmlDocument.setParameter("help", strHelp);


      xmlDocument.setData("reportCloseFactAcctGroupId", "liststructure", DropCloseFactAcctData.select(this,strKey));

      xmlDocument.setParameter("CloseFactAcctGroupId", strCloseFactAcctGroupId);


      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }

  public String getServletInfo() {
    return "Servlet Drop reg fact acct";
  } // end of getServletInfo() method
}

