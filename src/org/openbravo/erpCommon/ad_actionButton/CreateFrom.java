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
package org.openbravo.erpCommon.ad_actionButton;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.ad_reports.ReportTrialBalanceData;
import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openbravo.utils.Replace;
import java.math.BigDecimal;

// imports for transactions
import java.sql.Connection;


public class CreateFrom extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  static final BigDecimal ZERO = new BigDecimal(0.0);


  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strKey = vars.getGlobalVariable("inpKey", "CreateFrom|key");
      String strTableId = vars.getGlobalVariable("inpTableId", "CreateFrom|tableId");
      String strProcessId = vars.getGlobalVariable("inpProcessId", "CreateFrom|processId", "");
      String strPath = vars.getGlobalVariable("inpPath", "CreateFrom|path", strDireccion + request.getServletPath());
      String strWindowId = vars.getGlobalVariable("inpWindowId", "CreateFrom|windowId", "");
      String strTabName = vars.getGlobalVariable("inpTabName", "CreateFrom|tabName", "");
      String strDateInvoiced = vars.getGlobalVariable("inpDateInvoiced", "CreateFrom|dateInvoiced", "");
      String strBPartnerLocation = vars.getGlobalVariable("inpcBpartnerLocationId", "CreateFrom|bpartnerLocation", "");
      String strMPriceList = vars.getGlobalVariable("inpMPricelist", "CreateFrom|pricelist", "");
      String strBPartner = vars.getGlobalVariable("inpcBpartnerId", "CreateFrom|bpartner", "");
      String strStatementDate = vars.getGlobalVariable("inpstatementdate", "CreateFrom|statementDate", "");
      String strBankAccount = vars.getGlobalVariable("inpcBankaccountId", "CreateFrom|bankAccount", "");
      String strOrg = vars.getGlobalVariable("inpadOrgId", "CreateFrom|adOrgId", "");
      String strIsreceipt = vars.getGlobalVariable("inpisreceipt", "CreateFrom|isreceipt", "");
      
      if (log4j.isDebugEnabled()) log4j.debug("doPost - inpadOrgId = " + strOrg);
      if (log4j.isDebugEnabled()) log4j.debug("doPost - inpisreceipt = " + strIsreceipt);
      
      vars.removeSessionValue("CreateFrom|key");
      vars.removeSessionValue("CreateFrom|processId");
      vars.removeSessionValue("CreateFrom|path");
      vars.removeSessionValue("CreateFrom|windowId");
      vars.removeSessionValue("CreateFrom|tabName");
      vars.removeSessionValue("CreateFrom|dateInvoiced");
      vars.removeSessionValue("CreateFrom|bpartnerLocation");
      vars.removeSessionValue("CreateFrom|pricelist");
      vars.removeSessionValue("CreateFrom|bpartner");
      vars.removeSessionValue("CreateFrom|statementDate");
      vars.removeSessionValue("CreateFrom|bankAccount");
      vars.removeSessionValue("CreateFrom|adOrgId");
      vars.removeSessionValue("CreateFrom|isreceipt");
      
      // 26-06-07
      vars.setSessionValue("CreateFrom|default","1");

      printPage_FS(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strDateInvoiced, strBPartnerLocation, strMPriceList, strBPartner, strStatementDate, strBankAccount, strOrg, strIsreceipt);
    } else if (vars.commandIn("FRAME1")) {
      String strTableId = vars.getGlobalVariable("inpTableId", "CreateFrom|tableId");
      String strType = pageType(strTableId);
      String strKey = vars.getGlobalVariable("inpKey", "CreateFrom" + strType + "|key");
      String strProcessId = vars.getGlobalVariable("inpProcessId", "CreateFrom" + strType + "|processId", "");
      String strPath = vars.getGlobalVariable("inpPath", "CreateFrom" + strType + "|path", strDireccion + request.getServletPath());
      String strWindowId = vars.getGlobalVariable("inpWindowId", "CreateFrom" + strType + "|windowId");
      String strTabName = vars.getGlobalVariable("inpTabName", "CreateFrom" + strType + "|tabName");
      String strDateInvoiced = vars.getGlobalVariable("inpDateInvoiced", "CreateFrom" + strType + "|dateInvoiced", "");
      String strBPartnerLocation = vars.getGlobalVariable("inpcBpartnerLocationId", "CreateFrom" + strType + "|bpartnerLocation", "");
      String strPriceList = vars.getGlobalVariable("inpMPricelist", "CreateFrom" + strType + "|pricelist", "");
      String strBPartner = vars.getGlobalVariable("inpcBpartnerId", "CreateFrom" + strType + "|bpartner", "");
      String strStatementDate = vars.getGlobalVariable("inpstatementdate", "CreateFrom" + strType + "|statementDate", "");
      String strBankAccount = vars.getGlobalVariable("inpcBankaccountId", "CreateFrom" + strType + "|bankAccount", "");
      String strOrg = vars.getGlobalVariable("inpadOrgId", "CreateFrom" + strType + "|adOrgId", "");
      String strIsreceipt = vars.getGlobalVariable("inpisreceipt", "CreateFrom" + strType + "|isreceipt", "");
      
      if (log4j.isDebugEnabled()) log4j.debug("doPost - inpadOrgId = " + strOrg);
      if (log4j.isDebugEnabled()) log4j.debug("doPost - inpisreceipt = " + strIsreceipt);

      vars.removeSessionValue("CreateFrom" + strType + "|key");
      vars.removeSessionValue("CreateFrom|tableId");
      vars.removeSessionValue("CreateFrom" + strType + "|processId");
      vars.removeSessionValue("CreateFrom" + strType + "|path");
      vars.removeSessionValue("CreateFrom" + strType + "|windowId");
      vars.removeSessionValue("CreateFrom" + strType + "|tabName");
      vars.removeSessionValue("CreateFrom" + strType + "|dateInvoiced");
      vars.removeSessionValue("CreateFrom" + strType + "|bpartnerLocation");
      vars.removeSessionValue("CreateFrom" + strType + "|pricelist");
      vars.removeSessionValue("CreateFrom" + strType + "|bpartner");
      vars.removeSessionValue("CreateFrom" + strType + "|statementDate");
      vars.removeSessionValue("CreateFrom" + strType + "|bankAccount");
      vars.removeSessionValue("CreateFrom" + strType + "|adOrgId");
      vars.removeSessionValue("CreateFrom" + strType + "|isreceipt");

      callPrintPage(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strDateInvoiced, strBPartnerLocation, strPriceList, strBPartner, strStatementDate, strBankAccount, strOrg, strIsreceipt);
    } else if (vars.commandIn("FIND_PO", "FIND_INVOICE", "FIND_SHIPMENT", "FIND_BANK", "FIND_SETTLEMENT")) {
      String strKey = vars.getRequiredStringParameter("inpKey");
      String strTableId = vars.getStringParameter("inpTableId");
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strPath = vars.getStringParameter("inpPath", strDireccion + request.getServletPath());
      String strWindowId = vars.getStringParameter("inpWindowId");
      String strTabName = vars.getStringParameter("inpTabName");
      String strDateInvoiced = vars.getStringParameter("inpDateInvoiced");
      String strBPartnerLocation = vars.getStringParameter("inpcBpartnerLocationId");
      String strPriceList = vars.getStringParameter("inpMPricelist");
      String strBPartner = vars.getStringParameter("inpcBpartnerId");
      String strStatementDate = vars.getStringParameter("inpstatementdate");
      String strBankAccount = vars.getStringParameter("inpcBankaccountId");
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strIsreceipt = vars.getStringParameter("inpisreceipt");
      if (log4j.isDebugEnabled()) log4j.debug("doPost - inpadOrgId = " + strOrg);
      if (log4j.isDebugEnabled()) log4j.debug("doPost - inpisreceipt = " + strIsreceipt);

      callPrintPage(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strDateInvoiced, strBPartnerLocation, strPriceList, strBPartner, strStatementDate, strBankAccount, strOrg, strIsreceipt);
    } else if (vars.commandIn("REFRESH_INVOICES", "REFRESH_SHIPMENTS")) {
      String strBPartner = vars.getStringParameter("inpcBpartnerId");
      String strWindowId = vars.getStringParameter("inpWindowId");
      printPageInvoiceCombo(response, vars, strBPartner, strWindowId);
    } else if (vars.commandIn("SAVE")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strKey = vars.getRequiredStringParameter("inpKey");
      String strTableId = vars.getStringParameter("inpTableId");
      String strWindowId = vars.getStringParameter("inpWindowId");
      OBError myMessage = saveMethod(vars, strKey, strTableId, strProcessId, strWindowId);
      String strTabId = vars.getGlobalVariable("inpTabId", "CreateFrom|tabId");
      vars.setMessage(strTabId, myMessage);
      printPageClosePopUp(response, vars);
      //response.sendRedirect(strPath);
    } else pageErrorPopUp(response);
  }


  void printPage_FS(HttpServletResponse response, VariablesSecureApp vars, String strPath, String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName, String strDateInvoiced, String strBPartnerLocation, String strPriceList, String strBPartner, String strStatementDate, String strBankAccount, String strOrg, String strIsreceipt) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: FrameSet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_FS").createXmlDocument();
    String strType = pageType(strTableId);
    vars.setSessionValue("CreateFrom" + strType + "|path", strPath);
    vars.setSessionValue("CreateFrom" + strType + "|key", strKey);
    vars.setSessionValue("CreateFrom" + strType + "|processId", strProcessId);
    vars.setSessionValue("CreateFrom" + strType + "|windowId", strWindowId);
    vars.setSessionValue("CreateFrom" + strType + "|tabName", strTabName);
    vars.setSessionValue("CreateFrom" + strType + "|dateInvoiced", strDateInvoiced);
    vars.setSessionValue("CreateFrom" + strType + "|bpartnerLocation", strBPartnerLocation);
    vars.setSessionValue("CreateFrom" + strType + "|pricelist", strPriceList);
    vars.setSessionValue("CreateFrom" + strType + "|bpartner", strBPartner);
    vars.setSessionValue("CreateFrom" + strType + "|statementDate", strStatementDate);
    vars.setSessionValue("CreateFrom" + strType + "|bankAccount", strBankAccount);
    vars.setSessionValue("CreateFrom" + strType + "|adOrgId", strOrg);
    vars.setSessionValue("CreateFrom" + strType + "|isreceipt", strIsreceipt);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String pageType(String strTableId) {
    if (strTableId.equals("392")) return "Bank";
    else if (strTableId.equals("318")) return "Invoice";
    else if (strTableId.equals("319")) return "Shipment";
    else if (strTableId.equals("426")) return "Pay";
    else if (strTableId.equals("800019")) return "Settlement";
    else return "";
  }

  void callPrintPage(HttpServletResponse response, VariablesSecureApp vars, String strPath, String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName, String strDateInvoiced, String strBPartnerLocation, String strPriceList, String strBPartner, String strStatementDate, String strBankAccount, String strOrg, String strIsreceipt) throws IOException, ServletException {
    if (strTableId.equals("392")) { //C_BankStatement
      printPageBank(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strStatementDate, strBankAccount);
    } else if (strTableId.equals("318")) { //C_Invoice
      printPageInvoice(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strDateInvoiced, strBPartnerLocation, strPriceList, strBPartner);
    } else if (strTableId.equals("319")) { //M_InOut
      printPageShipment(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strBPartner);
    } else if (strTableId.equals("426")) { //C_PaySelection
      printPagePay(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strBPartner);
    } else if (strTableId.equals("800019")) { //C_Settlement
      printPageSettlement(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strBPartner);
    } else if (strTableId.equals("800176")) { //C_DP_Management
      printPageDPManagement(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strBPartner);
    } else if (strTableId.equals("800179")) { //C_Remittance
      printPageCRemittance(response, vars, strPath, strKey, strTableId, strProcessId, strWindowId, strTabName, strBPartner, strOrg, strIsreceipt);
    }else {
      pageError(response);
    }
  }

  void printPageBank(HttpServletResponse response, VariablesSecureApp vars, String strPath, String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName, String strStatementDate, String strBank) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Bank");
    String strcBPartner = vars.getStringParameter("inpcBpartnerId");
    String strPaymentRule = vars.getStringParameter("inppaymentrule");
    String strPlannedDateFrom = vars.getStringParameter("inpplanneddateFrom");
    String strPlannedDateTo = vars.getStringParameter("inpplanneddateTo");
    String strAmountFrom = vars.getStringParameter("inpamountFrom");
    String strAmountTo = vars.getStringParameter("inpamountTo");
    String strIsReceipt = vars.getStringParameter("inpisreceipt");
    String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    String strBankAccount = vars.getStringParameter("inpcBankaccountId");
    String strOrg = vars.getStringParameter("inpadOrgId");
    String strCharge = vars.getStringParameter("inpCharge");
    String strPlannedDate = vars.getStringParameter("inpplanneddate", strStatementDate);
    String strCost = vars.getStringParameter("inpcost", "0.00");
    String strDocumentNo = vars.getStringParameter("inpDocumentNo");
    CreateFromBankData[] data = null;
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_Bank").createXmlDocument();
    if (strcBPartner.equals("") && strPaymentRule.equals("") && strPlannedDateFrom.equals("") && strPlannedDateTo.equals("") && strIsReceipt.equals("") && strBankAccount.equals("") && strOrg.equals("")) {
      data = new CreateFromBankData[0];
    } else {
      if (vars.commandIn("FRAME1") && strIsReceipt.equals("")) strIsReceipt = isSOTrx;
      data = CreateFromBankData.select(this, vars.getLanguage(), strStatementDate, Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule, strPlannedDateFrom, strPlannedDateTo, strAmountFrom, strAmountTo, strIsReceipt, strBank, strOrg, strCharge, strDocumentNo);
    }
    if (vars.commandIn("FRAME1") && strIsReceipt.equals("")) strIsReceipt = isSOTrx;

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);
    xmlDocument.setParameter("statementDate", strStatementDate);
    xmlDocument.setParameter("paramCBankaccountID", strBank);
    xmlDocument.setParameter("paramPlannedDate", strPlannedDate);
    xmlDocument.setParameter("paramplanneddate", strPlannedDate);
    xmlDocument.setParameter("paramcost", strCost);
    xmlDocument.setParameter("documentNo", strDocumentNo);

    xmlDocument.setParameter("paymentRule", strPaymentRule);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "All_Payment Rule", "", Utility.getContext(this, vars, "#User_Org", "CreateFrom"), Utility.getContext(this, vars, "#User_Client", "CreateFrom"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateFrom", strPaymentRule);
      xmlDocument.setData("reportPaymentRule","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter("cbpartnerId", strcBPartner);

    xmlDocument.setParameter("cbpartnerId_DES", CreateFromBankData.bpartner(this, strcBPartner));
    xmlDocument.setParameter("plannedDateFrom", strPlannedDateFrom);
    xmlDocument.setParameter("plannedDateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTo", strPlannedDateTo);
    xmlDocument.setParameter("plannedDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("amountFrom", strAmountFrom);
    {
      OBError myMessage = vars.getMessage("CreateFrom");
      vars.removeMessage("CreateFrom");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    xmlDocument.setParameter("amountTo", strAmountTo);
    xmlDocument.setParameter("isreceiptPago", strIsReceipt);
    xmlDocument.setParameter("isreceiptCobro", strIsReceipt);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("charge", strCharge);


	try {
		ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_BankAccount_ID", "", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
		Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strBank);
		xmlDocument.setData("reportC_BankAccount_ID","liststructure", comboTableData.select(false));
		comboTableData = null;
	} catch (Exception ex) {
		throw new ServletException(ex);
	}


	try {
		ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
		Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strOrg);
		xmlDocument.setData("reportAD_Org_ID","liststructure", comboTableData.select(false));
		comboTableData = null;
	} catch (Exception ex) {
		throw new ServletException(ex);
	}

    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageInvoice(HttpServletResponse response, VariablesSecureApp vars, String strPath, String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName, String strDateInvoiced, String strBPartnerLocation, String strPriceList, String strBPartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Invoice");
    CreateFromInvoiceData[] data = null;
    XmlDocument xmlDocument;
    String strPO = vars.getStringParameter("inpPurchaseOrder");
    String strShipment = vars.getStringParameter("inpShipmentReciept");
    String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    if (vars.commandIn("FIND_PO")) strShipment="";
    else if (vars.commandIn("FIND_SHIPMENT")) strPO="";
    if (strPO.equals("") && strShipment.equals("")) {
      String[] discard = {"sectionDetail"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_Invoice", discard).createXmlDocument();
      data=CreateFromInvoiceData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_Invoice").createXmlDocument();
      if (strShipment.equals("")) {
        if (vars.getLanguage().equals("en_US")) {
          if (isSOTrx.equals("Y")) data=CreateFromInvoiceData.selectFromPOSOTrx(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
          else data=CreateFromInvoiceData.selectFromPO(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
        } else {
          if (isSOTrx.equals("Y")) data=CreateFromInvoiceData.selectFromPOTrlSOTrx(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
          else data=CreateFromInvoiceData.selectFromPOTrl(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
        }
      } else {
        if (vars.getLanguage().equals("en_US")) {
          if (isSOTrx.equals("Y")) data=CreateFromInvoiceData.selectFromShipmentSOTrx(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strShipment);
          else data=CreateFromInvoiceData.selectFromShipment(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strShipment);
        } else {
          if (isSOTrx.equals("Y")) data=CreateFromInvoiceData.selectFromShipmentTrlSOTrx(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strShipment);
          else data=CreateFromInvoiceData.selectFromShipmentTrl(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strShipment);
        }
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("dateInvoiced", strDateInvoiced);
    xmlDocument.setParameter("bpartnerLocation", strBPartnerLocation);
    xmlDocument.setParameter("pricelist", strPriceList);
    xmlDocument.setParameter("cBpartnerId", strBPartner);
    xmlDocument.setParameter("BPartnerDescription", CreateFromShipmentData.selectBPartner(this, strBPartner));
    xmlDocument.setParameter("PurchaseOrder", strPO);
    xmlDocument.setParameter("Shipment", strShipment);
    xmlDocument.setParameter("pType", (!strShipment.equals("")?"SHIPMENT":(!strPO.equals(""))?"PO":""));
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);


    if (strBPartner.equals("")) {
      xmlDocument.setData("reportShipmentReciept", "liststructure", new CreateFromInvoiceData[0]);
      xmlDocument.setData("reportPurchaseOrder", "liststructure", new CreateFromInvoiceData[0]);
    } else {
      if (isSOTrx.equals("Y")) {
        xmlDocument.setData("reportShipmentReciept", "liststructure", CreateFromInvoiceData.selectFromShipmentSOTrxCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        xmlDocument.setData("reportPurchaseOrder", "liststructure", CreateFromInvoiceData.selectFromPOSOTrxCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
      } else {
        xmlDocument.setData("reportShipmentReciept", "liststructure", CreateFromInvoiceData.selectFromShipmentCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        xmlDocument.setData("reportPurchaseOrder", "liststructure", CreateFromInvoiceData.selectFromPOCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
      }
    }
    {
      OBError myMessage = vars.getMessage("CreateFrom");
      vars.removeMessage("CreateFrom");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageShipment(HttpServletResponse response, VariablesSecureApp vars, String strPath, String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName, String strBPartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Shipment");
    CreateFromShipmentData[] data = null;
    XmlDocument xmlDocument;
    String strPO = vars.getStringParameter("inpPurchaseOrder");
    String strInvoice = vars.getStringParameter("inpInvoice");
    String strLocator = vars.getStringParameter("inpmLocatorId");
    String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    if (vars.commandIn("FIND_PO")) strInvoice="";
    else if (vars.commandIn("FIND_INVOICE")) strPO="";
    if (strPO.equals("") && strInvoice.equals("")) {
      String[] discard = {"sectionDetail"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_Shipment", discard).createXmlDocument();
      data=CreateFromShipmentData.set();
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_Shipment").createXmlDocument();
      if (strInvoice.equals("")) {
        if (vars.getLanguage().equals("en_US")) {
          if (isSOTrx.equals("Y")) data=CreateFromShipmentData.selectFromPOSOTrx(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
          else data=CreateFromShipmentData.selectFromPO(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
        } else {
          if (isSOTrx.equals("Y")) data=CreateFromShipmentData.selectFromPOTrlSOTrx(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
          else data=CreateFromShipmentData.selectFromPOTrl(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strPO);
        }
      } else {
        if (vars.getLanguage().equals("en_US")) {
          if (isSOTrx.equals("Y")) data = new CreateFromShipmentData[0];
          else data=CreateFromShipmentData.selectFromInvoice(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strInvoice);
        } else {
          if (isSOTrx.equals("Y")) data = new CreateFromShipmentData[0];
          else data=CreateFromShipmentData.selectFromInvoiceTrl(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strInvoice);
        }
      }
    }


    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("cBpartnerId", strBPartner);
    xmlDocument.setParameter("BPartnerDescription", CreateFromShipmentData.selectBPartner(this, strBPartner));
    xmlDocument.setParameter("PurchaseOrder", strPO);
    xmlDocument.setParameter("M_Locator_ID", strLocator);
    xmlDocument.setParameter("M_Locator_ID_DES", CreateFromShipmentData.selectLocator(this, strLocator));
    xmlDocument.setParameter("Invoice", strInvoice);
    xmlDocument.setParameter("pType", (!strInvoice.equals("")?"INVOICE":(!strPO.equals(""))?"PO":""));
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);

    if (strBPartner.equals("")) {
      xmlDocument.setData("reportInvoice", "liststructure", new CreateFromShipmentData[0]);
      xmlDocument.setData("reportPurchaseOrder", "liststructure", new CreateFromShipmentData[0]);
    } else {
      if (isSOTrx.equals("Y")) {
        xmlDocument.setData("reportInvoice", "liststructure", new CreateFromShipmentData[0]);
        xmlDocument.setData("reportPurchaseOrder", "liststructure", CreateFromShipmentData.selectFromPOSOTrxCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
      } else {
        xmlDocument.setData("reportInvoice", "liststructure", CreateFromShipmentData.selectFromInvoiceCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        xmlDocument.setData("reportPurchaseOrder", "liststructure", CreateFromShipmentData.selectFromPOCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
      }
    }

    {
      OBError myMessage = vars.getMessage("CreateFrom");
      vars.removeMessage("CreateFrom");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setData("structure1", data);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageInvoiceCombo(HttpServletResponse response, VariablesSecureApp vars, String strBPartner, String strWindowId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Refresh Invoices");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_F0").createXmlDocument();
    String strArray="";
    String strArray2 = "";
    String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);

    if (strBPartner.equals("")) {
      strArray = arrayEntradaSimple("arrDatos", new CreateFromShipmentData[0]);
      strArray2 = arrayEntradaSimple("arrDatos2", new CreateFromShipmentData[0]);
    } else {
      if (vars.commandIn("REFRESH_INVOICES")) { //Loading the combos in the delivery note's CreateFrom
        if (isSOTrx.equals("Y")) {
          strArray = arrayEntradaSimple("arrDatos", new CreateFromShipmentData[0]);
          strArray2 = arrayEntradaSimple("arrDatos2", CreateFromShipmentData.selectFromPOSOTrxCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        } else {
          strArray = arrayEntradaSimple("arrDatos", CreateFromShipmentData.selectFromInvoiceCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
          strArray2 = arrayEntradaSimple("arrDatos2", CreateFromShipmentData.selectFromPOCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        }
      } else { //Loading the Combos in the Invoice's CreateFrom
        if (isSOTrx.equals("Y")) {
          strArray = arrayEntradaSimple("arrDatos", CreateFromInvoiceData.selectFromShipmentSOTrxCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
          strArray2 = arrayEntradaSimple("arrDatos2", CreateFromInvoiceData.selectFromPOSOTrxCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        } else {
          strArray = arrayEntradaSimple("arrDatos", CreateFromInvoiceData.selectFromShipmentCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
          strArray2 = arrayEntradaSimple("arrDatos2", CreateFromInvoiceData.selectFromPOCombo(this, vars.getLanguage(), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strBPartner));
        }
      }
    }

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("array", strArray + "\n" + strArray2);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPagePay(HttpServletResponse response, VariablesSecureApp vars, String strPath, String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName, String strBPartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Pay");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_Pay").createXmlDocument();

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageSettlement(HttpServletResponse response, VariablesSecureApp vars, String strPath, String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName, String strBPartner) throws IOException, ServletException {
    
    if (log4j.isDebugEnabled()) log4j.debug("Output: Settlement");
    if (log4j.isDebugEnabled()) log4j.debug(vars.commandIn("DEFAULT"));
    
    String strcBPartner = vars.getStringParameter("inpcBpartnerId");
    String strPaymentRule = vars.getStringParameter("inppaymentrule");
    String strPlannedDateFrom = vars.getStringParameter("inpplanneddateFrom");
    String strPlannedDateTo = vars.getStringParameter("inpplanneddateTo");
    String strAmountFrom = vars.getStringParameter("inpamountFrom");
    String strAmountTo = vars.getStringParameter("inpamountTo");
    String strTotalAmount = vars.getStringParameter("inpamount");
    String strIsReceipt = vars.getStringParameter("inpisreceipt");
    if (log4j.isDebugEnabled()) log4j.debug("IsReceipt: "+strIsReceipt);
    String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    String strAutoCalc = vars.getStringParameter("inpAutoClaculated");
    String strAutoCalcSelect = "AMOUNT";
    
    if (strAutoCalc.equals("")) strAutoCalcSelect = "WRITEOFFAMT";

    String strOrg = vars.getStringParameter("inpadOrgId");
    String strMarcarTodos = vars.getStringParameter("inpTodos", "N");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_Settlement").createXmlDocument();
    CreateFromSettlementData[] data = null;
    
    if(vars.getSessionValue("CreateFrom|default").equals("1")) {
      
      vars.removeSessionValue("CreateFrom|default");
    
   /* 
    if (strcBPartner.equals("") && strPaymentRule.equals("") && strPlannedDateFrom.equals("") && strPlannedDateTo.equals("") && strIsReceipt.equals("") && strTotalAmount.equals("") && strOrg.equals("")) {
     */
     
      // Modified 26-06-07
      if (log4j.isDebugEnabled()) log4j.debug("strIsReceipt: \"\"");      
      
      data = new CreateFromSettlementData[0];
     
      if (vars.commandIn("FRAME1")) {
        strcBPartner = strBPartner;
        strIsReceipt = isSOTrx;
      }
    } 
    else {
     
      // Modified 26-06-07
      if (log4j.isDebugEnabled()) log4j.debug("strIsReceipt: "+ strIsReceipt);
     
        data = CreateFromSettlementData.select(this, vars.getLanguage(), strMarcarTodos, strAutoCalcSelect, Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule, strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom, strAmountTo, strTotalAmount, strOrg);
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);
    xmlDocument.setParameter("autoCalculated", strAutoCalc);
    xmlDocument.setParameter("amountFrom", strAmountFrom);
    xmlDocument.setParameter("amountTo", strAmountTo);
    xmlDocument.setParameter("totalAmount", strTotalAmount);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("marcarTodos", strMarcarTodos);

    xmlDocument.setParameter("paymentRule", strPaymentRule);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "All_Payment Rule", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strPaymentRule);
      xmlDocument.setData("reportPaymentRule","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strOrg);
      xmlDocument.setData("reportAD_Org_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (log4j.isDebugEnabled()) log4j.debug("strcBPartner: "+ strcBPartner);
    if (log4j.isDebugEnabled()) log4j.debug("strPlannedDateFrom: "+ strPlannedDateFrom);
    if (log4j.isDebugEnabled()) log4j.debug("strPlannedDateTo: "+ strPlannedDateTo);
    
    xmlDocument.setParameter("inpcBpartnerId", strcBPartner);
    xmlDocument.setParameter("inpBpartnerId_DES", CreateFromSettlementData.bpartner(this, strcBPartner));
    
    xmlDocument.setParameter("plannedDateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateFromValue", strPlannedDateFrom);
    
    xmlDocument.setParameter("plannedDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateToValue",strPlannedDateTo);
    
    xmlDocument.setParameter("isreceiptPago", strIsReceipt);
    xmlDocument.setParameter("isreceiptCobro", strIsReceipt);
    
    {
      OBError myMessage = vars.getMessage("CreateFrom");
      vars.removeMessage("CreateFrom");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }


    xmlDocument.setData("structure1", data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

void printPageDPManagement(HttpServletResponse response, VariablesSecureApp vars, String strPath, String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName, String strBPartner) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: DPManagement");
    String strcBPartner = vars.getStringParameter("inpcBpartnerId");
    String strPaymentRule = vars.getStringParameter("inppaymentrule");
    String strPlannedDateFrom = vars.getStringParameter("inpplanneddateFrom");
    String strPlannedDateTo = vars.getStringParameter("inpplanneddateTo");
    String strAmountFrom = vars.getStringParameter("inpamountFrom");
    String strAmountTo = vars.getStringParameter("inpamountTo");
    String strTotalAmount = vars.getStringParameter("inpamount");
    String strIsReceipt = vars.getStringParameter("inpisreceipt");
    String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
   // String strAutoCalc = vars.getStringParameter("inpAutoClaculated");
//    String strAutoCalcSelect = "AMOUNT";
//    if (strAutoCalc.equals("")) strAutoCalcSelect = "WRITEOFFAMT";

    String strOrg = vars.getStringParameter("inpadOrgId");
    String strStatusFrom = vars.getStringParameter("inpStatusFrom");
    String strStatusTo = vars.getStringParameter("inpStatusTo");

    String strMarcarTodos = vars.getStringParameter("inpTodos", "N");


    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_DPManagement").createXmlDocument();
    CreateFromDPManagementData[] data = null;
    if (strcBPartner.equals("") && strPaymentRule.equals("") && strPlannedDateFrom.equals("") && strPlannedDateTo.equals("") && strIsReceipt.equals("") && strTotalAmount.equals("") && strOrg.equals("")) {
      data = new CreateFromDPManagementData[0];
      if (vars.commandIn("FRAME1")) {
        strcBPartner = strBPartner;
        strIsReceipt = isSOTrx;
      }
    } else {
      data = CreateFromDPManagementData.select(this, vars.getLanguage(), strMarcarTodos,  Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strcBPartner, strPaymentRule, strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom, strAmountTo, strTotalAmount, strStatusFrom, strOrg);

      if (log4j.isDebugEnabled()) log4j.debug("DPSelect: lineas"+data.length+"client "+Utility.getContext(this, vars, "#User_Client", strWindowId)+ "userOrg "+Utility.getContext(this, vars, "#User_Org", strWindowId)+ " partner:"+strcBPartner+" rule:"+strPaymentRule+ "df"+strPlannedDateFrom+" dt:"+strPlannedDateTo+" rec:"+strIsReceipt+"amtF:"+strAmountFrom+"amt T:"+strAmountTo+"ttlamt:"+strTotalAmount+"org "+strOrg);
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);
  //  xmlDocument.setParameter("autoCalculated", strAutoCalc);
    xmlDocument.setParameter("amountFrom", strAmountFrom);
    xmlDocument.setParameter("amountTo", strAmountTo);
    xmlDocument.setParameter("totalAmount", strTotalAmount);
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("statusTo", strStatusTo);
    xmlDocument.setParameter("statusFrom", strStatusFrom);

    xmlDocument.setParameter("marcarTodos", strMarcarTodos);

    xmlDocument.setParameter("paymentRule", strPaymentRule);

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "All_Payment Rule", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strPaymentRule);
      xmlDocument.setData("reportPaymentRule","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strOrg);
      xmlDocument.setData("reportAD_Org_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "C_DP_Management_Status", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strStatusFrom);
      xmlDocument.setData("reportStatusFrom","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "C_DP_Management_Status", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strStatusFrom);
      xmlDocument.setData("reportStatusTo","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("cbpartnerId", strcBPartner);
    xmlDocument.setParameter("cbpartnerId_DES", CreateFromDPManagementData.bpartner(this, strcBPartner));
    xmlDocument.setParameter("plannedDateFrom", strPlannedDateFrom);
    xmlDocument.setParameter("plannedDateTo", strPlannedDateTo);

    xmlDocument.setParameter("plannedDateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("plannedDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("isreceiptPago", strIsReceipt);
    xmlDocument.setParameter("isreceiptCobro", strIsReceipt);

    xmlDocument.setData("structure1", data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageCRemittance(HttpServletResponse response, VariablesSecureApp vars, String strPath, String strKey, String strTableId, String strProcessId, String strWindowId, String strTabName, String strBPartner, String stradOrgId, String isReceipt) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: CRemittance");
    String strcBPartner = vars.getStringParameter("inpcBpartnerId");
    String strPaymentRule = vars.getStringParameter("inppaymentrule");
    String strPlannedDateFrom = vars.getStringParameter("inpplanneddateFrom");
    String strPlannedDateTo = vars.getStringParameter("inpplanneddateTo");
    String strAmountFrom = vars.getStringParameter("inpamountFrom");
    String strAmountTo = vars.getStringParameter("inpamountTo");
    String strTotalAmount = vars.getStringParameter("inpamount");
    String strIsReceipt = vars.getStringParameter("inpisreceipt", isReceipt);
    String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
   // String strAutoCalc = vars.getStringParameter("inpAutoCalculated");
//    String strAutoCalcSelect = "AMOUNT";
//    if (strAutoCalc.equals("")) strAutoCalcSelect = "WRITEOFFAMT";

    String strOrg = vars.getStringParameter("inpadOrgId", stradOrgId);
    String strStatusFrom = vars.getStringParameter("inpStatusFrom");
   // String strStatusTo = vars.getStringParameter("inpStatusTo");

    String strMarcarTodos = vars.getStringParameter("inpTodos", "N");
    String strTreeOrg = ReportTrialBalanceData.treeOrg(this, vars.getClient());
    String strOrgFamily = Tree.getMembers(this, strTreeOrg, strOrg);

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/CreateFrom_CRemittance").createXmlDocument();
    //XmlDocument xmlDocument = null;
    CreateFromCRemittanceData[] data = null;
    if (vars.commandIn("FRAME1")) {
      data = new CreateFromCRemittanceData[0];
      strcBPartner = strBPartner;
      strIsReceipt = isSOTrx;
    } else {
      data = CreateFromCRemittanceData.select(this, vars.getLanguage(), strMarcarTodos,  Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "#User_Org", strWindowId), strOrgFamily, strcBPartner, strPaymentRule, strPlannedDateFrom, strPlannedDateTo, strIsReceipt, strAmountFrom, strAmountTo, strTotalAmount,strStatusFrom);
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("path", strPath);
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("tableId", strTableId);
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabName", strTabName);
  //  xmlDocument.setParameter("autoCalculated", strAutoCalc);
    xmlDocument.setParameter("amountFrom", strAmountFrom);
    xmlDocument.setParameter("amountTo", strAmountTo);
    xmlDocument.setParameter("totalAmount", strTotalAmount);
  //  xmlDocument.setParameter("statusTo", strStatusTo);
    xmlDocument.setParameter("statusFrom", strStatusFrom);

    xmlDocument.setParameter("marcarTodos", strMarcarTodos);

    xmlDocument.setParameter("paymentRule", strPaymentRule);
    
    xmlDocument.setParameter("plannedDateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("plannedDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("plannedDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "All_Payment Rule", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strPaymentRule);
      xmlDocument.setData("reportPaymentRule","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strOrg);
      xmlDocument.setData("reportAD_Org_ID","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "", "C_DP_Management_Status", "", Utility.getContext(this, vars, "#User_Org", strWindowId), Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, strStatusFrom);
      xmlDocument.setData("reportStatusFrom","liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("cbpartnerId", strcBPartner);
    xmlDocument.setParameter("cbpartnerId_DES", CreateFromDPManagementData.bpartner(this, strcBPartner));
    xmlDocument.setParameter("plannedDateFrom", strPlannedDateFrom);
    xmlDocument.setParameter("plannedDateTo", strPlannedDateTo);
    xmlDocument.setParameter("isreceiptPago", strIsReceipt);
    xmlDocument.setParameter("isreceiptCobro", strIsReceipt);
    xmlDocument.setParameter("adOrgId", strOrg);

    xmlDocument.setData("structure1", data);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  OBError saveMethod(VariablesSecureApp vars, String strKey, String strTableId, String strProcessId, String strWindowId) throws IOException, ServletException {
    if (strTableId.equals("392")) return saveBank(vars, strKey, strTableId, strProcessId);
    else if (strTableId.equals("318")) return saveInvoice(vars, strKey, strTableId, strProcessId, strWindowId);
    else if (strTableId.equals("319")) return saveShipment(vars, strKey, strTableId, strProcessId, strWindowId);
    else if (strTableId.equals("426")) return savePay(vars, strKey, strTableId, strProcessId);
    else if (strTableId.equals("800019")) return saveSettlement(vars, strKey, strTableId, strProcessId);
    else if (strTableId.equals("800176")) return saveDPManagement(vars, strKey, strTableId, strProcessId);
    else if (strTableId.equals("800179")) return saveCRemittance(vars, strKey, strTableId, strProcessId);
    else return null;
  }

  OBError saveBank(VariablesSecureApp vars, String strKey, String strTableId, String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Save: Bank");
    String strPayment = vars.getInStringParameter("inpcPaymentId");
    String strStatementDate = vars.getStringParameter("inpstatementdate");
    String strDateplanned = "";
    String strChargeamt = "";
    OBError myMessage = null;
    Connection conn = null;
    
    if (strPayment.equals("")) return Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    
    try {
      conn = this.getTransactionConnection();
      if (strPayment.startsWith("(")) strPayment = strPayment.substring(1, strPayment.length()-1);
      if (!strPayment.equals("")) {
        strPayment = Replace.replace(strPayment, "'", "");
        StringTokenizer st = new StringTokenizer(strPayment, ",", false);
        while (st.hasMoreTokens()) {
          String strDebtPaymentId = st.nextToken().trim();
          if (!CreateFromBankData.NotIsReconcilied(conn, this, strDebtPaymentId)) {
            releaseRollbackConnection(conn);
            log4j.warn("CreateFrom.saveBank - debt_payment " + strDebtPaymentId + " is reconcilied");
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), "DebtPaymentReconcilied");
            return myMessage;
          }
          strDateplanned = vars.getStringParameter("inpplanneddate" + strDebtPaymentId.trim());
          strChargeamt = vars.getStringParameter("inpcost" + strDebtPaymentId.trim());
          String strSequence = SequenceIdData.getSequence(this, "C_BankStatementLine", vars.getClient());
          try {
            CreateFromBankData.insert(conn, this, strSequence, vars.getClient(), vars.getUser(), strKey, strDateplanned.equals("")?strStatementDate:strDateplanned, strChargeamt, strDebtPaymentId);
          } catch(ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
      }
      
      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));      
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  OBError saveInvoice(VariablesSecureApp vars, String strKey, String strTableId, String strProcessId, String strWindowId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Save: Invoice");
    String strDateInvoiced = vars.getRequiredStringParameter("inpDateInvoiced");
    String strBPartnerLocation = vars.getRequiredStringParameter("inpcBpartnerLocationId");
    String strBPartner = vars.getRequiredStringParameter("inpcBpartnerId");
    String strPriceList = vars.getRequiredStringParameter("inpMPricelist");
    String strType = vars.getRequiredStringParameter("inpType");
    String strClaves = Utility.stringList(vars.getRequiredInParameter("inpcOrderId"));
    String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    String strPO="", priceActual="0", priceLimit="0", priceList="0", strPriceListVersion="", priceStd="0";
    CreateFromInvoiceData[] data=null;
    OBError myMessage = null;
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      if (strType.equals("SHIPMENT")) {
        if (isSOTrx.equals("Y")) data = CreateFromInvoiceData.selectFromShipmentUpdateSOTrx(conn, this, strClaves);
        else data = CreateFromInvoiceData.selectFromShipmentUpdate(conn, this, strClaves);
        CreateFromInvoiceData[] dataAux = CreateFromInvoiceData.selectPriceList(conn, this, strDateInvoiced, strPriceList);
        if (dataAux==null || dataAux.length==0) {myMessage = Utility.translateError(this, vars, vars.getLanguage(), "PriceListVersionNotFound"); return myMessage;}
        strPriceListVersion = dataAux[0].id;
      } else {
        strPO = vars.getStringParameter("inpPurchaseOrder");
        if (isSOTrx.equals("Y")) data = CreateFromInvoiceData.selectFromPOUpdateSOTrx(conn, this, strClaves);
        else data = CreateFromInvoiceData.selectFromPOUpdate(conn, this, strClaves);
      }
      if (data!=null) {
        for (int i=0;i<data.length;i++) {
          String strSequence = SequenceIdData.getSequence(this, "C_InvoiceLine", vars.getClient());
          CreateFromInvoiceData[] price = null;
          String C_Tax_ID="";
          if (data[i].cOrderlineId.equals("")) C_Tax_ID = Tax.get (this, data[i].mProductId, strDateInvoiced, data[i].adOrgId, vars.getWarehouse(), strBPartnerLocation, strBPartnerLocation, CreateFromInvoiceData.selectProject(this, strKey), isSOTrx.equals("Y")?true:false);
          else C_Tax_ID = CreateFromInvoiceData.getTax(this, data[i].cOrderlineId);

          int stdPrecision = Integer.valueOf(data[i].stdprecision).intValue();
          if (!data[i].cOrderlineId.equals("")) {
            price = CreateFromInvoiceData.selectPrices(conn, this, data[i].cOrderlineId);
            if (price!=null && price.length>0) {
              priceList = price[0].pricelist;
              priceActual = price[0].priceactual;
              priceLimit = price[0].pricelimit;
              priceStd = price[0].pricestd;
            }
            price=null;
          } else {
            price = CreateFromInvoiceData.selectBOM(conn, this, strDateInvoiced, strBPartner, data[i].mProductId, strPriceListVersion);
            if (price!=null && price.length>0) {
              priceList = price[0].pricelist;
              priceActual = price[0].priceactual;
              priceLimit = price[0].pricelimit;
              priceStd = price[0].pricestd;
            }
            price=null;
          }
          BigDecimal LineNetAmt = (new BigDecimal(priceActual)).multiply(new BigDecimal(data[i].id));
          LineNetAmt.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);
          try {
            CreateFromInvoiceData.insert(conn, this, strSequence, strKey, vars.getClient(), data[i].adOrgId, vars.getUser(), data[i].cOrderlineId, data[i].mInoutlineId, data[i].description, data[i].mProductId, data[i].cUomId, data[i].id, priceList, priceActual, priceLimit, LineNetAmt.toString(), C_Tax_ID, data[i].quantityorder, data[i].mProductUomId, data[i].mAttributesetinstanceId, priceStd);
          } catch(ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
      }

      if (!strPO.equals("")) {
        try {
          int total = CreateFromInvoiceData.deleteC_Order_ID(conn, this, strKey, strPO);
          if (total==0) CreateFromInvoiceData.updateC_Order_ID(conn, this, strPO, strKey);
        } catch(ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myMessage;
        }

      }

      releaseCommitConnection(conn);
      if (log4j.isDebugEnabled()) log4j.debug("Save commit");
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch(Exception e){
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  OBError saveShipment(VariablesSecureApp vars, String strKey, String strTableId, String strProcessId, String strWindowId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Save: Shipment");
    String strLocator = vars.getRequiredStringParameter("inpmLocatorId");
    String strType = vars.getRequiredStringParameter("inpType");
    String strClaves = Utility.stringList(vars.getRequiredInParameter("inpId"));
    String isSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
    String strInvoice="", strPO="";
    CreateFromShipmentData[] data=null;
    OBError myMessage = null;
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      if (strType.equals("INVOICE")) {
        strInvoice = vars.getStringParameter("inpInvoice");
        if (!isSOTrx.equals("Y")) data = CreateFromShipmentData.selectFromInvoiceUpdate(conn, this, strClaves);
      } else {
        strPO = vars.getStringParameter("inpPurchaseOrder");
        if (isSOTrx.equals("Y")) data = CreateFromShipmentData.selectFromPOUpdateSOTrx(conn, this, strClaves);
        else data = CreateFromShipmentData.selectFromPOUpdate(conn, this, strClaves);
      }
      if (data!=null) {
        for (int i=0;i<data.length;i++) {
          String strMultiplyRate="";
          int stdPrecision =0;
          if (data[i].breakdown.equals("Y")) {
            if (data[i].cUomIdConversion.equals("")) {
              releaseRollbackConnection(conn);
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
              return myMessage;
            }
            String strInitUOM = data[i].cUomIdConversion;
            String strUOM = data[i].cUomId;
            if (strInitUOM.equals(strUOM)) strMultiplyRate = "1";
            else strMultiplyRate = CreateFromShipmentData.multiplyRate (this, strInitUOM, strUOM);
            if (strMultiplyRate.equals("")) strMultiplyRate = CreateFromShipmentData.divideRate (this, strUOM, strInitUOM);
            if (strMultiplyRate.equals("")) {
              strMultiplyRate = "1";
              releaseRollbackConnection(conn);
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
              return myMessage;
            }
            stdPrecision = Integer.valueOf(data[i].stdprecision).intValue();
            BigDecimal quantity, qty, multiplyRate;

            multiplyRate = new BigDecimal(strMultiplyRate);
            qty = new BigDecimal(data[i].id);
            boolean qtyIsNegative = false;
            if (qty.doubleValue() < ZERO.doubleValue()) {
              qtyIsNegative = true;
              qty = new BigDecimal(-1.0 * qty.doubleValue());
            }
            quantity = new BigDecimal(multiplyRate.doubleValue() * qty.doubleValue());
            if (quantity.scale() > stdPrecision) quantity = quantity.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);
            while (qty.doubleValue()>ZERO.doubleValue()) {
              String total = "1";
              BigDecimal conversion;
              if (quantity.doubleValue()<1.0) {
                total=quantity.toString();
                conversion = qty;
                quantity=ZERO;
                qty=ZERO;
              } else {
                conversion = new BigDecimal(1.0 * multiplyRate.doubleValue());
                if (conversion.doubleValue()>qty.doubleValue()) {
                  conversion=qty;
                  qty=ZERO;
                } else qty = new BigDecimal(qty.doubleValue() - conversion.doubleValue());
                quantity = new BigDecimal(quantity.doubleValue() - 1.0);
              }
              String strConversion = conversion.toString();
              String strSequence = SequenceIdData.getSequence(this, "M_InOutLine", vars.getClient());
              try {
                CreateFromShipmentData.insert(conn, this, strSequence, strKey, vars.getClient(), data[i].adOrgId, vars.getUser(), data[i].description, data[i].mProductId, data[i].cUomId, (qtyIsNegative?"-"+strConversion:strConversion), data[i].cOrderlineId, strLocator, CreateFromShipmentData.isInvoiced(conn, this, data[i].cInvoicelineId), (qtyIsNegative?"-"+total:total), data[i].mProductUomId, data[i].mAttributesetinstanceId);
                if (!strInvoice.equals("")) CreateFromShipmentData.updateInvoice(conn, this, strSequence, data[i].cInvoicelineId);
                else CreateFromShipmentData.updateInvoiceOrder(conn, this, strSequence, data[i].cOrderlineId);
              } catch(ServletException ex) {
                myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
                releaseRollbackConnection(conn);
                return myMessage;
              }
            }
          } else {
            String strSequence = SequenceIdData.getSequence(this, "M_InOutLine", vars.getClient());
            try {
              CreateFromShipmentData.insert(conn, this, strSequence, strKey, vars.getClient(), data[i].adOrgId, vars.getUser(), data[i].description, data[i].mProductId, data[i].cUomId, data[i].id, data[i].cOrderlineId, strLocator, CreateFromShipmentData.isInvoiced(conn, this, data[i].cInvoicelineId), data[i].quantityorder, data[i].mProductUomId, data[i].mAttributesetinstanceId);
              if (!strInvoice.equals("")) CreateFromShipmentData.updateInvoice(conn, this, strSequence, data[i].cInvoicelineId);
              else CreateFromShipmentData.updateInvoiceOrder(conn, this, strSequence, data[i].cOrderlineId);
            } catch(ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              releaseRollbackConnection(conn);
              return myMessage;
            }
          }
        }
      }

      if (!strPO.equals("")) {
        try {
          int total = CreateFromShipmentData.deleteC_Order_ID(conn, this, strKey, strPO);
          if (total==0) CreateFromShipmentData.updateC_Order_ID(conn, this, strPO, strKey);
        } catch(ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myMessage;
        }
      }
      if (!strInvoice.equals("")) {
        try {
          int total = CreateFromShipmentData.deleteC_Invoice_ID(conn, this, strKey, strInvoice);
          if (total==0) CreateFromShipmentData.updateC_Invoice_ID(conn, this, strInvoice, strKey);
        } catch(ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myMessage;
        }
      }

      releaseCommitConnection(conn);
      if (log4j.isDebugEnabled()) log4j.debug("Save commit");
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch(Exception e){
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  OBError savePay(VariablesSecureApp vars, String strKey, String strTableId, String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Save: Pay");
    return null;
  }

  OBError saveSettlement(VariablesSecureApp vars, String strKey, String strTableId, String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Save: Settlement");
    String strDebtPayment = vars.getInStringParameter("inpcDebtPaymentId");
    if (strDebtPayment.equals("")) return null;
    OBError myMessage = null;
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      if (strDebtPayment.startsWith("(")) strDebtPayment = strDebtPayment.substring(1, strDebtPayment.length()-1);
      if (!strDebtPayment.equals("")) {
        strDebtPayment = Replace.replace(strDebtPayment, "'", "");
        StringTokenizer st = new StringTokenizer(strDebtPayment, ",", false);
        while (st.hasMoreTokens()) {
          String strDebtPaymentId = st.nextToken().trim();
          String strWriteOff = vars.getStringParameter("inpwriteoff" + strDebtPaymentId);
          String strIsPaid = vars.getStringParameter("inpispaid" + strDebtPaymentId, "N");
          if (!CreateFromSettlementData.NotIsCancelled(conn, this, strDebtPaymentId)) {
            releaseRollbackConnection(conn);
            log4j.warn("CreateFrom.saveSettlement - debt_payment " + strDebtPaymentId + " is cancelled");
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), "DebtPaymentCancelled");
            return myMessage;
          }
          try {
            CreateFromSettlementData.update(conn, this, vars.getUser(), strKey, strWriteOff, strIsPaid, strDebtPaymentId);
          } catch(ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
      }
      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }

  OBError saveDPManagement(VariablesSecureApp vars, String strKey, String strTableId, String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Save: DPManagement");
    String strDebtPayment = vars.getInStringParameter("inpcDebtPaymentId");
    if (strDebtPayment.equals("")) return null;
    OBError myMessage = null;
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      String strStatusTo = vars.getStringParameter("inpStatusTo");
      if (strDebtPayment.startsWith("(")) strDebtPayment = strDebtPayment.substring(1, strDebtPayment.length()-1);
      if (!strDebtPayment.equals("")) {
        strDebtPayment = Replace.replace(strDebtPayment, "'", "");
        Integer line = new Integer(CreateFromDPManagementData.getLine(this, strKey));
        StringTokenizer st = new StringTokenizer(strDebtPayment, ",", false);
        while (st.hasMoreTokens()) {
          String strDebtPaymentId = st.nextToken().trim();
          if (!CreateFromDPManagementData.NotIsCancelled(conn, this, strDebtPaymentId)) {
            releaseRollbackConnection(conn);
            log4j.warn("CreateFrom.saveSettlement - debt_payment " + strDebtPaymentId + " is cancelled");
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), "DebtPaymentCancelled");
            return myMessage;
          }
          String strDPManagementLineID = SequenceIdData.getSequence(this, "C_DP_ManagementLine", vars.getClient());

          line += 10;
          try {
            CreateFromDPManagementData.insert(conn, this, strDPManagementLineID, vars.getClient(), vars.getUser(), strKey, strStatusTo, line.toString(), strDebtPaymentId);
          } catch(ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
      }
      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }


  OBError saveCRemittance(VariablesSecureApp vars, String strKey, String strTableId, String strProcessId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Save: Cremittance");
    String strDebtPayment = vars.getInStringParameter("inpcDebtPaymentId");
    if (strDebtPayment.equals("")) return null;
    OBError myMessage = null;
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      Integer lineNo = Integer.valueOf(CreateFromCRemittanceData.selectLineNo(this, strKey)).intValue();
     // String strStatusTo = vars.getStringParameter("inpStatusTo");
      if (strDebtPayment.startsWith("(")) strDebtPayment = strDebtPayment.substring(1, strDebtPayment.length()-1);
      if (!strDebtPayment.equals("")) {
        strDebtPayment = Replace.replace(strDebtPayment, "'", "");
        StringTokenizer st = new StringTokenizer(strDebtPayment, ",", false);
        while (st.hasMoreTokens()) {
          String strDebtPaymentId = st.nextToken().trim();

          if (!CreateFromDPManagementData.NotIsCancelled(conn, this, strDebtPaymentId)) {
            releaseRollbackConnection(conn);
            log4j.warn("CreateFrom.saveSettlement - debt_payment " + strDebtPaymentId + " is cancelled");
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), "DebtPaymentCancelled");
            return myMessage;
          }
          String strCRemittanceLineID = SequenceIdData.getSequence(this, "C_RemittanceLine", vars.getClient());
          lineNo += 10;
          try {
            CreateFromCRemittanceData.insert(conn, this, strCRemittanceLineID, vars.getClient(), vars.getUser(), strKey, lineNo.toString(),strDebtPaymentId);
          } catch(ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }
      }
      releaseCommitConnection(conn);
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myMessage;
  }



  public String getServletInfo() {
    return "Servlet that presents the button of CreateFrom";
  } // end of getServletInfo() method
}
