/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo */

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
    onAllTicketLinesChecked: 'allTicketLinesChecked',
    onToggleSelectionMode: 'toggleSelectionMode',
    onTableMultiSelectAll: 'tableMultiSelectAll'
  },
  components: [{
    style: 'margin: 5px',
    components: [{
      style: 'position: relative;background-color: #ffffff; color: black;overflow-y: auto; max-height:622px',
      components: [{
        kind: 'OB.UI.ReceiptsCounter',
        name: 'receiptcounter'
      }, {
        style: 'padding: 5px;',
        components: [{
          kind: 'OB.UI.OrderHeader',
          name: 'receiptheader'
        }, {
          classes: 'row-fluid',
          style: 'max-height: 536px;',
          components: [{
            classes: 'span12',
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