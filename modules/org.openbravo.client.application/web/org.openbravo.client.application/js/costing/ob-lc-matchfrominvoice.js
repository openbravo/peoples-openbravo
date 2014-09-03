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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.Costing = OB.Costing || {};

OB.Costing.MatchFromInvoiceAmtValidation = function (item, validator, value, record) {
  var selectedRecords = item.grid.getSelectedRecords(),
      selectedRecordsLength = selectedRecords.length,
      invoiceamt = BigDecimal.prototype.ZERO,
      matchedamt = BigDecimal.prototype.ZERO,
      editedRecord, i;
  
  if(!isc.isA.Number(value)) {
    item.grid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('APRM_NotValidNumber'));
    return false;
  }
  // Check that matched amount is not higher than invoice line net amount
  for (i = 0; i < selectedRecordsLength; i++) {
    editedRecord = isc.addProperties({}, selectedRecords[i], item.grid.getEditedRecord(selectedRecords[i]));
    matchedamt = matchedamt.add(new BigDecimal(String(editedRecord.matchedAmt)));
  }


  return true;
};