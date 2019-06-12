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
        }
      }
    );
  };

  OB.UTIL.StockUtils.navigateToStockScreen = function(
    product,
    warehouse,
    stockScreen
  ) {
    // Function to navigate to the stock screen window
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

  OB.UTIL.StockUtils.checkStockSuccessCallback = function(
    product,
    line,
    order,
    attrs,
    warehouse,
    allLinesQty,
    stockScreen,
    callback
  ) {
    // Function executed after the check stock process has sent a response, even if the response is an error response
    if (allLinesQty > warehouse.warehouseqty) {
      var productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(product),
        allowToAdd,
        allowMessage,
        notAllowMessage,
        askConfirmation;
      if (productStatus.restrictsaleoutofstock) {
        if (allowToAdd !== false) {
          if (
            OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)
          ) {
            if (line) {
              if (
                line.get('obrdmDeliveryMode') &&
                line.get('obrdmDeliveryMode') !== 'PickAndCarry'
              ) {
                allowToAdd = false;
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
                  allowToAdd =
                    product.get('obrdmDeliveryMode') === 'PickAndCarry';
                } else {
                  allowToAdd =
                    order.get('obrdmDeliveryModeProperty') === 'PickAndCarry';
                }
              }
            }
          } else {
            allowToAdd = true;
          }
        }
        if (_.isUndefined(allowMessage)) {
          allowMessage = OB.I18N.getLabel('OBPOS_DiscontinuedWithoutStock', [
            product.get('_identifier'),
            productStatus.name,
            warehouse.warehouseqty,
            warehouse.warehousename,
            allLinesQty
          ]);
        }
        if (_.isUndefined(notAllowMessage)) {
          notAllowMessage = OB.I18N.getLabel('OBPOS_CannotSellWithoutStock', [
            product.get('_identifier'),
            productStatus.name,
            allLinesQty,
            warehouse.warehouseqty,
            warehouse.warehousename
          ]);
        }
        askConfirmation = true;
      }
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreAddProductWithoutStock',
        {
          allowToAdd: allowToAdd,
          allowMessage: allowMessage,
          notAllowMessage: notAllowMessage,
          askConfirmation: askConfirmation,
          order: order,
          line: line,
          product: product,
          attrs: attrs
        },
        function(args) {
          if (args.cancelOperation) {
            if (callback && callback instanceof Function) {
              callback(false);
            }
            return;
          }
          if (!args.allowToAdd) {
            if (args.askConfirmation) {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBPOS_NotEnoughStock'),
                args.notAllowMessage,
                [
                  {
                    label: OB.I18N.getLabel('OBMOBC_LblOk'),
                    action: function() {
                      OB.UTIL.StockUtils.navigateToStockScreen(
                        product,
                        warehouse,
                        stockScreen
                      );
                    }
                  }
                ],
                {
                  onHideFunction: function() {
                    OB.UTIL.StockUtils.navigateToStockScreen(
                      product,
                      warehouse,
                      stockScreen
                    );
                  }
                }
              );
              if (callback && callback instanceof Function) {
                callback(false);
              }
            } else {
              if (callback && callback instanceof Function) {
                callback(false);
              }
            }
          } else {
            OB.UTIL.showLoading(false);
            if (args.askConfirmation) {
              OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
                popup: 'OBPOSPointOfSale_UI_Modals_ModalStockDiscontinued',
                args: {
                  header: OB.I18N.getLabel('OBPOS_NotEnoughStock'),
                  message: args.allowMessage,
                  product: product,
                  buttons: [
                    {
                      label: OB.I18N.getLabel('OBMOBC_LblOk'),
                      action: function() {
                        if (callback && callback instanceof Function) {
                          callback(true, warehouse, allLinesQty, stockScreen);
                        }
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
                        if (callback && callback instanceof Function) {
                          callback(false);
                        }
                      }
                    }
                  ],
                  options: {
                    onHideFunction: function() {
                      OB.UTIL.StockUtils.navigateToStockScreen(
                        product,
                        warehouse,
                        stockScreen
                      );
                      if (callback && callback instanceof Function) {
                        callback(false);
                      }
                    }
                  },
                  acceptLine: function(accept, newAttrs) {
                    if (accept && newAttrs) {
                      attrs = Object.assign(attrs, newAttrs);
                    }
                    callback(accept);
                  }
                }
              });
            } else {
              if (callback && callback instanceof Function) {
                callback(true, warehouse, allLinesQty, stockScreen);
              }
            }
          }
        }
      );
    } else if (callback && callback instanceof Function) {
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PreAddProductWithStock',
        {
          order: order,
          line: line,
          product: product
        },
        function(args) {
          if (args.cancelOperation) {
            if (callback && callback instanceof Function) {
              callback(false);
            }
            return;
          }
          callback(true, warehouse, allLinesQty, stockScreen);
        }
      );
    }
  };

  OB.UTIL.StockUtils.checkStockErrorCallback = function(
    product,
    line,
    order,
    allLinesQty,
    callback
  ) {
    // Function executed after the check stock process hasn't sent a response (timeout, server not available, etc.)
    var productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(product);
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
            if (callback && callback instanceof Function) {
              callback(true, null, null, null);
            }
          }
        },
        {
          label: OB.I18N.getLabel('OBMOBC_LblCancel'),
          action: function() {
            if (callback && callback instanceof Function) {
              callback(false);
            }
          }
        }
      ],
      {
        onHideFunction: function() {
          if (callback && callback instanceof Function) {
            callback(false);
          }
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
        checkStock =
          positiveQty &&
          (productStatus.restrictsaleoutofstock ||
            OB.UTIL.isCrossStoreProduct(product));

      OB.UTIL.HookManager.executeHooks(
        'OBPOS_CheckStockPrePayment',
        {
          order: order,
          orders: orders,
          line: line,
          checkStock: checkStock
        },
        function(args) {
          if (args.cancelOperation) {
            if (callback && callback instanceof Function) {
              callback(false);
            }
            return;
          }
          if (args.checkStock) {
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
                OB.UTIL.StockUtils.checkStockSuccessCallback(
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
                OB.UTIL.StockUtils.checkStockErrorCallback(
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
      if (order.get('isEditable')) {
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
