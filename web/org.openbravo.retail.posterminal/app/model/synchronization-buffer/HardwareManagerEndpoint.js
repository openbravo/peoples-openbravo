/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the HardwareManagerEndpoint class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function HardwareManagerEndpointDefinition() {
  // determines if a ticket line should be printed or not
  const isPrintable = line => {
    const { product, baseNetUnitAmount, baseGrossUnitAmount } = line;
    return (
      product.productType !== 'S' ||
      product.isPrintServices ||
      baseNetUnitAmount ||
      baseGrossUnitAmount
    );
  };

  // generates a printable version of the provided ticket
  // it also returns the backbone object of the printable version used for backward compatibility
  const toPrintable = ticket => {
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
  };

  // checks if a ticket should be printed twice. It should be printed twice when:
  // - it is a return and the corresponding preference is enabled
  // - or when one of the payment methods has the "printtwice" flag enabled
  const shouldPrintTwice = ticket => {
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
  };

  /**
   * A synchronization endpoint in charge of the messages for communicating with external devices.
   */
  class HardwareManagerEndpoint extends OB.App.Class.SynchronizationEndpoint {
    /**
     * Base constructor, it can be used once the terminal information is loaded.
     * That means that this endpoint should not be registered until that information is ready.
     */
    constructor(name) {
      super(name || 'HardwareManager');

      this.messageTypes.push(
        'displayTotal',
        'greetHardwareManager',
        'printTicket',
        'printTicketLine',
        'printWelcome'
      );

      this.controller = new OB.App.Class.ExternalDeviceController();
      this.templateStore = OB.App.PrintTemplateStore;
    }

    // Sets the legacy printers
    setPrinters(printers) {
      // kept for backwards compatibility in order to not break "OBPRINT_PrePrint" hooks
      // this hooks receive a parameter referencing the OB.OBPOSPointOfSale.Print.Receipt model
      this.legacyPrinter = printers.printer;
    }

    async synchronizeMessage(message) {
      switch (message.type) {
        case 'displayTotal':
          await this.displayTotal(message.messageObj);
          break;
        case 'greetHardwareManager':
          await this.greet();
          break;
        case 'printTicket':
          await this.printTickets(message.messageObj);
          break;
        case 'printTicketLine':
          await this.printTicketLine(message.messageObj);
          break;
        case 'printWelcome':
          await this.printWelcome();
          break;
        default:
          throw new Error(
            `Unkwnown Hardware Manager operation: ${message.type}`
          );
      }
    }

    async displayTotal(messageData) {
      try {
        const template = await this.templateStore.get(
          'printDisplayTotalTemplate'
        );
        await this.controller.display(template, {
          ticket: messageData.data.ticket
        });
      } catch (error) {
        OB.error(`Error displaying ticket total: ${error}`);
      }
    }

    async greet() {
      try {
        const data = await this.controller.getHardwareManagerStatus();

        if (Object.keys(data).length > 0) {
          await this.printWelcome();
        }

        // Save hardware manager information
        const { version, revision, javaInfo } = data;
        if (version) {
          // Max database string size: 10
          const hwmVersion =
            version.length > 10 ? version.substring(0, 9) : version;
          OB.UTIL.localStorage.setItem('hardwareManagerVersion', hwmVersion);
        }
        if (revision) {
          // Max database string size: 15
          const hwmRevision =
            revision.length > 15 ? revision.substring(0, 14) : revision;
          OB.UTIL.localStorage.setItem('hardwareManagerRevision', hwmRevision);
        }
        if (javaInfo) {
          OB.UTIL.localStorage.setItem('hardwareManagerJavaInfo', javaInfo);
        }
      } catch (error) {
        OB.error(`Error greeting hardware manager: ${error}`);
      }
    }

    async printTickets(messageData) {
      const { ticket } = messageData.data;
      const printSettings = messageData.data.printSettings || {};
      // print main ticket
      await this.printTicket(ticket, printSettings);
      // print related canceled ticket (if any)
      if (ticket.doCancelAndReplace && ticket.canceledorder) {
        const { negativeDocNo } = ticket;
        const canceledTicket = {
          ...ticket.canceledorder,
          ordercanceled: true,
          negativeDocNo
        };
        await this.printTicket(canceledTicket, printSettings);
      }
    }

    async printTicket(ticket, printSettings) {
      let isPdf = false;
      try {
        // Tickets with simplified invoice will print only the invoice
        if (ticket.calculatedInvoice && !ticket.calculatedInvoice.fullInvoice) {
          return;
        }

        const {
          cancelOperation,
          forcedtemplate
        } = await this.controller.executeHooks('OBPRINT_PrePrint', {
          forcePrint: printSettings.forcePrint,
          offline: printSettings.offline,
          ticket,
          forcedtemplate: printSettings.forcedtemplate,
          model: this.legacyPrinter ? this.legacyPrinter.model : null,
          cancelOperation: false
        });

        if (cancelOperation && cancelOperation === true) {
          return;
        }

        const terminal = OB.App.TerminalProperty.get('terminal');
        const { printableTicket, printableOrder } = toPrintable(ticket);

        if (!printableTicket.print) {
          return;
        }

        const template = await this.templateStore.selectTicketPrintTemplate(
          printableTicket,
          { forcedtemplate }
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

        const printedTicket = await this.controller.print(template, {
          ticket: ticketToPrint
        });

        if (
          shouldPrintTwice(printableTicket) ||
          terminal.terminalType.printTwice
        ) {
          await this.controller.print(template, { ticket: ticketToPrint });
        }

        await this.controller.executeHooks('OBPRINT_PostPrint', {
          ticket: printableOrder || printableTicket,
          printedTicket
        });

        await this.displayTicket(printableTicket);
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
        await this.printTicket(ticket, printSettings);
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

    async displayTicket(ticket) {
      const displayTicketTemplate = await this.templateStore.get(
        'displayReceiptTemplate'
      );
      await this.controller.display(displayTicketTemplate, {
        ticket
      });
    }

    async printTicketLine(messageData) {
      try {
        const template = await this.templateStore.get(
          'printReceiptLineTemplate'
        );
        await this.controller.display(template, {
          ticketLine: messageData.data.line
        });
      } catch (error) {
        OB.error(`Error printing ticket line: ${error}`);
      }
    }

    async printWelcome() {
      try {
        const template = await this.templateStore.get('printWelcomeTemplate');
        await this.controller.display(template);
      } catch (error) {
        OB.error(`Error displaying welcome message: ${error}`);
      }
    }
  }

  OB.App.Class.HardwareManagerEndpoint = HardwareManagerEndpoint;
})();
