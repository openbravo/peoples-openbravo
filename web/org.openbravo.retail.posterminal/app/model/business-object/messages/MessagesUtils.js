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
  // terminalPayments: OB.MobileApp.model.get('payments'),
  // terminalName: OB.MobileApp.model.get('terminal').searchKey,
  createPrintCashupKeptCashMessages(cashupData, printSettings) {
    return [];
    // return OB.App.State.Messages.Utils.createNewMessage(
    //   'OBMOBC_PrintCashupKeptCash',
    //   '',
    //   { cashupData, printSettings },
    //   {
    //     type: 'printCashup',
    //     consumeOffline: true
    //   }
    // );
  }
});
