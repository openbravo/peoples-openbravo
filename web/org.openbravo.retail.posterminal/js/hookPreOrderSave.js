/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global _, OBRDM */

(function () {

  if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true) && OB.UTIL.HookManager) {

    // Hook OBPOS_PreOrderSave to get context 
    OB.UTIL.HookManager.registerHook('OBPOS_PreOrderSave', function (args, callbacks) {
      var model = args.receipt,
          lines = model.get("lines"),
          deliver = model.get('completeTicket') || model.get('payOnCredit'),
          generateShipment = false,
          pickAndCarryPaymentStatus;

      if (model.get('completeTicket') && !model.get('isNegative')) {
        pickAndCarryPaymentStatus = OBRDM.UTIL.checkPickAndCarryPaidAmount(args.receipt);
        if (pickAndCarryPaymentStatus.payment < pickAndCarryPaymentStatus.pickAndCarryAmount) {
          var symbol = OB.MobileApp.model.get('terminal').symbol,
              symbolAtTheRight = OB.MobileApp.model.get('terminal').currencySymbolAtTheRight;
          args.cancellation = true;
          if (OB.MobileApp.model.showSynchronizedDialog) {
            OB.MobileApp.model.hideSynchronizingDialog();
          }
          if (args.context.context.get('leftColumnViewManager').isOrder()) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBRDM_PickAndCarryPaymentError', [OB.I18N.formatCurrencyWithSymbol(pickAndCarryPaymentStatus.pickAndCarryAmount, symbol, symbolAtTheRight)]), null, {
              onHideFunction: function () {
                OB.UTIL.HookManager.callbackExecutor(args, callbacks);
              }
            });
          } else {
            if (OB.MobileApp.view.openedPopup && OB.MobileApp.view.openedPopup.isPickAndCarryValidation) {
              var msg = OB.I18N.getLabel('OBRDM_PickAndCarryOrderError', [model.get('documentNo'), OB.I18N.formatCurrencyWithSymbol(pickAndCarryPaymentStatus.payment, symbol, symbolAtTheRight), OB.I18N.formatCurrencyWithSymbol(pickAndCarryPaymentStatus.pickAndCarryAmount, symbol, symbolAtTheRight)]);
              OB.MobileApp.view.openedPopup.$.bodyContent.$.scrollArea.createComponent({
                content: msg
              }).render();
            } else {
              var msgHeader = OB.I18N.getLabel('OBRDM_PickAndCarryPaymentErrorMultiOrder', [pickAndCarryPaymentStatus.pickAndCarryAmount]),
                  orderMsg = OB.I18N.getLabel('OBRDM_PickAndCarryOrderError', [model.get('documentNo'), OB.I18N.formatCurrencyWithSymbol(pickAndCarryPaymentStatus.payment, symbol, symbolAtTheRight), OB.I18N.formatCurrencyWithSymbol(pickAndCarryPaymentStatus.pickAndCarryAmount, symbol, symbolAtTheRight)]);
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), [msgHeader, orderMsg], null, {
                onShowFunction: function (popup) {
                  popup.isPickAndCarryValidation = true;
                },
                onHideFunction: function () {
                  OB.UTIL.HookManager.callbackExecutor(args, callbacks);
                }
              });
            }
          }
          return;
        }
      }

      // Deliver products and services 'Linked to Product'
      _.each(lines.models, function (line) {
        var isDeferredService = false,
            deliverNotServiceLine, deliverNegativeLine, deliverDeferredServiceLine;
        if (model.get('completeTicket') || model.get('payOnCredit')) {
          if (line.get('product').get('productType') !== 'S' && line.get('obrdmDeliveryMode') && line.get('obrdmDeliveryMode') !== 'PickAndCarry') {
            line.set('obposQtytodeliver', line.getDeliveredQuantity());
          } else if (line.get('product').get('productType') !== 'S' || (line.get('product').get('productType') === 'S' && line.get('product').get('isLinkedToProduct') && line.get('qty') < 0)) {
            line.set('obposQtytodeliver', line.get('qty'));
          } else if (line.get('product').get('productType') === 'S' && line.get('product').get('isLinkedToProduct')) {
            var qtyToDeliver = OB.DEC.Zero;

            _.each(line.get('relatedLines'), function (relatedLine) {
              var l;
              if (relatedLine.deferred) {
                if (!isDeferredService) {
                  isDeferredService = true;
                }
              } else {
                l = _.find(lines.models, function (l) {
                  return l.get('id') === relatedLine.orderlineId;
                });
              }

              // Calculate the quantity to deliver (the quantity that is going to be delivered in this product due to
              // the related product's delivery modes, but excluding the deferred services)
              if (l) {
                // Is not a deferred relation
                if (!l.get('obrdmDeliveryMode') || l.get('obrdmDeliveryMode') === 'PickAndCarry') {
                  if (line.get('product').get('quantityRule') === 'PP' && line.get('product').get('groupProduct')) {
                    qtyToDeliver = OB.DEC.add(qtyToDeliver, l.get('qty'));
                  } else {
                    qtyToDeliver = OB.DEC.One;
                  }
                } else {
                  if (line.get('product').get('quantityRule') === 'PP' && line.get('product').get('groupProduct')) {
                    qtyToDeliver = OB.DEC.add(qtyToDeliver, l.getDeliveredQuantity());
                  } else {
                    // If the related product already has deliveries
                    if (!qtyToDeliver && l.getDeliveredQuantity()) {
                      qtyToDeliver = OB.DEC.One;
                    }
                  }
                }
              } else {
                if (relatedLine.obposIspaid) {
                  if (line.get('product').get('quantityRule') === 'PP' && line.get('product').get('groupProduct')) {
                    qtyToDeliver = OB.DEC.add(qtyToDeliver, relatedLine.deliveredQuantity);
                  } else if (relatedLine.deliveredQuantity) {
                    qtyToDeliver = OB.DEC.One;
                  }
                }
              }
            });

            if (qtyToDeliver) {
              line.set('obposCanbedelivered', true);
            }

            line.set('obposQtytodeliver', qtyToDeliver);
          } else if (line.get('product').get('productType') === 'S' && !line.get('product').get('isLinkedToProduct')) {
            line.set('obposQtytodeliver', line.getDeliveredQuantity());
          }
        } else {
          line.set('obposQtytodeliver', line.getDeliveredQuantity());
          if (line.get('product').get('productType') === 'S' && line.get('product').get('isLinkedToProduct')) {
            if (_.find(line.get('relatedLines'), function (relatedLine) {
              return relatedLine.deferred;
            })) {
              isDeferredService = true;
            }
          }
        }
        deliverNotServiceLine = line.get('product').get('productType') !== 'S' && line.get('obposQtytodeliver') > line.getDeliveredQuantity();
        deliverNegativeLine = line.get('qty') < 0;
        deliverDeferredServiceLine = isDeferredService && line.get('obposQtytodeliver') > line.getDeliveredQuantity();
        // Set the 'deliver' property, which tells if the order will be fully delivered
        if (deliver && (line.get('product').get('productType') !== 'S' || isDeferredService) && line.get('obposQtytodeliver') < line.get('qty')) {
          deliver = false;
        }
        // Set the 'generateShipment' property, which tells if a shipment will be created in this order
        if (!generateShipment && (deliverNotServiceLine || deliverNegativeLine || deliverDeferredServiceLine)) {
          generateShipment = true;
        }
        line.set('isDeferredService', isDeferredService);
      });
      // Deliver Independent Services if the rest of the products of the ticket are delivered
      if (deliver) {
        _.each(lines.models, function (line) {
          if (line.get('product').get('productType') === 'S' && !line.get('product').get('isLinkedToProduct')) {
            line.set('obposQtytodeliver', line.get('qty'));
            generateShipment = true;
          }
        });
      }
      model.set('deliver', deliver);
      model.set('generateShipment', generateShipment);

      OB.UTIL.HookManager.callbackExecutor(args, callbacks);
    });

  }
}());