/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Defines the initCashup action
 */
(function initCashuplDefinition() {
  /**
   * Initialize the cashup
   */
  OB.App.StateAPI.Cashup.registerActions({
    initCashup(cashup, payload) {
      let newCashup;

      const { initCashupFrom, terminalPayments, lastCashUpPayments } = payload;
      if (initCashupFrom === 'local') {
        // init from local
        newCashup = OB.App.State.Cashup.Utils.addNewPaymentMethodsToCurrentCashup(
          { cashup, terminalPayments: payload.terminalPayments }
        );
      } else if (initCashupFrom === 'backend') {
        // init from backend
        const { currentCashupFromBackend } = payload;

        OB.App.State.Cashup.Utils.resetStatistics();

        newCashup = OB.App.State.Cashup.Utils.createNewCashupFromBackend({
          cashup,
          currentCashupFromBackend
        });

        if (currentCashupFromBackend.cashPaymentMethodInfo.length !== 0) {
          newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.addPaymentsFromBackendCashup(
            { currentCashupFromBackend, terminalPayments }
          );
        } else {
          newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.initializePaymentMethodCashup(
            {
              terminalPayments
            }
          );
        }
      } else if (initCashupFrom === 'scratch') {
        // init from scratch
        const { terminalIsSlave } = payload;

        OB.App.State.Cashup.Utils.resetStatistics();

        newCashup = OB.App.State.Cashup.Utils.createNewCashupFromScratch({
          cashup,
          payload
        });

        newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.initializePaymentMethodCashup(
          {
            terminalPayments,
            lastCashUpPayments,
            terminalIsSlave
          }
        );
      }
      return newCashup;
    }
  });

  OB.App.StateAPI.Cashup.initCashup.addActionPreparation(
    async (state, payload) => {
      const newPayload = { ...payload };

      if (OB.App.State.Cashup.Utils.isValidTheLocalCashup(state.Cashup)) {
        // init from local
        newPayload.initCashupFrom = 'local';
      } else {
        const backendCashupData = await OB.App.State.Cashup.Utils.requestNoProcessedCashupFromBackend();
        if (
          OB.App.State.Cashup.Utils.isValidTheBackendCashup(backendCashupData)
        ) {
          // init from backend
          newPayload.initCashupFrom = 'backend';
          // eslint-disable-next-line prefer-destructuring
          newPayload.currentCashupFromBackend = backendCashupData[0];
        } else {
          // init from scratch
          newPayload.initCashupFrom = 'scratch';
          const lastBackendCashupData = await OB.App.State.Cashup.Utils.requestProcessedCashupFromBackend();
          if (lastBackendCashupData[0]) {
            // payments from backend last cashup
            newPayload.lastCashUpPayments =
              lastBackendCashupData[0].cashPaymentMethodInfo;
          } else {
            if (lastBackendCashupData.exception) {
              // error reading payments of backend last cashup, show popup and reload
              OB.App.State.Cashup.Utils.showPopupNotEnoughDataInCache();
              throw new OB.App.Class.ActionCanceled('notEnoughDataInCache');
            }
            // is the first cashup of the terminal, initialize all payments to 0
            newPayload.lastCashUpPayments = null;
          }
        }
      }
      newPayload.creationDate = new Date().toISOString();
      newPayload.currentDate = new Date();
      newPayload.userId = OB.MobileApp.model.get('context').user.id;
      newPayload.posterminal = OB.MobileApp.model.get('terminal').id;
      newPayload.terminalIsSlave = OB.POS.modelterminal.get('terminal').isslave;

      newPayload.terminalPayments = [...OB.MobileApp.model.get('payments')];

      return newPayload;
    }
  );
})();
