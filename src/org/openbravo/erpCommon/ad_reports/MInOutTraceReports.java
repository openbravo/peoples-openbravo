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
package org.openbravo.erpCommon.ad_reports;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.xmlEngine.XmlDocument;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.math.BigDecimal;
import java.util.Hashtable;

import org.openbravo.erpCommon.ad_combos.AttributeSetInstanceComboData;

public class MInOutTraceReports extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  static final BigDecimal ZERO = new BigDecimal(0.0);
  private String strmProductIdGlobal = "";
  private String strmAttributesetinstanceIdGlobal = "";
  private Hashtable<String, Integer> calculated = new Hashtable<String, Integer>();

  int count = 0;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);


    if (vars.commandIn("DEFAULT")) {
      strmProductIdGlobal = vars.getGlobalVariable("inpmProductId", "MInOutTraceReports|M_Product_Id", "");
      strmAttributesetinstanceIdGlobal = vars.getGlobalVariable("inpmAttributeSetInstanceId", "MInOutTraceReports|M_AttributeSetInstance_Id", "");
      String strIn = vars.getGlobalVariable("inpInOut", "MInOutTraceReports|in", "Y");
      printPageDataSheet(response, vars, strIn);
    } else if (vars.commandIn("FIND")) {
      strmProductIdGlobal = vars.getRequestGlobalVariable("inpmProductId", "MInOutTraceReports|M_Product_Id");
      strmAttributesetinstanceIdGlobal = vars.getRequestGlobalVariable("inpmAttributeSetInstanceId", "MInOutTraceReports|M_AttributeSetInstance_Id");
      String strIn = vars.getStringParameter("inpInOut").equals("")?"N":vars.getStringParameter("inpInOut");
      printPageDataHtml(response, vars, strIn);
    } else if (vars.commandIn("INVERSE")) {
      strmProductIdGlobal = vars.getRequiredStringParameter("inpmProductId2");
      strmAttributesetinstanceIdGlobal = vars.getRequiredStringParameter("inpmAttributeSetInstanceId2");
      String strIn = vars.getRequiredStringParameter("inpIn2");
      if ( strIn.equals("")) strIn="N";
      vars.setSessionValue("MInOutTraceReports|in", strIn);
      printPageDataHtml(response, vars, strIn);
    } else pageError(response);
  }

  void printPageDataHtml(HttpServletResponse response, VariablesSecureApp vars, String strIn) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("/org/openbravo/erpCommon/ad_reports/MInOutTraceReportsEdit").createXmlDocument();
    MInOutTraceReportsData[] data=null;
    calculated.clear();
    if (strmProductIdGlobal.equals("")) {
      data = new MInOutTraceReportsData[0];
    } else {
      data = MInOutTraceReportsData.select(this, strmProductIdGlobal, strmAttributesetinstanceIdGlobal);
    }
    
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setData("structure1", processData(vars, data, strIn));
    if (log4j.isDebugEnabled()) log4j.debug("****FIN: "/* + ((data!=null && data.length>0)?data[0].html:"")*/);

    
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
  
  void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strIn) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("/org/openbravo/erpCommon/ad_reports/MInOutTraceReports").createXmlDocument();
 

    xmlDocument.setParameter("calendar", vars.getLanguage());
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("mProduct", strmProductIdGlobal);
    xmlDocument.setParameter("parameterM_ATTRIBUTESETINSTANCE_ID", strmAttributesetinstanceIdGlobal);
    xmlDocument.setData("reportM_ATTRIBUTESETINSTANCE_ID","liststructure",AttributeSetInstanceComboData.select(this, vars.getLanguage(), strmProductIdGlobal, Utility.getContext(this, vars, "#User_Client", "MInOutTraceReports"), Utility.getContext(this, vars, "#User_Org", "MInOutTraceReports")));
    xmlDocument.setParameter("productDescription", MInOutTraceReportsData.selectMproduct(this, strmProductIdGlobal));
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("in", strIn);
    xmlDocument.setParameter("out", strIn);
    
    if (log4j.isDebugEnabled()) log4j.debug("****FIN: "/* + ((data!=null && data.length>0)?data[0].html:"")*/);

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "MInOutTraceReports", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      KeyMap key = new KeyMap(this, vars, "MInOutTraceReports.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.MInOutTraceReports");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "MInOutTraceReports.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "MInOutTraceReports.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("MInOutTraceReports");
      vars.removeMessage("MInOutTraceReports");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
  
  /*void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strIn) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("/org/openbravo/erpCommon/ad_reports/MInOutTraceReports").createXmlDocument();
    MInOutTraceReportsData[] data=null;
    calculated.clear();
    if (strmProductIdGlobal.equals("")) {
      data = new MInOutTraceReportsData[0];
    } else {
      data = MInOutTraceReportsData.select(this, strmProductIdGlobal, strmAttributesetinstanceIdGlobal);
    }
    xmlDocument.setParameter("calendar", vars.getLanguage());
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("mProduct", strmProductIdGlobal);
    xmlDocument.setParameter("parameterM_ATTRIBUTESETINSTANCE_ID", strmAttributesetinstanceIdGlobal);
    xmlDocument.setData("reportM_ATTRIBUTESETINSTANCE_ID","liststructure",AttributeSetInstanceComboData.select(this, vars.getLanguage(), strmProductIdGlobal, Utility.getContext(this, vars, "#User_Client", "MInOutTraceReports"), Utility.getContext(this, vars, "#User_Org", "MInOutTraceReports")));
    xmlDocument.setParameter("productDescription", MInOutTraceReportsData.selectMproduct(this, strmProductIdGlobal));
    xmlDocument.setParameter("paramLanguage", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("paramIn", strIn);
    xmlDocument.setData("structure1", processData(vars, data, strIn));
    

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "MInOutTraceReports", false, "", "", "",false, "ad_reports",  strReplaceWith, false,  true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      KeyMap key = new KeyMap(this, vars, "MInOutTraceReports.html");
      xmlDocument.setParameter("keyMap", key.getReportKeyMaps());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_reports.MInOutTraceReports");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "MInOutTraceReports.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "MInOutTraceReports.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("MInOutTraceReports");
      vars.removeMessage("MInOutTraceReports");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }*/

  private MInOutTraceReportsData[] processData(VariablesSecureApp vars, MInOutTraceReportsData[] data, String strIn) throws ServletException {
    if (data==null || data.length==0) return data;
    for (int i=0;i<data.length;i++) {
      data[i].html = processChilds(vars, data[i].mAttributesetinstanceId, data[i].mProductId, data[i].mLocatorId, strIn);
    }
    return data;
  }

  private String insertTabHtml() {
    return "    <td width=\"20px\" >&nbsp;</td>\n";
  }

  private String insertHeaderHtml(boolean isClose, String border) {
    if (!isClose) return "<table border=\"" + border + "\" cellspacing=0 cellpadding=0 width=\"100%\" class=\"TraceTable\">\n";
    else return "</table>\n";
  }

  private String insertTotal(String strTotal, String strUnit, String strTotalPedido, String strUnitPedido) {
    BigDecimal total, totalPedido;
    total = new BigDecimal(strTotal);
    totalPedido = (!strTotalPedido.equals("")?new BigDecimal(strTotalPedido):ZERO);
    total = total.setScale(2, BigDecimal.ROUND_HALF_UP);
    totalPedido = totalPedido.setScale(2, BigDecimal.ROUND_HALF_UP);
    StringBuffer resultado = new StringBuffer();
    resultado.append("<td class=\"TraceQty\" width=\"80px\">\n");
    resultado.append(total.toString()).append(" ").append(strUnit);
    resultado.append("</td>\n");
    if (totalPedido.intValue()!=0) {
      resultado.append("<td class=\"TraceQtyOrder\" width=\"80px\">\n");
      resultado.append(totalPedido.toString()).append(" ").append(strUnitPedido);
      resultado.append("</td>\n");
    }
    return resultado.toString();
  }

  private String processChilds(VariablesSecureApp vars, String mAttributesetinstanceId, String mProductId, String mLocatorId, String strIn) throws ServletException {
    Double total = 0.0, totalPedido = 0.0;
    StringBuffer strHtml = new StringBuffer();
    count +=1;
    String strCalculated = mProductId + "&" + mAttributesetinstanceId + "&" + mLocatorId;
    calculated.put(strCalculated, new Integer(count));
    if (log4j.isDebugEnabled()) log4j.debug("****** Hashtable.add: " + strCalculated);
    MInOutTraceReportsData[] dataChild = MInOutTraceReportsData.selectChilds(this, vars.getLanguage(), mAttributesetinstanceId, mProductId, mLocatorId, strIn.equals("Y")?"plusQty":"minusQty", strIn.equals("N")?"minusQty":"plusQty");
    if (dataChild == null || dataChild.length==0) return "";

    strHtml.append(insertHeaderHtml(false, "0"));
    for (int i=0;i<dataChild.length;i++) {

      if (dataChild[i].movementtype.equalsIgnoreCase("W+")) {
        strHtml.append("<tr class=\"rojoOscuro\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("W-")) {
        strHtml.append("<tr class=\"rojoOscuro\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("C+")) {
        strHtml.append("<tr class=\"rojoClaro\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("C-")) {
        strHtml.append("<tr class=\"rojoClaro\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("V+")) {
        strHtml.append("<tr class=\"rojoClaro\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("V-")) {
        strHtml.append("<tr class=\"rojoClaro\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("I+")) {
        strHtml.append("<tr class=\"naranjaOscuro\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("I-")) {
        strHtml.append("<tr class=\"naranjaOscuro\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("M+")) {
        strHtml.append("<tr class=\"amarillo\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("M-")) {
        strHtml.append("<tr class=\"amarillo\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("P+")) {
        strHtml.append("<tr class=\"naranjaClaro\">");
      } else if (dataChild[i].movementtype.equalsIgnoreCase("P-")) {
        strHtml.append("<tr class=\"naranjaClaro\">");
      } else strHtml.append("<tr class=\"amarillo\">");


      strHtml.append("<td class=\"TraceTDSubTable\">\n");
      strHtml.append(getData(dataChild[i], "TraceSubTable"));
      strHtml.append("</td>");
      total += Double.valueOf(dataChild[i].movementqty).doubleValue();
      totalPedido += (!dataChild[i].quantityorder.equals("")?Double.valueOf(dataChild[i].quantityorder).doubleValue():0.0);
      strHtml.append(insertTotal(Double.toString(total), dataChild[i].uomName, Double.toString(totalPedido), dataChild[i].productUomName));
      strHtml.append("  </tr>\n");
      if (log4j.isDebugEnabled()) log4j.debug("****** New line, qty: " + dataChild[i].movementqty + " " + getData(dataChild[i], "TraceSubTable"));
      strHtml.append(processExternalChilds(vars, dataChild[i], strIn));
    }
    strHtml.append(insertHeaderHtml(true, ""));
    return strHtml.toString();
  }

  private String processExternalChilds(VariablesSecureApp vars, MInOutTraceReportsData dataChild, String strIn) throws ServletException {
    StringBuffer strHtml = new StringBuffer();
    Double movementQty = Double.valueOf(dataChild.movementqty).doubleValue();
    //if (log4j.isDebugEnabled()) log4j.debug("****PROCESSING EXTERNAL 1: " + movementQty.toString() + " and strIn: " + strIn);
    if (strIn.equals("Y")) movementQty = movementQty * (-1);
    if (log4j.isDebugEnabled()) log4j.debug("****PROCESSING EXTERNAL 2: " + movementQty.toString() + " and movementType:" + dataChild.movementtype);

    if (dataChild.movementtype.startsWith("P") && movementQty > 0) {
      String strNewId = dataChild.mProductionlineId;
      MInOutTraceReportsData[] dataProduction;
      if (log4j.isDebugEnabled()) log4j.debug("****PROCESSING PRODUCTIONLINE: " + strNewId + " " + strIn);
      if (strIn.equals("Y")) {
        dataProduction = MInOutTraceReportsData.selectProductionOut(this, vars.getLanguage(), strNewId);
      } else {
        dataProduction = MInOutTraceReportsData.selectProductionIn(this, vars.getLanguage(), strNewId);
      }
      if (dataProduction!=null && dataProduction.length>0) {
        strHtml.append("  <tr>\n");
        //        strHtml.append(insertTabHtml());
        strHtml.append("    <td colspan=\"3\">\n");
        strHtml.append(insertHeaderHtml(false, "1"));
        for (int j=0;j<dataProduction.length;j++) {
          strHtml.append("  <tr>\n");
          strHtml.append(insertTabHtml());
          strHtml.append("    <td >\n");

          String resultado2 = "";
          strHtml.append("<table border=\"0\" cellspacing=0 cellpadding=0 width=\"100%\">\n");
          strHtml.append("  <tr>\n");
          strHtml.append("    <td class=\"TraceDate\" width=\"70\">").append(dataProduction[j].movementdate).append("</td>\n");
          strHtml.append("    <td class=\"TraceMovementType\" width=\"100\">").append(dataProduction[j].movementtypeName).append("</td>\n");
          strHtml.append("    <td class=\"TraceLocator\" width=\"100\">").append(dataProduction[j].locatorName).append("</td>\n");
          strHtml.append("    <td class=\"TraceQty\" width=\"90\">").append(dataProduction[j].movementqty).append("&nbsp;").append(dataProduction[j].uomName).append("</td>\n");
          strHtml.append("    <td class=\"TraceQtyOrder\" width=\"90\">").append(dataProduction[j].quantityorder).append("&nbsp;").append(dataProduction[j].productUomName).append("</td>\n");
          resultado2 = dataProduction[j].productName;
          strHtml.append("    <td class=\"TraceDescription\"><a href=\"#\" onclick=\"submitCommandForm('INVERSE', true, null, 'RptM_InOutTraceReports.html?inpmProductId2=" +dataProduction[j].mProductId+"&inpmAttributeSetInstanceId2="+dataProduction[j].mAttributesetinstanceId+"&inpIn2="+(strIn.equals("Y")?"N":"Y")+ "', '_self');return true;\">");
          if (!resultado2.equals("")) strHtml.append(resultado2);
          strHtml.append("&nbsp;</a></td>\n");
          resultado2 = dataProduction[j].attributeName;
          strHtml.append("    <td class=\"TraceDescription\" width=\"120\">");
          if (!resultado2.equals("")) strHtml.append(resultado2);
          strHtml.append("&nbsp;</td>\n");
          strHtml.append("</tr></table>");

          strHtml.append("  </td></tr>\n");
          if (!dataProduction[j].mAttributesetinstanceId.equals("0")) {
            String strCalculate = dataProduction[j].mProductId + "&" + dataProduction[j].mAttributesetinstanceId + "&" + dataProduction[j].mLocatorId;
            if (log4j.isDebugEnabled()) log4j.debug("******** Hashtable.production: " + strCalculate);
            if (log4j.isDebugEnabled()) log4j.debug("******** Production, hashtable calculated: " + calculated.get(strCalculate));
            Integer isnull = calculated.get(strCalculate);
            if (isnull == null){
              String strPartial = processChilds(vars, dataProduction[j].mAttributesetinstanceId, dataProduction[j].mProductId, dataProduction[j].mLocatorId, strIn);
              if (!strPartial.equals("")) {
                strHtml.append("  <tr>\n");
                strHtml.append(insertTabHtml());
                strHtml.append("    <td>\n");
                strHtml.append(strPartial);
                strHtml.append("    </td>\n");
                strHtml.append("  </tr>\n");
              }
            }
          }
        }
        strHtml.append(insertHeaderHtml(true, ""));
        strHtml.append("</td></tr>\n");
      }
    }

    if (dataChild.movementtype.startsWith("M") && movementQty > 0) {
      String strNewId = dataChild.mMovementlineId;
      MInOutTraceReportsData[] dataMovement;
      if (log4j.isDebugEnabled()) log4j.debug("****PROCESSING MOVEMENTLINE: " + strNewId + " " + strIn);
      //      if (strIn.equals("Y")) {
      dataMovement = MInOutTraceReportsData.selectMovement(this, vars.getLanguage(),strIn.equals("Y")?"M+":"M-", strNewId);
      //      } else {
      //        dataProduction = MInOutTraceReportsData.selectProductionIn(this, vars.getLanguage(),'M-', strNewId);
      //      }
      if (dataMovement!=null && dataMovement.length>0) {
        strHtml.append("  <tr>\n");
        //        strHtml.append(insertTabHtml());
        strHtml.append("    <td colspan=\"3\">\n");
        strHtml.append(insertHeaderHtml(false, "1"));
        for (int j=0;j<dataMovement.length;j++) {
          strHtml.append("  <tr>\n");
          strHtml.append(insertTabHtml());
          strHtml.append("    <td >\n");

          String resultado2 = "";
          strHtml.append("<table border=\"0\" cellspacing=0 cellpadding=0 width=\"100%\">\n");
          strHtml.append("  <tr>\n");
          strHtml.append("    <td class=\"TraceDate\" width=\"70\">").append(dataMovement[j].movementdate).append("</td>\n");
          strHtml.append("    <td class=\"TraceMovementType\" width=\"100\">").append(dataMovement[j].movementtypeName).append("</td>\n");
          strHtml.append("    <td class=\"TraceLocator\" width=\"100\">").append(dataMovement[j].locatorName).append("</td>\n");
          strHtml.append("    <td class=\"TraceQty\" width=\"90\">").append(dataMovement[j].movementqty).append("&nbsp;").append(dataMovement[j].uomName).append("</td>\n");
          strHtml.append("    <td class=\"TraceQtyOrder\" width=\"90\">").append(dataMovement[j].quantityorder).append("&nbsp;").append(dataMovement[j].productUomName).append("</td>\n");
          resultado2 = dataMovement[j].productName;
          strHtml.append("    <td class=\"TraceDescription\">");
          if (!resultado2.equals("")) strHtml.append(resultado2);
          strHtml.append("&nbsp;</td>\n");
          resultado2 = dataMovement[j].attributeName;
          strHtml.append("    <td class=\"TraceDescription\" width=\"120\">");
          if (!resultado2.equals("")) strHtml.append(resultado2);
          strHtml.append("&nbsp;</td>\n");
          strHtml.append("</tr></table>");

          //          strHtml.append(getData(dataProduction[j], "Bordes"));
          strHtml.append("  </td></tr>\n");
          if (!dataMovement[j].mAttributesetinstanceId.equals("0")) {
            String strPartial = "";
            if (!dataMovement[j].mProductId.equals(strmProductIdGlobal)) {
              if (log4j.isDebugEnabled()) log4j.debug("******** hashtable.production: Prod: " + dataMovement[j].mProductId + " Attr " + dataMovement[j].mAttributesetinstanceId + " Loc: " + dataMovement[j].mLocatorId);
              String strCalculate = dataMovement[j].mProductId + "&" + dataMovement[j].mAttributesetinstanceId + "&" + dataMovement[j].mLocatorId;
              if (log4j.isDebugEnabled()) log4j.debug("******** Movement, hashtable calculated: " + calculated.get(strCalculate));
              if (calculated.get(strCalculate) == null){
                strPartial = processChilds(vars, dataMovement[j].mAttributesetinstanceId, dataMovement[j].mProductId, dataMovement[j].mLocatorId, strIn);
              }
            }
            if (!strPartial.equals("")) {
              strHtml.append("  <tr>\n");
              strHtml.append(insertTabHtml());
              strHtml.append("    <td>\n");
              strHtml.append(strPartial);
              strHtml.append("    </td>\n");
              strHtml.append("  </tr>\n");
            }
          }
        }
        strHtml.append(insertHeaderHtml(true, ""));
        strHtml.append("</td></tr>\n");
      }
    }
    return strHtml.toString();
  }


  private String getData(MInOutTraceReportsData data, String strClassName) throws ServletException {
    StringBuffer resultado = new StringBuffer();
    String resultado2 = "";
    resultado.append("<table border=\"0\" cellspacing=0 cellpadding=0 width=\"100%\" class=\"").append(strClassName).append("\">\n");
    resultado.append("  <tr>\n");
    resultado.append("    <td class=\"TraceDate\" width=\"70\">").append(data.movementdate).append("</td>\n");
    resultado.append("    <td class=\"TraceMovementType\" width=\"100\">").append(data.movementtypeName).append("</td>\n");
    resultado.append("    <td class=\"TraceLocator\" width=\"100\">").append(data.locatorName).append("</td>\n");
    resultado.append("    <td class=\"TraceQty\" width=\"90\">").append(data.movementqty).append("&nbsp;").append(data.uomName).append("</td>\n");
    if (!data.quantityorder.equals("")) {
      resultado.append("    <td class=\"TraceQtyOrder\" width=\"90\">").append(data.quantityorder).append("&nbsp;").append(data.productUomName).append("</td>\n");
    }
    if (data.movementtype.equalsIgnoreCase("W+")) {
      //resultado2 = data.productionName;
    } else if (data.movementtype.equalsIgnoreCase("W-")) {
      //resultado2 = data.productionName;
    } else if (data.movementtype.equalsIgnoreCase("C+")) {
      resultado2 = data.vendorName;
    } else if (data.movementtype.equalsIgnoreCase("C-")) {
      resultado2 = data.vendorName;
    } else if (data.movementtype.equalsIgnoreCase("V+")) {
      resultado2 = data.vendorName;
    } else if (data.movementtype.equalsIgnoreCase("V-")) {
      resultado2 = data.vendorName;
    } else if (data.movementtype.equalsIgnoreCase("I+")) {
      resultado2 = data.inventoryName;
    } else if (data.movementtype.equalsIgnoreCase("I-")) {
      resultado2 = data.inventoryName;
    } else if (data.movementtype.equalsIgnoreCase("M+")) {
      resultado2 = data.movementName;
    } else if (data.movementtype.equalsIgnoreCase("M-")) {
      resultado2 = data.movementName;
    } else if (data.movementtype.equalsIgnoreCase("P+")) {
      resultado2 = data.productionName;
    } else if (data.movementtype.equalsIgnoreCase("P-")) {
      resultado2 = data.productionName;
    } else resultado2 = data.name;

    resultado.append("    <td class=\"TraceDescription\">");
    if (!resultado2.equals("")) resultado.append(resultado2);
    resultado.append("&nbsp;</td>\n");
    resultado.append("</tr></table>");
    return resultado.toString();
  }


  public String getServletInfo() {
    return "Servlet MInOutTraceReports. This Servlet was made by Fernando Iriazabal";
  } // end of getServletInfo() method
}

