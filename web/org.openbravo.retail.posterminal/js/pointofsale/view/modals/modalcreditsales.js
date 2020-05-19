/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo*/

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit',
  classes: 'obObposPointOfSaleUiModalsModalEnoughCredit',
  i18nHeader: 'OBPOS_SellingOnCreditHeader',
  body: '',
  footer: {
    classes: 'obObposPointOfSaleUiModalsModalEnoughCredit-footer',
    components: [
      {
        kind:
          'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit.Components.cancel_button',
        classes:
          'obObposPointOfSaleUiModalsModalEnoughCredit-footer-obObposPointOfSaleUiModalsModalEnoughCreditComponentsCancelButton'
      },
      {
        kind:
          'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit.Components.apply_button',
        classes:
          'obObposPointOfSaleUiModalsModalEnoughCredit-footer-obObposPointOfSaleUiModalsModalEnoughCreditComponentsApplyButton'
      }
    ]
  },
  init: function(model) {
    this.model = model;
  },
  executeOnShow: function() {
    this.actionCancel = true;
    var pendingQty = this.args.order.getPending();
    var bpName = this.args.order.get('bp').get('_identifier');
    var selectedPaymentMethod = this.args.order.get('selectedPayment');
    var currSymbol = OB.MobileApp.model.get('terminal').symbol;
    var rate = 1,
      i;
    var paymentList = OB.MobileApp.model.get('payments');
    for (i = 0; i < paymentList.length; i++) {
      if (paymentList[i].payment.searchKey === selectedPaymentMethod) {
        rate = paymentList[i].mulrate;
        currSymbol = paymentList[i].symbol;
      }
    }
    if (this.args.message) {
      this.setBody(OB.I18N.getLabel(this.args.message));
    } else if (this.args.order.get('orderType') === 1) {
      this.setBody(
        OB.I18N.getLabel('OBPOS_enoughCreditReturnBody', [
          OB.I18N.formatCurrency(pendingQty * rate) + ' ' + currSymbol,
          bpName
        ])
      );
    } else {
      this.setBody(
        OB.I18N.getLabel('OBPOS_enoughCreditBody', [
          OB.I18N.formatCurrency(pendingQty * rate) + ' ' + currSymbol,
          bpName
        ])
      );
    }
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name:
    'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit.Components.apply_button',
  classes: 'obObposPointOfSaleUiModalsModalEnoughCreditComponentsApplyButton',
  i18nContent: 'OBPOS_ConfirmSellOnCredit',
  isDefaultAction: true,
  init: function(model) {
    this.model = model;
  },
  tap: function() {
    function error(tx) {
      OB.UTIL.showError(tx);
    }

    this.owner.owner.actionCancel = false;
    this.doHideThisPopup();
    this.model.get('order').set('payOnCredit', true);
    this.allowOpenDrawer = false;
    var payments = this.model.get('order').get('payments');
    var me = this;

    payments.each(function(payment) {
      if (payment.get('allowOpenDrawer') || payment.get('isCash')) {
        me.allowOpenDrawer = true;
      }
    });

    if (this.allowOpenDrawer) {
      OB.POS.hwserver.openDrawer(
        {
          openFirst: false,
          receipt: this.model.get('order')
        },
        OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales
      );
    }
    var bp = this.model.get('order').get('bp');
    var bpCreditUsed = this.model
      .get('order')
      .get('bp')
      .get('creditUsed');
    var totalPending = this.model.get('order').getPending();

    if (this.parent.parent.parent.args.order.get('gross') < 0) {
      bp.set('creditUsed', bpCreditUsed - totalPending);
    } else {
      bp.set('creditUsed', bpCreditUsed + totalPending);
    }
    me.model.get('order').trigger('paymentDone');
    // when in multiorder view, delete the order from the list after payment
    if (me.model.get('leftColumnViewManager').isMultiOrder()) {
      me.model
        .get('multiOrders')
        .get('multiOrdersList')
        .remove(me.model.get('order'));

      if (me.model.get('multiOrders').get('multiOrdersList').length === 0) {
        me.model.deleteMultiOrderList();
        me.model.get('multiOrders').resetValues();
        me.model.get('leftColumnViewManager').setOrderMode();
      }
    }
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name:
    'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit.Components.cancel_button',
  classes: 'obObposPointOfSaleUiModalsModalEnoughCreditComponentsCancelButton',
  i18nContent: 'OBMOBC_LblCancel',
  init: function(model) {
    this.model = model;
  },
  tap: function() {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.OBPOSPointOfSale.UI.Modals.modalNotEnoughCredit',
  classes: 'obObposPointOfSaleUiModalsModalNotEnoughCredit',
  i18nHeader: 'OBPOS_notEnoughCreditHeader',
  body: '',
  executeOnShow: function() {
    if (this.args) {
      this.setBody(
        OB.I18N.getLabel('OBPOS_notEnoughCreditBody', [
          this.args.bpName,
          OB.I18N.formatCurrency(this.args.actualCredit)
        ])
      );
    }
  },
  footer: {
    classes: 'obObposPointOfSaleUiModalsModalNotEnoughCredit-footer',
    components: [
      {
        kind: 'OB.UI.ModalDialogButton',
        name:
          'OB.OBPOSPointOfSale.UI.Modals.modalNotEnoughCredit.Components.ok_button',
        classes:
          'obObposPointOfSaleUiModalsModalNotEnoughCredit-footer-obObposPointOfSaleUiModalsModalNotEnoughCreditComponentsOkButton',
        i18nContent: 'OBMOBC_LblOk',
        isDefaultAction: true,
        init: function(model) {
          this.model = model;
        },
        tap: function() {
          this.doHideThisPopup();
        }
      }
    ]
  }
});
