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

    // starting receipt verifications
    this.receipt.on('closed', function () {
      // is important to write all the errors with the same and unique header to find the records in the database
      var errorHeader = "Receipt verification error";
      var eventParams = 'closed';

      // protect the application against verification exceptions
      try {
        var totalTaxes = 0;
        _.each(this.get('taxes'), function (tax) {
          totalTaxes += tax.amount;
        }, this);
        var gross = this.get('gross');
        var net = this.get('net');

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
        }

      } catch (e) {
        // do nothing, we do not want to generate another error
      }
    });
    // finished receipt verifications
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
        OB.Dal.transaction(function (tx) {
          OB.UTIL.cashUpReport(auxReceipt, function () {
            OB.UTIL.calculateCurrentCash(null, tx);
            OB.MobileApp.model.updateDocumentSequenceWhenOrderSaved(receipt.get('documentnoSuffix'), receipt.get('quotationnoSuffix'), function () {
              OB.trace('Saving receipt.');
              OB.Dal.saveInTransaction(tx, receipt);
            }, tx);
          }, tx);
        }, null, function () {
          // success transaction...
          OB.trace('Executing of post order save hook.');

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

      // issue 29164: sometimes, the quotations are sync without creation date
      if (creationDate === "Invalid Date") {
        creationDate = new Date();
      }

      this.receipt.set('creationDate', creationDate);
      this.receipt.set('hasbeenpaid', 'Y');
      this.context.get('multiOrders').trigger('integrityOk', this.receipt);
      OB.MobileApp.model.updateDocumentSequenceWhenOrderSaved(this.receipt.get('documentnoSuffix'), this.receipt.get('quotationnoSuffix'));

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

    }, this);
  };
}());