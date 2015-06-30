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
};

OB.ProductServices.relateOrderLinesSelectionChanged = function (record, state) {
  this.fireOnPause('selectionChanged' + record.id, function () {
    OB.ProductServices.doRelateOrderLinesSelectionChanged(record, state, this.view);
  }, 200);
  this.Super('selectionChanged', arguments);
};

OB.ProductServices.doRelateOrderLinesSelectionChanged = function (record, state, view) {
  var callback, orderLinesGrid = view.theForm.getItem('grid').canvas.viewGrid,
      serviceProductId = view.theForm.getItem('serviceProductId').getValue();

  callback = function (response, data, request) {
    if (data.amount) {
      orderLinesGrid.setEditValue(orderLinesGrid.getRecordIndex(record), 'amount', Number(data.amount));
    } else {
      orderLinesGrid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, data.message.title, data.message.text);
    }
  }

  if (state) {
    OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
      record: record,
      serviceProductId: serviceProductId
    }, {}, callback);
  } else {
    orderLinesGrid.setEditValue(orderLinesGrid.getRecordIndex(record), 'amount', null);
  }
};