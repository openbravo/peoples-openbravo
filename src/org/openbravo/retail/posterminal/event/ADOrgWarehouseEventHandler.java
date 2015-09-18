/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.OrgWarehouse;
import org.openbravo.retail.posterminal.OBPOS_OrgWarehouseExtra;
import org.openbravo.service.db.DalConnectionProvider;

public class ADOrgWarehouseEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      OrgWarehouse.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes
  EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    OrgWarehouse orgWarehouse = (OrgWarehouse) event.getTargetInstance();
    warehouseAddedInCCWarehouses(orgWarehouse);
  }

  public void onSave(@Observes
  EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    OrgWarehouse orgWarehouse = (OrgWarehouse) event.getTargetInstance();
    warehouseAddedInCCWarehouses(orgWarehouse);
  }

  private void warehouseAddedInCCWarehouses(OrgWarehouse orgWarehouse) {
    ConnectionProvider conn = new DalConnectionProvider(false);
    String language = OBContext.getOBContext().getLanguage().getLanguage();
    final OBCriteria<OBPOS_OrgWarehouseExtra> criteria = OBDal.getInstance().createCriteria(
        OBPOS_OrgWarehouseExtra.class);
    criteria.add(Restrictions.eq(OBPOS_OrgWarehouseExtra.PROPERTY_ORGANIZATION,
        orgWarehouse.getOrganization()));
    criteria
        .add(Restrictions.eq(OBPOS_OrgWarehouseExtra.PROPERTY_CLIENT, orgWarehouse.getClient()));
    criteria.add(Restrictions.eq(OBPOS_OrgWarehouseExtra.PROPERTY_WAREHOUSE,
        orgWarehouse.getWarehouse()));
    criteria.setMaxResults(1);
    if (criteria.uniqueResult() != null) {
      logger.error("ad_org_warehouse with m_warehouse_id " + orgWarehouse.getId()
          + " is being saved and same warehouse exists in obpos_org_warehouse_extra");
      throw new OBException(Utility.messageBD(conn, "OBPOS_WarehouseExistsInCCTab", language));
    }
  }
}