/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.apache.log4j.Logger;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.retail.posterminal.OrderLoaderPreAddShipmentLineHook.OrderLoaderPreAddShipmentLineHook_Actions;

public class OrderLoaderPreAddShipmentLineHook_Response {
  private static final Logger log = Logger
      .getLogger(OrderLoaderPreAddShipmentLineHook_Response.class);

  private boolean isValid;
  private boolean cancelExecution;
  private OrderLoaderPreAddShipmentLineHook_Actions action;
  private String msg;
  private Locator newLocator;

  public Locator getNewLocator() {
    return newLocator;
  }

  public void setNewLocator(Locator _newLocator) {
    if (this.isValid()
        && !(this.getAction()
            .equals(OrderLoaderPreAddShipmentLineHook_Actions.ACTION_STANDARD_SALE))) {
      this.newLocator = _newLocator;
    } else {
      this.newLocator = null;
      log.warn("New Locator only will be taken into account when response is valid and action is not "
          + OrderLoaderPreAddShipmentLineHook
              .getActionString(OrderLoaderPreAddShipmentLineHook_Actions.ACTION_STANDARD_SALE));
    }
  }

  public boolean isValid() {
    return isValid;
  }

  public void setValid(boolean _isValid) {
    this.isValid = _isValid;
  }

  public boolean isCancelExecution() {
    return cancelExecution;
  }

  public void setCancelExecution(boolean _cancelExecution) {
    if (this.action.equals(OrderLoaderPreAddShipmentLineHook_Actions.ACTION_STANDARD_SALE)) {
      this.cancelExecution = _cancelExecution;
    } else {
      this.cancelExecution = true;
      if (!_cancelExecution) {
        log.warn("Cancel Execution MUST BE true except for action "
            + OrderLoaderPreAddShipmentLineHook
                .getActionString(OrderLoaderPreAddShipmentLineHook_Actions.ACTION_STANDARD_SALE));
      }
    }
  }

  public OrderLoaderPreAddShipmentLineHook_Actions getAction() {
    return this.action;
  }

  public void setAction(OrderLoaderPreAddShipmentLineHook_Actions _action) {
    this.action = _action;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getActionString() {
    return OrderLoaderPreAddShipmentLineHook.getActionString(this.getAction());
  }

  public OrderLoaderPreAddShipmentLineHook_Response(
      OrderLoaderPreAddShipmentLineHook_Actions _action) {
    this.setAction(_action);
    switch (this.action) {
    case ACTION_SINGLEBIN:
    case ACTION_LAST_ATTEMPT:
    case ACTION_RETURN:
      this.setCancelExecution(true);
      break;
    case ACTION_STANDARD_SALE:
      this.setCancelExecution(false);
      break;
    default:
      this.setCancelExecution(true);
    }
    this.setMsg("This is a generic error message comming from the execution of -OrderLoaderPreAddShipmentLineHook- Hook");
  }
}