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
 * All portions are Copyright (C) 2012-2017 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.RequestScoped;

import org.apache.commons.io.FileUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.MobileCoreApplicationCacheComponent;
import org.openbravo.model.ad.module.ModuleDependency;

/**
 * 
 * @author iperdomo
 */

@RequestScoped
public class ApplicationCacheComponent extends MobileCoreApplicationCacheComponent {

  private static final String PATH_PREFIX = "web" + File.separatorChar;

  @Override
  public List<String> getAppList() {
    // TODO: review this list: is it needed to be hardcoded?
    List<String> resources = new ArrayList<String>();

    resources.add("../../web/org.openbravo.client.kernel/js/LAB.min.js");
    resources.add("../../web/org.openbravo.retail.posterminal/js/libs/jquery-1.7.2.js");
    resources.add("../../web/org.openbravo.retail.posterminal/js/libs/core-min.js");
    resources.add("../../web/org.openbravo.retail.posterminal/js/libs/sha1-min.js");

    // Boot code

    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=FinancialMgmtTaxRate&modelName=TaxRate&source=org.openbravo.retail.posterminal.master.TaxRate");
    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=FinancialMgmtTaxZone&modelName=TaxZone&source=org.openbravo.retail.posterminal.master.TaxZone");
    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=PricingProductPrice&modelName=ProductPrice&source=org.openbravo.retail.posterminal.master.ProductPrice");

    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=PricingAdjustment&modelName=Discount&source=org.openbravo.retail.posterminal.master.Discount");
    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=PricingAdjustmentBusinessPartner&modelName=DiscountFilterBusinessPartner&source=org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartner&remote=OBPOS_remote.discount.bp");
    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=PricingAdjustmentBusinessPartnerGroup&modelName=DiscountFilterBusinessPartnerGroup&source=org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartnerGroup");
    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=PricingAdjustmentProduct&modelName=DiscountFilterProduct&source=org.openbravo.retail.posterminal.master.DiscountFilterProduct");
    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=PricingAdjustmentProductCategory&modelName=DiscountFilterProductCategory&source=org.openbravo.retail.posterminal.master.DiscountFilterProductCategory");
    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=OBDISC_Offer_Role&modelName=DiscountFilterRole&source=org.openbravo.retail.posterminal.master.DiscountFilterRole");
    resources
        .add("../../org.openbravo.mobile.core/OBMOBC_Main/ClientModel?entity=PricingAdjustmentCharacteristic&modelName=DiscountFilterCharacteristic&source=org.openbravo.retail.posterminal.master.DiscountFilterCharacteristic");

    // default print templates
    resources.add("../../web/org.openbravo.retail.posterminal/res/printcashup.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/printcashmgmt.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/displaytotal.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/opendrawer.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/printclosedreceipt.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/printinvoice.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/printlayaway.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/printline.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/printreceipt.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/printreturn.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/printreturninvoice.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/welcome.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/goodbye.xml");
    resources.add("../../web/org.openbravo.retail.posterminal/res/checkdrawerstatus.xml");

    // App Icon
    resources.add("../../web/images/favicon.ico");

    OBContext.setAdminMode(true);
    try {
      for (PrintTemplate template : OBDal.getInstance().createCriteria(PrintTemplate.class).list()) {
        resources.add("../../web/org.openbravo.retail.posterminal/" + template.getTemplatePath());
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    resources
        .add("../../org.openbravo.mobile.core/OBCLKER_Kernel/StyleSheetResources?_appName=WebPOS");

    return resources;
  }

  @Override
  public List<String> getImageFileList() {
    final String[] extensions = { "png", "gif", "svg" };
    return getFileList(extensions);
  }

  private List<String> getFileList(String[] extensions) {
    List<String> fileList = new ArrayList<String>();
    String filePath, relativePath = PATH_PREFIX + getModulePackageName();
    int pos = 0;
    File directory = new File(RequestContext.getServletContext().getRealPath("/" + relativePath));
    Iterator<File> it = FileUtils.iterateFiles(directory, extensions, true);
    while (it.hasNext()) {
      final File f = (File) it.next();
      filePath = f.getPath();
      pos = filePath.indexOf(relativePath);
      fileList.add("../../" + filePath.substring(pos));
    }

    try {
      OBContext.setAdminMode();
      OBCriteria<ModuleDependency> mdCriteria = OBDal.getInstance().createCriteria(
          ModuleDependency.class);
      mdCriteria.add(Restrictions.eq(ModuleDependency.PROPERTY_DEPENDENTMODULE, getModule()));
      for (ModuleDependency dependency : mdCriteria.list()) {
        relativePath = PATH_PREFIX + dependency.getModule().getJavaPackage();
        directory = new File(RequestContext.getServletContext().getRealPath("/" + relativePath));
        if (directory.isDirectory()) {
          it = FileUtils.iterateFiles(directory, extensions, true);
          while (it.hasNext()) {
            final File f = (File) it.next();
            filePath = f.getPath();
            pos = filePath.indexOf(relativePath);
            fileList.add("../../" + filePath.substring(pos));
          }
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return fileList;
  }

  @Override
  public String getAppName() {
    return POSConstants.APP_NAME;
  }
}
