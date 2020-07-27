/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket global action that completes a ticket and moves it to a message in the state
 */

/* eslint-disable no-use-before-define */

(() => {
  OB.App.StateAPI.Global.registerAction(
    'completeMultiTicket',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };
      const newTicket = { ...newGlobalState.Ticket };
      let newDocumentSequence = { ...newGlobalState.DocumentSequence };
      let newCashup = { ...newGlobalState.Cashup };
      let newMessages = [...newGlobalState.Messages];
      const newTicketList = [...newGlobalState.TicketList];

      payload.ticketIdToClose.forEach(ticketCloseId => {
        let ticket;
        if (ticketCloseId === newTicket.id) {
          ticket = newTicket;
        } else {
          ticket = newTicketList.filter(
            ticketOfFilter => ticketOfFilter.id === ticketCloseId
          );
        }

        const ticketPayload = payload[ticket.id];

        // Set complete ticket properties
        ticket.completeTicket = true;
        ticket = OB.App.State.Ticket.Utils.completeTicket(
          ticket,
          ticketPayload
        );

        // FIXME: Move to calculateTotals?
        ticket = OB.App.State.Ticket.Utils.updateTicketType(
          ticket,
          ticketPayload
        );

        // Complete ticket payment
        ticket = OB.App.State.Ticket.Utils.completePayment(
          ticket,
          ticketPayload
        );

        // Document number generation
        ({
          ticket,
          documentSequence: newDocumentSequence
        } = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
          ticket,
          newDocumentSequence,
          ticketPayload
        ));

        // Delivery generation
        ticket = OB.App.State.Ticket.Utils.generateDelivery(
          ticket,
          ticketPayload
        );

        // Invoice generation
        ticket = OB.App.State.Ticket.Utils.generateInvoice(
          ticket,
          ticketPayload
        );
        if (ticket.calculatedInvoice) {
          ({
            ticket: ticket.calculatedInvoice,
            documentSequence: newDocumentSequence
          } = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
            ticket.calculatedInvoice,
            newDocumentSequence,
            ticketPayload
          ));
        }

        // Cashup update
        ({
          ticket,
          cashup: newCashup
        } = OB.App.State.Cashup.Utils.updateCashupFromTicket(
          ticket,
          newCashup,
          ticketPayload
        ));

        // Ticket synchronization message
        newMessages = [
          ...newMessages,
          OB.App.State.Messages.Utils.createNewMessage(
            'Order',
            'org.openbravo.retail.posterminal.OrderLoader',
            ticket
          )
        ];

        // Ticket print message
        newMessages = [
          ...newMessages,
          OB.App.State.Messages.Utils.createPrintTicketMessage(ticket)
        ];
        if (ticket.calculatedInvoice) {
          newMessages = [
            ...newMessages,
            OB.App.State.Messages.Utils.createPrintTicketMessage(
              ticket.calculatedInvoice
            )
          ];
        }
      });

      newGlobalState.Ticket = newTicket;
      newGlobalState.DocumentSequence = newDocumentSequence;
      newGlobalState.Cashup = newCashup;
      newGlobalState.Messages = newMessages;
      newGlobalState.TicketList = newTicketList;

      return newGlobalState;
    }
  );

  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = { ...payload };

      payload.ticketIdToClose.forEach(async ticketCloseId => {
        let ticket;
        if (ticketCloseId === globalState.Ticket.id) {
          ticket = globalState.Ticket;
        } else {
          ticket = globalState.TicketList.filter(
            ticketOfFilter => ticketOfFilter.id === ticketCloseId
          );
        }

        let ticketPayload = newPayload;

        ticketPayload = await checkAnonymousReturn(ticket, ticketPayload);
        ticketPayload = await checkAnonymousLayaway(ticket, ticketPayload);
        ticketPayload = await checkNegativePayments(ticket, ticketPayload);
        ticketPayload = await checkExtraPayments(ticket, ticketPayload);
        ticketPayload = await checkPrePayments(ticket, ticketPayload);
        ticketPayload = await checkOverPayments(ticket, ticketPayload);
        ticketPayload = await checkTicketUpdated(ticket, ticketPayload);

        newPayload[ticket.id] = ticketPayload;
      });

      return newPayload;
    },
    async (globalState, payload) => payload,
    100
  );

  const checkAnonymousReturn = async (ticket, payload) => {
    if (
      !payload.terminal.returnsAnonymousCustomer &&
      ticket.businessPartner.id === payload.terminal.businessPartner &&
      ticket.lines.some(line => line.qty < 0 && !line.originalDocumentNo)
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_returnServicesWithAnonimousCust'
      });
    }

    return payload;
  };

  const checkAnonymousLayaway = async (ticket, payload) => {
    const newPayload = { ...payload };
    if (
      ticket.orderType === 2 &&
      ticket.businessPartner.id === newPayload.businessPartner &&
      !newPayload.layawayAnonymousCustomer
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_layawaysOrdersWithAnonimousCust'
      });
    }

    return newPayload;
  };

  const checkNegativePayments = async (ticket, payload) => {
    if (
      ticket.payments
        .filter(payment => payment.isReturnOrder !== undefined)
        .some(payment => payment.isReturnOrder !== ticket.isNegative)
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: ticket.isNegative
          ? 'OBPOS_PaymentOnReturnReceipt'
          : 'OBPOS_NegativePaymentOnReceipt'
      });
    }

    return payload;
  };

  const checkExtraPayments = async (ticket, payload) => {
    ticket.payments.reduce((total, payment) => {
      if (total >= OB.DEC.abs(ticket.grossAmount) && !payment.paymentRounding) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_UnnecessaryPaymentAdded'
        });
      }

      if (
        payment.isReversePayment ||
        payment.isReversed ||
        payment.isPrePayment
      ) {
        return total;
      }

      return OB.DEC.add(total, payment.origAmount);
    }, OB.DEC.Zero);

    return payload;
  };

  const checkPrePayments = async (ticket, payload) => {
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
      ticket,
      payload
    );

    if (
      !payload.terminal.calculatePrepayments ||
      ticket.orderType === 1 ||
      ticket.orderType === 3 ||
      ticket.obposPrepaymentlimitamt === OB.DEC.Zero ||
      paymentStatus.totalAmt <= OB.DEC.Zero ||
      OB.DEC.sub(
        OB.DEC.add(ticket.obposPrepaymentlimitamt, paymentStatus.pendingAmt),
        paymentStatus.totalAmt
      ) <= OB.DEC.Zero
    ) {
      return payload;
    }

    if (!OB.App.Security.hasPermission('OBPOS_AllowPrepaymentUnderLimit')) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_PrepaymentUnderLimit_NotAllowed',
        messageParams: [ticket.obposPrepaymentlimitamt]
      });
    }

    if (
      ticket.approvals.some(
        approval =>
          approval.approvalType === 'OBPOS_approval.prepaymentUnderLimit'
      )
    ) {
      return payload;
    }

    const newPayload = await OB.App.Security.requestApprovalForAction(
      'OBPOS_approval.prepaymentUnderLimit',
      payload
    );
    return newPayload;
  };

  const checkOverPayments = async (ticket, payload) => {
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
      ticket,
      payload
    );

    if (paymentStatus.overpayment) {
      const confirmation = await OB.App.View.DialogUIHandler.askConfirmation({
        title: 'OBPOS_OverpaymentWarningTitle',
        message: 'OBPOS_OverpaymentWarningBody',
        messageParams: [
          OB.I18N.formatCurrencyWithSymbol(
            paymentStatus.overpayment,
            payload.terminal.symbol,
            payload.terminal.currencySymbolAtTheRight
          )
        ]
      });
      if (!confirmation) {
        throw new OB.App.Class.ActionCanceled();
      }
    } else if (
      ticket.payment !== OB.DEC.abs(ticket.grossAmount) &&
      !OB.App.State.Ticket.Utils.isLayaway(ticket) &&
      !ticket.payOnCredit &&
      OB.DEC.abs(ticket.obposPrepaymentamt) ===
        OB.DEC.abs(ticket.grossAmount) &&
      !payload.terminal.calculatePrepayments
    ) {
      const confirmation = await OB.App.View.DialogUIHandler.askConfirmation({
        title: 'OBPOS_PaymentAmountDistinctThanReceiptAmountTitle',
        message: 'OBPOS_PaymentAmountDistinctThanReceiptAmountBody'
      });
      if (!confirmation) {
        throw new OB.App.Class.ActionCanceled();
      }
    }

    return payload;
  };

  const checkTicketUpdated = async (ticket, payload) => {
    if (!ticket.isPaid && !ticket.isLayaway) {
      return payload;
    }

    const showTicketUpdatedError = async errorType => {
      if (
        errorType ||
        !payload.preferences.allowToSynchronizeLoadedReceiptsOffline
      ) {
        const getErrorConfirmation = () => {
          switch (errorType) {
            case 'P':
              return 'OBPOS_SyncPending';
            case 'E':
              return 'OBPOS_SyncWithErrors';
            case 'O':
              return 'OBPOS_RemoveAndLoad';
            default:
              return 'OBPOS_NotPossibleToConfirmReceipt';
          }
        };
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: getErrorConfirmation(),
          messageParams: [ticket.documentNo]
        });
      }

      const confirmation = await OB.App.View.DialogUIHandler.askConfirmation({
        title: 'OBPOS_UpdatedReceipt',
        message: 'OBPOS_NotPossibleToConfirmReceiptWarn',
        messageParams: [ticket.documentNo]
      });
      if (!confirmation) {
        throw new OB.App.Class.ActionCanceled();
      }

      return payload;
    };

    if (!payload.terminal.connectedToERP || !navigator.onLine) {
      return showTicketUpdatedError();
    }

    try {
      const data = await OB.App.Request.mobileServiceRequest(
        'org.openbravo.retail.posterminal.process.CheckUpdated',
        {
          order: {
            id: ticket.id,
            loaded: ticket.loaded,
            lines: ticket.lines.map(line => {
              return {
                id: line.id,
                loaded: line.loaded
              };
            })
          }
        }
      );
      if (data.response.data.type) {
        return showTicketUpdatedError(data.response.data.type);
      }
    } catch (error) {
      return showTicketUpdatedError();
    }

    return payload;
  };
})();
