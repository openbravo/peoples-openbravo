/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupCashup');
const deepfreeze = require('deepfreeze');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic.js');
require('./SetupCashupUtils');

const ticketBeforeUpdateCashup = require('./test-data/ticketBeforeUpdateCashup');
deepfreeze(ticketBeforeUpdateCashup);
const ticketAfterTicketDone = require('./test-data/ticketAfterTicketDone');
deepfreeze(ticketAfterTicketDone);
const cashupBeforeTicketDone = require('./test-data/cashupBeforeTicketDone');
deepfreeze(cashupBeforeTicketDone);
const cashupAfterTicketDone = require('./test-data/cashupAfterTicketDone');
deepfreeze(cashupAfterTicketDone);
const terminalPayments = require('./test-data/terminalPayments');
deepfreeze(terminalPayments);

describe('Cashup - updateCashup function', () => {
  it('updateCashup in ticket done', () => {
    const expectedResult = {
      ticket: ticketAfterTicketDone,
      cashup: cashupAfterTicketDone
    };
    deepfreeze(expectedResult);
    const result = OB.App.State.Cashup.Utils.updateCashupFromTicket(
      ticketBeforeUpdateCashup,
      cashupBeforeTicketDone,
      {
        terminal: {
          countLayawayAsSales: true
        },
        payments: terminalPayments
      }
    );
    expect(result).toEqual(expectedResult);
  });
});
