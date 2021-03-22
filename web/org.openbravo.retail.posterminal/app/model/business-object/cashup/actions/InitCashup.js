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
  OB.App.StateAPI.Global.registerActions({
    /**
     * Initialize a new cashup
     * - If local cashup is ok, it will use it
     * - In case there is no a local cashup, will try to load from backend
     * - In case there is no a open cashup in the backend will create a new one from scratch
     *   For this case will try to load the previous procesed cashup from the backend to know wich are the left cash
     * @param {*} state
     * @param {*} payload.terminalIsSlave/terminalIsMaster if using shared payment methods
     * @param {*} payload.initCashupFrom local, backend form scratch
     * @param {*} payload.terminalPayments the payments loaded in the terminal request
     * @param {*} payload.lastCashUpPayments the payments from the previous cashup, to get the left cash
     * @param {*} payload.terminalName/cacheSessionId info of the terminal and session
     */
    initCashup(state, payload) {
      const newState = { ...state };
      const cashup = { ...newState.Cashup };
      let newCashup;

      const {
        terminalIsSlave,
        terminalIsMaster,
        initCashupFrom,
        terminalPayments,
        lastCashUpPayments,
        terminalName,
        cacheSessionId
      } = payload;
      if (initCashupFrom === 'local') {
        // init from local
        newCashup = OB.App.State.Cashup.Utils.addNewPaymentMethodsToCurrentCashup(
          { cashup, terminalPayments: payload.terminalPayments }
        );
      } else if (initCashupFrom === 'backend') {
        // init from backend
        const { currentCashupFromBackend } = payload;

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
              terminalPayments,
              newCashup
            }
          );
        }
      } else if (initCashupFrom === 'scratch') {
        // init from scratch
        const { currentDate, userId, organization, terminalId } = payload;

        newCashup = OB.App.State.Cashup.Utils.createNewCashupFromScratch({
          cashup,
          payload: {
            currentDate,
            userId,
            organization,
            posterminal: terminalId
          }
        });

        newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.initializePaymentMethodCashup(
          {
            terminalPayments,
            lastCashUpPayments,
            terminalIsSlave,
            newCashup
          }
        );
      }
      newState.Cashup = newCashup;
      if (terminalIsSlave || terminalIsMaster) {
        const newMessagePayload = {
          id: OB.App.UUID.generate(),
          terminal: terminalName,
          cacheSessionId,
          data: [newCashup]
        };
        const newMessage = OB.App.State.Messages.Utils.createNewMessage(
          'OBPOS_CashUp',
          'org.openbravo.retail.posterminal.ProcessCashClose',
          newMessagePayload,
          {
            ...payload.extraProperties,
            name: 'OBPOS_CashUp'
          }
        );
        newState.Messages = [...newState.Messages, newMessage];
      }
      return newState;
    }
  });

  OB.App.StateAPI.Global.initCashup.addActionPreparation(
    async (state, payload) => {
      const newPayload = { ...payload };

      if (OB.App.State.Cashup.Utils.isValidTheLocalCashup(state.Cashup)) {
        // init from local
        newPayload.initCashupFrom = 'local';
      } else {
        const backendCashupResponse = await OB.App.State.Cashup.Utils.requestNoProcessedCashupFromBackend();
        if (
          !backendCashupResponse.error &&
          OB.App.State.Cashup.Utils.isValidTheBackendCashup(
            backendCashupResponse.data
          )
        ) {
          // init from backend
          newPayload.initCashupFrom = 'backend';
          newPayload.currentCashupFromBackend = {
            ...backendCashupResponse.data[0]
          };
          newPayload.currentCashupFromBackend.totalStartings = OB.DEC.Zero;
        } else {
          // init from scratch
          newPayload.initCashupFrom = 'scratch';
          const lastBackendCashupResponse = await OB.App.State.Cashup.Utils.requestProcessedCashupFromBackend();
          if (lastBackendCashupResponse.error) {
            // error reading payments of backend last cashup
            throw new OB.App.Class.ActionCanceled(
              lastBackendCashupResponse.error.message
            );
          }
          newPayload.lastCashUpPayments = null;
          if (lastBackendCashupResponse.data[0]) {
            // payments from backend last cashup
            newPayload.lastCashUpPayments =
              lastBackendCashupResponse.data[0].cashPaymentMethodInfo;
          }
        }
      }
      return newPayload;
    }
  );
})();
