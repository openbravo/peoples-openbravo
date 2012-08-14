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

OB.COMP.HWResource = function(res) {
  this.resource = res;
  this.resourcedata = null;
};

OB.COMP.HWResource.prototype.getData = function(callback) {
  if (this.resourcedata) {
    callback(this.resourcedata);
  } else {
    OB.UTIL.loadResource(this.resource, function(data) {
      this.resourcedata = data;
      callback(this.resourcedata);
    }, this);
  }
};

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
    this.templateline.getData(function(data) {
      OB.POS.hwserver.print(data, {
        line: line
      }, hwcallback);
    });
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

  template.getData(function(data) {
    OB.POS.hwserver.print(data, {
      order: receipt
    }, hwcallback);
  });
};

OB.COMP.HWManager.prototype.printCashUp = function() {
  var modeldaycash = this.modeldaycash;
  this.templatecashup.getData(function(data) {
    OB.POS.hwserver.print(data, {
      cashup: modeldaycash
    }, hwcallback);
  });
};

OB.COMP.HWManager.prototype.printCashMgmt = function() {
  var depsdropstosend = this.depsdropstosend;
  this.templatecashmgmt.getData(function(data) {
    OB.POS.hwserver.print(data, {
      cashmgmt: depsdropstosend
    }, cashMgmthwcallback);
  });
};

OB.COMP.HWManager.prototype.attr = function(attrs) {
  this.templateline = new OB.COMP.HWResource(attrs.templateline);
  this.templatereceipt = new OB.COMP.HWResource(attrs.templatereceipt);
  this.templateinvoice = new OB.COMP.HWResource(attrs.templateinvoice || attrs.templatereceipt);
  this.templatereturn = new OB.COMP.HWResource(attrs.templatereturn || attrs.templatereceipt);
  this.templatereturninvoice = new OB.COMP.HWResource(attrs.templatereturninvoice || attrs.templatereturn || attrs.templatereceipt);
  this.templatecashup = new OB.COMP.HWResource(attrs.templatecashup);
  this.templatecashmgmt = new OB.COMP.HWResource(attrs.templatecashmgmt);
};

OB.COMP.HWManager.prototype.load = function() {
  OB.UTIL.loadResource('res/welcome.xml', function(data) {
    OB.POS.hwserver.print(data, {}, hwcallback);
  }, this);
};