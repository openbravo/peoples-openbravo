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
 * All portions are Copyright (C) 2001-2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_combos.OrganizationComboData;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.reference.ActionButtonData;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.xmlEngine.XmlDocument;

public class ExpenseSOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final BigDecimal ZERO = new BigDecimal(0.0);

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strCompleteAuto = vars.getStringParameter("inpShowNullComplete", "Y");
      printPage(response, vars, "", "", "", "", "", strCompleteAuto);
    } else if (vars.commandIn("SAVE")) {
      String strBPartner = vars.getStringParameter("inpcBpartnerId");
      String strDatefrom = vars.getStringParameter("inpDateFrom");
      String strDateto = vars.getStringParameter("inpDateTo");
      String strDateOrdered = vars.getStringParameter("inpDateordered");
      String strOrganization = vars.getStringParameter("organization");
      String strCompleteAuto = vars.getStringParameter("inpShowNullComplete");

      OBError myMessage = processButton(vars, strBPartner, strDatefrom, strDateto, strDateOrdered,
          strOrganization, strCompleteAuto);
      vars.setMessage("ExpenseSOrder", myMessage);

      printPage(response, vars, strDatefrom, strDateto, strDateOrdered, strBPartner,
          strOrganization, strCompleteAuto);
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strBPartner, String strDatefrom,
      String strDateto, String strDateOrdered, String strOrganization, String strCompleteAuto) {
    StringBuffer textoMensaje = new StringBuffer();
    Connection conn = null;

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    String strCust = "";
    String strExpenseSheetDocno = "";
    String strExpenseSheetLineno = "";
    try {
      conn = getTransactionConnection();
      ExpenseSOrderData[] data = ExpenseSOrderData.select(this, strBPartner, strDatefrom,
          DateTimeData.nDaysAfter(this, strDateto, "1"), Utility.getContext(this, vars,
              "#User_Client", "ExpenseSOrder"), strOrganization.equals("") ? Utility.getContext(
              this, vars, "#User_Org", "ExpenseSOrder") : Utility.stringList(strOrganization));
      String strOldOrganization = "-1";
      String strOldBPartner = "-1";
      String strOldProject = "-1";
      String strDocStatus = "DR";
      String strDocAction = "CO";
      String strProcessing = "N";
      String strCOrderId = "";
      String strCCurrencyId = "";
      String strBPCCurrencyId = "";
      String strDateexpense = "";
      String priceactual = "";
      String pricelist = "";
      String pricelimit = "";
      String docType = "0";
      String docTargetType = "";
      int line = 0;
      int total = 0;
      // ArrayList order = new ArrayList();
      for (int i = 0; data != null && i < data.length; i++) {
        priceactual = "";
        pricelist = "";
        pricelimit = "";
        docTargetType = ExpenseSOrderData.cDoctypeTarget(conn, this, data[i].adClientId,
            data[i].adOrgId);

        if ((!data[i].cBpartnerId.equals(strOldBPartner)
            || !data[i].cProjectId.equals(strOldProject) || !data[i].adOrgId
            .equals(strOldOrganization))
            && !strCOrderId.equals("")) {
          releaseCommitConnection(conn);
          // Automatically processes Sales Order
          if (strCompleteAuto.equals("Y")) {
            try {
              OBError myError = processOrder(vars, strCOrderId);
              if (myError != null)
                textoMensaje.append(" -> ").append(myError.getMessage());
            } catch (ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              return myMessage;
            }
          }
          conn = getTransactionConnection();
        }
        // Creates a new sales order header, if required
        if (!data[i].cBpartnerId.equals(strOldBPartner)
            || !data[i].cProjectId.equals(strOldProject)
            || !data[i].adOrgId.equals(strOldOrganization)) {

          line = 0;
          if (total != 0)
            textoMensaje.append("<br>");
          total++;

          strOldBPartner = data[i].cBpartnerId;
          strOldProject = data[i].cProjectId;
          strOldOrganization = data[i].adOrgId;
          strCust = data[i].bpname;
          strExpenseSheetDocno = data[i].documentno;
          strExpenseSheetLineno = data[i].line;

          if (data[i].mPricelistId.equals("")) {
            throw new Exception("PricelistNotdefined");
          } else {
            // Selects the currency of the business partner price
            // list, that can be different from the currency of the
            // expense
            strBPCCurrencyId = ExpenseSOrderData.selectCurrency(this, data[i].mPricelistId);
          }

          BpartnerMiscData[] data1 = BpartnerMiscData.select(this, data[i].cBpartnerId);

          if (data1[0].paymentrule.equals("")) {
            throw new Exception("FormofPaymentNotdefined");
          }

          strCOrderId = SequenceIdData.getUUID();
          // order.add(strCOrderId);
          String strDocumentNo = Utility.getDocumentNo(this, vars, "", "C_Order", docTargetType,
              docTargetType, false, true);
          // Catch database error message
          try {
            ExpenseSOrderData.insertCOrder(conn, this, strCOrderId, data[i].adClientId,
                data[i].adOrgId, vars.getUser(), strDocumentNo, strDocStatus, strDocAction,
                strProcessing, docType, docTargetType, strDateOrdered, strDateOrdered,
                strDateOrdered, data[i].cBpartnerId, ExpenseSOrderData.cBPartnerLocationId(this,
                    data[i].cBpartnerId), ExpenseSOrderData.billto(this, data[i].cBpartnerId)
                    .equals("") ? ExpenseSOrderData.cBPartnerLocationId(this, data[i].cBpartnerId)
                    : ExpenseSOrderData.billto(this, data[i].cBpartnerId), strBPCCurrencyId,
                data1[0].paymentrule, data1[0].cPaymenttermId.equals("") ? ExpenseSOrderData
                    .selectPaymentTerm(this, data[i].adClientId) : data1[0].cPaymenttermId,
                data1[0].invoicerule.equals("") ? "I" : data1[0].invoicerule, data1[0].deliveryrule
                    .equals("") ? "A" : data1[0].deliveryrule, "I", data1[0].deliveryviarule
                    .equals("") ? "D" : data1[0].deliveryviarule,
                data[i].mWarehouseId.equals("") ? vars.getWarehouse() : data[i].mWarehouseId,
                data[i].mPricelistId, data[i].cProjectId, data[i].cActivityId, data[i].cCampaignId);
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
          textoMensaje.append(Utility.messageBD(this, "SalesOrderDocumentno", vars.getLanguage()))
              .append(" ").append(strDocumentNo).append(" ").append(
                  Utility.messageBD(this, "beenCreated", vars.getLanguage()));
        }

        // Determines the date of the expense (strDateExpense)
        strDateexpense = data[i].dateexpense.equals("") ? data[i].datereport : data[i].dateexpense;
        strDateexpense = strDateexpense.equals("") ? DateTimeData.today(this) : strDateexpense;

        // Gets the tax for the sales order line
        String strCTaxID = Tax.get(this, data[i].mProductId, strDateOrdered, data[i].adOrgId,
            data[i].mWarehouseId.equals("") ? vars.getWarehouse() : data[i].mWarehouseId,
            ExpenseSOrderData.cBPartnerLocationId(this, data[i].cBpartnerId), ExpenseSOrderData
                .cBPartnerLocationId(this, data[i].cBpartnerId), data[i].cProjectId, true);
        if (strCTaxID.equals(""))
          throw new Exception("TaxNotFound");

        // Currency of the expense line
        strCCurrencyId = data[i].cCurrencyId;

        String strPrecision = "0";
        String strPricePrecision = "0";
        String strDiscount = "";

        BigDecimal priceActual, priceList, priceLimit, discount;

        // If there is no invoice price, looks for the prices in the
        // pricelist of the business partner
        if (data[i].invoiceprice == null || data[i].invoiceprice.equals("")) {
          ExpenseSOrderData[] data3 = ExpenseSOrderData.selectPrice(this, data[i].mProductId,
              data[i].mPricelistId, strBPCCurrencyId);
          for (int j = 0; data3 != null && j < data3.length; j++) {
            if (data3[j].validfrom == null || data3[j].validfrom.equals("")
                || !DateTimeData.compare(this, strDateexpense, data3[j].validfrom).equals("-1")) {
              priceactual = data3[j].pricestd;
              pricelist = data3[j].pricelist;
              pricelimit = data3[j].pricelimit;
            }
          }
        } else {
          // If there is an invoice price, it takes it and makes
          // currency conversions, if necessary
          priceactual = data[i].invoiceprice;
          pricelist = "0";
          pricelimit = "0";

          // If the currency of the expense line is not the same as
          // the currency of the business partner pricelist, make the
          // corresponding conversions
          if (!strCCurrencyId.equals(strBPCCurrencyId)) {
            // Converts priceactual, pricelist and pricelimit to the
            // currency of the business partner pricelist
            priceactual = ExpenseSOrderData.selectConvertedAmt(this, priceactual, strCCurrencyId,
                strBPCCurrencyId, strDateexpense, vars.getClient(), vars.getOrg());
            pricelist = ExpenseSOrderData.selectConvertedAmt(this, pricelist, strCCurrencyId,
                strBPCCurrencyId, strDateexpense, vars.getClient(), vars.getOrg());
            pricelimit = ExpenseSOrderData.selectConvertedAmt(this, pricelimit, strCCurrencyId,
                strBPCCurrencyId, strDateexpense, vars.getClient(), vars.getOrg());
          }
        }

        // If priceactual is null
        if (priceactual.equals("") || priceactual == null) {
          throw new Exception("PriceNotFound");
        } else {
          // Recalculates precisions for the business partner pricelist
          // currency
          ExpenseSOrderData[] data4 = ExpenseSOrderData.selectPrecisions(this, strBPCCurrencyId);
          if (data4 != null && data4.length > 0) {
            strPrecision = data4[0].stdprecision.equals("") ? "0" : data4[0].stdprecision;
            strPricePrecision = data4[0].priceprecision.equals("") ? "0" : data4[0].priceprecision;
          }
          int StdPrecision = Integer.valueOf(strPrecision).intValue();
          int PricePrecision = Integer.valueOf(strPricePrecision).intValue();
          priceActual = (priceactual.equals("") ? ZERO : (new BigDecimal(priceactual)));
          priceActual = priceActual.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
          priceList = (pricelist.equals("") ? ZERO : (new BigDecimal(pricelist)));
          priceList = priceList.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
          priceLimit = (pricelimit.equals("") ? ZERO : (new BigDecimal(pricelimit)));
          priceLimit = priceLimit.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);

          // Calculating discount
          if (priceList.compareTo(BigDecimal.ZERO) == 0)
            discount = ZERO;
          else {
            if (log4j.isDebugEnabled())
              log4j.debug("pricelist:" + Double.toString(priceList.doubleValue()));
            if (log4j.isDebugEnabled())
              log4j.debug("priceActual:" + Double.toString(priceActual.doubleValue()));
            discount = ((priceList.subtract(priceActual)).divide(priceList, 12,
                BigDecimal.ROUND_HALF_EVEN)).multiply(new BigDecimal("100"));
          }
          if (log4j.isDebugEnabled())
            log4j.debug("Discount: " + discount.toString());
          if (discount.scale() > StdPrecision) {
            discount = discount.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
          }
          if (log4j.isDebugEnabled())
            log4j.debug("Discount rounded: " + discount.toString());

          priceactual = priceActual.toString();
          pricelist = priceList.toString();
          pricelimit = priceLimit.toString();
          strDiscount = discount.toString();

          String strCOrderlineID = SequenceIdData.getUUID();
          if (line == 0) {
            line = 10;
          } else {
            line = line + 10;
          }
          // Catch database error message
          try {
            ExpenseSOrderData.insertCOrderline(conn, this, strCOrderlineID, data[i].adClientId,
                strOrganization.equals("") ? data[i].adOrgId : strOrganization, vars.getUser(),
                strCOrderId, Integer.toString(line), data[i].cBpartnerId, ExpenseSOrderData
                    .cBPartnerLocationId(this, data[i].cBpartnerId), strDateOrdered,
                strDateOrdered, data[i].description, data[i].mProductId, data[i].mWarehouseId
                    .equals("") ? vars.getWarehouse() : data[i].mWarehouseId, data[i].cUomId
                    .equals("") ? Utility.getContext(this, vars, "#C_UOM_ID", "ExpenseSOrder")
                    : data[i].cUomId, data[i].qty, strBPCCurrencyId, pricelist, priceactual,
                pricelimit, strCTaxID, data[i].sResourceassignmentId, strDiscount);
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
          // Catch database error message
          try {
            // Updates expense line with the sales order line ID
            ExpenseSOrderData.updateTimeExpenseLine(conn, this, strCOrderlineID,
                data[i].sTimeexpenselineId);
          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
        }

        releaseCommitConnection(conn);
        if (!strCOrderId.equals("")) {
          // Automatically processes Sales Order
          if (strCompleteAuto.equals("Y")) {
            try {
              OBError myError = processOrder(vars, strCOrderId);
              if (myError != null)
                textoMensaje.append(" -> ").append(myError.getMessage());
            } catch (ServletException ex) {
              myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
              return myMessage;
            }
          }
        }
        if (total != 0)
          textoMensaje.append("<br>");
        myMessage.setType("Success");
        myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
        myMessage.setMessage(textoMensaje.toString()
            + Utility.messageBD(this, "Created", vars.getLanguage()) + ": "
            + Integer.toString(total));
      }
      return myMessage;
    } catch (Exception e) {
      try {
        if (conn != null)
          releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage.setType("Error");
      myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      if (e.getMessage().equals("PricelistNotdefined")) {
        myMessage.setMessage(Utility.messageBD(this, "TheCustomer", vars.getLanguage()) + ' '
            + strCust + ' ' + Utility.messageBD(this, "PricelistNotdefined", vars.getLanguage()));
      } else if (e.getMessage().equals("FormofPaymentNotdefined")) {
        myMessage.setMessage(Utility.messageBD(this, "TheCustomer", vars.getLanguage()) + ' '
            + strCust + ' '
            + Utility.messageBD(this, "FormofPaymentNotdefined", vars.getLanguage()));
      } else if (e.getMessage().equals("TaxNotFound")) {
        myMessage.setMessage(Utility.messageBD(this, "TaxNotFound", vars.getLanguage()));
      } else if (e.getMessage().equals("PriceNotFound")) {
        myMessage.setType("Error");
        myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        myMessage.setMessage(Utility.messageBD(this, "SOS_DOCUMENTNO", vars.getLanguage()) + " "
            + strExpenseSheetDocno + ", " + Utility.messageBD(this, "line", vars.getLanguage())
            + " " + strExpenseSheetLineno + ": "
            + Utility.messageBD(this, "PriceNotFound", vars.getLanguage()) + ".");
      } else {
        myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      }
      return myMessage;
    }
  }

  private OBError processOrder(VariablesSecureApp vars, String strCOrderId)
      throws ServletException, NoConnectionAvailableException, SQLException {
    Connection conn = null;
    conn = getTransactionConnection();

    OBError myMessage = null;

    String pinstance = SequenceIdData.getUUID();
    try {
      PInstanceProcessData.insertPInstance(this, pinstance, "104", strCOrderId, "N",
          vars.getUser(), vars.getClient(), vars.getOrg());
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      releaseRollbackConnection(conn);
      return myMessage;
    }

    ActionButtonData.process104(this, pinstance);
    PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
    myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);

    return myMessage;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDatefrom,
      String strDateto, String strDateOrdered, String strBPartner, String strOrganization,
      String strCompleteAuto) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process ExpenseSOrder");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "", strProcessId = "186";
    String[] discard = { "" };
    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/ExpenseSOrder").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ExpenseSOrder", false, "", "", "",
        false, "ad_actionButton", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    xmlDocument.setParameter("Bpartnerdescription", ExpenseSOrderData.selectBpartner(this,
        strBPartner));
    xmlDocument.setParameter("BpartnerId", strBPartner);
    xmlDocument.setParameter("adOrgId", strOrganization);
    xmlDocument.setParameter("dateFrom", strDatefrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateto);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateOrdered", strDateOrdered);
    xmlDocument.setParameter("dateOrddisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateOrdsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("paramShowNullComplete", strCompleteAuto);
    xmlDocument
        .setData("structureOrganizacion", OrganizationComboData.select(this, vars.getRole()));

    // New interface parameters
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_actionButton.ExpenseSOrder");

      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ExpenseSOrder.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ExpenseSOrder.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("ExpenseSOrder");
      vars.removeMessage("ExpenseSOrder");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    // //----

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Project set Type";
  } // end of getServletInfo() method
}
