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
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.ad_callouts;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;


public class SL_Project_Margin extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  static final BigDecimal ZERO = new BigDecimal(0.0);

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strTabId = vars.getStringParameter("inpTabId");
      String strcProjectId = vars.getStringParameter("inpcProjectId");
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      // Services
      String strServiceRevenue = vars.getStringParameter("inpservrevenue", "0");
      String strServiceCost = vars.getStringParameter("inpservcost", "0");
      String strServiceMargin = vars.getStringParameter("inpservmargin", "0");
      // Expenses
      String strPlannedExpenses = vars.getStringParameter("inpexpexpenses", "0");
      String strReinvoicedExpenses = vars.getStringParameter("inpexpreinvoicing", "0");
      String strPlannedMargin = vars.getStringParameter("inpexpmargin", "0");      
      try {
        printPage(response, vars, strTabId, strcProjectId, strChanged, strServiceRevenue, strServiceCost, strServiceMargin, strPlannedExpenses, strReinvoicedExpenses, strPlannedMargin);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strTabId, String strcProjectId, String strChanged, String strServiceRevenue, String strServiceCost, String strServiceMargin, String strPlannedExpenses, String strReinvoicedExpenses, String strPlannedMargin) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLProjectMarginData[] data = SLProjectMarginData.select(this, strcProjectId);
    String strPrecision = "0";
    if (data!=null && data.length>0) {
      strPrecision = data[0].stdprecision;
    }
    int StdPrecision = Integer.valueOf(strPrecision).intValue();

    BigDecimal ServiceRevenue, PlannedExpenses, ServiceCost, ReinvoicedExpenses, ServiceMargin, ExpensesMargin;
    // Services
    ServiceRevenue = new BigDecimal(strServiceRevenue);			// SR
    ServiceCost = new BigDecimal(strServiceCost);				// SC
    ServiceMargin = new BigDecimal(strServiceMargin);			// SM
    // Expenses
    PlannedExpenses = new BigDecimal(strPlannedExpenses);		// PE
    ReinvoicedExpenses = new BigDecimal(strReinvoicedExpenses);	// RE
    ExpensesMargin = new BigDecimal(strPlannedMargin);			// EM
    
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Project_Margin';\n\n");
    resultado.append("var respuesta = new Array(");
    
    // Services
    if (strChanged.equals("inpservrevenue") || strChanged.equals("inpservcost")){
    	// SM = (SR-SC)*100/SR
    	if (ServiceRevenue.doubleValue() != 0.0) {
        	ServiceMargin = new BigDecimal ((ServiceRevenue.doubleValue()-ServiceCost.doubleValue()) / ServiceRevenue.doubleValue() * 100.0).setScale(2, BigDecimal.ROUND_HALF_UP);
    	} else {
    		ServiceMargin = new BigDecimal(0.0);
    	}
    	resultado.append("\n new Array(\"inpservmargin\", " + ServiceMargin.toString() + ")");
    }
    
    if (strChanged.equals("inpservmargin")){
    	// SC = SR*(1-SM/100)
    	ServiceCost = new BigDecimal ((ServiceRevenue.doubleValue()) * (1 - (ServiceMargin.doubleValue() / 100.0)));
    	if (ServiceCost.scale() > StdPrecision)
    		ServiceCost = ServiceCost.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
    	resultado.append("\n new Array(\"inpservcost\", " + ServiceCost.toString() + ")\n");
    }
    
    // Expenses
    if (strChanged.equals("inpexpexpenses") || strChanged.equals("inpexpreinvoicing")){
    	// EM = (RE-PE)*100/RE
    	if (ReinvoicedExpenses.doubleValue() != 0.0) {
        	ExpensesMargin = new BigDecimal ((ReinvoicedExpenses.doubleValue()-PlannedExpenses.doubleValue()) / ReinvoicedExpenses.doubleValue() * 100.0).setScale(2, BigDecimal.ROUND_HALF_UP);
    	} else {
    		ExpensesMargin = new BigDecimal(0.0);
    	}
    	resultado.append("\n new Array(\"inpexpmargin\", " + ExpensesMargin.toString() + ")");
    }
    
    if (strChanged.equals("inpexpmargin")){    	
    	if (ExpensesMargin.doubleValue() == 100.0) {
    		// PE = 0 (because EM = 100 %)
    		PlannedExpenses = new BigDecimal(0.0);
    		if (PlannedExpenses.scale() > StdPrecision)
    			PlannedExpenses = PlannedExpenses.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
        	resultado.append("\n new Array(\"inpexpexpenses\", " + PlannedExpenses.toString() + ")\n");
		} else {
			// RE = PE/(1-EM/100)
    		ReinvoicedExpenses = new BigDecimal ((PlannedExpenses.doubleValue()) / (1 - (ExpensesMargin.doubleValue() / 100.0)));
        	if (ReinvoicedExpenses.scale() > StdPrecision)
        		ReinvoicedExpenses = ReinvoicedExpenses.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
        	resultado.append("\n new Array(\"inpexpreinvoicing\", " + ReinvoicedExpenses.toString() + ")\n");
		}
    }
    
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
