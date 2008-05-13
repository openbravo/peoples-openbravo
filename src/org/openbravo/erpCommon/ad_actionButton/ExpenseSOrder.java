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

import org.openbravo.erpCommon.utility.ToolBar;

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.reference.*;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.utils.Replace;
import org.openbravo.erpCommon.businessUtility.*;
import org.openbravo.erpCommon.ad_callouts.*;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.erpCommon.ad_combos.OrganizationComboData;

// imports for transactions
import java.sql.Connection;

import org.openbravo.erpCommon.utility.DateTimeData;

public class ExpenseSOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  static final BigDecimal ZERO = new BigDecimal(0.0);

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strCompleteAuto = vars.getStringParameter("inpShowNullComplete", "Y");
      printPage(response, vars, "", "", "", "","", strCompleteAuto);
    } else if (vars.commandIn("SAVE")) {
      String strBPartner = vars.getStringParameter("inpcBpartnerId");
      String strDatefrom = vars.getStringParameter("inpDateFrom");
      String strDateto = vars.getStringParameter("inpDateTo");
      String strDateOrdered = vars.getStringParameter("inpDateordered");
      String strOrganization = vars.getStringParameter("organization");
      String strCompleteAuto = vars.getStringParameter("inpShowNullComplete");
      
      OBError myMessage = processButton(vars, strBPartner, strDatefrom, strDateto, strDateOrdered, strOrganization, strCompleteAuto);
      vars.setMessage("ExpenseSOrder", myMessage);
      //vars.setSessionValue("ExpenseSOrder|message", messageResult);
      
      printPage(response, vars, strDatefrom, strDateto, strDateOrdered, strBPartner, strOrganization, strCompleteAuto);
    } else pageErrorPopUp(response);
  }


  OBError processButton(VariablesSecureApp vars, String strBPartner, String strDatefrom, String strDateto, String strDateOrdered, String strOrganization, String strCompleteAuto) {
    StringBuffer textoMensaje = new StringBuffer();
    Connection conn=null;
    
    OBError myMessage = null;
	myMessage = new OBError();
	myMessage.setTitle("");
    
    try {
      conn = getTransactionConnection();
      ExpenseSOrderData[] data = ExpenseSOrderData.select(this, strBPartner, strDatefrom, DateTimeData.nDaysAfter(this, strDateto,"1"), Utility.getContext(this, vars, "#User_Client", "ExpenseSOrder"), strOrganization.equals("")?Utility.getContext(this, vars, "#User_Org", "ExpenseSOrder"):strOrganization);
      String strOldOrganization = "-1";
      String strOldBPartner = "-1";
      String strOldProject = "-1";
      String strDocStatus = "DR";
      String strDocAction = "CO";
      String strProcessing = "N";
      String strCOrderId = "";
      String strCCurrencyId = "";
      String priceactual = "";
      String pricelist = "";
      String pricelimit = "";
      String docType = "0";
      String docTargetType = "";
      int line = 0;
      int total=0;
      //ArrayList order = new ArrayList();
      for (int i=0;data!=null && i<data.length;i++) {
	    docTargetType = ExpenseSOrderData.cDoctypeTarget(conn, this, data[i].adClientId, data[i].adOrgId);
            if ((!data[i].cBpartnerId.equals(strOldBPartner) || !data[i].cProjectId.equals(strOldProject)|| !data[i].adOrgId.equals(strOldOrganization)) && !strCOrderId.equals("")) {
              releaseCommitConnection(conn);
              // Automatically processes Sales Order
              if (strCompleteAuto.equals("Y")) {
	              String mensaje = processOrder(vars, strCOrderId);
	              if (!mensaje.equals("")) textoMensaje.append(mensaje).append("\n");
              }
              conn = getTransactionConnection();
            }
            if (!data[i].cBpartnerId.equals(strOldBPartner) || !data[i].cProjectId.equals(strOldProject) || !data[i].adOrgId.equals(strOldOrganization)) {
              line = 0;
              total++;
              strCOrderId = SequenceIdData.getSequence(this, "C_Order", vars.getClient());
              //order.add(strCOrderId);
              String strDocumentNo = Utility.getDocumentNo(this, vars, "", "C_Order", Utility.getContext(this, vars, "C_DocTypeTarget_ID", docTargetType), Utility.getContext(this, vars, "C_DocType_ID", docTargetType), false, true);

              strOldBPartner = data[i].cBpartnerId;
              strOldProject = data[i].cProjectId;
              strOldOrganization = data[i].adOrgId;
              strCCurrencyId = data[i].cCurrencyId.equals("")?Utility.getContext(this, vars, "$C_Currency_ID", "ExpenseSOrder"):data[i].cCurrencyId;

              SEOrderBPartnerData[] data1 = SEOrderBPartnerData.select(this, data[i].cBpartnerId);

              ExpenseSOrderData.insertCOrder(conn, this, strCOrderId, data[i].adClientId, data[i].adOrgId, vars.getUser(), strDocumentNo, strDocStatus, strDocAction, strProcessing, docType, docTargetType, strDateOrdered, strDateOrdered, strDateOrdered, data[i].cBpartnerId, ExpenseSOrderData.cBPartnerLocationId(this, data[i].cBpartnerId), ExpenseSOrderData.billto(this, data[i].cBpartnerId).equals("")?ExpenseSOrderData.cBPartnerLocationId(this, data[i].cBpartnerId):ExpenseSOrderData.billto(this, data[i].cBpartnerId), strCCurrencyId, data1[0].paymentrule, data1[0].cPaymenttermId.equals("")?SEOrderBPartnerData.selectPaymentTerm(this, data[i].adClientId):data1[0].cPaymenttermId, data1[0].invoicerule.equals("")?"I":data1[0].invoicerule, data1[0].deliveryrule.equals("")?"A":data1[0].deliveryrule, "I", data1[0].deliveryviarule.equals("")?"D":data1[0].deliveryviarule, data[i].mWarehouseId.equals("")?vars.getWarehouse():data[i].mWarehouseId, data[i].mPricelistId, data[i].cProjectId, data[i].cActivityId, data[i].cCampaignId);
            }

            String strCOrderlineID = SequenceIdData.getSequence(this, "C_OrderLine", vars.getClient());

            String strPrecision = "0";
            String strPricePrecision="0";
            String strDiscount = "";
            if (line==0) {
              line = 10;
            } else {
              line = line + 10;
            }
            if (data[i].invoiceprice == null || data[i].invoiceprice.equals("")) {
              SEExpenseProductData[] data3 = SEExpenseProductData.select(this, data[i].mProductId, data[i].mPricelistId);
              for (int j=0;data3!=null && j<data3.length;j++) {
                if (data3[j].validfrom == null  || data3[j].validfrom.equals("") || !DateTimeData.compare(this, DateTimeData.today(this), data3[j].validfrom).equals("-1")){
                priceactual = data3[j].pricestd;
                pricelist = data3[j].pricelist;
                pricelimit = data3[j].pricelimit;
                ExpenseSOrderData[] data4 = ExpenseSOrderData.selectPrecisions(this, strCCurrencyId);
                  if (data4!=null && data4.length>0) {
                  strPrecision = data4[0].stdprecision.equals("")?"0":data4[0].stdprecision;
                  strPricePrecision = data4[0].priceprecision.equals("")?"0":data4[0].priceprecision;
                  }
                int StdPrecision = Integer.valueOf(strPrecision).intValue();
                int PricePrecision = Integer.valueOf(strPricePrecision).intValue();

                BigDecimal priceActual, priceList, discount;

                priceActual = (priceactual.equals("")?ZERO:(new BigDecimal(priceactual)));
                priceActual.setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
                priceList = (pricelist.equals("")?ZERO:new BigDecimal(pricelist));
                
                //Calculating discount
                if (priceList.doubleValue() == 0.0) discount = ZERO;
                else {
                  if (log4j.isDebugEnabled()) log4j.debug("pricelist:" + Double.toString(priceList.doubleValue()));
                  if (log4j.isDebugEnabled()) log4j.debug("priceActual:" + Double.toString(priceActual.doubleValue()));
                  discount = new BigDecimal ((priceList.doubleValue()-priceActual.doubleValue()) / priceList.doubleValue() * 100.0);
                }
                if (log4j.isDebugEnabled()) log4j.debug("Discount: " + discount.toString());
                if (discount.scale() > StdPrecision){
                	discount = discount.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
                }
                if (log4j.isDebugEnabled()) log4j.debug("Discount rounded: " + discount.toString());
                
                strDiscount = discount.toString();
                priceactual = priceActual.toString();
                pricelist = priceList.toString();
                }
              }
              if (priceactual.equals("")) priceactual="0";
              if (pricelist.equals("")) pricelist="0";
              if (pricelimit.equals("")) pricelimit="0";
            } else {
            priceactual = data[i].invoiceprice;
            pricelist = "0";
            pricelimit = "0";
            }
            String strCTaxID = Tax.get(this, data[i].mProductId, DateTimeData.today(this), data[i].adOrgId, data[i].mWarehouseId.equals("")?vars.getWarehouse():data[i].mWarehouseId, ExpenseSOrderData.cBPartnerLocationId(this, data[i].cBpartnerId), ExpenseSOrderData.cBPartnerLocationId(this, data[i].cBpartnerId), data[i].cProjectId, true);

            ExpenseSOrderData.insertCOrderline(conn, this, strCOrderlineID, data[i].adClientId, strOrganization.equals("")?data[i].adOrgId:strOrganization, vars.getUser(), strCOrderId, Integer.toString(line), data[i].cBpartnerId, ExpenseSOrderData.cBPartnerLocationId(this, data[i].cBpartnerId), DateTimeData.today(this), DateTimeData.today(this), data[i].description, data[i].mProductId, data[i].mWarehouseId.equals("")?vars.getWarehouse():data[i].mWarehouseId, data[i].cUomId.equals("")?Utility.getContext(this, vars, "#C_UOM_ID", "ExpenseSOrder"):data[i].cUomId, data[i].qty, strCCurrencyId, pricelist, priceactual, pricelimit, strCTaxID, data[i].sResourceassignmentId, strDiscount);

            ExpenseSOrderData.updateTimeExpenseLine(conn, this, strCOrderlineID, data[i].sTimeexpenselineId);
      }
      releaseCommitConnection(conn);
      if (!strCOrderId.equals("")) {
        // Automatically processes Sales Order
    	if (strCompleteAuto.equals("Y")) {
	        String mensaje = processOrder(vars, strCOrderId);
	        if (!mensaje.equals("")) textoMensaje.append(mensaje).append("\n");
    	}
      }
      myMessage.setType("Success");
      myMessage.setMessage(textoMensaje.toString() + Utility.messageBD(this, "Created", vars.getLanguage()) + ": " + Integer.toString(total));
      return myMessage;
      //return (textoMensaje.toString() + Utility.messageBD(this, "Created", vars.getLanguage()) + ": " + Integer.toString(total));
      /*String [] allOrder = new String[order.size()];
      order.toArray(allOrder);
      if (processOrderPost(vars, allOrder)) return Utility.messageBD(this, "ProcessOK", vars.getLanguage());
      else return "Se han creado los pedidos de venta pero no se han podido procesar";*/
    } catch (Exception e) {
      try {
        if (conn!=null) releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage.setType("Error");
      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      return myMessage;
      //return Utility.messageBD(this, "ProcessRunError", vars.getLanguage());
    }
  }
    //processes the created order
    /*boolean processOrderPost(VariablesSecureApp vars, String [] allOrder) {
      Connection conn = this.getTransactionConnection();
      try {
        for (int i=0;i<allOrder.length;i++) {
          String pinstance = SequenceIdData.getSequence(this, "AD_PInstance", vars.getClient());
          ExpenseSOrderData.insertPInstance(conn, this, pinstance, "104", allOrder[i], vars.getUser(), vars.getClient(), vars.getOrg());
          ExpenseSOrderData.processOrder(conn, this, pinstance);
        }
        releaseCommitConnection(conn);
        return true;
      } catch (ServletException e) {
        releaseRollbackConnection(conn);
        return false;
      }

    }*/

  String processOrder(VariablesSecureApp vars, String strCOrderId) throws ServletException {
    String pinstance = SequenceIdData.getSequence(this, "AD_PInstance", vars.getClient());
    PInstanceProcessData.insertPInstance(this, pinstance, "104", strCOrderId, "N", vars.getUser(), vars.getClient(), vars.getOrg());
    ActionButtonData.process104(this, pinstance);

    PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
    String messageResult="";
    if (pinstanceData!=null && pinstanceData.length>0) {
      if (!pinstanceData[0].errormsg.equals("")) {
        String message = pinstanceData[0].errormsg;
        if (message.startsWith("@") && message.endsWith("@")) {
          message = message.substring(1, message.length()-1);
          if (message.indexOf("@")==-1) messageResult = Utility.messageBD(this, message, vars.getLanguage());
          else messageResult = Utility.parseTranslation(this, vars, vars.getLanguage(), "@" + message + "@");
        } else {
          messageResult = Utility.parseTranslation(this, vars, vars.getLanguage(), message);
        }
      } else if (!pinstanceData[0].pMsg.equals("")) {
        String message = pinstanceData[0].pMsg;
        messageResult = Utility.parseTranslation(this, vars, vars.getLanguage(), message);
      } else if (pinstanceData[0].result.equals("1")) {
        messageResult = "";
      } else {
        messageResult = Utility.messageBD(this, "Error", vars.getLanguage());
      }
    }
    messageResult = Replace.replace(messageResult, "'", "\\'");
    if (log4j.isDebugEnabled()) log4j.debug(messageResult);
    return (messageResult);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDatefrom,String strDateto,String strDateOrdered,String strBPartner, String strOrganization, String strCompleteAuto) throws IOException, ServletException {
	  if (log4j.isDebugEnabled()) log4j.debug("Output: process ExpenseSOrder");
      
      String[] discard = {""};
      String strHelp = ExpenseSOrderData.help(this, "S_ExpenseSOrder");
      if (strHelp.equals("")) discard[0] = new String("helpDiscard");

      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/ExpenseSOrder").createXmlDocument();
      
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ExpenseSOrder", false, "", "", "",false, "ad_actionButton",  strReplaceWith, false,  true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString()); 
      
      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
      xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("help", strHelp);
      xmlDocument.setParameter("Bpartnerdescription", ExpenseSOrderData.selectBpartner(this, strBPartner));
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
      xmlDocument.setParameter("description", ExpenseSOrderData.description(this, "S_ExpenseSOrder"));
      xmlDocument.setData("structureOrganizacion", OrganizationComboData.select(this, vars.getRole()));
      
      //New interface parameters
      try {
    	  WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_actionButton.ExpenseSOrder");
    	  
    	  xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
		  xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
		  xmlDocument.setParameter("childTabContainer", tabs.childTabs());
		  xmlDocument.setParameter("theme", vars.getTheme());
		  NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "ExpenseSOrder.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
		  xmlDocument.setParameter("navigationBar", nav.toString());
		  LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "ExpenseSOrder.html", strReplaceWith);
		  xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
		} catch (Exception ex) {
		  throw new ServletException(ex);
		}
		{
		  OBError myMessage = vars.getMessage("ExpenseSOrder");
		  vars.removeMessage("ExpenseSOrder");
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
    return "Servlet Project set Type";
  } // end of getServletInfo() method
}

