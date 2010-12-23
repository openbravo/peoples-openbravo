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
 * All portions are Copyright (C) 2001-2008 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class Test_In extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  static int WARNING;
  static int YES_NO_OPTION;
  static int NO;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strKey = vars.getRequiredGlobalVariable("inpatTestId", strWindow + "|AT_Test_ID");
      printPage(response, vars, strKey, strWindow, strTab, strProcessId);
    } else if (vars.commandIn("SAVE")) {
      TestLine tl = new TestLine(vars, "inpFile", false, "Aplicación de gestión", "", false, this);
      String strWindow = vars.getStringParameter("inpwindowId");
      String strKey = vars.getStringParameter("inpatTestId");
      vars.setSessionValue(strWindow + "|AT_Test_ID", strKey);
      String strTab = vars.getStringParameter("inpTabId");

      String strWindowPath = Utility.getTabURL(strTab, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myMessage = processButton(vars, tl.getFieldProvider(), strKey, strWindow);
      vars.setMessage(strTab, myMessage);
      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  OBError processButton(VariablesSecureApp vars, FieldProvider[] dataLineas, String strTestId,
      String windowId) {
    OBError myMessage = null;
    Connection conn = null;
    if (dataLineas == null) {
      return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    String strCommandId = "";
    String strType = "C";
    String strLineId = "";
    int intSeqNo = 10;
    try {
      conn = getTransactionConnection();
      if (!TestInData.countLines(this, strTestId).equals("0")) {
        return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
      }
      for (int i = 0; i < dataLineas.length; i++) {
        strCommandId = dataLineas[i].getField("comando");
        if (strCommandId != null) {
          TestInData[] datacomando = TestInData.select(conn, this, strCommandId);
          strLineId = SequenceIdData.getUUID();
          try {
            TestInData.insert(conn, this, strLineId, vars.getClient(), vars.getOrg(), vars
                .getUser(), strTestId, strCommandId, String.valueOf(intSeqNo), dataLineas[i]
                .getField("arg1"), dataLineas[i].getField("arg2"), dataLineas[i].getField("arg3"),
                strType, datacomando[0].argno, datacomando[0].arghelp1, datacomando[0].arghelp2,
                datacomando[0].arghelp3);
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
          intSeqNo = intSeqNo + 10;
        }
      }

      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (Exception e) {
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String windowId, String strTab, String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: Button process Project Close");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);

    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "" };
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/Test_In", discard).createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("question", Utility.messageBD(this, "StartProcess?", vars
        .getLanguage()));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Project set Type";
  } // end of getServletInfo() method
}
