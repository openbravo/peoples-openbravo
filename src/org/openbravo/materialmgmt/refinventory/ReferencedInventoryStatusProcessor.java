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
package org.openbravo.materialmgmt.refinventory;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.synchronization.event.SynchronizationEvent;

/**
 * In charge of updating the status of a handling unit and triggering the
 * API_HandlingUnitStatusChange push API event.
 */
@ApplicationScoped
public class ReferencedInventoryStatusProcessor {
  private static final Logger log = LogManager.getLogger();

  public enum ReferencedInventoryStatus {
    OPEN, CLOSED, DESTROYED;

    private boolean isStatusOf(ReferencedInventory handlingUnit) {
      return name().equals(handlingUnit.getStatus());
    }

    private static boolean isClosed(ReferencedInventory handlingUnit) {
      return CLOSED.isStatusOf(handlingUnit);
    }

    private static boolean isNotDestroyed(ReferencedInventory handlingUnit) {
      return !isDestroyed(handlingUnit);
    }

    private static boolean isDestroyed(ReferencedInventory handlingUnit) {
      return DESTROYED.isStatusOf(handlingUnit);
    }
  }

  /**
   * Sets the status of a handling unit and to its child handling units in cascade
   * 
   * @param handlingUnit
   *          the handling unit
   * @param newStatus
   *          the new status to be set
   * @throws OBException
   *           if the handling unit is destroyed or if the parent of the handling unit is closed
   */
  public void changeStatus(ReferencedInventory handlingUnit, ReferencedInventoryStatus newStatus) {
    if (newStatus.isStatusOf(handlingUnit)) {
      log.warn("Skipping status change. The current status of the handling unit {} is already {}",
          handlingUnit.getSearchKey(), newStatus);
      return;
    }
    checkIsDestroyed(handlingUnit);
    checkIsAnyAncestorClosed(handlingUnit);
    changeStatusInCascade(handlingUnit, newStatus);
    triggerHandlingUnitStatusChangeEvent(handlingUnit, newStatus);
  }

  private void checkIsDestroyed(ReferencedInventory handlingUnit) {
    if (ReferencedInventoryStatus.isDestroyed(handlingUnit)) {
      log.error("Cannot change the status of the handling unit {} because it is destroyed",
          handlingUnit.getSearchKey());
      throw new OBException(OBMessageUtils.getI18NMessage("HandlingUnitIsDestroyed"));
    }
  }

  private void checkIsAnyAncestorClosed(ReferencedInventory handlingUnit) {
    findClosedAncestor(handlingUnit).ifPresent(ancestor -> {
      throw new OBException(OBMessageUtils.getI18NMessage("ParentHandlingUnitIsClosed",
          new String[] { handlingUnit.getSearchKey(), ancestor.getSearchKey() }));
    });
  }

  private Optional<ReferencedInventory> findClosedAncestor(ReferencedInventory handlingUnit) {
    ReferencedInventory parent = handlingUnit.getParentRefInventory();
    if (parent == null) {
      return Optional.empty();
    }
    if (ReferencedInventoryStatus.isClosed(parent)) {
      return Optional.of(parent);
    } else {
      return findClosedAncestor(parent);
    }
  }

  private void changeStatusInCascade(ReferencedInventory handlingUnit,
      ReferencedInventoryStatus status) {
    handlingUnit.setStatus(status.name());
    if (status.equals(ReferencedInventoryStatus.CLOSED)) {
      ReferencedInventoryUtil.getDirectChildReferencedInventories(handlingUnit)
          .filter(ReferencedInventoryStatus::isNotDestroyed)
          .forEach(child -> changeStatusInCascade(child, status));
    }
  }

  /**
   * Triggers a handling unit status change event based on the new status of the handling unit.
   * <p>
   * This method manages specific events related to handling unit status changes. Depending on the
   * {@link ReferencedInventoryStatus} provided, it triggers one of the predefined events for the
   * "CLOSED" or "DESTROYED" statuses. Additionally, it always triggers a general status change
   * event.
   * </p>
   * <p>
   * Note: Although this implementation creates a dependency with the Business API module due to the
   * hardcoded event names (e.g., "API_HandlingUnitStatusToClosed"), it avoids the complexity of
   * introducing an abstraction layer with hooks to be implemented by external modules.
   * </p>
   *
   * @param handlingUnit
   *          the {@link ReferencedInventory} instance representing the handling unit whose status
   *          is changing
   * @param newStatus
   *          the {@link ReferencedInventoryStatus} representing the new status of the handling unit
   */
  private void triggerHandlingUnitStatusChangeEvent(ReferencedInventory handlingUnit,
      ReferencedInventoryStatus newStatus) {
    switch (newStatus) {
      case CLOSED: {
        SynchronizationEvent.getInstance()
            .triggerEvent("API_HandlingUnitStatusToClosed", handlingUnit.getId());
        break;
      }
      case DESTROYED: {
        SynchronizationEvent.getInstance()
            .triggerEvent("API_HandlingUnitStatusToDestroyed", handlingUnit.getId());
        break;
      }
      default:
        break;
    }
    SynchronizationEvent.getInstance()
        .triggerEvent("API_HandlingUnitStatusChange", handlingUnit.getId());
  }
}
