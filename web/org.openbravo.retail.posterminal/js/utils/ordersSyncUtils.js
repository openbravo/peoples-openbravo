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

  OB.UTIL.processPaidOrders = function (model, orderSucessCallback) {
    // Processes the paid, unprocessed orders
    var me = this,
        criteria = {
        hasbeenpaid: 'Y'
        };
    if (OB.MobileApp.model.get('connectedToERP')) {
      OB.Dal.find(OB.Model.Order, criteria, function (ordersPaidNotProcessed) { //OB.Dal.find success
        var successCallback, errorCallback, pendingOrdersMessage;
        if (!ordersPaidNotProcessed || ordersPaidNotProcessed.length === 0) {
          if (orderSucessCallback) {
            orderSucessCallback(model);
            return;
          }
        }
        ordersPaidNotProcessed.each(function (order) {
          order.set('isbeingretriggered', 'Y');
        });
        pendingOrdersMessage = OB.UTIL.showAlert.display(OB.I18N.getLabel('OBPOS_ProcessPendingOrders'), OB.I18N.getLabel('OBPOS_Info'), null, true);
        successCallback = function () {
          pendingOrdersMessage.hide();
          if (orderSucessCallback) {
            orderSucessCallback(model);
            return;
          }
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessProcessOrder'));
        };
        errorCallback = function () {
          pendingOrdersMessage.hide();
        };
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
          // 2 options:
          // a-> timeout -> Don't remove from local DB. We are not sure if the information was or not processed. If yes we will send again the orders and the mechanism to detect duplicates will act.
          // b-> At least one error -> Remove the orders which was saved in the backend (correctly or as an error). Don't remove those which failed.
          if (data.exception && data.exception.status && data.exception.status.timeout) {
            //FLOW A
            orders.each(function (order) {
              order.set('isbeingprocessed', 'N');
              OB.Dal.save(order, null, function (tx, err) {
                OB.UTIL.showError(err);
              });
            });
          } else {
            // FLOW B
            if (data.exception && data.exception.status && data.exception.status.errorids && data.exception.status.errorids.length > 0) {
              var notProcessedOrders = data.exception.status.errorids;
              orders.each(function (order) {
                var isErrorId = _.find(notProcessedOrders, function (errId) {
                  if (order.get('id') === errId) {
                    return true;
                  }
                });
                if (isErrorId) {
                  order.set('isbeingprocessed', 'N');
                  OB.Dal.save(order, null, function (tx, err) {
                    OB.UTIL.showError(err);
                  });
                } else {
                  if (model) {
                    model.get('orderList').remove(order);
                  }
                  OB.Dal.remove(order, null, function (tx, err) {
                    OB.UTIL.showError(err);
                  });
                }
              });
            } else {
              //Others, Again flow A. Don't remove from local DB.
              orders.each(function (order) {
                order.set('isbeingprocessed', 'N');
                OB.Dal.save(order, null, function (tx, err) {
                  OB.UTIL.showError(err);
                });
              });
            }
          }
          if (errorCallback) {
            errorCallback();
          }
        } else {
          // NORMAL FLOW: Orders have been processed, delete them
          var me = this;
          me.updatedLastDocNumber = false;
          orders.each(function (order) {
            var numSequence;
            if (model) {
              model.get('orderList').remove(order);
            }
            OB.Dal.remove(order, null, function (tx, err) {
              OB.UTIL.showError(err);
            });
            // update the terminal info with the last document number sent to backoffice
            if (!order.get('isQuotation')) {
              numSequence = OB.UTIL.getNumberOfSequence(order.get('documentNo'), false);
              if (!OB.UTIL.isNullOrUndefined(numSequence) && OB.MobileApp.model.get('terminal').lastDocumentNumber < numSequence) {
                OB.MobileApp.model.get('terminal').lastDocumentNumber = numSequence;
                me.updatedLastDocNumber = true;
              }
            } else {
              numSequence = OB.UTIL.getNumberOfSequence(order.get('documentNo'), true);
              if (!OB.UTIL.isNullOrUndefined(numSequence) && OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber < numSequence) {
                OB.MobileApp.model.get('terminal').lastQuotationDocumentNumber = numSequence;
                me.updatedLastDocNumber = true;
              }
            }
          });
          // if has been updated the last doc number then terminalinfo is updated
          if (me.updatedLastDocNumber) {
            OB.POS.terminal.terminal.saveTerminalInfo();
          }
          if (successCallback) {
            successCallback();
          }
        }
      }, null, null, 7000);
    }
  };
}());