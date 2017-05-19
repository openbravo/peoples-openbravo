/*
 ************************************************************************************
 * Copyright (C) 2013-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _ */

(function () {

  function dumyFunction() {}

  function extendHWResource(resource, template) {
    var terminal = OB.MobileApp.model.get('terminal');

    if (terminal[template + 'IsPdf'] === 'true') {
      resource.ispdf = true;
      resource.printer = terminal[template + 'Printer'];
      var i = 0,
          subreports = [];

      while (terminal.hasOwnProperty(template + 'Subrep' + i)) {
        subreports[i] = new OB.DS.HWResource(terminal[template + 'Subrep' + i]);
        subreports[i].getData(dumyFunction);
        i++;
      }
      resource.subreports = subreports;
      resource.getData(dumyFunction);
    }
  }

  var PrintReceipt = function (model) {
      var terminal = OB.MobileApp.model.get('terminal');

      this.receipt = model.get('order');
      this.multiOrders = model.get('multiOrders');
      this.multiOrders.on('print', function (order, args) {

        this.print(order, args);
      }, this);
      this.receipt.on('print', function (order, args) {
        try {
          this.print(order, args);
        } catch (e) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorPrintingReceipt'));
          OB.error('Error printing the receipt:' + e);
        }
      }, this);

      this.receipt.on('displayTotal', this.displayTotal, this);
      this.multiOrders.on('displayTotal', function () {
        this.displayTotalMultiorders();
      }, this);

      this.templatereceipt = new OB.DS.HWResource(terminal.printTicketTemplate || OB.OBPOSPointOfSale.Print.ReceiptTemplate);
      extendHWResource(this.templatereceipt, "printTicketTemplate");
      this.templateclosedreceipt = new OB.DS.HWResource(terminal.printClosedReceiptTemplate || OB.OBPOSPointOfSale.Print.ReceiptClosedTemplate);
      extendHWResource(this.templateclosedreceipt, "printClosedReceiptTemplate");
      this.templateinvoice = new OB.DS.HWResource(terminal.printInvoiceTemplate || OB.OBPOSPointOfSale.Print.ReceiptTemplateInvoice);
      extendHWResource(this.templateinvoice, "printInvoiceTemplate");
      this.templatereturn = new OB.DS.HWResource(terminal.printReturnTemplate || OB.OBPOSPointOfSale.Print.ReceiptTemplateReturn);
      extendHWResource(this.templatereturn, "printReturnTemplate");
      this.templatereturninvoice = new OB.DS.HWResource(terminal.printReturnInvoiceTemplate || OB.OBPOSPointOfSale.Print.ReceiptTemplateReturnInvoice);
      extendHWResource(this.templatereturninvoice, "printReturnInvoiceTemplate");
      this.templatelayaway = new OB.DS.HWResource(terminal.printLayawayTemplate || OB.OBPOSPointOfSale.Print.ReceiptTemplateLayaway);
      extendHWResource(this.templatelayaway, "printLayawayTemplate");
      this.templatecashup = new OB.DS.HWResource(terminal.printCashUpTemplate || OB.OBPOSPointOfSale.Print.CashUpTemplate);
      extendHWResource(this.templatecashup, "printCashUpTemplate");
      this.templatecashmgm = new OB.DS.HWResource(terminal.printCashMgnTemplate || OB.OBPOSPointOfSale.Print.CashMgmTemplate);
      extendHWResource(this.templatecashmgm, "printCashMgmTemplate");
      this.templatequotation = new OB.DS.HWResource(terminal.printQuotationTemplate || OB.OBPOSPointOfSale.Print.QuotationTemplate);
      extendHWResource(this.templatequotation, "printQuotationTemplate");

      this.templatetotal = new OB.DS.HWResource(terminal.printDisplayTotalTemplate || OB.OBPOSPointOfSale.Print.DisplayTotal);
      extendHWResource(this.templatetotal, "printDisplayTotalTemplate");
      this.templatedisplayreceipt = new OB.DS.HWResource(terminal.displayReceiptTemplate || OB.OBPOSPointOfSale.Print.DisplayReceiptTemplate);
      extendHWResource(this.templatedisplayreceipt, "displayReceiptTemplate");
      this.templateline = new OB.DS.HWResource(terminal.printReceiptLineTemplate || OB.OBPOSPointOfSale.Print.ReceiptLineTemplate);
      extendHWResource(this.templateline, "printReceiptLineTemplate");

      this.templategoodbye = new OB.DS.HWResource(terminal.printGoodByeTemplate || OB.OBPOSPointOfSale.Print.GoodByeTemplate);
      extendHWResource(this.templategoodbye, "printGoodByeTemplate");
      this.templatewelcome = new OB.DS.HWResource(terminal.printWelcomeTemplate || OB.OBPOSPointOfSale.Print.WelcomeTemplate);
      extendHWResource(this.templatewelcome, "printWelcomeTemplate");
      this.templateclosedinvoice = new OB.DS.HWResource(terminal.printClosedInvoiceTemplate || OB.OBPOSPointOfSale.Print.ClosedInvoiceTemplate);
      extendHWResource(this.templateclosedinvoice, "printClosedInvoiceTemplate");
      this.templatecanceledreceipt = new OB.DS.HWResource(terminal.printCanceledReceiptTemplate || OB.OBPOSPointOfSale.Print.CanceledReceiptTemplate);
      extendHWResource(this.templatecanceledreceipt, "printCanceledReceiptTemplate");
      };

  PrintReceipt.prototype.print = function (order, printargs) {

    printargs = printargs || {};

    // Clone the receipt
    var receipt = new OB.Model.Order(),
        me = this,
        template;

    OB.UTIL.HookManager.executeHooks('OBPRINT_PrePrint', {
      forcePrint: printargs.forcePrint,
      offline: printargs.offline,
      order: order ? order : me.receipt,
      template: template,
      forcedtemplate: printargs.forcedtemplate,
      callback: printargs.callback
    }, function (args) {
      function printPDF(receipt, args) {
        OB.POS.hwserver._printPDF({
          param: receipt.serializeToJSON(),
          mainReport: args.template,
          subReports: args.template.subreports
        }, function (result) {
          var myreceipt = receipt;
          if (result && result.exception) {
            // callbacks definition
            var successfunc = function () {
                me.print(receipt, printargs);
                if (args.callback) {
                  args.callback();
                }
                return true;
                };
            var hidefunc = function () {
                if (printargs.offline && OB.MobileApp.model.get('terminal').printoffline) {
                  OB.Dal.save(new OB.Model.OfflinePrinter({
                    data: result.data,
                    sendfunction: '_sendPDF'
                  }));
                }
                if (args.callback) {
                  args.callback();
                }
                };
            OB.OBPOS.showSelectPrinterDialog(successfunc, hidefunc, null, true, 'OBPOS_MsgPDFPrintAgain');
          } else {
            // Success. Try to print the pending receipts.
            OB.Model.OfflinePrinter.printPendingJobs();
            OB.UTIL.HookManager.executeHooks('OBPRINT_PostPrint', {
              receipt: receipt
            }, function () {
              OB.debug('Executed hooks of OBPRINT_PostPrint');
              if (args.callback) {
                args.callback();
              }
            });
          }
        });
      }

      function printFiles(receipt, args) {
        var filesArray = [],
            callbackGetFiles, callbackGetBinaryData, recursivePrintFiles, i = 0;

        if (receipt.get('isPaid') || receipt.get('payment') >= receipt.get('gross') || (receipt.isLayaway() && receipt.get('payment') > 0)) {

          recursivePrintFiles = function () {
            if (filesArray.length > 0) {
              OB.POS.hwserver._printFile({
                name: filesArray[i].name,
                printer: filesArray[i].printer,
                document: filesArray[i].document
              }, function (result) {
                if (result && result.exception) {
                  // callbacks definition
                  var successfunc = function () {
                      me.print(receipt, printargs);
                      if (args.callback) {
                        args.callback();
                      }
                      return true;
                      };
                  var hidefunc = function () {
                      if (printargs.offline && OB.MobileApp.model.get('terminal').printoffline) {
                        OB.Dal.save(new OB.Model.OfflinePrinter({
                          data: result.data,
                          sendfunction: '_sendFile'
                        }));
                      }
                      if (args.callback) {
                        args.callback();
                      }
                      };
                  OB.OBPOS.showSelectPrinterDialog(successfunc, hidefunc, null, true, 'OBPOS_MsgPDFPrintAgain');
                } else {
                  i++;
                  if (i < filesArray.length) {
                    recursivePrintFiles();
                  } else {
                    // Success. Try to print the pending receipts.
                    OB.Model.OfflinePrinter.printPendingJobs();
                    if (args.callback) {
                      args.callback();
                    }
                  }
                }
              });
            }
          };

          callbackGetFiles = _.after(receipt.get('lines').length, function () {
            recursivePrintFiles();
          });

          _.each(receipt.get('lines').models, function (line) {
            var criteria = {},
                productId = line.get('product').id;
            if (line.get('qty') > 0) {
              if (OB.MobileApp.model.hasPermission('OBPOS_remote.obposfiles', true)) {
                criteria.remoteFilters = [{
                  columns: ['product'],
                  operator: 'equals',
                  value: productId,
                  isId: true
                }];
              } else {
                criteria.product = productId;
              }
              OB.Dal.find(OB.Model.OBPOSProdFiles, criteria, function (productFiles) {
                if (productFiles.length > 0) {
                  callbackGetBinaryData = _.after(productFiles.length, function () {
                    callbackGetFiles();
                  });

                  _.each(productFiles.models, function (productFile) {
                    var printer = 1;
                    OB.Dal.get(OB.Model.OBPOSFiles, productFile.get('posfile'), function (posFile) {
                      if (productFile.get('printer')) {
                        printer = productFile.get('printer');
                      }
                      filesArray.push({
                        name: posFile.get('name'),
                        printer: printer,
                        document: posFile.get('binaryData')
                      });
                      callbackGetBinaryData();
                    }, function () {
                      OB.error(arguments);
                    });
                  });
                } else {
                  callbackGetFiles();
                }
              }, function () {
                OB.error(arguments);
              });
            } else {
              callbackGetFiles();
            }
          });
        }
      }

      if (args.cancelOperation && args.cancelOperation === true) {
        if (args.callback) {
          args.callback();
        }
        return true;
      }
      if (!_.isUndefined(args.order) && !_.isNull(args.order)) {
        OB.UTIL.clone(args.order, receipt);
      } else {
        OB.UTIL.clone(me.receipt, receipt);
      }

      var hasNegativeLines = _.filter(receipt.get('lines').models, function (line) {
        return line.get('qty') < 0;
      }).length === receipt.get('lines').size() ? true : false;

      var linesToRemove = [];
      receipt.get('lines').forEach(function (line) {
        if (!line.isPrintableService()) {
          //Prevent service lines with prices different than zero to be removed:
          if (line.get('net') || line.get('gross')) {
            return;
          }
          linesToRemove.push(line);
        }
      });

      receipt.get('lines').remove(linesToRemove);

      if (args.forcedtemplate) {
        args.template = args.forcedtemplate;
      } else if (receipt.get('ordercanceled') || receipt.get('cancelLayaway')) {
        args.template = me.templatecanceledreceipt;
      } else if (receipt.get('generateInvoice') && receipt.get('orderType') !== 2 && receipt.get('orderType') !== 3 && !receipt.get('isLayaway')) {
        if (receipt.get('orderType') === 1 || hasNegativeLines) {
          args.template = me.templatereturninvoice;
        } else if (receipt.get('isQuotation')) {
          args.template = me.templatequotation;
        } else if (receipt.get('isPaid')) {
          args.template = me.templateclosedinvoice;
        } else {
          args.template = me.templateinvoice;
        }
      } else {
        if (receipt.get('isPaid')) {
          if (receipt.get('orderType') === 1 || hasNegativeLines) {
            args.template = me.templatereturn;
          } else if (receipt.get('isQuotation')) {
            args.template = me.templatequotation;
          } else {
            args.template = me.templateclosedreceipt;
          }
        } else {
          if (receipt.get('orderType') === 1 || hasNegativeLines) {
            args.template = me.templatereturn;
          } else if (receipt.get('orderType') === 2 || receipt.get('isLayaway') || receipt.get('orderType') === 3) {
            args.template = me.templatelayaway;
          } else if (receipt.get('isQuotation')) {
            args.template = me.templatequotation;
          } else {
            args.template = me.templatereceipt;
          }
        }
      }
      if (args.template.ispdf) {
        args.template.dateFormat = OB.Format.date;
        if (receipt.get('canceledorder')) {
          var clonedreceipt = new OB.Model.Order();
          OB.UTIL.clone(receipt, clonedreceipt);
          clonedreceipt.unset("canceledorder", {
            silent: true
          });
          printPDF(clonedreceipt, args);
        } else {
          printPDF(receipt, args);
        }
        if (((receipt.get('orderType') === 1 || hasNegativeLines) && !OB.MobileApp.model.hasPermission('OBPOS_print.once', true)) || OB.MobileApp.model.get('terminal').terminalType.printTwice) {
          printPDF(receipt, args);
        }
      } else {
        if (receipt.get('print')) { //Print option of order property
          OB.POS.hwserver.print(args.template, {
            order: receipt
          }, function (result, printedReceipt) {
            var myreceipt = receipt;
            if (result && result.exception) {
              // callbacks definition
              var successfunc = function () {
                  me.print(receipt, printargs);
                  return true;
                  };
              var cancelfunc = function () {
                  if (args.callback) {
                    args.callback();
                  }
                  return true;
                  };
              var hidefunc = function () {
                  if (printargs.offline && OB.MobileApp.model.get('terminal').printoffline) {
                    OB.Dal.save(new OB.Model.OfflinePrinter({
                      data: result.data,
                      sendfunction: '_send'
                    }));
                  }
                  if (args.callback) {
                    args.callback();
                  }
                  };
              OB.OBPOS.showSelectPrinterDialog(successfunc, hidefunc, cancelfunc, false, 'OBPOS_MsgPrintAgain');
            } else {
              // Success. Try to print the pending receipts.
              OB.Model.OfflinePrinter.printPendingJobs();
              OB.UTIL.HookManager.executeHooks('OBPRINT_PostPrint', {
                receipt: receipt,
                printedReceipt: printedReceipt
              }, function () {
                OB.debug("Executed hooks of OBPRINT_PostPrint");
                if (args.callback) {
                  args.callback();
                }
              });
            }
          });
        } else {
          if (args.callback) {
            args.callback();
          }
        } // order property.
        //Print again when it is a return and the preference is 'Y' or when one of the payments method has the print twice checked
        if (receipt.get('print')) { //Print option of order property
          if (((receipt.get('orderType') === 1 || hasNegativeLines) && !OB.MobileApp.model.hasPermission('OBPOS_print.once', true)) || _.filter(receipt.get('payments').models, function (iter) {
            if (iter.get('printtwice')) {
              return iter;
            }
          }).length > 0 || OB.MobileApp.model.get('terminal').terminalType.printTwice) {
            OB.POS.hwserver.print(args.template, {
              order: receipt
            }, function (result) {
              var myreceipt = receipt;
              if (result && result.exception) {
                // callbacks definition
                var successfunc = function () {
                    me.print(receipt, printargs);
                    return true;
                    };
                var hidefunc = function (dialog) {
                    if (printargs.offline && OB.MobileApp.model.get('terminal').printoffline) {
                      OB.Dal.save(new OB.Model.OfflinePrinter({
                        data: result.data,
                        sendfunction: '_send'
                      }));
                    }
                    if (args.callback) {
                      args.callback();
                    }
                    };
                OB.OBPOS.showSelectPrinterDialog(successfunc, hidefunc, null, false, 'OBPOS_MsgPrintAgain');
              } else {
                // Success. Try to print the pending receipts.
                OB.Model.OfflinePrinter.printPendingJobs();
              }
            });
          }
        } // order property.
      }
      if (receipt.get('canceledorder')) {
        var negativeDocNo = receipt.get('negativeDocNo');
        receipt.get('canceledorder').set('ordercanceled', true);
        receipt.get('canceledorder').set('negativeDocNo', negativeDocNo);
        me.print(receipt.get('canceledorder'), args);
      }

      OB.POS.hwserver.print(me.templatedisplayreceipt, {
        order: receipt
      }, null, OB.DS.HWServer.DISPLAY);
    });
  };

  PrintReceipt.prototype.displayTotal = function () {
    // Clone the receipt
    var receipt = new OB.Model.Order();
    OB.UTIL.clone(this.receipt, receipt);
    OB.POS.hwserver.print(this.templatetotal, {
      order: receipt
    }, null, OB.DS.HWServer.DISPLAY);
  };

  PrintReceipt.prototype.displayTotalMultiorders = function () {
    // Clone the receipt
    var multiOrders;
    multiOrders = this.multiOrders;
    OB.POS.hwserver.print(this.templatetotal, {
      order: multiOrders
    }, null, OB.DS.HWServer.DISPLAY);
  };

  var PrintReceiptLine = function (receipt) {
      var terminal = OB.MobileApp.model.get('terminal');

      this.receipt = receipt;

      this.receipt.get('lines').on('add', function (line) {
        if (this.receipt.get("isPaid") === true) {
          return;
        }
        line.on('change:gross', this.print, this);
      }, this);
      this.templateline = new OB.DS.HWResource(terminal.printReceiptLineTemplate || OB.OBPOSPointOfSale.Print.ReceiptLineTemplate);
      extendHWResource(this.templateline, 'printReceiptLineTemplate');
      };

  PrintReceiptLine.prototype.print = function (line) {
    OB.POS.hwserver.print(this.templateline, {
      line: line
    }, null, OB.DS.HWServer.DISPLAY);
  };

  OB.OBPOS = {};
  OB.OBPOS.showSelectPrinterDialog = function (successfunc, hidefunc, cancelfunc, isPdf, msg) {
    // Create dialog buttons
    var dialogbuttons = [];
    dialogbuttons.push({
      label: OB.I18N.getLabel('OBPOS_LblRetry'),
      isConfirmButton: true,
      action: successfunc
    });
    if (OB.POS.modelterminal.hasPermission('OBPOS_retail.selectprinter') && _.any(OB.POS.modelterminal.get('hardwareURL'), function (printer) {
      return isPdf ? printer.hasPDFPrinter : printer.hasReceiptPrinter;
    })) {
      // Show this button entry only if there are 
      dialogbuttons.push({
        name: 'selectAnotherPrinterButton',
        label: OB.I18N.getLabel('OBPOS_SelectAnotherPrinter'),
        action: function () {
          OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
            popup: 'modalSelectPrinters',
            args: {
              title: isPdf ? OB.I18N.getLabel('OBPOS_SelectPDFPrintersTitle') : OB.I18N.getLabel('OBPOS_SelectPrintersTitle'),
              hasPrinterProperty: isPdf ? 'hasPDFPrinter' : 'hasReceiptPrinter',
              serverURLProperty: isPdf ? 'activepdfurl' : 'activeurl',
              serverURLSetter: isPdf ? 'setActivePDFURL' : 'setActiveURL',
              onSuccess: successfunc,
              onCancel: cancelfunc,
              onHide: hidefunc
            }
          });
          return true;
        }
      });
    }
    dialogbuttons.push({
      label: OB.I18N.getLabel('OBMOBC_LblCancel'),
      action: cancelfunc
    });
    // Display error message
    OB.UTIL.showConfirmation.display(
    OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'), OB.I18N.getLabel(msg, isPdf ? [OB.POS.hwserver.activepdfidentifier] : [OB.POS.hwserver.activeidentifier]), dialogbuttons, {
      onHideFunction: hidefunc
    });
  };

  // Public object definition
  OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
  OB.OBPOSPointOfSale.Print = OB.OBPOSPointOfSale.Print || {};

  OB.OBPOSPointOfSale.Print.Receipt = PrintReceipt;
  OB.OBPOSPointOfSale.Print.ReceiptTemplate = '../org.openbravo.retail.posterminal/res/printreceipt.xml';
  OB.OBPOSPointOfSale.Print.ReceiptClosedTemplate = '../org.openbravo.retail.posterminal/res/printclosedreceipt.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateInvoice = '../org.openbravo.retail.posterminal/res/printinvoice.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateReturn = '../org.openbravo.retail.posterminal/res/printreturn.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateReturnInvoice = '../org.openbravo.retail.posterminal/res/printreturninvoice.xml';
  OB.OBPOSPointOfSale.Print.ReceiptLine = PrintReceiptLine;
  OB.OBPOSPointOfSale.Print.ReceiptLineTemplate = '../org.openbravo.retail.posterminal/res/printline.xml';
  OB.OBPOSPointOfSale.Print.ReceiptTemplateLayaway = '../org.openbravo.retail.posterminal/res/printlayaway.xml';
  OB.OBPOSPointOfSale.Print.DisplayTotal = '../org.openbravo.retail.posterminal/res/displaytotal.xml';
  OB.OBPOSPointOfSale.Print.DisplayReceiptTemplate = '../org.openbravo.retail.posterminal/res/displayreceipt.xml';
  OB.OBPOSPointOfSale.Print.CashUpTemplate = '../org.openbravo.retail.posterminal/res/printcashup.xml';
  OB.OBPOSPointOfSale.Print.CashMgmTemplate = '../org.openbravo.retail.posterminal/res/printcashmgmt.xml';
  OB.OBPOSPointOfSale.Print.GoodByeTemplate = '../org.openbravo.retail.posterminal/res/goodbye.xml';
  OB.OBPOSPointOfSale.Print.WelcomeTemplate = '../org.openbravo.retail.posterminal/res/welcome.xml';
  OB.OBPOSPointOfSale.Print.QuotationTemplate = '../org.openbravo.retail.posterminal/res/printquotation.xml';
  OB.OBPOSPointOfSale.Print.ClosedInvoiceTemplate = '../org.openbravo.retail.posterminal/res/printclosedinvoice.xml';
  OB.OBPOSPointOfSale.Print.CanceledReceiptTemplate = '../org.openbravo.retail.posterminal/res/printcanceledreceipt.xml';
}());