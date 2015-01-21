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
 * All portions are Copyright (C) 2014-2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.report;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.client.kernel.reference.UIDefinitionController.FormatDefinition;
import org.openbravo.dal.core.DalContextListener;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.JRFormatFactory;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.utility.FileType;
import org.openbravo.service.db.DalConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportingUtils {
  public static final String JASPER_PARAM_OBCONTEXT = "jasper_obContext";
  public static final String JASPER_PARAM_HBSESSION = "jasper_hbSession";
  private static final Logger log = LoggerFactory.getLogger(ReportingUtils.class);

  /**
   * Exports the report to a file in a temporary folder.
   * 
   * @param jasperFilePath
   *          The path to the JR template of the report.
   * @param expType
   *          The desired output type of the report.
   * @param parameters
   *          The parameters to be sent to Jasper Report.
   * @param strFileName
   *          The name to be used on the generated file.
   * @return a String with the complete path of the generated file.
   * @throws OBException
   *           In case there is any error generating the file an exception is thrown with the error
   *           message.
   */
  public static String exportJR(String jasperFilePath, ExportType expType,
      Map<String, Object> parameters, String strFileName) throws OBException {
    try {
      parameters.put(JASPER_PARAM_HBSESSION, OBDal.getInstance().getSession());
      parameters.put(JASPER_PARAM_OBCONTEXT, OBContext.getOBContext());
      parameters.putAll(expType.getExportParameters());

      {
        final FormatDefinition reportFormat = UIDefinitionController.getInstance()
            .getFormatDefinition("amount", UIDefinitionController.NORMALFORMAT_QUALIFIER);

        final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator(reportFormat.getDecimalSymbol().charAt(0));
        dfs.setGroupingSeparator(reportFormat.getGroupingSymbol().charAt(0));

        final DecimalFormat numberFormat = new DecimalFormat(correctMaskForGrouping(
            reportFormat.getFormat(), reportFormat.getDecimalSymbol(),
            reportFormat.getGroupingSymbol()), dfs);
        parameters.put("AMOUNTFORMAT", numberFormat);
      }

      {
        final FormatDefinition reportFormat = UIDefinitionController.getInstance()
            .getFormatDefinition("generalQty", UIDefinitionController.SHORTFORMAT_QUALIFIER);

        final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator(reportFormat.getDecimalSymbol().charAt(0));
        dfs.setGroupingSeparator(reportFormat.getGroupingSymbol().charAt(0));

        final DecimalFormat numberFormat = new DecimalFormat(correctMaskForGrouping(
            reportFormat.getFormat(), reportFormat.getDecimalSymbol(),
            reportFormat.getGroupingSymbol()), dfs);
        parameters.put("QUANTITYFORMAT", numberFormat);
      }

      final JRFormatFactory jrFormatFactory = new JRFormatFactory();
      jrFormatFactory.setDatePattern((OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java")));
      parameters.put(JRParameter.REPORT_FORMAT_FACTORY, jrFormatFactory);

      String strClientId = OBContext.getOBContext().getCurrentClient().getId();
      parameters.put("Current_Client_ID", strClientId);
      String strOrgs = "";
      boolean isNotFirst = false;
      for (String strOrgId : OBContext.getOBContext().getReadableOrganizations()) {
        if (isNotFirst) {
          strOrgs += ",";
        }
        strOrgs += "'" + strOrgId + "'";
        isNotFirst = true;
      }
      parameters.put("Readable_Organizations", strOrgs);

      JRSwapFileVirtualizer virtualizer = null;
      // if no custom virtualizer is requested use a default one
      if (!parameters.containsKey(JRParameter.REPORT_VIRTUALIZER)) {
        // virtualizer is essentially using a tmp-file to avoid huge memory consumption by jasper
        // when processing big reports
        JRSwapFile swap = new JRSwapFile(System.getProperty("java.io.tmpdir"), 4096, 1);
        // start using the virtualizer when having more than 100 pages of data
        virtualizer = new JRSwapFileVirtualizer(100, swap);
        parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
      }

      if (log.isDebugEnabled()) {
        log.debug("list of parameters available in the jasper report");
        for (Iterator<String> keys = parameters.keySet().iterator(); keys.hasNext();) {
          String key = keys.next();
          String value = "null";
          if (parameters.get(key) != null) {
            value = parameters.get(key).toString();
          }
          log.debug("parameter name: " + key + " value: " + value);
        }
      }

      JasperPrint jasperPrint = null;
      if (jasperFilePath.endsWith("jrxml")) {
        String strBaseDesign = DalContextListener.getServletContext().getRealPath("");
        JasperReport jReport = Utility.getTranslatedJasperReport(new DalConnectionProvider(false),
            jasperFilePath, OBContext.getOBContext().getLanguage().getLanguage(), strBaseDesign);
        jasperPrint = JasperFillManager.fillReport(jReport, parameters, OBDal.getInstance()
            .getConnection());

      } else {
        jasperPrint = JasperFillManager.fillReport(jasperFilePath, parameters);
      }
      String target = getTempFolder();
      if (!target.endsWith("/")) {
        target += "/";
      }
      target += strFileName;
      switch (expType) {
      case PDF:
        JasperExportManager.exportReportToPdfFile(jasperPrint, target);
        break;
      case XLS:
        JExcelApiExporter exporter = new JExcelApiExporter();
        Map<Object, Object> exportParameters = new HashMap<Object, Object>();
        exportParameters.put(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exportParameters.put(JRExporterParameter.OUTPUT_FILE_NAME, target);
        exportParameters.put(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
        exportParameters.put(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
            Boolean.TRUE);
        exportParameters.put(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, true);
        exporter.setParameters(exportParameters);
        exporter.exportReport();
        break;
      }

      return target;
    } catch (JRException e) {
      log.error("Error generating Jasper Report: " + jasperFilePath, e);
      throw new OBException(e.getMessage(), e);
    }
  }

  /**
   * Changes the mask to use "." as Decimal Symbol and "," as grouping symbol.
   * 
   * @param mask
   *          the current mask
   * @param decimalSymbol
   *          the current decimal symbol
   * @param groupingSymbol
   *          the current grouping symbol
   * @return the mask with the updated decimal and grouping symbols.
   */
  private static String correctMaskForGrouping(String mask, String decimalSymbol,
      String groupingSymbol) {
    final String strTmpDecSymbol = "xxTmpDecSymbolxx";
    String localMask = mask.replace(decimalSymbol, strTmpDecSymbol);
    localMask = localMask.replace(groupingSymbol, ",");
    return localMask.replace(strTmpDecSymbol, ".");
  }

  /**
   * enum with the supported Export Outputs. Includes custom properties to be used when the report
   * is generated.
   */
  public enum ExportType {
    PDF("pdf", "103", new HashMap<String, Object>() {
      {
        put("IS_IGNORE_PAGINATION", false);
      }
    }), //
    XLS("xls", "101", new HashMap<String, Object>() {
      {
        put("IS_IGNORE_PAGINATION", true);
      }
    });
    private final String extension;
    private final FileType fileType;
    private final Map<String, Object> params;

    ExportType(String extension, String strFileTypeId, Map<String, Object> params) {
      this.extension = extension;
      this.fileType = OBDal.getInstance().get(FileType.class, strFileTypeId);
      this.params = params;
    }

    public String getExtension() {
      return this.extension;
    }

    public String getContentType() {
      return fileType.getFormat();
    }

    public Map<String, Object> getExportParameters() {
      return params;
    }

    /**
     * Returns the corresponding ExportType item based on the action.
     */
    public static ExportType getExportType(String action) throws OBException {
      if ("PDF".equals(action)) {
        return ExportType.PDF;
      } else if ("XLS".equals(action)) {
        return ExportType.XLS;
      } else {
        throw new OBException(OBMessageUtils.messageBD("OBUIAPP_UnsupportedAction"));
      }
    }
  }

  private static String getTempFolder() {
    final String tmpFolder = System.getProperty("java.io.tmpdir");

    return tmpFolder;
  }
}