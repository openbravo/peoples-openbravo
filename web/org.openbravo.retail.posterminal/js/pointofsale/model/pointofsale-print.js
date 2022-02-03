/*
 ************************************************************************************
 * Copyright (C) 2013-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global */

(function() {
  function dumyFunction() {}

  function extendHWResource(resource, template) {
    var terminal = OB.MobileApp.model.get('terminal');

    if (terminal[template + 'IsPdf'] === true) {
      resource.ispdf = true;
      resource.printer = terminal[template + 'Printer'];
      var i = 0,
        subreports = [];

      while (
        Object.prototype.hasOwnProperty.call(terminal, template + 'Subrep' + i)
      ) {
        subreports[i] = new OB.DS.HWResource(terminal[template + 'Subrep' + i]);
        subreports[i].getData(dumyFunction);
        i++;
      }
      resource.subreports = subreports;
      resource.getData(dumyFunction);
    }
  }

  var PrintReceipt = function(model) {
    var terminal = OB.MobileApp.model.get('terminal');
    this.model = model;
    this.receipt = model.get('order');
    this.multiOrders = model.get('multiOrders');
    this.multiOrders.on(
      'print',
      function(order, args) {
        this.print(order, args);
      },
      this
    );
    this.receipt.on(
      'print',
      function(order, args) {
        try {
          this.print(order, args);
        } catch (e) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorPrintingReceipt'));
          OB.error('Error printing the receipt:' + e);
        }
      },
      this
    );
    this.receipt.on('displayTotal', this.displayTotal, this);
    this.multiOrders.on(
      'displayTotal',
      function() {
        this.displayTotalMultiorders();
      },
      this
    );

    this.templatereceipt = new OB.DS.HWResource(
      terminal.printTicketTemplate || OB.OBPOSPointOfSale.Print.ReceiptTemplate
    );
    extendHWResource(this.templatereceipt, 'printTicketTemplate');
    this.templateclosedreceipt = new OB.DS.HWResource(
      terminal.printClosedReceiptTemplate ||
        OB.OBPOSPointOfSale.Print.ReceiptClosedTemplate
    );
    extendHWResource(this.templateclosedreceipt, 'printClosedReceiptTemplate');
    this.templateinvoice = new OB.DS.HWResource(
      terminal.printInvoiceTemplate ||
        OB.OBPOSPointOfSale.Print.ReceiptTemplateInvoice
    );
    extendHWResource(this.templateinvoice, 'printInvoiceTemplate');
    this.templatesimplifiedinvoice = new OB.DS.HWResource(
      terminal.printSimplifiedInvoiceTemplate ||
        OB.OBPOSPointOfSale.Print.ReceiptTemplateSimplifiedInvoice
    );
    extendHWResource(
      this.templatesimplifiedinvoice,
      'printSimplifiedInvoiceTemplate'
    );
    this.templatereturn = new OB.DS.HWResource(
      terminal.printReturnTemplate ||
        OB.OBPOSPointOfSale.Print.ReceiptTemplateReturn
    );
    extendHWResource(this.templatereturn, 'printReturnTemplate');
    this.templatereturninvoice = new OB.DS.HWResource(
      terminal.printReturnInvoiceTemplate ||
        OB.OBPOSPointOfSale.Print.ReceiptTemplateReturnInvoice
    );
    extendHWResource(this.templatereturninvoice, 'printReturnInvoiceTemplate');
    this.templatesimplifiedreturninvoice = new OB.DS.HWResource(
      terminal.printSimplifiedReturnInvoiceTemplate ||
        OB.OBPOSPointOfSale.Print.ReceiptTemplateSimplifiedReturnInvoice
    );
    extendHWResource(
      this.templatesimplifiedreturninvoice,
      'printSimplifiedReturnInvoiceTemplate'
    );
    this.templatelayaway = new OB.DS.HWResource(
      terminal.printLayawayTemplate ||
        OB.OBPOSPointOfSale.Print.ReceiptTemplateLayaway
    );
    extendHWResource(this.templatelayaway, 'printLayawayTemplate');
    this.templatecashup = new OB.DS.HWResource(
      terminal.printCashUpTemplate || OB.OBPOSPointOfSale.Print.CashUpTemplate
    );
    extendHWResource(this.templatecashup, 'printCashUpTemplate');
    this.templatecashmgm = new OB.DS.HWResource(
      terminal.printCashMgnTemplate || OB.OBPOSPointOfSale.Print.CashMgmTemplate
    );
    extendHWResource(this.templatecashmgm, 'printCashMgmTemplate');
    this.templatequotation = new OB.DS.HWResource(
      terminal.printQuotationTemplate ||
        OB.OBPOSPointOfSale.Print.QuotationTemplate
    );
    extendHWResource(this.templatequotation, 'printQuotationTemplate');

    this.templatetotal = new OB.DS.HWResource(
      terminal.printDisplayTotalTemplate ||
        OB.OBPOSPointOfSale.Print.DisplayTotal
    );
    extendHWResource(this.templatetotal, 'printDisplayTotalTemplate');
    this.templatedisplayreceipt = new OB.DS.HWResource(
      terminal.displayReceiptTemplate ||
        OB.OBPOSPointOfSale.Print.DisplayReceiptTemplate
    );
    extendHWResource(this.templatedisplayreceipt, 'displayReceiptTemplate');
    this.templateline = new OB.DS.HWResource(
      terminal.printReceiptLineTemplate ||
        OB.OBPOSPointOfSale.Print.ReceiptLineTemplate
    );
    extendHWResource(this.templateline, 'printReceiptLineTemplate');

    this.templategoodbye = new OB.DS.HWResource(
      terminal.printGoodByeTemplate || OB.OBPOSPointOfSale.Print.GoodByeTemplate
    );
    extendHWResource(this.templategoodbye, 'printGoodByeTemplate');
    this.templatewelcome = new OB.DS.HWResource(
      terminal.printWelcomeTemplate || OB.OBPOSPointOfSale.Print.WelcomeTemplate
    );
    extendHWResource(this.templatewelcome, 'printWelcomeTemplate');
    this.templateclosedinvoice = new OB.DS.HWResource(
      terminal.printClosedInvoiceTemplate ||
        OB.OBPOSPointOfSale.Print.ClosedInvoiceTemplate
    );
    extendHWResource(this.templateclosedinvoice, 'printClosedInvoiceTemplate');
    this.templatesimplifiedclosedinvoice = new OB.DS.HWResource(
      terminal.printSimplifiedClosedInvoiceTemplate ||
        OB.OBPOSPointOfSale.Print.SimplifiedClosedInvoiceTemplate
    );
    extendHWResource(
      this.templatesimplifiedclosedinvoice,
      'printSimplifiedClosedInvoiceTemplate'
    );
    this.templatecanceledreceipt = new OB.DS.HWResource(
      terminal.printCanceledReceiptTemplate ||
        OB.OBPOSPointOfSale.Print.CanceledReceiptTemplate
    );
    extendHWResource(
      this.templatecanceledreceipt,
      'printCanceledReceiptTemplate'
    );
    this.templatecanceledlayaway = new OB.DS.HWResource(
      terminal.printCanceledLayawayTemplate ||
        OB.OBPOSPointOfSale.Print.CanceledLayawayTemplate
    );
    extendHWResource(
      this.templatecanceledlayaway,
      'printCanceledLayawayTemplate'
    );
    this.isRetry = false;
  };

  PrintReceipt.prototype.print = function(receipt, printargs) {
    const payload = {};
    if (printargs) {
      payload.printSettings = {
        forcePrint: printargs.forcePrint,
        offline: printargs.offline,
        forcedtemplate: printargs.forcedtemplate,
        skipSelectPrinters: printargs.skipSelectPrinters
      };
    }
    if (!OB.UTIL.isNullOrUndefined(receipt)) {
      payload.ticket = OB.App.StateBackwardCompatibility.getInstance(
        'Ticket'
      ).toStateObject(receipt);
    }
    OB.App.State.Global.printTicket(payload);
  };

  PrintReceipt.prototype.displayTotal = function() {
    OB.App.State.Global.displayTotal();
  };

  PrintReceipt.prototype.doDisplayTotal = function(receipt) {
    OB.POS.hwserver.print(
      this.templatetotal,
      {
        order: receipt
      },
      null,
      OB.DS.HWServer.DISPLAY
    );
  };

  PrintReceipt.prototype.displayTotalMultiorders = function() {
    OB.App.State.Global.displayTotal({
      ticket: OB.UTIL.TicketUtils.toMultiTicket(this.multiOrders)
    });
  };

  var PrintReceiptLine = function(receipt) {
    var terminal = OB.MobileApp.model.get('terminal');

    this.receipt = receipt;

    this.receipt.get('lines').on(
      'add',
      function(line) {
        if (this.receipt.get('isPaid') === true) {
          return;
        }
      },
      this
    );
    this.templateline = new OB.DS.HWResource(
      terminal.printReceiptLineTemplate ||
        OB.OBPOSPointOfSale.Print.ReceiptLineTemplate
    );
    extendHWResource(this.templateline, 'printReceiptLineTemplate');
  };

  PrintReceiptLine.prototype.print = function(line) {
    OB.App.State.Global.printTicketLine({
      line: OB.UTIL.TicketUtils.toTicketLine(line)
    });
  };

  OB.OBPOS = {};
  OB.OBPOS.showSelectPrinterDialog = function(
    successfunc,
    hidefunc,
    cancelfunc,
    isPdf,
    msg
  ) {
    // Create dialog buttons
    var dialogbuttons = [];

    dialogbuttons.push({
      label: OB.I18N.getLabel('OBMOBC_LblCancel'),
      action: cancelfunc
    });
    if (
      OB.POS.modelterminal.hasPermission('OBPOS_retail.selectprinter') &&
      _.any(OB.POS.modelterminal.get('hardwareURL'), function(printer) {
        return isPdf ? printer.hasPDFPrinter : printer.hasReceiptPrinter;
      })
    ) {
      // Show this button entry only if there are
      dialogbuttons.push({
        name: 'selectAnotherPrinterButton',
        classes: 'selectAnotherPrinterButton',
        label: OB.I18N.getLabel('OBPOS_SelectAnotherPrinter'),
        action: function() {
          OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
            popup: isPdf ? 'modalSelectPDFPrinters' : 'modalSelectPrinters',
            args: {
              title: isPdf
                ? OB.I18N.getLabel('OBPOS_SelectPDFPrintersTitle')
                : OB.I18N.getLabel('OBPOS_SelectPrintersTitle'),
              hasPrinterProperty: isPdf ? 'hasPDFPrinter' : 'hasReceiptPrinter',
              serverURLProperty: isPdf ? 'activepdfurl' : 'activeurl',
              serverURLSetter: isPdf ? 'setActivePDFURL' : 'setActiveURL',
              onSuccess: successfunc,
              onCancel: cancelfunc,
              onHide: hidefunc,
              isRetry: true
            }
          });
          return true;
        }
      });
    }
    dialogbuttons.push({
      label: OB.I18N.getLabel('OBPOS_LblRetry'),
      isConfirmButton: true,
      isDefaultAction: true,
      action: successfunc
    });
    // Display error message
    OB.UTIL.showConfirmation.display(
      OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'),
      OB.I18N.getLabel(
        msg,
        isPdf
          ? [OB.POS.hwserver.activepdfidentifier]
          : [OB.POS.hwserver.activeidentifier]
      ),
      dialogbuttons,
      {
        onHideFunction: hidefunc
      }
    );
  };

  OB.OBPOS.showSelectPrintersWindow = function(
    successfunc,
    hidefunc,
    cancelfunc,
    isPdf,
    isRetry
  ) {
    if (
      OB.POS.modelterminal.hasPermission('OBPOS_retail.selectprinter') &&
      _.any(OB.POS.modelterminal.get('hardwareURL'), function(printer) {
        return isPdf ? printer.hasPDFPrinter : printer.hasReceiptPrinter;
      })
    ) {
      OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
        popup: isPdf ? 'modalSelectPDFPrinters' : 'modalSelectPrinters',
        args: {
          title: isPdf
            ? OB.I18N.getLabel('OBPOS_SelectPDFPrintersTitle')
            : OB.I18N.getLabel('OBPOS_SelectPrintersTitle'),
          hasPrinterProperty: isPdf ? 'hasPDFPrinter' : 'hasReceiptPrinter',
          serverURLProperty: isPdf ? 'activepdfurl' : 'activeurl',
          serverURLSetter: isPdf ? 'setActivePDFURL' : 'setActiveURL',
          onSuccess: successfunc,
          onCancel: cancelfunc,
          onHide: hidefunc,
          isRetry: isRetry
        }
      });
    } else {
      successfunc();
    }
  };

  var offlinePrinter = {
    addData: function(data) {
      var offlineData =
        JSON.parse(OB.UTIL.localStorage.getItem('OBPOS_OfflinePrinterData')) ||
        [];
      data.session = OB.MobileApp.model.get('session');
      offlineData.push(data);
      OB.UTIL.localStorage.setItem(
        'OBPOS_OfflinePrinterData',
        JSON.stringify(offlineData)
      );
    },
    printPendingJobs: function(callback) {
      var offlineData =
        JSON.parse(OB.UTIL.localStorage.getItem('OBPOS_OfflinePrinterData')) ||
        [];

      var printData = function() {
        if (offlineData.length === 0) {
          OB.UTIL.localStorage.removeItem('OBPOS_OfflinePrinterData');
          if (callback) {
            callback(true);
          }
        } else {
          var job = offlineData[0];
          if (job.session === OB.MobileApp.model.get('session')) {
            OB.POS.hwserver[job['sendfunction']](job['data'], function(result) {
              if (result && result.exception) {
                OB.UTIL.showError(result.exception.message);
                if (callback) {
                  callback(false);
                }
              } else {
                offlineData.shift();
                printData();
              }
            });
          } else {
            offlineData.shift();
            printData();
          }
        }
      };
      printData();
    }
  };

  // Public object definition
  OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
  OB.OBPOSPointOfSale.Print = OB.OBPOSPointOfSale.Print || {};

  OB.OBPOSPointOfSale.Print.Receipt = PrintReceipt;
  OB.OBPOSPointOfSale.Print.ReceiptTemplate =
    '../org.openbravo.retail.posterminal/res/printreceipt.xml';
  OB.OBPOSPointOfSale.Print.ReceiptClosedTemplate =
    '../org.openbravo.retail.posterminal/res/printclosedreceipt.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateInvoice =
    '../org.openbravo.retail.posterminal/res/printinvoice.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateSimplifiedInvoice =
    '../org.openbravo.retail.posterminal/res/printsimplifiedinvoice.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateReturn =
    '../org.openbravo.retail.posterminal/res/printreturn.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateReturnInvoice =
    '../org.openbravo.retail.posterminal/res/printreturninvoice.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateSimplifiedReturnInvoice =
    '../org.openbravo.retail.posterminal/res/printsimplifiedreturninvoice.xml';
  OB.OBPOSPointOfSale.Print.ReceiptLine = PrintReceiptLine;
  OB.OBPOSPointOfSale.Print.ReceiptLineTemplate =
    '../org.openbravo.retail.posterminal/res/printline.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateLayaway =
    '../org.openbravo.retail.posterminal/res/printlayaway.xml';
  OB.OBPOSPointOfSale.Print.DisplayTotal =
    '../org.openbravo.retail.posterminal/res/displaytotal.xml';
  OB.OBPOSPointOfSale.Print.DisplayReceiptTemplate =
    '../org.openbravo.retail.posterminal/res/displayreceipt.xml';
  OB.OBPOSPointOfSale.Print.CashUpTemplate =
    '../org.openbravo.retail.posterminal/res/printcashup.xml';
  OB.OBPOSPointOfSale.Print.CashUpKeptCashTemplate =
    '../org.openbravo.retail.posterminal/res/printcashupkeptcash.xml';
  OB.OBPOSPointOfSale.Print.CashMgmTemplate =
    '../org.openbravo.retail.posterminal/res/printcashmgmt.xml';
  OB.OBPOSPointOfSale.Print.GoodByeTemplate =
    '../org.openbravo.retail.posterminal/res/goodbye.xml';
  OB.OBPOSPointOfSale.Print.WelcomeTemplate =
    '../org.openbravo.retail.posterminal/res/welcome.xml';
  OB.OBPOSPointOfSale.Print.QuotationTemplate =
    '../org.openbravo.retail.posterminal/res/printquotation.xml';
  OB.OBPOSPointOfSale.Print.ClosedInvoiceTemplate =
    '../org.openbravo.retail.posterminal/res/printclosedinvoice.xml';
  OB.OBPOSPointOfSale.Print.SimplifiedClosedInvoiceTemplate =
    '../org.openbravo.retail.posterminal/res/printsimplifiedclosedinvoice.xml';
  OB.OBPOSPointOfSale.Print.CanceledReceiptTemplate =
    '../org.openbravo.retail.posterminal/res/printcanceledreceipt.xml';
  OB.OBPOSPointOfSale.Print.CanceledLayawayTemplate =
    '../org.openbravo.retail.posterminal/res/printcanceledlayaway.xml';

  OB.OBPOSPointOfSale.OfflinePrinter = offlinePrinter;

  OB.OBPOSPointOfSale.Print.printWelcome = function() {
    OB.App.State.Global.printWelcome();
  };
})();
