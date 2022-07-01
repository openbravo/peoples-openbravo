/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Messages Model
 * @author Javier Armendáriz <javier.armendariz@openbravo.com>
 */
OB.App.StateAPI.Messages.registerUtilityFunctions({
  /**
   * Returns a list of messages to print kept cash tickets. One per copy set for
   * the current terminal. It returns [] if no ticket should be printed
   */
  createPrintCashupKeptCashMessages(cashupData, keptCashData, printSettings) {
    if (keptCashData.printablePaymentsCounted) {
      const keptCashupMessages = [];
      [...Array(keptCashData.numberOfCopies).keys()].forEach(() => {
        keptCashupMessages.push(
          OB.App.State.Messages.Utils.createNewMessage(
            'OBMOBC_PrintCashupKeptCash',
            '',
            { cashupData, keptCashData, printSettings },
            {
              type: 'printKeptCash',
              consumeOffline: true
            }
          )
        );
      });

      return keptCashupMessages;
    }

    return [];
  },

  /**
   * Creates the message required to print a ticket
   *
   * @param {array} messages - The Messages state
   * @param {object} ticket - The ticket to be printed
   * @param {object} printSettings - Extra print settings
   * @param {string} deliverAction - Defines the delivery action for ticket. It could be 'print'(default), 'send', 'printAndSend' or 'none'
   * @param {object} deliverService - Indicates the deliver service where the ticket should go. it contains
   * @param {string} deliverService.type - service type to be handled in BO (type of data for import entry)
   * @param {string} deliverService.to - destination to be sent
   * @returns New Messages array with the information to print and send (if necessary) of the ticket that can be synchronized by the SynchronizationBuffer
   */
  generateDeliverTicketMessages(
    messages,
    ticket,
    printSettings = {},
    deliverAction = 'print',
    deliverService = {}
  ) {
    const deliverParams = { ticket, printSettings };
    const templateName = OB.App.State.Ticket.Utils.getTicketTemplateName(
      ticket,
      printSettings
    );
    const printParams = {
      modelName: 'OBMOBC_PrintTicket',
      serviceName: '',
      extraProperties: { type: 'printTicket', consumeOffline: true }
    };
    const sendParams = {
      templateName,
      deliverService
    };

    return OB.App.State.Messages.Utils.generateDeliverDocumentMessages(
      messages,
      deliverAction,
      deliverParams,
      printParams,
      sendParams
    );
  }
});
