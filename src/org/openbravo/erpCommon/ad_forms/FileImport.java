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
package org.openbravo.erpCommon.ad_forms;

import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.erpCommon.utility.ComboTableData;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.Connection;

import org.openbravo.data.FieldProvider;

public class FileImport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  static boolean firstRowHeaders = true;
  
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (log4j.isDebugEnabled()) log4j.debug("Command: "+vars.getStringParameter("Command"));
    String strFirstLineHeader = vars.getStringParameter("inpFirstLineHeader");
    firstRowHeaders = (strFirstLineHeader.equals("Y"))?true:false;
    FileLoadData fieldsData = null;

    if (vars.commandIn("DEFAULT")) {
      String strAdImpformatId = vars.getStringParameter("inpadImpformatId");
      String texto = procesarFichero(vars, null, request, response, strAdImpformatId);
      printPage(response, vars, strFirstLineHeader, vars.getCommand(), texto, strAdImpformatId);
    } else if (vars.commandIn("FIND")) {
      //String strFirstLineHeader = vars.getStringParameter("inpFirstLineHeader");
      String strAdImpformatId = vars.getStringParameter("inpadImpformatId");
      //MultipartRequest multi = new MultipartRequest(vars, "", fieldsData, firstRowHeaders);
      FieldProvider[] rows = null;
      String strSeparator = FileImportData.selectSeparator(this, strAdImpformatId);
      if (log4j.isDebugEnabled()) log4j.debug("First Row Header: "+firstRowHeaders);
      if (strSeparator.equalsIgnoreCase("F")) rows = FileImportData.select(this, strAdImpformatId);
      fieldsData = new FileLoadData(vars, "inpFile", firstRowHeaders, strSeparator, rows);
      String texto = procesarFichero(vars, fieldsData.getFieldProvider(), request, response, strAdImpformatId);
      printPage(response, vars, strFirstLineHeader, vars.getCommand(), texto, strAdImpformatId);
    } else if (vars.commandIn("SAVE")) {
      String strAdImpformatId = vars.getStringParameter("inpadImpformatId");
      FieldProvider[] rows = null;
      String strSeparator = FileImportData.selectSeparator(this, strAdImpformatId);
      if (strSeparator.equalsIgnoreCase("F")) rows = FileImportData.select(this, strAdImpformatId);
      fieldsData = new FileLoadData(vars, "inpFile", firstRowHeaders, strSeparator, rows);      
      OBError myMessage = importarFichero(vars, fieldsData.getFieldProvider(), request, response, strAdImpformatId);               
      vars.setMessage("FileImport", myMessage);      
      response.sendRedirect(strDireccion + request.getServletPath());
    } else pageError(response);
  }

  public String procesarFichero(VariablesSecureApp vars, FieldProvider[] data2, HttpServletRequest request, HttpServletResponse response, String strAdImpformatId) throws ServletException, IOException {
    if (data2==null) return "";
    StringBuffer texto = new StringBuffer("");
    FileImportData [] data = FileImportData.select(this, strAdImpformatId);
    if (data==null) return "";
    int constant = 0;
    if (log4j.isDebugEnabled()) log4j.debug("data2.length: "+data2.length);
    for (int i=0;i<data2.length;i++){
      if (log4j.isDebugEnabled()) log4j.debug("i:"+i+" - data.length"+data.length);
      texto.append("<tr>");
      for (int j=0;j<data.length;j++){
        texto.append("<td>");
        if (!data[j].constantvalue.equals("")){
          texto.append(data[j].constantvalue);
          constant = constant + 1;
        } else texto.append(parseField(data2[i].getField(String.valueOf(j-constant)), data[j].fieldlength, data[j].datatype, data[j].dataformat, data[j].decimalpoint));
        texto.append("<td\\>");
      }
      constant = 0;
      texto.append("<tr\\>");
    }
    return texto.toString();
  }

  public OBError importarFichero(VariablesSecureApp vars, FieldProvider[] data2, HttpServletRequest request, HttpServletResponse response, String strAdImpformatId) throws ServletException, IOException {
    Connection con = null;
    StringBuffer strFields = new StringBuffer("");
    StringBuffer strValues = new StringBuffer("");
    FileImportData [] data = null;    
    int constant = 0;
    OBError myMessage = null;
    
    try{
      con = getTransactionConnection();
      data = FileImportData.select(this, strAdImpformatId);
      FileImportData.delete(con, this, vars.getClient());
      String strTable = FileImportData.table(this, strAdImpformatId);
      for (int i=0;i<data2.length;i++){
        String sequence = SequenceIdData.getSequence(this, FileImportData.table(this,strAdImpformatId), vars.getClient());
        FileImportData.insert(con, this, strTable, (strTable + "_ID"), sequence, vars.getClient(), vars.getOrg(), vars.getUser());
        int jj=0;
        for (int j=0;j<data.length;j++){
          if((data2[i].getField(String.valueOf(j-constant))==null || data2[i].getField(String.valueOf(j-constant)).equals("")) && data[j].constantvalue.equals(""))
            continue;
          if (jj>0) strFields.append(",");
          jj++;
          strFields.append(data[j].columnname).append(" = ");
          strValues.append("'");
          if ((data[j].datatype.equals("C"))&&(!data[j].constantvalue.equals(""))){
            strValues.append(data[j].constantvalue);
            constant = constant + 1;
          } else strValues.append(parseField(data2[i].getField(String.valueOf(j-constant)),data[j].fieldlength,data[j].datatype,data[j].dataformat, data[j].decimalpoint));
          strValues.append("'");
          strFields.append(strValues);
          strValues.delete(0,strValues.length());
        }
        constant = 0;
        if (log4j.isDebugEnabled()) log4j.debug("##########iteration - " + (i+1) + " - strFields = " + strFields);
        FileImportData.update(con, this, strTable, strFields.toString(), (strTable + "_id = " + sequence));
        strFields.delete(0,strFields.length());
      }
      releaseCommitConnection(con);
      //return "OK";
      //New message system
      myMessage = new OBError();
      myMessage.setType("Success");
      myMessage.setTitle("");
      myMessage.setMessage(Utility.messageBD(this, "Success", vars.getLanguage()));
      return myMessage;
    }catch(Exception e){
      try {
        releaseRollbackConnection(con);
      } catch (Exception ignored) {}
      e.printStackTrace();
      //return "";
      //New message system
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
      return myMessage;
    }
  }

  public String parseField(String strTexto, String strLength, String strDataType, String strDataFormat, String strDecimalPoint) throws ServletException {
    if (strDataType.equals("D")){
      strTexto = FileImportData.parseDate(this, strTexto, strDataFormat);
        return strTexto;
    }else if (strDataType.equals("N")){
      if (strDecimalPoint.equals(",")){
        strTexto=strTexto.replace('.',' ').trim();
        return strTexto.replace(',','.');
      }else{
        return strTexto;
      }
    }else{
      if (log4j.isDebugEnabled()) log4j.debug("##########iteration - strTexto:"+strTexto+" - length:"+strLength);
      int len = Integer.valueOf(strLength).intValue();
      strTexto = strTexto.substring(0, (len>strTexto.length())?strTexto.length():len);
      if (log4j.isDebugEnabled()) log4j.debug("########## end of iteration - ");
      return strTexto.replace('\'','´');
    }
  }


  /*void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: file importing Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/FileImport_FS").createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());  
    out.close();
  }*/

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strFirstLineHeader, String strCommand, String texto, String strAdImpformatId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: file importing Frame 1");
    XmlDocument xmlDocument = null;

    String[] discard = {""};
    FileImportData[] data = null;
    if (strCommand.equals("DEFAULT") && texto.equals("") && strAdImpformatId.equals("")) {
    	discard[0] = "sectionDetail";
      data = FileImportData.set();
    } else data = FileImportData.select(this, strAdImpformatId);
    if (log4j.isDebugEnabled()) log4j.debug("1");
    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/FileImport", discard).createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "FileImport", false, "", "", "",false, "ad_forms",  strReplaceWith, false,  true);

    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());
    if (log4j.isDebugEnabled()) log4j.debug("2");

    try {
      WindowTabs tabs = new WindowTabs(this, vars, "org.openbravo.erpCommon.ad_forms.FileImport");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "FileImport.html", classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "FileImport.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("theme", vars.getTheme());
    {
      OBError myMessage = vars.getMessage("FileImport");
      vars.removeMessage("FileImport");
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

if (log4j.isDebugEnabled()) log4j.debug("3");

    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("firstLineHeader", strFirstLineHeader);
    if (log4j.isDebugEnabled()) log4j.debug("4");

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ImpFormat_ID", "", "", Utility.getContext(this, vars, "#User_Org", ""), Utility.getContext(this, vars, "#User_Client", ""), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "", "");
      xmlDocument.setData("reportImpFormat", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    if (log4j.isDebugEnabled()) log4j.debug("5");
    data = FileImportData.set();
    xmlDocument.setParameter("data", texto);
    xmlDocument.setData("structure1", data);
    if (log4j.isDebugEnabled()) log4j.debug("6");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }


/*  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strCommand, String texto, String strAdImpformatId) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: file importing Frame 2");
    XmlDocument xmlDocument;
    FileImportData[] data = null;
    if (strCommand.equals("FRAME2") && texto.equals("") && strAdImpformatId.equals("")) {
      String[] discard = {"sectionDetail"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/FileImport_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", FileImportData.set());
    } else {
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_forms/FileImport_F2").createXmlDocument();
      data = FileImportData.select(this, strAdImpformatId);
    }
    xmlDocument.setData("structure1", data);
    xmlDocument.setParameter("data", texto);
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }*/

  public String getServletInfo() {
    return "Servlet that presents the files-importing process";
   // end of getServletInfo() method
}
}
