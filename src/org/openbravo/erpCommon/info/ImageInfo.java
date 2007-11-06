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
package org.openbravo.erpCommon.info;

import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.erpCommon.utility.Utility;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;



public class ImageInfo extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strKey = vars.getStringParameter("inpNameValue");
      String strNameValue = ImageInfoData.selectName(this, strKey);
      if (!strNameValue.equals("")) vars.setSessionValue("ImageInfo.name", strNameValue + "%");
      printPageFS(response, vars);
    } else if (vars.commandIn("FRAME1")) {
      String strNameValue = vars.getGlobalVariable("inpName", "ImageInfo.name", "");
      printPageFrame1(response, vars, strNameValue);
    } else if (vars.commandIn("FRAME2")) {
      String strName = vars.getGlobalVariable("inpName", "ImageInfo.name", "");
      String strURL = vars.getStringParameter("inpURL");
      printPageFrame2(response, vars, strName, strURL);
    } else if (vars.commandIn("FIND")) {
      String strName = vars.getRequestGlobalVariable("inpName", "ImageInfo.name");
      String strURL = vars.getStringParameter("inpURL");

      vars.setSessionValue("ImageInfo.initRecordNumber", "0");

      printPageFrame2(response, vars, strName, strURL);
    } else if (vars.commandIn("FRAME3")) {
      printPageFrame3(response, vars);
    } else if (vars.commandIn("PREVIOUS")) {
      String strInitRecord = vars.getSessionValue("ImageInfo.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ImageInfo");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) vars.setSessionValue("ImageInfo.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue("ImageInfo.initRecordNumber", strInitRecord);
      }

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else if (vars.commandIn("NEXT")) {
      String strInitRecord = vars.getSessionValue("ImageInfo.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ImageInfo");
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue("ImageInfo.initRecordNumber", strInitRecord);

      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=FRAME2");
    } else pageError(response);
  }

  void printPageFS(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Image seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ImageInfo_FS").createXmlDocument();

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame1(HttpServletResponse response, VariablesSecureApp vars, String strNameValue) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the image seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ImageInfo_F1").createXmlDocument();
    if (strNameValue.equals("")) {
      xmlDocument.setParameter("name", "%");
    } else {
      xmlDocument.setParameter("name", strNameValue);
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame2(HttpServletResponse response, VariablesSecureApp vars, String strName, String strURL) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 2 of the image seeker");
    XmlDocument xmlDocument;

    String strRecordRange = Utility.getContext(this, vars, "#RecordRangeInfo", "ImageInfo");
    int intRecordRange = (strRecordRange.equals("")?0:Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("ImageInfo.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));

    if (strName.equals("") && strURL.equals("")) {
      String[] discard = {"sectionDetail", "hasPrevious", "hasNext"};
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ImageInfo_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", ImageInfoData.set());
    } else {
      String[] discard = {"withoutPrevious", "withoutNext"};
      ImageInfoData[] data = ImageInfoData.select(this, Utility.getContext(this, vars, "#User_Client", "ImageInfo"), Utility.getContext(this, vars, "#User_Org", "ImageInfo"), strName, strURL, initRecordNumber, intRecordRange);
      if (data==null || data.length==0 || initRecordNumber<=1) discard[0] = new String("hasPrevious");
      if (data==null || data.length==0 || data.length<intRecordRange) discard[1] = new String("hasNext");
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ImageInfo_F2", discard).createXmlDocument();
      xmlDocument.setData("structure1", data);
    }
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageFrame3(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 3 of the image seeker");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ImageInfo_F3").createXmlDocument();
    xmlDocument.setParameter("direction", "var baseDirection = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "LNG_POR_DEFECTO=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents que image seeker";
  } // end of getServletInfo() method
}
