/*
 ************************************************************************************
 * Copyright (C) 2013-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, enyo, console */

(function () {

  OB.DATA = window.OB.DATA || {};

  OB.DATA.OrderSave = function (model) {
    this.context = model;
    this.receipt = model.get('order');
    this.ordersToSend = OB.DEC.Zero;
    this.hasInvLayaways = false;

    // start of receipt verifications
    var maxLogLinesPerReceipt = 100;
    var errorsFound = 0;
    this.receipt.on('all', function (eventParams) {
      // list of events to be processed
      var isVerify = false;
      isVerify = isVerify || (eventParams === 'eventExecutionDone');
      isVerify = isVerify || (eventParams === 'calculategross');
      isVerify = isVerify || (eventParams === 'saveCurrent');
      isVerify = isVerify || (eventParams === 'closed');
      if (!isVerify) {
        return;
      }
      // restart the number of allowed log lines when the receipt is closed/finished
      if (eventParams === 'closed') {
        errorsFound = 0;
      }
      if (errorsFound >= maxLogLinesPerReceipt) {
        return;
      }

      // the same header in all messages is important when looking for records in the database
      var errorHeader = "Receipt verification error";
      var errorCount = 0;

      // protect the application against verification exceptions
      try {
        // get tax information
        var totalTaxes = 0;
        _.each(this.get('taxes'), function (tax) {
          // tax.amount;
          // tax.gross;
          // tax.net;
          totalTaxes += tax.amount;
        }, this);

        var gross = this.get('gross');
        var net = this.get('net');

        // 1. verify that the sign of the net, gross and tax is consistent
        // Only do this if net+tax!=0, there is a special case if paying by gift card that the net is negative and the tax
        // positive, for example: net -20, tax +20, total is zero (as the gift card pays for the amount).
        if ((net + totalTaxes) !== 0 && Math.abs(totalTaxes) > 0 && ((Math.sign(net) !== Math.sign(gross)) || (Math.sign(net) !== Math.sign(totalTaxes)))) {
          // OB.UTIL.saveLogClient(JSON.stringify(signInconsistentErrorMessage), "Error");
          OB.error(enyo.format("%s: the sign of the net, gross and tax is inconsistent. event: '%s', gross: %s, net: %s, tax: %s", errorHeader, eventParams, gross, net, totalTaxes));
          errorCount += 1;
        }

        // 2. verify that the net is not higher than the gross
        if ((net + totalTaxes) !== 0 && Math.abs(net) > Math.abs(gross)) {
          OB.error(enyo.format("%s: net is bigger than the gross. event: '%s', gross: %s, net: %s, tax: %s", errorHeader, eventParams, gross, net, totalTaxes));
          errorCount += 1;
        }

        // 3. verify that the sum of the gross of each line equals the total gross
        var difference;
        var field = '';
        if (this.get('priceIncludesTax')) {
          difference = OB.DEC.sub(gross, totalTaxes);
          field = 'discountedNet';
        } else {
          difference = gross;
          field = 'discountedGross';
        }
        var isFieldUndefined = false;
        _.each(this.get('lines').models, function (line) {
          var fieldValue = line.get(field);
          if (!fieldValue) {
            isFieldUndefined = true;
            return;
          }
          difference = OB.DEC.sub(difference, fieldValue);
        });
        if (!isFieldUndefined && difference !== 0) {
          OB.error(enyo.format("%s: total gross does not equal the sum of the gross of each line. event: '%s', gross: %s, difference: %s", errorHeader, eventParams, gross, difference));
          errorCount += 1;
        }

      } catch (e) {
        console.error(enyo.format("%s. event: %s. %s", errorHeader, eventParams, e.stack));
        errorCount += 1;
      }
      errorsFound += errorCount;
    });
    // end of receipt verifications

    this.receipt.on('closed', function (eventParams) {
      this.receipt = model.get('order');
      OB.info('Ticket closed', this.receipt.getOrderDescription());
      var me = this,
          docno = this.receipt.get('documentNo'),
          isLayaway = (this.receipt.get('orderType') === 2 || this.receipt.get('isLayaway')),
          json = this.receipt.serializeToJSON(),
          receiptId = this.receipt.get('id'),
          creationDate = this.receipt.get('creationDate') || new Date();

      if (this.receipt.get('isbeingprocessed') === 'Y') {
        //The receipt has already been sent, it should not be sent again
        return;
      }

      this.receipt.set('hasbeenpaid', 'Y');

      OB.trace('Executing pre order save hook.');

      OB.UTIL.HookManager.executeHooks('OBPOS_PreOrderSave', {
        context: this,
        model: model,
        receipt: model.get('order')
      }, function (args) {
        if (args && args.cancellation && args.cancellation === true) {
          args.context.receipt.set('isbeingprocessed', 'N');
          args.context.receipt.set('hasbeenpaid', 'N');
          args.context.receipt.trigger('paymentCancel');
          return true;
        }
        var receipt = args.context.receipt,
            auxReceipt = new OB.Model.Order(),
            currentDocNo = receipt.get('documentNo') || docno;

        OB.trace('Execution of pre order save hook OK.');

        delete receipt.attributes.json;
        receipt.set('creationDate', creationDate);
        receipt.set('timezoneOffset', creationDate.getTimezoneOffset());
        receipt.set('created', creationDate.getTime());
        receipt.set('obposCreatedabsolute', OB.I18N.formatDateISO(creationDate));

        // multiterminal support
        // be sure that the active terminal is the one set as the order proprietary
        receipt.set('posTerminal', OB.MobileApp.model.get('terminal').id);
        receipt.set('posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.MobileApp.model.get('terminal')._identifier);

        receipt.get("approvals").forEach(function (approval) {
          if (typeof (approval.approvalType) === 'object') {
            approval.approvalMessage = OB.I18N.getLabel(approval.approvalType.message, approval.approvalType.params);
            approval.approvalType = approval.approvalType.approval;
          }
        });

        receipt.set('obposAppCashup', OB.MobileApp.model.get('terminal').cashUpId);
        // convert returns
        if (receipt.getGross() < 0) {
          _.forEach(receipt.get('payments').models, function (item) {
            item.set('amount', -item.get('amount'));
            item.set('origAmount', -item.get('origAmount'));
            item.set('paid', -item.get('paid'));
          });
        }
        receipt.set('json', JSON.stringify(receipt.serializeToJSON()));

        OB.trace('Calculationg cashup information.');

        auxReceipt.clearWith(receipt);
        OB.UTIL.cashUpReport(auxReceipt, function () {
          OB.UTIL.calculateCurrentCash();

          OB.MobileApp.model.updateDocumentSequenceWhenOrderSaved(receipt.get('documentnoSuffix'), receipt.get('quotationnoSuffix'), function () {
            OB.trace('Saving receipt.');

            OB.Dal.save(receipt, function () {

              var successCallback = function (model) {
                  OB.trace('Sync process success.');

                  //In case the processed document is a quotation, we remove its id so it can be reactivated
                  if (model && !_.isNull(model)) {
                    if (model.get('order') && model.get('order').get('isQuotation')) {
                      model.get('order').set('oldId', model.get('order').get('id'));
                      model.get('order').set('id', null);
                      model.get('order').set('isbeingprocessed', 'N');
                      OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_QuotationSaved', [currentDocNo]));
                    } else {
                      if (isLayaway) {
                        OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgLayawaySaved', [currentDocNo]));
                      } else {
                        OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgReceiptSaved', [currentDocNo]));
                      }
                    }
                  }

                  OB.trace('Order successfully removed.');
                  };

              OB.trace('Executing of post order save hook.');

              if (OB.UTIL.HookManager.get('OBPOS_PostSyncReceipt')) {
                //If there are elements in the hook, we are forced to execute the callback only after the synchronization process
                //has been executed, to prevent race conditions with the callback processes (printing and deleting the receipt)
                OB.trace('Execution Sync process.');

                OB.MobileApp.model.runSyncProcess(function () {
                  OB.UTIL.HookManager.executeHooks('OBPOS_PostSyncReceipt', {
                    receipt: auxReceipt
                  }, function (args) {
                    successCallback();
                    if (eventParams && eventParams.callback) {
                      eventParams.callback();
                    }
                  });
                }, function () {
                  OB.UTIL.HookManager.executeHooks('OBPOS_PostSyncReceipt', {
                    receipt: auxReceipt
                  }, function (args) {
                    if (eventParams && eventParams.callback) {
                      eventParams.callback();
                    }
                  });
                });
              } else {

                OB.trace('Execution Sync process.');

                //If there are no elements in the hook, we can execute the callback asynchronusly with the synchronization process
                OB.MobileApp.model.runSyncProcess(function () {
                  successCallback(model);
                });
                if (eventParams && eventParams.callback) {
                  eventParams.callback();
                }
              }

            }, function () {
              //We do nothing: we don't need to alert the user, as the order is still present in the database, so it will be resent as soon as the user logs in again
            });
          });
        });
      });
    }, this);

    this.context.get('multiOrders').on('closed', function (receipt) {

      OB.info('Multiorders ticket closed.');

      if (!_.isUndefined(receipt)) {
        this.receipt = receipt;
      }
      var me = this,
          docno = this.receipt.get('documentNo'),
          isLayaway = (this.receipt.get('orderType') === 2 || this.receipt.get('isLayaway')),
          json = this.receipt.serializeToJSON(),
          receiptId = this.receipt.get('id'),
          creationDate = this.receipt.get('creationDate') || new Date();

      this.receipt.set('creationDate', creationDate);
      this.receipt.set('hasbeenpaid', 'Y');
      this.context.get('multiOrders').trigger('integrityOk', this.receipt);

      delete this.receipt.attributes.json;
      this.receipt.set('timezoneOffset', creationDate.getTimezoneOffset());
      this.receipt.set('created', creationDate.getTime());
      this.receipt.set('obposCreatedabsolute', OB.I18N.formatDateISO(creationDate)); // Absolute date in ISO format

      // multiterminal support
      // be sure that the active terminal is the one set as the order proprietary
      receipt.set('posTerminal', OB.MobileApp.model.get('terminal').id);
      receipt.set('posTerminal' + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER, OB.MobileApp.model.get('terminal')._identifier);

      this.receipt.set('obposAppCashup', OB.MobileApp.model.get('terminal').cashUpId);
      this.receipt.set('json', JSON.stringify(this.receipt.toJSON()));

      OB.trace('Executing pre order save hook.');

      OB.UTIL.HookManager.executeHooks('OBPOS_PreOrderSave', {
        context: this,
        model: model,
        receipt: this.receipt
      }, function (args) {

        OB.trace('Execution of pre order save hook OK.');
        if (args && args.cancellation && args.cancellation === true) {
          args.context.receipt.set('isbeingprocessed', 'N');
          return true;
        }

        OB.MobileApp.model.updateDocumentSequenceWhenOrderSaved(receipt.get('documentnoSuffix'), receipt.get('quotationnoSuffix'), function () {
          OB.trace('Saving receipt.');

          OB.Dal.save(me.receipt, function () {
            OB.Dal.get(OB.Model.Order, receiptId, function (receipt) {
              var successCallback, errorCallback;
              successCallback = function () {

                OB.trace('Sync process success.');
                OB.UTIL.showLoading(false);
                if (me.hasInvLayaways) {
                  OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_noInvoiceIfLayaway'));
                  me.hasInvLayaways = false;
                }
                OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgAllReceiptSaved'));
              };
              errorCallback = function () {
                OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgAllReceiptNotSaved'));
              };


              if (!_.isUndefined(receipt.get('amountToLayaway')) && !_.isNull(receipt.get('amountToLayaway')) && receipt.get('generateInvoice')) {
                me.hasInvLayaways = true;
              }
              model.get('orderList').current = receipt;
              model.get('orderList').deleteCurrent();
              me.ordersToSend += 1;
              if (model.get('multiOrders').get('multiOrdersList').length === me.ordersToSend) {
                model.get('multiOrders').resetValues();

                OB.trace('Execution Sync process.');

                OB.MobileApp.model.runSyncProcess(successCallback);
                me.ordersToSend = OB.DEC.Zero;
              }

            }, null);
          }, function () {
            //We do nothing: we don't need to alert the user, as the order is still present in the database, so it will be resent as soon as the user logs in again
          });
        });
      });

    }, this);
  };
}());