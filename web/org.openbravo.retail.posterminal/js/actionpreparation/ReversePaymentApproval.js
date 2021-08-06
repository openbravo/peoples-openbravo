/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.App.StateAPI.Ticket.reversePayment.addActionPreparation(
    async (ticket, payload) => {
      const payloadWithApprovals = await checkApprovals(payload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    400
  );

  async function checkApprovals(payload) {
    const usedPayment = OB.App.State.Ticket.Utils.getUsedPayment(payload);
    const usedPaymentMethod = usedPayment[0].paymentMethod;
    const currentDate = new Date();
    currentDate.setHours(0);
    currentDate.setMinutes(0);
    currentDate.setSeconds(0);
    currentDate.setMilliseconds(0);

    if (
      usedPaymentMethod.isreversable &&
      !(
        !(
          usedPaymentMethod.availableReverseDelay === null ||
          usedPaymentMethod.availableReverseDelay === undefined
        ) &&
        currentDate.getTime() <=
          new Date(payload.payment.reversedPayment.paymentDate).getTime() +
            usedPaymentMethod.availableReverseDelay * 86400000
      )
    ) {
      const newPayload = await OB.App.Security.requestApprovalForAction(
        'OBPOS_approval.reversePayment',
        payload
      );
      return newPayload;
    }
    return payload;
  }
})();