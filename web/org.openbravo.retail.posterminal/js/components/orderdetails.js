/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.OrderDetails',
  published: {
    order: null
  },
  attributes: {
    style: 'float:left; padding: 15px 15px 5px 10px; font-weight: bold; color: #6CB33F;'
  },
  initComponents: function () {},
  renderData: function (docNo) {
    if (this.order.get('orderDate') instanceof Date) {
      this.setContent(OB.I18N.formatHour(this.order.get('orderDate')) + ' - ' + docNo);
    } else {
      this.setContent(this.order.get('orderDate') + ' - ' + docNo);
    }
  },
  orderChanged: function (oldValue) {
    this.renderData(this.order.get('documentNo'));
    this.order.on('change:documentNo', function (model) {
      this.renderData(model.get('documentNo'));
    }, this);
  }
});