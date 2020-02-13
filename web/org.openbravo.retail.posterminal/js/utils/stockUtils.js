/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _ */

(function() {
  OB.UTIL.StockUtils = {};

  OB.UTIL.StockUtils.getReceiptLineStock = function(
    productId,
    line,
    successCallback,
    errorCallback
  ) {
    var serverCallStoreDetailedStock = new OB.DS.Process(
      'org.openbravo.retail.posterminal.stock.StoreDetailedStock'
    );
    serverCallStoreDetailedStock.exec(
      {
        crossOrganization: line ? line.get('organization').id : null,
        product: productId ? productId : line.get('product').get('id'),
        line: line
      },
      function(data) {
        successCallback(data);
      },
      function(data) {
        if (errorCallback && errorCallback instanceof Function) {
          errorCallback(data);
        } else if (successCallback && successCallback instanceof Function) {
          successCallback(data);
        }
      }
    );
  };

  OB.UTIL.StockUtils.hasStockAction = function(checkStockActions, stockAction) {
    return !_.isUndefined(
      _.find(checkStockActions, function(checkStockAction) {
        return checkStockAction === stockAction;
      })
    );
  };

  //Function to navigate to the stock screen window
  OB.UTIL.StockUtils.navigateToStockScreen = function(
    product,
    warehouse,
    stockScreen
  ) {
    if (stockScreen && OB.MobileApp.model.get('connectedToERP')) {
      var params = {};
      params.leftSubWindow =
        OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow;
      params.product = product;
      params.warehouse = warehouse;
      OB.MobileApp.view.$.containerWindow
        .getRoot()
        .showLeftSubWindow({}, params);
    }
  };

  //Function executed after the check stock process has sent a response, even if the response is an error response
  OB.UTIL.StockUtils.checkStockCallback = function(
    checkStockActions,
    product,
    line,
    order,
    attrs,
    warehouse,
    allLinesQty,
    stockScreen,
    callback
  ) {
    function finalCallback(status, _warehouse, _allLinesQty, _stockScreen) {
      if (callback && callback instanceof Function) {
        callback(status, _warehouse, _allLinesQty, _stockScreen);
      }
    }

    if (allLinesQty > warehouse.warehouseqty) {
      var stockActions = [];

      if (
        OB.UTIL.StockUtils.hasStockAction(checkStockActions, 'stockValidation')
      ) {
        var stockValidationAction = {
          actionName: 'stockValidation'
        };
        stockValidationAction.allowToAdd = true;
        stockValidationAction.askConfirmation = true;
        stockValidationAction.allowMessage = OB.I18N.getLabel(
          'OBPOS_NotStockValidation',
          [
            product.get('_identifier'),
            allLinesQty,
            warehouse.warehousename,
            warehouse.warehouseqty
          ]
        );
        stockActions.push(stockValidationAction);
      }

      if (
        OB.UTIL.StockUtils.hasStockAction(checkStockActions, 'discontinued')
      ) {
        var discontinuedAction = {
            actionName: 'discontinued'
          },
          productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(product),
          allowToAdd;
        if (line) {
          if (
            line.get('obrdmDeliveryMode') &&
            line.get('obrdmDeliveryMode') !== 'PickAndCarry'
          ) {
            allowToAdd = false;
          } else {
            allowToAdd = true;
          }
        } else {
          if (order.get('orderType') === 2) {
            if (product.get('obrdmDeliveryModeLyw')) {
              allowToAdd =
                product.get('obrdmDeliveryModeLyw') === 'PickAndCarry';
            } else {
              allowToAdd =
                order.get('obrdmDeliveryModeProperty') === 'PickAndCarry';
            }
          } else {
            if (product.get('obrdmDeliveryMode')) {
              allowToAdd = product.get('obrdmDeliveryMode') === 'PickAndCarry';
            } else {
              allowToAdd =
                order.get('obrdmDeliveryModeProperty') === 'PickAndCarry';
            }
          }
        }
        discontinuedAction.allowToAdd = allowToAdd;
        discontinuedAction.askConfirmation = true;
        discontinuedAction.allowMessage = OB.I18N.getLabel(
          'OBPOS_DiscontinuedWithoutStock',
          [
            product.get('_identifier'),
            productStatus.name,
            warehouse.warehouseqty,
            warehouse.warehousename,
            allLinesQty
          ]
        );
        discontinuedAction.notAllowMessage = OB.I18N.getLabel(
          'OBPOS_CannotSellWithoutStock',
          [
            product.get('_identifier'),
            productStatus.name,
            allLinesQty,
            warehouse.warehouseqty,
            warehouse.warehousename
          ]
        );
        stockActions.push(discontinuedAction);
      }

      if (OB.UTIL.StockUtils.hasStockAction(checkStockActions, 'crossStore')) {
        var crossStoreAction = {
          actionName: 'crossStore'
        };
        crossStoreAction.allowToAdd = false;
        crossStoreAction.askConfirmation = true;
        crossStoreAction.notAllowMessage = OB.I18N.getLabel(
          'OBPOS_NotStockCrossStore',
          [
            allLinesQty,
            product.get('_identifier'),
            warehouse.warehousename,
            warehouse.warehouseqty
          ]
        );
        stockActions.push(crossStoreAction);
      }

      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreAddProductWithoutStock',
        {
          checkStockActions: checkStockActions,
          stockActions: stockActions,
          order: order,
          line: line,
          product: product,
          attrs: attrs,
          warehouse: warehouse,
          allLinesQty: allLinesQty
        },
        function(args) {
          if (args.cancelOperation) {
            finalCallback(false);
            return;
          }
          var allowToAdd = true,
            askConfirmation = false,
            allowMessage = '',
            notAllowMessage = '',
            actionName = '',
            actionsNotAllowToAdd = _.filter(args.stockActions, function(
              stockAction
            ) {
              return !stockAction.allowToAdd;
            }),
            setConfirmationMsg = function(models) {
              var confirmationAction = _.find(models, function(stockAction) {
                return stockAction.askConfirmation;
              });
              if (confirmationAction) {
                askConfirmation = true;
                allowMessage = confirmationAction.allowMessage;
                notAllowMessage = confirmationAction.notAllowMessage;
                actionName = confirmationAction.actionName;
              }
            };

          if (actionsNotAllowToAdd.length) {
            allowToAdd = false;
            setConfirmationMsg(actionsNotAllowToAdd);
          } else {
            setConfirmationMsg(args.stockActions);
          }

          if (askConfirmation) {
            var message, buttons;

            if (allowToAdd) {
              message = allowMessage;
              buttons = [
                {
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  action: function() {
                    finalCallback(true, warehouse, allLinesQty, stockScreen);
                  }
                },
                {
                  label: OB.I18N.getLabel('OBMOBC_LblCancel'),
                  action: function() {
                    OB.UTIL.StockUtils.navigateToStockScreen(
                      product,
                      warehouse,
                      stockScreen
                    );
                    finalCallback(false);
                  }
                }
              ];
            } else {
              message = notAllowMessage;
              buttons = [
                {
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  action: function() {
                    OB.UTIL.StockUtils.navigateToStockScreen(
                      product,
                      warehouse,
                      stockScreen
                    );
                    finalCallback(false);
                  }
                }
              ];
            }

            OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
              popup: 'OBPOSPointOfSale_UI_Modals_ModalStockDiscontinued',
              args: {
                header: OB.I18N.getLabel('OBPOS_NotEnoughStock'),
                message: message,
                order: order,
                line: line,
                product: product,
                actionName: actionName,
                buttons: buttons,
                attrs: attrs,
                options: {
                  onHideFunction: function() {
                    OB.UTIL.StockUtils.navigateToStockScreen(
                      product,
                      warehouse,
                      stockScreen
                    );
                    finalCallback(false);
                  }
                },
                acceptLine: function(accept, newAttrs) {
                  if (accept && newAttrs) {
                    attrs = Object.assign(attrs, newAttrs);
                  }
                  finalCallback(accept);
                }
              }
            });
          } else {
            if (allowToAdd) {
              finalCallback(true, warehouse, allLinesQty, stockScreen);
            } else {
              finalCallback(false);
            }
          }
        }
      );
    } else {
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreAddProductWithStock',
        {
          checkStockActions: checkStockActions,
          order: order,
          line: line,
          product: product,
          warehouse: warehouse
        },
        function(args) {
          if (args.cancelOperation) {
            finalCallback(false);
            return;
          }
          finalCallback(true, warehouse, allLinesQty, stockScreen);
        }
      );
    }
  };

  //Function executed after the check stock process hasn't sent a response (timeout, server not available, etc.)
  OB.UTIL.StockUtils.noConnectionCheckStockCallback = function(
    product,
    line,
    order,
    allLinesQty,
    callback
  ) {
    var productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(product);

    function finalCallback(status, _warehouse, _allLinesQty, _stockScreen) {
      if (callback && callback instanceof Function) {
        callback(status, _warehouse, _allLinesQty, _stockScreen);
      }
    }

    OB.UTIL.showConfirmation.display(
      OB.I18N.getLabel('OBMOBC_ConnectionFail'),
      OB.I18N.getLabel('OBPOS_CannotVerifyStock', [
        product.get('_identifier'),
        productStatus.name
      ]),
      [
        {
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          action: function() {
            finalCallback(true, null, null, null);
          }
        },
        {
          label: OB.I18N.getLabel('OBMOBC_LblCancel'),
          action: function() {
            finalCallback(false);
          }
        }
      ],
      {
        onHideFunction: function() {
          finalCallback(false);
        }
      }
    );
  };

  OB.UTIL.StockUtils.checkOrderLinesStock = function(orders, callback) {
    var checkedLines = [],
      checkOrderStock,
      checkOrderLineStock;
    checkOrderLineStock = function(idxOrderLine, order, orderCallback) {
      if (idxOrderLine === order.get('lines').length) {
        orderCallback();
        return;
      }
      var line = order.get('lines').at(idxOrderLine),
        product = line.get('product'),
        productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(product),
        positiveQty = OB.DEC.compare(line.get('qty')) > 0,
        checkStockActions = [];

      if (product.get('productType') === 'S') {
        checkOrderLineStock(idxOrderLine + 1, order, orderCallback);
        return;
      }

      if (
        positiveQty &&
        OB.MobileApp.model.hasPermission('OBPOS_EnableStockValidation', true) &&
        OB.UTIL.ProcessController.isProcessActive('createOrderFromQuotation')
      ) {
        checkStockActions.push('stockValidation');
      }

      if (positiveQty && productStatus.restrictsaleoutofstock) {
        checkStockActions.push('discontinued');
      }

      if (positiveQty && OB.UTIL.isCrossStoreProduct(product)) {
        checkStockActions.push('crossStore');
      }

      OB.UTIL.HookManager.executeHooks(
        'OBPOS_CheckStockPrePayment',
        {
          order: order,
          orders: orders,
          line: line,
          checkStockActions: checkStockActions
        },
        function(args) {
          if (args.cancelOperation) {
            if (callback && callback instanceof Function) {
              callback(false);
            }
            return;
          }
          if (args.checkStockActions.length) {
            var qtyInOtherOrders = OB.DEC.Zero,
              options = {
                line: line
              },
              i,
              j,
              checkedLine;
            // Get the quantity if the other editable orders for this line
            for (i = orders.indexOf(order); i < orders.length; i++) {
              var currentOrder = orders[i];
              if (
                order.id !== currentOrder.id &&
                currentOrder.get('isEditable')
              ) {
                for (j = 0; j < currentOrder.get('lines').length; j++) {
                  var currentOrderLine = currentOrder.get('lines').models[j];
                  if (
                    currentOrderLine.get('product').get('id') ===
                      line.get('product').get('id') &&
                    currentOrderLine.get('warehouse').id ===
                      line.get('warehouse').id
                  ) {
                    qtyInOtherOrders += currentOrderLine.get('qty');
                  }
                }
              }
            }
            checkedLine = _.find(checkedLines, function(l) {
              return (
                l.productId === line.get('product').get('id') &&
                l.warehouseId === line.get('warehouse').id
              );
            });
            if (!checkedLine) {
              checkedLine = {
                productId: line.get('product').get('id'),
                warehouseId: line.get('warehouse').id
              };
              checkedLines.push(checkedLine);
              order.getStoreStock(
                line.get('product'),
                qtyInOtherOrders,
                options,
                null,
                args.checkStockActions,
                function(hasStock, warehouse, allLinesQty, stockScreen) {
                  if (hasStock) {
                    checkedLine.warehouse = warehouse;
                    checkedLine.allLinesQty = allLinesQty;
                    checkedLine.stockScreen = stockScreen;
                    checkOrderLineStock(idxOrderLine + 1, order, orderCallback);
                  } else {
                    callback(false);
                  }
                }
              );
            } else {
              var addLineCallback = function(hasStock) {
                if (hasStock) {
                  checkOrderLineStock(idxOrderLine + 1, order, orderCallback);
                } else {
                  callback(false);
                }
              };
              if (
                !_.isNull(checkedLine.warehouse) &&
                !_.isNull(checkedLine.allLinesQty)
              ) {
                OB.UTIL.StockUtils.checkStockCallback(
                  args.checkStockActions,
                  line.get('product'),
                  line,
                  order,
                  null,
                  checkedLine.warehouse,
                  checkedLine.allLinesQty,
                  checkedLine.stockScreen,
                  function(hasStock) {
                    addLineCallback(hasStock);
                  }
                );
              } else {
                OB.UTIL.StockUtils.noConnectionCheckStockCallback(
                  line.get('product'),
                  line,
                  order,
                  checkedLine.allLinesQty,
                  function(hasStock) {
                    addLineCallback(hasStock);
                  }
                );
              }
            }
          } else {
            checkOrderLineStock(idxOrderLine + 1, order, orderCallback);
          }
        }
      );
    };
    checkOrderStock = function(idxOrder) {
      if (idxOrder === orders.length) {
        callback(true);
        return;
      }
      var order = orders[idxOrder];
      if (
        (order.get('isEditable') && !order.get('isQuotation')) ||
        OB.UTIL.ProcessController.isProcessActive('createOrderFromQuotation')
      ) {
        checkOrderLineStock(0, order, function() {
          checkOrderStock(idxOrder + 1);
        });
      } else {
        checkOrderStock(idxOrder + 1);
      }
    };
    // Check stock for the lines that are not allowed to be sold without stock
    checkOrderStock(0);
  };
})();
