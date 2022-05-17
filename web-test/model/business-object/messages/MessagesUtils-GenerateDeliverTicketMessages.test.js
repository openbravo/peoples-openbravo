/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
require('../global/SetupGlobal');
require('../global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

describe('Messages Utils generateDeliverTicketMessages function', () => {
  it('should add deliver ticket message', () => {
    const messages = deepfreeze([]);
    const ticket = deepfreeze({ id: 'Ticket01' });

    const newMessages = OB.App.State.Messages.Utils.generateDeliverTicketMessages(
      messages,
      ticket
    );

    expect(newMessages).toEqual([
      {
        consumeOffline: true,
        messageObj: {
          data: {
            printSettings: {},
            ticket: {
              id: 'Ticket01'
            }
          }
        },
        modelName: 'OBMOBC_PrintTicket',
        name: 'OBMOBC_PrintTicket',
        type: 'printTicket',
        time: expect.any(Number),
        service: ''
      }
    ]);
  });
});
