/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Defines the action of creating a cash management, adding it in Messages and inside the corresponding Cashup Payment method
 */
(function createCashManagementDefinition() {
  /**
   * Create a cash management,  add it in Messages and inside the corresponding Cashup Payment method
   */
  OB.App.StateAPI.Cashup.registerActions({
    createCashManagement(state, payload) {
      const cashup = { ...state };

      const cashManagement = JSON.parse(JSON.stringify(payload.cashManagement));
      cashManagement.isDraft = true;

      // Save the cash management in the Cashup
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
   * Create a cash management,  add it in Messages and inside the corresponding Cashup Payment method
   */
  OB.App.StateAPI.Global.registerActions({
    processCashManagements(state, payload) {
      const newState = { ...state };
      const cashup = { ...newState.Cashup };

      // Save the cash management in the Cashup
      cashup.cashPaymentMethodInfo = cashup.cashPaymentMethodInfo.map(
        paymentMethod => {
          if (paymentMethod.cashManagements.length > 0) {
            const newPaymentMethod = { ...paymentMethod };
            newPaymentMethod.cashManagements = paymentMethod.cashManagements.map(
              cashManagement => {
                if (!cashManagement.isDraft) {
                  return cashManagement;
                }
                const newCashManagement = { ...cashManagement };
                delete newCashManagement.isDraft;
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
   * Create a cash management,  add it in Messages and inside the corresponding Cashup Payment method
   */
  OB.App.StateAPI.Cashup.registerActions({
    cancelCashManagements(state) {
      const cashup = { ...state };

      // Save the cash management in the Cashup
      cashup.cashPaymentMethodInfo = cashup.cashPaymentMethodInfo.map(
        paymentMethod => {
          if (paymentMethod.cashManagements.length === 0) {
            return paymentMethod;
          }
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
