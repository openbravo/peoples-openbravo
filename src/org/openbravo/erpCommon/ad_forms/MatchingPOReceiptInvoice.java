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
package org.openbravo.erpCommon.ad_forms;

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


// imports for transactions
import java.sql.Connection;


public class MatchingPOReceiptInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (!Utility.hasFormAccess(this, vars, "", "org.openbravo.erpCommon.ad_forms.MatchingPOReceiptInvoice")) {
      bdError(response, "AccessTableNoView", vars.getLanguage());
      return;
    }

    if (vars.commandIn("DEFAULT")) {
      vars.getRequestGlobalVariable("inpsourcedoc", "MatchingPO|sourcedoc");
      vars.getRequestGlobalVariable("inpfinaldoc", "MatchingPO|finaldoc");
      vars.getRequestGlobalVariable("inpsearchmode", "MatchingPO|searchmode");
      vars.getRequestGlobalVariable("inpcBpartnerId", "MatchingPO|cBpartnerId");
      vars.getRequestGlobalVariable("inpDesde", "MatchingPO|desde");
      vars.getRequestGlobalVariable("inpHasta", "MatchingPO|hasta");
      vars.getRequestGlobalVariable("inpmProductId", "MatchingPO|mProductId");
      printPageFS(response, vars);
    } else if (vars.commandIn("RELOAD")) {
      vars.getGlobalVariable("inpsourcedoc", "MatchingPO|sourcedoc", "");
      vars.getGlobalVariable("inpfinaldoc", "MatchingPO|finaldoc", "");
      vars.getGlobalVariable("inpsearchmode", "MatchingPO|searchmode", "");
      vars.getGlobalVariable("inpcBpartnerId", "MatchingPO|cBpartnerId", "");
      vars.getGlobalVariable("inpDesde", "MatchingPO|desde", "");
      vars.getGlobalVariable("inpHasta", "MatchingPO|hasta", "");
      vars.getGlobalVariable("inpmProductId", "MatchingPO|mProductId", "");
      printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strSourceDoc = vars.getGlobalVariable("inpsourcedoc", "MatchingPO|sourcedoc", "");
      String strFinalDoc = vars.getGlobalVariable("inpfinaldoc", "MatchingPO|finaldoc", "");
      String strSearchMode = vars.getGlobalVariable("inpsearchmode", "MatchingPO|searchmode", "");
      String strBpartner = vars.getGlobalVariable("inpcBpartnerId", "MatchingPO|cBpartnerId", "");
      String strDesde = vars.getGlobalVariable("inpDesde", "MatchingPO|desde", "");
      String strHasta = vars.getGlobalVariable("inpHasta", "MatchingPO|hasta", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "MatchingPO|mProductId", "");
      printPageF1(response, vars, strSourceDoc, strFinalDoc, strSearchMode, strBpartner, strDesde, strHasta, strProduct);
    } else if (vars.commandIn("FIND")) {
      String strSourceDoc = vars.getRequestGlobalVariable("inpsourcedoc", "MatchingPO|sourcedoc");
      String strFinalDoc = vars.getRequestGlobalVariable("inpfinaldoc", "MatchingPO|finaldoc");
      String strSearchMode = vars.getRequestGlobalVariable("inpsearchmode", "MatchingPO|searchmode");
      String strBpartner = vars.getRequestGlobalVariable("inpcBpartnerId", "MatchingPO|cBpartnerId");
      String strDesde = vars.getRequestGlobalVariable("inpDesde", "MatchingPO|desde");
      String strHasta = vars.getRequestGlobalVariable("inpHasta", "MatchingPO|hasta");
      String strProduct = vars.getRequestGlobalVariable("inpmProductId", "MatchingPO|mProductId");
      printPageF2(response, vars, strSourceDoc, strFinalDoc, strSearchMode, strBpartner, strDesde, strHasta, strProduct);
    } else if (vars.commandIn("FRAME2")) {
      String strSourceDoc = vars.getGlobalVariable("inpsourcedoc", "MatchingPO|sourcedoc", "");
      String strFinalDoc = vars.getGlobalVariable("inpfinaldoc", "MatchingPO|finaldoc", "");
      String strSearchMode = vars.getGlobalVariable("inpsearchmode", "MatchingPO|searchmode", "");
      String strBpartner = vars.getGlobalVariable("inpcBpartnerId", "MatchingPO|cBpartnerId", "");
      String strDesde = vars.getGlobalVariable("inpDesde", "MatchingPO|desde", "");
      String strHasta = vars.getGlobalVariable("inpHasta", "MatchingPO|hasta", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "MatchingPO|mProductId", "");
      printPageF2(response, vars, strSourceDoc, strFinalDoc, strSearchMode, strBpartner, strDesde, strHasta, strProduct);
    } else if (vars.commandIn("FRAME3")) {
      printPageF3(response, vars);
    } else if (vars.commandIn("FRAME4", "FIND_SECONDARY")) {
      String strClave = vars.getStringParameter("inpClave");
      String strType = vars.getStringParameter("inpType");
      String strSameBpartner = vars.getStringParameter("inpSameBpartner", "0");
      String strSameProduct = vars.getStringParameter("inpSameProduct", "0");
      String strSameQty = vars.getStringParameter("inpSameQty", "0");
      String strSourceDoc = vars.getStringParameter("inpsourcedoc");
      String strSearchMode = vars.getStringParameter("inpsearchmode", "0");
      String strDesde = vars.getStringParameter("inpDesde");
      String strHasta = vars.getStringParameter("inpHasta");
      printPageF4(response, vars, strClave, strSourceDoc, strType, strSameBpartner, strSameProduct, strSameQty, strSearchMode, strDesde, strHasta);
    } else if (vars.commandIn("FRAME5")) {
      printPageF5(response, vars);
    } else if (vars.commandIn("PROCESS")) {
      String strMatched = vars.getRequiredStringParameter("inpMatchLine");
      String strProduct = vars.getStringParameter("inpmProductId");
      String strSearchMode = vars.getStringParameter("inpsearchmode");
      String strSourceDoc = vars.getStringParameter("inpsourcedoc");
      String strFinalDoc = vars.getStringParameter("inpfinaldoc");
      String strMatchTo = vars.getInStringParameter("inpClave");
      String strMatchQty = vars.getStringParameter("inpMatchQty");
      processMatch(vars, strMatched, strProduct, strSearchMode, strSourceDoc, strFinalDoc, strMatchTo, strMatchQty);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=RELOAD");
    } else pageError(response);
  }

  private void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException{
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_FS").createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageF1(HttpServletResponse response, VariablesSecureApp vars, String strSourceDoc, String strFinalDoc, String strSearchMode, String strBpartner, String strDesde, String strHasta, String strProduct) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F1").createXmlDocument();

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");

    xmlDocument.setParameter("sourceDoc", strSourceDoc);
    xmlDocument.setParameter("finalDoc", strFinalDoc);
    xmlDocument.setParameter("searchMode", strSearchMode);
    xmlDocument.setParameter("bpartner", strBpartner);
    xmlDocument.setParameter("desde", strDesde);
    xmlDocument.setParameter("hasta", strHasta);
    xmlDocument.setParameter("product", strProduct);
    xmlDocument.setParameter("productName", MatchingPOReceiptInvoiceData.product(this, strProduct));
    xmlDocument.setParameter("bpartnerName", MatchingPOReceiptInvoiceData.bpartner(this, strBpartner));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageF2(HttpServletResponse response, VariablesSecureApp vars, String strSourceDoc, String strFinalDoc, String strSearchMode, String strBpartner, String strDesde, String strHasta, String strProduct) throws IOException, ServletException {
    XmlDocument xmlDocument = null;
    if (strSourceDoc.equals("") && strSearchMode.equals("") && strBpartner.equals("") && strDesde.equals("") && strHasta.equals("") && strProduct.equals("")) {
      String[] discard={"sectionDetail"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.set());
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F2").createXmlDocument();
      if (strSourceDoc.equals("1")) { // Invoice
        xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.selectFactura(this, strProduct, strBpartner, "", strDesde, strHasta, Utility.getContext(this, vars, "#User_Org", "MatchingPO"), Utility.getContext(this, vars, "#User_Client", "MatchingPO"), "", (strSearchMode.equals("1")?"0":"lin.QtyInvoiced")));
      } else if (strSourceDoc.equals("2")) { // Delivery note
        xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.selectAlbaran(this, (strFinalDoc.equals("3")?"M_MatchPO":"M_MatchInv"), strProduct, strBpartner, "", strDesde, strHasta, Utility.getContext(this, vars, "#User_Org", "MatchingPO"), Utility.getContext(this, vars, "#User_Client", "MatchingPO"), "", (strSearchMode.equals("1")?"0":"lin.MovementQty")));
      } else if (strSourceDoc.equals("3")) { //Procured order
        xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.select(this, strProduct, strBpartner, "", strDesde, strHasta, Utility.getContext(this, vars, "#User_Org", "MatchingPO"), Utility.getContext(this, vars, "#User_Client", "MatchingPO"), "", (strSearchMode.equals("1")?"0":"lin.QtyOrdered")));
      }
    }

    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageF3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException{
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F3").createXmlDocument();
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private MatchingPOReceiptInvoiceData[] getLine(VariablesSecureApp vars, String strClave, String strSourceDoc) throws ServletException {
    if (strSourceDoc.equals("1")) return MatchingPOReceiptInvoiceData.selectLineFactura(this, strClave);
    else if (strSourceDoc.equals("2")) return MatchingPOReceiptInvoiceData.selectLineAlbaran(this, strClave);
    else return MatchingPOReceiptInvoiceData.selectLine(this, strClave);
  }

  private void printPageF4(HttpServletResponse response, VariablesSecureApp vars, String strClave, String strSourceDoc, String strType, String strSameBpartner, String strSameProduct, String strSameQty, String strSearchMode, String strDesde, String strHasta) throws IOException, ServletException{
    XmlDocument xmlDocument = null;
    String strProduct="";
    String[] discard = {"sectionDetail"};
    if (strClave.equals("") || strType.equals("")) {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F4", discard).createXmlDocument();
      xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.set());
    } else {
      MatchingPOReceiptInvoiceData[] data = null;
      if (strType.equals("1")) { //Invoice
        data = getLine(vars, strClave, strSourceDoc);
        if (data==null || data.length==0) {
          xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F4", discard).createXmlDocument();
          xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.set());
        } else {
          xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F4").createXmlDocument();
          strProduct = data[0].mProductId;
          xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.selectFactura(this, (strSameProduct.equals("0")?"":data[0].mProductId), (strSameBpartner.equals("0")?"":data[0].cBpartnerId), (strSameQty.equals("0")?"":data[0].qty), strDesde, strHasta, Utility.getContext(this, vars, "#User_Org", "MatchingPO"), Utility.getContext(this, vars, "#User_Client", "MatchingPO"), "", (strSearchMode.equals("1")?"0":"lin.QtyInvoiced")));
        }
      } else if (strType.equals("2")) { //Delivery note
        data = getLine(vars, strClave, strSourceDoc);
        if (data==null || data.length==0) {
          xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F4", discard).createXmlDocument();
          xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.set());
        } else {
          xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F4").createXmlDocument();
          strProduct = data[0].mProductId;
          xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.selectAlbaran(this, (strSourceDoc.equals("3")?"M_MatchPO":"M_MatchInv"), (strSameProduct.equals("0")?"":data[0].mProductId), (strSameBpartner.equals("0")?"":data[0].cBpartnerId), (strSameQty.equals("0")?"":data[0].qty), strDesde, strHasta, Utility.getContext(this, vars, "#User_Org", "MatchingPO"), Utility.getContext(this, vars, "#User_Client", "MatchingPO"), "", (strSearchMode.equals("1")?"0":"lin.MovementQty")));
        }
      } else if (strType.equals("3")) { //Prucured order
        data = getLine(vars, strClave, strSourceDoc);
        if (data==null || data.length==0) {
          xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F4", discard).createXmlDocument();
          xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.set());
        } else {
          xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F4").createXmlDocument();
          strProduct = data[0].mProductId;
          xmlDocument.setData("structure1", MatchingPOReceiptInvoiceData.select(this, (strSameProduct.equals("0")?"":data[0].mProductId), (strSameBpartner.equals("0")?"":data[0].cBpartnerId), (strSameQty.equals("0")?"":data[0].qty), strDesde, strHasta, Utility.getContext(this, vars, "#User_Org", "MatchingPO"), Utility.getContext(this, vars, "#User_Client", "MatchingPO"), "", (strSearchMode.equals("1")?"0":"lin.QtyOrdered")));
        }
      }
    }
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("searchMode", strSearchMode);
    xmlDocument.setParameter("product", strProduct);
    xmlDocument.setParameter("sourceDoc", strSourceDoc);
    xmlDocument.setParameter("finalDoc", strType);
    xmlDocument.setParameter("matchLine", strClave);
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageF5(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/MatchingPO-Receipt-Invoice_F5").createXmlDocument();

    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void processMatch(VariablesSecureApp vars, String strMatched, String strProduct, String strSearchMode, String strSourceDoc, String strFinalDoc, String strMatchTo, String strMatchQty) throws ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("processing match line: " + strMatched + " - Qty: " + strMatchQty);
    MatchingPOReceiptInvoiceData[] data = null;
    if (strFinalDoc.equals("1")) { //Invoice
      data = MatchingPOReceiptInvoiceData.selectFactura(this, "", "", "", "", "", Utility.getContext(this, vars, "#User_Org", "MatchingPO"), Utility.getContext(this, vars, "#User_Client", "MatchingPO"), strMatchTo, (strSearchMode.equals("1")?"0":"lin.QtyInvoiced"));
    } else if (strFinalDoc.equals("2")) { //Delivery note
      data = MatchingPOReceiptInvoiceData.selectAlbaran(this, (strSourceDoc.equals("3")?"M_MatchPO":"M_MatchInv"), "", "", "", "", "", Utility.getContext(this, vars, "#User_Org", "MatchingPO"), Utility.getContext(this, vars, "#User_Client", "MatchingPO"), strMatchTo, (strSearchMode.equals("1")?"0":"lin.MovementQty"));
    } else if (strFinalDoc.equals("3")) { //Procured order
      data = MatchingPOReceiptInvoiceData.select(this, "", "", "", "", "", Utility.getContext(this, vars, "#User_Org", "MatchingPO"), Utility.getContext(this, vars, "#User_Client", "MatchingPO"), strMatchTo, (strSearchMode.equals("1")?"0":"lin.QtyOrdered"));
    }
    if (data==null || data.length==0) return;
    double totalQty = (strMatchQty.equals("")?0.0D:Double.valueOf(strMatchQty).doubleValue());
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();
      for (int i=0;i<data.length;i++) {
        if (!strProduct.equals(data[i].mProductId)) continue;
        double qty=0.0D;
        if (strSearchMode.equals("0")) qty = Double.valueOf(data[i].qty).doubleValue();
        qty -= (data[i].qtyMatched.equals("")?0.0D:Double.valueOf(data[i].qtyMatched).doubleValue());
        if (qty > totalQty) qty = totalQty;
        totalQty -= qty;
        boolean invoice = true;
        if (strSourceDoc.equals("3") || strFinalDoc.equals("3")) invoice=false;
        String M_InOutLine_ID = "";
        String Line_ID = "";
        if(strSourceDoc.equals("2")) {
            M_InOutLine_ID = strMatched;
            Line_ID = data[i].id;
        } else {
            M_InOutLine_ID = data[i].id;
            Line_ID = strMatched;
        }
        if (log4j.isDebugEnabled()) log4j.debug("createMatchRecord IsInvoiced: " + invoice + " - M_InOutLine_ID: " + M_InOutLine_ID + " - Line_ID: " + Line_ID + " - M_Product_ID: " + strProduct + " - Qty: " + qty);
        if (invoice) {
          String strSequence = SequenceIdData.getSequence(this, "M_MatchInv", vars.getClient());
          MatchingPOReceiptInvoiceData.insertInvoice(conn, this, strSequence, vars.getClient(), vars.getOrg(), vars.getUser(), M_InOutLine_ID, Line_ID, strProduct, Double.toString(qty));
        } else {
          String strSequence = SequenceIdData.getSequence(this, "M_MatchPO", vars.getClient());
          MatchingPOReceiptInvoiceData.insert(conn, this, strSequence, vars.getClient(), vars.getOrg(), vars.getUser(), M_InOutLine_ID, Line_ID, strProduct, Double.toString(qty));
          //MatchingPOReceiptInvoiceData.update(conn, this, Double.toString(qty), strProduct, M_InOutLine_ID);
        }
      }
      releaseCommitConnection(conn);
    } catch(Exception e){
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {}
      e.printStackTrace();
      throw new ServletException(e);
    }
  }


  public String getServletInfo() {
    return "marchingPO Servlet";
  } // end of getServletInfo() method
}
