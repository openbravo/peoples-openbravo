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
    createCashManagement(state, payload) {
      const cashup = { ...state };

      const cashManagement = JSON.parse(JSON.stringify(payload.cashManagement));
      cashManagement.isDraft = true;

      cashup.cashPaymentMethodInfo = cashup.cashPaymentMethodInfo.map(
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

      return cashup;
    }
  });

  /**
   * Process cash managemets: Set as processed cash managements in draft and move to Messages(State)
   */
  OB.App.StateAPI.Global.registerActions({
    processCashManagements(state, payload) {
      const newState = { ...state };
      const cashup = { ...newState.Cashup };

      cashup.cashPaymentMethodInfo = cashup.cashPaymentMethodInfo.map(
        paymentMethod => {
          if (paymentMethod.cashManagements.length > 0) {
            const newPaymentMethod = { ...paymentMethod };
            newPaymentMethod.cashManagements = paymentMethod.cashManagements.map(
              cashManagement => {
                // Cash management already processed
                if (!cashManagement.isDraft) {
                  return cashManagement;
                }

                // Remove isDraft to set the cash management as processed
                const newCashManagement = { ...cashManagement };
                delete newCashManagement.isDraft;

                // Update the Cashup information with cash management to process
                newCashManagement.cashUpReportInformation = { ...cashup };
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

                // Optimization: don't send all payments, only the one used in the cash management
                newCashManagement.cashUpReportInformation.cashPaymentMethodInfo = [
                  { ...newPaymentMethod }
                ];

                // Create a Message to synchronize the Cash Management
                const { terminalName, cacheSessionId } = payload.parameters;
                const newMessagePayload = {
                  id: OB.App.UUID.generate(),
                  terminal: terminalName,
                  cacheSessionId,
                  data: [newCashManagement]
                };
                const newMessage = OB.App.State.Messages.Utils.createNewMessage(
                  'Cash Management',
                  'org.openbravo.retail.posterminal.ProcessCashMgmt',
                  newMessagePayload
                );
                newState.Messages = [...newState.Messages, newMessage];

                return newCashManagement;
              }
            );
            return newPaymentMethod;
          }
          return paymentMethod;
        }
      );

      newState.Cashup = cashup;

      return newState;
    }
  });

  /**
   * Cancel cash managements in Draft: Remove cash managements not processed yet
   */
  OB.App.StateAPI.Cashup.registerActions({
    cancelCashManagements(state) {
      const cashup = { ...state };

      cashup.cashPaymentMethodInfo = cashup.cashPaymentMethodInfo.map(
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

      return cashup;
    }
  });
})();
