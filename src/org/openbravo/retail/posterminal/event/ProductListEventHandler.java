/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import javax.enterprise.event.Observes;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LockOptions;
import org.hibernate.query.Query;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.config.OBRETCOProductcategory;
import org.openbravo.retail.config.OBRETCOProlProduct;

public class ProductListEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(OBRETCOProlProduct.ENTITY_NAME) };
  protected Logger logger = LogManager.getLogger();

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final OBRETCOProlProduct assortmentProduct = (OBRETCOProlProduct) event.getTargetInstance();
    addProductCategoryToAssortment(assortmentProduct.getObretcoProductlist(),
        assortmentProduct.getProduct().getProductCategory());
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final Entity prolProductEntity = ModelProvider.getInstance()
        .getEntity(OBRETCOProlProduct.ENTITY_NAME);
    final Property prolProductProductProperty = prolProductEntity
        .getProperty(OBRETCOProlProduct.PROPERTY_PRODUCT);

    final OBRETCOProlProduct assortmentProduct = (OBRETCOProlProduct) event.getTargetInstance();
    final Product previousProduct = (Product) event.getPreviousState(prolProductProductProperty);
    final Product currentProduct = (Product) event.getCurrentState(prolProductProductProperty);

    if (!StringUtils.equals(previousProduct.getId(), currentProduct.getId())) {
      removeProductCategoryFromAssortment(assortmentProduct.getObretcoProductlist(),
          previousProduct.getProductCategory());
      addProductCategoryToAssortment(assortmentProduct.getObretcoProductlist(),
          currentProduct.getProductCategory());
    }

  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final OBRETCOProlProduct assortmentProduct = (OBRETCOProlProduct) event.getTargetInstance();
    removeProductCategoryFromAssortment(assortmentProduct.getObretcoProductlist(),
        assortmentProduct.getProduct().getProductCategory());
  }

  private void addProductCategoryToAssortment(final OBRETCOProductList assortment,
      final ProductCategory productCategory) {
    final OBRETCOProductcategory assortmentProductCategory = getAssortmentProductCategory(
        assortment, productCategory);
    if (assortmentProductCategory == null && !productCategory.isSummaryLevel()) {
      createAssortmentProductCategory(assortment, productCategory);
    }
  }

  private void removeProductCategoryFromAssortment(final OBRETCOProductList assortment,
      final ProductCategory productCategory) {
    if (getAssortmentProductCountByCategory(assortment, productCategory) == 1) {
      final OBRETCOProductcategory assortmentProductCategory = getAssortmentProductCategory(
          assortment, productCategory);
      if (assortmentProductCategory != null) {
        removeAssortmentProductCategory(assortmentProductCategory);
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

  private void removeAssortmentProductCategory(
      final OBRETCOProductcategory assortmentProductCategory) {
    OBDal.getInstance().remove(assortmentProductCategory);
  }

  private OBRETCOProductcategory getAssortmentProductCategory(final OBRETCOProductList assortment,
      final ProductCategory productCategory) {
    Query<OBRETCOProductList> assormentQuery = OBDal.getInstance()
        .getSession()
        .createQuery("from OBRETCO_ProductList where id=:assortmentId", OBRETCOProductList.class);
    assormentQuery.setParameter("assortmentId", assortment.getId());
    assormentQuery.setLockOptions(LockOptions.UPGRADE);
    OBRETCOProductList lockedAssortment = assormentQuery.uniqueResult();

    if (lockedAssortment != null) {
      final String query = "from OBRETCO_Productcategory where client.id=:clientId and "
          + "productCategory.id=:categoryId and obretcoProductlist.id=:assortmentId";
      Query<OBRETCOProductcategory> qry = OBDal.getInstance()
          .getSession()
          .createQuery(query, OBRETCOProductcategory.class);
      qry.setParameter("clientId", lockedAssortment.getClient().getId());
      qry.setParameter("categoryId", productCategory.getId());
      qry.setParameter("assortmentId", lockedAssortment.getId());
      return (OBRETCOProductcategory) qry.uniqueResult();
    }
    return null;
  }

  private long getAssortmentProductCountByCategory(final OBRETCOProductList assortment,
      final ProductCategory productCategory) {
    Query<OBRETCOProductList> assormentQuery = OBDal.getInstance()
        .getSession()
        .createQuery("from OBRETCO_ProductList where id=:assortmentId", OBRETCOProductList.class);
    assormentQuery.setParameter("assortmentId", assortment.getId());
    assormentQuery.setLockOptions(LockOptions.UPGRADE);
    OBRETCOProductList lockedAssortment = assormentQuery.uniqueResult();

    if (lockedAssortment != null) {
      final String query = "select count(id) from OBRETCO_Prol_Product where client.id=:clientId and "
          + "obretcoProductlist.id=:assortmentId and product.productCategory.id=:categoryId";
      @SuppressWarnings("rawtypes")
      Query qry = OBDal.getInstance().getSession().createQuery(query);
      qry.setParameter("clientId", lockedAssortment.getClient().getId());
      qry.setParameter("assortmentId", lockedAssortment.getId());
      qry.setParameter("categoryId", productCategory.getId());
      return (long) qry.uniqueResult();
    }
    return 0;
  }

}
