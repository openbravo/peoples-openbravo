/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.TicketUtils = OB.UTIL.TicketUtils || {};

  OB.UTIL.TicketUtils.loadAndSyncTicketFromState = function() {
    if (OB.App.StateBackwardCompatibility) {
      const tmpTicket = new OB.Model.Order();
      const compatibleModel = OB.App.StateBackwardCompatibility.bind(
        OB.App.State.Ticket,
        tmpTicket
      );

      const stateTicket = OB.App.State.getState().Ticket;
      if (Object.keys(stateTicket).length !== 0) {
        compatibleModel.handleStateChange(stateTicket);
      }

      OB.MobileApp.model.receipt.clearWith(tmpTicket);

      OB.App.StateBackwardCompatibility.bind(
        OB.App.State.Ticket,
        OB.MobileApp.model.receipt
      );
    }
  };

  OB.UTIL.TicketUtils.addTicketCreationDataToPayload = function(payload = {}) {
    const newPayload = { ...payload };

    newPayload.terminal = OB.MobileApp.model.get('terminal');
    newPayload.store = OB.MobileApp.model.get('store');
    newPayload.warehouses = OB.MobileApp.model.get('warehouses');
    newPayload.businessPartner = OB.MobileApp.model.get('businessPartner')
      ? JSON.parse(JSON.stringify(OB.MobileApp.model.get('businessPartner')))
      : undefined;
    newPayload.payments = OB.MobileApp.model.get('payments');
    newPayload.paymentcash = OB.MobileApp.model.get('paymentcash');
    newPayload.deliveryPaymentMode = OB.MobileApp.model.get(
      'deliveryPaymentMode'
    );
    newPayload.session = OB.MobileApp.model.get('session');
    newPayload.orgUserId = OB.MobileApp.model.get('orgUserId');
    newPayload.pricelist = OB.MobileApp.model.get('pricelist');
    newPayload.context = OB.MobileApp.model.get('context');
    newPayload.documentNumberSeparator = OB.Model.Order.prototype
      .includeDocNoSeperator
      ? '/'
      : '';
    newPayload.multiTickets = OB.MobileApp.model.multiOrders
      ? JSON.parse(JSON.stringify(OB.MobileApp.model.multiOrders))
      : undefined;
    newPayload.ticketExtraProperties = OB.UTIL.TicketUtils.getTicketExtraProperties();
    newPayload.discountRules = OB.Discounts.Pos.ruleImpls;
    newPayload.bpSets = OB.Discounts.Pos.bpSets;
    newPayload.taxRules = OB.Taxes.Pos.ruleImpls;
    newPayload.preferences = {
      salesWithOneLineNegativeAsReturns: OB.MobileApp.model.hasPermission(
        'OBPOS_SalesWithOneLineNegativeAsReturns',
        true
      ),
      splitChange: OB.MobileApp.model.hasPermission('OBPOS_SplitChange', true),
      removeTicket: OB.MobileApp.model.hasPermission(
        'OBPOS_remove_ticket',
        true
      ),
      alwaysCreateNewReceiptAfterPayReceipt: OB.MobileApp.model.hasPermission(
        'OBPOS_alwaysCreateNewReceiptAfterPayReceipt',
        true
      ),
      enableMultiPriceList: OB.MobileApp.model.hasPermission(
        'EnableMultiPriceList',
        true
      ),
      enableDeliveryModes: OB.MobileApp.model.hasPermission(
        'OBRDM_EnableDeliveryModes',
        true
      )
    };
    newPayload.constants = {
      fieldSeparator: OB.Constants.FIELDSEPARATOR,
      identifierSuffix: OB.Constants.IDENTIFIER
    };

    return newPayload;
  };

  OB.UTIL.TicketUtils.getTicketExtraProperties = function() {
    const window = OB.MobileApp.view.$.containerWindow;
    if (
      window &&
      window.getRoot() &&
      window.getRoot().$.receiptPropertiesDialog
    ) {
      const properties = window.getRoot().$.receiptPropertiesDialog
        .newAttributes;
      return properties.reduce((o, p) => {
        if (p.modelProperty) {
          return { ...o, [p.modelProperty]: p.defaultValue || '' };
        }
        if (p.extraProperties) {
          const extraProperties = p.extraProperties.reduce((oep, ep) => {
            return { ...oep, [ep]: '' };
          }, {});
          return { ...o, ...extraProperties };
        }
        return o;
      }, {});
    }
    return {};
  };

  OB.UTIL.TicketUtils.printTicketLine = function(receipt, lineIds) {
    const receiptLines = receipt
      .get('lines')
      .filter(l => lineIds.includes(l.id))
      .flat();
    receiptLines.forEach(line => {
      OB.App.State.Global.printTicketLine({
        line: JSON.parse(JSON.stringify(line))
      });
    });
  };
})();
