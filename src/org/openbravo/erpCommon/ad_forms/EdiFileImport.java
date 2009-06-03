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
package org.openbravo.erpCommon.ad_forms;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class EdiFileImport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    FileLoadData fieldsData = null;

    if (vars.commandIn("DEFAULT")) {
      printPage(response, vars);
    } else if (vars.commandIn("SAVE")) {
      fieldsData = new FileLoadData(vars, "inpFile", false, "P");
      String strMessage = importarFichero(vars, fieldsData.getFieldProvider(), request, response);
      if (strMessage.equals("OK"))
        strMessage = Utility.messageBD(this, "Success", vars.getLanguage());
      else
        strMessage = Utility.messageBD(this, "ProcessRunError", vars.getLanguage());
      if (!strMessage.equals(""))
        vars.setSessionValue("EdiFileImport.message", strMessage);
      response.sendRedirect(strDireccion + request.getServletPath());
    } else
      pageError(response);
  }

  private String importarFichero(VariablesSecureApp vars, FieldProvider[] data2,
      HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {
    Connection con = null;
    boolean firstline = true;
    EdiFileImportData data = new EdiFileImportData();
    try {
      con = getTransactionConnection();
      EdiFileImportData.delete(con, this, vars.getClient());
      for (int i = 0; i < data2.length; i++) {
        String codigo = data2[i].getField("0");
        if (codigo.equals("UNH")) {
          if (!data2[i].getField("2").equals("ORDERS:D:96A:UN:EAN008'"))
            log4j.error("The file's format is not recognized. Found: " + data2[i].getField("2"));
        } else if (codigo.equals("BGM")) {
          String tipo = data2[i].getField("1");
          String valor = get(data2[i].getField("2"), 0);
          String formato = get(data2[i].getField("3"), 0);
          if (!tipo.equals("220"))
            log4j.error("The type of order is not yet recognized.");
          else if (!formato.equals("9"))
            log4j.error("Only EAN orders accepted. Found: " + formato);
          else
            data.orderReferenceno = valor;
        } else if (codigo.equals("DTM")) {
          String tipo = get(data2[i].getField("1"), 0);
          String valor = get(data2[i].getField("1"), 1);
          String formato = get(data2[i].getField("1"), 2);
          if (!formato.equals("102") && !formato.equals("203"))
            log4j
                .error("Dates must be supplied in YYYYMMDD format (102) or in YYYMMDDHHMM format (203). Found: "
                    + formato);
          else if (tipo.equals("137"))
            data.dateordered = valor;
          else if (tipo.equals("200"))
            data.datepromised = valor;
          else if (tipo.equals("2"))
            ;
          else
            log4j.error("DTM type not recognized: " + tipo);
        } else if (codigo.equals("FTX")) {
          ;
        } else if (codigo.equals("NAD")) {
          String tipo = get(data2[i].getField("1"), 0);
          String valor = get(data2[i].getField("2"), 0);
          String formato = get(data2[i].getField("2"), 2);
          if (!formato.equals("9") && !formato.equals("92"))
            log4j.error("NAD fields are only supported in EAN format. Found: " + formato);
          else if (tipo.equals("BY"))
            data.bpartnerupc = valor;
          else if (tipo.equals("SU"))
            data.vendorUpc = valor;
          else if (tipo.equals("IV"))
            data.billtoUpc = valor;
          else if (tipo.equals("DP"))
            data.description = valor;
          else if (tipo.equals("PR"))
            ;
          else if (tipo.equals("CA"))
            data.cBpartnerLocationUpc = valor;
        } else if (codigo.equals("RFF")) {
          ;
        } else if (codigo.equals("TOD")) {
          ;
        } else if (codigo.equals("LIN")) {
          if (firstline)
            firstline = false;
          else
            data = insert(this, vars, con, data);
          String valor = get(data2[i].getField("3"), 0);
          String formato = get(data2[i].getField("3"), 1);
          if (!formato.equals("EN"))
            log4j.error("Products are only supported in EAN format (EN). Found: " + formato);
          else
            data.upc = valor;
        } else if (codigo.equals("PIA")) {
          ;
        } else if (codigo.equals("IMD")) {
          String tipo = get(data2[i].getField("1"), 0);
          String valor = get(data2[i].getField("3"), 3);
          if (!tipo.equals("F"))
            log4j.error("Description only allowed in free form (F). Found: " + tipo);
          else
            data.linedescription = valor;
        } else if (codigo.equals("MEA")) {
          String tipo = get(data2[i].getField("1"), 0);
          String dimension = get(data2[i].getField("2"), 0);
          String exact = get(data2[i].getField("2"), 1);
          String unidad = get(data2[i].getField("3"), 0);
          if (!tipo.equals("PD"))
            log4j.error("Only physical dimensions (PD) allowed. Found: " + tipo);
          else if (!dimension.equals("ULY"))
            log4j.error("Only units per layer (ULY) allowed. Found: " + dimension);
          else if (!exact.equals("4"))
            log4j.error("Only exact form (4) allowed. Found: " + exact);
          else if (!unidad.equals("PCE"))
            log4j.error("Only units (PCE) allowed. Found: " + unidad);
          else
            ;
        } else if (codigo.equals("QTY")) {
          String tipo = get(data2[i].getField("1"), 0);
          String valor = get(data2[i].getField("1"), 1);
          if (tipo.equals("21"))
            data.qtyordered = valor;
          else if (tipo.equals("59"))
            ; // units per trade item
        } else if (codigo.equals("PAC")) {
          ;
        } else if (codigo.equals("UNS")) {
          data = insert(this, vars, con, data);
        } else if (codigo.equals("UNT")) {
          ;
        }
      }
      releaseCommitConnection(con);
      return "OK";
    } catch (Exception e) {
      try {
        releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      return "";
    }
  }

  private EdiFileImportData insert(ConnectionProvider conn, VariablesSecureApp vars,
      Connection con, EdiFileImportData data) throws ServletException {
    data.iOrderId = SequenceIdData.getUUID();
    data.adClientId = vars.getClient();
    data.adOrgId = vars.getOrg();
    data.adUserId = vars.getUser();
    data.iIsimported = "N";
    data.issotrx = "N";
    data.processing = "N";
    data.processed = "N";
    data.doctypename = "Ped.estándar";
    data.insert(con, conn);
    // clear line data
    data.iOrderId = "";
    data.sku = "";
    data.linedescription = "";
    data.qtyordered = "";

    return data;
  }

  private String clean(String oldstr) {
    String newstr = oldstr.substring(0, oldstr.length() - 1);
    return newstr;
  }

  private String get(String str, int pos) {
    String[] tokens = str.split(":");
    if (pos < tokens.length) {
      String result = tokens[pos];
      if (result.endsWith("'"))
        result = result.substring(0, result.length() - 1);
      return result;
    } else {
      log4j.error("getField: requested an out of bounds token");
      return "";
    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: edi files importing Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_forms/EdiFileImport").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "EdiFileImport", false, "", "", "",
        false, "ad_forms", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.EdiFileImport");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "EdiFileImport.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "EdiFileImport.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("EdiFileImport");
      vars.removeMessage("EdiFileImport");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }

      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
      /*
       * String strMessage = vars.getSessionValue("EdiFileImport.message").
       * equals("")?"xx();":"alert('" + vars.getSessionValue("EdiFileImport.message") + "');";
       * vars.removeSessionValue("EdiFileImport.message"); xmlDocument.setParameter("mensaje",
       * strMessage);
       */
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet that presents the file-importing process";
  } // end of getServletInfo() method
}
