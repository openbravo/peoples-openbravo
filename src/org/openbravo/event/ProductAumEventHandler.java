/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.event;

import java.util.List;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.materialmgmt.UOMUtil;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductAUM;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * 
 * @author Nono Carballo
 *
 */
public class ProductAumEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance()
      .getEntity(ProductAUM.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    validateAum(event);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    validateAum(event);
  }

  /**
   * Checks for duplicate Aum for the product and the uniqueness of a primary Aum for Sales,
   * Purchase and Logistic flows.
   * 
   * @param event
   */
  private void validateAum(EntityPersistenceEvent event) {
    String language = OBContext.getOBContext().getLanguage().getLanguage();
    ConnectionProvider conn = new DalConnectionProvider(false);

    ProductAUM target = (ProductAUM) event.getTargetInstance();
    Product product = target.getProduct();
    List<ProductAUM> productAumList = product.getProductAUMList();
    for (ProductAUM productAum : productAumList) {
      if (productAum.getId().equals(target.getId()) && event instanceof EntityNewEvent) {
        throw new OBException(Utility.messageBD(conn, "DuplicateAUM", language));
      }
      if (target.getSales().equals(UOMUtil.UOM_PRIMARY)
          && productAum.getSales().equals(UOMUtil.UOM_PRIMARY)
          && !target.getId().equals(productAum.getId())) {
        throw new OBException(Utility.messageBD(conn, "DuplicatePrimarySalesAUM", language));
      }
      if (target.getPurchase().equals(UOMUtil.UOM_PRIMARY)
          && productAum.getPurchase().equals(UOMUtil.UOM_PRIMARY)
          && !target.getId().equals(productAum.getId())) {
        throw new OBException(Utility.messageBD(conn, "DuplicatePrimaryPurchaseAUM", language));
      }
      if (target.getLogistics().equals(UOMUtil.UOM_PRIMARY)
          && productAum.getLogistics().equals(UOMUtil.UOM_PRIMARY)
          && !target.getId().equals(productAum.getId())) {
        throw new OBException(Utility.messageBD(conn, "DuplicatePrimaryLogisticsAUM", language));
      }
    }
  }
}
