/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2007-2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openbravo.client.application.report.ReportingUtils;
import org.openbravo.client.application.report.ReportingUtils.ExportType;

class GridBO {
  public static void createHTMLReport(InputStream reportFile, GridReportVO gridReportVO,
      String path, String fileName) throws JRException, IOException {
    gridReportVO.setPagination(false);
    JasperPrint jasperPrint = ReportingUtils.createJasperPrint(reportFile, gridReportVO);
    ReportingUtils.saveReport(jasperPrint, ExportType.HTML, null, new File(path + "/" + fileName));
  }

  public static void createPDFReport(InputStream reportFile, GridReportVO gridReportVO,
      String path, String fileName) throws JRException, IOException {
    gridReportVO.setPagination(false);
    JasperPrint jasperPrint = ReportingUtils.createJasperPrint(reportFile, gridReportVO);
    ReportingUtils.saveReport(jasperPrint, ExportType.PDF, null, new File(path + "/" + fileName));
  }

  public static void createXLSReport(InputStream reportFile, GridReportVO gridReportVO,
      String path, String fileName) throws JRException, IOException {
    gridReportVO.setPagination(true);
    JasperPrint jasperPrint = ReportingUtils.createJasperPrint(reportFile, gridReportVO);
    ReportingUtils.saveReport(jasperPrint, ExportType.XLS, null, new File(path + "/" + fileName));
  }

  public static void createCSVReport(InputStream reportFile, GridReportVO gridReportVO,
      String path, String fileName) throws JRException, IOException {
    gridReportVO.setPagination(true);
    JasperPrint jasperPrint = ReportingUtils.createJasperPrint(reportFile, gridReportVO);
    ReportingUtils.saveReport(jasperPrint, ExportType.CSV, null, new File(path + "/" + fileName));
  }

  public static void createXMLReport(InputStream reportFile, GridReportVO gridReportVO,
      OutputStream os) throws JRException, IOException {
    gridReportVO.setPagination(true);
    JasperPrint jasperPrint = ReportingUtils.createJasperPrint(reportFile, gridReportVO);
    ReportingUtils.saveReport(jasperPrint, ExportType.XML, null, os);
  }
}
