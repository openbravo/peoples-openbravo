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
 * All portions are Copyright (C) 2012-2020 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.openbravo.client.kernel.StaticResourceProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.MobileCoreApplicationCacheComponent;
import org.openbravo.mobile.core.MobileCoreConstants;
import org.openbravo.retail.posterminal.utility.OBPOSPrintTemplateReader;

/**
 * 
 * @author iperdomo
 */

@RequestScoped
public class ApplicationCacheComponent extends MobileCoreApplicationCacheComponent {

  @Inject
  private StaticResourceProvider resourceProvider;

  @Override
  public List<String> getAppList() {
    // TODO: review this list: is it needed to be hardcoded?
    List<String> resources = new ArrayList<String>();

    resources.add("../../web/org.openbravo.client.kernel/js/LAB.min.js");
    resources.add("../../web/org.openbravo.retail.posterminal/js/libs/jquery-1.7.2.js");

    // Sounds
    resources.add("../../web/org.openbravo.retail.posterminal/sounds/drawerAlert.mp3");

    // App Icon
    resources.add("../../web/images/favicon.ico");

    OBContext.setAdminMode(true);
    try {
      OBCriteria<PrintTemplate> criteria = OBDal.getInstance().createCriteria(PrintTemplate.class);
      criteria.addOrderBy(PrintTemplate.PROPERTY_ID, true);
      for (PrintTemplate template : criteria.list()) {
        resources.add("../../web/org.openbravo.retail.posterminal/" + template.getTemplatePath()
            + "?hash=" + OBPOSPrintTemplateReader.getInstance().getPrintTemplatesIdentifier());
        if (template.isPdf()) {
          for (PrintTemplateSubrep subreport : template.getOBPOSPrintTemplateSubrepList()) {
            resources.add("../../web/org.openbravo.retail.posterminal/"
                + subreport.getTemplatePath() + "?hash="
                + OBPOSPrintTemplateReader.getInstance().getPrintTemplatesIdentifier());
          }
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    resources
        .add("../../org.openbravo.mobile.core/OBCLKER_Kernel/StyleSheetResources?_appName=WebPOS");

    resources.sort(Comparator.comparing(String::toString));
    return resources;
  }

  @Override
  public String getETag() {
    final String appNameKey = getAppNameKey();
    if (resourceProvider.getStaticResourceCachedInfo(appNameKey) == null) {
      // do something unique
      return String.valueOf(System.currentTimeMillis());
    } else {
      // compute the md5 of the cached content
      return DigestUtils.md5Hex(resourceProvider.getStaticResourceCachedInfo(appNameKey));
    }
  }

  private String getAppNameKey() {
    return getApplicationName() + "_" + MobileCoreConstants.APP_CACHE_COMPONENT;
  }

  @Override
  public String generate() {
    final String content = super.generate();
    if (!isInDevelopment()) {
      resourceProvider.putStaticResourceCachedInfo(getAppNameKey(), content);
    }
    return content;
  }

  @Override
  public String getAppName() {
    return POSConstants.APP_NAME;
  }
}
