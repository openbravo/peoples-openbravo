/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo,_ */
enyo.kind({
  name: 'OB.UI.RemoveMultiOrders',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-darkgray btnlink-payment-clear btn-icon-small btn-icon-clearPayment',
  events: {
    onRemoveMultiOrders: ''
  },
  tap: function () {
    var me = this;
    if ((_.isUndefined(this.deleting) || this.deleting === false)) {
      this.deleting = true;
      this.removeClass('btn-icon-clearPayment');
      this.addClass('btn-icon-loading');
      this.doRemoveMultiOrders({
        order: this.owner.model
      });
    }
  }
});

enyo.kind({
  kind: 'OB.UI.SelectButton',
  name: 'OB.UI.RenderMultiOrdersLineValues',
  classes: 'btnselect-orderline',
  handlers: {
    onChangeEditMode: 'changeEditMode'
  },
  events: {
    onShowPopup: ''
  },
  components: [{
    name: 'line',
    style: 'line-height: 23px; width: 100%;',
    components: [{
      name: 'multiTopLine',
      initComponents: function () {
        this.setContent(this.owner.owner.model.get('documentNo') + ' - ' + this.owner.owner.model.get('bp').get('_identifier'));
      }
    }, {
      style: 'color: #888888; display: inline;',
      name: 'multiBottonLine',
      initComponents: function () {
        this.setContent(this.owner.owner.model.printTotal() + ' - Remaining to pay: ' + this.owner.owner.model.printPending() + ' - (' + OB.I18N.formatDate(new Date(this.owner.owner.model.get('orderDate'))) + ') ');
      }
    }, {
      style: 'font-weight: bold; float: right; text-align:right; ',
      name: 'total',
      initComponents: function () {
        this.setContent(this.owner.owner.model.get('amountToLayaway') ? OB.I18N.formatCurrency(this.owner.owner.model.get('amountToLayaway')) : this.owner.owner.model.printPending());
      }
    }, {
      style: 'clear: both;'
    }]
  }],
  tap: function () {
    this.doShowPopup({
      popup: 'modalmultiorderslayaway',
      args: this.owner.model
    });
  },
  changeEditMode: function (inSender, inEvent) {
    this.addRemoveClass('btnselect-orderline-edit', inEvent.edit);
    this.bubble('onShowColumn', {
      colNum: 1
    });
  }
});

enyo.kind({
  name: 'OB.UI.RenderMultiOrdersLine',
  style: 'border-bottom: 1px solid #cccccc; width: 100%',
  components: [{
    style: 'display: inline; width: 8%',
    kind: 'OB.UI.RemoveMultiOrders'
  }, {
    style: 'display: inline; width: 88%; border: none; margin: 1px; padding-right: 6px;',
    kind: 'OB.UI.RenderMultiOrdersLineValues'
  }]
});

enyo.kind({
  name: 'OB.UI.RenderMultiOrdersLineEmpty',
  style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_ReceiptNew'));
  }
});