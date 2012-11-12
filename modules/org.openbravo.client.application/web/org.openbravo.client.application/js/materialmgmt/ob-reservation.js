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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.Reservation = OB.Reservation || {};

/**
 * Check that entered quantity to reserve is available in the selected record
 * and that total reserved quantity is below the needed quantity
 */
OB.Reservation.QuantityValidate = function(item, validator, value, record) {
  var availableQty = isc.isA.Number(record.availableQty) ? new BigDecimal(String(record.availableQty)) : BigDecimal.prototype.ZERO,
      releasedQty = isc.isA.Number(record.released) ? new BigDecimal(String(record.released)) : BigDecimal.prototype.ZERO,
      quantity = null,
      reservedQty = BigDecimal.prototype.ZERO,
      totalQty = new BigDecimal(String(item.grid.view.parentWindow.activeView.getContextInfo().inpquantity)),
      totalReleased = isc.isA.Number(item.grid.view.parentWindow.activeView.getContextInfo().inpreleasedqty) ? new BigDecimal(String(item.grid.view.parentWindow.activeView.getContextInfo().inpreleasedqty)) : BigDecimal.prototype.ZERO,
      selectedRecords = item.grid.getSelectedRecords(),
      selectedRecordsLength = selectedRecords.length,
      editedRecord = null,
      i;

  if (!isc.isA.Number(value)) {
    return false;
  }
  if (value === null || value < 0) {
    return false;
  }
  quantity = new BigDecimal(String(value));
  if (quantity.compareTo(availableQty) > 0) {
    return false;
  }
  if (quantity.compareTo(releasedQty) < 0) {
    return false;
  }
  for (i = 0; i < selectedRecordsLength; i++) {
    editedRecord = isc.addProperties({}, selectedRecords[i], item.grid.getEditedRecord(selectedRecords[i]));
    if (isc.isA.Number(editedRecord.quantity)) {
      reservedQty = reservedQty.add(new BigDecimal(String(editedRecord.quantity)));
    }
  }
  if (reservedQty.compareTo(totalQty) > 0) {
    return false;
  }
  if (reservedQty.compareTo(releasedQty) < 0) {
    return false;
  }
  // get reservation quantity and released quantity to check totals
  return true;
};