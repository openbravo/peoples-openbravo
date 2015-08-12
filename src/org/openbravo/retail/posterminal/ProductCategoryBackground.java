/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.config.OBRETCOProductcategory;
import org.openbravo.retail.config.OBRETCOProlProduct;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductCategoryBackground extends DalBaseProcess {
  private static final Logger log = LoggerFactory.getLogger(ProductCategoryBackground.class);

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    ProcessLogger bgLogger = bundle.getLogger();
    VariablesSecureApp vars = bundle.getContext().toVars();
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    if (isRemote) {
      Client client = OBDal.getInstance().get(Client.class, vars.getClient());
      Organization org = OBDal.getInstance().get(Organization.class, vars.getOrg());
      OBContext.setAdminMode(true);
      try {
        OBCriteria<OBRETCOProductList> assormentList = OBDal.getInstance().createCriteria(
            OBRETCOProductList.class);
        assormentList.add(Restrictions.eq(OBRETCOProductList.PROPERTY_CLIENT, client));
        assormentList.add(Restrictions.eq(OBRETCOProductList.PROPERTY_ORGANIZATION, org));

        for (OBRETCOProductList assortment : assormentList.list()) {
          List<ProductCategory> productCategoryList = new ArrayList<ProductCategory>();
          List<OBRETCOProductcategory> productCategoryElementList = new ArrayList<OBRETCOProductcategory>();

          OBCriteria<OBRETCOProlProduct> productList = OBDal.getInstance().createCriteria(
              OBRETCOProlProduct.class);
          productList.add(Restrictions.eq(OBRETCOProlProduct.PROPERTY_OBRETCOPRODUCTLIST,
              assortment));
          assortment.getOBRETCOProductcategoryList().clear();
          OBDal.getInstance().save(assortment);
          OBDal.getInstance().flush();
          String logMsg = "Product category list cleared for assortment: " + assortment.getName();
          bgLogger.log(logMsg + "\n\n");
          log.debug(logMsg);

          for (OBRETCOProlProduct assortmentProduct : productList.list()) {
            if (!productCategoryList.contains(assortmentProduct.getProduct().getProductCategory())
                && assortmentProduct.getProduct().isActive() == true) {
              productCategoryList.add(assortmentProduct.getProduct().getProductCategory());

              final OBRETCOProductcategory productCategoryElement = OBProvider.getInstance().get(
                  OBRETCOProductcategory.class);
              productCategoryElement.setClient(assortment.getClient());
              productCategoryElement.setOrganization(assortment.getOrganization());
              productCategoryElement.setProductCategory(assortmentProduct.getProduct()
                  .getProductCategory());
              productCategoryElement.setObretcoProductlist(assortment);
              OBDal.getInstance().save(productCategoryElement);
              productCategoryElementList.add(productCategoryElement);
              assortment.getOBRETCOProductcategoryList().add(productCategoryElement);

              logMsg = "Product category: "
                  + assortmentProduct.getProduct().getProductCategory().getName()
                  + " created for assortment: " + assortment.getName();
              bgLogger.log(logMsg + "\n\n");
              log.debug(logMsg);
            }
          }
        }
      } catch (Exception e) {
        log.error("Error executing product category background ", e);
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }
}
