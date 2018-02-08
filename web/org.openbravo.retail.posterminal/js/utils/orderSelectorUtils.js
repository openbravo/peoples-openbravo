/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, Backbone */

(function () {

  OB.UTIL.OrderSelectorUtils = {};

  OB.UTIL.OrderSelectorUtils.addToListOfReceipts = function (receipt) {
    if (OB.UTIL.isNullOrUndefined(this.listOfReceipts)) {
      this.listOfReceipts = [];
    }
    if (!OB.UTIL.isNullOrUndefined(receipt)) {
      this.listOfReceipts.push(receipt);
    }
  };

  OB.UTIL.OrderSelectorUtils.checkOrderAndLoad = function (model, orderList, context, originServer, fromSelector) {
    var me = this,
        continueAfterPaidReceipt, checkListCallback, loadOrder;

    checkListCallback = function () {
      if (me.listOfReceipts && me.listOfReceipts.length > 0) {
        var currentReceipt = me.listOfReceipts.shift();
        orderList.checkForDuplicateReceipts(currentReceipt, loadOrder, checkListCallback);
      } else {
        me.loadingReceipt = false;
      }
    };

    continueAfterPaidReceipt = function (order) {
      var loadNextOrder;

      loadNextOrder = function () {
        if (order.get('searchSynchId')) {
          OB.UTIL.SynchronizationHelper.finished(order.get('searchSynchId'), 'clickSearchNewReceipt');
          order.unset('searchSynchId');
        }
        if (order.get('askForRelatedReceipts') && OB.MobileApp.model.get('terminal').terminalType.openrelatedreceipts && order.get('bp').get('id') !== OB.MobileApp.model.get('terminal').businessPartner) {
          order.unset('askForRelatedReceipts');
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_OpenRelatedReceiptsTitle'), OB.I18N.getLabel('OBPOS_OpenRelatedReceiptsBody'), [{
            label: OB.I18N.getLabel('OBPOS_LblOk'),
            isConfirmButton: true,
            action: function () {
              var processRelatedReceipts = new OB.DS.Process('org.openbravo.retail.posterminal.process.OpenRelatedReceipts');
              processRelatedReceipts.exec({
                bp: order.get('bp').get('id'),
                currentOrder: order.get('id')
              }, function (data) {
                if (data && data.exception) {
                  OB.UTIL.showConfirmation.display('', data.exception.message);
                } else {
                  if (data.length > 0) {
                    if (OB.UTIL.isNullOrUndefined(me.listOfReceipts)) {
                      me.listOfReceipts = [];
                    }
                    _.each(data, function (newOrder) {
                      var newModel = new Backbone.Model(newOrder);
                      me.listOfReceipts.push(newModel);
                    });
                    checkListCallback();
                  } else {
                    checkListCallback();
                  }
                }
              }, function (error) {
                OB.UTIL.showError(error);
                checkListCallback();
              });
            }
          }, {
            label: OB.I18N.getLabel('OBMOBC_LblCancel'),
            isConfirmButton: false,
            action: function () {
              checkListCallback();
            }
          }], {
            onHideFunction: function (popup) {
              checkListCallback();
            },
            autoDismiss: true
          });
        } else {
          order.unset('askForRelatedReceipts');
          checkListCallback();
        }
      };

      if (order.get('isLayaway')) {
        order.calculateReceipt(function () {
          loadNextOrder();
        });
      } else {
        order.calculateGrossAndSave(false, function () {
          loadNextOrder();
        });
      }
    };

    loadOrder = function (order) {
      var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('clickSearch'),
          process = new OB.DS.Process('org.openbravo.retail.posterminal.PaidReceipts');
      OB.UTIL.showLoading(true);
      process.exec({
        orderid: order.get('id'),
        originServer: originServer
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
              if (order.get('askForRelatedReceipts')) {
                order.unset('askForRelatedReceipts');
                data[0].askForRelatedReceipts = true;
              }
              orderList.newPaidReceipt(data[0], function (newOrder) {
                newOrder.set('searchSynchId', searchSynchId);
                orderList.addPaidReceipt(newOrder, continueAfterPaidReceipt);
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

    if (me.loadingReceipt) {
      OB.UTIL.OrderSelectorUtils.addToListOfReceipts(model);
      return;
    }
    me.loadingReceipt = true;
    model.attributes.askForRelatedReceipts = true;
    orderList.checkForDuplicateReceipts(model, loadOrder, checkListCallback, fromSelector);
  };

}());