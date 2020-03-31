/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, OB */

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BusinessPartner',
  classes: 'obUiBusinessPartner',
  published: {
    order: null
  },
  events: {
    onShowPopup: ''
  },
  handlers: {
    onBPSelectionDisabled: 'buttonDisabled'
  },
  buttonDisabled: function(inSender, inEvent) {
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
    if (!this.isEnabled) {
      this.removeClass('obUiBusinessPartner_enabled');
      this.addClass('obUiBusinessPartner_disabled');
    } else {
      this.removeClass('obUiBusinessPartner_disabled');
      this.addClass('obUiBusinessPartner_enabled');
    }
  },
  tap: function() {
    var qty = 0;
    enyo.forEach(this.order.get('lines').models, function(l) {
      if (l.get('originalOrderLineId')) {
        qty = qty + 1;
        return;
      }
    });
    if (
      qty !== 0 &&
      !OB.MobileApp.model.hasPermission(
        'OBPOS_AllowChangeCustomerVerifiedReturns',
        true
      )
    ) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_Cannot_Change_BPartner'));
      return;
    }

    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalcustomer'
      });
    }
  },
  initComponents: function() {
    return this;
  },
  renderCustomer: function(newCustomer) {
    this.setContent(newCustomer);
  },
  orderChanged: function(oldValue) {
    if (this.order.get('bp')) {
      this.renderCustomer(this.order.get('bp').get('_identifier'));
    } else {
      this.renderCustomer('');
    }

    this.order.on(
      'change:bp',
      function(model) {
        if (model.get('bp')) {
          model.set(
            'generateInvoice',
            OB.MobileApp.model.get('terminal').terminalType.generateInvoice
          );
          model.set('fullInvoice', false);
          this.renderCustomer(model.get('bp').get('_identifier'));
        } else {
          this.renderCustomer('');
        }
      },
      this
    );
  }
});

/*Modal*/

/*header of scrollable table*/
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.NewCustomerWindowButton',
  events: {
    onChangeSubWindow: '',
    onHideThisPopup: ''
  },
  disabled: false,
  classes:
    'obUiNewCustomerWindowButton businesspartner-obUiButton-generic_yellow',
  i18nLabel: 'OBPOS_LblNewCustomer',
  handlers: {
    onSetModel: 'setModel',
    onNewBPDisabled: 'doDisableNewBP'
  },
  setModel: function(inSender, inEvent) {
    this.model = inEvent.model;
  },
  doDisableNewBP: function(inSender, inEvent) {
    this.setDisabled(inEvent.status);
  },
  tap: function(model) {
    if (this.disabled) {
      return true;
    }
    this.doHideThisPopup();
    this.doChangeSubWindow({
      newWindow: {
        name: 'customerCreateAndEdit',
        params: {
          navigateOnClose: 'mainSubWindow'
        }
      }
    });
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.AdvancedSearchCustomerWindowButton',
  classes:
    'obUiAdvancedSearchCustomerWindowButton businesspartner-obUiButton-generic_yellow',
  i18nLabel: 'OBPOS_LblAdvancedSearch',
  disabled: false,
  handlers: {
    onSetModel: 'setModel',
    onNewBPDisabled: 'doDisableNewBP'
  },
  setModel: function(inSender, inEvent) {
    this.model = inEvent.model;
  },
  doDisableNewBP: function(inSender, inEvent) {
    this.setDisabled(inEvent.status);
  },
  events: {
    onHideThisPopup: ''
  },
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.doHideThisPopup();
    this.model.get('subWindowManager').set('currentWindow', {
      name: 'customerAdvancedSearch',
      params: {
        caller: 'mainSubWindow'
      }
    });
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setDisabled(
      !OB.MobileApp.model.hasPermission('OBPOS_receipt.customers')
    );
  }
});
