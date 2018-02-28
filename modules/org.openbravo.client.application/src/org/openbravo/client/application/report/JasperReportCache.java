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
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.report;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;

import net.sf.jasperreports.engine.JasperReport;

/**
 * This class is used to keep in cache the reports generated through the {@link ReportingUtils}
 * class.
 */
class JasperReportCache {
  private static JasperReportCache instance = new JasperReportCache();

  private ConcurrentHashMap<String, CompiledJasperReport> jasperReports;
  private boolean isDisabled;

  private JasperReportCache() {
    jasperReports = new ConcurrentHashMap<>();
    isDisabled = isInDevelopment();
  }

  private boolean isInDevelopment() {
    final String query = "select 1 from ADModule m where m.inDevelopment=true";
    final Query indevelMods = OBDal.getInstance().getSession().createQuery(query);
    indevelMods.setMaxResults(1);
    return !indevelMods.list().isEmpty();
  }

  static JasperReportCache getInstance() {
    return instance;
  }

  JasperReport getReport(String reportPath, String language) {
    String key = reportPath + "-" + language;
    CompiledJasperReport compiledReport = getCompiledReport(key);
    if (compiledReport == null) {
      return null;
    }
    return compiledReport.mainReport;
  }

  JasperReport getReport(String reportPath) {
    CompiledJasperReport compiledReport = getCompiledReport(reportPath);
    if (compiledReport == null) {
      return null;
    }
    return compiledReport.mainReport;
  }

  Map<String, JasperReport> getSubReports(String reportPath, String language) {
    String key = reportPath + "-" + language;
    CompiledJasperReport compiledReport = getCompiledReport(key);
    if (compiledReport == null) {
      return null;
    }
    return compiledReport.subReports;
  }

  private CompiledJasperReport getCompiledReport(String key) {
    return jasperReports.get(key);
  }

  void put(String reportPath, String language, JasperReport jasperReport) {
    put(reportPath, language, jasperReport, null);
  }

  void put(String reportPath, String language, JasperReport jasperReport,
      Map<String, JasperReport> subReports) {
    String key = reportPath + "-" + language;
    CompiledJasperReport compiledJasperReport;
    if (subReports == null) {
      compiledJasperReport = new CompiledJasperReport(jasperReport);
    } else {
      compiledJasperReport = new CompiledJasperReport(jasperReport, subReports);
    }
    put(key, compiledJasperReport);
  }

  void put(String reportPath, JasperReport jasperReport) {
    CompiledJasperReport compiledJasperReport = new CompiledJasperReport(jasperReport);
    put(reportPath, compiledJasperReport);
  }

  private void put(String key, CompiledJasperReport compiledReport) {
    if (isDisabled) {
      return;
    }
    jasperReports.putIfAbsent(key, compiledReport);
  }

  private static class CompiledJasperReport {
    JasperReport mainReport;
    Map<String, JasperReport> subReports;

    public CompiledJasperReport(JasperReport mainReport) {
      this.mainReport = mainReport;
    }

    public CompiledJasperReport(JasperReport mainReport, Map<String, JasperReport> subReports) {
      this.mainReport = mainReport;
      this.subReports = subReports;
    }
  }
}
