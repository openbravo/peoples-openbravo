/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _ */


enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.Customer',
  classes: 'btnlink btnlink-small btnlink-gray',
  style: 'float:left; margin:7px; height:27px; padding: 4px 15px 7px 15px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap; width: 80%;',
  published: {
    order: null,
    target: null,
    popup: null
  },
  events: {
    onShowPopup: '',
    onHidePopup: ''
  },
  tap: function () {
    if (!this.disabled) {
      var qty = 0;
      enyo.forEach(this.order.get('lines').models, function (l) {
        if (l.get('originalOrderLineId')) {
          qty = qty + 1;
          return;
        }
      });
      if (qty !== 0 && !OB.MobileApp.model.hasPermission('OBPOS_AllowChangeCustomerVerifiedReturns', true)) {
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
        popup: 'modalcustomer',
        args: {
          target: this.target
        }
      });
    }
  },
  init: function (model) {
    this.setOrder(model.get('order'));
    this.hiddenPopup = false;
  },
  renderCustomer: function (newCustomer) {
    this.setContent(newCustomer);
  },
  orderChanged: function (oldValue) {
    if (this.order.get('bp')) {
      this.renderCustomer(this.order.get('bp').get('_identifier'));
    } else {
      this.renderCustomer('');
    }

    this.order.on('change:bp', function (model) {
      if (model.get('bp')) {
        this.renderCustomer(model.get('bp').get('_identifier'));
      } else {
        this.renderCustomer('');
      }
    }, this);
  }
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.ShipTo',
  classes: 'btnlink btnlink-small btnlink-gray',
  style: 'float:left; margin:7px; height:27px; padding: 4px 15px 7px 15px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap; width: 80%;',
  published: {
    order: null,
    target: null,
    popup: null
  },
  events: {
    onShowPopup: '',
    onHidePopup: ''
  },
  tap: function () {
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
  init: function (model) {
    this.setOrder(model.get('order'));
    this.hiddenPopup = false;
  },
  renderAddrShip: function (newAddr) {
    this.setContent(newAddr);
  },
  orderChanged: function (oldValue) {
    if (this.order.get('bp')) {
      this.renderAddrShip(this.order.get('bp').get('shipLocName'));
    } else {
      this.renderAddrShip('');
    }
    this.order.on('change:bp', function (model) {
      if (model.get('bp')) {
        this.renderAddrShip(model.get('bp').get('shipLocName'));
      } else {
        this.renderAddrShip('');
      }
    }, this);
  }
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BillTo',
  classes: 'btnlink btnlink-small btnlink-gray',
  style: 'float:left; margin:7px; height:27px; padding: 4px 15px 7px 15px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap; width: 80%;',
  published: {
    order: null,
    target: null,
    popup: null
  },
  events: {
    onShowPopup: '',
    onHidePopup: ''
  },
  tap: function () {
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
  init: function (model) {
    this.setOrder(model.get('order'));
    this.hiddenPopup = false;
  },
  renderAddrBill: function (newAddr) {
    this.setContent(newAddr);
  },
  orderChanged: function (oldValue) {
    if (this.order.get('bp')) {
      this.renderAddrBill(this.order.get('bp').get('locName'));
    } else {
      this.renderAddrBill('');
    }
    this.order.on('change:bp', function (model) {
      if (model.get('bp')) {
        this.renderAddrBill(model.get('bp').get('locName'));
      } else {
        this.renderAddrBill('');
      }
    }, this);
  }
});