/*global */

(function() {

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

  OB.COMP.HWManager.prototype.printLine = function() {
    if (this.line) {
      OB.POS.hwserver.print(this.templatelinedata, {
        line: this.line
      }, hwcallback);
    }
  };

  OB.COMP.HWManager.prototype.printOrder = function() {
    OB.POS.hwserver.print(this.templatereceiptdata, {
      order: this.receipt
    }, hwcallback);
  };

  OB.COMP.HWManager.prototype.printCashUp = function() {
    OB.POS.hwserver.print(this.templatecashupdata, {
      cashup: this.modeldaycash
    }, hwcallback);
  };
  OB.COMP.HWManager.prototype.printCashMgmt = function() {
    OB.POS.hwserver.print(this.templatecashmgmtdata, {
      cashmgmt: this.depsdropstosend
    }, hwcallback);
  };

  OB.COMP.HWManager.prototype.attr = function(attrs) {
    this.templateline = attrs.templateline;
    this.templatereceipt = attrs.templatereceipt;
    this.templatecashup = attrs.templatecashup;
    this.templatecashmgmt = attrs.templatecashmgmt;
  };

  OB.COMP.HWManager.prototype.load = function() {
    OB.UTIL.loadResource(this.templateline, function(data) {
      this.templatelinedata = data;
    }, this);
    OB.UTIL.loadResource(this.templatereceipt, function(data) {
      this.templatereceiptdata = data;
    }, this);
    OB.UTIL.loadResource(this.templatecashup, function(data) {
      this.templatecashupdata = data;
    }, this);
    OB.UTIL.loadResource(this.templatecashmgmt, function(data) {
      this.templatecashmgmtdata = data;
    }, this);
    OB.UTIL.loadResource('res/welcome.xml', function(data) {
      OB.POS.hwserver.print(data, {}, hwcallback);
    }, this);
  };
}());