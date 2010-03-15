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
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpReports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.xmlEngine.XmlDocument;

public class RptC_Bpartner extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strcBpartnerId = vars.getSessionValue("RptC_Bpartner.inpcBpartnerId_R");
      /*
       * String strcBpartnerId = vars.getStringParameter("inpcBpartnerId");
       */
      if (strcBpartnerId.equals(""))
        strcBpartnerId = vars.getSessionValue("RptC_Bpartner.inpcBpartnerId");
      printPageDataSheet(response, vars, strcBpartnerId);
    } else if (vars.commandIn("OPEN")) {
      String strcBpartnerId = vars.getRequiredStringParameter("inpcBpartnerId");
      String strmProductTemplate = vars.getRequiredStringParameter("inpProductTemplate");
      printPageAjaxResponse(response, vars, strcBpartnerId, strmProductTemplate);
    } else if (vars.commandIn("OPENDOCUMENT")) {
      String strcBpartnerId = vars.getRequiredStringParameter("inpcBpartnerId");
      String strmTypeDocument = vars.getRequiredStringParameter("inpTypeDocument");
      printPageAjaxDocumentResponse(response, vars, strcBpartnerId, strmTypeDocument);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strcBpartnerId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "", "", "", "", "", "", "", "", "", "", "" };
    XmlDocument xmlDocument = null;

    RptCBpartnerData[] dataPartner = RptCBpartnerData.select(this, vars.getLanguage(),
        strcBpartnerId);
    RptCBpartnerData[] dataAccount = RptCBpartnerData.selectAccount(this, strcBpartnerId);
    RptCBpartnerData[] dataShipper = RptCBpartnerData.selectShipper(this, strcBpartnerId);
    RptCBpartnerData[] dataTemplate = RptCBpartnerData.selectTemplate(this, vars.getLanguage(),
        strcBpartnerId);
    RptCBpartnerData[] dataDiscount = RptCBpartnerData.selectDiscount(this, strcBpartnerId);
    RptCBpartnerCustomerData[] dataCustomer = RptCBpartnerCustomerData.select(this, vars
        .getLanguage(), strcBpartnerId);
    RptCBpartnerVendorData[] dataVendor = RptCBpartnerVendorData.select(this, vars.getLanguage(),
        strcBpartnerId);
    RptCBpartnerlocationData[] dataLocation = RptCBpartnerlocationData.select(this, strcBpartnerId);
    RptCBpartnercontactData[] dataContact = RptCBpartnercontactData.select(this, strcBpartnerId);
    RptCBpartnerSalesData[] dataSales = RptCBpartnerSalesData.selectOrder(this, strcBpartnerId);
    RptCBpartnerSalesData[] dataInvoice = RptCBpartnerSalesData.select(this, strcBpartnerId);
    RptCBpartnerSalesData[] dataInout = RptCBpartnerSalesData.selectinout(this, strcBpartnerId);
    RptCBpartnerSalesData[] dataABC = RptCBpartnerSalesData.selectABC(this, DateTimeData
        .sysdateYear(this), DateTimeData.lastYear(this), strcBpartnerId);

    if (dataAccount == null || dataAccount.length == 0) {
      dataAccount = RptCBpartnerData.set();
      discard[0] = "selDelete1";
    }
    if (dataShipper == null || dataShipper.length == 0) {
      dataShipper = RptCBpartnerData.set();
      discard[1] = "selDelete2";
    }
    if (dataTemplate == null || dataTemplate.length == 0) {
      dataTemplate = RptCBpartnerData.set();
      discard[2] = "selDelete4";
    }
    if (dataDiscount == null || dataDiscount.length == 0) {
      dataDiscount = RptCBpartnerData.set();
      discard[3] = "selDelete3";
    }
    if (dataCustomer == null || dataCustomer.length == 0) {
      dataCustomer = RptCBpartnerCustomerData.set();
      discard[4] = "selDelete5";
    }
    if (dataVendor == null || dataVendor.length == 0) {
      dataVendor = RptCBpartnerVendorData.set();
      discard[5] = "selDelete6";
    }
    if (dataLocation == null || dataLocation.length == 0) {
      dataLocation = RptCBpartnerlocationData.set();
      discard[6] = "selDelete7";
    }
    if (dataContact == null || dataContact.length == 0) {
      dataContact = RptCBpartnercontactData.set();
      discard[7] = "selDelete8";
    }
    if (dataSales == null || dataSales.length == 0) {
      dataSales = RptCBpartnerSalesData.set();
      discard[8] = "selDelete9";
    }
    if (dataSales == null || dataSales.length == 0) {
      dataInvoice = RptCBpartnerSalesData.set();
      discard[9] = "selDelete10";
    }
    if (dataSales == null || dataSales.length == 0) {
      dataInout = RptCBpartnerSalesData.set();
      discard[10] = "selDelete11";
    }

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_Bpartner", discard)
        .createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("paramBpartner", dataPartner[0].cBpartnerId);
    xmlDocument.setParameter("paramSysdate", DateTimeData.today(this));

    xmlDocument.setData("structureAccount", dataAccount);
    xmlDocument.setData("structureShipper", dataShipper);
    xmlDocument.setData("structureTemplate", dataTemplate);
    xmlDocument.setData("structureDiscount", dataDiscount);
    xmlDocument.setData("structureCustomer", dataCustomer);
    xmlDocument.setData("structureVendor", dataVendor);
    xmlDocument.setData("structureLocation", dataLocation);
    xmlDocument.setData("structureContact", dataContact);
    xmlDocument.setData("structureSalesorder", dataSales);
    xmlDocument.setData("structureSalesinvoice", dataInvoice);
    xmlDocument.setData("structureSalesinout", dataInout);
    xmlDocument.setData("structureABC", dataABC);
    xmlDocument.setData("structure1", dataPartner);

    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageAjaxResponse(HttpServletResponse response, VariablesSecureApp vars,
      String strcBpartnerId, String strmProductTemplate) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajaxreponse");
    XmlDocument xmlDocument = null;

    RptCBpartnerData[] data = RptCBpartnerData.selectTemplateDetail(this, strcBpartnerId,
        strmProductTemplate);

    if (data == null || data.length == 0)
      data = RptCBpartnerData.set();

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerTemplateLines")
        .createXmlDocument();

    response.setContentType("text/plain; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();

    xmlDocument.setData("structure", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageAjaxDocumentResponse(HttpServletResponse response, VariablesSecureApp vars,
      String strcBpartnerId, String strmTypeDocument) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: ajaxreponse");
    XmlDocument xmlDocument = null;

    RptCBpartnerSalesData[] data = null;
    RptCBpartnerSalesData[] dataPeriod = RptCBpartnerSalesData.selectperiod(this);

    if (strmTypeDocument.equals("INVOICE")) {
      data = RptCBpartnerSalesData.selectInvoiceperiod(this, strcBpartnerId);
      xmlDocument = xmlEngine
          .readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerPeriodInvoice")
          .createXmlDocument();
      xmlDocument.setData("structurePeriod", dataPeriod);
      if (data == null || data.length == 0)
        data = RptCBpartnerSalesData.set();
    }
    if (strmTypeDocument.equals("ORDER")) {
      data = RptCBpartnerSalesData.selectOrderperiod(this, strcBpartnerId);
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerPeriodSales")
          .createXmlDocument();
      xmlDocument.setData("structurePeriod", dataPeriod);
      if (data == null || data.length == 0)
        data = RptCBpartnerSalesData.set();
    }
    if (strmTypeDocument.equals("INOUT")) {
      data = RptCBpartnerSalesData.selectInoutperiod(this, strcBpartnerId);
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerPeriodInout")
          .createXmlDocument();
      xmlDocument.setData("structurePeriod", dataPeriod);
      if (data == null || data.length == 0)
        data = RptCBpartnerSalesData.set();
    }
    if (strmTypeDocument.equals("ABC")) {
      data = RptCBpartnerSalesData.selectABCactualdetail(this, DateTimeData.sysdateYear(this),
          strcBpartnerId);
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerABC")
          .createXmlDocument();
    }
    if (strmTypeDocument.equals("ABCREF")) {
      data = RptCBpartnerSalesData.selectABCrefdetail(this, DateTimeData.lastYear(this),
          strcBpartnerId);
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpReports/RptC_BpartnerABCref")
          .createXmlDocument();
    }

    response.setContentType("text/plain; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    xmlDocument.setData("structure", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet RptC_Bpartner. This Servlet was made by Pablo Sarobe";
  } // End of getServletInfo() method
}
