/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Defines cancelCashManagement action
 * @author Miguel de Juana <miguel.dejuana@openbravo.com>
 */
(function cashManagementActionsDefinition() {
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
