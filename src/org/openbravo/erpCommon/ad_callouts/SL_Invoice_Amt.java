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
package org.openbravo.erpCommon.ad_callouts;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.erpCommon.utility.*;
import java.io.*;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;

public class SL_Invoice_Amt extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  static final BigDecimal ZERO = new BigDecimal(0.0);

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled()) log4j.debug("CHANGED: " + strChanged);
      String strQtyInvoice = vars.getStringParameter("inpqtyinvoiced");
      String strPriceActual = vars.getStringParameter("inppriceactual");
      String strPriceLimit = vars.getStringParameter("inppricelimit");
      String strInvoiceId = vars.getStringParameter("inpcInvoiceId");
      String strProduct = vars.getStringParameter("inpmProductId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strPriceList = vars.getStringParameter("inppricelist");
      
      try {
        printPage(response, vars, strChanged, strQtyInvoice, strPriceActual, strInvoiceId, strProduct, strPriceLimit, strTabId, strPriceList);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged, String strQtyInvoice, String strPriceActual, String strInvoiceId, String strProduct, String strPriceLimit, String strTabId, String strPriceList) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLInvoiceAmtData[] data = SLInvoiceAmtData.select(this, strInvoiceId);
    String strPrecision = "0", strPricePrecision="0";
    boolean enforcedLimit=false;
    if (data!=null && data.length>0) {
      strPrecision = data[0].stdprecision;
      strPricePrecision = data[0].priceprecision;
      enforcedLimit = (data[0].enforcepricelimit.equals("Y")?true:false);
    }
    int StdPrecision = Integer.valueOf(strPrecision).intValue();
    int PricePrecision = Integer.valueOf(strPricePrecision).intValue();

    BigDecimal qtyInvoice, priceActual, LineNetAmt, priceLimit;

    if (strQtyInvoice.equals("")) qtyInvoice=ZERO;
    else qtyInvoice = new BigDecimal(strQtyInvoice);
    if (strChanged.equals("inpqtyinvoiced")||strChanged.equals("inppricelist"))
    {
      if (log4j.isDebugEnabled()) log4j.debug("strPriceList: "+strPriceList.replace("\"", "")+" product:"+strProduct+" qty:"+strQtyInvoice);
      strPriceActual = SLOrderProductData.getOffersPriceInvoice(this, data[0].dateinvoiced, data[0].cBpartnerId, strProduct, strPriceList.replace("\"", ""), strQtyInvoice, data[0].mPricelistId, data[0].id);
    }

    
    if (strPriceActual.equals("")) strPriceActual = "0";
    if (strPriceLimit.equals("")) strPriceLimit = "0";
 
    if (log4j.isDebugEnabled()) log4j.debug("strPriceActual: "+strPriceActual);
    if (log4j.isDebugEnabled()) log4j.debug("strPriceLimit: "+strPriceLimit);
    priceActual = (new BigDecimal(strPriceActual)).setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
    priceLimit = (new BigDecimal(strPriceLimit)).setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);


    LineNetAmt = qtyInvoice.multiply(priceActual);

    if (LineNetAmt.scale() > StdPrecision)
      LineNetAmt = LineNetAmt.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);


    StringBuffer resultado = new StringBuffer();

    resultado.append("var calloutName='SL_Invoice_Amt';\n\n");
    resultado.append("var respuesta = new Array(");
    if (enforcedLimit) {
      if (priceLimit.doubleValue() != 0.0 && priceActual.compareTo(priceLimit) < 0) resultado.append("new Array('MESSAGE', \"" + FormatUtilities.replaceJS(Utility.messageBD(this, "UnderLimitPrice", vars.getLanguage())) + "\"), ");
    }
    resultado.append("new Array(\"inplinenetamt\", \"" + LineNetAmt.toString() + "\"),");
    resultado.append("new Array(\"inppriceactual\", \"" + priceActual.toString() + "\")");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "frameAplicacion");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());


    out.close();
  }
}
