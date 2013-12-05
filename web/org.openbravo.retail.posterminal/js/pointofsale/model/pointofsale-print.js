/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $, _ */

(function () {

  var PrintReceipt = function (model) {
      var terminal = OB.POS.modelterminal.get('terminal');
      this.receipt = model.get('order');
      this.multiOrders = model.get('multiOrders');
      this.multiOrders.on('print', function (order, forcePrint) {
        this.print(order, forcePrint);
      }, this);
      this.receipt.on('print', function (order, forcePrint) {
        this.print(null, forcePrint);
      }, this);

      this.receipt.on('displayTotal', this.displayTotal, this);
      this.multiOrders.on('displayTotal', function () {
        this.displayTotalMultiorders();
      }, this);

      this.templatereceipt = new OB.DS.HWResource(terminal.printTicketTemplate || OB.OBPOSPointOfSale.Print.ReceiptTemplate);
      this.templateclosedreceipt = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptClosedTemplate);
      this.templateinvoice = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptTemplateInvoice);
      this.templatereturn = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptTemplateReturn);
      this.templatereturninvoice = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptTemplateReturnInvoice);
      this.templatelayaway = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptTemplateLayaway);
      this.templatecashup = new OB.DS.HWResource(terminal.printCashUpTemplate || OB.OBPOSPointOfSale.Print.CashUpTemplate);
      };

  PrintReceipt.prototype.print = function (order, forcePrint) {

    // Clone the receipt
    var receipt = new OB.Model.Order();
    var me = this;
    var template;

    OB.MobileApp.model.hookManager.executeHooks('OBPRINT_PrePrint', {
      forcePrint: forcePrint
    }, function (args) {
      if (args.cancelOperation && args.cancelOperation === true) {
        return true;
      }
      if (!_.isUndefined(order) && !_.isNull(order)) {
        receipt.clearWith(order);
      } else {
        receipt.clearWith(me.receipt);
      }
      if (receipt.get('generateInvoice') && receipt.get('orderType') !== 2 && receipt.get('orderType') !== 3 && !receipt.get('isLayaway')) {
        if (receipt.get('orderType') === 1) {
          template = me.templatereturninvoice;
        } else {
          template = me.templateinvoice;
        }
      } else {
        if (receipt.get('isPaid')) {
          if (receipt.get('orderType') === 1) {
            template = me.templatereturn;
          } else {
            template = me.templateclosedreceipt;
          }
        } else {
          if (receipt.get('orderType') === 1) {
            template = me.templatereturn;
          } else if (receipt.get('orderType') === 2 || receipt.get('isLayaway') || receipt.get('orderType') === 3) {
            template = me.templatelayaway;
          } else {
            template = me.templatereceipt;
          }
        }
      }
      OB.POS.hwserver.print(template, {
        order: receipt
      }, function (result) {
        var otherMe = me;
        var myreceipt = receipt;
        if (result && result.exception) {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'), OB.I18N.getLabel('OBPOS_MsgPrintAgain'), [{
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            action: function () {
              var otherOtherMe = otherMe;
              otherOtherMe.print();
              return true;
            }
          }, {
            label: OB.I18N.getLabel('OBMOBC_LblCancel')
          }]);
        }
      });
      if (receipt.get('orderType') === 1 && !OB.POS.modelterminal.hasPermission('OBPOS_print.once')) {
        OB.POS.hwserver.print(template, {
          order: receipt
        }, function (result) {
          var otherMe = me;
          var myreceipt = receipt;
          if (result && result.exception) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'), OB.I18N.getLabel('OBPOS_MsgPrintAgain'), [{
              label: OB.I18N.getLabel('OBMOBC_LblOk'),
              action: function () {
                var otherOtherMe = otherMe;
                otherOtherMe.print();
                return true;
              }
            }, {
              label: OB.I18N.getLabel('OBMOBC_LblCancel')
            }]);
          }
        });
      }
    });





  };

  PrintReceipt.prototype.displayTotal = function () {
    // Clone the receipt
    var receipt = new OB.Model.Order();
    receipt.clearWith(this.receipt);
    this.template = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.DisplayTotal);
    OB.POS.hwserver.print(this.template, {
      order: receipt
    });
  };

  PrintReceipt.prototype.displayTotalMultiorders = function () {
    // Clone the receipt
    var multiOrders;
    multiOrders = this.multiOrders;
    this.template = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.DisplayTotal);
    OB.POS.hwserver.print(this.template, {
      order: multiOrders
    });
  };

  var PrintReceiptLine = function (receipt) {
      this.receipt = receipt;
      this.line = null;

      this.receipt.get('lines').on('selected', function (line) {
        if (this.receipt.get("isPaid") === true) {
          return;
        }
        if (this.line) {
          this.line.off('change', this.print);
        }
        this.line = line;
        if (this.line) {
          this.line.on('change', this.print, this);
        }
        this.print();
      }, this);

      this.templateline = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptLineTemplate);
      };

  PrintReceiptLine.prototype.print = function () {
    if (this.line) {
      OB.POS.hwserver.print(this.templateline, {
        line: this.line
      });
    }
  };

  // Public object definition
  OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
  OB.OBPOSPointOfSale.Print = OB.OBPOSPointOfSale.Print || {};

  OB.OBPOSPointOfSale.Print.Receipt = PrintReceipt;
  OB.OBPOSPointOfSale.Print.ReceiptTemplate = 'res/printreceipt.xml';
  OB.OBPOSPointOfSale.Print.ReceiptClosedTemplate = 'res/printclosedreceipt.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateInvoice = 'res/printinvoice.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateReturn = 'res/printreturn.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateReturnInvoice = 'res/printreturninvoice.xml';
  OB.OBPOSPointOfSale.Print.ReceiptLine = PrintReceiptLine;
  OB.OBPOSPointOfSale.Print.ReceiptLineTemplate = 'res/printline.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateLayaway = 'res/printlayaway.xml';
  OB.OBPOSPointOfSale.Print.DisplayTotal = 'res/displaytotal.xml';
  OB.OBPOSPointOfSale.Print.CashUpTemplate = 'res/printcashup.xml';

}());