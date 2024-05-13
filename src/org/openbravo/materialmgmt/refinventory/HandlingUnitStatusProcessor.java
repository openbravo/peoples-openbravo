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

import javax.enterprise.context.ApplicationScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;

/**
 * In charge of updating the status of a handling unit
 */
@ApplicationScoped
public class HandlingUnitStatusProcessor {
  private static final Logger log = LogManager.getLogger();

  public enum HandlingUnitStatus {
    OPEN, CLOSED, DESTROYED
  }

  /**
   * Sets the status of a handling unit and to its child handling units in cascade
   * 
   * @param handlingUnit
   *          the handling unit
   * @param status
   *          the new status to be set
   * @throws OBException
   *           if the handling unit is destroyed or if any of the parents of the handling unit is
   *           closed.
   */
  public void changeHandlingUnitStatus(ReferencedInventory handlingUnit,
      HandlingUnitStatus status) {
    checkIsDestroyed(handlingUnit);
    checkIsAnyParentClosed(handlingUnit);
    changeStatusInCascade(handlingUnit, status);
  }

  private void changeStatusInCascade(ReferencedInventory handlingUnit, HandlingUnitStatus status) {
    if (isInStatus(handlingUnit, status)) {
      log.warn("Skipping status change. The current status of the handling unit {} is already {}",
          handlingUnit.getSearchKey(), status);
      return;
    }
    handlingUnit.setStatus(status.name());
    ReferencedInventoryUtil.getDirectChildReferencedInventories(handlingUnit)
        .filter(this::isNotDestroyed)
        .forEach(child -> changeStatusInCascade(child, status));
  }

  private void checkIsDestroyed(ReferencedInventory handlingUnit) {
    if (isDestroyed(handlingUnit)) {
      log.error("Cannot change the status of the handling unit {} because it is destroyed",
          handlingUnit.getSearchKey());
      throw new OBException("Cannot change the status of a destroyed handling unit");
    }
  }

  private void checkIsAnyParentClosed(ReferencedInventory handlingUnit) {
    ReferencedInventoryUtil.findFirstParent(handlingUnit, this::isClosed).ifPresent(hu -> {
      throw new OBException(
          "Cannot change the status of the handling unit " + handlingUnit.getSearchKey()
              + " because its parent handling unit " + hu.getSearchKey() + " is closed");
    });
  }

  private boolean isClosed(ReferencedInventory handlingUnit) {
    return isInStatus(handlingUnit, HandlingUnitStatus.CLOSED);
  }

  private boolean isDestroyed(ReferencedInventory handlingUnit) {
    return isInStatus(handlingUnit, HandlingUnitStatus.DESTROYED);
  }

  private boolean isNotDestroyed(ReferencedInventory handlingUnit) {
    return !isDestroyed(handlingUnit);
  }

  private boolean isInStatus(ReferencedInventory handlingUnit, HandlingUnitStatus newStatus) {
    return newStatus.name().equals(handlingUnit.getStatus());
  }
}
