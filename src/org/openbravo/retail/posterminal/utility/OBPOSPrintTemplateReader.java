/*
 ************************************************************************************
 * Copyright (C) 2018-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.ConfigParameters;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.PrintTemplate;
import org.openbravo.retail.posterminal.PrintTemplateSubrep;

/**
 * Get an identifier for template files to check changes in them
 *
 * @author mdj
 */
public class OBPOSPrintTemplateReader {
  private static final Logger log = LogManager.getLogger();
  String printTemplateIdentifier = null;
  private static OBPOSPrintTemplateReader instance = new OBPOSPrintTemplateReader();

  public static final OBPOSPrintTemplateReader getInstance() {
    return instance;
  }

  public String getPrintTemplatesIdentifier() {
    if (printTemplateIdentifier == null) {
      final StringBuilder sb = new StringBuilder();
      OBCriteria<PrintTemplate> criteria = OBDal.getInstance().createCriteria(PrintTemplate.class);
      criteria.addOrderBy(PrintTemplate.PROPERTY_ID, true);
      for (PrintTemplate template : criteria.list()) {
        try {
          if (template.getTemplatePath() != null || !template.getTemplatePath().isEmpty()) {
            sb.append(readTemplateFile(template.getTemplatePath()));
            if (template.isPdf()) {
              for (PrintTemplateSubrep subreport : template.getOBPOSPrintTemplateSubrepList()) {
                sb.append(readTemplateFile(subreport.getTemplatePath()));
              }
            }
          }
        } catch (Exception e) {
          log.error("Error reading file: " + template.getTemplatePath(), e);
        }
      }
      printTemplateIdentifier = DigestUtils.md5Hex(sb.toString());
    }
    return printTemplateIdentifier;
  }

  private String readTemplateFile(String templatePath) throws IOException {
    ConfigParameters confParam = ConfigParameters.retrieveFrom(RequestContext.getServletContext());
    final File file = new File(
        confParam.prefix + "web/org.openbravo.retail.posterminal/" + templatePath);
    if (!file.exists() || !file.canRead()) {
      log.error(templatePath + " cannot be read");
      return "";
    }
    return FileUtils.readFileToString(file, "UTF-8");
  }
}
