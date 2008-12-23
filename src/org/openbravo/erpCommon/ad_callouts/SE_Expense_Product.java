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
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;

import java.math.BigDecimal;

public class SE_Expense_Product extends HttpSecureAppServlet {
    private static final long serialVersionUID = 1L;

    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        VariablesSecureApp vars = new VariablesSecureApp(request);
        if (vars.commandIn("DEFAULT")) {
            String strDateexpense = vars.getStringParameter("inpdateexpense");
            String strmProductId = vars.getStringParameter("inpmProductId");
            String strsTimeexpenseId = vars
                    .getStringParameter("inpsTimeexpenseId");
            String strcCurrencyId = vars.getStringParameter("inpcCurrencyId");
            String strqty = vars.getStringParameter("inpqty");
            String strChanged = vars.getStringParameter("inpLastFieldChanged");
            String strTabId = vars.getStringParameter("inpTabId");
            String strInvPrice = vars.getStringParameter("inpinvoiceprice");

            try {
                printPage(response, vars, strDateexpense, strmProductId,
                        strsTimeexpenseId, strqty, strcCurrencyId, strInvPrice,
                        strChanged, strTabId);
            } catch (ServletException ex) {
                pageErrorCallOut(response);
            }
        } else
            pageError(response);
    }

    void printPage(HttpServletResponse response, VariablesSecureApp vars,
            String strDateexpense, String strmProductId,
            String strsTimeexpenseId, String strqty, String strcCurrencyId,
            String strInvPrice, String strChanged, String strTabId)
            throws IOException, ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: dataSheet");
        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/ad_callouts/CallOut")
                .createXmlDocument();
        String strmPricelistId = SEExpenseProductData.priceList(this,
                strsTimeexpenseId);
        SEExpenseProductData[] data = SEExpenseProductData.select(this,
                strmProductId, strmPricelistId);

        String strUOM = SEExpenseProductData.selectUOM(this, strmProductId);
        boolean noPrice = true;
        String priceActual = "";
        String CCurrencyID = "";
        BigDecimal Qty = new BigDecimal(strqty);
        BigDecimal Amount = null;

        if (strDateexpense.equals("")) {
            strDateexpense = SEExpenseProductData.selectReportDate(this,
                    strsTimeexpenseId).equals("") ? DateTimeData.today(this)
                    : SEExpenseProductData.selectReportDate(this,
                            strsTimeexpenseId);
        }

        if (strInvPrice.equals("")) {
            for (int i = 0; data != null && i < data.length && noPrice; i++) {
                if (data[i].validfrom == null
                        || data[i].validfrom.equals("")
                        || !DateTimeData.compare(this, strDateexpense,
                                data[i].validfrom).equals("-1")) {
                    noPrice = false;
                    // Price
                    priceActual = data[i].pricestd;
                    if (priceActual.equals(""))
                        priceActual = data[i].pricelist;
                    if (priceActual.equals(""))
                        priceActual = data[i].pricelimit;
                    // Currency
                    CCurrencyID = data[i].cCurrencyId;
                }
            }
            if (noPrice) {
                data = SEExpenseProductData.selectBasePriceList(this,
                        strmProductId, strmPricelistId);
                for (int i = 0; data != null && i < data.length && noPrice; i++) {
                    if (data[i].validfrom == null
                            || data[i].validfrom.equals("")
                            || !DateTimeData.compare(this, strDateexpense,
                                    data[i].validfrom).equals("-1")) {
                        noPrice = false;
                        // Price
                        priceActual = data[i].pricestd;
                        if (priceActual.equals(""))
                            priceActual = data[i].pricelist;
                        if (priceActual.equals(""))
                            priceActual = data[i].pricelimit;
                        // Currency
                        CCurrencyID = data[i].cCurrencyId;
                    }
                }
            }
        } else {
            priceActual = strInvPrice;
        }

        if (strChanged.equals("inpqty") || CCurrencyID.equals("")) {
            CCurrencyID = strcCurrencyId;
        }
        String strPrecision = "0";
        if (!CCurrencyID.equals("")) {
            strPrecision = SEExpenseProductData.selectPrecision(this,
                    CCurrencyID);
        }
        int StdPrecision = Integer.valueOf(strPrecision).intValue();

        StringBuffer resultado = new StringBuffer();
        resultado.append("var calloutName='SE_Expense_Product';\n\n");
        resultado.append("var respuesta = new Array(");
        resultado.append("new Array(\"inpcUomId\", "
                + (strUOM.equals("") ? "\"\"" : strUOM) + ")\n");

        if (!priceActual.equals("")) {
            Amount = new BigDecimal(priceActual);
            Amount = Amount.multiply(Qty);
        } else {
            Amount = new BigDecimal(0.0);
        }
        if (Amount.scale() > StdPrecision)
            Amount = Amount.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
        resultado.append(", new Array(\"inpexpenseamt\", \""
                + Amount.toString() + "\")");
        String C_Currency_To_ID = Utility.getContext(this, vars,
                "$C_Currency_ID", "");
        // Checks if there is a conversion rate for each of the transactions of
        // the report
        String strConvRateErrorMsg = "";
        OBError myMessage = null;
        myMessage = new OBError();
        if (!CCurrencyID.equals("")) {
            String convertedAmount = Amount.toString();
            if (!CCurrencyID.equals(C_Currency_To_ID)) {
                try {
                    convertedAmount = SEExpenseProductData.selectConvertedAmt(
                            this, Amount.toString(), CCurrencyID,
                            C_Currency_To_ID, strDateexpense, vars.getClient(),
                            vars.getOrg());
                } catch (ServletException e) {
                    convertedAmount = "";
                    myMessage = Utility.translateError(this, vars, vars
                            .getLanguage(), e.getMessage());
                    strConvRateErrorMsg = myMessage.getMessage();
                    log4j.warn("Currency does not exist. Exception:" + e);
                }
            }
            String strPrecisionConv = "0";
            if (!C_Currency_To_ID.equals("")) {
                strPrecisionConv = SEExpenseProductData.selectPrecision(this,
                        C_Currency_To_ID);
            }
            int StdPrecisionConv = Integer.valueOf(strPrecisionConv).intValue();
            BigDecimal ConvAmount;
            if (!convertedAmount.equals("")) {
                ConvAmount = new BigDecimal(convertedAmount);
            } else {
                ConvAmount = new BigDecimal(0.0);
            }
            if (ConvAmount.scale() > StdPrecisionConv)
                ConvAmount = ConvAmount.setScale(StdPrecisionConv,
                        BigDecimal.ROUND_HALF_UP);
            resultado.append(", new Array(\"inpconvertedamt\", \""
                    + (ConvAmount.equals(new BigDecimal(0.0)) ? "" : ConvAmount
                            .toString()) + "\")");
        }
        if (strChanged.equals("inpmProductId") && !CCurrencyID.equals("")) {
            resultado.append(", new Array(\"inpcCurrencyId\", \"" + CCurrencyID
                    + "\")");
        }
        if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
            resultado.append(", new Array('MESSAGE', \"" + strConvRateErrorMsg
                    + "\")");
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
