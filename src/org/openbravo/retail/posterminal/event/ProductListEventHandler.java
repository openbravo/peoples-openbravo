/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.config.OBRETCOProductcategory;
import org.openbravo.retail.config.OBRETCOProlProduct;

public class ProductListEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      OBRETCOProlProduct.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      logger.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    if (isRemote) {
      Product product = (Product) event.getTargetInstance().get("product");
      OBRETCOProductList assortment = (OBRETCOProductList) event.getTargetInstance().get(
          "obretcoProductlist");
      final List<OBRETCOProductcategory> filterproductcategorylist = assortment
          .getOBRETCOProductcategoryList();
      if (!existProductCategory(product.getProductCategory(), filterproductcategorylist)) {
        final OBRETCOProductcategory productCategoryElement = OBProvider.getInstance().get(
            OBRETCOProductcategory.class);
        productCategoryElement.setClient(assortment.getClient());
        productCategoryElement.setOrganization(assortment.getOrganization());
        productCategoryElement.setProductCategory(product.getProductCategory());
        productCategoryElement.setObretcoProductlist(assortment);
        filterproductcategorylist.add(productCategoryElement);
      }
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      logger.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    if (isRemote) {
      Product product = (Product) event.getTargetInstance().get("product");
      OBRETCOProductList assortment = (OBRETCOProductList) event.getTargetInstance().get(
          "obretcoProductlist");
      final List<OBRETCOProductcategory> filterproductcategorylist = assortment
          .getOBRETCOProductcategoryList();
      if (!existProductCategory(product.getProductCategory(), filterproductcategorylist)) {
        final OBRETCOProductcategory productCategoryElement = OBProvider.getInstance().get(
            OBRETCOProductcategory.class);
        productCategoryElement.setClient(assortment.getClient());
        productCategoryElement.setOrganization(assortment.getOrganization());
        productCategoryElement.setProductCategory(product.getProductCategory());
        productCategoryElement.setObretcoProductlist(assortment);
        filterproductcategorylist.add(productCategoryElement);
      }
    }
  }

  public boolean existProductCategory(ProductCategory productCategory,
      List<OBRETCOProductcategory> pcategorylist) {
    for (final OBRETCOProductcategory e : pcategorylist) {
      if (e.getProductCategory().equals(productCategory)) {
        return true;
      }
    }
    return false;
  }
}