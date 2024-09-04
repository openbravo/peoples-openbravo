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
 * All portions are Copyright (C) 2024 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.event.Observes;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.plm.ProductionBOMType;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;
import org.openbravo.model.materialmgmt.transaction.ProductionTransaction;

class ProductionEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(ProductionTransaction.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final ProductionTransaction production = (ProductionTransaction) event.getTargetInstance();
    if (production.getProductionbomType() != null
        && production.getProductionbomType().getType().equalsIgnoreCase("T")) {
      final Entity productionEntity = ModelProvider.getInstance()
          .getEntity(ProductionTransaction.ENTITY_NAME);
      final Property recordsCreatedProperty = productionEntity
          .getProperty(ProductionTransaction.PROPERTY_RECORDSCREATED);
      event.setCurrentState(recordsCreatedProperty, true);
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final Entity productionEntity = ModelProvider.getInstance()
        .getEntity(ProductionTransaction.ENTITY_NAME);
    final Property recordsCreatedProperty = productionEntity
        .getProperty(ProductionTransaction.PROPERTY_RECORDSCREATED);
    final Property bomTypeProperty = productionEntity
        .getProperty(ProductionTransaction.PROPERTY_PRODUCTIONBOMTYPE);
    final Property userProperty = productionEntity
        .getProperty(ProductionTransaction.PROPERTY_USERCONTACT);
    final Property productionPlanListProperty = productionEntity
        .getProperty(ProductionTransaction.PROPERTY_MATERIALMGMTPRODUCTIONPLANLIST);

    ProductionBOMType bomType = (ProductionBOMType) event.getCurrentState(bomTypeProperty);
    ProductionBOMType previousBomType = (ProductionBOMType) event.getPreviousState(bomTypeProperty);

    if (bomType != null) {
      if (previousBomType == null || !previousBomType.getId().equals(bomType.getId())) {
        if (bomType.getType().equalsIgnoreCase("T")) {
          event.setCurrentState(recordsCreatedProperty, true);
        } else {
          event.setCurrentState(recordsCreatedProperty, false);
        }
      }
    } else if (previousBomType != null && previousBomType.getType().equalsIgnoreCase("T")) {
      event.setCurrentState(recordsCreatedProperty, false);
    }

    User assignedUser = (User) event.getCurrentState(userProperty);
    User previousAssignedUser = (User) event.getPreviousState(userProperty);
    @SuppressWarnings("unchecked")
    List<ProductionPlan> productionPlanList = (List<ProductionPlan>) event
        .getCurrentState(productionPlanListProperty);
    if (previousAssignedUser != null && assignedUser == null) {
      updateProductionLinesConfirmedQty(productionPlanList, null);
    } else if (previousAssignedUser == null && assignedUser != null) {
      updateProductionLinesConfirmedQty(productionPlanList, BigDecimal.ZERO);
    }
  }

  private int updateProductionLinesConfirmedQty(List<ProductionPlan> productionPlanList,
      BigDecimal confirmedQty) {
    //@formatter:off
    String hql =
             "update ManufacturingProductionLine pl" +
             "  set pl.confirmedQuantity = :confirmedQty," +
             "  pl.updated = :newDate," +
             "  pl.updatedBy.id = :currentUserId" +
             "  where pl.productionPlan.id in :planId";
    //@formatter:on

    return OBDal.getInstance()
        .getSession()
        .createQuery(hql)
        .setParameter("confirmedQty", confirmedQty)
        .setParameter("newDate", new Date())
        .setParameter("currentUserId", OBContext.getOBContext().getUser().getId())
        .setParameterList("planId", getPlanIdListFromPlans(productionPlanList))
        .executeUpdate();
  }

  private List<String> getPlanIdListFromPlans(List<ProductionPlan> productionPlanList) {
    List<String> planIdList = new ArrayList<>();
    for (ProductionPlan plans : productionPlanList) {
      planIdList.add(plans.getId());
    }
    return planIdList;
  }
}
