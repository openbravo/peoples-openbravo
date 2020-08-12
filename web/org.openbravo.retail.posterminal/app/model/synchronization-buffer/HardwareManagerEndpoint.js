/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the HardwareMaangerEndpoint class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function HardwareManagerEndpointDefinition() {
  // Turns an state ticket into a backbone order
  const toOrder = ticket => {
    if (!ticket.id) {
      // force to have a ticket id: if no id is provided an empty backbone order is created
      // eslint-disable-next-line no-param-reassign
      ticket.id = OB.App.UUID.generate();
    }
    return OB.App.StateBackwardCompatibility.getInstance(
      'Ticket'
    ).toBackboneObject(ticket);
  };

  // Turns a JSON representation of a backbone order line into a backbone order line
  const toOrderLine = line => {
    return new OB.Model.OrderLine(line);
  };

  /**
   * A synchronization endpoint in charge of the messages for communicating with the Hardware Manager.
   */
  class HardwareManagerEndpoint extends OB.App.Class.SynchronizationEndpoint {
    constructor(name) {
      super(name || 'HardwareManager');
      this.messageTypes.push(
        'displayTotal',
        'printTicket',
        'printTicketLine',
        'printWelcome'
      );
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
          this.displayTotal(message.messageObj);
          break;
        case 'printTicket':
          await this.printTicket(message.messageObj);
          break;
        case 'printTicketLine':
          this.printTicketLine(message.messageObj);
          break;
        case 'printWelcome':
          this.printWelcome();
          break;
        default:
          throw new Error(
            `Unkwnown Hardware Manager operation: ${message.type}`
          );
      }
    }

    displayTotal(messageData) {
      if (!this.printer) {
        throw new Error(`The endpoint has no printer assigned`);
      }
      try {
        const order = toOrder(messageData.data.ticket);
        this.printer.doDisplayTotal(order);
      } catch (error) {
        OB.error(`Error displaying total: ${error}`);
      }
    }

    async printTicket(messageData) {
      if (!this.printer) {
        throw new Error(`The endpoint has no printer assigned`);
      }
      try {
        const order = toOrder(messageData.data.ticket);
        await this.printer.doPrint(order, {
          ...messageData.data.printSettings
        });
      } catch (error) {
        OB.error(`Error printing ticket: ${error}`);
      }
    }

    printTicketLine(messageData) {
      if (!this.linePrinter) {
        throw new Error(`The endpoint has no line printer assigned`);
      }
      try {
        const line = toOrderLine(messageData.data.line);
        this.linePrinter.doPrint(line);
      } catch (error) {
        OB.error(`Error printing ticket line: ${error}`);
      }
    }

    printWelcome() {
      if (!this.welcomePrinter) {
        throw new Error(`The endpoint has no printer assigned`);
      }
      this.welcomePrinter.doPrintWelcome();
    }
  }

  OB.App.Class.HardwareManagerEndpoint = HardwareManagerEndpoint;
})();
