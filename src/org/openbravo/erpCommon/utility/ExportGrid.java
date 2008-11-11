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
 * All portions are Copyright (C) 2007 Openbravo SL
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
package org.openbravo.erpCommon.utility;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.data.FieldProvider;

import java.util.LinkedList;
import java.util.Vector;
import javax.servlet.http.*;
import java.io.*;
import javax.servlet.*;
import java.sql.*;
import net.sf.jasperreports.engine.JRException;

public class ExportGrid extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private GridBO gridBO;
  private PreparedStatement st = null;

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String strTabId = vars.getRequiredStringParameter("inpTabId");
    String strWindowId = vars.getRequiredStringParameter("inpWindowId");
    String strAccessLevel = vars.getRequiredStringParameter("inpAccessLevel");
    if (log4j.isDebugEnabled()) log4j.debug("Export grid, tabID: " + strTabId);
    ServletOutputStream os=null;
    InputStream is = null;

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);
    if (log4j.isDebugEnabled()) log4j.debug("*********************Base design path: " + strBaseDesign);

    try {
      GridReportVO gridReportVO = createGridReport(vars, strTabId, strWindowId, strAccessLevel, vars.commandIn("EXCEL"));
      os = response.getOutputStream();
      is = getInputStream(strBaseDesign+"/org/openbravo/erpCommon/utility/"+gridReportVO.getJrxmlTemplate());
      gridBO = new GridBO();

      if(log4j.isDebugEnabled()) log4j.debug("Create report, type: " + vars.getCommand());

      if (vars.commandIn("HTML")) gridBO.createHTMLReport(is, gridReportVO, os);
      else if (vars.commandIn("PDF")) {
        response.setContentType("application/pdf");
        gridBO.createPDFReport(is, gridReportVO, os);
      } else if (vars.commandIn("EXCEL")) {
        response.setContentType("application/vnd.ms-excel");
        gridBO.createXLSReport(is, gridReportVO, os);
      } else if (vars.commandIn("CSV")) {
        response.setContentType("text/csv");
        gridBO.createCSVReport(is, gridReportVO, os);
      }
    } catch (JRException e){
      throw new ServletException(e.getMessage());
    } finally {
      try {
        this.releasePreparedStatement(st);
      } catch (SQLException ex){ }
      is.close();
      os.close();
    }
  }

  GridReportVO createGridReport(VariablesSecureApp vars, String strTabId, String strWindowId, String strAccessLevel) throws ServletException{
	return createGridReport(vars, strTabId, strWindowId, strAccessLevel, false);
  }

  GridReportVO createGridReport(VariablesSecureApp vars, String strTabId, String strWindowId, String strAccessLevel, boolean useFieldLength) throws ServletException{
    if (log4j.isDebugEnabled()) log4j.debug("Create Grid Report, tabID: " + strTabId);
    LinkedList<GridColumnVO> columns = new LinkedList<GridColumnVO>();
    FieldProvider[] data = null;
    TableSQLData tableSQL = null;
    try {
      tableSQL = new TableSQLData(vars, this, strTabId, Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId, Integer.valueOf(strAccessLevel).intValue()), Utility.getContext(this, vars, "#User_Client", strWindowId), Utility.getContext(this, vars, "ShowAudit", strWindowId).equals("Y"));
    } catch (Exception ex) {
      ex.printStackTrace();
      log4j.error(ex.getMessage());
      throw new ServletException(ex.getMessage());
    }
    SQLReturnObject[] headers = tableSQL.getHeaders(true, useFieldLength);

    if (tableSQL!=null && headers!=null) {
      try{
        if (log4j.isDebugEnabled()) log4j.debug("Geting the grid data.");
        vars.setSessionValue(strTabId + "|newOrder", "1");
        String strSQL = ModelSQLGeneration.generateSQL(this, vars, tableSQL, "", new Vector<String>(), new Vector<String>(), 0, 0);
        //if (log4j.isDebugEnabled()) log4j.debug("SQL: " + strSQL);
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValues());
        st = this.getPreparedStatement(strSQL);
        data = execquery.select();
      } catch (Exception e) {
        if (log4j.isDebugEnabled()) log4j.debug("Error obtaining rows data");
        e.printStackTrace();
        throw new ServletException(e.getMessage());
      }
    }
    int totalWidth = 0;
    for (int i=0; i<headers.length; i++){
      if (headers[i].getField("isvisible").equals("true")){
        String columnname = headers[i].getField("columnname");
        if (!tableSQL.getSelectField(columnname + "_R").equals("")) columnname += "_R";
        if (log4j.isDebugEnabled()) log4j.debug("Add column: " + columnname + " width: " + headers[i].getField("width") + " reference: " + headers[i].getField("adReferenceId"));
        totalWidth += Integer.valueOf(headers[i].getField("width"));
        Class<?> fieldClass = String.class;
        if (headers[i].getField("adReferenceId").equals("11")) fieldClass = Double.class;
        else if (headers[i].getField("adReferenceId").equals("22") || headers[i].getField("adReferenceId").equals("12") || headers[i].getField("adReferenceId").equals("800008") || headers[i].getField("adReferenceId").equals("800019")) fieldClass = java.math.BigDecimal.class;

        columns.add(new GridColumnVO(headers[i].getField("name"), columnname, Integer.valueOf(headers[i].getField("width")), fieldClass));
      }
    }
    String strTitle = ExportGridData.getTitle(this, strTabId, vars.getLanguage());
    GridReportVO gridReportVO = new GridReportVO("plantilla.jrxml", data, strTitle, columns, strReplaceWithFull, totalWidth, vars.getJavaDateFormat());
    return gridReportVO;
  }
  private InputStream getInputStream(String reportFile) throws IOException {
    if (log4j.isDebugEnabled()) log4j.debug("Get input stream file: " + reportFile);
    return (new FileInputStream(reportFile));
  }
}
