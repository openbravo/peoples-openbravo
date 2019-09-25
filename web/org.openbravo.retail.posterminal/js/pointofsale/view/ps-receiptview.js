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
    onChangeOrderCaptionWidth: 'changeOrderCaptionWidth'
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
  changeOrderCaptionWidth: function(inSender, inEvent) {
    if (
      this.$.orderview.$.listOrderLines.getScrollArea().getScrollBounds()
        .maxTop !== 0
    ) {
      this.$.orderCaptions.$.description.hasNode().style.width =
        'calc(100% - 285px)';
    } else if (inEvent.status) {
      this.$.orderCaptions.$.description.hasNode().style.width =
        'calc(100% - 270px)';
    } else {
      if (this.$.orderCaptions.$.description.hasNode()) {
        this.$.orderCaptions.$.description
          .hasNode()
          .style.removeProperty('width');
      }
    }
  }
});
