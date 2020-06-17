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
  classes: 'obObposPointOfSaleUiReceiptView',
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
    onTableMultiSelectAll: 'tableMultiSelectAll',
    onAdjustOrderCaption: 'adjustOrderCaption'
  },
  components: [
    {
      kind: 'OB.UI.ReceiptsCounter',
      name: 'receiptcounter',
      classes: 'obObposPointOfSaleUiReceiptView-receiptcounter'
    },
    {
      name: 'receiptWrapper',
      classes: 'obObposPointOfSaleUiReceiptView-receiptWrapper',
      components: [
        {
          kind: 'OB.UI.OrderHeader',
          name: 'receiptHeader',
          classes:
            'obObposPointOfSaleUiReceiptView-receiptWrapper-receiptHeader'
        },
        {
          name: 'receiptBody',
          classes: 'obObposPointOfSaleUiReceiptView-receiptWrapper-receiptBody',
          components: [
            {
              kind: 'OB.UI.OrderCaptions',
              name: 'orderCaptions',
              classes:
                'obObposPointOfSaleUiReceiptView-receiptWrapper-receiptBody-orderCaptions'
            },
            {
              kind: 'OB.UI.OrderView',
              name: 'orderview',
              classes:
                'obObposPointOfSaleUiReceiptView-receiptWrapper-receiptBody-orderview'
            }
          ]
        },
        {
          kind: 'OB.UI.OrderFooter',
          name: 'receiptFooter',
          classes:
            'obObposPointOfSaleUiReceiptView-receiptWrapper-receiptFooter'
        }
      ]
    }
  ],
  orderChanged: function(oldValue) {
    this.$.receiptHeader.setOrder(this.order);
    this.$.orderview.setOrder(this.order);
    this.$.receiptFooter.setOrder(this.order);
  },
  orderListChanged: function(oldValue) {
    this.$.receiptcounter.setOrderList(this.orderList);
  },
  toggleSelectionMode: function(inSender, inEvent) {
    this.waterfall('onToggleSelectionTable', inEvent);
    this.doChangeSelectionMode(inEvent);
  },
  tableMultiSelectAll: function(inSender, inEvent) {
    this.waterfall('onMultiSelectAllTable');
  },
  adjustOrderCaption: function(inSender, inEvent) {
    var containerWidth = this.$.orderview.$.listOrderLines.getBounds().width,
      receiptLineWidth = this.$.orderview.$.listOrderLines.$.tbody.getBounds()
        .width,
      scrollWidth = 0;

    if (containerWidth && receiptLineWidth) {
      scrollWidth = OB.DEC.sub(containerWidth, receiptLineWidth);
    }

    if (this.order.get('lines').length > 0 && scrollWidth > 0) {
      this.$.orderCaptions.setStyle('margin-right: ' + scrollWidth + 'px');
      this.$.orderview.$.totalAndBreakdowns.setStyle(
        'padding-right: ' + scrollWidth + 'px'
      );
    } else {
      this.$.orderCaptions.setStyle('');
      this.$.orderview.$.totalAndBreakdowns.setStyle('');
    }
  }
});
