/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the StockChecker class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function StockCheckerDefinition() {
  // gets a line of a backbone order by its id
  const getLineById = (order, lineId) => {
    order.get('lines').find(l => {
      return l.id === lineId;
    });
  };

  // transform settings to be able to use the current stock calculation logic
  const transformSettings = settings => {
    // eslint-disable-next-line no-param-reassign
    settings.order = OB.App.StateBackwardCompatibility.getInstance(
      'Ticket'
    ).toBackboneObject(settings.ticket);
  };

  /**
   * A singleton class used to retrieve information about the product stock.
   */
  class StockChecker {
    /**
     * Checks if there is stock of a particular product
     * @param {object} product - A product object
     * @param {number} qty - The quantity to be checked
     * @param {object} settings - additional information used to get the stock
     */
    async hasStock(product, qty, settings = {}) {
      try {
        transformSettings(settings);
        const hasStock = await this.checkLineStock(
          new OB.Model.Product(product),
          qty,
          settings
        );
        return hasStock;
      } catch (err) {
        throw new Error(
          `Could not check stock for product ${product.id}: ${err}`
        );
      }
    }

    checkLineStock(product, qty, settings = {}) {
      const { order } = settings;
      const { attrs } = settings;
      const productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(
        product
      );

      return new Promise(resolve => {
        if (order.get('isQuotation')) {
          resolve(true);
        }
        const positiveQty = OB.DEC.compare(qty) > 0;
        const checkStockActions = [];
        let stockValidation = false;
        const scanningProduct =
          attrs.kindOriginator === 'OB.OBPOSPointOfSale.UI.KeyboardOrder' &&
          attrs.isScanning;

        const getLineStock = () => {
          if (
            checkStockActions.length &&
            (stockValidation || !scanningProduct)
          ) {
            this.getStoreStock(
              product,
              qty,
              settings,
              checkStockActions,
              hasStock => resolve(hasStock)
            );
          } else {
            resolve(true);
          }
        };

        if (
          positiveQty &&
          OB.MobileApp.model.hasPermission('OBPOS_EnableStockValidation', true)
        ) {
          checkStockActions.push('stockValidation');
          stockValidation = true;
        }

        if (
          OB.MobileApp.model.hasPermission(
            'OBPOS_CheckStockForNotSaleWithoutStock',
            true
          )
        ) {
          if (positiveQty && productStatus.restrictsaleoutofstock) {
            checkStockActions.push('discontinued');
          }

          const line = settings.line ? getLineById(order, settings.line) : null;
          let isCrossStore = false;
          if (line) {
            isCrossStore = OB.UTIL.isCrossStoreLine(line);
          } else if (attrs && attrs.organization) {
            isCrossStore = OB.UTIL.isCrossStoreOrganization(attrs.organization);
          }

          if (
            positiveQty &&
            isCrossStore &&
            (!line || OB.DEC.compare(OB.DEC.add(line.get('qty'), qty)) > 0)
          ) {
            checkStockActions.push('crossStore');
          }

          OB.UTIL.HookManager.executeHooks(
            'OBPOS_CheckStockAddProduct',
            {
              order,
              product,
              line,
              qty,
              checkStockActions
            },
            args => {
              if (args && args.cancelOperation) {
                resolve(false);
              }
              getLineStock();
            }
          );
        } else {
          getLineStock();
        }
      });
    }

    // eslint-disable-next-line class-methods-use-this
    getStoreStock(product, qty, settings, checkStockActions, callback) {
      const { order } = settings;
      const { attrs } = settings;
      const { options } = settings;

      if (product.get('productType') === 'S') {
        callback(true);
        return;
      }

      const lines = order.get('lines');
      let line =
        options && options.line ? getLineById(order, options.line) : null;
      if (!line && product.get('groupProduct')) {
        line = lines.find(l => {
          if (
            l.get('product').id === product.id &&
            ((l.get('qty') > 0 && qty > 0) || (l.get('qty') < 0 && qty < 0))
          ) {
            const affectedByPack = l.isAffectedByPack();
            if (!affectedByPack) {
              return true;
            }
            if (
              (options && options.packId === affectedByPack.ruleId) ||
              !(options && options.packId)
            ) {
              return true;
            }
          }
          return false;
        });
      }

      let warehouseId;
      let warehouse;
      if (attrs && attrs.warehouse) {
        warehouseId = attrs.warehouse.id;
      } else if (line) {
        warehouseId = line.get('warehouse').id;
      } else {
        warehouseId = OB.MobileApp.model.get('warehouses')[0].warehouseid;
      }

      let allLinesQty = qty;
      _.forEach(lines.models, l => {
        if (
          (l.get('product').get('id') === product.get('id') &&
            l.get('warehouse').id === warehouseId) ||
          (line && l.get('id') === line.get('id'))
        ) {
          allLinesQty = OB.DEC.add(
            allLinesQty,
            OB.DEC.sub(l.get('qty'), l.getDeliveredQuantity())
          );
        }
      });

      if (allLinesQty <= 0) {
        callback(true);
      }

      const stockScreen = options && options.stockScreen;
      if (
        stockScreen &&
        attrs &&
        attrs.warehouse &&
        !OB.UTIL.isNullOrUndefined(attrs.warehouse.warehouseqty)
      ) {
        OB.UTIL.StockUtils.checkStockCallback(
          checkStockActions,
          product,
          line,
          order,
          attrs,
          attrs.warehouse,
          allLinesQty,
          stockScreen,
          callback
        );
      } else if (
        !OB.MobileApp.model.get('connectedToERP') ||
        !navigator.onLine
      ) {
        OB.UTIL.StockUtils.noConnectionCheckStockCallback(
          product,
          line,
          order,
          allLinesQty,
          callback
        );
      } else {
        OB.UTIL.StockUtils.getReceiptLineStock(
          product.get('id'),
          line,
          data => {
            if (data && data.exception) {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                OB.I18N.getLabel('OBPOS_ErrorServerGeneric') +
                  data.exception.message
              );
              if (callback) {
                callback(false);
              }
            } else {
              warehouse = _.find(data.warehouses, w => {
                return w.warehouseid === warehouseId;
              });
              if (!warehouse) {
                warehouse = {
                  bins: [],
                  warehouseid: OB.MobileApp.model.get('warehouses')[0]
                    .warehouseid,
                  warehousename: OB.MobileApp.model.get('warehouses')[0]
                    .warehousename,
                  warehouseqty: OB.DEC.Zero
                };
              }
              OB.UTIL.StockUtils.checkStockCallback(
                checkStockActions,
                product,
                line,
                order,
                attrs,
                warehouse,
                allLinesQty,
                stockScreen,
                callback
              );
            }
          },
          () => {
            OB.UTIL.StockUtils.noConnectionCheckStockCallback(
              product,
              line,
              order,
              allLinesQty,
              callback
            );
          }
        );
      }
    }
  }

  OB.App.StockChecker = new StockChecker();
})();
