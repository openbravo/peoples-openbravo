/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,_*/

(function () {

  OB = window.OB || {};
  OB.UTILS = window.OB.UTILS || {};

  OB.UTIL.processPaidOrders = function (model) {
    // Processes the paid, unprocessed orders
    var me = this,
        criteria = {
        hasbeenpaid: 'Y'
        };
    if (OB.MobileApp.model.get('connectedToERP')) {
      OB.Dal.find(OB.Model.Order, criteria, function (ordersPaidNotProcessed) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!ordersPaidNotProcessed || ordersPaidNotProcessed.length === 0) {
          return;
        }
        ordersPaidNotProcessed.each(function (order) {
          order.set('isbeingretriggered', 'Y');
        });
        successCallback = function () {
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessProcessOrder'));
        };
        errorCallback = function () {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorProcessOrder'));
        };
        OB.UTIL.showAlert.display(OB.I18N.getLabel('OBPOS_ProcessPendingOrders'), OB.I18N.getLabel('OBPOS_Info'));
        OB.UTIL.processOrders(model, ordersPaidNotProcessed, successCallback, errorCallback);
      });
    }
  };

  OB.UTIL.processOrderClass = 'org.openbravo.retail.posterminal.OrderLoader';

  OB.UTIL.processOrders = function (model, orders, successCallback, errorCallback) {
    var ordersToJson = [];
    orders.each(function (order) {
      ordersToJson.push(order.serializeToJSON());
    });
    this.proc = new OB.DS.Process(OB.UTIL.processOrderClass);
    if (OB.MobileApp.model.get('connectedToERP')) {
      this.proc.exec({
        order: ordersToJson
      }, function (data, message) {
        if (data && data.exception) {
          // Orders have not been processed
          orders.each(function (order) {
            order.set('isbeingprocessed', 'N');
            OB.Dal.save(order, null, function (tx, err) {
              OB.UTIL.showError(err);
            });
          });
          if (errorCallback) {
            errorCallback();
          }
        } else {
          // Orders have been processed, delete them
          orders.each(function (order) {
            if (model) {
              model.get('orderList').remove(order);
            }
            OB.Dal.remove(order, null, function (tx, err) {
              OB.UTIL.showError(err);
            });
          });
          if (successCallback) {
            successCallback();
          }
        }
      }, null, null, 4000);
    }
  };
}());