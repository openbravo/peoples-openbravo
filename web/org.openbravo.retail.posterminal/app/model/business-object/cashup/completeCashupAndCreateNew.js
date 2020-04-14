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
   * Complete the cashup
   */
  OB.App.StateAPI.Global.registerActions({
    completeCashupAndCreateNew(state, payload) {
      const newState = { ...state };

      // read cashup from cashup window,
      const { cashupWindowCashup } = payload.completeCashup;

      const oldCashup = { ...newState.Cashup };

      const objToSend = JSON.parse(cashupWindowCashup.get('objToSend'));
      oldCashup.cashCloseInfo = objToSend.cashCloseInfo;
      oldCashup.cashMgmtIds = objToSend.cashMgmtIds;
      oldCashup.cashUpDate = objToSend.cashUpDate;
      oldCashup.timezoneOffset = objToSend.timezoneOffset;
      oldCashup.lastcashupeportdate = objToSend.lastcashupeportdate;
      oldCashup.approvals = objToSend.approvals;

      oldCashup.isprocessed = 'Y';

      // create new message with current cashup
      const { terminal, cacheSessionId } = payload.completeCashup;
      const newMessagePayload = {
        id: OB.App.UUID.generate(),
        terminal,
        cacheSessionId,
        data: [oldCashup]
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
        terminalPayments,
        currentDate,
        userId,
        posterminal
      } = payload.createCashup;

      OB.App.State.Cashup.Utils.resetStatistics();

      newCashup = OB.App.State.Cashup.Utils.createNewCashupFromScratch({
        newCashup,
        payload: { currentDate, userId, posterminal }
      });

      const lastCashUpPayments = oldCashup.cashCloseInfo;

      newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.initializePaymentMethodCashup(
        {
          terminalPayments,
          lastCashUpPayments,
          terminalIsSlave
        }
      );

      newState.Cashup = newCashup;

      return newState;
    }
  });
  OB.App.StateAPI.Global.completeCashupAndCreateNew.addActionPreparation(
    async (state, payload) => {
      const newPayload = { ...payload };

      newPayload.createCashup = {
        currentDate: OB.App.Date.getDate(),
        userId: OB.MobileApp.model.get('context').user.id,
        posterminal: OB.MobileApp.model.get('terminal').id,
        terminalIsSlave: OB.POS.modelterminal.get('terminal').isslave,
        terminalPayments: OB.MobileApp.model.get('payments')
      };

      newPayload.completeCashup = {
        ...newPayload.completeCashup,
        terminal: OB.MobileApp.model.get('logConfiguration').deviceIdentifier,
        cacheSessionId: OB.UTIL.localStorage.getItem('cacheSessionId')
      };

      return newPayload;
    }
  );
})();
