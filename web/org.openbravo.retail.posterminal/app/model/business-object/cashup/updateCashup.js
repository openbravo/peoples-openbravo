/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Defines the updateCashup action
 */
(function updateCashupDefinition() {
  /**
   * Update the cashup , after a ticket done
   * TODO: this action will disapear when ticket is included in the state
   */
  OB.App.StateAPI.Cashup.registerActions({
    updateCashup(cashup, payload) {
      const newCashup = { ...cashup };

      const { tickets, countLayawayAsSales } = payload;

      // TODO: update statistics

      tickets.forEach(order => {
        const orderType = order.get('orderType');

        let gross;
        let taxOrderType;
        let taxAmount;
        let amount;
        let precision;
        let netSales = OB.DEC.Zero;
        let grossSales = OB.DEC.Zero;
        let netReturns = OB.DEC.Zero;
        let grossReturns = OB.DEC.Zero;
        let taxSales = OB.DEC.Zero;
        let taxReturns = OB.DEC.Zero;
        let ctaxSales;
        const maxtaxSales = OB.DEC.Zero;
        let ctaxReturns;
        const maxtaxReturns = OB.DEC.Zero;
        if (
          !(order.has('isQuotation') && order.get('isQuotation')) &&
          !order.get('isPaid') &&
          ((countLayawayAsSales &&
            !(order.get('isLayaway') && !order.get('voidLayaway'))) ||
            (!countLayawayAsSales &&
              order.isFullyPaid() &&
              (order.get('isLayaway') && !order.get('voidLayaway'))))
        ) {
          order.get('lines').models.forEach(line => {
            if (order.get('priceIncludesTax')) {
              gross = line.get('lineGrossAmount');
            } else {
              gross = line.get('discountedGross');
            }
            if (order.get('doCancelAndReplace')) {
              if (!line.get('replacedorderline')) {
                netSales = OB.DEC.add(netSales, line.get('net'));
                grossSales = OB.DEC.add(grossSales, gross);
              }
            } else if (order.get('cancelLayaway')) {
              // Cancel Layaway
              netSales = OB.DEC.add(netSales, line.get('net'));
              grossSales = OB.DEC.add(grossSales, gross);
            } else if (order.get('voidLayaway')) {
              // Void Layaway
              netSales = OB.DEC.add(netSales, -line.get('net'));
              grossSales = OB.DEC.add(grossSales, -gross);
            } else if (line.get('qty') > 0) {
              // Sales order: Positive line
              netSales = OB.DEC.add(netSales, line.get('net'));
              grossSales = OB.DEC.add(grossSales, gross);
            } else if (line.get('qty') < 0) {
              // Return from customer or Sales with return: Negative line
              netReturns = OB.DEC.add(netReturns, -line.get('net'));
              grossReturns = OB.DEC.add(grossReturns, -gross);
            }
          });
          newCashup.netSales = OB.DEC.add(newCashup.netSales, netSales);
          newCashup.grossSales = OB.DEC.add(newCashup.grossSales, grossSales);
          newCashup.netReturns = OB.DEC.add(newCashup.netReturns, netReturns);
          newCashup.grossReturns = OB.DEC.add(
            newCashup.grossReturns,
            grossReturns
          );
          newCashup.totalRetailTransactions = OB.DEC.sub(
            newCashup.grossSales,
            newCashup.grossReturns
          );

          // group and sum the taxes
          const newCashupTaxes = [];
          order.get('lines').each(function perLine(line) {
            const taxLines = line.get('taxLines');
            if (
              orderType === 1 ||
              (line.get('qty') < 0 &&
                !order.get('cancelLayaway') &&
                !order.get('voidLayaway'))
            ) {
              taxOrderType = '1';
            } else {
              taxOrderType = '0';
            }

            Object.values(taxLines).forEach(taxLine => {
              if (!(order.has('isQuotation') && order.get('isQuotation'))) {
                if (
                  order.get('cancelLayaway') ||
                  (line.get('qty') > 0 && !order.get('isLayaway'))
                ) {
                  taxAmount = taxLine.amount;
                } else if (
                  order.get('voidLayaway') ||
                  (line.get('qty') < 0 && !order.get('isLayaway'))
                ) {
                  taxAmount = -taxLine.amount;
                }
              }

              if (!OB.UTIL.isNullOrUndefined(taxAmount)) {
                newCashupTaxes.push({
                  taxName: taxLine.name,
                  taxAmount,
                  taxOrderType
                });
              }
            });
          });

          // Calculate adjustment taxes
          newCashupTaxes.forEach(t => {
            if (t.taxOrderType === '0') {
              // sale
              taxSales = OB.DEC.add(taxSales, t.taxAmount);
              if (t.taxAmount > maxtaxSales) {
                ctaxSales = t;
              }
            } else {
              // return
              taxReturns = OB.DEC.add(taxReturns, t.taxAmount);
              if (t.taxAmount > maxtaxReturns) {
                ctaxReturns = t;
              }
            }
          });

          // Do the adjustment
          if (ctaxSales) {
            ctaxSales.taxAmount = OB.DEC.add(
              ctaxSales.taxAmount,
              OB.DEC.sub(OB.DEC.sub(grossSales, netSales), taxSales)
            );
          }
          if (ctaxReturns) {
            ctaxReturns.taxAmount = OB.DEC.add(
              ctaxReturns.taxAmount,
              OB.DEC.sub(OB.DEC.sub(grossReturns, netReturns), taxReturns)
            );
          }

          // save the calculated taxes into the cashup
          newCashupTaxes.forEach(newCashupTax => {
            const cashupTax = newCashup.cashTaxInfo.filter(function filter(
              cashupTaxFilter
            ) {
              return (
                cashupTaxFilter.name === newCashupTax.taxName &&
                cashupTaxFilter.orderType === newCashupTax.taxOrderType
              );
            })[0];
            if (cashupTax) {
              cashupTax.amount = OB.DEC.add(
                cashupTax.amount,
                newCashupTax.taxAmount
              );
            } else {
              newCashup.cashTaxInfo.push({
                id: OB.UTIL.get_UUID(), // not totally pure, but acepted since cannot predict in the action preparation how many new uuid will be needed
                name: newCashupTax.taxName,
                amount: newCashupTax.taxAmount,
                orderType: newCashupTax.taxOrderType
              });
            }
          });

          // set all payment methods to not used in the trx
          newCashup.cashPaymentMethodInfo.forEach(paymentMethod => {
            if (paymentMethod.usedInCurrentTrx !== false) {
              // eslint-disable-next-line no-param-reassign
              paymentMethod.usedInCurrentTrx = false;
            }
          });

          order.get('payments').models.forEach(orderPayment => {
            const cashupPayment = newCashup.cashPaymentMethodInfo.filter(
              function filter(cashupPaymentMethodFilter) {
                return (
                  cashupPaymentMethodFilter.searchKey ===
                    orderPayment.get('kind') &&
                  !orderPayment.get('isPrePayment')
                );
              }
            )[0];
            if (!cashupPayment) {
              // We cannot find this payment in local database, it must be a new payment method, we skip it.
              return;
            }
            precision =
              OB.MobileApp.model.paymentnames[cashupPayment.searchKey]
                .obposPosprecision;
            amount = _.isNumber(orderPayment.get('amountRounded'))
              ? orderPayment.get('amountRounded')
              : orderPayment.get('amount');
            if (
              amount < 0 ||
              (orderPayment.has('paymentRounding') &&
                orderPayment.get('paymentRounding') &&
                orderPayment.get('isReturnOrder'))
            ) {
              cashupPayment.totalReturns = OB.DEC.sub(
                cashupPayment.totalReturns,
                amount,
                precision
              );
            } else {
              cashupPayment.totalSales = OB.DEC.add(
                cashupPayment.totalSales,
                amount,
                precision
              );
            }
            // set used in transaction payment methods to true
            cashupPayment.usedInCurrentTrx = true;
          });
        }
      });

      return newCashup;
    }
  });

  OB.App.StateAPI.Cashup.updateCashup.addActionPreparation(
    async (state, payload) => {
      const newPayload = { ...payload };
      const { tickets } = payload;

      newPayload.tickets = [];
      tickets.forEach(ticket => {
        if (!ticket.has('obposIsDeleted')) {
          newPayload.tickets.push(ticket);
        }
      });
      if (newPayload.tickets.length === 0) {
        throw new OB.App.Class.ActionCanceled('tickets length is 0');
      }

      return newPayload;
    }
  );
})();
