/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo*/

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ReceiptView',
  classes: 'span6',
  published: {
    order: null,
    orderList: null
  },
  events: {
    onChangeSelectionMode: ''
  },
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxBehavior',
    onToggleSelectionMode: 'toggleSelectionMode',
    onTableMultiSelectAll: 'tableMultiSelectAll'
  },
  components: [{
    style: 'margin: 5px',
    components: [{
      style: 'position: relative; background-color: #ffffff; color: black;overflow-y: auto; max-height: 612px',
      components: [{
        kind: 'OB.UI.ReceiptsCounter',
        name: 'receiptcounter'
      }, {
        style: 'padding: 5px;',
        components: [{
          kind: 'OB.UI.OrderHeader',
          name: 'receiptheader'
        }, {
          style: 'max-height: 536px;',
          components: [{
            components: [{
              kind: 'OB.UI.OrderView',
              name: 'orderview'
            }]
          }]
        }, {
          kind: 'OB.UI.OrderFooter',
          name: 'receiptfooter'
        }]
      }]
    }]
  }],
  orderChanged: function (oldValue) {
    this.$.receiptheader.setOrder(this.order);
    this.$.orderview.setOrder(this.order);
    this.$.receiptfooter.setOrder(this.order);
  },
  orderListChanged: function (oldValue) {
    this.$.receiptcounter.setOrderList(this.orderList);
  },
  toggleSelectionMode: function (inSender, inEvent) {
    this.waterfall('onToggleSelectionTable', inEvent);
    this.doChangeSelectionMode(inEvent);
  },
  tableMultiSelectAll: function (inSender, inEvent) {
    this.waterfall('onMultiSelectAllTable');
  }
});