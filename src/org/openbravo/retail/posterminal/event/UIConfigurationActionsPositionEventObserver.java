/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.event;

import java.util.List;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.mobile.core.MobileUiConfigurationWindowAbaAction;

/**
 * Checks that actions created by a user for an specific ABA are not in conflict with other actions
 * and they are inside ABA ranges
 */
public class UIConfigurationActionsPositionEventObserver extends EntityPersistenceEventObserver {
  private static final String OKMSG = "[[OK]]";
  private Long maxRows;
  private Long maxColumns;
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(MobileUiConfigurationWindowAbaAction.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    validateActionPosition(event);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    validateActionPosition(event);
  }

  /**
   * Main method to check that actions are not in conflict
   */
  private void validateActionPosition(EntityPersistenceEvent event) {
    MobileUiConfigurationWindowAbaAction targetActionInABA = (MobileUiConfigurationWindowAbaAction) event
        .getTargetInstance();
    this.maxRows = targetActionInABA.getABAUiConfiguration().getActionButtonArea().getMaxRows();
    this.maxColumns = targetActionInABA.getABAUiConfiguration()
        .getActionButtonArea()
        .getMaxColumns();

    String validationMsg = validateActionPositionWithoutCheckOthers(event, targetActionInABA);
    if (!validationMsg.equals(OKMSG)) {
      throw new OBException(validationMsg, false);
    }
    final List<MobileUiConfigurationWindowAbaAction> currentConfiguredActionsInABA = getCurrentConfiguredActionsInABA(
        event, targetActionInABA);
    if (!currentConfiguredActionsInABA.isEmpty()) {
      validationMsg = validateActionPositionCheckingOthers(event, targetActionInABA,
          currentConfiguredActionsInABA);
      if (!validationMsg.equals(OKMSG)) {
        throw new OBException(validationMsg, false);
      }
    }
  }

  /**
   * Validate basic configurations of the action without check others. If this validations are not
   * passed then next validations will not be executed
   */
  private String validateActionPositionWithoutCheckOthers(EntityPersistenceEvent event,
      MobileUiConfigurationWindowAbaAction targetActionInABA) {
    if (targetActionInABA.getRowposition() >= 0
        && targetActionInABA.getRowposition() < this.maxRows) {
      if (targetActionInABA.getColumnposition() >= 0
          && targetActionInABA.getColumnposition() < this.maxColumns) {
        if (targetActionInABA.getRowspan() >= 1 && targetActionInABA.getRowspan() <= this.maxRows) {
          if (targetActionInABA.getColspan() >= 1
              && targetActionInABA.getColspan() <= this.maxColumns) {
            if (targetActionInABA.getRowposition()
                + targetActionInABA.getRowspan() <= this.maxRows) {
              if (targetActionInABA.getRowposition()
                  + targetActionInABA.getRowspan() <= this.maxRows) {
                return OKMSG;
              } else {
                Long total = targetActionInABA.getColumnposition() + targetActionInABA.getColspan();
                return getErrorMessage(
                    "OBPOS_UIConfig_ColPositionAndColSpanCombinationExceedsABAColLimits",
                    total.toString(), this.maxColumns.toString());
              }
            } else {
              Long total = targetActionInABA.getRowposition() + targetActionInABA.getRowspan();
              return getErrorMessage(
                  "OBPOS_UIConfig_RowPositionAndRowSpanCombinationExceedsABARowLimits",
                  total.toString(), this.maxRows.toString());
            }
          } else {
            return getErrorMessage("OBPOS_UIConfig_ColSpanExceedsABAColLimits",
                targetActionInABA.getColumnposition().toString(), this.maxColumns.toString());
          }
        } else {
          return getErrorMessage("OBPOS_UIConfig_RowSpanExceedsABARowLimits",
              targetActionInABA.getRowposition().toString(), this.maxRows.toString());
        }
      } else {
        return getErrorMessage("OBPOS_UIConfig_ColMustBeBetweenColLimits",
            targetActionInABA.getColumnposition().toString(), this.maxColumns.toString());
      }
    } else {
      return this.getErrorMessage("OBPOS_UIConfig_RowMustBeBetweenRowLimits",
          targetActionInABA.getRowposition().toString(), this.maxRows.toString());
    }
  }

  /**
   * Validate that changed/created action is compatible with the position provided for the rest of
   * the actions presents in the area
   */
  private String validateActionPositionCheckingOthers(EntityPersistenceEvent event,
      MobileUiConfigurationWindowAbaAction targetActionInABA,
      List<MobileUiConfigurationWindowAbaAction> currentConfiguredActionsInABA) {
    int[][] abaGrid = this.buildGrid(currentConfiguredActionsInABA);
    int positionToPlace = abaGrid[targetActionInABA.getRowposition().intValue()][targetActionInABA
        .getColumnposition()
        .intValue()];
    if (positionToPlace == 0) {
      if (targetActionInABA.getRowspan() > 1) {
        for (int i = 1; i < targetActionInABA.getRowspan(); i++) {
          int rowPositionToCheck = targetActionInABA.getRowposition().intValue() + i;
          if (abaGrid[rowPositionToCheck][targetActionInABA.getColumnposition().intValue()] != 0) {
            return getErrorMessage("OBPOS_UIConfig_PositionConflict",
                String.valueOf(rowPositionToCheck),
                targetActionInABA.getColumnposition().toString());
          }
        }
      }
      if (targetActionInABA.getColspan() > 1) {
        for (int i = 1; i < targetActionInABA.getColspan(); i++) {
          int colPositionToCheck = targetActionInABA.getColumnposition().intValue() + i;
          if (abaGrid[targetActionInABA.getRowposition().intValue()][colPositionToCheck] != 0) {
            return getErrorMessage("OBPOS_UIConfig_PositionConflict",
                targetActionInABA.getRowposition().toString(), String.valueOf(colPositionToCheck));
          }
        }
      }
    } else {
      return getErrorMessage("OBPOS_UIConfig_PositionConflict",
          targetActionInABA.getRowposition().toString(),
          targetActionInABA.getColumnposition().toString());
    }
    return OKMSG;
  }

  /**
   * Generate a grid with positions which are being used (1) and free ones (0)
   */
  private int[][] buildGrid(
      List<MobileUiConfigurationWindowAbaAction> currentConfiguredActionsInABA) {
    int[][] abaGrid = new int[this.maxRows.intValue()][this.maxColumns.intValue()];
    for (MobileUiConfigurationWindowAbaAction configAction : currentConfiguredActionsInABA) {
      abaGrid[configAction.getRowposition().intValue()][configAction.getColumnposition()
          .intValue()] = 1;
      if (configAction.getRowspan() > 1) {
        for (int i = 1; i < configAction.getRowspan(); i++) {
          abaGrid[configAction.getRowposition().intValue() + i][configAction.getColumnposition()
              .intValue()] = 1;
        }
      }
      if (configAction.getColspan() > 1) {
        for (int i = 1; i < configAction.getColspan(); i++) {
          abaGrid[configAction.getRowposition().intValue()][configAction.getColumnposition()
              .intValue() + i] = 1;
        }
      }
    }
    return abaGrid;
  }

  /**
   * Retrieve actions of current area excluding the one which is being updated
   */
  private List<MobileUiConfigurationWindowAbaAction> getCurrentConfiguredActionsInABA(
      EntityPersistenceEvent event, MobileUiConfigurationWindowAbaAction targetActionInABA) {

    final StringBuilder whereClause = new StringBuilder();
    whereClause.append(" as act_in_aba ");
    whereClause.append(" where act_in_aba.aBAUiConfiguration.id = :curABAId ");

    if (event instanceof EntityUpdateEvent) {
      whereClause.append(" and act_in_aba.id <> :curActionId");
    }

    OBQuery<MobileUiConfigurationWindowAbaAction> query = OBDal.getInstance()
        .createQuery(MobileUiConfigurationWindowAbaAction.class, whereClause.toString());
    query.setNamedParameter("curABAId", targetActionInABA.getABAUiConfiguration().getId());
    query.setNamedParameter("curActionId", targetActionInABA.getId());

    return query.list();
  }

  /**
   * Utility to generate error message
   */
  private String getErrorMessage(String errorMsgKey, String msgParam1, String msgParam2) {
    String message = OBMessageUtils.messageBD(errorMsgKey);
    String formattedMessage = String.format(message, msgParam1, msgParam2);
    return formattedMessage;
  }
}
