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
  classes: 'obObposPointOfSaleUiReceiptView span6',
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
    classes: 'obObposPointOfSaleUiReceiptView-container1',
    components: [{
      classes: 'obObposPointOfSaleUiReceiptView-container1-container1',
      components: [{
        kind: 'OB.UI.ReceiptsCounter',
        name: 'receiptcounter',
        classes: 'obObposPointOfSaleUiReceiptView-container1-container1-receiptcounter'
      }, {
        classes: 'obObposPointOfSaleUiReceiptView-container1-container1-container2',
        components: [{
          kind: 'OB.UI.OrderHeader',
          name: 'receiptheader',
          classes: 'obObposPointOfSaleUiReceiptView-container1-container1-container2-receiptheader'
        }, {
          classes: 'obObposPointOfSaleUiReceiptView-container1-container1-container2-container2',
          components: [{
            classes: 'obObposPointOfSaleUiReceiptView-container1-container1-container2-container2-container1',
            components: [{
              kind: 'OB.UI.OrderView',
              name: 'orderview',
              classes: 'obObposPointOfSaleUiReceiptView-container1-container1-container2-container2-container1-orderview'
            }]
          }]
        }, {
          kind: 'OB.UI.OrderFooter',
          name: 'receiptfooter',
          classes: 'obObposPointOfSaleUiReceiptView-container1-container1-container2-receiptfooter'
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