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

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.exception.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import org.openbravo.data.*;

// imports for transactions
import java.sql.Connection;


public class Test_In extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  
  static int WARNING;
  static int YES_NO_OPTION;
  static int NO;

  
  //static String currentWindow = "Application managemente";
  //static String currentFrame= "";
  Connection conn;


  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      conn = getTransactionConnection();
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@" + ex2.getMessage());
    }
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
      ActionButtonDefaultData[] tab = ActionButtonDefaultData.windowName(this, strTab);
      String strWindowPath="", strTabName="";
      if (tab!=null && tab.length!=0) {
        strTabName = FormatUtilities.replace(tab[0].name);
        strWindowPath = "../" + FormatUtilities.replace(tab[0].description) + "/" + strTabName + "_Relation.html";
      } else strWindowPath = strDefaultServlet;
      String messageResult = processButton(vars, tl.getFieldProvider(), strKey, strWindow);
      vars.setSessionValue(strWindow + "|" + strTabName + ".message", messageResult);
      printPageClosePopUp(response, vars, strWindowPath);
    } else pageErrorPopUp(response);
  }


  String processButton(VariablesSecureApp vars, FieldProvider[] dataLineas, String strTestId, String windowId) {

    if (dataLineas==null) return "";
    String strCommandId="";
    String strType = "C";
    String strLineId ="";
    int intSeqNo = 10;
    try {
      if (!TestInData.countLines(this, strTestId).equals("0")) return "Las lineas del test ya han sido importadas previamente";
      for (int i=0;i<dataLineas.length;i++) {
        strCommandId = dataLineas[i].getField("comando");
        if (strCommandId != null){
          TestInData[] datacomando = TestInData.select(conn, this, strCommandId);
          strLineId = SequenceIdData.getSequence(this, "AT_Line", vars.getClient());
          TestInData.insert(this, strLineId, vars.getClient(), vars.getOrg(), vars.getUser(), strTestId, strCommandId, String.valueOf(intSeqNo), dataLineas[i].getField("arg1"), dataLineas[i].getField("arg2"), dataLineas[i].getField("arg3"), strType, datacomando[0].argno, datacomando[0].arghelp1, datacomando[0].arghelp2, datacomando[0].arghelp3);
          intSeqNo = intSeqNo + 10;
        }
      }
      return Utility.messageBD(this, "ProcessOK", vars.getLanguage());
    } catch (Exception e) {
      e.printStackTrace();
      log4j.warn(e);
      return Utility.messageBD(this, "ProcessRunError", vars.getLanguage());
    }
  }


  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey, String windowId, String strTab, String strProcessId)
    throws IOException, ServletException {
      if (log4j.isDebugEnabled()) log4j.debug("Output: Button process Project Close");

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
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/Test_In", discard).createXmlDocument();
      xmlDocument.setParameter("key", strKey);
      xmlDocument.setParameter("window", windowId);
      xmlDocument.setParameter("tab", strTab);
      xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("question", Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
      xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
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
