/*
 ************************************************************************************
 * Copyright (C) 2012-2014 Openbravo S.L.U.
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
    style: 'padding: 13px 50px 15px 10px; font-weight: bold; color: #6CB33F;'
  },
  initComponents: function () {},
  renderData: function (docNo) {
    var content, me = this;
    if (this.order.get('orderDate') instanceof Date) {
      content = OB.I18N.formatHour(this.order.get('orderDate')) + ' - ' + docNo;
    } else {
      content = this.order.get('orderDate') + ' - ' + docNo;
    }
    OB.UTIL.HookManager.executeHooks('OBPOS_OrderDetailContentHook', {
      content: content,
      docNo: docNo,
      order: me.order
    }, function (args) {
      me.setContent(args.content);
    });
  },
  orderChanged: function (oldValue) {
    this.renderData(this.order.get('documentNo'));
    this.order.on('change:documentNo', function (model) {
      this.renderData(model.get('documentNo'));
    }, this);
  }
});