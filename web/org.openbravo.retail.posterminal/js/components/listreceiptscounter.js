/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.ReceiptsCounter',
  style: 'position: absolute; top:0px; right: 0px;',
  showing: false,
  published: {
    orderList: null
  },
  components: [{
    kind: 'OB.UI.ReceiptsCounterButton',
    name: 'receiptsCounterButton'
  }],
  events: {
    onSetReceiptsList: ''
  },
  renderNrItems: function (nrItems) {
    var receiptLabels;
    try {
      receiptLabels = OB.POS.terminal.$.containerWindow.getRoot().$.multiColumn.$.leftPanel.$.receiptview.$.receiptheader.$.receiptLabels;
    } catch (e) {
      OB.error('receiptLabels not found');
    }
    if (nrItems > 1) {
      this.$.receiptsCounterButton.$.counter.setContent(nrItems - 1);
      if (receiptLabels) {
        // If the receipt counter button is shown, the receipt top labels should have a right padding to avoid overlapping
        receiptLabels.applyStyle('padding-right', '55px');
      }
      this.show();
    } else {
      this.$.receiptsCounterButton.$.counter.setContent('');
      if (receiptLabels) {
        // If the receipt counter button is not shown, the receipt top labels should reach the right of the receipt area
        receiptLabels.applyStyle('padding-right', '5px');
      }
      this.hide();
    }
  },
  orderListChanged: function (oldValue) {
    var me = this;
    this.doSetReceiptsList({
      orderList: this.orderList
    });
    this.renderNrItems(this.orderList.length);
    this.orderList.on('add remove reset', function () {
      me.renderNrItems(me.orderList.length);
    }, this);
  },
  initComponents: function () {
    this.inherited(arguments);
  }
});


enyo.kind({
  name: 'OB.UI.ReceiptsCounterButton',
  kind: 'OB.UI.Button',
  classes: 'btnlink btnlink-gray',
  style: 'position: relative; overflow: hidden; margin:0px; padding:0px; height:50px; width: 50px;',
  events: {
    onShowPopup: ''
  },
  handlers: {
    onOrderSelectionDisabled: 'orderDisabled'
  },
  orderDisabled: function (inSender, inEvent) {
    this.setDisabled(inEvent.status);
    this.addRemoveClass('disabled', inEvent.status);
  },
  tap: function () {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalreceipts'
      });
    }
  },
  components: [{
    style: 'position: absolute; top: -35px; right:-35px; background: #404040; height:70px; width: 70px; -webkit-transform: rotate(45deg); -moz-transform: rotate(45deg); -ms-transform: rotate(45deg); -transform: rotate(45deg);'
  }, {
    name: 'counter',
    style: 'position: absolute; top: 0px; right:0px; padding-top: 5px; padding-right: 10px; font-weight: bold; color: white;'
  }]
});