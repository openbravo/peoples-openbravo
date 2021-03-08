/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Add Product action
 */

OB.App.StateAPI.Ticket.registerUtilityFunctions({
  /**
   * Throws error in case ticket is not editable.
   */
  checkIsEditable(ticket) {
    if (ticket.isEditable === false) {
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_modalNoEditableHeader',
        errorConfirmation: 'OBPOS_modalNoEditableBody'
      });
    }
  }
});
