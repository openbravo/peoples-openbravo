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
import org.openbravo.common.businessObject.TaxCalculator;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_TaxRate_Order extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  //
  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strOrderId = vars.getStringParameter("inpcOrderId");
      String strOrderlineId = vars.getStringParameter("inpcOrderlineId");
      String strTax = vars.getStringParameter("inpcTaxId");
      String strTaxInclusive = vars.getNumericParameter("inptaxinclusive");
      String strpriceActual = vars.getNumericParameter("inppriceactual");

      try {
        printPage(response, vars, strChanged, strOrderId, strOrderlineId, strTax, strTaxInclusive,
            strpriceActual);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strOrderId, String strOrderlineId, String strTax, String strTaxInclusive,
      String strpriceActual) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) {
      log4j.debug("Output: dataSheet");
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    BigDecimal tax = new BigDecimal(0);
    BigDecimal unitPrice = new BigDecimal(0);

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_TaxRate_Order';\n\n");
    resultado.append("var respuesta = new Array(");
    tax = new BigDecimal(strTaxInclusive.trim());
    unitPrice = new BigDecimal(strpriceActual.trim());
    TaxCalculator generator;
    generator = new TaxCalculator(strTax);
    if (TaxCalculator.isPriceTaxInclusive(strOrderId)) {
      unitPrice = generator.taxCalculationFromOrder(strOrderId, tax);
      resultado.append("new Array(\"inppriceactual\",\"" + unitPrice + "\")");
    } else {
      tax = generator.taxCalculationFromOrder(strOrderId, unitPrice);
      resultado.append("new Array(\"inptaxinclusive\",\"" + tax + "\"),");
      resultado.append("new Array(\"inpgrossprice\",\"" + unitPrice.add(tax) + "\")");
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
