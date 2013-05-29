/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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
  init: function (model) {
    this.model = model;
  },
  tap: function () {
    this.model.get('multiOrders').set('isMultiOrders', false);
    this.doAddNewOrder();
  }
});


enyo.kind({
  name: 'OB.UI.ButtonDelete',
  kind: 'OB.UI.ToolbarButton',
  icon: 'btn-icon btn-icon-delete',
  events: {
    onShowPopup: '',
    onDeleteOrder: ''
  },
  handlers: {
    onLeftToolbarDisabled: 'disabledButton'
  },
  disabledButton: function (inSender, inEvent) {
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
  },
  tap: function () {
    if (this.model.get('multiOrders').get('isMultiOrders')) {
      this.model.get('multiOrders').set('isMultiOrders', false);
      return true;
    }
    if (!this.model.get('order').get('isPaid') && !this.model.get('order').get('isQuotation') && !this.model.get('order').get('isLayaway')) {
      this.doShowPopup({
        popup: 'modalConfirmReceiptDelete'
      });
    } else {
      this.doDeleteOrder();
    }
  },
  init: function (model) {
    this.model = model;
    this.model.get('multiOrders').on('change:isMultiOrders', function (model) {
      if (model.get('isMultiOrders')) {
        this.addClass('paidticket');
      } else {
        this.removeClass('paidticket');
      }
      return true;
    }, this);
    this.model.get('order').on('change:isPaid change:isQuotation change:isLayaway change:hasbeenpaid', function (changedModel) {
      if (changedModel.get('isPaid') || changedModel.get('isLayaway') || (changedModel.get('isQuotation') && changedModel.get('hasbeenpaid') === 'Y')) {
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
    if (inEvent.exceptionPanel === this.tabPanel) {
      return true;
    }
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
      if ((this.model.get('order').get('isEditable') === false && !this.model.get('order').get('isLayaway')) || this.model.get('order').get('orderType') === 3) {
        return true;
      }
      OB.MobileApp.view.scanningFocus(false);
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
    this.model.get('order').on('change:isEditable change:isLayaway', function (newValue) {
      if (newValue) {
        if (newValue.get('isEditable') === false && !newValue.get('isLayaway')) {
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
  menuEntries: [],
  buttons: [{
    kind: 'OB.UI.ButtonNew',
    span: 3
  }, {
    kind: 'OB.UI.ButtonDelete',
    span: 3
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.ButtonTabPayment',
    name: 'payment',
    span: 6
  }],
  initComponents: function () {
    // set up the POS menu
    this.menuEntries = [];
    this.menuEntries.push({
      kind: 'OB.UI.MenuReturn'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuVoidLayaway'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuProperties'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuInvoice'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuPrint'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuLayaway'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuCustomers'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuPaidReceipts'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuQuotations'
    });

    // TODO: what is this for?!!
    // this.menuEntries = this.menuEntries.concat(this.externalEntries);
    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuDiscounts'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuReactivateQuotation'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuRejectQuotation'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuCreateOrderFromQuotation'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuQuotation'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuLayaways'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuMultiOrders'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuBackOffice'
    });

    this.inherited(arguments);
  }
});