/*
 ************************************************************************************
 * Copyright (C) 2015-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.system.Client;
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
    addProductCategoryToAssortment(event);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    addProductCategoryToAssortment(event);
  }

  private void addProductCategoryToAssortment(EntityPersistenceEvent event) {
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
      OBRETCOProlProduct assortmentProduct = (OBRETCOProlProduct) event.getTargetInstance();
      ProductCategory productCategory = assortmentProduct.getProduct().getProductCategory();
      OBRETCOProductList assortment = assortmentProduct.getObretcoProductlist();
      if (isNewProductCategory(assortment, productCategory, assortmentProduct.getClient())) {
        final OBRETCOProductcategory assortmentProdCat = OBProvider.getInstance().get(
            OBRETCOProductcategory.class);
        assortmentProdCat.setOrganization(assortmentProduct.getOrganization());
        assortmentProdCat.setProductCategory(productCategory);
        assortmentProdCat.setObretcoProductlist(assortment);
        OBDal.getInstance().save(assortmentProdCat);
      }
    }
  }

  private boolean isNewProductCategory(OBRETCOProductList assortment,
      ProductCategory productCategory, Client client) {
    StringBuilder hql = new StringBuilder();
    hql.append("select id from ").append(OBRETCOProductcategory.ENTITY_NAME);
    hql.append(" where ").append(OBRETCOProductcategory.PROPERTY_CLIENT).append(" = :client");
    hql.append("  and ").append(OBRETCOProductcategory.PROPERTY_PRODUCTCATEGORY)
        .append(" = :prodCat");
    hql.append(" and ").append(OBRETCOProductcategory.PROPERTY_OBRETCOPRODUCTLIST)
        .append(" = :assortment");
    Query<Object> qry = OBDal.getInstance().getSession().createQuery(hql.toString(), Object.class);
    qry.setParameter("client", client);
    qry.setParameter("assortment", assortment);
    qry.setParameter("prodCat", productCategory);

    return qry.uniqueResult() == null;
  }
}