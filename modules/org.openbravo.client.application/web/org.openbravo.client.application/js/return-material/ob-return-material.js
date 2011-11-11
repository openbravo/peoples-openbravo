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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.RM = OB.RM || {};

/**
 * Check that entered return quantity is less than original inout qty.
 */
OB.RM.RMOrderQtyValidate = function (item, validator, value, record) {
  return (value !== null) && (value <= record.movementQuantity) && (value > 0);
};

/**
 * Check that entered received quantity is less than pending qty.
 */
OB.RM.RMReceiptQtyValidate = function (item, validator, value, record) {
  return (value !== null) && (value <= record.pending) && (value > 0);
};

/**
 * Set quantity, storage bin and condition of the goods.
 */
OB.RM.RMReceiptSelectionChange = function(grid, record, state) {
  var contextInfo = null;
  if (state) {
    record.receiving = record.pending;
    contextInfo = grid.view.parentWindow.activeView.getContextInfo(false, true, false, true)
    record.storageBin = contextInfo.ReturnLocator; 
    record.conditionGoods = contextInfo.inpmConditionGoodsId;
  }
}

/**
 * Check that entered shipped quantity is less than pending qty.
 */
OB.RM.RMShipmentQtyValidate = function (item, validator, value, record) {
  var orderLine = record.orderLine,
      pendingQty = record.pending,
      selectedRecords = item.grid.getSelectedRecords(),
      selectedRecordsLength = selectedRecords.length,
      editedRecord = null,
      i;
  // check value is positive and below available qty and pending qty
  if (value === null || value < 0 || value > record.pending || value > record.availableQty) {
    return false;
  }
  // check shipped total quantity for the order line is below pending qty.
  for (i = 0; i < selectedRecordsLength; i++) {
    editedRecord = isc.addProperties({}, selectedRecords[i], item.grid.getEditedRecord(selectedRecords[i]));
    if (editedRecord.orderLine === orderLine) {
      pendingQty -= editedRecord.movementQuantity;
      if (pendingQty < 0) {
        return false;
      }
    }
  }
  return true;
};