/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Defines createCashManagement action
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
})();
