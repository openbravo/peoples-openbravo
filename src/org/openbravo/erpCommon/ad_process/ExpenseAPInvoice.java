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
package org.openbravo.erpCommon.ad_process;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.*;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;

// imports for transactions
import java.sql.Connection;

import org.openbravo.erpCommon.utility.DateTimeData;

public class ExpenseAPInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;	
  static Logger log4j = Logger.getLogger(ExpenseAPInvoice.class);

String strVersionNo = "";
String strIsHTML = "";
String strMailHeader = "";
String strMailText = "";
String strrMailTextId = "";
String strAttachAsset = "";
String strProductDownloadURL = "";
String strProductVersionNo = "";
String strProductRMailTextID = "";

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      String strcBpartnerId = vars.getStringParameter("inpcBpartnerId");
      String strDatereportFrom = vars.getGlobalVariable("inpReportDateFrom", "ExpenseAPInvoice.reportdatefrom", "");
      String strDatereportTo = vars.getGlobalVariable("inpReportDateTo", "ExpenseAPInvoice.reportdateto", "");
      String strDateInvoiced = vars.getGlobalVariable("inpDateinvoiced", "ExpenseAPInvoice.dateinvoiced", "");
      printPage(response, vars, strcBpartnerId, strDatereportFrom, strDatereportTo, strDateInvoiced);
    } else if (vars.commandIn("SAVE")) {
      String strcBpartnerId = vars.getStringParameter("inpcBpartnerId", "");
      String strDatereportFrom = vars.getRequestGlobalVariable("inpReportDateFrom", "ExpenseAPInvoice.reportdatefrom");
      String strDatereportTo = vars.getRequestGlobalVariable("inpReportDateTo", "ExpenseAPInvoice.reportdateto");
      String strDateInvoiced = vars.getRequestGlobalVariable("inpDateinvoiced", "ExpenseAPInvocie.dateinvoiced");
      processExpense(vars, strcBpartnerId, strDatereportFrom, strDatereportTo, strDateInvoiced);

      printPage(response, vars, strcBpartnerId, strDatereportFrom, strDatereportTo, strDateInvoiced);
    } else pageErrorPopUp(response);
  }

  void processExpense(VariablesSecureApp vars, String strcBpartnerId, String strDatereportFrom, String strDatereportTo, String strDateInvoiced) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Save: Expense AP Invoice");
    String strMessageResult = "";
    int line = 0;
    ExpenseAPInvoiceData[] data = ExpenseAPInvoiceData.select(this, Utility.getContext(this, vars, "#User_Client", "ExpenseAPInvoice"), Utility.getContext(this, vars, "#User_Org", "ExpenseAPInvoice"), strDatereportFrom, DateTimeData.nDaysAfter(this, strDatereportTo, "1"), strcBpartnerId);
    String strcBpartnerIdOld = "";
    String strcInvoiceId = "";
    String strPricelistId = "";
    String strcTaxID = "";
    String strcBpartnerLocationId = "";
    String strcInvoiceLineId = "";
    String strPricestd = "";
    String strPricelimit = "";
    String strPricelist = "";
    float qty = 0;
    float amount = 0;

    OBError myMessage = null;

    Connection conn = null;
    try{
      conn = this.getTransactionConnection();
//      String docTargetType = ExpenseAPInvoiceData.cDoctypeTarget(this, Utility.getContext(this, vars, "#User_Client", "ExpenseAPInvoice"), Utility.getContext(this, vars, "#User_Org", "ExpenseAPInvoice"));
      for (int i = 0; i<data.length; i++){
       String docTargetType = ExpenseAPInvoiceData.cDoctypeTarget(this, data[i].adClientId, data[i].adOrgId);
       if (strcBpartnerIdOld!=data[i].cBpartnerId){
       //insert invoice customer header
       // checks if there are invoices not processed that full filled the requirements
        String strcInvoiceIdOld = ExpenseAPInvoiceData.selectInvoiceHeader(conn, this, data[i].adClientId, data[i].adOrgId, strDateInvoiced, data[i].cBpartnerId, data[i].cCurrencyId, data[i].cProjectId, data[i].cActivityId, data[i].cCampaignId);
        strPricelistId = ExpenseAPInvoiceData.pricelistId(this, data[i].cBpartnerId);
        strcBpartnerLocationId = ExpenseAPInvoiceData.bPartnerLocation(this, data[i].cBpartnerId);


        

        if (strcInvoiceIdOld.equals("")) {
         strcInvoiceId = SequenceIdData.getSequence(this, "C_Invoice", data[i].adClientId);
         String strDocumentno = Utility.getDocumentNo(this, vars, "", "C_Invoice", Utility.getContext(this, vars, "C_DocTypeTarget_ID", docTargetType)        , Utility.getContext(this, vars, "C_DocType_ID", docTargetType), false, true);
         //String strDocType = ExpenseAPInvoiceData.cDoctypeId(this, docTargetType);
         String strDocType = ExpenseAPInvoiceData.cDoctypeTarget(this, data[i].adClientId, data[i].adOrgId);
         //strcBpartnerLocationId = ExpenseAPInvoiceData.bPartnerLocation(this, data[i].cBpartnerId);
         //String strSalesrepId = ExpenseAPInvoiceData.salesrepId(this, data[i].cBpartnerId);
         String strSalesrepId = "";
         String strPaymentRule = ExpenseAPInvoiceData.paymentrule(this, data[i].cBpartnerId);
         String strPaymentterm = ExpenseAPInvoiceData.paymentterm(this, data[i].cBpartnerId);

         //strPricelistId = ExpenseAPInvoiceData.pricelistId(this, data[i].cBpartnerId);

         ExpenseAPInvoiceData.insert(conn, this, strcInvoiceId, "N", "", "N", "N", "N", "N", "N", "N", "N", "N", "N", data[i].adClientId, data[i].adOrgId, "", "", strDocumentno, "", "", "Y", docTargetType, strDateInvoiced, strDateInvoiced, data[i].cBpartnerId, strcBpartnerLocationId, "", strPricelistId, data[i].cCurrencyId, strSalesrepId, "N", "", "", strPaymentRule, strPaymentterm, "N", "N", data[i].cProjectId, data[i].cActivityId, data[i].cCampaignId, vars.getOrg(), "", "", "0", "0", "DR", strDocType, "N", "CO", "N", vars.getUser(), vars.getUser());
        }
        else strcInvoiceId = strcInvoiceIdOld;
       }

       String strmProductUomId = ExpenseAPInvoiceData.mProductUomId(this, data[i].mProductId);

       ExpenseAPInvoiceData[] dataPrice = ExpenseAPInvoiceData.selectPrice(this, data[i].mProductId, strPricelistId, strDateInvoiced);

       //SLInvoiceTaxData[] dataTax = SLInvoiceTaxData.select(this, strcInvoiceId);
       //if (data!=null && data.length!=0){
       strPricestd = data[i].invoiceprice.equals("")?dataPrice[0].pricestd:data[i].invoiceprice;
       strPricelimit = data[i].invoiceprice.equals("")?dataPrice[0].pricelimit:data[i].invoiceprice;
       strPricelist = data[i].invoiceprice.equals("")?dataPrice[0].pricelist:data[i].invoiceprice;
       String bpartnerLocationShip = ExpenseAPInvoiceData.shipto(this, data[i].cBpartnerId);

       strcTaxID = Tax.get(this, data[i].mProductId, strDateInvoiced, data[i].adOrgId, vars.getWarehouse(), strcBpartnerLocationId, bpartnerLocationShip, data[i].cProjectId, false);
       //}
       //checks if there are lines with the same conditions in the current invoice

       ExpenseAPInvoiceData[] dataInvoiceline = ExpenseAPInvoiceData.selectInvoiceLine(conn, this, strcInvoiceId, data[i].adClientId, data[i].adOrgId, data[i].mProductId, data[i].cUomId, strPricestd, strPricelist, strPricelimit, data[i].description, strcTaxID);

       if (log4j.isDebugEnabled()) log4j.debug("dataInvoiceline: "+dataInvoiceline.length);
       if (dataInvoiceline == null || dataInvoiceline.length == 0){ 
        //if new, calculate c_invoiceline_id and qty
         strcInvoiceLineId = SequenceIdData.getSequence(this, "C_InvoiceLine", data[i].adClientId);
         qty = Float.valueOf(data[i].qty);
         String strLine = ExpenseAPInvoiceData.selectLine(conn, this, strcInvoiceId);
         if (strLine.equals("")) strLine = "10";
         line+=Integer.valueOf(strLine);
	 
       if (log4j.isDebugEnabled()) log4j.debug("*****************+client: "+ (data[i].invoiceprice.equals("")?dataPrice[0].pricestd:data[i].invoiceprice));
         ExpenseAPInvoiceData.insertLine(conn, this, data[i].adClientId, data[i].adOrgId, strcInvoiceId, "", String.valueOf(line), "", data[i].mProductId, "", data[i].description, "", strmProductUomId, String.valueOf(qty), data[i].cUomId, strPricestd, strPricelist, strcTaxID, String.valueOf(Float.valueOf(strPricestd)*qty), "", strPricestd, strPricelimit, "", "", "", "Y", "0", "", "", strcInvoiceLineId, "", "N", vars.getUser(), vars.getUser());


       } else {
         //if there are more lines that full filled the requirements, add the new amount to the old
         strcInvoiceLineId = dataInvoiceline[0].cInvoicelineId;
         qty = Float.valueOf(dataInvoiceline[0].qtyinvoiced)+Float.valueOf(data[i].qty);
         ExpenseAPInvoiceData.updateInvoiceline(conn, this, String.valueOf(qty), String.valueOf(Float.valueOf(strPricestd)*qty), strcInvoiceLineId);

       }

       if (!data[i].cProjectId.equals("")){
         //if there are acctidimensions that full filled the requirements
         ExpenseAPInvoiceData[] dataAcctdimension = ExpenseAPInvoiceData.selectAcctdimension(conn, this, data[i].adClientId, data[i].adOrgId, strcInvoiceLineId, data[i].cProjectId, data[i].cCampaignId);
         if (dataAcctdimension == null || dataAcctdimension.length == 0) {
          String strcInvoicelineAcctdimension = SequenceIdData.getSequence(this, "C_InvoiceLine_AcctDimension", data[i].adClientId);
          ExpenseAPInvoiceData.insertInvoicelineAcctdimension(conn, this, strcInvoicelineAcctdimension, data[i].adClientId, data[i].adOrgId, "Y", vars.getUser(), vars.getUser(), strcInvoiceLineId, String.valueOf(qty*Float.valueOf(strPricestd)), data[i].cProjectId, data[i].cCampaignId, "", "");
         } else {
          amount = Float.valueOf(dataAcctdimension[0].amt)+(Float.valueOf(data[i].qty)*Float.valueOf(strPricestd));
          ExpenseAPInvoiceData.updateAcctdimension(conn, this, String.valueOf(amount), dataAcctdimension[0].cInvoicelineAcctdimensionId);
         }
       }
       ExpenseAPInvoiceData.updateExpense(conn, this, strcInvoiceLineId, data[i].sTimeexpenselineId);
       
      }
      releaseCommitConnection(conn);
    } catch (ArrayIndexOutOfBoundsException f){
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      f.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "PriceListVersionNotFound");
    } catch (Exception e){
      try {
         releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    if (myMessage==null) {
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(strMessageResult.equals("")?Utility.messageBD(this, "Success", vars.getLanguage()):strMessageResult);
    }
    vars.setMessage("ExpenseAPInvoice", myMessage);
  }


  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strcBpartnerId, String strDatereportFrom, String strDatereportTo, String strDateInvoiced) throws IOException, ServletException {
      if (log4j.isDebugEnabled()) log4j.debug("Output: process ExpenseAPInvoice");
      
      String[] discard = {""};
      String strHelp = ExpenseAPInvoiceData.help(this, "S_ExpenseAPInvoice");
      if (strHelp.equals("")) discard[0] = new String("helpDiscard");
      
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_process/ExpenseAPInvoice").createXmlDocument();
      
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ExpenseAPInvoice", false, "", "", "",false, "ad_process",  strReplaceWith, false,  true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());
      
      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
      xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("help", strHelp);
      xmlDocument.setParameter("dateFrom", strDatereportFrom);
      xmlDocument.setParameter("dateTo", strDatereportTo);
      xmlDocument.setParameter("dateInvoiced", strDateInvoiced);
      xmlDocument.setParameter("description", ExpenseAPInvoiceData.description(this, "S_ExpenseAPInvoice"));

      try {
    	  ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "C_BPartner_ID", "C_BPartner Employee w Address", "", Utility.getContext(this, vars, "#User_Client",""), Utility.getContext(this, vars, "#AD_Client_ID", "ExpenseAPInvoice"), 0);
    	  Utility.fillSQLParameters(this, vars, null, comboTableData, "ExpenseAPInvoice", "");
    	  xmlDocument.setData("reportC_BPARTNERID","liststructure", comboTableData.select(false));
    	  comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      
      xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	  xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	  xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	  xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	  xmlDocument.setParameter("dateInvdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
	  xmlDocument.setParameter("dateInvsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    
    
      // New interface parameters
      try {
        WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_process.ExpenseAPInvoice");

        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ExpenseAPInvoice.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ExpenseAPInvoice.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ExpenseAPInvoice");
        vars.removeMessage("ExpenseAPInvoice");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }      
     ////----
     
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }

  public String getServletInfo() {
    return "Servlet ExpenseAPInvoice";
  } // end of getServletInfo() method
}

