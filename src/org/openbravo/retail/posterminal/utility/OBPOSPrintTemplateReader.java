/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.io.File;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.ConfigParameters;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.StaticResourceComponent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.PrintTemplate;

/**
 * Get an identifier for template files to check changes in them
 *
 * @author mdj
 */
public class OBPOSPrintTemplateReader {
  private static final Logger log = Logger.getLogger(StaticResourceComponent.class);
  String printTemplateIdentifier = null;
  private static OBPOSPrintTemplateReader instance = new OBPOSPrintTemplateReader();

  public static OBPOSPrintTemplateReader getInstance() {
    return instance;
  }

  public String getPrintTemplatesIdentifier() {
    if(printTemplateIdentifier == null) {
      final StringBuffer sb = new StringBuffer();
      for (PrintTemplate template : OBDal.getInstance().createCriteria(PrintTemplate.class).list()) {
        try {
          ConfigParameters confParam = ConfigParameters
              .retrieveFrom(RequestContext.getServletContext());
          final File file = new File(confParam.prefix
              + "web/org.openbravo.retail.posterminal/"+template.getTemplatePath());
          if (!file.exists() || !file.canRead()) {
            log.error(template.getTemplatePath() + " cannot be read");
            continue;
          }
          String resourceContents = FileUtils.readFileToString(file, "UTF-8");
          sb.append(resourceContents);
        } catch (Exception e) {
          log.error("Error reading file: " + template.getTemplatePath(), e);
        }
      }
      printTemplateIdentifier = DigestUtils.md5Hex(sb.toString());
    }
    return printTemplateIdentifier;
  }

}
