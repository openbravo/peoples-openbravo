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
import org.openbravo.erpCommon.utility.ComboTableData;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.Replace;



public class Account extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "Account.combination");
      String strAcctSchema = vars.getRequestGlobalVariable("inpAcctSchema", "Account.cAcctschemaId");
      if (strAcctSchema.equals("")) {
        strAcctSchema = Utility.getContext(this, vars, "$C_AcctSchema_ID", "Account");
        vars.setSessionValue("Account.cAcctschemaId", strAcctSchema);
      }
      vars.removeSessionValue("Account.alias");
      if (!strNameValue.equals("")) vars.setSessionValue("Account.combination", strNameValue + "%");
      printPageFS(response, vars);
    } else if (vars.commandIn("LOAD_FIELD")) {
      String strClave = vars.getStringParameter("inpClave");
      printPageFrame1(response, vars, "", "", strClave, false);
    } else if (vars.commandIn("KEY")) {
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "Account.alias");
      String strAcctSchema = vars.getRequestGlobalVariable("inpAcctSchema", "Account.cAcctschemaId");
      if (strAcctSchema.equals("")) {
        strAcctSchema = Utility.getContext(this, vars, "$C_AcctSchema_ID", "Account");
        vars.setSessionValue("Account.cAcctschemaId", strAcctSchema);
      }
      vars.removeSessionValue("Account.combination");
      vars.setSessionValue("Account.alias", strKeyValue + "%");
      AccountData[] data = AccountData.selectKey(this, strAcctSchema, Utility.getContext(this, vars, "#User_Client", "Account"), Utility.getContext(this, vars, "#User_Org", "Account"), strKeyValue + "%");
      if (data!=null && data.length==1) {
        printPageKey(response, vars, data);
      } else printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strAlias = vars.getGlobalVariable("inpAlias", "Account.alias", "");
      String strCombination = vars.getGlobalVariable("inpCombination", "Account.combination", "");
      printPageFrame1(response, vars, strAlias, strCombination, "", true);
    } else if (vars.commandIn("FRAME2")) {
      String strAlias = vars.getGlobalVariable("inpAlias", "Account.alias", "");
      String strCombination = vars.getGlobalVariable("inpCombination", "Account.combination", "");
      String strOrganization = vars.getStringParameter("inpOrganization");
      String strAccount = vars.getStringParameter("inpAccount");
      String strProduct = vars.getStringParameter("inpProduct");
      String strBPartner = vars.getStringParameter("inpBPartner");
      String strProject = vars.getStringParameter("inpProject");
      String strCampaign = vars.getStringParameter("inpCampaign");
      printPageFrame2(response, vars, strAlias, strCombination, strOrganization, strAccount, strProduct, strBPartner, strProject, strCampaign);
    } else if (vars.commandIn("FIND")) {
      String strAlias = vars.getRequestGlobalVariable("inpAlias", "Account.alias");
      String strCombination = vars.getRequestGlobalVariable("inpCombination", "Account.combination");
      String strOrganization = vars.getStringParameter("inpOrganization");
      String strAccount = vars.getStringParameter("inpAccount");
      String strProduct = vars.getStringParameter("inpProduct");
      String strBPartner = vars.getStringParameter("inpBPartner");
      String strProject = vars.getStringParameter("inpProject");
      String strCampaign = vars.getStringParameter("inpCampaign");

      vars.setSessionValue("Account.initRecordNumber", "0");
      printPageFrame2(response, vars, strAlias, strCombination, strOrganization, strAccount, strProduct, strBPartner, strProject, strCampaign);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("SAVE")) {
      String strAcctSchema = vars.getSessionValue("Account.cAcctschemaId");
      String strClave = vars.getStringParameter("inpValidCombination");
      String strAlias = vars.getRequestGlobalVariable("inpAlias", "Account.alias");
      String strOrganization = vars.getRequiredStringParameter("inpOrganization");
      String strAccount = vars.getRequiredStringParameter("inpAccount");
      String strProduct = vars.getStringParameter("inpProduct");
      String strBPartner = vars.getStringParameter("inpBPartner");
      String strProject = vars.getStringParameter("inpProject");
      String strCampaign = vars.getStringParameter("inpCampaign");
      AccountData data = AccountData.insert(this, vars.getClient(), strOrganization, strAcctSchema, strAccount, strClave, strAlias, vars.getUser(), strProduct, strBPartner, strProject, strCampaign);
      if (data!=null) strClave = data.cValidcombinationId;
      vars.removeSessionValue("Account.alias");
      vars.setSessionValue("Account.combination", AccountData.combination(this, strClave));
      printPageFS(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("Account.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Account");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("Account.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("Account.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("Account.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Account");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("Account.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Account seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Account_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, AccountData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Account seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(AccountData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();

    html.append("\nfunction depurarSelector() {\n");
    html.append("var clave = \"" + data[0].cValidcombinationId + "\";\n");
    html.append("var texto = \"" + Replace.replace(data[0].combination, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", clave, texto, null);\n");
    html.append("}\n");
    return html.toString();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strAlias, String strCombination, String strValidCombination, boolean isDefault) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the accounts seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Account_F1").createXmlDocument();
    AccountData[] data = null;
    if (isDefault) {
      if (strAlias.equals("") && strCombination.equals("")) strAlias = "%";
      data = AccountData.set(strAlias, strCombination);
    } else {
      data = AccountData.select(this, "", "", "", "", "", "", "", "", "", strValidCombination, Utility.getContext(this, vars, "#User_Client", "Account"), Utility.getContext(this, vars, "#User_Org", "Account"));
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setData("structure1", data);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "C_Campaign_ID", "C_Campaign", "", Utility.getContext(this, vars, "#User_Org", "Account"), Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData("reportC_Campaign_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "C_Project_ID", "C_Project", "", Utility.getContext(this, vars, "#User_Org", "Account"), Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData("reportC_Project_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "AD_Org_ID", "AD_Org (Trx)", "", Utility.getContext(this, vars, "#User_Org", "Account"), Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData("reportAD_Org_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "Account_ID", "C_ElementValue (Accounts)", "", Utility.getContext(this, vars, "#User_Org", "Account"), Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData("reportAccount_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "M_Product_ID", "M_Product (no summary)", "", Utility.getContext(this, vars, "#User_Org", "Account"), Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData("reportM_Product_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }


    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "C_BPartner_ID", "C_BPartner", "", Utility.getContext(this, vars, "#User_Org", "Account"), Utility.getContext(this, vars, "#User_Client", "Account"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "Account", "");
      xmlDocument.setData("reportC_BPartner_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strAlias, String strCombination, String strOrganization, String strAccount, String strProduct, String strBPartner, String strProject, String strCampaign) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the accounts seeker");
    XmlDocument xmlDocument;
    String strAcctSchema = vars.getSessionValue("Account.cAcctschemaId");

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "Account");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("Account.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strAlias.equals("") && strCombination.equals("") && strOrganization.equals("") && strAccount.equals("") && strProduct.equals("") && strBPartner.equals("") && strProject.equals("") && strCampaign.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Account_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", AccountData.set(strAlias, strCombination));
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      AccountData[] data = AccountData.select(this, strAcctSchema, strAlias, strCombination, strOrganization, strAccount, strProduct, strBPartner, strProject, strCampaign, "", Utility.getContext(this, vars, "#User_Client", "Account"), Utility.getContext(this, vars, "#User_Org", "Account"), initRecordNumber, intRecordRange);
      if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Account_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", data);
    }

    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the business partners seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/Account_F3").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents que accounts seeker";
  } // end of getServletInfo() method
}
