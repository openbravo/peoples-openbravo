/*
 ************************************************************************************
 * Copyright (C) 2013-2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

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
    var i;
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      for (i = 0; this.model.get('multiOrders').get('multiOrdersList').length > i; i++) {
        if (!this.model.get('multiOrders').get('multiOrdersList').at(i).get('isLayaway')) { //if it is not true, means that iti is a new order (not a loaded layaway)
          this.model.get('multiOrders').get('multiOrdersList').at(i).unset('amountToLayaway');
          this.model.get('multiOrders').get('multiOrdersList').at(i).set('orderType', 0);
          continue;
        }
        this.model.get('orderList').current = this.model.get('multiOrders').get('multiOrdersList').at(i);
        this.model.get('orderList').deleteCurrent();
        if (!_.isNull(this.model.get('multiOrders').get('multiOrdersList').at(i).id)) {
          this.model.get('orderList').deleteCurrentFromDatabase(this.model.get('multiOrders').get('multiOrdersList').at(i));
        }
      }
      this.model.get('multiOrders').resetValues();
      this.model.get('leftColumnViewManager').setOrderMode();
    } else {
      if (OB.MobileApp.model.get('permissions')['OBPOS_print.suspended'] && this.model.get('order').get('lines').length !== 0) {
        this.model.get('order').trigger('print');
      }
    }
    this.doAddNewOrder();
    return true;
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
    var i, me = this;

    if (me.model.get('leftColumnViewManager').isMultiOrder()) {
      for (i = 0; me.model.get('multiOrders').get('multiOrdersList').length > i; i++) {
        if (!me.model.get('multiOrders').get('multiOrdersList').at(i).get('isLayaway')) { //if it is not true, means that iti is a new order (not a loaded layaway)
          me.model.get('multiOrders').get('multiOrdersList').at(i).unset('amountToLayaway');
          me.model.get('multiOrders').get('multiOrdersList').at(i).set('orderType', 0);
          continue;
        }
        me.model.get('orderList').current = me.model.get('multiOrders').get('multiOrdersList').at(i);
        me.model.get('orderList').deleteCurrent();
        if (!_.isNull(me.model.get('multiOrders').get('multiOrdersList').at(i).id)) {
          me.model.get('orderList').deleteCurrentFromDatabase(me.model.get('multiOrders').get('multiOrdersList').at(i));
        }
      }
      me.model.get('multiOrders').resetValues();
      me.model.get('leftColumnViewManager').setOrderMode();
      return true;
    }
    // deletion without warning is allowed if the ticket has been processed
    if (me.hasClass('paidticket')) {
      me.doDeleteOrder();
    } else {
      OB.UTIL.Approval.requestApproval(
      this.model, 'OBPOS_approval.removereceipts', function (approved, supervisor, approvalType) {
        if (approved) {
          me.doShowPopup({
            popup: 'modalConfirmReceiptDelete'
          });
        }
      });
    }

  },
  init: function (model) {
    this.model = model;
    this.model.get('leftColumnViewManager').on('multiorder', function () {
      this.addClass('paidticket');
      return true;
    }, this);
    this.model.get('leftColumnViewManager').on('order', function () {
      this.removeClass('paidticket');
      if (this.model.get('order').get('isPaid') || this.model.get('order').get('isLayaway') || (this.model.get('order').get('isQuotation') && this.model.get('order').get('hasbeenpaid') === 'Y')) {
        this.addClass('paidticket');
      }
      this.bubble('onChangeTotal', {
        newTotal: this.model.get('order').getTotal()
      });
    }, this);

    //    this.model.get('multiOrders').on('change:isMultiOrders', function (model) {
    //      if (model.get('isMultiOrders')) {
    //        this.addClass('paidticket');
    //      } else {
    //        this.removeClass('paidticket');
    //      }
    //      return true;
    //    }, this);
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
    onRightToolbarDisabled: 'disabledButton',
    synchronizing: 'disableButton',
    synchronized: 'enableButton'
  },
  disabledButton: function (inSender, inEvent) {
    if (inEvent.exceptionPanel === this.tabPanel) {
      return true;
    }
    this.isEnabled = !inEvent.status;
    this.disabledChanged(inEvent.status);
  },
  disableButton: function () {
    this.disabledChanged(true);
  },
  enableButton: function () {
    this.disabledChanged(false);
  },
  disabledChanged: function (isDisabled) {
    // if the button is requested to be enabled, verify that the conditions are met
    if (isDisabled === false) {
      // by default, disabled
      isDisabled = true;
      if (OB.UTIL.SynchronizationHelper.isSynchronized()) {
        if (this.model) {
          var receipt = this.model.get('order');
          if (receipt) {
            if (receipt.get('id') && receipt.get('documentNo') && receipt.get('documentNo').length > 3 && receipt.get('bp') && receipt.get('bp').get('id')) {
              if (receipt.get('lines') && receipt.get('lines').length > 0) {
                if (receipt.get('hasbeenpaid') === 'N') {
                  // enable it if all conditions are met
                  isDisabled = false;
                }
              }
            }
          }
        }
      }
    }
    this.disabled = isDisabled; // for getDisabled() to return the correct value
    this.setAttribute('disabled', isDisabled); // to effectively turn the button enabled or disabled
  },
  events: {
    onTabChange: '',
    onClearUserInput: ''
  },

  showPaymentTab: function () {
    var receipt = this.model.get('order'),
        me = this;
    if (receipt.get('isQuotation')) {
      if (receipt.get('hasbeenpaid') !== 'Y') {
        receipt.prepareToSend(function () {
          receipt.trigger('closed', {
            callback: function () {
              receipt.trigger('scan');
            }
          });
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
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      this.model.get('multiOrders').trigger('displayTotal');
    } else {
      receipt.trigger('displayTotal');
    }

    me.doTabChange({
      tabPanel: me.tabPanel,
      keyboard: 'toolbarpayment',
      edit: false
    });
    me.bubble('onShowColumn', {
      colNum: 1
    });
  },

  tap: function () {
    if (this.disabled === false) {
      this.model.on('approvalChecked', function (event) {
        this.model.off('approvalChecked');
        if (event.approved) {
          this.showPaymentTab();
        }
      }, this);
      this.model.completePayment();
      this.doClearUserInput();
    }
  },
  attributes: {
    style: 'text-align: center; font-size: 30px;'
  },
  components: [{
    style: 'font-weight: bold; margin: 0px 5px 0px 0px;',
    kind: 'OB.UI.Total',
    name: 'totalPrinter'
  }],
  getLabel: function () {
    return this.$.totalPrinter.getContent();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.removeClass('btnlink-gray');
  },
  renderTotal: function (inSender, inEvent) {
    this.$.totalPrinter.renderTotal(inEvent.newTotal);
    this.disabledChanged(false);
  },
  init: function (model) {
    this.model = model;
    this.model.get('order').on('change:isEditable change:isLayaway', function (newValue) {
      if (newValue) {
        if (newValue.get('isEditable') === false && !newValue.get('isLayaway')) {
          this.tabPanel = null;
          this.disabledChanged(true);
          return;
        }
      }
      this.tabPanel = 'payment';
      this.disabledChanged(false);
    }, this);
    this.model.get('order').on('change:id', function (newValue) {
      this.disabledChanged(false);
    }, this);
    // the button state must be set only once, in the initialization
    this.setDisabled(true);
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
    name: 'btnTotalToPay',
    span: 6
  }],
  initComponents: function () {
    // set up the POS menu
    //Menu entries is used for modularity. cannot be initialized
    //this.menuEntries = [];
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
    this.menuEntries.push({
      kind: 'OB.UI.MenuOpenDrawer'
    });
    // TODO: what is this for?!!
    // this.menuEntries = this.menuEntries.concat(this.externalEntries);
    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator',
      name: 'sep1'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuDiscounts'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator',
      name: 'sep2'
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
      kind: 'OB.UI.MenuSeparator',
      name: 'sep3'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuBackOffice'
    });

    //remove duplicates
    this.menuEntries = _.uniq(this.menuEntries, false, function (p) {
      return p.kind + p.name;
    });

    this.inherited(arguments);
  }
});