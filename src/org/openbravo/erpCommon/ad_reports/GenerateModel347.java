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
package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.ad_combos.OrganizationComboData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class GenerateModel347 extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strType = vars.getStringParameter("inpReportType", "New");
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strmProductId = vars.getInGlobalVariable("inpmProductId_IN",
          "GenerateModel347|product", "", IsIDFilter.instance);
      printPageDataSheet(response, vars, strType, strDateFrom, strDateTo, strmProductId);
    } else if (vars.commandIn("FIND")) {
      String strDateFrom = vars.getStringParameter("inpDateFrom");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strType = vars.getStringParameter("inpReportType");
      String strOrg = vars.getStringParameter("inpOrg", "0");
      String strComplementar = vars.getNumericParameter("inpComplementar");
      String strHac = vars.getStringParameter("inpType", "ES");
      String strID = vars.getNumericParameter("inpID", "");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "GenerateModel347|product", IsIDFilter.instance);
      printPageGenerate(response, vars, strDateFrom, strDateTo, strType, strComplementar, strOrg,
          strHac, strID, strmProductId);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strType, String strDateFrom, String strDateTo, String strmProductId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_reports/GenerateModel347")
        .createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GenerateModel347", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.erpCommon.ad_reports.GenerateModel347");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "GenerateModel347.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GenerateModel347.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("GenerateModel347");
      vars.removeMessage("GenerateModel347");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("newType", strType);
    xmlDocument.setParameter("complementaryType", strType);
    xmlDocument.setParameter("sustitutiveType", strType);
    xmlDocument.setData("reportAD_ORGID", "liststructure", OrganizationComboData.selectCombo(this,
        vars.getRole()));
    xmlDocument.setData("reportMProductId_IN", "liststructure", SelectorUtilityData.selectMproduct(
        this, Utility.getContext(this, vars, "#AccessibleOrgTree", ""), Utility.getContext(this,
            vars, "#User_Client", ""), strmProductId));
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageGenerate(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strType, String strComplementar, String strOrg,
      String strHac, String strID, String strmProductId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pageFind");
    GenerateModel347Data[] data = new GenerateModel347Data[0];
    if (strHac.equals("ES"))
      data = GenerateModel347Data.selectAEAT(this, strDateFrom.substring(strDateFrom.length() - 4),
          strID, strType, strComplementar, strDateFrom, DateTimeData.nDaysAfter(this, strDateTo,
              "1"), Tree.getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg));
    else if (strHac.equals("NA"))
      data = GenerateModel347Data.select(this, strType, strComplementar, strDateFrom, DateTimeData
          .nDaysAfter(this, strDateTo, "1"), Tree.getMembers(this, TreeData.getTreeOrg(this, vars
          .getClient()), strOrg));
    if (data.length > 0) {
      response.setContentType("application/txt");
      response.setHeader("Content-Disposition", "attachment; filename=MODEL347.DAT");
      PrintWriter out = response.getWriter();
      StringBuffer strBuf = new StringBuffer();

      String strCabecera = "";

      GenerateModel347Data[] dataLines = new GenerateModel347Data[0];
      if (strHac.equals("ES"))
        dataLines = GenerateModel347Data.selectAEATType2(this, strDateFrom.substring(strDateFrom
            .length() - 4), strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), Tree
            .getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg));
      else if (strHac.equals("NA"))
        dataLines = GenerateModel347Data.selectNavarraType2(this, strDateFrom, DateTimeData
            .nDaysAfter(this, strDateTo, "1"), Tree.getMembers(this, TreeData.getTreeOrg(this, vars
            .getClient()), strOrg));

      SelectorUtilityData[] products = SelectorUtilityData.selectMproduct(this, Utility.getContext(
          this, vars, "#AccessibleOrgTree", ""),
          Utility.getContext(this, vars, "#User_Client", ""), strmProductId);
      String productList = "";
      for (int i = 0; i < products.length; i++) {
        productList += "'" + products[i].id + "',";
      }
      productList = productList.substring(0, Math.max(0, productList.length() - 1));
      GenerateModel347Data[] arrendamientos;
      String strLinea = "";
      String strLineaArrendamientos = "";
      BigDecimal totalArrendamientos = BigDecimal.ZERO;
      BigDecimal numArrendamientos = BigDecimal.ZERO;
      for (int i = 0; i < dataLines.length; i++) {
        strLinea = dataLines[i].constant1 + dataLines[i].model + dataLines[i].ejercicio
            + dataLines[i].nifDeclarante + dataLines[i].nifDeclarado
            + dataLines[i].nifRepresentante + dataLines[i].nombreSocial;
        if (strHac.equals("ES")) {
          arrendamientos = GenerateModel347Data.selectLeaseProducts(this,
              (productList == "") ? "null" : productList, strDateFrom, DateTimeData.nDaysAfter(
                  this, strDateTo, "1"), Tree.getMembers(this, TreeData.getTreeOrg(this, vars
                  .getClient()), strOrg), dataLines[i].cBpartnerId);
          if (arrendamientos.length > 0) {
            strLineaArrendamientos = strLinea;
            BigDecimal inmuebles = new BigDecimal(arrendamientos[0].importesMetalicoEInmuebles);
            numArrendamientos = numArrendamientos.add(BigDecimal.ONE);
            totalArrendamientos = totalArrendamientos.add(inmuebles);
            strLineaArrendamientos = strLineaArrendamientos + "I" + "                       "
                + new String("000000000000000").substring(0, 15 - inmuebles.toString().length())
                + inmuebles.toString();
            if (arrendamientos[0].description != null)
              strLineaArrendamientos = strLineaArrendamientos
                  + arrendamientos[0].description.substring(0, Math.min(146,
                      arrendamientos[0].description.length()))
                  + new String(
                      "                                                                                                                                                  ")
                      .substring(0, Math.max(0, 146 - arrendamientos[0].description.length()));
            else
              strLineaArrendamientos = strLineaArrendamientos
                  + "                                                                                                                                                  ";
            strLineaArrendamientos = strLineaArrendamientos
                + "                                                                                                                                                                                                                                                ";
          }
          strLinea = strLinea + dataLines[i].tipoDeclaracion + dataLines[i].codigoProvincia
              + dataLines[i].codigoPais + dataLines[i].espacio + dataLines[i].claveCodigo
              + dataLines[i].importe + dataLines[i].operacionSeguro + dataLines[i].arrendamiento
              + dataLines[i].importesMetalicoEInmuebles + dataLines[i].blancos
              + dataLines[i].nifRepresentanteLegal + dataLines[i].masBlancos;
        } else if (strHac.equals("NA"))
          strLinea = strLinea + dataLines[i].importe + dataLines[i].operacionSeguro
              + dataLines[i].arrendamiento + dataLines[i].blancos;
        strBuf = strBuf.append("\r\n").append(strLinea);
        if (strLineaArrendamientos != "")
          strBuf = strBuf.append("\r\n").append(strLineaArrendamientos);
        strLineaArrendamientos = "";
      }
      strCabecera = data[0].constant1
          + data[0].model
          + data[0].ejercicio
          + data[0].nifDeclarante
          + data[0].nombreDeclarante
          + data[0].soporte
          + data[0].persona
          + data[0].numeroJustif
          + data[0].tipoDeclaracion
          + data[0].tipoDeclaracion2
          + data[0].numeroDec
          + data[0].numeroPersonas
          + data[0].importe
          + new String("000000000").substring(0, 9 - numArrendamientos.toString().length())
          + numArrendamientos.toString()
          + new String("000000000000000")
              .substring(0, 15 - totalArrendamientos.toString().length())
          + totalArrendamientos.toString() + data[0].blancos + data[0].nifRepresentanteLegal
          + data[0].masBlancos;
      StringBuffer strAux = new StringBuffer();
      strAux = strAux.append(strCabecera);
      strAux.append(strBuf);
      strBuf = strAux;

      strBuf = removeCharacters(strBuf);

      out.print(strBuf.toString());
      out.close();
    }
  }

  private StringBuffer removeCharacters(StringBuffer strIn) {
    StringBuffer strValidCharacters = new StringBuffer(
        "abcdefghijklmnñopqrstuvwxyzçABCDEFGHIJKLMNÑOPQRSTUVWXYZÇ0123456789,.-_ \n\r");

    for (int i = 0; i < strIn.length(); i++) {
      if (strValidCharacters.indexOf(strIn.substring(i, i + 1)) == -1) {
        strIn.replace(i, i + 1, " ");
      }
    }

    return strIn;
  }

  public String getServletInfo() {
    return "Servlet ReportInvoices. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
