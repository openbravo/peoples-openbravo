/*
 ************************************************************************************
 * Copyright (C) 2016-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global OB, enyo*/

enyo.kind({
  kind: 'OB.UI.FormElement.Selector',
  name: 'OB.UI.Customer',
  classes: 'obUiCustomer',
  published: {
    order: null,
    target: null,
    popup: null
  },
  events: {
    onShowPopup: '',
    onHidePopup: ''
  },
  tap: function() {
    if (!this.disabled) {
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

      this.hiddenPopup = true;
      this.doHidePopup({
        popup: this.popup,
        args: {
          activeFlow: true
        }
      });
      this.doShowPopup({
        popup: OB.UTIL.modalCustomer(),
        args: {
          target: this.target
        }
      });
    }
  },
  init: function(model) {
    this.setOrder(model.get('order'));
    this.hiddenPopup = false;
  },
  renderCustomer: function(newCustomerId, newCustomerName) {
    this.setValue(newCustomerId, newCustomerName);
  },
  renderCurrentCustomer: function() {
    if (this.order.get('externalBusinessPartner')) {
      // External Business Partner if available
      const bp = new OB.App.Class.ExternalBusinessPartner(
        this.order.get('externalBusinessPartner')
      );
      this.renderCustomer(bp.getKey(), bp.getIdentifier());
    } else if (this.order.get('bp')) {
      // Business Partner if available
      this.renderCustomer(
        this.order.get('bp').get('id'),
        this.order.get('bp').get('_identifier')
      );
    } else {
      // No Business Partner available
      this.renderCustomer(null, '');
    }
  },
  orderChanged: function(oldValue) {
    this.renderCurrentCustomer();
    this.order.on('change:bp change:externalBusinessPartner', () => {
      this.renderCurrentCustomer();
    });
  }
});

enyo.kind({
  kind: 'OB.UI.FormElement.Selector',
  name: 'OB.UI.ShipTo',
  classes: 'obUiShipTo',
  published: {
    order: null,
    target: null,
    popup: null
  },
  events: {
    onShowPopup: '',
    onHidePopup: ''
  },
  tap: function() {
    if (!this.disabled) {
      this.hiddenPopup = true;
      this.doHidePopup({
        popup: this.popup
      });
      this.doShowPopup({
        popup: 'modalcustomershipaddress',
        args: {
          target: this.target,
          flowTrigger: 'flowReceiptProperties'
        }
      });
    }
  },
  init: function(model) {
    this.setOrder(model.get('order'));
    this.hiddenPopup = false;
  },
  renderAddrShip: function(newAddrId, newAddrName) {
    this.setValue(newAddrId, newAddrName);
  },
  orderChanged: function(oldValue) {
    if (OB.UTIL.externalBp()) {
      return;
    }
    if (this.order.get('bp')) {
      this.renderAddrShip(
        this.order.get('bp').get('shipLocId'),
        this.order.get('bp').get('shipLocName')
      );
    } else {
      this.renderAddrShip(null, '');
    }
    this.order.on(
      'change:bp',
      function(model) {
        if (model.get('bp')) {
          this.renderAddrShip(
            model.get('bp').get('shipLocId'),
            model.get('bp').get('shipLocName')
          );
        } else {
          this.renderAddrShip(null, '');
        }
      },
      this
    );
  }
});

enyo.kind({
  kind: 'OB.UI.FormElement.Selector',
  name: 'OB.UI.BillTo',
  classes: 'obUiBillTo',
  published: {
    order: null,
    target: null,
    popup: null
  },
  events: {
    onShowPopup: '',
    onHidePopup: ''
  },
  tap: function() {
    if (!this.disabled) {
      this.hiddenPopup = true;
      this.doHidePopup({
        popup: this.popup
      });
      this.doShowPopup({
        popup: 'modalcustomeraddress',
        args: {
          target: this.target,
          flowTrigger: 'flowReceiptProperties'
        }
      });
    }
  },
  init: function(model) {
    this.setOrder(model.get('order'));
    this.hiddenPopup = false;
  },
  renderAddrBill: function(newAddrId, newAddrName) {
    this.setValue(newAddrId, newAddrName);
  },
  orderChanged: function(oldValue) {
    if (OB.UTIL.externalBp()) {
      return;
    }
    if (this.order.get('bp')) {
      this.renderAddrBill(
        this.order.get('bp').get('locId'),
        this.order.get('bp').get('locName')
      );
    } else {
      this.renderAddrBill(null, '');
    }
    this.order.on(
      'change:bp',
      function(model) {
        if (model.get('bp')) {
          this.renderAddrBill(
            model.get('bp').get('locId'),
            model.get('bp').get('locName')
          );
        } else {
          this.renderAddrBill(null, '');
        }
      },
      this
    );
  }
});
