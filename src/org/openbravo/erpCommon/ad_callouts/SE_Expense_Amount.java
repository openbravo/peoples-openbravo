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
import org.openbravo.erpCommon.utility.Utility;
import java.io.*;
import java.math.BigDecimal;

import javax.servlet.*;
import javax.servlet.http.*;


public class SE_Expense_Amount extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strExpenseAmt = vars.getStringParameter("inpexpenseamt");
      String strDateexpense = vars.getStringParameter("inpdateexpense");
      String strcCurrencyId = vars.getStringParameter("inpcCurrencyId");
      String strTabId = vars.getStringParameter("inpTabId");
      
      try {
        printPage(response, vars, strExpenseAmt, strDateexpense, strcCurrencyId, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strExpenseAmt, String strDateexpense, String strcCurrencyId, String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    
    String C_Currency_To_ID = Utility.getContext(this, vars, "$C_Currency_ID", "");
    
    BigDecimal Amount = null;
    if (!strExpenseAmt.equals("")) {
      Amount = new BigDecimal(strExpenseAmt);    
    }
    else {
      Amount = new BigDecimal(0.0);
    }
    String strPrecision = "0";    
    if (!strcCurrencyId.equals("")){
      strPrecision = SEExpenseAmountData.selectPrecision(this, strcCurrencyId);
    }
    int StdPrecision = Integer.valueOf(strPrecision).intValue();
    if (Amount.scale() > StdPrecision)
      Amount = Amount.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
    
    String convertedAmount = strExpenseAmt;    
    BigDecimal ConvAmount = Amount;    
    
    if (!strcCurrencyId.equals(C_Currency_To_ID)){
      String strPrecisionConv = "0";    
      if (!C_Currency_To_ID.equals("")){
        strPrecisionConv = SEExpenseAmountData.selectPrecision(this, C_Currency_To_ID);
      }
      int StdPrecisionConv = Integer.valueOf(strPrecisionConv).intValue();
      convertedAmount = SEExpenseAmountData.selectConvertedAmt(this, strExpenseAmt, strcCurrencyId, C_Currency_To_ID, strDateexpense, vars.getClient(), vars.getOrg());      
      if (!convertedAmount.equals("")) {
        ConvAmount = new BigDecimal(convertedAmount);
      } else {
        ConvAmount = new BigDecimal(0.0);
      }
      if (ConvAmount.scale() > StdPrecisionConv)
        ConvAmount = ConvAmount.setScale(StdPrecisionConv, BigDecimal.ROUND_HALF_UP);      
    }
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SE_Expense_Amount';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpexpenseamt\", \"" + Amount.toString() + "\")");
    resultado.append(", new Array(\"inpconvertedamt\", \"" + ConvAmount.toString() + "\")");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "frameAplicacion");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
