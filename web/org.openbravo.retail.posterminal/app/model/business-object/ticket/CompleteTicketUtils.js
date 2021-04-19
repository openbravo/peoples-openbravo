/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Complete Ticket action
 */

OB.App.StateAPI.Ticket.registerUtilityFunctions({
  /**
   * Set needed properties when completing a ticket.
   *
   * @param {object} ticket - The ticket being processed
   * @param {object} payload - The calculation payload, which include:
   *             * terminal.id - Terminal id
   *             * approvals - Approvals to add to the ticket
   *
   * @returns {object} The new state of Ticket after being processed.
   */
  processTicket(ticket, payload) {
    const newTicket = { ...ticket };
    const currentDate = new Date();
    const creationDate = newTicket.creationDate
      ? new Date(newTicket.creationDate)
      : currentDate;

    newTicket.hasbeenpaid = 'Y';
    newTicket.orderDate = currentDate.toISOString();
    newTicket.movementDate = currentDate.toISOString();
    newTicket.accountingDate = currentDate.toISOString();
    newTicket.creationDate = creationDate.toISOString();
    newTicket.obposCreatedabsolute = creationDate.toISOString();
    newTicket.created = creationDate.getTime();
    newTicket.timezoneOffset = creationDate.getTimezoneOffset();
    newTicket.posTerminal = payload.terminal.id;
    newTicket.undo = null;
    newTicket.multipleUndo = null;
    newTicket.paymentMethodKind =
      newTicket.payments.length === 1 &&
      OB.App.State.Ticket.Utils.isFullyPaid(newTicket)
        ? newTicket.payments[0].kind
        : null;
    newTicket.approvals = [
      ...newTicket.approvals,
      ...(payload.approvals || [])
    ];

    // FIXME: Remove once every use of OB.UTIL.Approval.requestApproval() send approvalType as string
    newTicket.approvals = newTicket.approvals.map(approval => {
      const newApproval = { ...approval };
      if (typeof approval.approvalType === 'object') {
        newApproval.approvalType = approval.approvalType.approval;
      }
      return newApproval;
    });

    return newTicket;
  },

  /**
   * Remove some unneeded properties from ticket to make it smaller.
   *
   * @param {object} ticket - The ticket being cleaned
   *
   * @returns {object} The new state of Ticket after being cleaned.
   */
  cleanTicket(ticket) {
    const newTicket = { ...ticket };
    const productPropertiesToRemove = [
      'uOM',
      'uOMsymbol',
      'uOMstandardPrecision',
      'productCategory',
      'taxCategory',
      'img',
      'imgId',
      'description',
      'obposScale',
      'groupProduct',
      'stocked',
      'showstock',
      'isGeneric',
      'generic_product_id',
      'characteristicDescription',
      'showchdesc',
      'bestseller',
      'ispack',
      'listPrice',
      'standardPrice',
      'priceLimit',
      'cost',
      'algorithm',
      'currentStandardPrice',
      'includeProductCategories',
      'includeProducts',
      'printDescription',
      'oBPOSAllowAnonymousSale',
      'returnable',
      'overdueReturnDays',
      'isPriceRuleBased',
      'proposalType',
      'availableForMultiline',
      'isLinkedToProduct',
      'modifyTax',
      'allowDeferredSell',
      'deferredSellMaxDays',
      'quantityRule',
      'isPrintServices',
      'obposEditablePrice',
      'isSerialNo',
      'productStatus',
      'productAssortmentStatus',
      'crossStore',
      'obrdmDeliveryMode',
      'obrdmDeliveryModeLyw',
      'obrdmIsdeliveryservice'
    ];
    const removeProductProperties = line => {
      if (line.product.saveToReceipt) {
        return line;
      }
      const newProduct = { ...line.product };
      productPropertiesToRemove.forEach(
        property => delete newProduct[property]
      );
      return {
        ...line,
        product: newProduct
      };
    };

    newTicket.lines = newTicket.lines.map(removeProductProperties);
    if (newTicket.deletedLines) {
      newTicket.deletedLines = newTicket.deletedLines.map(
        removeProductProperties
      );
    }

    return newTicket;
  },

  /**
   * Generates the corresponding delivery for the given ticket.
   *
   * @param {object} ticket - The ticket whose delivery will be generated
   * @param {object} payload - The calculation payload, which include:
   *             * terminal.organization - The terminal organization.
   *
   * @returns {object} The ticket with the result of the delivery generation
   */
  generateDelivery(ticket, payload) {
    const newTicket = { ...ticket };
    const isFullyPaidOrPaidOnCredit =
      OB.App.State.Ticket.Utils.isFullyPaid(ticket) || ticket.payOnCredit;
    const isCrossStore = OB.App.State.Ticket.Utils.isCrossStore(
      ticket,
      payload
    );

    newTicket.lines = ticket.lines.map(line => {
      const newLine = { ...line };

      if (isFullyPaidOrPaidOnCredit || newLine.obposCanbedelivered) {
        newLine.obposCanbedelivered = true;
        newLine.obposIspaid = true;
      }

      if (
        (!ticket.completeTicket && !ticket.payOnCredit) ||
        (isCrossStore && !newLine.originalOrderLineId)
      ) {
        newLine.obposQtytodeliver = newLine.deliveredQuantity || OB.DEC.Zero;
        return newLine;
      }

      if (
        newLine.product.productType === 'S' &&
        newLine.product.isLinkedToProduct
      ) {
        if (newLine.qty > 0) {
          const qtyToDeliver = newLine.relatedLines.reduce(
            (total, relatedLine) => {
              const orderLine = ticket.lines.find(
                l => l.id === relatedLine.orderlineId
              );
              if (
                orderLine &&
                (!orderLine.obrdmDeliveryMode ||
                  orderLine.obrdmDeliveryMode === 'PickAndCarry') &&
                (isFullyPaidOrPaidOnCredit || orderLine.obposCanbedelivered)
              ) {
                return OB.DEC.add(total, orderLine.qty);
              }
              if (relatedLine.obposIspaid) {
                return OB.DEC.add(total, relatedLine.deliveredQuantity);
              }
              return total;
            },
            OB.DEC.Zero
          );

          newLine.obposQtytodeliver =
            qtyToDeliver && newLine.product.quantityRule === 'UQ'
              ? OB.DEC.One
              : qtyToDeliver;
          if (qtyToDeliver) {
            newLine.obposCanbedelivered = true;
          }
        } else if (newLine.qty < 0) {
          newLine.obposQtytodeliver = newLine.qty;
          newLine.obposCanbedelivered = true;
        }
      } else if (
        newLine.obposCanbedelivered &&
        (!newLine.obrdmDeliveryMode ||
          newLine.obrdmDeliveryMode === 'PickAndCarry')
      ) {
        newLine.obposQtytodeliver = newLine.qty;
      } else {
        newLine.obposQtytodeliver = newLine.deliveredQuantity || OB.DEC.Zero;
      }

      return newLine;
    });

    newTicket.generateShipment = newTicket.lines.some(line => {
      return line.obposQtytodeliver !== (line.deliveredQuantity || OB.DEC.Zero);
    });
    newTicket.deliver = !newTicket.lines.some(line => {
      return line.obposQtytodeliver !== line.qty;
    });

    return newTicket;
  },

  /**
   * Generates the corresponding invoice for the given ticket.
   *
   * @param {object} ticket - The ticket whose invoice will be generated
   * @param {object} payload - The calculation payload, which include:
   *             * discountRules - The discount rules to be considered for discount calculation
   *             * bpSets - The businessPartner sets for discount calculation
   *             * taxRules - The tax rules to be considered for tax calculation
   *
   * @returns {object} The new state of Ticket and DocumentSequence after invoice generation.
   */
  generateInvoice(ticket, payload) {
    const generateInvoice =
      !ticket.obposIsDeleted &&
      (ticket.payOnCredit ||
        (ticket.invoiceTerms === 'I' && ticket.generateInvoice) ||
        (ticket.invoiceTerms === 'O' && ticket.deliver) ||
        (ticket.invoiceTerms === 'D' &&
          (ticket.generateShipment ||
            ticket.lines.find(
              line => line.deliveredQuantity !== line.invoicedQuantity
            ))));

    if (!generateInvoice) {
      return ticket;
    }

    const invoiceLines = ticket.lines.flatMap(line => {
      const originalQty = line.qty;
      const qtyAlreadyInvoiced = line.invoicedQuantity || OB.DEC.Zero;
      const qtyPendingToBeInvoiced = OB.DEC.sub(line.qty, qtyAlreadyInvoiced);

      let qtyToInvoice;
      if (ticket.invoiceTerms === 'D') {
        qtyToInvoice = OB.DEC.sub(line.obposQtytodeliver, qtyAlreadyInvoiced);
      } else if (ticket.invoiceTerms === 'I' || ticket.invoiceTerms === 'O') {
        qtyToInvoice = qtyPendingToBeInvoiced;
      } else {
        qtyToInvoice = OB.DEC.Zero;
      }

      if (
        !qtyToInvoice ||
        (ticket.invoiceTerms !== 'I' &&
          !line.obposCanbedelivered &&
          !line.obposIspaid)
      ) {
        return [];
      }

      const invoiceLine = { ...line };
      invoiceLine.id = OB.App.UUID.generate();
      invoiceLine.qty = qtyToInvoice;
      invoiceLine.orderLineId = line.id;
      invoiceLine.product = { ...invoiceLine.product };
      invoiceLine.product.ignorePromotions = true;
      invoiceLine.product.img = undefined;

      if (OB.DEC.abs(qtyAlreadyInvoiced) > 0) {
        invoiceLine.promotions = line.promotions.map(promotion => {
          const invoiceLinePromotion = { ...promotion };
          if (OB.DEC.abs(qtyToInvoice) < OB.DEC.abs(qtyPendingToBeInvoiced)) {
            invoiceLinePromotion.amt = OB.DEC.mul(
              invoiceLinePromotion.amt,
              OB.DEC.div(qtyToInvoice, originalQty)
            );
            invoiceLinePromotion.obdiscQtyoffer = qtyToInvoice;
            if (invoiceLinePromotion.actualAmt) {
              invoiceLinePromotion.actualAmt = OB.DEC.mul(
                invoiceLinePromotion.actualAmt,
                OB.DEC.div(qtyToInvoice, originalQty)
              );
            }
            if (invoiceLinePromotion.displayedTotalAmount) {
              invoiceLinePromotion.displayedTotalAmount = OB.DEC.mul(
                invoiceLinePromotion.displayedTotalAmount,
                OB.DEC.div(qtyToInvoice, originalQty)
              );
            }
            if (invoiceLinePromotion.fullAmt) {
              invoiceLinePromotion.fullAmt = OB.DEC.mul(
                invoiceLinePromotion.fullAmt,
                OB.DEC.div(qtyToInvoice, originalQty)
              );
            }
            if (invoiceLinePromotion.qtyOffer) {
              invoiceLinePromotion.qtyOffer = qtyToInvoice;
            }
            if (invoiceLinePromotion.pendingQtyOffer) {
              invoiceLinePromotion.pendingQtyOffer = qtyToInvoice;
            }
          } else {
            invoiceLinePromotion.amt = OB.DEC.sub(
              invoiceLinePromotion.amt,
              OB.DEC.mul(
                invoiceLinePromotion.amt,
                OB.DEC.div(qtyAlreadyInvoiced, originalQty)
              )
            );
            invoiceLinePromotion.obdiscQtyoffer = qtyToInvoice;
            if (invoiceLinePromotion.actualAmt) {
              invoiceLinePromotion.actualAmt = OB.DEC.sub(
                invoiceLinePromotion.actualAmt,
                OB.DEC.mul(
                  invoiceLinePromotion.actualAmt,
                  OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                )
              );
            }
            if (invoiceLinePromotion.displayedTotalAmount) {
              invoiceLinePromotion.displayedTotalAmount = OB.DEC.sub(
                invoiceLinePromotion.displayedTotalAmount,
                OB.DEC.mul(
                  invoiceLinePromotion.displayedTotalAmount,
                  OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                )
              );
            }
            if (invoiceLinePromotion.fullAmt) {
              invoiceLinePromotion.fullAmt = OB.DEC.sub(
                invoiceLinePromotion.fullAmt,
                OB.DEC.mul(
                  invoiceLinePromotion.fullAmt,
                  OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                )
              );
            }
            if (invoiceLinePromotion.qtyOffer) {
              invoiceLinePromotion.qtyOffer = qtyToInvoice;
            }
            if (invoiceLinePromotion.pendingQtyOffer) {
              invoiceLinePromotion.pendingQtyOffer = qtyToInvoice;
            }
          }

          return invoiceLinePromotion;
        });
      }

      return invoiceLine;
    });

    if (!invoiceLines.length) {
      return ticket;
    }

    const newTicket = { ...ticket };
    const invoice = { ...ticket };
    invoice.orderId = ticket.id;
    invoice.id = OB.App.UUID.generate();
    invoice.isInvoice = true;
    invoice.documentNo = undefined;
    invoice.obposSequencename = undefined;
    invoice.obposSequencenumber = undefined;
    invoice.orderDocumentNo = ticket.documentNo;
    invoice.lines = invoiceLines;

    if (invoice.lines.length === ticket.lines.length) {
      newTicket.calculatedInvoice = invoice;
    } else {
      newTicket.calculatedInvoice = OB.App.State.Ticket.Utils.calculateTotals(
        {
          ...invoice,
          lines: invoice.lines.map(line => {
            return { ...line, skipApplyPromotions: true };
          })
        },
        payload
      );
    }

    return newTicket;
  },

  /**
   * Completes ticket payment generating change payment if needed and converting payment to negative in case of return.
   *
   * @param {object} ticket - The ticket whose payment will be completed
   * @param {object} payload - The calculation payload, which include:
   *             * payments - Terminal payment types
   *             * terminal.multiChange - Terminal multichange configuration
   *             * preferences.splitChange - OBPOS_SplitChange preference value
   *
   * @returns {object} The new state of Ticket after payment completing.
   */
  completePayment(ticket, payload) {
    let newTicket = { ...ticket };

    // Manage change payments if there is change
    if (newTicket.changePayments && newTicket.changePayments.length > 0) {
      const mergeable =
        !payload.terminal.multiChange && !payload.preferences.splitChange;

      newTicket.changePayments.forEach(changePayment => {
        const terminalPayment = payload.payments.find(
          paymentType => paymentType.payment.searchKey === changePayment.key
        );

        // Generate change payment
        newTicket = OB.App.State.Ticket.Utils.addPayment(newTicket, {
          ...payload,
          payment: {
            kind: terminalPayment.payment.searchKey,
            name: terminalPayment.payment.commercialName,
            amount: OB.DEC.sub(
              OB.DEC.Zero,
              changePayment.amount,
              terminalPayment.obposPosprecision || OB.DEC.getScale()
            ),
            amountRounded: OB.DEC.sub(
              OB.DEC.Zero,
              changePayment.amountRounded || OB.DEC.Zero,
              terminalPayment.obposPosprecision || OB.DEC.getScale()
            ),
            origAmount: OB.DEC.sub(OB.DEC.Zero, changePayment.origAmount),
            origAmountRounded: OB.DEC.sub(
              OB.DEC.Zero,
              OB.DEC.mul(
                changePayment.amountRounded || OB.DEC.Zero,
                terminalPayment.rate || OB.DEC.One
              )
            ),
            rate: terminalPayment.rate,
            mulrate: terminalPayment.mulrate,
            isocode: terminalPayment.isocode,
            allowOpenDrawer: terminalPayment.paymentMethod.allowopendrawer,
            isCash: terminalPayment.paymentMethod.iscash,
            openDrawer: terminalPayment.paymentMethod.openDrawer,
            printtwice: terminalPayment.paymentMethod.printtwice,
            changePayment: true,
            paymentData: {
              changePayment: true,
              mergeable,
              label: changePayment.label
            }
          }
        });
      });
    }

    // Convert return payments in negative
    if (newTicket.isNegative) {
      newTicket.payments = newTicket.payments.map(payment => {
        const newPayment = { ...payment };

        if (
          payment.isPrePayment ||
          payment.reversedPaymentId ||
          newTicket.isPaid
        ) {
          newPayment.paid = payment.amount;
          return newPayment;
        }

        newPayment.amount = -payment.amount;
        newPayment.origAmount = -payment.origAmount;
        newPayment.paid = -payment.paid;
        newPayment.amountRounded = payment.amountRounded
          ? -payment.amountRounded
          : payment.amountRounded;
        newPayment.origAmountRounded = payment.origAmountRounded
          ? -payment.origAmountRounded
          : payment.origAmountRounded;
        return newPayment;
      });
    }

    if (
      newTicket.payments.find(p => {
        return !p.isPrePayment;
      })
    ) {
      newTicket.isPaymentModified = true;
    }
    return newTicket;
  },

  /**
   * Checks if given ticket is a return with anonymous customer.
   */
  async checkAnonymousReturn(ticket, payload) {
    if (
      !OB.App.TerminalProperty.get('terminal').returns_anonymouscustomer &&
      ticket.businessPartner.id ===
        OB.App.TerminalProperty.get('terminal').businessPartner &&
      ticket.lines.some(line => line.qty < 0 && !line.originalDocumentNo)
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_returnServicesWithAnonimousCust'
      });
    }

    return payload;
  },

  /**
   * Checks if given ticket is a layaway with anonymous customer.
   */
  async checkAnonymousLayaway(ticket, payload) {
    if (
      ticket.orderType === 2 &&
      !OB.App.TerminalProperty.get('terminal').layaway_anonymouscustomer &&
      ticket.businessPartner.id ===
        OB.App.TerminalProperty.get('terminal').businessPartner
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_layawaysOrdersWithAnonimousCust'
      });
    }

    return payload;
  },

  /**
   * Checks if no processed payments exists for given ticket.
   */
  async checkUnprocessedPayments(ticket, payload) {
    const terminal = OB.App.TerminalProperty.get('terminal');
    const payment = ticket.payments.find(p => !p.isPrePayment);
    if (payment) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_C&RDeletePaymentsBody',
        messageParams: [
          `${payment.name} (${OB.App.Locale.formatAmount(payment.amount, {
            currencySymbol: terminal.symbol,
            currencySymbolAtTheRight: terminal.currencySymbolAtTheRight
          })})`
        ]
      });
    }

    return payload;
  },

  /**
   * Checks if given ticket includes negative payments.
   */
  async checkNegativePayments(ticket, payload) {
    const isReturnTicket = payload.multiTicketList ? false : ticket.isNegative;
    if (
      ticket.payments
        .filter(payment => payment.isReturnOrder !== undefined)
        .some(payment => payment.isReturnOrder !== isReturnTicket)
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: isReturnTicket
          ? 'OBPOS_PaymentOnReturnReceipt'
          : 'OBPOS_NegativePaymentOnReceipt'
      });
    }

    return payload;
  },

  /**
   * Checks if unnecessary payments exists for given ticket.
   */
  async checkExtraPayments(ticket, payload) {
    const totalToPaid = OB.DEC.abs(
      payload.multiTicketList ? ticket.total : ticket.grossAmount
    );
    ticket.payments.reduce((total, payment) => {
      if (total >= totalToPaid && !payment.paymentRounding) {
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
  },

  /**
   * Checks if prepayments exists for given ticket.
   */
  async checkPrePayments(ticket, payload) {
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
      ticket,
      payload
    );

    const ticketHasPrepayment =
      (payload.multiTicketList ||
        (ticket.orderType !== 1 && ticket.orderType !== 3)) &&
      ticket.obposPrepaymentlimitamt !== OB.DEC.Zero;
    const pendingPrepayment = OB.DEC.sub(
      OB.DEC.add(ticket.obposPrepaymentlimitamt, paymentStatus.pendingAmt),
      OB.DEC.add(paymentStatus.totalAmt, ticket.existingPayment || OB.DEC.Zero)
    );

    if (
      !OB.App.TerminalProperty.get('terminal').terminalType
        .calculateprepayments ||
      !ticketHasPrepayment ||
      ticket.obposPrepaymentlimitamt === OB.DEC.Zero ||
      paymentStatus.totalAmt <= OB.DEC.Zero ||
      pendingPrepayment <= OB.DEC.Zero
    ) {
      return payload;
    }

    if (!OB.App.Security.hasPermission('OBPOS_AllowPrepaymentUnderLimit')) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_PrepaymentUnderLimit_NotAllowed',
        messageParams: [ticket.obposPrepaymentlimitamt]
      });
    }

    const approvals = payload.multiTicketList
      ? ticket.multiOrdersList
          .map(ticketMap => ticketMap.approvals)
          .reduce((a, b) => a.concat(b))
      : ticket.approvals;
    if (
      approvals.some(
        approval =>
          approval.approvalType === 'OBPOS_approval.prepaymentUnderLimit'
      )
    ) {
      return payload;
    }

    const newPayload = { ...payload };
    newPayload.checkApproval = { prepaymentUnderLimit: true };

    return newPayload;
  },

  /**
   * Checks if overpayment exists for given ticket.
   */
  async checkOverPayments(ticket, payload) {
    const terminal = OB.App.TerminalProperty.get('terminal');
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
      ticket,
      payload
    );

    if (paymentStatus.overpayment) {
      await OB.App.View.DialogUIHandler.askConfirmationWithCancel({
        title: 'OBPOS_OverpaymentWarningTitle',
        message: 'OBPOS_OverpaymentWarningBody',
        messageParams: [
          OB.App.Locale.formatAmount(paymentStatus.overpayment, {
            currencySymbol: terminal.symbol,
            currencySymbolAtTheRight: terminal.currencySymbolAtTheRight
          })
        ]
      });
    } else if (
      !payload.multiTicketList &&
      ticket.payment !== OB.DEC.abs(ticket.grossAmount) &&
      !OB.App.State.Ticket.Utils.isLayaway(ticket) &&
      !ticket.payOnCredit &&
      OB.DEC.abs(ticket.obposPrepaymentamt) ===
        OB.DEC.abs(ticket.grossAmount) &&
      !terminal.terminalType.calculateprepayments
    ) {
      await OB.App.View.DialogUIHandler.askConfirmationWithCancel({
        title: 'OBPOS_PaymentAmountDistinctThanReceiptAmountTitle',
        message: 'OBPOS_PaymentAmountDistinctThanReceiptAmountBody'
      });
    }

    return payload;
  },

  /**
   * Checks if given ticket was updated in a different terminal or it is pending to be synchronized.
   */
  async checkTicketUpdated(ticket, payload) {
    const runCheckTicketUpdatedRequest = async () => {
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
        return data;
      } catch (error) {
        if (
          !OB.App.Security.hasPermission(
            'OBPOS_AllowToSynchronizeLoadedReceiptsOffline'
          )
        ) {
          throw new OB.App.Class.ActionCanceled({
            errorConfirmation: 'OBPOS_NotPossibleToConfirmReceipt',
            messageParams: [ticket.documentNo]
          });
        }
        return false;
      }
    };

    const showTicketUpdatedError = async errorType => {
      if (errorType) {
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
      await OB.App.View.DialogUIHandler.askConfirmationWithCancel({
        title: 'OBPOS_UpdatedReceipt',
        message: 'OBPOS_NotPossibleToConfirmReceiptWarn',
        messageParams: [ticket.documentNo]
      });
      return payload;
    };

    if (!ticket.isPaid && !ticket.isLayaway) {
      return payload;
    }

    const data = await runCheckTicketUpdatedRequest();
    if (!data || data.response.data.type) {
      return showTicketUpdatedError(data ? data.response.data.type : undefined);
    }

    return payload;
  },

  /**
   * Checks if given ticket was canceled in a different terminal.
   */
  async checkTicketCanceled(ticket, payload) {
    try {
      const result = await OB.App.Request.mobileServiceRequest(
        'org.openbravo.retail.posterminal.process.IsOrderCancelled',
        {
          orderId: ticket.id,
          documentNo: ticket.documentNo,
          orderLoaded: ticket.loaded,
          orderLines: ticket.lines.map(line => ({
            id: line.id,
            loaded: line.loaded
          })),
          checkNotEditableLines: payload.checkNotEditableLines,
          checkNotDeliveredDeferredServices:
            payload.checkNotDeliveredDeferredServices
        }
      );

      if (result.response.error) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: result.response.error.message
        });
      }
      if (result.response.data.orderCancelled) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_LayawayCancelledError'
        });
      }
      if (
        result.response.data.notDeliveredDeferredServices &&
        result.response.data.notDeliveredDeferredServices.length
      ) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_CannotCancelLayWithDeferredOrders',
          messageParams: [
            result.response.data.notDeliveredDeferredServices.join(', ')
          ]
        });
      }
    } catch (error) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBMOBC_OfflineWindowRequiresOnline'
      });
    }

    return payload;
  },

  /**
   * Checks if given ticket business partner has available credit.
   */
  async checkBusinessPartnerCredit(ticket, payload) {
    const paymentStatus = OB.App.State.Ticket.Utils.getPaymentStatus(
      ticket,
      payload
    );

    const showCompleteCreditTicketConfirmation = async message => {
      await OB.App.View.DialogUIHandler.askConfirmationWithCancel({
        title: 'OBPOS_SellingOnCreditHeader',
        message
      });
      return payload;
    };

    const runCheckBusinessPartnerCreditRequest = async () => {
      try {
        const data = await OB.App.Request.mobileServiceRequest(
          'org.openbravo.retail.posterminal.CheckBusinessPartnerCredit',
          {
            businessPartnerId: ticket.businessPartner.id,
            totalPending: paymentStatus.pendingAmt
          }
        );
        return data;
      } catch (error) {
        if (
          !OB.App.Security.hasPermission('OBPOS_AllowSellOnCreditWhileOffline')
        ) {
          throw new OB.App.Class.ActionCanceled({
            errorConfirmation: 'OBPOS_UnabletoSellOncredit'
          });
        }
        return false;
      }
    };

    if (paymentStatus.isReturn) {
      return showCompleteCreditTicketConfirmation();
    }

    const data = await runCheckBusinessPartnerCreditRequest();
    if (data && !data.response.data) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_MsgErrorCreditSales'
      });
    }

    if (data && !data.response.data.enoughCredit) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_notEnoughCreditBody',
        messageParams: [
          data.response.data.bpName,
          data.response.data.actualCredit
        ]
      });
    }

    return showCompleteCreditTicketConfirmation(
      data ? undefined : 'OBPOS_Unabletocheckcredit'
    );
  }
});
