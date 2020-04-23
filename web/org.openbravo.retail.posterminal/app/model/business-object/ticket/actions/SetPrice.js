/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global lodash */

(function SetQuantityDefinition() {
  OB.App.StateAPI.Ticket.registerAction('setPrice', (state, payload) => {
    const ticket = { ...state };
    const { lineIds, price, reason } = payload;

    ticket.lines = ticket.lines.map(l => {
      if (!lineIds.includes(l.id)) {
        return l;
      }

      const newLine = { ...l, price };

      if (reason) {
        newLine.oBPOSPriceModificationReason = reason;
      } else {
        delete newLine.oBPOSPriceModificationReason;
      }

      return newLine;
    });

    return ticket;
  });

  function checkParameters(ticket, lineIds, price) {
    if (lineIds === undefined) {
      throw new Error('lineIds parameter is mandatory');
    }

    if (!(lineIds instanceof Array)) {
      throw new Error('lineIds parameter must be an array of Ids');
    }

    const ticketLineIds = ticket.lines.map(l => l.id);
    const notPresentLineIds = lineIds.filter(
      lid => !ticketLineIds.includes(lid)
    );
    if (notPresentLineIds.length !== 0) {
      throw new Error(`not found lineIds: [${notPresentLineIds.join(',')}]`);
    }

    if (price === undefined) {
      throw new Error('price parameter is mandatory');
    }

    if (!lodash.isNumber(price)) {
      throw new Error(`price is not numeric: ${price}`);
    }

    if (price < 0) {
      throw new Error('Cannot set price less than 0');
    }
  }

  function checkRestrictions(ticket, lines, price) {
    if (ticket.isEditable === false) {
      throw new OB.App.Class.ActionCanceled(
        'Setting price not editable ticket',
        {
          errorConfirmation: 'OBPOS_modalNoEditableBody'
        }
      );
    }

    if (
      lines.some(
        l =>
          !l.product.obposEditablePrice || l.product.isEditablePrice === false
      )
    ) {
      throw new OB.App.Class.ActionCanceled(
        'Setting price not editable ticket',
        {
          errorConfirmation: 'OBPOS_modalNoEditableLineBody'
        }
      );
    }

    if (lines.some(l => l.replacedorderline && l.qty < 0)) {
      throw new OB.App.Class.ActionCanceled(
        'Setting price to replaced return lines',
        {
          errorConfirmation: 'OBPOS_CancelReplaceReturnPriceChange'
        }
      );
    }

    // in a verified return it's only allowed to change price with permissions
    // and even with permissions only to decrease the price
    const canModifyVerifiedReturn =
      !ticket.isPaid &&
      OB.App.Security.hasPermission('OBPOS_ModifyPriceVerifiedReturns');
    if (
      lines.some(
        l =>
          !l.isEditable &&
          !(canModifyVerifiedReturn && l.originalDocumentNo && price < l.price)
      )
    ) {
      throw new OB.App.Class.ActionCanceled('Cannot change price', {
        errorMsg: 'OBPOS_CannotChangePrice'
      });
    }
  }

  async function checkApprovals(payload, lines) {
    // TODO: check if this logic is correct (moved from old implementation):
    // request approval if there is at least one Item product, what has services to do with this?
    if (
      !OB.App.Security.hasPermission('OBPOS_ChangeServicePriceNeedApproval') &&
      lines.some(l => l.product.productType === 'I')
    ) {
      const newPayload = await OB.App.Security.requestApprovalForAction(
        'OBPOS_approval.setPrice',
        payload
      );
      return newPayload;
    }
    return payload;
  }

  OB.App.StateAPI.Ticket.setPrice.addActionPreparation(
    async (state, payload) => {
      const ticket = state.Ticket;
      const { price, lineIds } = payload;

      checkParameters(ticket, lineIds, price);

      const lines = ticket.lines.filter(l => lineIds.includes(l.id));
      checkRestrictions(ticket, lines, price);

      const payloadWithApprovals = await checkApprovals(payload, lines);
      return payloadWithApprovals;
    }
  );
})();
