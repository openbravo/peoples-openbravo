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
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.PROC = OB.PROC || {};

/**
 * Modify base Quantity when the Aum Quantity changes
 */

OB.PROC.CreateLinesOnChangeQuantityAum = function (item, view, form, grid) {
  var aumQty = isc.isA.Number(item.getValue()) ? new BigDecimal(item.getValue()) : 0;
  var record = grid.getSelectionObject().lastSelectionItem;
  var aum = grid.getEditValues(grid.getRecordIndex(item.record)).aum;

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.CreateLinesFromPOConvertAUM', {
    productId: record.product,
    quantity: aumQty,
    toUOM: aum,
    reverse: false
  }, {}, function (response, data, request) {
    if (data.amount) {
      grid.setEditValue(item.grid.getEditRow(), 'orderedQuantity', data.amount);
    }
  });
};

/**
 * Modify base Aum Quantity when the base Quantity changes
 */

OB.PROC.CreateLinesOnChangeQuantity = function (item, view, form, grid) {
  var qty = isc.isA.Number(item.getValue()) ? new BigDecimal(item.getValue()) : 0;
  var record = grid.getSelectionObject().lastSelectionItem;
  var aum = grid.getEditValues(grid.getRecordIndex(item.record)).aum;

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.CreateLinesFromPOConvertAUM', {
    productId: record.product,
    quantity: qty,
    toUOM: aum,
    reverse: true
  }, {}, function (response, data, request) {
    if (data.amount) {
      grid.setEditValue(item.grid.getEditRow(), 'aumQuantity', data.amount);
    }
  });
};

/**
 * Modify base Quantity when the Aum selected changes
 */

OB.PROC.CreateLinesOnChangeAum = function (item, validator, value, record) {
  var aum = item.getValue();
  var changed_record = item.grid.getSelectionObject().lastSelectionItem;
  var aumQty = item.grid.getEditValues(item.grid.getRecordIndex(item.record)).aumQuantity;

  if (aumQty !== undefined) {
    OB.RemoteCallManager.call('org.openbravo.common.actionhandler.CreateLinesFromPOConvertAUM', {
      productId: changed_record.product,
      quantity: aumQty,
      toUOM: aum,
      reverse: false
    }, {}, function (response, data, request) {
      if (data.amount) {
        item.grid.setEditValue(item.grid.getEditRow(), 'orderedQuantity', data.amount);
      }
    });
  }
};