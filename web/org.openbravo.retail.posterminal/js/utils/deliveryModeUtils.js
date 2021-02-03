/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

var OBRDM = {};
OBRDM.UTIL = {};

(function() {
  /**
   * Add a NONE condition to data collection
   *
   * @param Collection
   */

  OBRDM.UTIL.addNoneCondition = function(data) {
    data.add(
      {
        id: '',
        name: ''
      },
      {
        at: 0
      }
    );
    return data;
  };

  /**
   * Fill Delivery Modes combo box
   *
   * @param combo Combo box
   * @param args Arguments of function: fetchDataFunction
   */

  OBRDM.UTIL.fillComboCollection = function(combo, args) {
    var deliveryModes = OB.MobileApp.model.get('deliveryModes'),
      modes = [];
    if (deliveryModes && deliveryModes.length > 0) {
      if (
        OB.UTIL.isCrossStoreLine(args.model) ||
        (!OB.UTIL.isNullOrUndefined(args.organization) &&
          args.organization.id !==
            OB.MobileApp.model.get('terminal').organization)
      ) {
        _.each(deliveryModes, function(delivery) {
          if (delivery.id !== 'PickAndCarry') {
            modes.push(delivery);
          }
        });
        deliveryModes = modes;
      }
      var data = new Backbone.Collection();
      data.add(deliveryModes);
      combo.dataReadyFunction(data, args);
    } else {
      OB.UTIL.showError(OB.I18N.getLabel('OBRDM_ErrorGettingDeliveryModes'));
      combo.dataReadyFunction(null, args);
    }
  };

  /**
   * Convert a delivery modes to string ready to used in sql IN
   *
   * @param deliveryModes Delivery modes list
   * @return SQL condition
   */
  OBRDM.UTIL.deliveryModesForFilter = function(deliveryModes) {
    var excluded = [];
    _.each(deliveryModes, function(excl) {
      excluded.push("'" + excl + "'");
    });
    return excluded.length > 0 ? excluded.join(',') : '';
  };

  /**
   * Validate if the Pick and Carry lines of the order are completely paid
   *
   * @param order Order that is being checked
   * @return true if the Pick and Carry lines are completely paid
   */

  OBRDM.UTIL.checkPickAndCarryPaidAmount = function(order) {
    var pickAndCarryAmount = OB.DEC.Zero;
    _.each(order.get('lines').models, function(line) {
      if (line.get('product').get('productType') !== 'S') {
        //Products
        if (
          !line.get('obrdmDeliveryMode') ||
          line.get('obrdmDeliveryMode') === 'PickAndCarry'
        ) {
          if (line.has('obposLinePrepaymentAmount')) {
            pickAndCarryAmount = OB.DEC.add(
              pickAndCarryAmount,
              line.get('obposLinePrepaymentAmount')
            );
          } else if (line.get('obposCanbedelivered')) {
            var discountAmt = _.reduce(
              line.get('promotions'),
              function(memo, promo) {
                return OB.DEC.add(
                  memo,
                  OB.UTIL.isNullOrUndefined(promo.amt) ? OB.DEC.Zero : promo.amt
                );
              },
              0
            );
            pickAndCarryAmount = OB.DEC.add(
              pickAndCarryAmount,
              OB.DEC.sub(line.get('gross'), discountAmt)
            );
          }
        }
      }
    });
    return {
      pickAndCarryAmount: pickAndCarryAmount,
      payment: order.getPaymentWithSign()
    };
  };

  OBRDM.UTIL.processDeliveryProductsServices = async function(selectedLines) {
    var carrierLines = null,
      me = OB.POS.terminal.$.containerWindow.getRoot().$
        .OBRDM_ReceiptMultilines;
    if (selectedLines) {
      carrierLines = selectedLines
        .filter(function(l) {
          return l.get('obrdmDeliveryMode') === 'HomeDelivery';
        })
        .map(function(l) {
          return {
            lineId: l.id,
            product: l.get('product').get('id'),
            productCategory: l.get('product').get('productCategory')
          };
        });
    }

    async function countDeliveryServices(data) {
      var countLines = 0,
        crossStoreProduct = selectedLines.find(function(l) {
          return l.get('product').get('crossStore') === true;
        });
      data.forEach(function(res) {
        var orderLine = selectedLines.find(function(l) {
          return l.id === res.lineId;
        });
        orderLine.set('hasDeliveryServices', res.hasDeliveryServices);
        if (res.hasDeliveryServices) {
          countLines++;
        }
      });
      if (countLines === carrierLines.length) {
        //trigger search
        var previousStatus = {
          tab: OB.MobileApp.model.get('lastPaneShown'),
          filterText: '',
          category: '',
          filteringBy: '',
          filter: '',
          customFilters: '',
          genericParent: ''
        };

        //Clear existing filters in Product Search
        me.owner.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.doClearAction();
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomClear();
        OB.UI.SearchProductCharacteristic.prototype.filterCustomClearConditions();
        OB.UI.SearchProductCharacteristic.prototype.filtersCustomAdd(
          new OB.UI.SearchServicesFilter({
            text: selectedLines
              .map(function(line) {
                return line.get('product').get('_identifier');
              })
              .join(', '),
            productList: selectedLines.map(function(line) {
              return line.get('product').get('id');
            }),
            orderlineList: selectedLines,
            extraParams: {
              isDeliveryService: true
            }
          })
        );
        me.bubble('onSelectFilter', {
          params: {
            skipProductCharacteristic: true,
            crossStore: crossStoreProduct ? true : false,
            crossStoreProductOrg: crossStoreProduct
              ? crossStoreProduct.get('organization')
              : undefined,
            crossStoreProductWhs: crossStoreProduct
              ? crossStoreProduct.get('warehouse')
              : undefined,
            searchCallback: function(data) {
              if (data && data.length === 1) {
                var attrs = {};
                OB.UI.SearchProductCharacteristic.prototype.customFilters.forEach(
                  function(filter) {
                    var filterAttr = filter.lineAttributes();
                    if (filterAttr) {
                      _.each(_.keys(filterAttr), function(key) {
                        attrs[key] = filterAttr[key];
                      });
                    }
                  }
                );
                me.bubble('onAddProduct', {
                  product: data[0],
                  attrs: attrs
                });
                if (OB.MobileApp.model.get('deliveryModesCrossStore')) {
                  me.owner.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.doClearAction();
                }
              } else if (data && data.length > 1) {
                me.bubble('onToggleLineSelection', {
                  status: true
                });
                me.bubble('onTabChange', {
                  tabPanel: 'searchCharacteristic'
                });
                me.bubble('onManageServiceProposal', {
                  proposalType: 'mandatory',
                  previousStatus: previousStatus
                });
                OB.MobileApp.view.waterfallDown('onShowProductList', {
                  productList: data
                });
              } else {
                if (OB.MobileApp.model.get('deliveryModesCrossStore')) {
                  me.owner.$.multiColumn.$.rightPanel.$.toolbarpane.$.searchCharacteristic.$.searchCharacteristicTabContent.doClearAction();
                }
              }
              OB.MobileApp.model.unset('deliveryModesCrossStore');
            }
          }
        });
      }
    }

    if (carrierLines && carrierLines.length === selectedLines.length) {
      //Trigger Delivery Services Search
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
        const date = new Date();
        const body = {
          lines: carrierLines,
          terminalTime: date,
          terminalTimeOffset: date.getTimezoneOffset(),
          remoteFilters: [
            {
              columns: [],
              operator: 'filter',
              value: 'OBRDM_DeliveryServiceFilter',
              params: [true]
            }
          ]
        };
        try {
          let data = await OB.App.Request.mobileServiceRequest(
            'org.openbravo.retail.posterminal.process.HasDeliveryServices',
            body
          );
          data = data.response.data;
          if (data && data.exception) {
            //ERROR or no connection
            OB.UTIL.showError(
              OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
            );
          } else if (data) {
            await countDeliveryServices(data);
          } else {
            OB.UTIL.showError(
              OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
            );
          }
        } catch (error) {
          OB.UTIL.showError(
            OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
          );
        }
      } else {
        var hasDeliveryServices = async function(line, callback) {
          let criteria = new OB.App.Class.Criteria();
          criteria = await OB.UTIL.servicesFilter(
            criteria,
            line.get('product').get('id'),
            line.get('product').get('productCategory')
          );
          criteria.criterion('obrdmIsdeliveryservice', true);
          try {
            const products = await OB.App.MasterdataModels.Product.find(
              criteria.build()
            );
            let data = [];
            for (let i = 0; i < products.length; i++) {
              data.push(OB.Dal.transform(OB.Model.Product, products[i]));
            }

            if (data && data.length > 0) {
              callback(true);
            } else {
              callback(false);
            }
          } catch (error) {
            OB.UTIL.showError(
              OB.I18N.getLabel('OBPOS_ErrorGettingRelatedServices')
            );
            callback(false);
          }
        };

        var data = [];
        var finalCallback = _.after(carrierLines.length, async function() {
          await countDeliveryServices(data);
        });

        selectedLines.forEach(function(carrierLine) {
          hasDeliveryServices(carrierLine, function(res) {
            data.push({
              lineId: carrierLine.get('id'),
              hasDeliveryServices: res
            });
            finalCallback();
          });
        });
      }
    }
  };
})();
