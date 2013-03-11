/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

/*left toolbar*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
  tag: 'li',
  classes: 'span4',
  components: [{
    name: 'theButton',
    attributes: {
      style: 'margin: 0px 5px 0px 5px;'
    }
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.theButton.createComponent(this.button);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbar',
  classes: 'span3',
  components: [{
    tag: 'ul',
    classes: 'unstyled nav-pos row-fluid',
    name: 'toolbar'
  }],
  initComponents: function () {
    this.inherited(arguments);
    enyo.forEach(this.buttons, function (btn) {
      this.$.toolbar.createComponent({
        kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
        button: btn
      });
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.ButtonNew',
  kind: 'OB.UI.ToolbarButton',
  icon: 'btn-icon btn-icon-new',
  events: {
    onAddNewOrder: ''
  },
  handlers: {
    onLeftToolbarDisabled: 'disabledButton'
  },
  disabledButton: function (inSender, inEvent) {
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
  },
  tap: function () {
    this.doAddNewOrder();
  }
});


enyo.kind({
  name: 'OB.UI.ButtonDelete',
  kind: 'OB.UI.ToolbarButton',
  icon: 'btn-icon btn-icon-delete',
  events: {
    onShowPopup: ''
  },
  handlers: {
    onLeftToolbarDisabled: 'disabledButton'
  },
  disabledButton: function (inSender, inEvent) {
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
  },
  tap: function () {
    if (this.model.get('order').get('isPaid')) {
      this.doShowPopup({
        popup: 'modalConfirmClosePaidTicket'
      });
    } else {
      this.doShowPopup({
        popup: 'modalConfirmReceiptDelete'
      });
    }
  },
  init: function (model) {
    this.model = model;
    this.model.get('order').on('change:isPaid change:isQuotation change:hasbeenpaid', function (changedModel) {
      if (changedModel.get('isPaid') || (changedModel.get('isQuotation') && changedModel.get('hasbeenpaid') === 'Y')) {
        this.addClass('paidticket');
        return;
      }
      this.removeClass('paidticket');
    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabPayment',
  kind: 'OB.UI.ToolbarButtonTab',
  tabPanel: 'payment',
  handlers: {
    onChangedTotal: 'renderTotal',
    onRightToolbarDisabled: 'disabledButton'
  },
  disabledButton: function (inSender, inEvent) {
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
  },
  events: {
    onTabChange: ''
  },
  tap: function () {
    if (this.disabled === false) {
      var receipt = this.model.get('order');
      if (receipt.get('isQuotation')) {
        if (receipt.get('hasbeenpaid') !== 'Y') {
          receipt.prepareToSend(function () {
            receipt.trigger('closed');
            receipt.trigger('scan');
          });
        } else {
          receipt.prepareToSend(function () {
            receipt.trigger('scan');
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_QuotationClosed'));
          });
        }
        return;
      }
      if (this.model.get('order').get('isEditable') === false) {
        return true;
      }
      this.doTabChange({
        tabPanel: this.tabPanel,
        keyboard: 'toolbarpayment',
        edit: false
      });
      this.bubble('onShowColumn', {
          colNum: 1
        });
    }
  },
  attributes: {
    style: 'text-align: center; font-size: 30px;'
  },
  components: [{
    tag: 'span',
    attributes: {
      style: 'font-weight: bold; margin: 0px 5px 0px 0px;'
    },
    components: [{
      kind: 'OB.UI.Total',
      name: 'totalPrinter'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.removeClass('btnlink-gray');
  },
  renderTotal: function (inSender, inEvent) {
    this.$.totalPrinter.renderTotal(inEvent.newTotal);
  },
  init: function (model) {
    this.model = model;
    this.model.get('order').on('change:isEditable', function (newValue) {
      if (newValue) {
        if (newValue.get('isEditable') === false) {
          this.tabPanel = null;
          this.setDisabled(true);
          return;
        }
      }
      this.tabPanel = 'payment';
      this.setDisabled(false);
    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl',
  kind: 'OB.UI.MultiColumn.Toolbar',
  buttons: [{
    kind: 'OB.UI.ButtonNew',
    span: 2
  }, {
    kind: 'OB.UI.ButtonDelete',
    span: 2
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.StandardMenu',
    span: 2
  },{
	    kind: 'OB.OBPOSPointOfSale.UI.ButtonTabPayment',
	    name: 'payment',
	    span: 6
	  }]
});