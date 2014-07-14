/*
 ************************************************************************************
 * Copyright (C) 2013-2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,_*/

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.OrderSave = function (model) {
    this.context = model;
    this.receipt = model.get('order');
    this.ordersToSend = OB.DEC.Zero;
    this.hasInvLayaways = false;

    this.receipt.on('closed', function (eventParams) {
      OB.warn('Ticket closed. Id: ' + model.get('order').get('id') + ". Docno: " + model.get('order').get('documentNo')+". Total gross: "+model.get('order').get('gross'));
      this.receipt = model.get('order');
      var me = this,
          docno = this.receipt.get('documentNo'),
          isLayaway = (this.receipt.get('orderType') === 2 || this.receipt.get('isLayaway')),
          json = this.receipt.serializeToJSON(),
          receiptId = this.receipt.get('id'),
          creationDate = new Date(),
          creationDateTransformed = new Date(creationDate.getUTCFullYear(), creationDate.getUTCMonth(), creationDate.getUTCDate(), creationDate.getUTCHours(), creationDate.getUTCMinutes(), creationDate.getUTCSeconds());

      if (this.receipt.get('isbeingprocessed') === 'Y') {
        //The receipt has already been sent, it should not be sent again
        return;
      }
      this.receipt.set('hasbeenpaid', 'Y');

      OB.UTIL.updateDocumentSequenceInDB(docno);

      delete this.receipt.attributes.json;
      this.receipt.set('timezoneOffset', creationDate.getTimezoneOffset());
      this.receipt.set('created', creationDate.getTime());
      this.receipt.set('obposCreatedabsolute', OB.I18N.formatDateISO(creationDate)); // Absolute date in ISO format
      // The order will not be processed if the navigator is offline
      if (OB.POS.modelterminal.get('connectedToERP')) {
        this.receipt.set('isbeingprocessed', 'Y');
      }

      OB.MobileApp.model.hookManager.executeHooks('OBPOS_PreOrderSave', {
        context: this,
        model: model,
        receipt: model.get('order')
      }, function (args) {
        if (args && args.cancellation && args.cancellation === true) {
          args.context.receipt.set('isbeingprocessed', 'N');
          return true;
        }
        var receipt = args.context.receipt,
            auxReceipt = new OB.Model.Order(),
            currentDocNo = receipt.get('documentNo') || docno;

        receipt.set('obposAppCashup', OB.MobileApp.model.get('terminal').cashUpId);
        receipt.set('json', JSON.stringify(receipt.toJSON()));

        auxReceipt.clearWith(receipt);
        OB.UTIL.cashUpReport(auxReceipt, OB.UTIL.calculateCurrentCash);

        OB.Dal.save(receipt, function () {
          var successCallback = function (model) {
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
              };
          if (OB.MobileApp.model.hookManager.get('OBPOS_PostSyncReceipt')) {
            //If there are elements in the hook, we are forced to execute the callback only after the synchronization process
            //has been executed, to prevent race conditions with the callback processes (printing and deleting the receipt)
            OB.MobileApp.model.runSyncProcess(function () {
              OB.MobileApp.model.hookManager.executeHooks('OBPOS_PostSyncReceipt', {
                receipt: auxReceipt
              }, function (args) {
                successCallback();
                if (eventParams && eventParams.callback) {
                  eventParams.callback();
                }
              });
            }, function () {
              OB.MobileApp.model.hookManager.executeHooks('OBPOS_PostSyncReceipt', {
                receipt: auxReceipt
              }, function (args) {
                if (eventParams && eventParams.callback) {
                  eventParams.callback();
                }
              });
            });
          } else {
            //If there are no elements in the hook, we can execute the callback asynchronusly with the synchronization process
            OB.MobileApp.model.runSyncProcess(function () {
              successCallback();
            });
            if (eventParams && eventParams.callback) {
              eventParams.callback();
            }
          }

        }, function () {
          //We do nothing: we don't need to alert the user, as the order is still present in the database, so it will be resent as soon as the user logs in again
        });
      });
    }, this);

    this.context.get('multiOrders').on('closed', function (receipt) {
      if (!_.isUndefined(receipt)) {
        this.receipt = receipt;
      }
      var me = this,
          docno = this.receipt.get('documentNo'),
          isLayaway = (this.receipt.get('orderType') === 2 || this.receipt.get('isLayaway')),
          json = this.receipt.serializeToJSON(),
          receiptId = this.receipt.get('id'),
          creationDate = new Date(),
          creationDateTransformed = new Date(creationDate.getUTCFullYear(), creationDate.getUTCMonth(), creationDate.getUTCDate(), creationDate.getUTCHours(), creationDate.getUTCMinutes(), creationDate.getUTCSeconds());

      if (this.receipt.get('isbeingprocessed') === 'Y') {
        //The receipt has already been sent, it should not be sent again
        return;
      }

      this.receipt.set('hasbeenpaid', 'Y');

      OB.UTIL.updateDocumentSequenceInDB(docno);

      delete this.receipt.attributes.json;
      this.receipt.set('timezoneOffset', creationDate.getTimezoneOffset());
      this.receipt.set('created', creationDate.getTime());
      this.receipt.set('obposCreatedabsolute', OB.I18N.formatDateISO(creationDate)); // Absolute date in ISO format
      this.receipt.set('obposAppCashup', OB.MobileApp.model.get('terminal').cashUpId);
      this.receipt.set('json', JSON.stringify(this.receipt.toJSON()));

      // The order will not be processed if the navigator is offline
      if (OB.POS.modelterminal.get('connectedToERP')) {
        this.receipt.set('isbeingprocessed', 'Y');
      }
      OB.MobileApp.model.hookManager.executeHooks('OBPOS_PreOrderSave', {
        context: this,
        model: model,
        receipt: this.receipt
      }, function (args) {
        if (args && args.cancellation && args.cancellation === true) {
          args.context.receipt.set('isbeingprocessed', 'N');
          return true;
        }
        OB.Dal.save(me.receipt, function () {
          OB.Dal.get(OB.Model.Order, receiptId, function (receipt) {
            var successCallback, errorCallback;
            successCallback = function () {
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