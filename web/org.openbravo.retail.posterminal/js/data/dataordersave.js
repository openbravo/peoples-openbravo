/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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

    this.receipt.on('closed', function () {
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

      // The order will not be processed if the navigator is offline
      if (OB.POS.modelterminal.get('connectedToERP')) {
        this.receipt.set('isbeingprocessed', 'Y');
      }

      OB.MobileApp.model.hookManager.executeHooks('OBPOS_PreOrderSave', {
        context: this,
        receipt: model.get('order')
      }, function (args) {
        var receipt = args.context.receipt,
            auxReceipt = new OB.Model.Order(),
            currentDocNo = receipt.get('documentNo') || docno;

        receipt.set('json', JSON.stringify(receipt.toJSON()));

        auxReceipt.clearWith(receipt);
        OB.UTIL.cashUpReport(auxReceipt);

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
          OB.MobileApp.model.runSyncProcess(model, successCallback);
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
      this.receipt.set('json', JSON.stringify(this.receipt.toJSON()));

      // The order will not be processed if the navigator is offline
      if (OB.POS.modelterminal.get('connectedToERP')) {
        this.receipt.set('isbeingprocessed', 'Y');
      }

      OB.Dal.save(this.receipt, function () {
        if (OB.POS.modelterminal.get('connectedToERP')) {
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
              OB.MobileApp.model.runSyncProcess(model, successCallback);
              me.ordersToSend = OB.DEC.Zero;
            }

          }, null);
        }
      }, function () {
        //We do nothing: we don't need to alert the user, as the order is still present in the database, so it will be resent as soon as the user logs in again
      });
    }, this);
  };
}());