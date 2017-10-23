/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone */

(function () {

  OB.UTIL.OrderSelectorUtils = {};

  OB.UTIL.OrderSelectorUtils.checkOrderAndLoad = function (model, context) {
    var orderCounter = 0,
        orderData, continueAfterPaidReceipt, loadOrder;

    continueAfterPaidReceipt = function (order) {
      order.calculateReceipt(function () {
        var loadNextOrder = function () {
            var newModel = new Backbone.Model(orderData[orderCounter]);
            orderCounter += 1;
            context.model.get('orderList').checkForDuplicateReceipts(newModel, loadOrder, continueAfterPaidReceipt);
            };
        if (order.get('searchSynchId')) {
          OB.UTIL.SynchronizationHelper.finished(order.get('searchSynchId'), 'clickSearchNewReceipt');
          order.unset('searchSynchId');
        }
        if (orderCounter === 0) {
          if (OB.MobileApp.model.get('terminal').terminalType.obsrOpenrelatedreceipts) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBSR_OpenRelatedReceipts'), null, [{
              label: OB.I18N.getLabel('OBPOS_LblOk'),
              isConfirmButton: true,
              action: function () {
                var processRelatedReceipts = new OB.DS.Process('org.openbravo.retail.scanreceipt.OpenRelatedReceipts');
                processRelatedReceipts.exec({
                  bp: order.get('bp').get('id'),
                  currentOrder: order.get('id')
                }, function (data) {
                  if (data && data.exception) {
                    OB.UTIL.showConfirmation.display('', data.exception.message);
                  } else {
                    if (data.length > 0) {
                      orderData = data;
                      loadNextOrder();
                    }
                  }
                }, function (error) {});
              }
            }, {
              label: OB.I18N.getLabel('OBMOBC_LblCancel'),
              isConfirmButton: false,
              action: function () {}
            }], {
              onHideFunction: function (popup) {},
              autoDismiss: true
            });
          }
        } else {
          if (orderCounter < orderData.length) {
            loadNextOrder();
          }
        }
      });
    };

    loadOrder = function (model) {
      var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('clickSearch'),
          process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceipts');
      OB.UTIL.showLoading(true);
      process.exec({
        orderid: model.get('id')
      }, function (data) {
        if (data && data.length === 1) {
          if (context.model.get('leftColumnViewManager').isMultiOrder()) {
            if (context.model.get('multiorders')) {
              context.model.get('multiorders').resetValues();
            }
            context.model.get('leftColumnViewManager').setOrderMode();
          }
          OB.UTIL.HookManager.executeHooks('OBRETUR_ReturnFromOrig', {
            order: data[0],
            context: context,
            params: {}
          }, function (args) {
            if (!args.cancelOperation) {
              var searchSynchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('clickSearchNewReceipt');
              context.model.get('orderList').newPaidReceipt(data[0], function (order) {
                order.set('searchSynchId', searchSynchId);
                context.model.get('orderList').addPaidReceipt(order, continueAfterPaidReceipt);
              });
            }
          });
        } else {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
        }
        OB.UTIL.SynchronizationHelper.finished(synchId, 'clickSearch');
      }, function (error) {
        OB.UTIL.showLoading(false);
      }, true, 5000);
    };

    context.model.get('orderList').checkForDuplicateReceipts(model, loadOrder);
  };

}());