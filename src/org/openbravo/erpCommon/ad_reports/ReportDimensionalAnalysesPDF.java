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
 * All portions are Copyright (C) 2001-2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_reports;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.IsPositiveIntFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportDimensionalAnalysesPDF extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|dateToRef");
      String strPartnerGroup = vars.getRequestGlobalVariable("inpPartnerGroup",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|partnerGroup");
      String strcBpartnerId = vars.getRequestInGlobalVariable("inpcBPartnerId_IN",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|partner", IsIDFilter.instance);
      String strProductCategory = vars.getRequestGlobalVariable("inpProductCategory",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|productCategory");
      String strmProductId = vars.getRequestInGlobalVariable("inpmProductId_IN",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|product", IsIDFilter.instance);
      // numeric hardcoded in switch in the report
      String strNotShown = vars.getInStringParameter("inpNotShown", IsPositiveIntFilter.instance);
      String strShown = vars.getInStringParameter("inpShown", IsPositiveIntFilter.instance);
      String strOrg = vars.getGlobalVariable("inpOrg",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|org", "0");
      String strsalesrepId = vars.getRequestGlobalVariable("inpSalesrepId",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|salesrep");
      String strOrder = vars.getRequestGlobalVariable("inpOrder",
          "ReportRefundInvoiceCustomerDimensionalAnalyses|order");
      String strMayor = vars.getStringParameter("inpMayor", "");
      String strMenor = vars.getStringParameter("inpMenor", "");
      String strRatioMayor = vars.getStringParameter("inpRatioMayor", "");
      String strRatioMenor = vars.getStringParameter("inpRatioMenor", "");
      String strComparative = vars.getStringParameter("inpComparative", "N");
      printPagePdf(response, vars, strComparative, strDateFrom, strDateTo, strPartnerGroup,
          strcBpartnerId, strProductCategory, strmProductId, strNotShown, strShown, strDateFromRef,
          strDateToRef, strOrg, strsalesrepId, strOrder, strMayor, strMenor, strRatioMayor,
          strRatioMenor);
    } else
      pageErrorPopUp(response);
  }

  private void printPagePdf(HttpServletResponse response, VariablesSecureApp vars,
      String strComparative, String strDateFrom, String strDateTo, String strPartnerGroup,
      String strcBpartnerId, String strProductCategory, String strmProductId, String strNotShown,
      String strShown, String strDateFromRef, String strDateToRef, String strOrg,
      String strsalesrepId, String strOrder, String strMayor, String strMenor,
      String strRatioMayor, String strRatioMenor) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: print pdf");
    XmlDocument xmlDocument = null;
    String strOrderby = "";
    if (log4j.isDebugEnabled())
      log4j.debug("********************************" + strComparative);
    String[] discard = { "", "", "", "", "", "" };
    String[] discard1 = { "selEliminarBody1", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard", "discard", "discard", "discard",
        "discard", "discard", "discard", "discard", "discard" };
    if (strComparative.equals("Y"))
      discard1[0] = "selEliminarBody2";
    String strTitle = "";
    strTitle = "del " + strDateFrom + " al " + strDateTo;
    if (!strPartnerGroup.equals(""))
      strTitle = strTitle + ", para el grupo de terceros "
          + ReportRefundInvoiceCustomerDimensionalAnalysesData.selectBpgroup(this, strPartnerGroup);
    // if (!strcBpartnerId.equals("")) strTitle =
    //strTitle+", para el tercero "+ReportRefundInvoiceCustomerDimensionalAnalysesData.selectBpartner
    // (this,
    // strcBpartnerId);
    if (!strProductCategory.equals(""))
      strTitle = strTitle
          + ", categoria de producto "
          + ReportInvoiceCustomerDimensionalAnalysesData.selectProductCategory(this,
              strProductCategory);
    if (!strsalesrepId.equals(""))
      strTitle = strTitle + "y el comercial "
          + ReportRefundInvoiceCustomerDimensionalAnalysesData.selectSalesrep(this, strsalesrepId);
    // if (!strmProductId.equals("")) strTitle =
    // strTitle+" y el producto "+ReportRefundInvoiceCustomerDimensionalAnalysesData.selectMproduct(
    // this,
    // strmProductId);
    ReportRefundInvoiceCustomerDimensionalAnalysesData[] data = null;
    String[] strShownArray = { "", "", "", "", "", "" };
    if (strShown.startsWith("("))
      strShown = strShown.substring(1, strShown.length() - 1);
    if (!strShown.equals("")) {
      strShown = Replace.replace(strShown, "'", "");
      strShown = Replace.replace(strShown, " ", "");
      StringTokenizer st = new StringTokenizer(strShown, ",", false);
      int intContador = 0;
      while (st.hasMoreTokens()) {
        strShownArray[intContador] = st.nextToken();
        intContador++;
      }

    }
    String[] strTextShow = { "", "", "", "", "", "" };
    int intDiscard = 0;
    int intAuxDiscard = -1;
    for (int i = 0; i < 6; i++) {
      if (strShownArray[i].equals("1")) {
        strTextShow[i] = "C_BP_GROUP.NAME";
        discard[i] = "10";
        intDiscard++;
      } else if (strShownArray[i].equals("2")) {
        strTextShow[i] = "C_BPARTNER.NAME";
        discard[i] = "10";
        intDiscard++;
      } else if (strShownArray[i].equals("3")) {
        strTextShow[i] = "M_PRODUCT_CATEGORY.NAME";
        discard[i] = "10";
        intDiscard++;
      } else if (strShownArray[i].equals("4")) {
        strTextShow[i] = "M_PRODUCT.NAME";
        discard[i] = "10";
        intAuxDiscard = i;
      } else if (strShownArray[i].equals("5")) {
        strTextShow[i] = "C_INVOICE.DOCUMENTNO";
        discard[i] = "10";
        intDiscard++;
      } else if (strShownArray[i].equals("6")) {
        strTextShow[i] = "AD_USER.FIRSTNAME||' '||' '||AD_USER.LASTNAME";
        discard[i] = "10";
        intDiscard++;
      } else {
        strTextShow[i] = "''";
        discard[i] = "0.1";
      }
    }
    if (intDiscard != 0 || intAuxDiscard != -1) {
      int k = 1;
      if (intDiscard == 1) {
        strOrderby = " ORDER BY NIVEL" + k + ",";
      } else {
        strOrderby = " ORDER BY ";
      }
      while (k < intDiscard) {
        strOrderby = strOrderby + "NIVEL" + k + ",";
        k++;
      }
      if (k == 1) {
        if (strOrder.equals("Normal")) {
          strOrderby = " ORDER BY NIVEL" + k;
        } else if (strOrder.equals("Amountasc")) {
          strOrderby = " ORDER BY LINENETAMT ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby = " ORDER BY LINENETAMT DESC";
        } else if (strOrder.equals("Ratioasc")) {
          strOrderby = " ORDER BY LINENETAMT ASC";
        } else if (strOrder.equals("Ratiodesc")) {
          strOrderby = " ORDER BY LINENETAMT DESC";
        } else {
          strOrderby = "1";
        }
      } else {
        if (strOrder.equals("Normal")) {
          strOrderby += "NIVEL" + k;
        } else if (strOrder.equals("Amountasc")) {
          strOrderby += "LINENETAMT ASC";
        } else if (strOrder.equals("Amountdesc")) {
          strOrderby += "LINENETAMT DESC";
        } else if (strOrder.equals("Ratioasc")) {
          strOrderby += "LINENETAMT ASC";
        } else if (strOrder.equals("Ratiodesc")) {
          strOrderby += "LINENETAMT DESC";
        } else {
          strOrderby = "1";
        }
      }

    } else {
      strOrderby = " ORDER BY 1";
    }
    String strHaving = "";
    if (!strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING SUM(LINENETAMT) > " + strMayor + " AND SUM(LINENETAMT) < " + strMenor;
    } else if (!strMayor.equals("") && strMenor.equals("")) {
      strHaving = " HAVING SUM(LINENETAMT) > " + strMayor;
    } else if (strMayor.equals("") && !strMenor.equals("")) {
      strHaving = " HAVING SUM(LINENETAMT) < " + strMenor;
    } else {
    }
    if (strHaving.equals("")) {
      if (!strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving = " HAVING C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor
            + " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else if (!strRatioMayor.equals("") && strRatioMenor.equals("")) {
        strHaving = " HAVING C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor;
      } else if (strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving = " HAVING C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else {
      }
    } else {
      if (!strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving += " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor
            + " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else if (!strRatioMayor.equals("") && strRatioMenor.equals("")) {
        strHaving += " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 > "
            + strRatioMayor;
      } else if (strRatioMayor.equals("") && !strRatioMenor.equals("")) {
        strHaving += " AND C_DIVIDE(SUM(REFUNDAMT), (SUM(LINENETAMT)+SUM(REFUNDAMT)))*100 < "
            + strRatioMenor;
      } else {
      }
    }
    strOrderby = strHaving + strOrderby;
    if (log4j.isDebugEnabled())
      log4j.debug("*******************PDF" + strOrderby);
    if (strComparative.equals("Y")) {
      data = ReportRefundInvoiceCustomerDimensionalAnalysesData.select(this, strTextShow[0],
          strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4], strTextShow[5], Tree
              .getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility
              .getContext(this, vars, "#User_Client",
                  "ReportRefundInvoiceCustomerDimensionalAnalyses"), strDateFrom, DateTimeData
              .nDaysAfter(this, strDateTo, "1"), strPartnerGroup, strcBpartnerId,
          strProductCategory, strmProductId, strsalesrepId, strDateFromRef, DateTimeData
              .nDaysAfter(this, strDateToRef, "1"), strOrderby);
    } else {
      data = ReportRefundInvoiceCustomerDimensionalAnalysesData.selectNoComparative(this,
          strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3], strTextShow[4],
          strTextShow[5], Tree
              .getMembers(this, TreeData.getTreeOrg(this, vars.getClient()), strOrg), Utility
              .getContext(this, vars, "#User_Client",
                  "ReportRefundInvoiceCustomerDimensionalAnalyses"), strDateFrom, DateTimeData
              .nDaysAfter(this, strDateTo, "1"), strPartnerGroup, strcBpartnerId,
          strProductCategory, strmProductId, strsalesrepId, strOrderby);
    }
    // data =
    // ReportRefundInvoiceCustomerDimensionalAnalysesData.select(this,
    // strTextShow[0], strTextShow[1], strTextShow[2], strTextShow[3],
    // strTextShow[4], strTextShow[5], vars.getClient(), strOrg,
    // Utility.getContext(this, vars, "#User_Client",
    // "ReportRefundInvoiceCustomerDimensionalAnalyses"), strDateFrom,
    // DateTimeData.nDaysAfter(this, strDateTo,"1"), strPartnerGroup,
    // strcBpartnerId, strProductCategory, strmProductId, strsalesrepId,
    // strDateFromRef, DateTimeData.nDaysAfter(this, strDateToRef,"1"));
    if (data.length == 0 || data == null) {
      // discard1[0] = "selEliminar1";
      if (log4j.isDebugEnabled())
        log4j.debug("********************No dataPDF");
      data = ReportRefundInvoiceCustomerDimensionalAnalysesData.set();
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("*************************level1PDF" + data[0].nivel1);
      int contador = intDiscard;
      if (intAuxDiscard != -1)
        contador = intAuxDiscard;
      int k = 1;
      if (strComparative.equals("Y")) {
        for (int j = contador; j > 0; j--) {
          discard1[k] = "fieldTotalQtyNivel" + String.valueOf(j);
          discard1[k + 10] = "fieldTotalRefundQtyNivel" + String.valueOf(j);
          discard1[k + 20] = "fieldUomsymbol" + String.valueOf(j);
          discard1[k + 5] = "fieldTotalRefQtyNivel" + String.valueOf(j);
          discard1[k + 15] = "fieldTotalRefRefundQtyNivel" + String.valueOf(j);
          k++;
        }
      } else {
        for (int j = contador; j > 0; j--) {
          discard1[k] = "fieldNoncomparativeTotalQtyNivel" + String.valueOf(j);
          discard1[k + 10] = "fieldNoncomparativeTotalRefundQtyNivel" + String.valueOf(j);
          discard1[k + 20] = "fieldNoncomparativeUomsymbol" + String.valueOf(j);
          k++;
        }
      }

    }
    xmlDocument = xmlEngine
        .readXmlTemplate(
            "org/openbravo/erpCommon/ad_reports/ReportRefundInvoiceCustomerDimensionalAnalysesEditionPDF",
            discard1).createXmlDocument();

    xmlDocument.setParameter("eliminar2", discard[1]);
    xmlDocument.setParameter("eliminar3", discard[2]);
    xmlDocument.setParameter("eliminar4", discard[3]);
    xmlDocument.setParameter("eliminar5", discard[4]);
    xmlDocument.setParameter("eliminar6", discard[5]);
    xmlDocument.setParameter("title", strTitle);
    xmlDocument.setParameter("constante", "100");
    if (strComparative.equals("Y")) {
      xmlDocument.setData("structure1", data);
    } else {
      xmlDocument.setData("structure2", data);
    }
    String strResult = xmlDocument.print();
    renderFO(strResult, response);
  }

  public String getServletInfo() {
    return "Servlet ReportRefundInvoiceCustomerDimensionalAnalyses. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method
}
