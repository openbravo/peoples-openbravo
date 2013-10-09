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
      this.receipt = model.get('order');
      this.multiOrders = model.get('multiOrders');
      this.multiOrders.on('print', function (order) {
        this.print(order);
      }, this);
      this.receipt.on('print', this.print, this);
      this.receipt.on('popenDrawer', this.openDrawer, this);
      this.receipt.on('displayTotal', this.displayTotal, this);
      this.multiOrders.on('displayTotal', function () {
        this.displayTotalMultiorders();
      }, this);

      this.templateopendrawer = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.OpenDrawer);
      this.templatereceipt = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptTemplate);
      this.templateclosedreceipt = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptClosedTemplate);
      this.templateinvoice = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptTemplateInvoice);
      this.templatereturn = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptTemplateReturn);
      this.templatereturninvoice = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptTemplateReturnInvoice);
      this.templatelayaway = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.ReceiptTemplateLayaway);
      this.templatecashup = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.CashUpTemplate);
      };

  PrintReceipt.prototype.print = function (order) {
    // Clone the receipt
    var receipt = new OB.Model.Order();
    var me = this;
    var template;
    if (!_.isUndefined(order)) {
      receipt.clearWith(order);
    } else {
      receipt.clearWith(this.receipt);
    }

    if (receipt.get('generateInvoice') && receipt.get('orderType') !== 2 && receipt.get('orderType') !== 3 && !receipt.get('isLayaway')) {
      if (receipt.get('orderType') === 1) {
        template = this.templatereturninvoice;
      } else {
        template = this.templateinvoice;
      }
    } else {
      if (receipt.get('isPaid')) {
        if (receipt.get('orderType') === 1) {
          template = this.templatereturn;
        } else {
          template = this.templateclosedreceipt;
        }
      } else {
        if (receipt.get('orderType') === 1) {
          template = this.templatereturn;
        } else if (receipt.get('orderType') === 2 || receipt.get('isLayaway') || receipt.get('orderType') === 3) {
          template = this.templatelayaway;
        } else {
          template = this.templatereceipt;
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
  };

  PrintReceipt.prototype.openDrawer = function () {
    // Clone the receipt
    var receipt = new OB.Model.Order();
    receipt.clearWith(this.receipt);
    this.template = new OB.DS.HWResource(OB.OBPOSPointOfSale.Print.OpenDrawerTemplate);
    OB.POS.hwserver.print(this.template, {
      order: receipt
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
  OB.OBPOSPointOfSale.Print.OpenDrawerTemplate = 'res/opendrawer.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateLayaway = 'res/printlayaway.xml';
  OB.OBPOSPointOfSale.Print.DisplayTotal = 'res/displaytotal.xml';
  OB.OBPOSPointOfSale.Print.CashUpTemplate = 'res/printcashup.xml';

}());