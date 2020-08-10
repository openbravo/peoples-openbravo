/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import java.util.List;

import javax.enterprise.event.Observes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.config.OBRETCOProductcategory;
import org.openbravo.retail.config.OBRETCOProlProduct;

public class ProductCategoryEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ProductCategory.ENTITY_NAME) };
  protected Logger logger = LogManager.getLogger();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final Entity productCategoryEntity = ModelProvider.getInstance()
        .getEntity(ProductCategory.ENTITY_NAME);
    final Property isSummaryProperty = productCategoryEntity
        .getProperty(ProductCategory.PROPERTY_SUMMARYLEVEL);
    final boolean previousSummary = (boolean) event.getPreviousState(isSummaryProperty);
    final boolean currentSummary = (boolean) event.getCurrentState(isSummaryProperty);
    ProductCategory productCtgry = (ProductCategory) event.getTargetInstance();

    if (previousSummary != currentSummary) {
      if (currentSummary) {
        //@formatter:off
        String hql = "as assortctgry "
            +" join assortctgry.productCategory as pc "
            +" where pc.id= :prodCategoryId ";
        //@formatter:on
        final List<OBRETCOProductcategory> assortmentProdCategoryList = OBDal.getInstance()
            .createQuery(OBRETCOProductcategory.class, hql)
            .setNamedParameter("prodCategoryId", productCtgry.getId())
            .list();

        // Remove product category from assortment
        for (OBRETCOProductcategory assortProdCtgry : assortmentProdCategoryList) {
          OBDal.getInstance().remove(assortProdCtgry);
        }
      } else {
        //@formatter:off
        String hql = "as asortprod "
            +" join asortprod.product as p "
            +" join p.productCategory as pc "
            +" where pc.id= :prodCategoryId ";
        //@formatter:on
        final List<OBRETCOProlProduct> assortmentProdList = OBDal.getInstance()
            .createQuery(OBRETCOProlProduct.class, hql)
            .setNamedParameter("prodCategoryId", productCtgry.getId())
            .list();

        // Add product category to assortment
        for (OBRETCOProlProduct assortProd : assortmentProdList) {
          createAssortmentProductCategory(assortProd.getObretcoProductlist(), productCtgry);
        }
      }
    }
  }

  private void createAssortmentProductCategory(final OBRETCOProductList assortment,
      final ProductCategory productCategory) {
    final OBRETCOProductcategory assortmentProductCategory = OBProvider.getInstance()
        .get(OBRETCOProductcategory.class);
    assortmentProductCategory.setOrganization(assortment.getOrganization());
    assortmentProductCategory.setObretcoProductlist(assortment);
    assortmentProductCategory.setProductCategory(productCategory);
    OBDal.getInstance().save(assortmentProductCategory);
  }
}
