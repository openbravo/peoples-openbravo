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
  /**
   * A synchronization endpoint in charge of the messages for communicating with the Hardware Manager.
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
        'printTicket',
        'printTicketLine',
        'printWelcome'
      );

      this.controller = new OB.App.Class.HardwareManagerController();
      this.templateStore = OB.App.PrintTemplateStore;
    }

    // Sets the printers
    setPrinters(printers) {
      this.printer = printers.printer;
      this.linePrinter = printers.linePrinter;
      this.welcomePrinter = printers.welcomePrinter;
    }

    async synchronizeMessage(message) {
      switch (message.type) {
        case 'displayTotal':
          await this.displayTotal(message.messageObj);
          break;
        case 'printTicket':
          await this.printTicket(message.messageObj);
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
        const template = this.templateStore.getDisplayTotalTemplate();
        await this.controller.display(template, {
          ticket: messageData.data.ticket
        });
      } catch (error) {
        OB.error(`Error displaying ticket total: ${error}`);
      }
    }

    async printTicket(messageData) {
      try {
        const { ticket } = messageData.data;
        const printSettings = messageData.data.printSettings || {};

        // Tickets with simplified invoice will print only the invoice
        if (ticket.calculatedInvoice && !ticket.calculatedInvoice.fullInvoice) {
          return;
        }

        let additionalPrintSettings;
        try {
          additionalPrintSettings = await this.executeHooks(
            'OBPRINT_PrePrint',
            printSettings
          );
        } catch (error) {
          return;
        }

        const terminal = OB.App.TerminalProperty.get('terminal');

        if (!(ticket.orderDate instanceof Date)) {
          ticket.orderDate = new Date(ticket.orderDate);
        }

        const negativeLines = ticket.lines.filter(line => line.qty < 0);

        const hasNegativeLines =
          negativeLines.length === ticket.lines.length ||
          (negativeLines.length > 0 &&
            OB.App.TerminalProperty.get('permissions')
              .OBPOS_SalesWithOneLineNegativeAsReturns);

        const isPrintable = line => {
          const { product } = line;
          if (product.productType === 'S' && !product.isPrintServices) {
            return false;
          }
          return true;
        };

        ticket.lines = ticket.lines.filter(
          line =>
            isPrintable(line) ||
            line.baseNetUnitAmount ||
            line.baseGrossUnitAmount
        );

        if (OB.App.State.Ticket.Utils.isNegative(ticket)) {
          ticket.payments = ticket.payments.map(payment => {
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

        const template = this.templateStore.selectTicketPrintTemplate(ticket, {
          forcedtemplate: additionalPrintSettings.forcedtemplate
        });

        if (template.ispdf) {
          const printPdfProcess = () => {
            template.dateFormat = OB.Format.date;
            if (ticket.canceledorder) {
              const clonedTicket = { ...ticket };
              delete clonedTicket.canceledorder;
              // TODO: implement printPDF
              // printPDF(clonedTicket, args);
            } else {
              // printPDF(ticket, args);
            }
            if (
              ((ticket.orderType === 1 || hasNegativeLines) &&
                !OB.App.Security.hasPermission('OBPOS_print.once', true)) ||
              terminal.terminalType.printTwice
            ) {
              // printPDF(ticket, args);
            }
          };

          if (
            terminal.terminalType.selectprinteralways &&
            !printSettings.skipSelectPrinters
          ) {
            // TODO -- implement printer selection dialog
            /* OB.OBPOS.showSelectPrintersWindow(
              printPdfProcess,
              cancelSelectPrinter,
              cancelSelectPrinter,
              true,
              me.isRetry
            ); */
          } else {
            printPdfProcess();
          }
        } else {
          if (
            terminal.terminalType.selectprinteralways &&
            !printSettings.skipSelectPrinters
          ) {
            /* OB.OBPOS.showSelectPrintersWindow(
              printProcess,
              cancelSelectPrinter,
              cancelSelectPrinter,
              false,
              me.isRetry
            ); */
          } else {
            await this.controller.print(template, { ticket });
          }

          if (ticket.doCancelAndReplace && ticket.canceledorder) {
            // const { negativeDocNo } = ticket;
            // TODO -- this:
            /* receipt.get('canceledorder').set('ordercanceled', true);
            receipt.get('canceledorder').set('negativeDocNo', negativeDocNo);
            me.print(receipt.get('canceledorder'), args); */
          }

          await this.controller.display(
            this.templateStore.getDisplayTicketTemplate(),
            {
              ticket
            }
          );
        }
      } catch (error) {
        OB.error(`Error printing ticket: ${error}`);
      }
    }

    // eslint-disable-next-line class-methods-use-this
    async executeHooks(hookName, printSettings) {
      // TODO -- complete data properties: order, template, model
      const data = {
        forcePrint: printSettings.forcePrint,
        offline: printSettings.offline,
        // order: receipt,
        // template: template,
        forcedtemplate: printSettings.forcedtemplate
        // model: me.model
      };

      if (!OB.UTIL.HookManager) {
        return data;
      }

      const finalData = await new Promise((resolve, reject) => {
        OB.UTIL.HookManager.executeHooks(hookName, data, args => {
          if (args.cancelOperation && args.cancelOperation === true) {
            reject();
          }
          resolve(args);
        });
      });

      return finalData;
    }

    async printTicketLine(messageData) {
      if (!this.linePrinter) {
        throw new Error(`The endpoint has no line printer assigned`);
      }
      try {
        const template = this.templateStore.getTicketLineTemplate();
        await this.controller.display(template, {
          ticketLine: messageData.data.line
        });
      } catch (error) {
        OB.error(`Error printing ticket line: ${error}`);
      }
    }

    async printWelcome() {
      try {
        const template = this.templateStore.getWelcomeTemplate();
        await this.controller.display(template);
      } catch (error) {
        OB.error(`Error displaying welcome message: ${error}`);
      }
    }
  }

  OB.App.Class.HardwareManagerEndpoint = HardwareManagerEndpoint;
})();
