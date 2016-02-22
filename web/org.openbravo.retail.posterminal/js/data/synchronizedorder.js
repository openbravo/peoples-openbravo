/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _, enyo */

OB.RR.HandleResponseCallbacks.push({
  name: 'unavailable',
  handleAction: function (callback, ajaxRequest, args) {
    var receipt = OB.MobileApp.model.receipt;
    if (OB.MobileApp.model.hasPermission('OBPOS_SynchronizedRequestOrder', true) && ajaxRequest.url.indexOf('org.openbravo.retail.posterminal.OrderLoader') !== -1) {
      OB.Dal.removeAll(OB.Model.Message, undefined, function () {
        // the transaction failed
        if (OB.MobileApp.model.frozenReceipt && OB.MobileApp.model.frozenReceipt.get('hasbeenpaid') === 'Y') {
          OB.MobileApp.model.frozenReceipt.set('hasbeenpaid', 'N');
          OB.Dal.save(OB.MobileApp.model.frozenReceipt, function () {
            //do relogin
            OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Online'), OB.I18N.getLabel('OBMOBC_OnlineConnectionHasReturned'), [{
              label: OB.I18N.getLabel('OBMOBC_LblLoginAgain'),
              action: function () {
                OB.MobileApp.model.lock();
                OB.UTIL.showLoading(true);
              }
            }], {
              autoDismiss: false,
              onHideFunction: function () {
                OB.MobileApp.model.lock();
                OB.UTIL.showLoading(true);
              }
            });
          }, undefined, true);
        } else {
          //do relogin
          OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Online'), OB.I18N.getLabel('OBMOBC_OnlineConnectionHasReturned'), [{
            label: OB.I18N.getLabel('OBMOBC_LblLoginAgain'),
            action: function () {
              OB.MobileApp.model.lock();
              OB.UTIL.showLoading(true);
            }
          }], {
            autoDismiss: false,
            onHideFunction: function () {
              OB.MobileApp.model.lock();
              OB.UTIL.showLoading(true);
            }
          });
        }
      });
    }

  }
}, {
  name: 'Order',
  handleAction: function (callback, ajaxRequest, args) {
    var receiptList = OB.MobileApp.model.orderList,
        receipt = OB.MobileApp.model.receipt,
        clonedCollection = new Backbone.Collection(),
        inResponse = ajaxRequest.success['arguments'][1];
    if (OB.MobileApp.model.frozenReceipt) {
      if (inResponse.response.status === 0) {
        receipt.get('payments').each(function (model) {
          clonedCollection.add(new Backbone.Model(model.toJSON()));
        });
        OB.Dal.transaction(function (tx) {
          // when all the properties of the receipt have been set, keep a copy
          OB.UTIL.cashUpReport(receipt, function () {
            OB.UTIL.calculateCurrentCash(null, tx);
            OB.MobileApp.model.updateDocumentSequenceWhenOrderSaved(receipt.get('documentnoSuffix'), receipt.get('quotationnoSuffix'), function () {
              OB.trace('Saving receipt.');
              OB.Dal.saveInTransaction(tx, receipt, function () {
                // the trigger is fired on the receipt object, as there is only 1 that is being updated
                receipt.trigger('integrityOk'); // Is important for module print last receipt. This module listen trigger.   
              });
            }, tx);
          }, tx);
        }, function () {
          // the transaction failed
          OB.error("[receipt.closed] The transaction failed to be commited. ReceiptId: " + receipt.get('id'));
          // rollback other changes
          receipt.set('hasbeenpaid', 'N');
          OB.MobileApp.model.frozenReceipt = undefined;
          OB.UTIL.showLoading(false);
          enyo.$.scrim.hide();
          callback();
        }, function () {
          function serverMessageForQuotation(receipt) {
            var isLayaway = (receipt.get('orderType') === 2 || receipt.get('isLayaway'));
            var currentDocNo = receipt.get('documentNo');
            if (receipt && receipt.get('isQuotation')) {
              OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_QuotationSaved', [currentDocNo]));
            } else {
              if (isLayaway) {
                OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgLayawaySaved', [currentDocNo]));
              } else {
                OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgReceiptSaved', [currentDocNo]));
              }
            }
            OB.trace('Order successfully removed.');
          }

          function cleanCallback() {
            var orderToPrint = OB.UTIL.clone(receipt);
            orderToPrint.get('payments').reset();
            clonedCollection.each(function (model) {
              orderToPrint.get('payments').add(new Backbone.Model(model.toJSON()), {
                silent: true
              });
            });
            orderToPrint.set('hasbeenpaid', 'Y');
            receipt.trigger('print', orderToPrint, {
              offline: true
            });
            // Verify that the receipt has not been changed while the ticket has being closed
            var diff = OB.UTIL.diffJson(receipt.serializeToJSON(), OB.MobileApp.model.frozenReceipt.serializeToJSON());
            // hasBeenPaid is the only difference allowed in the receipt
            delete diff.hasbeenpaid;
            // verify if there have been any modification to the receipt
            var diffStringified = JSON.stringify(diff, undefined, 2);
            if (diffStringified !== '{}') {
              OB.error("The receipt has been modified while it was being closed:\n" + diffStringified + "\n");
            }
            OB.MobileApp.model.frozenReceipt = undefined;
            receipt.setIsCalculateReceiptLockState(false);
            receipt.setIsCalculateGrossLockState(false);

            receiptList.deleteCurrent();
            receiptList.synchronizeCurrentOrder();
            enyo.$.scrim.hide();
          }
          OB.info("[receipt.closed] Transaction success. ReceiptId: " + receipt.get('id'));
          // create a clone of the receipt to be used when executing the final callback
          if (OB.UTIL.HookManager.get('OBPOS_PostSyncReceipt')) {
            // create a clone of the receipt to be used within the hook
            var receiptForPostSyncReceipt = new OB.Model.Order();
            OB.UTIL.clone(receipt, receiptForPostSyncReceipt);
            //If there are elements in the hook, we are forced to execute the callback only after the synchronization process
            //has been executed, to prevent race conditions with the callback processes (printing and deleting the receipt)
            OB.trace('Execution Sync process.');
            OB.UTIL.HookManager.executeHooks('OBPOS_PostSyncReceipt', {
              receipt: receiptForPostSyncReceipt
            }, function () {
              serverMessageForQuotation(receipt);
              cleanCallback();
            });
          } else {
            OB.trace('Execution Sync process.');
            //If there are no elements in the hook, we can execute the callback asynchronusly with the synchronization process
            serverMessageForQuotation(receipt);
            cleanCallback();
          }
          OB.UTIL.showLoading(false);
          enyo.$.scrim.hide();
          callback();
        });
      } else {
        // the transaction failed
        OB.error("[receipt.closed] The transaction failed to be commited. ReceiptId: " + receipt.get('id'));
        // rollback other changes
        receipt.set('hasbeenpaid', 'N');
        OB.MobileApp.model.frozenReceipt = undefined;
        OB.Dal.save(receipt, function () {
          OB.UTIL.showLoading(false);
          enyo.$.scrim.hide();
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), inResponse.response.error.message);
          callback();
        }, function () {}, true);
      }
    }
  }


});