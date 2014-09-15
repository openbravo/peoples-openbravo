/*
 ************************************************************************************
 * Copyright (C) 2012-2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

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
    var content, me = this,
        orderDate = this.order.get('orderDate');
    if (this.order.get('hasbeenpaid') === 'Y') {
      orderDate = this.order.get('creationDate');
    }
    if (orderDate instanceof Date) {
      content = OB.I18N.formatHour(orderDate) + ' - ' + docNo;
    } else {
      content = orderDate + ' - ' + docNo;
    }
    OB.UTIL.HookManager.executeHooks('OBPOS_OrderDetailContentHook', {
      content: content,
      docNo: docNo,
      order: me.order
    }, function (args) {
      me.setContent(args.content);
    });
  },
  renderDataFromModel: function (order) {
    var content, me = this,
        orderDate = order.get('orderDate'),
        docNo = order.get('documentNo');
    if (order.get('hasbeenpaid') === 'Y') {
      orderDate = order.get('creationDate');
    }
    if (orderDate instanceof Date) {
      content = OB.I18N.formatHour(orderDate) + ' - ' + docNo;
    } else {
      content = orderDate + ' - ' + docNo;
    }
    OB.UTIL.HookManager.executeHooks('OBPOS_OrderDetailContentHook', {
      content: content,
      docNo: docNo,
      order: order,
      orderDate: orderDate
    }, function (args) {
      me.setContent(args.content);
    });
  },
  orderChanged: function (oldValue) {
    this.renderData(this.order.get('documentNo'));
    this.order.on('change:documentNo', function (model) {
      this.renderData(model.get('documentNo'));
    }, this);
    this.order.on('change:creationDate', function (model) {
      this.renderDataFromModel(model);
    }, this);
  }
});