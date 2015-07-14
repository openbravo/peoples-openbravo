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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.ProductServices = OB.ProductServices || {};

OB.ProductServices.onLoad = function (view) {
  var orderLinesGrid = view.theForm.getItem('grid').canvas.viewGrid;
  orderLinesGrid.selectionChanged = OB.ProductServices.relateOrderLinesSelectionChanged;
  orderLinesGrid.userSelectAllRecords = OB.ProductServices.userSelectAllRecords;
  orderLinesGrid.deselectAllRecords = OB.ProductServices.deselectAllRecords;
};

OB.ProductServices.onLoadGrid = function (grid) {
  OB.ProductServices.updateTotalLinesAmount(this.view.theForm);
  OB.ProductServices.updateServicePrice(this.view);
};

OB.ProductServices.userSelectAllRecords = function () {
  this.obaprmAllRecordsSelectedByUser = true;
  this.Super('userSelectAllRecords', arguments);
};

OB.ProductServices.deselectAllRecords = function () {
  this.obaprmAllRecordsSelectedByUser = true;
  this.Super('deselectAllRecords', arguments);
};

OB.ProductServices.updateTotalLinesAmount = function (form) {
  var totalLinesAmt = BigDecimal.prototype.ZERO,
      grid = form.getItem('grid').canvas.viewGrid,
      amountField = grid.getFieldByColumnName('amount'),
      selectedRecords = grid.getSelectedRecords(),
      totalLinesAmountlItem = form.getItem('totallinesamount'),
      i, lineAmt;

  for (i = 0; i < selectedRecords.length; i++) {
    lineAmt = new BigDecimal(String(grid.getEditedCell(grid.getRecordIndex(selectedRecords[i]), amountField)));
    totalLinesAmt = totalLinesAmt.add(lineAmt);
  }
  totalLinesAmountlItem.setValue(Number(totalLinesAmt.toString()));
  return true;
};

OB.ProductServices.orderLinesGridQtyOnChange = function (item, view, form, grid) {
  var amount, newAmount = new BigDecimal(String(item.getValue())).multiply(new BigDecimal(String(item.record.price))),
      oldAmount = grid.getEditValues(grid.getRecordIndex(item.record)).amount,
      originalQty = new BigDecimal(String(item.record.originalOrderedQuantity)),
      newQty = new BigDecimal(String(item.getValue())),
      precision = form.getItem('pricePrecision').getValue();
      newAmount = newAmount.setScale(precision, BigDecimal.prototype.ROUND_HALF_UP);
  if (!oldAmount) {
    oldAmount = new BigDecimal(String(item.record.amount));
  } else {
    oldAmount = new BigDecimal(String(grid.getEditValues(grid.getRecordIndex(item.record)).amount));
  }
  if (newAmount.compareTo(oldAmount) !== 0 && newQty.compareTo(originalQty) <= 0) {
    grid.setEditValue(grid.getRecordIndex(item.record), 'amount', Number(newAmount));
    OB.ProductServices.updateTotalLinesAmount(form);
    OB.ProductServices.updateServicePrice(view);
  }
};

OB.ProductServices.QuantityValidate = function (item, validator, value, record) {
  var quantity = null,
      recordQty = item.grid.getRecord(item.grid.getRecordIndex(record)).orderedQuantity;

  if (!isc.isA.Number(value)) {
    return false;
  }
  if (value === null || value < 0) {
    return false;
  }
  quantity = new BigDecimal(String(value));
  recordQty = new BigDecimal(String(record.originalOrderedQuantity));
  if (quantity.compareTo(recordQty) > 0) {
    isc.warn(OB.I18N.getLabel('ServiceQuantityMoreThanOrdered', [quantity, recordQty]));
    return false;
  }
  return true;
};

OB.ProductServices.relateOrderLinesSelectionChanged = function (record, state) {
  this.fireOnPause('selectionChanged' + record.id, function () {
    OB.ProductServices.doRelateOrderLinesSelectionChanged(record, state, this.view);
  }, 200);
  this.Super('selectionChanged', arguments);
};

OB.ProductServices.doRelateOrderLinesSelectionChanged = function (record, state, view) {
  var totalLinesAmount = view.theForm.getItem('totallinesamount'),
      totalLinesAmountValue = new BigDecimal(String(view.theForm.getItem('totallinesamount').getValue() || 0)),
      orderLinesGrid = view.theForm.getItem('grid').canvas.viewGrid,
      totalServiceAmount = view.theForm.getItem('totalserviceamount');

  if (!orderLinesGrid.obaprmAllRecordsSelectedByUser || (orderLinesGrid.obaprmAllRecordsSelectedByUser && (orderLinesGrid.getRecordIndex(record) === orderLinesGrid.getTotalRows() - 1))) {

    if (orderLinesGrid.obaprmAllRecordsSelectedByUser) {
      OB.ProductServices.updateTotalLinesAmount(view.theForm);
      delete orderLinesGrid.obaprmAllRecordsSelectedByUser;
    } else {
      if (state) {
        totalLinesAmountValue = totalLinesAmountValue.add(new BigDecimal(String(record.amount || 0)));
      } else {
        totalLinesAmountValue = totalLinesAmountValue.subtract(new BigDecimal(String(record.amount || 0)));
      }
      totalLinesAmount.setValue(Number(totalLinesAmountValue.toString()));
    }
    totalLinesAmountValue = new BigDecimal(String(view.theForm.getItem('totallinesamount').getValue() || 0));
    if (totalLinesAmountValue.compareTo(BigDecimal.prototype.ZERO) !== 0) {
      OB.ProductServices.updateServicePrice(view);
    } else {
      totalServiceAmount.setValue(Number("0"));
    }
  }
};

OB.ProductServices.updateServicePrice = function (view) {
  var callback, totalServiceAmount = view.theForm.getItem('totalserviceamount'),
      orderLinesGrid = view.theForm.getItem('grid').canvas.viewGrid;

  callback = function (response, data, request) {
    if (data.amount || data.amount === 0) {
      totalServiceAmount.setValue(Number(data.amount));
    } else {
      orderLinesGrid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, data.message.title, data.message.text);
      totalServiceAmount.setValue(Number("0"));
    }
  };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    orderlineId: view.theForm.getItem('orderlineId').getValue(),
    amount: view.theForm.getItem('totallinesamount').getValue()
  }, {}, callback);
}