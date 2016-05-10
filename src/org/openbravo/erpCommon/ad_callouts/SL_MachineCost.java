/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_MachineCost extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strPurchaseAmt = vars.getNumericParameter("inppurchaseamt");
      String strToolsetAmt = vars.getNumericParameter("inptoolsetamt");
      String strYearValue = vars.getNumericParameter("inpyearvalue");
      String strAmortization = vars.getNumericParameter("inpamortization");
      String strDaysYear = vars.getNumericParameter("inpdaysyear");
      String strDayHours = vars.getNumericParameter("inpdayhours");
      String strImproductiveHoursYear = vars.getNumericParameter("inpimproductivehoursyear");
      String strCostUomYear = vars.getNumericParameter("inpcostuomyear");
      String strCost = vars.getNumericParameter("inpcost");
      String strCostUom = vars.getStringParameter("inpcostuom");
      try {
        printPage(response, vars, strChanged, strPurchaseAmt, strToolsetAmt, strYearValue,
            strAmortization, strDaysYear, strDayHours, strImproductiveHoursYear, strCostUomYear,
            strCost, strCostUom);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strPurchaseAmt, String strToolsetAmt, String strYearValue, String strAmortization,
      String strDaysYear, String strDayHours, String strImproductiveHoursYear,
      String strCostUomYear, String strCost, String strCostUom) throws IOException,
      ServletException {
    String localStrYearValue = strYearValue;
    String localStrImproductiveHoursYear = strImproductiveHoursYear;
    String localStrCostUomYear = strCostUomYear;
    String localStrCost = strCost;
    String localStrAmortization = strAmortization;
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    if (strChanged.equals("inppurchaseamt") || strChanged.equals("inptoolsetamt")
        || strChanged.equals("inpyearvalue")) {
      if (strPurchaseAmt != null && !strPurchaseAmt.equals("") && strToolsetAmt != null
          && !strToolsetAmt.equals("") && localStrYearValue != null
          && !localStrYearValue.equals("")) {
        BigDecimal fPurchaseAmt = new BigDecimal(strPurchaseAmt);
        BigDecimal fToolsetAmt = new BigDecimal(strToolsetAmt);
        BigDecimal fYearValue = new BigDecimal(localStrYearValue);
        BigDecimal fAmortization = (fPurchaseAmt.add(fToolsetAmt)).divide(fYearValue, 12,
            BigDecimal.ROUND_HALF_EVEN);
        localStrAmortization = fAmortization.toString();

        if (localStrCostUomYear != null && !localStrCostUomYear.equals("")) {
          BigDecimal fCostUomYear = new BigDecimal(localStrCostUomYear);
          BigDecimal fCost = fYearValue.divide(fCostUomYear, 12, BigDecimal.ROUND_HALF_EVEN);
          localStrCost = fCost.toPlainString();
        }
      }
    } else if (strChanged.equals("inpamortization")) {
      if (strPurchaseAmt != null && !strPurchaseAmt.equals("") && strToolsetAmt != null
          && !strToolsetAmt.equals("") && localStrAmortization != null
          && !localStrAmortization.equals("")) {
        BigDecimal fPurchaseAmt = new BigDecimal(strPurchaseAmt);
        BigDecimal fToolsetAmt = new BigDecimal(strToolsetAmt);
        BigDecimal fAmortization = new BigDecimal(localStrAmortization);
        BigDecimal fYearValue = (fPurchaseAmt.add(fToolsetAmt)).divide(fAmortization, 12,
            BigDecimal.ROUND_HALF_EVEN);
        localStrYearValue = fYearValue.toPlainString();

        if (localStrCostUomYear != null && !localStrCostUomYear.equals("")) {
          BigDecimal fCostUomYear = new BigDecimal(localStrCostUomYear);
          BigDecimal fCost = fYearValue.divide(fCostUomYear, 12, BigDecimal.ROUND_HALF_EVEN);
          localStrCost = fCost.toPlainString();
        }
      }
    } else if (strChanged.equals("inpdaysyear") || strChanged.equals("inpdayhours")
        || strChanged.equals("inpimproductivehoursyear")) {
      if (strDaysYear != null && !strDaysYear.equals("") && strDayHours != null
          && !strDayHours.equals("") && localStrImproductiveHoursYear != null
          && !localStrImproductiveHoursYear.equals("")) {
        BigDecimal fDaysYear = new BigDecimal(strDaysYear);
        BigDecimal fDayHours = new BigDecimal(strDayHours);
        BigDecimal fImproductiveHoursYear = new BigDecimal(localStrImproductiveHoursYear);
        BigDecimal fCostUomYear = (fDaysYear.multiply(fDayHours)).subtract(fImproductiveHoursYear);
        localStrCostUomYear = fCostUomYear.toPlainString();

        if (localStrYearValue != null && !localStrYearValue.equals("")) {
          BigDecimal fYearValue = new BigDecimal(localStrYearValue);
          BigDecimal fCost = fYearValue.divide(fCostUomYear, 12, BigDecimal.ROUND_HALF_EVEN);
          localStrCost = fCost.toPlainString();
        }
      }
    } else if (strChanged.equals("inpcostuomyear")) {
      if (strCostUom.equals("H"))
        if (strDaysYear != null && !strDaysYear.equals("") && strDayHours != null
            && !strDayHours.equals("") && localStrCostUomYear != null
            && !localStrCostUomYear.equals("")) {
          BigDecimal fDaysYear = new BigDecimal(strDaysYear);
          BigDecimal fDayHours = new BigDecimal(strDayHours);
          BigDecimal fCostUomYear = new BigDecimal(localStrCostUomYear);
          BigDecimal fImproductiveHoursYear = (fDaysYear.multiply(fDayHours))
              .subtract(fCostUomYear);
          localStrImproductiveHoursYear = fImproductiveHoursYear.toPlainString();
        }
      if (localStrYearValue != null && !localStrYearValue.equals("") && localStrCostUomYear != null
          && !localStrCostUomYear.equals("")) {
        BigDecimal fYearValue = new BigDecimal(localStrYearValue);
        BigDecimal fCostUomYear = new BigDecimal(localStrCostUomYear);
        BigDecimal fCost = fYearValue.divide(fCostUomYear, 12, BigDecimal.ROUND_HALF_EVEN);
        localStrCost = fCost.toPlainString();
      }
    } else if (strChanged.equals("inpcost")) {
      if (localStrCost != null && !localStrCost.equals("") && localStrCostUomYear != null
          && !localStrCostUomYear.equals("")) {
        BigDecimal fCostUomYear = new BigDecimal(localStrCostUomYear);
        BigDecimal fCost = new BigDecimal(localStrCost);
        BigDecimal fYearValue = fCost.multiply(fCostUomYear);
        localStrYearValue = fYearValue.toPlainString();

        if (strPurchaseAmt != null && !strPurchaseAmt.equals("") && strToolsetAmt != null
            && !strToolsetAmt.equals("")) {
          BigDecimal fPurchaseAmt = new BigDecimal(strPurchaseAmt);
          BigDecimal fToolsetAmt = new BigDecimal(strToolsetAmt);
          BigDecimal fAmortization = (fPurchaseAmt.add(fToolsetAmt)).divide(fYearValue, 12,
              BigDecimal.ROUND_HALF_EVEN);
          localStrAmortization = fAmortization.toPlainString();
        }
      }
    }

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_MachineCost';\n\n");
    resultado.append("var respuesta = new Array(");
    if (!"".equals(strPurchaseAmt) && strPurchaseAmt != null) {
      resultado.append("new Array(\"inppurchaseamt\", " + strPurchaseAmt + "),\n");
    }
    if (!"".equals(strToolsetAmt) && strToolsetAmt != null) {
      resultado.append("new Array(\"inptoolsetamt\", " + strToolsetAmt + "),\n");
    }
    if (!"".equals(localStrYearValue) && localStrYearValue != null) {
      resultado.append("new Array(\"inpyearvalue\", " + localStrYearValue + "),\n");
    }
    if (!"".equals(localStrAmortization) && localStrAmortization != null) {
      resultado.append("new Array(\"inpamortization\", " + localStrAmortization + "), \n");
    }
    if (!"".equals(strDaysYear) && strDaysYear != null) {
      resultado.append("new Array(\"inpdaysyear\", " + strDaysYear + "),\n");
    }
    if (!"".equals(strDayHours) && strDayHours != null) {
      resultado.append("new Array(\"inpdayhours\", " + strDayHours + "),\n");
    }
    if (!"".equals(localStrImproductiveHoursYear) && localStrImproductiveHoursYear != null) {
      resultado.append("new Array(\"inpimproductivehoursyear\", " + localStrImproductiveHoursYear
          + "),\n");
    }
    if (!"".equals(localStrCostUomYear) && localStrCostUomYear != null) {
      resultado.append("new Array(\"inpcostuomyear\", " + localStrCostUomYear + "),\n");
    }
    if (!"".equals(localStrCost) && localStrCost != null) {
      resultado.append("new Array(\"inpcost\", " + localStrCost + ") \n");
    }
    resultado.append(");\n");
    xmlDocument.setParameter("array", resultado.toString());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
