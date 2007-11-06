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
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.erpCommon.utility.DateTimeData;
import java.math.BigDecimal;


public class SE_Expense_Product extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strDateexpense = vars.getStringParameter("inpdateexpense", DateTimeData.today(this));
      String strmProductId = vars.getStringParameter("inpmProductId");
      //String strUOM = vars.getStringParameter("inpmProductId_UOM");
      String strsTimeexpenseId = vars.getStringParameter("inpsTimeexpenseId");
      String strqty = vars.getStringParameter("inpqty");
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      String strTabId = vars.getStringParameter("inpTabId");
      
      try {
        printPage(response, vars, strDateexpense, strmProductId, strsTimeexpenseId, strqty, strChanged, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDateexpense, String strmProductId, String strsTimeexpenseId, String strqty, String strChanged, String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    String strmPricelistId = SEExpenseProductData.priceList(this, strsTimeexpenseId);
    SEExpenseProductData[] data = SEExpenseProductData.select(this, strmProductId, strmPricelistId);
    String strUOM = SEExpenseProductData.selectUOM(this, strmProductId);
    boolean noPrice = true;
    String priceActual = "";
    String CCurrencyID = "";
    BigDecimal Qty = new BigDecimal(strqty);
    BigDecimal Amount = null;
    for (int i=0;data!=null && i<data.length && noPrice;i++){
      if (data[i].validfrom == null  || data[i].validfrom.equals("") || !DateTimeData.compare(this, strDateexpense, data[i].validfrom).equals("-1")){
        noPrice = false;
        //	Price
        priceActual = data[i].pricestd;
        if (priceActual.equals(""))
        priceActual = data[i].pricelist;
        if (priceActual.equals(""))
        priceActual = data[i].pricelimit;
        //	Currency
        CCurrencyID = data[i].cCurrencyId;
      }
    }
    if (noPrice){
      data = SEExpenseProductData.selectBasePriceList(this, strmProductId, strmPricelistId);
      for (int i=0;data!=null && i<data.length && noPrice;i++){
        if (data[i].validfrom == null  || data[i].validfrom.equals("") || !DateTimeData.compare(this, strDateexpense, data[i].validfrom).equals("-1")){
          noPrice = false;
          //	Price
          priceActual = data[i].pricestd;
          if (priceActual.equals(""))
          priceActual = data[i].pricelist;
          if (priceActual.equals(""))
          priceActual = data[i].pricelimit;
          //	Currency
          CCurrencyID = data[i].cCurrencyId;
        }
      }
    }
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_Expense_Product';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpcUomId\", " + (strUOM.equals("")?"\"\"":strUOM) + ")\n");
    if (!priceActual.equals("") && !CCurrencyID.equals("")) {
      if (!priceActual.equals("")) Amount = new BigDecimal(priceActual);
      else Amount = new BigDecimal("0.0");
      priceActual = Amount.multiply(Qty).toString();
      resultado.append(", new Array(\"inpexpenseamt\", \"" + priceActual + "\")");
      if (strChanged.equals("inpmProductId"))resultado.append(",new Array(\"inpcCurrencyId\", \"" + CCurrencyID + "\")");
    }
    
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "frameAplicacion");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
