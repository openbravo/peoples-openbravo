/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Defines actions for cash managements.
 * @author Miguel de Juana <miguel.dejuana@openbravo.com>
 */
(function cashManagementActionsDefinition() {
  /**
   * Create a cash management: add it in draft mode inside the corresponding Cashup Payment method of the Cashup(State)
   */
  OB.App.StateAPI.Cashup.registerActions({
    createCashManagement(cashup, payload) {
      const newCashup = { ...cashup };

      const cashManagement = { ...payload.cashManagement };
      cashManagement.isDraft = true;

      newCashup.cashPaymentMethodInfo = newCashup.cashPaymentMethodInfo.map(
        paymentMethod => {
          if (
            paymentMethod.paymentMethodId !== cashManagement.paymentMethodId
          ) {
            return paymentMethod;
          }
          const cashManagements = [
            ...paymentMethod.cashManagements,
            cashManagement
          ];
          return { ...paymentMethod, cashManagements };
        }
      );

      return newCashup;
    }
  });

  /**
   * Process cash managemets: Set as processed cash managements in draft and move to Messages(State)
   */
  OB.App.StateAPI.Global.registerActions({
    processCashManagements(state, payload) {
      const newState = { ...state };
      const newCashup = { ...newState.Cashup };
      const { terminalPayments } = payload.parameters;
      const cashManagementsToProcess = [];

      newCashup.cashPaymentMethodInfo = newCashup.cashPaymentMethodInfo.map(
        paymentMethod => {
          const newPaymentMethod = { ...paymentMethod };
          if (paymentMethod.cashManagements.length > 0) {
            newPaymentMethod.cashManagements = paymentMethod.cashManagements.map(
              cashManagement => {
                // Cash management already processed
                if (!cashManagement.isDraft) {
                  return cashManagement;
                }

                // Remove isDraft to set the cash management as processed
                const newCashManagement = { ...cashManagement };
                delete newCashManagement.isDraft;

                if (newCashManagement.type === 'deposit') {
                  newPaymentMethod.totalDeposits = OB.DEC.add(
                    newPaymentMethod.totalDeposits,
                    newCashManagement.amount
                  );
                } else if (newCashManagement.type === 'drop') {
                  newPaymentMethod.totalDrops = OB.DEC.add(
                    newPaymentMethod.totalDrops,
                    newCashManagement.amount
                  );
                }

                // Update the Cashup information with cash management to process
                const cashupToSend = { ...newCashup };
                cashupToSend.cashPaymentMethodInfo = cashupToSend.cashPaymentMethodInfo.map(
                  cashupReportPaymentMethod => {
                    if (cashupReportPaymentMethod.id === newPaymentMethod.id) {
                      const cashupReportNewPaymentMethod = {
                        ...newPaymentMethod
                      };
                      cashupReportNewPaymentMethod.usedInCurrentTrx = true;
                      return cashupReportNewPaymentMethod;
                    }
                    return cashupReportPaymentMethod;
                  }
                );

                newCashManagement.cashUpReportInformation = cashupToSend;

                const cashupPayments = cashupToSend.cashPaymentMethodInfo;
                cashupToSend.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.getCashupPaymentsThatAreAlsoInTerminalPayments(
                  cashupPayments,
                  terminalPayments
                );
                cashManagementsToProcess.push(newCashManagement);
                return newCashManagement;
              }
            );
          }
          delete newPaymentMethod.newPaymentMethod;
          delete newPaymentMethod.usedInCurrentTrx;
          return newPaymentMethod;
        }
      );

      // Create a Message to synchronize the Cash Management
      const { terminalName, cacheSessionId } = payload.parameters;
      cashManagementsToProcess
        .sort((a, b) => new Date(b.date) - new Date(a.date))
        .forEach(function createMessage(cashManagementToProcess) {
          const newMessagePayload = {
            id: OB.App.UUID.generate(),
            terminal: terminalName,
            cacheSessionId,
            data: [cashManagementToProcess]
          };
          const newMessage = OB.App.State.Messages.Utils.createNewMessage(
            'Cash Management',
            'org.openbravo.retail.posterminal.ProcessCashMgmt',
            newMessagePayload
          );
          newState.Messages = [...newState.Messages, newMessage];
        });

      newState.Cashup = newCashup;

      return newState;
    }
  });

  /**
   * Cancel cash managements in Draft: Remove cash managements not processed yet
   */
  OB.App.StateAPI.Cashup.registerActions({
    cancelCashManagements(cashup) {
      const newCashup = { ...cashup };

      newCashup.cashPaymentMethodInfo = newCashup.cashPaymentMethodInfo.map(
        paymentMethod => {
          if (paymentMethod.cashManagements.length === 0) {
            return paymentMethod;
          }

          // Get only processed cash managements
          const cashManagements = paymentMethod.cashManagements.filter(
            cashManagement => !cashManagement.isDraft
          );

          return { ...paymentMethod, cashManagements };
        }
      );

      return newCashup;
    }
  });
})();
