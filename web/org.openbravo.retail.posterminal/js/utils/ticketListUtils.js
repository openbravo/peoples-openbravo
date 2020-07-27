/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/*global OB, _,moment, Backbone, enyo, BigDecimal*/

(function() {
  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.TicketListUtils = OB.UTIL.TicketListUtils || {};

  const triggerTicketLoadEvents = () => {
    OB.MobileApp.model.receipt.trigger('updateView');
    OB.MobileApp.model.receipt.trigger('change');
    OB.MobileApp.model.receipt.trigger('clear');
    OB.MobileApp.model.receipt.trigger('paintTaxes');
    OB.MobileApp.model.receipt.trigger('updatePending');
  };

  OB.UTIL.TicketListUtils.loadStateTicket = async function(ticket) {
    await OB.App.State.Global.loadTicket({
      ticket
    });
    OB.MobileApp.model.set(
      'terminalLogContext',
      OB.App.State.getState().Ticket.id
    );
    triggerTicketLoadEvents();
  };

  OB.UTIL.TicketListUtils.loadTicket = async function(ticketModel) {
    await OB.UTIL.TicketListUtils.loadStateTicket(
      JSON.parse(JSON.stringify(ticketModel.toJSON()))
    );
    triggerTicketLoadEvents();
  };

  OB.UTIL.TicketListUtils.loadTicketById = async function(ticketId) {
    await OB.App.State.Global.loadTicketById({
      id: ticketId
    });
    OB.MobileApp.model.set(
      'terminalLogContext',
      OB.App.State.getState().Ticket.id
    );
    triggerTicketLoadEvents();
  };

  const newOrder = function(bp, propertiesToReset) {
    var order = new OB.Model.Order(),
      i;
    bp = bp ? bp : OB.MobileApp.model.get('businessPartner');

    if (propertiesToReset && _.isArray(propertiesToReset)) {
      for (i = 0; i < propertiesToReset.length; i++) {
        if (!OB.UTIL.isNullOrUndefined(propertiesToReset[i].defaultValue)) {
          order.set(
            propertiesToReset[i].propertyName,
            propertiesToReset[i].defaultValue
          );
        } else {
          order.set(propertiesToReset[i].propertyName, '');
        }
      }
    }

    order.set('client', OB.MobileApp.model.get('terminal').client);
    order.set('organization', OB.MobileApp.model.get('terminal').organization);
    order.set(
      'organizationAddressIdentifier',
      OB.MobileApp.model.get('terminal').organizationAddressIdentifier
    );
    order.set(
      'trxOrganization',
      OB.MobileApp.model.get('terminal').organization
    );
    order.set('createdBy', OB.MobileApp.model.get('orgUserId'));
    order.set('updatedBy', OB.MobileApp.model.get('orgUserId'));
    order.set(
      'documentType',
      OB.MobileApp.model.get('terminal').terminalType.documentType
    );
    order.set(
      'orderType',
      OB.MobileApp.model.get('terminal').terminalType.layawayorder ? 2 : 0
    ); // 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
    order.setFullInvoice(false);
    order.set('isQuotation', false);
    order.set('oldId', null);
    order.set('session', OB.MobileApp.model.get('session'));
    order.set('cashVAT', OB.MobileApp.model.get('terminal').cashVat);
    order.set('bp', bp);
    order.set('externalBusinessPartner', null);
    order.set('externalBusinessPartnerReference', null);
    order.set('invoiceTerms', bp.get('invoiceTerms'));
    if (OB.MobileApp.model.hasPermission('EnableMultiPriceList', true)) {
      // Set price list for order
      order.set('priceList', bp.get('priceList'));
      var priceIncludesTax = bp.get('priceIncludesTax');
      if (OB.UTIL.isNullOrUndefined(priceIncludesTax)) {
        priceIncludesTax = OB.MobileApp.model.get('pricelist').priceIncludesTax;
      }
      order.set('priceIncludesTax', priceIncludesTax);
    } else {
      order.set('priceList', OB.MobileApp.model.get('terminal').priceList);
      order.set(
        'priceIncludesTax',
        OB.MobileApp.model.get('pricelist').priceIncludesTax
      );
    }
    order.set('currency', OB.MobileApp.model.get('terminal').currency);
    order.set(
      'currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
      OB.MobileApp.model.get('terminal')[
        'currency' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER
      ]
    );
    order.set('warehouse', OB.MobileApp.model.get('terminal').warehouse);
    if (OB.MobileApp.model.get('context').user.isSalesRepresentative) {
      order.set(
        'salesRepresentative',
        OB.MobileApp.model.get('context').user.id
      );
      order.set(
        'salesRepresentative' +
          OB.Constants.FIELDSEPARATOR +
          OB.Constants.IDENTIFIER,
        OB.MobileApp.model.get('context').user._identifier
      );
    } else {
      order.set('salesRepresentative', null);
      order.set(
        'salesRepresentative' +
          OB.Constants.FIELDSEPARATOR +
          OB.Constants.IDENTIFIER,
        null
      );
    }
    order.set('posTerminal', OB.MobileApp.model.get('terminal').id);
    order.set(
      'posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER,
      OB.MobileApp.model.get('terminal')._identifier
    );
    order.set('orderDate', OB.I18N.normalizeDate(new Date()));
    order.set('creationDate', null);
    order.set('isPaid', false);
    order.set('creditAmount', OB.DEC.Zero);
    order.set('paidPartiallyOnCredit', false);
    order.set('paidOnCredit', false);
    order.set('isLayaway', false);
    order.set('isPartiallyDelivered', false);
    order.set('taxes', {});
    order.set('print', true);
    order.set('sendEmail', false);
    order.set('openDrawer', false);
    order.set(
      'orderManualPromotions',
      new OB.Collection.OrderManualPromotionsList()
    );
    if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)) {
      order.set('obrdmDeliveryModeProperty', 'PickAndCarry');
    }

    OB.UTIL.HookManager.executeHooks('OBPOS_NewReceipt', {
      newOrder: order
    });
    return order;
  };
  OB.UTIL.TicketListUtils.newOrder = function(bp) {
    var i,
      p,
      receiptProperties,
      propertiesToReset = [];
    // reset in new order properties defined in Receipt Properties dialog
    if (
      OB.MobileApp.view.$.containerWindow &&
      OB.MobileApp.view.$.containerWindow.getRoot() &&
      OB.MobileApp.view.$.containerWindow.getRoot().$.receiptPropertiesDialog
    ) {
      receiptProperties = OB.MobileApp.view.$.containerWindow.getRoot().$
        .receiptPropertiesDialog.newAttributes;
      for (i = 0; i < receiptProperties.length; i++) {
        if (receiptProperties[i].modelProperty) {
          var properties = {
            propertyName: receiptProperties[i].modelProperty
          };
          if (!OB.UTIL.isNullOrUndefined(receiptProperties[i].defaultValue)) {
            properties.defaultValue = receiptProperties[i].defaultValue;
          }
          propertiesToReset.push(properties);
        }
        if (receiptProperties[i].extraProperties) {
          for (p = 0; p < receiptProperties[i].extraProperties.length; p++) {
            propertiesToReset.push({
              propertyName: receiptProperties[i].extraProperties[p]
            });
          }
        }
      }
    }
    return newOrder(bp, propertiesToReset);
  };

  OB.UTIL.TicketListUtils.loadExternalCustomer = function(
    externalBpReference,
    callback
  ) {
    OB.App.ExternalBusinessPartnerAPI.getBusinessPartner(
      externalBpReference
    ).then(bp => {
      callback(bp);
    });
  };

  OB.UTIL.TicketListUtils.loadCustomer = function(model, callback) {
    var bpId,
      bpLocId,
      bpBillLocId,
      bpLoc,
      bpBillLoc,
      loadBusinesPartner,
      loadLocations,
      finalCallback,
      isLoadedPartiallyFromBackend = false;

    bpId = model.bpId;
    bpLocId = model.bpLocId;
    bpBillLocId = model.bpBillLocId || model.bpLocId;

    finalCallback = function(bp, bpLoc, bpBillLoc) {
      bp.set('locations', bp.get('locations') || []);
      bp.set('shipLocId', bpLoc.get('id'));
      bp.set('shipLocName', bpLoc.get('name'));
      bp.set('shipPostalCode', bpLoc.get('postalCode'));
      bp.set('shipCityName', bpLoc.get('cityName'));
      bp.set('shipCountryId', bpLoc.get('countryId'));
      bp.set('shipCountryName', bpLoc.get('countryName'));
      bp.set('shipRegionId', bpLoc.get('regionId'));
      bp.set('locId', (bpBillLoc || bpLoc).get('id'));
      bp.set('locName', (bpBillLoc || bpLoc).get('name'));
      bp.set('postalCode', (bpBillLoc || bpLoc).get('postalCode'));
      bp.set('cityName', (bpBillLoc || bpLoc).get('cityName'));
      bp.set('countryName', (bpBillLoc || bpLoc).get('countryName'));
      bp.set('locationModel', bpLoc);
      bp.get('locations').push(bpLoc);
      if (bpBillLoc) {
        bp.set('locationBillModel', bpBillLoc);
        bp.get('locations').push(bpBillLoc);
      }
      callback(bp, bpLoc, bpBillLoc);
    };

    loadBusinesPartner = function(
      bpartnerId,
      bpLocationId,
      bpBillLocationId,
      callback
    ) {
      var loadCustomerParameters = {
        bpartnerId: bpartnerId,
        bpLocationId: bpLocationId
      };
      if (bpLocationId !== bpBillLocationId) {
        loadCustomerParameters.bpBillLocationId = bpBillLocationId;
      }
      new OB.DS.Request(
        'org.openbravo.retail.posterminal.master.LoadedCustomer'
      ).exec(
        loadCustomerParameters,
        function(data) {
          if (data.length >= 2) {
            isLoadedPartiallyFromBackend = true;
            callback({
              bpartner: OB.Dal.transform(OB.Model.BusinessPartner, data[0]),
              bpLoc: OB.Dal.transform(OB.Model.BPLocation, data[1]),
              bpBillLoc:
                bpLocationId !== bpBillLocationId
                  ? OB.Dal.transform(OB.Model.BPLocation, data[2])
                  : null
            });
          } else {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_InformationTitle'),
              OB.I18N.getLabel('OBPOS_NoCustomerForPaidReceipt'),
              [
                {
                  label: OB.I18N.getLabel('OBPOS_LblOk'),
                  isConfirmButton: true
                }
              ]
            );
          }
        },
        function() {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_InformationTitle'),
            OB.I18N.getLabel('OBPOS_NoCustomerForPaidReceipt'),
            [
              {
                label: OB.I18N.getLabel('OBPOS_LblOk'),
                isConfirmButton: true
              }
            ]
          );
        }
      );
    };

    loadLocations = function(bp) {
      if (bpLocId === bpBillLocId) {
        if (isLoadedPartiallyFromBackend) {
          finalCallback(bp, bpLoc, null);
        } else {
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
            OB.Dal.get(
              OB.Model.BPLocation,
              bpLocId,
              function(bpLoc) {
                finalCallback(bp, bpLoc, null);
              },
              function(tx, error) {
                OB.UTIL.showError(error);
              },
              function() {
                loadBusinesPartner(bpId, bpLocId, bpBillLocId, function(data) {
                  finalCallback(bp, data.bpLoc, null);
                });
              }
            );
          } else {
            OB.App.MasterdataModels.BusinessPartnerLocation.withId(bpLocId)
              .then(bPLocation => {
                if (bPLocation) {
                  let bpLoc = OB.Dal.transform(OB.Model.BPLocation, bPLocation);
                  finalCallback(bp, bpLoc, null);
                } else {
                  loadBusinesPartner(bpId, bpLocId, bpBillLocId, function(
                    data
                  ) {
                    finalCallback(bp, data.bpLoc, null);
                  });
                }
              })
              .catch(error => {
                OB.error(error);
              });
          }
        }
      } else {
        if (
          isLoadedPartiallyFromBackend &&
          !OB.UTIL.isNullOrUndefined(bpLoc) &&
          !OB.UTIL.isNullOrUndefined(bpBillLoc)
        ) {
          finalCallback(bp, bpLoc, bpBillLoc);
        } else {
          var criteria;
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
            var remoteCriteria = [
              {
                columns: ['id'],
                operator: 'equals',
                value: [bpLocId, bpBillLocId]
              }
            ];
            criteria = {};
            criteria.remoteFilters = remoteCriteria;
            OB.Dal.find(
              OB.Model.BPLocation,
              criteria,
              function(locations) {
                if (locations.models.length === 2) {
                  _.each(locations.models, function(l) {
                    if (l.id === bpLocId) {
                      bpLoc = l;
                    } else if (l.id === bpBillLocId) {
                      bpBillLoc = l;
                    }
                  });
                  finalCallback(bp, bpLoc, bpBillLoc);
                } else {
                  loadBusinesPartner(bpId, bpLocId, bpBillLocId, function(
                    data
                  ) {
                    finalCallback(bp, data.bpLoc, data.bpBillLoc);
                  });
                }
              },
              function(tx, error) {
                OB.UTIL.showError(error);
              },
              bpLoc
            );
          } else {
            criteria = new OB.App.Class.Criteria();
            criteria.criterion('id', [bpLocId, bpBillLocId], 'in');
            OB.App.MasterdataModels.BusinessPartnerLocation.find(
              criteria.build()
            )
              .then(bPLocations => {
                let locations = [];
                for (let i = 0; i < bPLocations.length; i++) {
                  locations.push(
                    OB.Dal.transform(OB.Model.BPLocation, bPLocations[i])
                  );
                }
                if (locations.length === 2) {
                  for (const l of locations) {
                    if (l.id === bpLocId) {
                      bpLoc = l;
                    } else if (l.id === bpBillLocId) {
                      bpBillLoc = l;
                    }
                  }
                  finalCallback(bp, bpLoc, bpBillLoc);
                } else {
                  loadBusinesPartner(bpId, bpLocId, bpBillLocId, function(
                    data
                  ) {
                    finalCallback(bp, data.bpLoc, data.bpBillLoc);
                  });
                }
              })
              .catch(error => {
                OB.UTIL.showError(error);
              });
          }
        }
      }
    };
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      OB.Dal.get(
        OB.Model.BusinessPartner,
        bpId,
        function(bp) {
          loadLocations(bp);
        },
        null,
        function() {
          //Empty
          loadBusinesPartner(bpId, bpLocId, bpBillLocId, function(data) {
            bpLoc = data.bpLoc;
            if (bpLocId !== bpBillLocId) {
              bpBillLoc = data.bpBillLoc;
            }
            loadLocations(data.bpartner);
          });
        }
      );
    } else {
      OB.App.MasterdataModels.BusinessPartner.withId(bpId)
        .then(bp => {
          if (bp !== undefined) {
            loadLocations(OB.Dal.transform(OB.Model.BusinessPartner, bp));
          } else {
            loadBusinesPartner(bpId, bpLocId, bpBillLocId, function(data) {
              bpLoc = data.bpLoc;
              if (bpLocId !== bpBillLocId) {
                bpBillLoc = data.bpBillLoc;
              }
              loadLocations(data.bpartner);
            });
          }
        })
        .catch(error => {
          OB.error(error);
        });
    }
  };
  OB.UTIL.TicketListUtils.newPaidReceipt = async function(model, callback) {
    var order = new OB.Model.Order(),
      lines,
      newline,
      payments,
      curPayment,
      taxes,
      numberOfLines = model.receiptLines.length,
      orderQty = 0,
      NoFoundProduct = true,
      execution = OB.UTIL.ProcessController.start('newPaidReceipt');

    // Each payment that has been reverted stores the id of the reversal payment
    // Web POS, instead of that, need to have the information of the payment reverted on the reversal payment
    // This loop switches the information between them
    _.each(
      _.filter(model.receiptPayments, function(payment) {
        return payment.isReversed;
      }),
      function(payment) {
        var reversalPayment = _.find(model.receiptPayments, function(
          currentPayment
        ) {
          return currentPayment.paymentId === payment.reversedPaymentId;
        });
        reversalPayment.reversedPaymentId = payment.paymentId;
        reversalPayment.isReversePayment = true;
        delete payment.reversedPaymentId;
      }
    );

    // Call orderLoader plugings to adjust remote model to local model first
    // ej: sales on credit: Add a new payment if total payment < total receipt
    // ej: gift cards: Add a new payment for each gift card discount
    _.each(OB.Model.modelLoaders, function(f) {
      f(model);
    });

    //model.set('id', null);
    lines = new Backbone.Collection();

    // set all properties coming from the model
    order.set(model);

    // setting specific properties
    order.set('isbeingprocessed', 'N');
    order.set('hasbeenpaid', 'N');
    order.set('isEditable', false);
    order.set('isModified', false);
    order.set('checked', model.checked); //TODO: what is this for, where it comes from?
    order.set('orderDate', OB.I18N.normalizeDate(model.orderDate));
    order.set('creationDate', OB.I18N.normalizeDate(model.creationDate));
    order.set('updatedBy', OB.MobileApp.model.usermodel.id);
    order.set('paidPartiallyOnCredit', false);
    order.set('paidOnCredit', false);
    order.set('session', OB.MobileApp.model.get('session'));
    order.set('skipApplyPromotions', true);
    if (model.isQuotation) {
      order.set('isQuotation', true);
      order.set('oldId', model.orderid);
      order.set('id', null);
      order.set(
        'documentType',
        OB.MobileApp.model.get('terminal').terminalType
          .documentTypeForQuotations
      );
      order.set('hasbeenpaid', 'Y');
      // TODO: this commented lines are kept just in case this issue happens again
      // Set creationDate milliseconds to 0, if the date is with milisecond, the date with miliseconds is rounded to seconds:
      // so, the second can change, and the creationDate in quotation should not be changed when quotation is reactivated
      // order.set('creationDate', moment(model.creationDate.toString(), "YYYY-MM-DD hh:m:ss").toDate());
    }
    if (model.isLayaway) {
      order.set('isLayaway', true);
      order.set('id', model.orderid);
      order.set('hasbeenpaid', 'N');
    } else {
      order.set('isPaid', true);
      var paidByPayments = OB.DEC.Zero;
      _.each(model.receiptPayments, function(receiptPayment) {
        paidByPayments = OB.DEC.add(
          paidByPayments,
          OB.DEC.mul(receiptPayment.amount, receiptPayment.rate)
        );
      });

      var creditAmount = OB.DEC.sub(model.totalamount, paidByPayments);
      if (
        OB.DEC.compare(model.totalamount) > 0 &&
        OB.DEC.compare(creditAmount) > 0 &&
        !model.isQuotation
      ) {
        order.set('creditAmount', creditAmount);
        if (paidByPayments) {
          order.set('paidPartiallyOnCredit', true);
        }
        order.set('paidOnCredit', true);
      }
      order.set('id', model.orderid);
      if (
        order.get('documentType') ===
        OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns
      ) {
        //It's a return
        order.set('orderType', 1);
      }
    }
    let loadProducts = async function() {
      var linepos = 0,
        hasDeliveredProducts = false,
        hasNotDeliveredProducts = false,
        i,
        sortedPayments = false;

      function getReverserPayment(payment, Payments) {
        return _.filter(model.receiptPayments, function(receiptPayment) {
          return receiptPayment.paymentId === payment.reversedPaymentId;
        })[0];
      }

      i = 0;
      // Sort payments array, puting reverser payments inmediatly after their reversed payment
      while (i < model.receiptPayments.length) {
        var payment = model.receiptPayments[i];
        if (payment.reversedPaymentId && !payment.isSorted) {
          var reversed_index = model.receiptPayments.indexOf(
            getReverserPayment(payment, model.receiptPayments)
          );
          payment.isSorted = true;
          if (i < reversed_index) {
            model.receiptPayments.splice(i, 1);
            model.receiptPayments.splice(reversed_index, 0, payment);
            sortedPayments = true;
          } else if (i > reversed_index + 1) {
            model.receiptPayments.splice(i, 1);
            model.receiptPayments.splice(reversed_index + 1, 0, payment);
            sortedPayments = true;
          }
        } else {
          i++;
        }
      }
      if (sortedPayments) {
        model.receiptPayments.forEach(function(receitPayment) {
          if (receitPayment.isSorted) {
            delete receitPayment.isSorted;
          }
        });
      }
      //order.set('payments', model.receiptPayments);
      payments = new Backbone.Collection();
      _.each(model.receiptPayments, function(iter) {
        var paymentProp;
        curPayment = new OB.Model.PaymentLine();
        for (paymentProp in iter) {
          if (iter.hasOwnProperty(paymentProp)) {
            if (paymentProp === 'paymentDate') {
              if (
                !OB.UTIL.isNullOrUndefined(iter[paymentProp]) &&
                moment(iter[paymentProp]).isValid()
              ) {
                curPayment.set(
                  paymentProp,
                  OB.I18N.normalizeDate(new Date(iter[paymentProp]))
                );
              } else {
                curPayment.set(paymentProp, null);
              }
            } else {
              curPayment.set(paymentProp, iter[paymentProp]);
            }
          }
        }
        curPayment.set('orderGross', order.get('gross'));
        curPayment.set('isPaid', order.get('isPaid'));
        curPayment.set('date', new Date(iter.paymentDate));
        payments.add(curPayment);
      });
      order.set('payments', payments);
      order.adjustPayment();

      taxes = {};
      _.each(model.receiptTaxes, function(iter) {
        var taxProp;
        taxes[iter.taxid] = {};
        for (taxProp in iter) {
          if (iter.hasOwnProperty(taxProp)) {
            taxes[iter.taxid][taxProp] = iter[taxProp];
          }
        }
      });
      order.set('taxes', taxes);

      if (!model.isLayaway && !model.isQuotation) {
        if (model.totalamount > 0 && order.get('payment') < model.totalamount) {
          order.set('paidOnCredit', true);
        } else if (
          model.totalamount < 0 &&
          (order.get('payment') === 0 ||
            OB.DEC.abs(model.totalamount) > order.get('payment'))
        ) {
          order.set('paidOnCredit', true);
        }
      }
      if (model.receiptLines.length === 0) {
        order.set('json', JSON.stringify(order.toJSON()));
        callback(order);
        OB.UTIL.ProcessController.finish('newPaidReceipt', execution);
      }

      for (let i = 0; i < model.receiptLines.length; i++) {
        let iter = model.receiptLines[i];
        var price = order.get('priceIncludesTax')
            ? OB.DEC.number(iter.baseGrossUnitPrice)
            : OB.DEC.number(iter.baseNetUnitPrice),
          lineGross = order.get('priceIncludesTax')
            ? OB.DEC.number(iter.lineGrossAmount)
            : null,
          lineNet = order.get('priceIncludesTax')
            ? null
            : OB.DEC.number(iter.lineNetAmount);
        iter.linepos = linepos;
        var addLineForProduct = async function(prod) {
          // Set product services
          await order._loadRelatedServices(
            prod.get('productType'),
            prod.get('id'),
            prod.get('productCategory'),
            async function(data) {
              let hasservices;
              if (
                !OB.UTIL.isNullOrUndefined(data) &&
                OB.DEC.number(iter.quantity) > 0
              ) {
                hasservices = data.hasservices;
              }

              for (let promotion of iter.promotions) {
                try {
                  const discount = OB.Discounts.Pos.manualRuleImpls.find(
                    discount => discount.id === promotion.ruleId
                  );
                  if (
                    discount &&
                    OB.Discounts.Pos.getManualPromotions().includes(
                      discount.discountType
                    )
                  ) {
                    var percentage;
                    if (discount.obdiscPercentage) {
                      percentage = OB.DEC.mul(
                        OB.DEC.div(promotion.amt, iter.lineGrossAmount),
                        new BigDecimal('100')
                      );
                    }
                    promotion.userAmt = percentage ? percentage : promotion.amt;
                    promotion.discountType = discount.discountType;
                    promotion.manual = true;
                  }
                } catch (error) {
                  OB.UTIL.showError(error);
                }
              }

              if (
                OB.MobileApp.model.hasPermission(
                  'OBPOS_EnableSupportForProductAttributes',
                  true
                )
              ) {
                if (iter.attributeValue && _.isString(iter.attributeValue)) {
                  var processedAttValues = OB.UTIL.AttributeUtils.generateDescriptionBasedOnJson(
                    iter.attributeValue
                  );
                  if (
                    processedAttValues &&
                    processedAttValues.keyValue &&
                    _.isArray(processedAttValues.keyValue) &&
                    processedAttValues.keyValue.length > 0
                  ) {
                    iter.attSetInstanceDesc = processedAttValues.description;
                  }
                }
              }
              newline = new OB.Model.OrderLine({
                id: iter.lineId,
                product: prod,
                uOM: iter.uOM,
                qty: OB.DEC.number(
                  iter.quantity,
                  prod.get('uOMstandardPrecision')
                ),
                price: price,
                unitPrice: iter.unitPrice,
                priceList: order.get('priceIncludesTax')
                  ? OB.DEC.number(iter.grossListPrice)
                  : OB.DEC.number(iter.listPrice),
                net: lineNet,
                gross: lineGross,
                promotions: iter.promotions,
                description: iter.description,
                priceIncludesTax: order.get('priceIncludesTax'),
                hasRelatedServices: hasservices,
                attributeValue: iter.attributeValue,
                warehouse: {
                  id: iter.warehouse,
                  warehousename: iter.warehousename
                },
                relatedLines: iter.relatedLines,
                groupService: prod.get('groupProduct'),
                isEditable: true,
                isDeletable: true,
                attSetInstanceDesc: iter.attSetInstanceDesc
                  ? iter.attSetInstanceDesc
                  : null,
                lineGrossAmount: iter.lineGrossAmount,
                country:
                  iter.obrdmDeliveryMode === 'HomeDelivery'
                    ? order.get('bp').get('shipLocId')
                      ? order
                          .get('bp')
                          .get('locationModel')
                          .get('countryId')
                      : null
                    : iter.organization
                    ? iter.organization.country
                    : OB.MobileApp.model.get('terminal').organizationCountryId,
                region:
                  iter.obrdmDeliveryMode === 'HomeDelivery'
                    ? order.get('bp').get('shipLocId')
                      ? order
                          .get('bp')
                          .get('locationModel')
                          .get('regionId')
                      : null
                    : iter.organization
                    ? iter.organization.region
                    : OB.MobileApp.model.get('terminal').organizationRegionId
              });

              // copy verbatim not owned properties -> modular properties.
              _.each(iter, function(value, key) {
                if (!newline.ownProperties[key]) {
                  newline.set(key, value);
                }
              });

              // add the created line
              lines.add(newline, {
                at: iter.linepos
              });
              numberOfLines--;
              orderQty = OB.DEC.add(iter.quantity, orderQty);
              if (numberOfLines === 0) {
                lines.reset(
                  lines.sortBy(function(line) {
                    return line.get('linepos');
                  })
                );
                order.set('lines', lines);
                order.set('qty', orderQty);
                order.set(
                  'isPartiallyDelivered',
                  hasDeliveredProducts && hasNotDeliveredProducts ? true : false
                );
                if (hasDeliveredProducts && !hasNotDeliveredProducts) {
                  order.set('isFullyDelivered', true);
                }
                if (order.get('isPartiallyDelivered')) {
                  var partiallyPaid = 0;
                  _.each(
                    _.filter(order.get('receiptLines'), function(reciptLine) {
                      return reciptLine.deliveredQuantity;
                    }),
                    function(deliveredLine) {
                      partiallyPaid = OB.DEC.add(
                        partiallyPaid,
                        OB.DEC.mul(
                          deliveredLine.deliveredQuantity,
                          deliveredLine.grossUnitPrice
                        )
                      );
                    }
                  );
                  order.set('deliveredQuantityAmount', partiallyPaid);
                  if (
                    order.get('deliveredQuantityAmount') &&
                    order.get('deliveredQuantityAmount') > order.get('payment')
                  ) {
                    order.set('isDeliveredGreaterThanGross', true);
                  }
                }
                order.set('json', JSON.stringify(order.toJSON()));
                callback(order);
                OB.UTIL.ProcessController.finish('newPaidReceipt', execution);
              }
            }
          );
        };

        if (!iter.deliveredQuantity) {
          hasNotDeliveredProducts = true;
        } else {
          hasDeliveredProducts = true;
          if (iter.deliveredQuantity < iter.quantity) {
            hasNotDeliveredProducts = true;
          }
        }

        if (iter.relatedLines && !order.get('hasServices')) {
          order.set('hasServices', true);
        }
        try {
          const product = await OB.App.MasterdataModels.Product.withId(iter.id);
          if (product) {
            await addLineForProduct(
              OB.Dal.transform(OB.Model.Product, product)
            );
          } else {
            //Empty
            const body = {
              productId: iter.id,
              salesOrderLineId: iter.lineId
            };
            try {
              let data = await OB.App.Request.mobileServiceRequest(
                'org.openbravo.retail.posterminal.master.LoadedProduct',
                body
              );
              data = data.response.data;
              await addLineForProduct(
                OB.Dal.transform(OB.Model.Product, data[0])
              );
            } catch (error) {
              if (NoFoundProduct) {
                NoFoundProduct = false;
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBPOS_InformationTitle'),
                  OB.I18N.getLabel('OBPOS_NoReceiptLoadedText'),
                  [
                    {
                      label: OB.I18N.getLabel('OBPOS_LblOk'),
                      isConfirmButton: true
                    }
                  ]
                );
              }
            }
          }
        } catch (error) {
          OB.error(error.message);
        }
        linepos++;
      }
    };

    async function callToLoadCustomer() {
      await OB.UTIL.TicketListUtils.loadCustomer(
        {
          bpId: model.bp,
          bpLocId: model.bpLocId,
          bpBillLocId: model.bpBillLocId || model.bpLocId
        },
        async function(bp, loc, billLoc) {
          order.set({
            bp: bp
          });
          order.set('gross', model.totalamount);
          order.set('net', model.totalNetAmount);
          order.trigger('change:bp', order);
          await loadProducts();
        }
      );
    }

    if (OB.UTIL.isNotEmptyString(model.externalBusinessPartnerReference)) {
      OB.UTIL.TicketListUtils.loadExternalCustomer(
        model.externalBusinessPartnerReference,
        function(extBp) {
          order.set('externalBusinessPartner', extBp.getPlainObject());
          order.set(
            'externalBusinessPartnerReference',
            model.externalBusinessPartnerReference
          );
          callToLoadCustomer.call();
        }
      );
    } else {
      callToLoadCustomer.call();
    }
  };

  const loadCurrent = function(isNew) {
    if (isNew) {
      OB.MobileApp.model.receipt.trigger(
        'beforeChangeOrderForNewOne',
        OB.MobileApp.model.receipt
      );
    }
    triggerTicketLoadEvents();
  };

  OB.UTIL.TicketListUtils.addPaidReceipt = function(model, callback) {
    let execution = OB.UTIL.ProcessController.start('addPaidReceipt');

    function executeFinalCallback() {
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PostAddPaidReceipt',
        {
          order: model
        },
        function(args) {
          if (callback instanceof Function) {
            callback(OB.MobileApp.model.receipt);
          }
        }
      );
    }

    OB.MobileApp.model.receipt.trigger('updateView');
    const clonedTicket = JSON.parse(
      JSON.stringify(OB.App.State.getState().Ticket)
    );
    OB.MobileApp.model.receipt.clearWith(model);
    OB.App.State.TicketList.saveTicket(clonedTicket).then(() => {
      loadCurrent(true);
      OB.UTIL.ProcessController.finish('addPaidReceipt', execution);
      executeFinalCallback();
    });
  };

  OB.UTIL.TicketListUtils.addNewQuotation = function() {
    OB.MobileApp.model.receipt.trigger('updateView');
    OB.App.State.Global.addNewQuotation();
    loadCurrent();
  };

  OB.UTIL.TicketListUtils.checkOrderListPayment = function() {
    if (
      OB.App.State.Ticket.Utils.checkTicketPayments(
        OB.App.State.getState().Ticket
      )
    ) {
      return true;
    } else {
      return OB.App.State.getState().TicketList.tickets.reduce(
        (accum, ticket) =>
          accum || OB.App.State.Ticket.Utils.checkTicketPayments(ticket),
        false
      );
    }
  };

  OB.UTIL.TicketListUtils.checkForDuplicateReceipts = function(
    model,
    callback,
    errorCallback,
    calledFrom
  ) {
    function openReceiptPermissionError(orderType) {
      if (calledFrom === 'orderSelector' || calledFrom === 'return') {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          OB.I18N.getLabel('OBPOS_OpenReceiptPermissionError', [orderType])
        );
      } else {
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_OpenReceiptPermissionError', [orderType])
        );
      }
      if (errorCallback) {
        errorCallback();
      }
    }

    //Check Permissions
    switch (model.get('orderType')) {
      case 'QT':
        if (!OB.MobileApp.model.hasPermission('OBPOS_retail.quotations')) {
          openReceiptPermissionError(OB.I18N.getLabel('OBPOS_Quotations'));
          return;
        }
        break;
      case 'LAY':
        if (!OB.MobileApp.model.hasPermission('OBPOS_retail.layaways')) {
          openReceiptPermissionError(OB.I18N.getLabel('OBPOS_LblLayaways'));
          return;
        }
        break;
      default:
        if (!OB.MobileApp.model.hasPermission('OBPOS_retail.paidReceipts')) {
          openReceiptPermissionError(OB.I18N.getLabel('OBPOS_LblPaidReceipts'));
          return;
        }
        break;
    }

    var orderTypeMsg,
      i,
      showErrorMessage = function(errorMsg) {
        if (calledFrom === 'orderSelector' || calledFrom === 'return') {
          OB.POS.terminal.$.containerWindow.getRoot().doShowPopup({
            popup: 'OB_UI_MessageDialog',
            args: {
              message: errorMsg
            }
          });
        } else {
          OB.UTIL.showWarning(errorMsg);
        }
        if (errorCallback) {
          errorCallback();
        }
      };

    // Check in Current Session
    const openTicketList = OB.App.OpenTicketList.getAllTickets();
    for (i = 0; i < openTicketList.length; i++) {
      const modelAtIndex = openTicketList[i];
      if (
        modelAtIndex.id === model.get('id') ||
        (!_.isNull(modelAtIndex.oldId) &&
          modelAtIndex.oldId === model.get('id')) ||
        (modelAtIndex.canceledorder &&
          modelAtIndex.canceledorder.id === model.get('id'))
      ) {
        var errorMsg;
        orderTypeMsg = OB.I18N.getLabel('OBPOS_ticket');
        errorMsg = enyo.format(
          OB.I18N.getLabel('OBPOS_ticketAlreadyOpened'),
          orderTypeMsg,
          modelAtIndex.documentNo
        );
        if (modelAtIndex.isLayaway) {
          orderTypeMsg = OB.I18N.getLabel('OBPOS_LblLayaway');
          errorMsg = enyo.format(
            OB.I18N.getLabel('OBPOS_ticketAlreadyOpened'),
            orderTypeMsg,
            modelAtIndex.documentNo
          );
        } else if (modelAtIndex.isQuotation) {
          orderTypeMsg = OB.I18N.getLabel('OBPOS_Quotation');
          errorMsg = enyo.format(
            OB.I18N.getLabel('OBPOS_ticketAlreadyOpened'),
            orderTypeMsg,
            modelAtIndex.documentNo
          );
        } else if (
          !_.isNull(modelAtIndex.oldId) &&
          modelAtIndex.oldId === model.get('id')
        ) {
          var SoFromQtDocNo = modelAtIndex.documentNo;
          var QtDocumentNo = model.get('documentNo');
          errorMsg = OB.I18N.getLabel(
            'OBPOS_OrderAssociatedToQuotationInProgress',
            [QtDocumentNo, SoFromQtDocNo, QtDocumentNo, SoFromQtDocNo]
          );
        }
        showErrorMessage(errorMsg);
        if (
          OB.MobileApp.model.receipt.get('documentNo') !==
          model.get('documentNo')
        ) {
          OB.UTIL.TicketListUtils.loadStateTicket(modelAtIndex);
        }
        if (model.get('searchSynchId')) {
          model.unset('searchSynchId');
        }
        return true;
      }
    }

    // Check in Other Session
    const ordersNotProcessed = OB.App.OpenTicketList.getAllTickets().filter(
      ticket => ticket.hasbeenpaid === 'N'
    );

    if (ordersNotProcessed.length > 0) {
      var existingOrder = _.find(ordersNotProcessed.models, function(order) {
        return (
          order.get('id') === model.get('id') ||
          order.get('oldId') === model.get('id') ||
          (order.get('canceledorder') &&
            order.get('canceledorder').get('id') === model.get('id'))
        );
      });
      if (existingOrder) {
        orderTypeMsg = OB.I18N.getLabel('OBPOS_ticket');
        if (existingOrder.get('isLayaway')) {
          orderTypeMsg = OB.I18N.getLabel('OBPOS_LblLayaway');
        } else if (existingOrder.get('isQuotation')) {
          orderTypeMsg = OB.I18N.getLabel('OBPOS_Quotation');
        }
        // Getting Other Session User's username

        OB.App.OfflineSession.sessionWithId(existingOrder.get('session'))
          .then(session => {
            if (!session) {
              return null;
            }
            return OB.App.OfflineSession.withId(this.model.get('updatedBy'));
          })
          .then(user => {
            if (!user) {
              return;
            }
            OB.UTIL.showConfirmation.display(
              enyo.format(
                OB.I18N.getLabel('OBPOS_ticketAlreadyOpenedInSession'),
                orderTypeMsg,
                existingOrder.get('documentNo'),
                user.name
              ),
              enyo.format(
                OB.I18N.getLabel('OBPOS_MsgConfirmSaveInCurrentSession'),
                user.name
              ),
              [
                {
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  action: function() {
                    //replace for state action that removes ticket in ticketlist
                    OB.Dal.remove(
                      existingOrder,
                      function() {
                        callback(model);
                      },
                      OB.UTIL.showError
                    );
                  }
                },
                {
                  label: OB.I18N.getLabel('OBMOBC_LblCancel'),
                  action: function() {
                    if (errorCallback) {
                      errorCallback();
                    }
                  }
                }
              ],
              {
                onHideFunction: function(dialog) {
                  return true;
                }
              }
            );
          });
      } else {
        return callback(model);
      }
    } else {
      return callback(model);
    }
  };

  OB.UTIL.TicketListUtils.removeTicket = async function(payload) {
    try {
      await OB.App.State.Global.removeTicket(payload);
      triggerTicketLoadEvents();
    } catch (error) {
      OB.App.View.ActionCanceledUIHandler.handle(error);
    } finally {
      OB.UTIL.checkRefreshMasterData();
    }
  };
})();
