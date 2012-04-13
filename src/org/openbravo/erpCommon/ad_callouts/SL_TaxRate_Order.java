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
      String strQtyOrdered = vars.getNumericParameter("inpqtyordered");

      try {
        printPage(response, vars, strChanged, strOrderId, strOrderlineId, strQtyOrdered, strTax,
            strTaxInclusive, strpriceActual);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strOrderId, String strOrderlineId, String strQtyOrdered, String strTax,
      String strTaxInclusive, String strpriceActual) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) {
      log4j.debug("Output: dataSheet");
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    BigDecimal taxInclusivePrice = new BigDecimal(0);
    BigDecimal unitPrice = new BigDecimal(0);

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_TaxRate_Order';\n\n");
    resultado.append("var respuesta = new Array(");
    taxInclusivePrice = new BigDecimal(strTaxInclusive.trim());
    unitPrice = new BigDecimal(strpriceActual.trim());
    TaxCalculator generator;
    generator = new TaxCalculator(strTax);
    if (TaxCalculator.isPriceTaxInclusive(strOrderId)) {
      unitPrice = taxInclusivePrice.subtract(generator.taxCalculationFromOrder(strOrderId,
          taxInclusivePrice));
      resultado.append("new Array(\"inppriceactual\",\"" + unitPrice + "\"),");
      resultado.append("new Array(\"inppricelist\", \"" + unitPrice + "\"),");
      resultado.append("new Array(\"inppricelimit\", \"" + unitPrice + "\"),");
      resultado.append("new Array(\"inppricestd\", \"" + unitPrice + "\"),");

    } else {
      taxInclusivePrice = generator.taxCalculationFromOrder(strOrderId, unitPrice);
      BigDecimal taxInclusive = unitPrice.add(taxInclusivePrice);
      resultado.append("new Array(\"inptaxinclusive\",\"" + taxInclusive + "\")");
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
