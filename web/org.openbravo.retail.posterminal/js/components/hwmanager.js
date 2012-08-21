/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global */

OB = window.OB || {};
OB.COMP = window.OB.COMP || {};

OB.COMP.HWManager = function(context) {
  if (context.modelorder) {
    this.receipt = context.modelorder;
    this.line = null;
    this.receipt.get('lines').on('selected', function(line) {
      if (this.line) {
        this.line.off('change', this.printLine);
      }
      this.line = line;
      if (this.line) {
        this.line.on('change', this.printLine, this);
      }
      this.printLine();
    }, this);

    this.receipt.on('closed print', this.printOrder, this);
  }
  if (context.modeldaycash) {
    this.modeldaycash = context.modeldaycash;
    this.modeldaycash.on('print', this.printCashUp, this);
  }
  if (context.depsdropstosend) {
    this.depsdropstosend = context.depsdropstosend;
    context.on('print', this.printCashMgmt, this);
  }
};

function hwcallback(e) {
  if (e.exception) {
    OB.UTIL.showError(e.exception.message);
  }
}

function cashMgmthwcallback(e) {
  if (e.result === 'OK') {
    OB.POS.navigate('retail.pointofsale');
  } else if (e.exception) {
    OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMgmtDonePrintNot'));
    OB.UTIL.showLoading(true);
    OB.POS.navigate('retail.pointofsale');

  }
}

OB.COMP.HWManager.prototype.printLine = function() {
  var line = this.line;
  if (line) {
    OB.POS.hwserver.print(this.templateline, {
      line: line
    }, hwcallback);
  }
};

OB.COMP.HWManager.prototype.printOrder = function() {

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

  OB.POS.hwserver.print(template, {
    order: receipt
  }, hwcallback);
};

OB.COMP.HWManager.prototype.printCashUp = function() {
  OB.POS.hwserver.print(this.templatecashup, {
    cashup: this.modeldaycash
  }, hwcallback);
};

OB.COMP.HWManager.prototype.printCashMgmt = function() {
  OB.POS.hwserver.print(this.templatecashmgmt, {
    cashmgmt: this.depsdropstosend
  }, cashMgmthwcallback);
};

OB.COMP.HWManager.prototype.attr = function(attrs) {
  this.templateline = new OB.DS.HWResource(attrs.templateline);
  this.templatereceipt = new OB.DS.HWResource(attrs.templatereceipt);
  this.templateinvoice = new OB.DS.HWResource(attrs.templateinvoice || attrs.templatereceipt);
  this.templatereturn = new OB.DS.HWResource(attrs.templatereturn || attrs.templatereceipt);
  this.templatereturninvoice = new OB.DS.HWResource(attrs.templatereturninvoice || attrs.templatereturn || attrs.templatereceipt);
  this.templatecashup = new OB.DS.HWResource(attrs.templatecashup);
  this.templatecashmgmt = new OB.DS.HWResource(attrs.templatecashmgmt);
};

OB.COMP.HWManager.prototype.load = function() {
  OB.UTIL.loadResource('res/welcome.xml', function(data) {
    OB.POS.hwserver.print(data, {}, hwcallback);
  }, this);
};