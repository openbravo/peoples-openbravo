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
 * All portions are Copyright (C) 2001-2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.info;

import org.openbravo.base.secureApp.*;
import org.openbravo.xmlEngine.XmlDocument;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.Utility;
import java.io.*;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import org.openbravo.utils.Replace;

import org.openbravo.erpCommon.utility.DateTimeData;

public class ShipmentReceiptLine extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  public void init (ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
			removePageSessionVariables(vars);
      String strNameValue = vars.getRequestGlobalVariable("inpNameValue", "ShipmentReceiptLine.name");
      String windowId = vars.getRequestGlobalVariable("WindowID", "ShipmentReceiptLine.windowId");
      String strSOTrx = vars.getStringParameter("inpisSOTrxTab");
      if (strSOTrx.equals("")) strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ShipmentReceiptLine.isSOTrx", strSOTrx);
      String strProduct = vars.getRequestGlobalVariable("inpProduct", "ShipmentReceiptLine.product");
      String strBPartner = vars.getRequestGlobalVariable("inpBPartner", "ShipmentReceiptLine.bpartner");
			String strDocumentNo = "";
			String strDateFrom = "";
			String strDateTo = "";
      if (!strNameValue.equals("")) {
        int i=0, count=1, inicio=0;
        String search = " - ", token="";
        do {
          i = strNameValue.indexOf(search, inicio);
          if (i>=0) {
            token = strNameValue.substring(inicio, i);
            inicio = i + search.length();
          } else {
            token = strNameValue.substring(inicio);
          }

          switch (count) {
            case 1: 
              vars.setSessionValue("ShipmentReceiptLine.line", token.trim());
              break;
            case 2: 
              vars.setSessionValue("ShipmentReceiptLine.movementqty", token.trim());
              break;
            case 3: 
              strDocumentNo = token.trim();
              vars.setSessionValue("ShipmentReceiptLine.documentno", strDocumentNo);
              break;
            case 4: 
              strDateFrom = token.trim();
              strDateTo = token.trim();
              vars.setSessionValue("ShipmentReceiptLine.datefrom", strDateFrom);
              vars.setSessionValue("ShipmentReceiptLine.dateto", strDateTo);
              break;
            case 5: 
              String ID = ShipmentReceiptLineData.getProductID(this, token);
              if (!ID.equals("")) strProduct = ID;
					vars.setSessionValue("ShipmentReceiptLine.product", strProduct);
              break;
          }
          count++;
        } while (i!=-1);
      }
      printPage(response, vars, strBPartner, strProduct, strDocumentNo, strDateFrom, strDateTo);
    } else if (vars.commandIn("KEY")) {
			removePageSessionVariables(vars);
      String strKeyValue = vars.getRequestGlobalVariable("inpNameValue", "ShipmentReceiptLine.documentno");
      String windowId = vars.getRequestGlobalVariable("WindowID", "ShipmentReceiptLine.windowId");
      String strSOTrx = vars.getStringParameter("inpisSOTrxTab");
      if (strSOTrx.equals("")) strSOTrx = Utility.getContext(this, vars, "isSOTrx", windowId);
      vars.setSessionValue("ShipmentReceiptLine.isSOTrx", strSOTrx);
      strKeyValue = strKeyValue + "%";
      vars.setSessionValue("ShipmentReceiptLine.documentno", strKeyValue);
      ShipmentReceiptLineData[] data = null;
      if (strSOTrx.equals("Y")) data = ShipmentReceiptLineData.selectKey(this, Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strKeyValue);
      else data = ShipmentReceiptLineData.selectKeySOTrx(this, Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strKeyValue);
      if (data!=null && data.length==1) printPageKey(response, vars, data);
      else printPage(response, vars, "", "", strKeyValue, "", "");
    } else if(vars.commandIn("STRUCTURE")) {
    	printGridStructure(response, vars);
    } else if(vars.commandIn("DATA")) {
    	if(vars.getStringParameter("newFilter").equals("1")){
    	  removePageSessionVariables(vars);
    	}
      String strBpartnerId = vars.getGlobalVariable("inpBpartnerId", "ShipmentReceiptLine.bpartner", "");
      String strProduct = vars.getGlobalVariable("inpmProductId", "ShipmentReceiptLine.product", "");
      String strDocumentNo = vars.getGlobalVariable("inpdocumentno", "ShipmentReceiptLine.documentno", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "ShipmentReceiptLine.datefrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "ShipmentReceiptLine.dateto", "");
      String strDescription = vars.getGlobalVariable("inpDescription", "ShipmentReceiptLine.description", "");
      String strOrder = vars.getGlobalVariable("inpOrder", "ShipmentReceiptLine.order", "");
      String strInvoiced = vars.getGlobalVariable("inpinvoiced", "ShipmentReceiptLine.invoiced", "N");
			String strSOTrx = vars.getSessionValue("ShipmentReceiptLine.isSOTrx");

        String strNewFilter = vars.getStringParameter("newFilter");
        String strOffset = vars.getStringParameter("offset");
        String strPageSize = vars.getStringParameter("page_size");
        String strSortCols = vars.getStringParameter("sort_cols").toUpperCase();
        String strSortDirs = vars.getStringParameter("sort_dirs").toUpperCase();

    	printGridData(response, vars, strDocumentNo, strBpartnerId, strDateFrom, strDateTo, strDescription, strOrder, strProduct, strInvoiced, strSOTrx, strSortCols + " " + strSortDirs, strOffset, strPageSize, strNewFilter);
    } else pageError(response);
  }

	private void removePageSessionVariables(VariablesSecureApp vars){
      vars.removeSessionValue("ShipmentReceiptLine.bpartner");
      vars.removeSessionValue("ShipmentReceiptLine.product");
      vars.removeSessionValue("ShipmentReceiptLine.documentno");
      vars.removeSessionValue("ShipmentReceiptLine.datefrom");
      vars.removeSessionValue("ShipmentReceiptLine.dateto");
      vars.removeSessionValue("ShipmentReceiptLine.description");
      vars.removeSessionValue("ShipmentReceiptLine.order");
      vars.removeSessionValue("ShipmentReceiptLine.invoiced");
	}

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner, String strProduct, String strDocumentNo, String strDateFrom, String strDateTo) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: Frame 1 of the delivery lines seekern");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/ShipmentReceiptLine").createXmlDocument();

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0,2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("documentno", strDocumentNo);
    xmlDocument.setParameter("datefrom", strDateFrom);
    xmlDocument.setParameter("dateto", strDateTo);
    xmlDocument.setParameter("cBpartnerId", strBPartner);
    xmlDocument.setParameter("cBpartnerId_DES", ShipmentReceiptLineData.selectBPartner(this, strBPartner));
    xmlDocument.setParameter("mProductId", strProduct);
    xmlDocument.setParameter("mProductId_DES", ShipmentReceiptLineData.selectProduct(this, strProduct));
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

	    xmlDocument.setParameter("grid", "20");
	    xmlDocument.setParameter("grid_Offset", "");
	    xmlDocument.setParameter("grid_SortCols", "1");
	    xmlDocument.setParameter("grid_SortDirs", "ASC");
	    xmlDocument.setParameter("grid_Default", "0");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void printPageKey(HttpServletResponse response, VariablesSecureApp vars, ShipmentReceiptLineData[] data) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: delivery note lines seeker Frame Set");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/info/SearchUniqueKeyResponse").createXmlDocument();

    xmlDocument.setParameter("script", generateResult(data));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  String generateResult(ShipmentReceiptLineData[] data) throws IOException, ServletException {
    StringBuffer html = new StringBuffer();
    
    html.append("\nfunction validateSelector() {\n");
    html.append("var key = \"" + data[0].mInoutlineId + "\";\n");
    html.append("var text = \"" + Replace.replace(data[0].lineText, "\"", "\\\"") + "\";\n");
    html.append("parent.opener.closeSearch(\"SAVE\", key, text);\n");
    html.append("}\n");
    return html.toString();
  }

  void printGridStructure(HttpServletResponse response, VariablesSecureApp vars) throws IOException, ServletException {
	  if (log4j.isDebugEnabled()) log4j.debug("Output: print page structure");
	    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/utility/DataGridStructure").createXmlDocument();
	    
	    SQLReturnObject[] data = getHeaders(vars);
	    String type = "Hidden";
	    String title = "";
	    String description = "";
	   	    
	    xmlDocument.setParameter("type", type);
	    xmlDocument.setParameter("title", title);
	    xmlDocument.setParameter("description", description);
	    xmlDocument.setData("structure1", data);
	    response.setContentType("text/xml; charset=UTF-8");
	    response.setHeader("Cache-Control", "no-cache");
	    PrintWriter out = response.getWriter();
	    if (log4j.isDebugEnabled()) log4j.debug(xmlDocument.print());
	    out.println(xmlDocument.print());
	    out.close();
  }
  
  private SQLReturnObject[] getHeaders(VariablesSecureApp vars) {
	  SQLReturnObject[] data = null;
	  Vector<SQLReturnObject> vAux = new Vector<SQLReturnObject>();	  
	  String[] colNames = {"bpartner_name", "movementdate","documentno","issotrx", "product_name", "qty", "locator_name", "attribute_name", "rowkey"};
//	  String[] gridNames = {"Key", "Name","Disp. Credit","Credit used", "Contact", "Phone no.", "Zip", "City", "Income", "c_bpartner_id", "c_bpartner_contact_id", "c_bpartner_location_id", "rowkey"};
	  String[] colWidths = {"160", "80", "100", "44", "140", "78", "110", "80", "0"};
	  for(int i=0; i < colNames.length; i++) {
		  SQLReturnObject dataAux = new SQLReturnObject();
		  dataAux.setData("columnname", colNames[i]);
	      dataAux.setData("gridcolumnname", colNames[i]);
	      dataAux.setData("adReferenceId", "AD_Reference_ID");
	      dataAux.setData("adReferenceValueId", "AD_ReferenceValue_ID");	      
	      dataAux.setData("isidentifier", (colNames[i].equals("rowkey")?"true":"false"));
	      dataAux.setData("iskey", (colNames[i].equals("rowkey")?"true":"false"));
	      dataAux.setData("isvisible", (colNames[i].endsWith("_id") || colNames[i].equals("rowkey")?"false":"true"));
	      String name = Utility.messageBD(this, "SRLS_" + colNames[i].toUpperCase(), vars.getLanguage());
	      dataAux.setData("name", (name.startsWith("SRLS_")?colNames[i]:name));
	      dataAux.setData("type", "string");
	      dataAux.setData("width", colWidths[i]);
	      vAux.addElement(dataAux);
	  }
	  data = new SQLReturnObject[vAux.size()];
	  vAux.copyInto(data);
	  return data;
  }
  
  void printGridData(HttpServletResponse response, VariablesSecureApp vars, String strDocumentNo, String strBpartnerId, String strDateFrom, String strDateTo, String strDescription, String strOrder, String strProduct, String strInvoiced, String strSOTrx, String strOrderBy, String strOffset, String strPageSize, String strNewFilter ) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: print page rows");
    
    SQLReturnObject[] headers = getHeaders(vars);
    FieldProvider[] data = null;
    String type = "Hidden";
    String title = "";
    String description = "";
    String strNumRows = "0";
    
    if (headers!=null) {
      try{
	  	if(strNewFilter.equals("1") || strNewFilter.equals("")) { // New filter or first load    	
      	if (strSOTrx.equals("Y")) {
        	data = ShipmentReceiptLineData.select(this, "1", Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strProduct, strInvoiced,  strOrderBy, "", "");
      	} else {
        	data = ShipmentReceiptLineData.selectSOTrx(this, "1", Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strProduct, (strInvoiced.equals("Y")?"=":"<>"),  strOrderBy, "", "");
      	}
	  		strNumRows = String.valueOf(data.length);
	  		vars.setSessionValue("ShipmentReceiptLine.numrows", strNumRows);
	  	}
  		else {
  			strNumRows = vars.getSessionValue("ShipmentReceiptLine.numrows");
  		}
	  			
  		// Filtering result
    	if(this.myPool.getRDBMS().equalsIgnoreCase("ORACLE")) {
    		String oraLimit = strOffset + " AND " + String.valueOf(Integer.valueOf(strOffset).intValue() + Integer.valueOf(strPageSize));    		
    		if (strSOTrx.equals("Y")) {
        	data = ShipmentReceiptLineData.select(this, "ROWNUM", Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strProduct, strInvoiced,  strOrderBy, oraLimit, "");
      	} else {
        	data = ShipmentReceiptLineData.selectSOTrx(this, "ROWNUM", Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strProduct, (strInvoiced.equals("Y")?"=":"<>"),  strOrderBy, oraLimit, "");
      	}
    	}
    	else {
    		String pgLimit = strPageSize + " OFFSET " + strOffset;
      	if (strSOTrx.equals("Y")) {
        	data = ShipmentReceiptLineData.select(this, "1", Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strProduct, strInvoiced,  strOrderBy, "", pgLimit);
      	} else {
        	data = ShipmentReceiptLineData.selectSOTrx(this, "1", Utility.getContext(this, vars, "#User_Client", "ShipmentReceiptLine"), Utility.getContext(this, vars, "#User_Org", "ShipmentReceiptLine"), strDocumentNo, strDescription, strOrder, strBpartnerId, strDateFrom, DateTimeData.nDaysAfter(this,strDateTo, "1"), strProduct, (strInvoiced.equals("Y")?"=":"<>"),  strOrderBy, "", pgLimit);
      	}
    	}    	
      } catch (ServletException e) {
        log4j.error("Error in print page data: " + e);
        e.printStackTrace();
        OBError myError = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
        if (!myError.isConnectionAvailable()) {
          bdErrorAjax(response, "Error", "Connection Error", "No database connection");
          return;
        } else {
          type = myError.getType();
          title = myError.getTitle();
          if (!myError.getMessage().startsWith("<![CDATA[")) description = "<![CDATA[" + myError.getMessage() + "]]>";
          else description = myError.getMessage();
        }
      } catch (Exception e) { 
        if (log4j.isDebugEnabled()) log4j.debug("Error obtaining rows data");
        type = "Error";
        title = "Error";
        if (e.getMessage().startsWith("<![CDATA[")) description = "<![CDATA[" + e.getMessage() + "]]>";
        else description = e.getMessage();
        e.printStackTrace();
      }
    }
    
    if (!type.startsWith("<![CDATA[")) type = "<![CDATA[" + type + "]]>";
    if (!title.startsWith("<![CDATA[")) title = "<![CDATA[" + title + "]]>";
    if (!description.startsWith("<![CDATA[")) description = "<![CDATA[" + description + "]]>";
    StringBuffer strRowsData = new StringBuffer();
    strRowsData.append("<xml-data>\n");
    strRowsData.append("  <status>\n");
    strRowsData.append("    <type>").append(type).append("</type>\n");
    strRowsData.append("    <title>").append(title).append("</title>\n");
    strRowsData.append("    <description>").append(description).append("</description>\n");
    strRowsData.append("  </status>\n");
    strRowsData.append("  <rows numRows=\"").append(strNumRows).append("\">\n");
    if (data!=null && data.length>0) {
      for (int j=0;j<data.length;j++) {
        strRowsData.append("    <tr>\n");
        for (int k=0;k<headers.length;k++) {
          strRowsData.append("      <td><![CDATA[");
          String columnname = headers[k].getField("columnname");
          
          /*
          if ((
        	   (headers[k].getField("iskey").equals("false") 
        	&& !headers[k].getField("gridcolumnname").equalsIgnoreCase("keyname"))
        	 || !headers[k].getField("iskey").equals("true")) && !tableSQL.getSelectField(columnname + "_R").equals("")) {
        	  columnname += "_R";
          }*/
          
          if ((data[j].getField(columnname)) != null) {
            if (headers[k].getField("adReferenceId").equals("32")) strRowsData.append(strReplaceWith).append("/images/");
            strRowsData.append(data[j].getField(columnname).replaceAll("<b>","").replaceAll("<B>","").replaceAll("</b>","").replaceAll("</B>","").replaceAll("<i>","").replaceAll("<I>","").replaceAll("</i>","").replaceAll("</I>","").replaceAll("<p>","&nbsp;").replaceAll("<P>","&nbsp;").replaceAll("<br>","&nbsp;").replaceAll("<BR>","&nbsp;"));
          } else {
            if (headers[k].getField("adReferenceId").equals("32")) {
              strRowsData.append(strReplaceWith).append("/images/blank.gif");
            } else strRowsData.append("&nbsp;");
          }
          strRowsData.append("]]></td>\n");
        }
        strRowsData.append("    </tr>\n");
      }
    }
    strRowsData.append("  </rows>\n");
    strRowsData.append("</xml-data>\n");
        
    response.setContentType("text/xml; charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    PrintWriter out = response.getWriter();
    if (log4j.isDebugEnabled()) log4j.debug(strRowsData.toString());  
    out.print(strRowsData.toString());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents the delivery-note lines seeker";
  } // end of getServletInfo() method
}
