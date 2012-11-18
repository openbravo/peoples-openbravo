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
    if (nrItems > 1) {
      this.$.receiptsCounterButton.$.counter.setContent(nrItems - 1);
      this.show();
    } else {
      this.$.receiptsCounterButton.$.counter.setContent('');
      this.hide();
    }
  },
  orderListChanged: function (oldValue) {
    var me = this;
    this.doSetReceiptsList({
      orderList: this.orderList
    });
    this.renderNrItems(this.orderList.length);
    this.orderList.on('all', function (model) {
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
  tap: function () {
    this.doShowPopup({
      popup: 'modalreceipts'
    });
  },
  components: [{
    style: 'position: absolute; top: -35px; right:-35px; background: #404040; height:70px; width: 70px; -webkit-transform: rotate(45deg); -moz-transform: rotate(45deg); -ms-transform: rotate(45deg); -transform: rotate(45deg);'
  }, {
    name: 'counter',
    style: 'position: absolute; top: 0px; right:0px; padding-top: 5px; padding-right: 10px; font-weight: bold; color: white;'
  }]
});