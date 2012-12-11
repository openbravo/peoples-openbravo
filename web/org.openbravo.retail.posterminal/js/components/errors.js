/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function () {

  OB.OBPOS = {};

  OB.OBPOS.Errors = {};

  OB.OBPOS.Errors.saveOrder = function (params, view) {
    var selectedRecords = view.view.viewGrid.getSelectedRecords();
    var i, requestOrderParams, requestBPParams;
    var orderIds = [];
    var customerIds = [];
    for (i = 0; i < selectedRecords.length; i++) {
      if (selectedRecords[i].typeofdata) {
        if (selectedRecords[i].id && selectedRecords[i].typeofdata === 'BP') {
          customerIds.push(selectedRecords[i].id);
        } else if (selectedRecords[i].id && selectedRecords[i].typeofdata === 'order') {
          orderIds.push(selectedRecords[i].id);
        }
      } else {
        orderIds.push(selectedRecords[i].id);
      }
    }

    var callbackOrders = function (response, data, request) {
        isc.say(data.message);
        params.button.closeProcessPopup();
        };

    var callbackBP = function (response, data, request) {
        isc.say(data.message);
        if (orderIds.length > 0) {
          requestOrderParams = {
            recordIds: orderIds
          };
          OB.RemoteCallManager.call('org.openbravo.retail.posterminal.SaveOrderActionHandler', orderIds, requestOrderParams, callbackOrders);
        } else {
          params.button.closeProcessPopup();
        }
        };

    if (customerIds.length > 0) {
      requestBPParams = {
        recordIds: customerIds
      };
      //callback will process orders
      OB.RemoteCallManager.call('org.openbravo.retail.posterminal.SaveCustomerActionHandler', customerIds, requestBPParams, callbackBP);
    } else if (orderIds.length > 0) {
      requestOrderParams = {
        recordIds: orderIds
      };
      OB.RemoteCallManager.call('org.openbravo.retail.posterminal.SaveOrderActionHandler', orderIds, requestOrderParams, callbackOrders);
    }
  };
  OB.OBPOS.Errors.clearError = function (params, view) {
    var selectedRecords = view.view.viewGrid.getSelectedRecords();
    var theParams = params;
    var i, requestParams;
    var ids = [];

    var callback = function (response, data, request) {
        isc.say(data.message);
        params.button.closeProcessPopup();
        };

    for (i = 0; i < selectedRecords.length; i++) {
      ids.push(selectedRecords[i].id);
    }

    requestParams = {
      recordIds: ids
    };
    isc.confirm(OB.I18N.getLabel('OBPOS_ClearError'), function (value) {
      if (value !== null && value) {
        OB.RemoteCallManager.call('org.openbravo.retail.posterminal.ClearErrorActionHandler', ids, requestParams, callback);
      }
    });
  };

}());