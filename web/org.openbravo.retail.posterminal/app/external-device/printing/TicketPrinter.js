/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the TicketPrinter class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function TicketPrinterDefinition() {
  /**
   * This is an internal class used by the HardwareManagerEndpoint when consumming some of its messages.
   * It contains the logic to print ticket information through external devices.
   *
   * @see HardwareManagerEndpoint
   */
  class TicketPrinter {
    constructor() {
      this.controller = new OB.App.Class.ExternalDeviceController();
    }

    // Sets the legacy printers
    setLegacyPrinter(printer) {
      // kept for backwards compatibility in order to not break "OBPRINT_PrePrint" hooks
      // this kind of hooks receive a parameter referencing the PointOfSale model
      this.legacyPrinter = printer;
    }

    async displayTotal(message) {
      const messageData = message.messageObj;
      try {
        const template = OB.App.PrintTemplateStore.get(
          'printDisplayTotalTemplate'
        );
        await this.controller.display(template, {
          ticket: messageData.data.ticket
        });
      } catch (error) {
        OB.error(`Error displaying ticket total: ${error}`);
      }
    }

    async printTicket(message) {
      const messageData = message.messageObj;
      const { ticket } = messageData.data;
      const printSettings = messageData.data.printSettings || {};

      // print main ticket
      await this.doPrintTicket(includeOverPaymentAmt(ticket), printSettings);

      // print related canceled ticket (if any)
      if (ticket.doCancelAndReplace && ticket.canceledorder) {
        const { negativeDocNo } = ticket;
        const canceledTicket = {
          ...ticket.canceledorder,
          ordercanceled: true,
          negativeDocNo
        };
        await this.doPrintTicket(canceledTicket, printSettings);
      }
    }

    async doPrintTicket(ticket, printSettings) {
      let isPdf = false;
      try {
        // Tickets with simplified invoice will print only the invoice
        if (ticket.calculatedInvoice && !ticket.calculatedInvoice.fullInvoice) {
          return;
        }

        const prePrintData = await this.controller.executeHooks(
          'OBPRINT_PrePrint',
          {
            forcePrint: printSettings.forcePrint,
            offline: printSettings.offline,
            ticket,
            forcedtemplate: printSettings.forcedtemplate,
            model: this.legacyPrinter ? this.legacyPrinter.model : null,
            cancelOperation: false
          }
        );

        if (prePrintData.cancelOperation === true) {
          return;
        }

        const terminal = OB.App.TerminalProperty.get('terminal');
        const { printableTicket, printableOrder } = toPrintable(
          prePrintData.ticket
        );

        if (!printableTicket.print) {
          return;
        }

        const template = OB.App.PrintTemplateStore.selectTicketPrintTemplate(
          printableTicket,
          { forcedtemplate: toPrintTemplate(prePrintData.forcedtemplate) }
        );
        isPdf = template.ispdf;

        await this.selectPrinter({
          isPdf,
          isRetry: false,
          skipSelectPrinters: printSettings.skipSelectPrinters
        });

        const ticketToPrint =
          isPdf && printableOrder
            ? printableOrder.serializeToJSON()
            : printableTicket;

        const printedData = await this.controller.print(template, {
          ticket: ticketToPrint
        });

        if (
          shouldPrintTwice(printableTicket) ||
          terminal.terminalType.printTwice
        ) {
          await this.controller.print(template, { ticket: ticketToPrint });
        }

        await this.controller.executeHooks('OBPRINT_PostPrint', {
          ticket: printableTicket,
          printedReceipt: isPdf ? undefined : printedData
        });
      } catch (error) {
        OB.error(`Error printing ticket: ${error}`);
        await this.retryPrintTicket(ticket, printSettings, isPdf);
      }
    }

    async retryPrintTicket(ticket, printSettings, isPdf) {
      const retry = await OB.App.View.DialogUIHandler.askConfirmation({
        title: 'OBPOS_MsgHardwareServerNotAvailable',
        message: isPdf ? 'OBPOS_MsgPDFPrintAgain' : 'OBPOS_MsgPrintAgain',
        messageParams: [this.controller.getActiveURLIdentifier()],
        confirmLabel: 'OBPOS_LblRetry',
        additionalButtons: this.canSelectPrinter(isPdf)
          ? [
              {
                label: isPdf
                  ? 'OBPOS_SelectPDFPrintersTitle'
                  : 'OBPOS_SelectPrintersTitle',
                action: async () => {
                  const printer = await this.selectPrinter({
                    isPdf,
                    isRetry: true,
                    forceSelect: true
                  });
                  return printer != null;
                }
              }
            ]
          : []
      });

      if (retry) {
        await this.doPrintTicket(ticket, printSettings);
      }
    }

    async selectPrinter(options) {
      const terminal = OB.App.TerminalProperty.get('terminal');
      const { isPdf, isRetry, skipSelectPrinters, forceSelect } = options;

      if (
        !forceSelect &&
        (!terminal.terminalType.selectprinteralways ||
          skipSelectPrinters ||
          !this.canSelectPrinter(isPdf))
      ) {
        // skip printer selection
        return null;
      }

      const { printer } = await OB.App.View.DialogUIHandler.inputData(
        isPdf ? 'modalSelectPDFPrinters' : 'modalSelectPrinters',
        {
          title: isPdf
            ? OB.I18N.getLabel('OBPOS_SelectPDFPrintersTitle')
            : OB.I18N.getLabel('OBPOS_SelectPrintersTitle'),
          isRetry
        }
      );

      if (printer) {
        if (isPdf) {
          this.controller.setActivePDFURL(printer);
        } else {
          this.controller.setActiveURL(printer);
        }
      }

      return printer;
    }

    canSelectPrinter(isPdf) {
      return (
        OB.App.Security.hasPermission('OBPOS_retail.selectprinter') &&
        this.controller.hasAvailablePrinter(isPdf)
      );
    }

    async printTicketLine(message) {
      const messageData = message.messageObj;
      try {
        const template = OB.App.PrintTemplateStore.get(
          'printReceiptLineTemplate'
        );
        await this.controller.display(template, {
          ticketLine: messageData.data.line
        });
      } catch (error) {
        OB.error(`Error printing ticket line: ${error}`);
      }
    }
  }

  OB.App.Class.TicketPrinter = TicketPrinter;

  // determines if a ticket line should be printed or not
  function isPrintable(line) {
    const { product, baseNetUnitAmount, baseGrossUnitAmount } = line;
    return (
      product.productType !== 'S' ||
      product.isPrintServices ||
      baseNetUnitAmount ||
      baseGrossUnitAmount
    );
  }

  // generates a printable version of the provided ticket
  // it also returns the backbone object of the printable version used for backward compatibility
  function toPrintable(ticket) {
    const printableTicket = { ...ticket };
    if (!(printableTicket.orderDate instanceof Date)) {
      printableTicket.orderDate = new Date(printableTicket.orderDate);
    }

    printableTicket.lines = printableTicket.lines.filter(line =>
      isPrintable(line)
    );

    if (OB.App.State.Ticket.Utils.isNegative(printableTicket)) {
      printableTicket.payments = printableTicket.payments.map(payment => {
        if (!payment.isPrePayment && !payment.isReversePayment) {
          return {
            ...payment,
            amount: -Math.abs(payment.amount),
            origAmount: -Math.abs(payment.origAmount)
          };
        }
        return payment;
      });
    }

    if (printableTicket.canceledorder) {
      delete printableTicket.canceledorder;
    }

    if (OB.App.StateBackwardCompatibility != null) {
      const printableOrder = OB.App.StateBackwardCompatibility.getInstance(
        'Ticket'
      ).toBackboneObject(printableTicket);
      return { printableTicket, printableOrder };
    }

    return { printableTicket };
  }

  // Include over-payment amount to total paid amount from change amount
  function includeOverPaymentAmt(ticket) {
    const newTicket = { ...ticket };
    const changePrePayments = newTicket.payments.filter(payment => {
      const { paymentData } = payment;
      return paymentData && paymentData.changePayment;
    });
    const ticketPayments = newTicket.payments.filter(payment => {
      const { paymentData } = payment;
      return !paymentData || !paymentData.changePayment;
    });

    newTicket.payments = ticketPayments.map(payment => {
      const { paymentData } = payment;
      const overPaymentAmounts = { amount: 0, origAmount: 0 };

      if (paymentData && paymentData.key === payment.kind) {
        // Do not print over-payment amount for CancelAndReplace's old-payment and MultiTicket payments
        if (!ticket.doCancelAndReplace && !paymentData.isMultiTicketPayment) {
          overPaymentAmounts.amount = isNumeric(paymentData.amount)
            ? paymentData.amount
            : 0;
          overPaymentAmounts.origAmount = isNumeric(paymentData.origAmount)
            ? paymentData.origAmount
            : 0;
        }
      } else if (
        !changePrePayments.find(p => p.kind === payment.kind) &&
        ticket.changePayments &&
        ticket.changePayments.length > 0
      ) {
        // Do not print over-payment amount for MultiTicket payments
        const changePayments = ticket.changePayments.filter(
          chngpayment =>
            chngpayment.key === payment.kind &&
            !chngpayment.isMultiTicketPayment
        );
        const changeAmountPayment = changePayments.find(p =>
          isNumeric(p.amount)
        );
        overPaymentAmounts.amount = changeAmountPayment
          ? changeAmountPayment.amount
          : 0;
        const changeOrigAmountPayment = changePayments.find(p =>
          isNumeric(p.origAmount)
        );
        overPaymentAmounts.origAmount = changeOrigAmountPayment
          ? changeOrigAmountPayment.origAmount
          : 0;
      }

      if (overPaymentAmounts.amount || overPaymentAmounts.origAmount) {
        const { amount, origAmount } = overPaymentAmounts;
        const newPayment = { ...payment };
        newPayment.amount += ticket.isNegative ? -amount : amount;
        newPayment.origAmount += ticket.isNegative ? -origAmount : origAmount;
        return newPayment;
      }

      return payment;
    });

    return newTicket;
  }

  // checks whether a value is a numeric value
  function isNumeric(value) {
    return !Number.isNaN(Number(value));
  }

  // checks if a ticket should be printed twice. It should be printed twice when:
  // - it is a return and the corresponding preference is enabled
  // - or when one of the payment methods has the "printtwice" flag enabled
  function shouldPrintTwice(ticket) {
    const negativeLines = ticket.lines.filter(line => line.qty < 0);
    const hasNegativeLines =
      negativeLines.length === ticket.lines.length ||
      (negativeLines.length > 0 &&
        OB.App.Security.hasPermission(
          'OBPOS_SalesWithOneLineNegativeAsReturns'
        ));

    return (
      ((ticket.orderType === 1 || hasNegativeLines) &&
        ticket.lines.length > 0 &&
        !OB.App.Security.hasPermission('OBPOS_print.once')) ||
      ticket.payments.some(payment => payment.printtwice)
    );
  }

  // registers a OB.DS.HWResource as a PrintTemplate for backwards compatibility support
  // and returns the name of the registered PrintTemplate
  // if the provided template is not a OB.DS.HWResource then this function just returns it without changes
  function toPrintTemplate(template) {
    if (!template || !OB.DS || !OB.DS.HWResource) {
      return template;
    }
    if (template instanceof OB.DS.HWResource) {
      try {
        return OB.App.PrintTemplateStore.get(template.resource).name;
      } catch (error) {
        // template not registered yet
        OB.App.PrintTemplateStore.register(
          template.resource,
          template.resource,
          { isLegacy: true }
        );
        return OB.App.PrintTemplateStore.get(template.resource).name;
      }
    }
    return template;
  }
})();
