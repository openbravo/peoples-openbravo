/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.ReceiptsCounter',
  classes: 'obUiReceiptsCounter',
  showing: false,
  published: {
    orderList: null
  },
  components: [
    {
      kind: 'OB.UI.ReceiptsCounterButton',
      name: 'receiptsCounterButton',
      classes: 'obUiReceiptsCounter-receiptsCounterButton'
    }
  ],
  events: {
    onSetReceiptsList: ''
  },
  initComponents: function() {
    this.inherited(arguments);
    OB.App.PersistenceChangeListenerManager.addListener(
      state => {
        this.renderNrItems(state.TicketList.length + 1);
      },
      ['TicketList']
    );
  },
  renderNrItems: function(nrItems) {
    var receiptLabels;
    try {
      receiptLabels = OB.POS.terminal.$.containerWindow.getRoot().$.multiColumn
        .$.leftPanel.$.receiptview.$.receiptHeader.$.receiptLabels;
    } catch (e) {
      OB.error('receiptLabels not found');
    }
    if (nrItems > 1) {
      if (nrItems < 1000) {
        this.$.receiptsCounterButton.setLabel(nrItems);
      } else {
        this.$.receiptsCounterButton.setLabel('...');
      }
      if (receiptLabels) {
        // If the receipt counter button is shown, the receipt top labels should have a right padding to avoid overlapping
        // [TODO] Change at the same time as component/order.js
        receiptLabels.removeClass(
          'obUiReceiptsCounter-receiptsCounterButton-receiptLabel_singleLine'
        );
        receiptLabels.addClass(
          'obUiReceiptsCounter-receiptsCounterButton-receiptLabel_multiLine'
        );
      }
      this.show();
    } else {
      this.$.receiptsCounterButton.setLabel('');
      if (receiptLabels) {
        // If the receipt counter button is not shown, the receipt top labels should reach the right of the receipt area
        // [TODO] Change at the same time as component/order.js
        receiptLabels.removeClass(
          'obUiReceiptsCounter-receiptsCounterButton-receiptLabel_multiLine'
        );
        receiptLabels.addClass(
          'obUiReceiptsCounter-receiptsCounterButton-receiptLabel_singleLine'
        );
      }
      this.hide();
    }
  },
  orderListChanged: function(oldValue) {
    // var me = this;
    // this.doSetReceiptsList({
    //   orderList: this.orderList
    // });
    // this.renderNrItems(this.orderList.length);
    // this.orderList.on(
    //   'add remove reset',
    //   function() {
    //     me.renderNrItems(me.orderList.length);
    //   },
    //   this
    // );
  }
});

enyo.kind({
  name: 'OB.UI.ReceiptsCounterButton',
  kind: 'OB.UI.Button',
  classes: 'obUiReceiptsCounterButton',
  events: {
    onShowPopup: ''
  },
  handlers: {
    onOrderSelectionDisabled: 'orderDisabled'
  },
  orderDisabled: function(inSender, inEvent) {
    this.setDisabled(inEvent.status);
    this.addRemoveClass('disabled', inEvent.status);
  },
  tap: function() {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalreceipts'
      });
    }
  }
});
