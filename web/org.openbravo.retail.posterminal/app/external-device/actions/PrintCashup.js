/*
 ************************************************************************************
 * Copyright (C) 2021-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.App.StateAPI.Global.registerAction('printCashup', (state, payload) => {
  const newState = { ...state };

  const printCashupMsg = OB.App.State.Messages.Utils.createPrintCashupMessage(
    payload
  );

  const keptCashMessages = OB.App.State.Messages.Utils.createPrintCashupKeptCashMessages(
    payload
  );

  newState.Messages = [
    ...newState.Messages,
    printCashupMsg,
    ...keptCashMessages
  ];

  return newState;
});
