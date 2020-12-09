/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.UTIL.OrderSelectorUtils = {};

  OB.UTIL.OrderSelectorUtils.addToListOfReceipts = function(
    model,
    orderList,
    context,
    originServer,
    calledFrom,
    callback
  ) {
    if (OB.UTIL.isNullOrUndefined(this.listOfReceipts)) {
      this.listOfReceipts = [];
    }
    if (!OB.UTIL.isNullOrUndefined(model)) {
      var receipt = {
        model: model,
        orderList: orderList,
        context: context,
        originServer: originServer,
        calledFrom: calledFrom,
        callback: callback
      };
      this.listOfReceipts.push(receipt);
    }
  };

  OB.UTIL.OrderSelectorUtils.checkOrderAndLoad = function(
    model,
    orderList,
    context,
    originServer,
    calledFrom,
    orderCallback
  ) {
    let me = this;
    if (me.loadingReceipt) {
      OB.UTIL.OrderSelectorUtils.addToListOfReceipts(
        model,
        orderList,
        context,
        originServer,
        calledFrom,
        orderCallback
      );
      return;
    }
    me.loadingReceipt = true;
    me.listOfExecution = me.listOfExecution || [];

    let continueAfterPaidReceipt,
      checkListCallback,
      errorCallback,
      orderLoaded,
      loadOrder,
      loadOrders,
      loadOrdersProcess,
      recursiveCallback,
      recursiveIdx,
      currentModel,
      currentContext,
      currentOriginServer,
      currentCallback;

    checkListCallback = function() {
      if (currentCallback && currentCallback instanceof Function) {
        currentCallback();
      }
      OB.UTIL.ProcessController.finish(
        'loadPaidReceipts',
        me.listOfExecution.shift()
      );
      if (me.listOfReceipts && me.listOfReceipts.length > 0) {
        var currentReceipt = me.listOfReceipts.shift();
        loadOrdersProcess(
          currentReceipt.model,
          currentReceipt.orderList,
          currentReceipt.context,
          currentReceipt.originServer,
          currentReceipt.calledFrom,
          currentReceipt.callback
        );
      } else {
        me.loadingReceipt = false;
      }
    };

    errorCallback = function(msg, msgInPopup) {
      if (msg) {
        if (msgInPopup) {
          OB.UTIL.showConfirmation.display('', msg);
        } else {
          OB.UTIL.showError(msg);
        }
      }
      recursiveCallback = undefined;
      recursiveIdx = undefined;
      checkListCallback();
    };

    continueAfterPaidReceipt = function(order) {
      var loadNextOrder = function() {
        if (recursiveCallback) {
          recursiveCallback(recursiveIdx + 1);
        } else {
          checkListCallback();
        }
      };

      if (order.get('isLayaway')) {
        order.calculateReceipt(function() {
          loadNextOrder();
        });
      } else {
        order.calculateGrossAndSave(true, function() {
          loadNextOrder();
        });
      }
    };

    orderLoaded = function(data) {
      if (data && data.length === 1) {
        if (currentContext.model.get('leftColumnViewManager').isMultiOrder()) {
          if (currentContext.model.get('multiorders')) {
            currentContext.model.get('multiorders').resetValues();
          }
          currentContext.model.get('leftColumnViewManager').setOrderMode();
        }
        OB.UTIL.HookManager.executeHooks(
          'OBRETUR_ReturnFromOrig',
          {
            order: data[0],
            context: currentContext,
            params: {}
          },
          function(args) {
            if (args.cancelOperation) {
              errorCallback();
              return;
            }
            OB.UTIL.TicketListUtils.newPaidReceipt(data[0], function(newOrder) {
              OB.UTIL.TicketListUtils.addPaidReceipt(
                newOrder,
                continueAfterPaidReceipt
              );
            });
          }
        );
      } else {
        errorCallback();
      }
    };

    loadOrder = function(order) {
      var process = new OB.DS.Process(
        'org.openbravo.retail.posterminal.PaidReceipts'
      );
      process.exec(
        {
          orderid: order.get('id'),
          originServer: currentOriginServer,
          crossStore: OB.UTIL.isCrossStoreReceipt(order)
            ? order.get('organization')
            : null
        },
        function(data) {
          if (data && data.exception) {
            errorCallback(data.exception.message, true);
          } else {
            if (data[0].recordInImportEntry) {
              errorCallback();
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                OB.I18N.getLabel('OBPOS_ReceiptNotSynced', [data[0].documentNo])
              );
            } else {
              orderLoaded(data);
            }
          }
        },
        function(error) {
          errorCallback();
        },
        true,
        5000
      );
    };

    loadOrders = function(models) {
      OB.UTIL.ProcessController.finish(
        'loadPaidReceipts',
        me.listOfExecution.shift()
      );
      context.doShowPopup({
        popup: 'modalOpenRelatedReceipts',
        args: {
          models: models,
          callback: function(selectedModels) {
            me.listOfExecution.push(
              OB.UTIL.ProcessController.start('loadPaidReceipts')
            );
            if (selectedModels.length === 1) {
              loadOrder(models[0]);
            } else {
              var process = new OB.DS.Process(
                'org.openbravo.retail.posterminal.process.OpenRelatedReceipts'
              );
              process.exec(
                {
                  orders: selectedModels,
                  originServer: currentOriginServer
                },
                function(data) {
                  if (data && data.exception) {
                    errorCallback(data.exception.message, true);
                  } else if (data && data.length > 0) {
                    var loadOrder;
                    loadOrder = function(idx) {
                      if (idx === data.length) {
                        recursiveCallback = undefined;
                        recursiveIdx = undefined;
                        checkListCallback();
                      } else {
                        var order = data[idx];
                        recursiveIdx = idx;
                        orderLoaded([order]);
                      }
                    };
                    recursiveCallback = loadOrder;
                    loadOrder(0);
                  } else {
                    errorCallback(
                      OB.I18N.getLabel('OBPOS_RelatedReceiptNotFound')
                    );
                  }
                },
                function(error) {
                  errorCallback();
                },
                true,
                5000
              );
            }
          }
        }
      });
    };

    loadOrdersProcess = function(
      model,
      orderList,
      context,
      originServer,
      calledFrom,
      callback
    ) {
      currentModel = model;
      currentContext = context;
      currentOriginServer = originServer;
      currentCallback = callback;

      if (calledFrom === 'return') {
        orderLoaded([model]);
        return;
      }
      me.listOfExecution.push(
        OB.UTIL.ProcessController.start('loadPaidReceipts')
      );
      OB.UTIL.TicketListUtils.checkForDuplicateReceipts(
        model,
        function(order) {
          if (
            !OB.MobileApp.model.get('terminal').terminalType
              .ignoreRelatedreceipts &&
            OB.MobileApp.model.get('terminal').terminalType
              .openrelatedreceipts &&
            model.get('businessPartner') !==
              OB.MobileApp.model.get('terminal').businessPartner
          ) {
            var process = new OB.DS.Process(
              'org.openbravo.retail.posterminal.process.SearchRelatedReceipts'
            );
            process.exec(
              {
                orderId: order.get('id'),
                bp: order.get('businessPartner'),
                originServer: originServer
              },
              function(data) {
                if (data && data.exception) {
                  errorCallback(data.exception.message, true);
                } else {
                  if (data.length > 0) {
                    var models = [currentModel],
                      checkForDuplicateReceipt,
                      newOrder,
                      newModel;
                    checkForDuplicateReceipt = function(idx) {
                      if (idx === data.length) {
                        if (models.length === 1) {
                          // If there's only one model, it means that the related orders are already loaded (in this or in other session)
                          // Only the required order is loaded and is not asked to open the related receipts
                          loadOrder(models[0]);
                        } else {
                          loadOrders(models);
                        }
                      } else {
                        newOrder = data[idx];
                        newModel = new Backbone.Model(newOrder);
                        OB.UTIL.TicketListUtils.checkForDuplicateReceipts(
                          newModel,
                          function(checkedOrder) {
                            models.push(checkedOrder);
                            checkForDuplicateReceipt(idx + 1);
                          },
                          function() {
                            checkForDuplicateReceipt(idx + 1);
                          }
                        );
                      }
                    };
                    checkForDuplicateReceipt(0);
                  } else {
                    loadOrder(order);
                  }
                }
              },
              function(error) {
                errorCallback();
              }
            );
          } else {
            loadOrder(order);
          }
        },
        function() {
          checkListCallback();
        },
        calledFrom
      );
    };

    loadOrdersProcess(
      model,
      null,
      context,
      originServer,
      calledFrom,
      orderCallback
    );
  };
})();
