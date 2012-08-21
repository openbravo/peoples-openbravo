/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $ */

(function () {

  var PrintReceipt = function(receipt) {
    this.receipt = receipt;
    this.receipt.on('closed print', this.print, this);

    this.templatereceipt = new OB.DS.HWResource('res/printreceipt.xml');
    this.templateinvoice = new OB.DS.HWResource('res/printinvoice.xml');
    this.templatereturn = new OB.DS.HWResource('res/printreturn.xml');
    this.templatereturninvoice = new OB.DS.HWResource('res/printreturninvoice.xml');    
  };
  
  PrintReceipt.prototype.print = function () {
    
    // Clone the receipt
    var receipt = new OB.Model.Order();
    receipt.clearWith(this.receipt);
  
    var template;
    if (receipt.get('generateInvoice')) {
      if (receipt.get('orderType') === 1) {
        template = this.templatereturninvoice;
      } else {
        template = this.templateinvoice;
      }
    } else {
      if (receipt.get('orderType') === 1) {
        template = this.templatereturn;
      } else {
        template = this.templatereceipt;
      }
    }
  
    OB.POS.hwserver.print(template, { order: receipt });    
  };
  
  var PrintReceiptLine = function (receipt) {
    this.receipt = receipt;
    this.line = null;
    
    this.receipt.get('lines').on('selected', function(line) {
      if (this.line) {
        this.line.off('change', this.print);
      }
      this.line = line;
      if (this.line) {
        this.line.on('change', this.print, this);
      }
      this.print();
    }, this);
    
    this.templateline = new OB.DS.HWResource('res/printline.xml');    
  };
  
  PrintReceiptLine.prototype.print = function () {
    if (this.line) {
      OB.POS.hwserver.print(this.templateline, { line: this.line });
    }    
  };
  
  // Public object definition
  OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
  OB.OBPOSPointOfSale.Print = OB.OBPOSPointOfSale.Print || {}; 
  
  OB.OBPOSPointOfSale.Print.Receipt = PrintReceipt; 
  OB.OBPOSPointOfSale.Print.ReceiptLine = PrintReceiptLine; 

}());