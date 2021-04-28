/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket action that creates a replace ticket in the state
 */

(() => {
  OB.App.StateAPI.Ticket.registerAction(
    'createReplaceTicket',
    (ticket, payload) => {
      const newTicket = {
        ...ticket,
        id: OB.App.UUID.generate(),
        replacedorder: ticket.id,
        orderid: undefined,
        orderDate: new Date(),
        creationDate: null,
        createdBy: payload.orgUserId,
        session: payload.session,
        hasbeenpaid: 'N',
        isPaid: false,
        isEditable: true,
        doCancelAndReplace: true,
        isLayaway: false,
        orderType:
          ticket.isLayaway ||
          (payload.terminal.terminalType.layawayorder &&
            !ticket.lines.some(line => line.deliveredQuantity > 0))
            ? 2
            : ticket.orderType,
        invoiceCreated: undefined,
        documentNo: OB.App.State.DocumentSequence.Utils.calculateReplaceDocumentNumber(
          ticket.documentNo,
          {
            replaceNumberSeparator: payload.terminal.cancelAndReplaceSeparator,
            documentNumberPadding: payload.terminal.documentnoPadding
          }
        ),
        replacedorder_documentNo: ticket.documentNo,
        negativeDocNo: `${ticket.documentNo}*R*`,
        documentType: payload.terminal.terminalType.documentType,
        cashVAT: payload.terminal.cashVat,
        posTerminal: payload.terminal.id,
        salesRepresentative:
          ticket.salesRepresentative || payload.context.isSalesRepresentative
            ? payload.context.user.id
            : undefined,
        paidOnCredit: false,
        paidPartiallyOnCredit: false,
        creditAmount: OB.DEC.Zero,
        canceledorder: ticket
      };

      newTicket.lines = ticket.lines
        .map(line => {
          const newLine = {
            ...line,
            id: OB.App.UUID.generate(),
            replacedorderline: line.id,
            invoicedQuantity: undefined,
            grossUnitPrice: undefined,
            lineGrossAmount: undefined,
            obposIspaid: false,
            documentType: newTicket.documentType,
            obposCanbedelivered:
              line.obposCanbedelivered || line.deliveredQuantity === line.qty
          };

          return newLine;
        })
        .map((line, index, lines) => {
          // Update or remove related lines
          if (!ticket.hasServices || !line.relatedLines) {
            return line;
          }

          const newLine = { ...line };
          newLine.relatedLines = line.relatedLines.map(relatedLine => ({
            ...relatedLine,
            orderId: newTicket.id,
            orderlineId: lines.find(
              l => l.replacedorderline === relatedLine.orderlineId
            )
          }));
          return newLine;
        });

      // if (deferredLines.length) {
      //   linesWithDeferred.push(
      //     OB.I18N.getLabel('OBPOS_NotModifiableDefLinesBody')
      //   );
      // }
      // //Set to not editable and not deletable to all deferred lines or lines that have deferred services
      // _.each(deferredLines, function(deferredLine) {
      //   var deffLine = _.find(me.get('lines').models, function(line) {
      //     return (
      //       deferredLine === OB.DEC.mul(OB.DEC.add(line.get('linepos'), 1), 10)
      //     );
      //   });
      //   deffLine.set('isEditable', false);
      //   deffLine.set('isDeletable', false);
      //   linesWithDeferred.push(
      //     OB.I18N.getLabel('OBMOBC_Character')[1] +
      //       ' ' +
      //       deffLine.get('product').get('_identifier') +
      //       ' (' +
      //       OB.I18N.getLabel('OBPOS_LineQuantity') +
      //       ': ' +
      //       deffLine.get('qty') +
      //       ')'
      //   );
      // });
      // if (deferredLines.length) {
      //   linesWithDeferred.push(
      //     OB.I18N.getLabel('OBPOS_NotModifiableDefLinesBodyFooter')
      //   );
      //   linesWithDeferred.push(
      //     OB.I18N.getLabel('OBPOS_NotModifiableDefLinesBodyFooter2')
      //   );
      //   OB.UTIL.showConfirmation.display(
      //     OB.I18N.getLabel('OBPOS_NotModifiableLines'),
      //     linesWithDeferred
      //   );
      // }

      // // Set the last line as selected to call the 'onRearrangeEditButtonBar' event and update the isEditable and
      // // isDeletable status for the lines (to hide or show the buttons)
      // if (deferredLines.length) {
      //   me.get('lines')
      //     .at(me.get('lines').models.length - 1)
      //     .trigger(
      //       'selected',
      //       me.get('lines').at(me.get('lines').models.length - 1)
      //     );
      // }

      return newTicket;
    }
  );
})();
