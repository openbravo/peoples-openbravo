/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.event;

import javax.enterprise.event.Observes;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.OrgWarehouse;
import org.openbravo.model.common.enterprise.Organization;

public class WarehouseOrganizationEventHandler extends EntityPersistenceEventObserver {
  private static final Entity[] entities = {
      ModelProvider.getInstance().getEntity(OrgWarehouse.ENTITY_NAME) };
  private static final String WAREHOUSEATSTORELEVEL_PREFERENCE = "OBRETCO_WarehouseAtStoreLevel";

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final OrgWarehouse orgWarehouse = (OrgWarehouse) event.getTargetInstance();
    if (isWarehouseAtStoreLevelPreferenceEnabled()
        && isStoreOrganization(orgWarehouse.getOrganization())
        && !isSameOrganization(orgWarehouse.getOrganization(),
            orgWarehouse.getWarehouse().getOrganization())) {
      throw new OBException(OBMessageUtils.messageBD(WAREHOUSEATSTORELEVEL_PREFERENCE));
    }
  }

  private boolean isWarehouseAtStoreLevelPreferenceEnabled() {
    try {
      final String nullString = null;
      return StringUtils.equals(Preferences.getPreferenceValue(WAREHOUSEATSTORELEVEL_PREFERENCE,
          true, nullString, nullString, nullString, nullString, nullString), Preferences.YES);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isStoreOrganization(Organization org) {
    return StringUtils.equals(org.getOBRETCORetailOrgType(), "S");
  }

  private boolean isSameOrganization(Organization org1, Organization org2) {
    return StringUtils.equals(org1.getId(), org2.getId());
  }
}
