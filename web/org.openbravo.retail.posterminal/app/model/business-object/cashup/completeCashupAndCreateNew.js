/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Defines the completeCashupAndCreateNew action
 */
(function completeCashupAndCreateNewDefinition() {
  /**
   * Complete the cashup and create a new one
   */
  OB.App.StateAPI.Global.registerActions({
    /**
     * After process a cashup in the cashup window, this method is called, it sends the current cashup to the backend.
     * In the message of the cashup to the backend also adds all the edited info in the cashup window.
     * And after that, creates a new empty cashup
     * @param {*} state
     * @param {*} payload.completedCashupParams params for the cashup to complete and send to backend, including the closeCashupInfo
     * @param {*} payload.newCashupParams params with info for create the new cashup
     */
    completeCashupAndCreateNew(state, payload) {
      const newState = { ...state };
      const oldCashup = { ...newState.Cashup };

      oldCashup.isprocessed = 'Y';

      // create new message with current cashup
      const {
        closeCashupInfo,
        terminalName,
        cacheSessionId
      } = payload.completedCashupParams;
      const newMessagePayload = {
        id: OB.App.UUID.generate(),
        terminal: terminalName,
        cacheSessionId,
        data: [{ ...oldCashup, ...closeCashupInfo }]
      };
      const newMessage = OB.App.State.Messages.Utils.createNewMessage(
        'Cash Up',
        'org.openbravo.retail.posterminal.ProcessCashClose',
        newMessagePayload
      );
      newState.Messages = [...newState.Messages, newMessage];

      // initialize the new cashup
      let newCashup = {};
      const {
        terminalIsSlave,
        terminalIsMaster,
        terminalPayments,
        currentDate,
        userId,
        terminalId
      } = payload.newCashupParams;

      OB.App.State.Cashup.Utils.resetStatistics();

      newCashup = OB.App.State.Cashup.Utils.createNewCashupFromScratch({
        newCashup,
        payload: { currentDate, userId, posterminal: terminalId }
      });

      const lastCashUpPayments = closeCashupInfo.cashCloseInfo;

      newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.initializePaymentMethodCashup(
        {
          terminalPayments,
          lastCashUpPayments,
          terminalIsSlave,
          newCashup
        }
      );

      newState.Cashup = newCashup;
      if (terminalIsSlave || terminalIsMaster) {
        const newMessagePayloadCashup = {
          id: OB.App.UUID.generate(),
          terminal: terminalName,
          cacheSessionId,
          data: [newCashup]
        };
        const newMessageCashup = OB.App.State.Messages.Utils.createNewMessage(
          'Cash Up',
          'org.openbravo.retail.posterminal.ProcessCashClose',
          newMessagePayloadCashup
        );
        newState.Messages = [...newState.Messages, newMessageCashup];
      }

      return newState;
    }
  });
})();
