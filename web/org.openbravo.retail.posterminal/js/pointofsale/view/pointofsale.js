/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo, $, confirm, _ */

// Point of sale main window view
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PointOfSale',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSPointOfSale.Model.PointOfSale,
  tag: 'section',
  handlers: {
    onAddProduct: 'addProductToOrder',
    onViewProductDetails: 'viewProductDetails',
    onCloseProductDetailsView: 'showOrder',
    onCancelReceiptToInvoice: 'cancelReceiptToInvoice',
    onReceiptToInvoice: 'receiptToInvoice',
    onCreateQuotation: 'createQuotation',
    onCreateOrderFromQuotation: 'createOrderFromQuotation',
    onShowCreateOrderPopup: 'showCreateOrderPopup',
    onReactivateQuotation: 'reactivateQuotation',
    onShowReactivateQuotation: 'showReactivateQuotation',
    onRejectQuotation: 'rejectQuotation',
    onQuotations: 'quotations',
    onShowDivText: 'showDivText',
    onAddNewOrder: 'addNewOrder',
    onDeleteOrder: 'deleteCurrentOrder',
    onTabChange: 'tabChange',
    onDeleteLine: 'deleteLine',
    onEditLine: 'editLine',
    onExactPayment: 'exactPayment',
    onRemovePayment: 'removePayment',
    onChangeCurrentOrder: 'changeCurrentOrder',
    onChangeBusinessPartner: 'changeBusinessPartner',
    onPrintReceipt: 'printReceipt',
    onBackOffice: 'backOffice',
    onPaidReceipts: 'paidReceipts',
    onChangeSubWindow: 'changeSubWindow',
    onShowLeftSubWindow: 'showLeftSubWindow',
    onCloseLeftSubWindow: 'showOrder',
    onSetProperty: 'setProperty',
    onSetLineProperty: 'setLineProperty',
    onSetReceiptsList: 'setReceiptsList',
    onShowReceiptProperties: 'showModalReceiptProperties',
    onDiscountsMode: 'discountsMode',
    onDiscountsModeFinished: 'discountsModeFinished',
    onDisableLeftToolbar: 'leftToolbarDisabled',
    onDisableBPSelection: 'BPSelectionDisabled',
    onDisableOrderSelection: 'orderSelectionDisabled',
    onDisableKeyboard: 'keyboardDisabled',
    onDiscountsModeKeyboard: 'keyboardOnDiscountsMode',
    onCheckAllTicketLines: 'allTicketLinesChecked',
    onSetDiscountQty: 'discountQtyChanged',
    onLineChecked: 'checkedLine',
    onStatusChanged: 'statusChanged',
    onLayaways: 'layaways',
    onChangeSalesRepresentative: 'changeSalesRepresentative',
    onMultiOrders: 'multiOrders',
    onSelectMultiOrders: 'selectMultiOrders',
    onRemoveMultiOrders: 'removeMultiOrders',
    onRightToolDisabled: 'rightToolbarDisabled'
  },
  events: {
    onShowPopup: '',
    onButtonStatusChanged: ''
  },
  components: [{
    name: 'otherSubWindowsContainer',
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.customers.ModalConfigurationRequiredForCreateCustomers',
      name: 'modalConfigurationRequiredForCreateNewCustomers'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.customers.cas',
      name: 'customerAdvancedSearch'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.customers.newcustomer',
      name: 'customerCreateAndEdit'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.customers.editcustomer',
      name: 'customerView'
    }, {
      kind: 'OB.UI.ModalDeleteReceipt',
      name: 'modalConfirmReceiptDelete'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalProductCannotBeGroup',
      name: 'modalProductCannotBeGroup'
    }, {
      kind: 'OB.UI.Modalnoteditableorder',
      name: 'modalNotEditableOrder'
    }, {
      kind: 'OB.UI.ModalNotEditableLine',
      name: 'modalNotEditableLine'
    }, {
      kind: 'OB.UI.ModalBusinessPartners',
      name: "modalcustomer"
    }, {
      kind: 'OB.UI.ModalReceipts',
      name: 'modalreceipts'
    }, {
      kind: 'OB.UI.ModalPaidReceipts',
      name: 'modalPaidReceipts'
    }, {
      kind: 'OB.UI.ModalMultiOrders',
      name: 'modalMultiOrders'
    }, {
      kind: 'OB.UI.ModalCreateOrderFromQuotation',
      name: 'modalCreateOrderFromQuotation'
    }, {
      kind: 'OB.UI.ModalReactivateQuotation',
      name: 'modalReactivateQuotation'
    }, {
      kind: 'OB.UI.ModalReceiptPropertiesImpl',
      name: 'receiptPropertiesDialog'
    }, {
      kind: 'OB.UI.ModalReceiptLinesPropertiesImpl',
      name: "receiptLinesPropertiesDialog"
    }, {
      kind: 'OB.UI.ModalPayment',
      name: "modalpayment"
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalConfigurationRequiredForCrossStore',
      name: 'modalConfigurationRequiredForCrossStore'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore',
      name: 'modalLocalStock'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInOtherStores',
      name: 'modalStockInOtherStores'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.modalEnoughCredit',
      name: 'modalEnoughCredit'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.modalNotEnoughCredit',
      name: 'modalNotEnoughCredit'
    }, {
      kind: 'OB.UI.ValidateAction',
      name: 'modalValidateAction'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.modalDiscountNeedQty',
      name: 'modalDiscountNeedQty'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.modalNotValidValueForDiscount',
      name: 'modalNotValidValueForDiscount'
    }, {
      kind: 'OB.UI.ModalSalesRepresentative',
      name: "modalsalesrepresentative"
    }, {
      kind: 'OB.UI.ModalMultiOrdersLayaway',
      name: "modalmultiorderslayaway"
    }]
  }, {
    name: 'mainSubWindow',
    isMainSubWindow: true,
    components: [{
      kind: 'OB.UI.MultiColumn',
      name: 'multiColumn',
      handlers: {
        onChangeTotal: 'processChangeTotal'
      },
      leftToolbar: {
        kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl',
        name: 'leftToolbar',
        showMenu: true,
        showWindowsMenu: true
      },
      leftPanel: {
        name: 'leftPanel',
        components: [{
          classes: 'span12',
          kind: 'OB.OBPOSPointOfSale.UI.ReceiptView',
          name: 'receiptview',
          init: function (model) {
            this.model = model;
            this.model.get('leftColumnViewManager').on('change:currentView', function (changedModel) {
              this.setShowing(changedModel.isOrder());
            }, this);
            //            this.model.get('multiOrders').on('change:isMultiOrders', function () {
            //              this.setShowing(!this.model.get('multiOrders').get('isMultiOrders'));
            //            }, this);
          }
        }, {
          classes: 'span12',
          kind: 'OB.OBPOSPointOfSale.UI.MultiReceiptView',
          name: 'multireceiptview',
          showing: false,
          init: function (model) {
            this.model = model;
            this.model.get('leftColumnViewManager').on('change:currentView', function (changedModel) {
              this.setShowing(changedModel.isMultiOrder());
            }, this);
            //            this.model.get('multiOrders').on('change:isMultiOrders', function () {
            //              this.setShowing(this.model.get('multiOrders').get('isMultiOrders'));
            //            }, this);
          }
        }, {
          name: 'leftSubWindowsContainer',
          components: [{
            classes: 'span12',
            kind: 'OB.OBPOSPointOfSale.UI.ProductDetailsView',
            name: 'productdetailsview'
          }]
        }]
      },
      rightToolbar: {
        kind: 'OB.OBPOSPointOfSale.UI.RightToolbarImpl',
        name: 'rightToolbar'
      },
      rightPanel: {
        name: 'keyboardTabsPanel',
        components: [{
          classes: 'span12',
          components: [{
            kind: 'OB.OBPOSPointOfSale.UI.RightToolbarPane',
            name: 'toolbarpane'
          }, {
            kind: 'OB.OBPOSPointOfSale.UI.KeyboardOrder',
            name: 'keyboard'
          }]
        }]
      },
      processChangeTotal: function (inSender, inEvent) {
        this.waterfall('onChangedTotal', {
          newTotal: inEvent.newTotal
        });
      }
    }]
  }],
  classModel: new Backbone.Model(),
  printReceipt: function () {
    if (OB.POS.modelterminal.hasPermission('OBPOS_print.receipt')) {
      if (this.model.get('leftColumnViewManager').isOrder()) {
        var receipt = this.model.get('order');
        if (receipt.get("isPaid")) {
          receipt.trigger('print');
          return;
        }
        receipt.calculateTaxes(function () {
          receipt.trigger('print');
        });
        return;
      }
      if (this.model.get('leftColumnViewManager').isMultiOrder()) {
        _.each(this.model.get('multiOrders').get('multiOrdersList').models, function (order) {
          this.model.get('multiOrders').trigger('print', order);
        }, this);
      }
    }
  },
  paidReceipts: function (inSender, inEvent) {
    this.$.modalPaidReceipts.setParams({
      isQuotation: false
    });
    this.$.modalPaidReceipts.waterfall('onClearAction');
    this.doShowPopup({
      popup: 'modalPaidReceipts'
    });
    return true;
  },

  quotations: function (inSender, inEvent) {
    this.$.modalPaidReceipts.setParams({
      isQuotation: true
    });
    this.$.modalPaidReceipts.waterfall('onClearAction');
    this.doShowPopup({
      popup: 'modalPaidReceipts'
    });
  },

  backOffice: function (inSender, inEvent) {
    if (inEvent.url) {
      window.open(inEvent.url, '_blank');
    }
  },
  addNewOrder: function (inSender, inEvent) {
    this.$.receiptPropertiesDialog.newOrderCreated();
    this.model.get('orderList').addNewOrder();
    return true;
  },
  deleteCurrentOrder: function () {
    if (this.model.get('order').get('id')) {
      this.model.get('orderList').saveCurrent();
      OB.Dal.remove(this.model.get('orderList').current, null, null);
    }
    this.model.get('orderList').deleteCurrent();
    return true;
  },
  addProductToOrder: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }

    if (inEvent.ignoreStockTab) {
      this.showOrder(inSender, inEvent);
    } else {
      if (!this.model.get('order').get('lines').isProductPresent(inEvent.product) && inEvent.product.get('showstock') && !inEvent.product.get('ispack') && OB.POS.modelterminal.get('connectedToERP')) {
        inEvent.leftSubWindow = OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow;
        this.showLeftSubWindow(inSender, inEvent);
        if (enyo.Panels.isScreenNarrow()) {
          this.$.multiColumn.switchColumn();
        }
        return true;
      } else {
        this.showOrder(inSender, inEvent);
      }
    }

    this.model.get('order').addProduct(inEvent.product, inEvent.qty, inEvent.options);
    this.model.get('orderList').saveCurrent();
    return true;
  },
  showOrder: function (inSender, inEvent) {
    var allHidden = true;
    enyo.forEach(this.$.multiColumn.$.leftPanel.$.leftSubWindowsContainer.getControls(), function (component) {
      if (component.showing === true) {
        if (component.mainBeforeSetHidden) {
          if (!component.mainBeforeSetHidden(inEvent)) {
            allHidden = false;
            return false;
          } else {
            component.setShowing(false);
          }
        }
      }
    }, this);
    if (allHidden) {
      this.$.multiColumn.$.leftPanel.$.receiptview.setShowing(true);
    }
  },
  showLeftSubWindow: function (inSender, inEvent) {
    if (this.$.multiColumn.$.leftPanel.$[inEvent.leftSubWindow]) {
      if (this.$.multiColumn.$.leftPanel.$[inEvent.leftSubWindow].mainBeforeSetShowing) {
        var allHidden = true;
        enyo.forEach(this.$.multiColumn.$.leftPanel.getControls(), function (component) {
          if (component.showing === true) {
            if (component.mainBeforeSetHidden) {
              if (!component.mainBeforeSetHidden(inEvent)) {
                allHidden = false;
                return false;
              }
            }
          }
        }, this);
        if (this.$.multiColumn.$.leftPanel.$[inEvent.leftSubWindow].mainBeforeSetShowing(inEvent) && allHidden) {
          this.$.multiColumn.$.leftPanel.$.receiptview.setShowing(false);
          this.$.multiColumn.$.leftPanel.$[inEvent.leftSubWindow].setShowing(true);
        }
      }
    }
  },
  viewProductDetails: function (inSender, inEvent) {
    this.$.multiColumn.$.leftPanel.$.receiptview.applyStyle('display', 'none');
    this.$.productdetailsview.updateProduct(inEvent.product);
    this.$.productdetailsview.applyStyle('display', 'inline');
    return true;
  },
  changeBusinessPartner: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    this.model.get('order').setBPandBPLoc(inEvent.businessPartner, false, true);
    this.model.get('orderList').saveCurrent();
    return true;
  },
  receiptToInvoice: function () {
    if (this.model.get('leftColumnViewManager').isOrder()) {
      if (this.model.get('order').get('isEditable') === false && !this.model.get('order').get('isLayaway')) {
        this.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return true;
      }
      this.model.get('order').setOrderInvoice();
      this.model.get('orderList').saveCurrent();
      return true;
    }
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      this.model.get('multiOrders').toInvoice(true);
    }
  },
  createQuotation: function () {
    this.model.get('orderList').addNewQuotation();
    return true;
  },

  createOrderFromQuotation: function () {
    this.model.get('order').createOrderFromQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },

  showReactivateQuotation: function () {
    this.doShowPopup({
      popup: 'modalReactivateQuotation'
    });
  },

  reactivateQuotation: function () {
    this.model.get('order').reactivateQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  rejectQuotation: function () {
    this.model.get('order').rejectQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  showDivText: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false && !this.model.get('order').get('isLayaway')) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    //Void Layaway must block keyboard actions
    if (inEvent.orderType === 3) {
      this.$.multiColumn.$.rightPanel.$.keyboard.setStatus('');
    }
    this.model.get('order').setOrderType(inEvent.permission, inEvent.orderType);
    this.model.get('orderList').saveCurrent();
    return true;
  },

  cancelReceiptToInvoice: function (inSender, inEvent) {
    if (this.model.get('leftColumnViewManager').isOrder()) {
      if (this.model.get('order').get('isEditable') === false) {
        this.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return true;
      }
      this.model.get('order').resetOrderInvoice();
      this.model.get('orderList').saveCurrent();
      return true;
    }
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      if (this.model.get('leftColumnViewManager').isMultiOrder()) {
        this.model.get('multiOrders').toInvoice(false);
      }
    }
  },
  checkedLine: function (inSender, inEvent) {
    if (inEvent.originator.kind === 'OB.UI.RenderOrderLine') {
      this.waterfall('onCheckedTicketLine', inEvent);
      return true;
    }
  },
  discountQtyChanged: function (inSender, inEvent) {
    this.waterfall('onDiscountQtyChanged', inEvent);
  },
  keyboardOnDiscountsMode: function (inSender, inEvent) {
    this.waterfall('onKeyboardOnDiscountsMode', inEvent);
  },
  keyboardDisabled: function (inSender, inEvent) {
    this.waterfall('onKeyboardDisabled', inEvent);
  },
  allTicketLinesChecked: function (inSender, inEvent) {
    this.waterfall('onAllTicketLinesChecked', inEvent);
  },
  leftToolbarDisabled: function (inSender, inEvent) {
    this.waterfall('onLeftToolbarDisabled', inEvent);
  },
  rightToolbarDisabled: function (inSender, inEvent) {
    this.waterfall('onRightToolbarDisabled', inEvent);
  },
  BPSelectionDisabled: function (inSender, inEvent) {
    this.waterfall('onBPSelectionDisabled', inEvent);
  },
  orderSelectionDisabled: function (inSender, inEvent) {
    this.waterfall('onOrderSelectionDisabled', inEvent);
  },
  discountsMode: function (inSender, inEvent) {
    this.leftToolbarDisabled(inSender, {
      status: true
    });
    this.rightToolbarDisabled(inSender, {
      status: true
    });
    this.BPSelectionDisabled(inSender, {
      status: true
    });
    this.orderSelectionDisabled(inSender, {
      status: true
    });
    this.keyboardOnDiscountsMode(inSender, {
      status: true
    });
    this.waterfall('onCheckBoxBehaviorForTicketLine', {
      status: true
    });
    this.tabChange(inSender, inEvent);
  },
  tabChange: function (inSender, inEvent) {

    this.waterfall('onTabButtonTap', {
      tabPanel: inEvent.tabPanel,
      options: inEvent.options
    });
    this.waterfall('onChangeEditMode', {
      edit: inEvent.edit
    });
    if (inEvent.keyboard) {
      this.$.multiColumn.$.rightPanel.$.keyboard.showToolbar(inEvent.keyboard);
    } else {
      this.$.multiColumn.$.rightPanel.$.keyboard.hide();
    }
    if (!_.isUndefined(inEvent.status)) {
      this.$.multiColumn.$.rightPanel.$.keyboard.setStatus(inEvent.status);
    }
  },
  discountsModeFinished: function (inSender, inEvent) {
    this.leftToolbarDisabled(inSender, {
      status: false
    });
    this.keyboardOnDiscountsMode(inSender, {
      status: false
    });
    this.rightToolbarDisabled(inSender, {
      status: false
    });

    this.keyboardDisabled(inSender, {
      status: false
    });

    this.BPSelectionDisabled(inSender, {
      status: false
    });

    this.orderSelectionDisabled(inSender, {
      status: false
    });

    this.waterfall('onCheckBoxBehaviorForTicketLine', {
      status: false
    });

    this.allTicketLinesChecked(inSender, {
      status: false
    });

    this.tabChange(inSender, inEvent);
  },
  deleteLine: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    var line = inEvent.line,
        receipt = this.model.get('order');
    if (line && receipt) {
      receipt.deleteLine(line);
      receipt.trigger('scan');
    }
  },
  editLine: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    this.doShowPopup({
      popup: 'receiptLinesPropertiesDialog'
    });
  },
  exactPayment: function (inSender, inEvent) {
    this.$.multiColumn.$.rightPanel.$.keyboard.execStatelessCommand('cashexact');
  },
  changeCurrentOrder: function (inSender, inEvent) {
    this.model.get('orderList').load(inEvent.newCurrentOrder);
    return true;
  },
  removePayment: function (inSender, inEvent) {
    var me = this;
    if (inEvent.payment.get('paymentData')) {
      if (!confirm(OB.I18N.getLabel('OBPOS_MsgConfirmRemovePayment'))) {
        if (inEvent.removeCallback) {
          inEvent.removeCallback();
        }
        //canceled, not remove
        return;
      } else {
        //To remove this payment we've to connect with server
        //a callback is defined to receive the confirmation
        var callback = function (hasError, error) {
            if (inEvent.removeCallback) {
              inEvent.removeCallback();
            }
            if (hasError) {
              OB.UTIL.showError(error);
            } else {
              //            if (!me.model.get('multiOrders').get('isMultiOrders')) {
              //              me.model.get('order').removePayment(inEvent.payment);
              //            } else {
              //              me.model.get('multiOrders').removePayment(inEvent.payment);
              //            }
              if (me.model.get('leftColumnViewManager').isOrder()) {
                me.model.get('order').removePayment(inEvent.payment);
                return;
              }
              if (me.model.get('leftColumnViewManager').isMultiOrder()) {
                me.model.get('multiOrders').removePayment(inEvent.payment);
                return;
              }
            }
            };
        //async call with defined callback
        inEvent.payment.get('paymentData').voidTransaction(callback);
        return;
      }
    } else {
      //      if (!me.model.get('multiOrders').get('isMultiOrders')) {
      //        me.model.get('order').removePayment(inEvent.payment);
      //      } else {
      //        me.model.get('multiOrders').removePayment(inEvent.payment);
      //      }
      if (me.model.get('leftColumnViewManager').isOrder()) {
        me.model.get('order').removePayment(inEvent.payment);
        return;
      }
      if (me.model.get('leftColumnViewManager').isMultiOrder()) {
        me.model.get('multiOrders').removePayment(inEvent.payment);
        return;
      }
    }
  },
  changeSubWindow: function (inSender, inEvent) {
    this.model.get('subWindowManager').set('currentWindow', inEvent.newWindow);
  },
  setReceiptsList: function (inSender, inEvent) {
    this.$.modalreceipts.setReceiptsList(inEvent.orderList);
  },
  showModalReceiptProperties: function (inSender, inEvent) {
    this.doShowPopup({
      popup: 'receiptPropertiesDialog'
    });
    return true;
  },
  setProperty: function (inSender, inEvent) {
    var i;
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    if (inEvent.extraProperties) {
      for (i = 0; i < inEvent.extraProperties.length; i++) {
        this.model.get('order').setProperty(inEvent.extraProperties[i], inEvent.value);
      }
    }
    this.model.get('order').setProperty(inEvent.property, inEvent.value);
    this.model.get('orderList').saveCurrent();
    return true;
  },
  setLineProperty: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    var line = inEvent.line,
        receipt = this.model.get('order');
    if (line && receipt) {
      receipt.setLineProperty(line, inEvent.property, inEvent.value);
    }
    this.model.get('orderList').saveCurrent();
    return true;
  },
  statusChanged: function (inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onButtonStatusChanged', {
      value: inEvent
    });
  },
  layaways: function (inSender, inEvent) {
    this.$.modalPaidReceipts.setParams({
      isLayaway: true
    });
    this.$.modalPaidReceipts.waterfall('onClearAction');
    this.doShowPopup({
      popup: 'modalPaidReceipts'
    });
  },
  changeSalesRepresentative: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    this.model.get('order').set('salesRepresentative', inEvent.salesRepresentative.get('id'));
    this.model.get('order').set('salesRepresentative$_identifier', inEvent.salesRepresentative.get('_identifier'));
    this.model.get('orderList').saveCurrent();
    return true;
  },
  multiOrders: function (inSender, inEvent) {
    this.model.get('multiOrders').resetValues();
    this.doShowPopup({
      popup: 'modalMultiOrders'
    });
    return true;
  },
  selectMultiOrders: function (inSender, inEvent) {
    var me = this;
    me.model.get('multiOrders').get('multiOrdersList').reset();
    _.each(inEvent.value, function (iter) {
      //iter.set('isMultiOrder', true);
      me.model.get('orderList').addMultiReceipt(iter);
      me.model.get('multiOrders').get('multiOrdersList').add(iter);
    });
    this.model.get('leftColumnViewManager').setMultiOrderMode();
    //this.model.get('multiOrders').set('isMultiOrders', true);
    return true;
  },
  removeMultiOrders: function (inSender, inEvent) {
    var me = this;
    me.model.get('multiOrders').get('multiOrdersList').remove(inEvent.order);
    me.model.get('orderList').current = inEvent.order;
    me.model.get('orderList').deleteCurrent();
    if (!_.isNull(inEvent.order.id)) {
      me.model.get('orderList').deleteCurrentFromDatabase(inEvent.order);
    }
    return true;
  },
  init: function () {
    var receipt, receiptList, LeftColumnCurrentView;
    this.inherited(arguments);
    receipt = this.model.get('order');
    receiptList = this.model.get('orderList');
    OB.MobileApp.view.scanningFocus(true);

    this.model.get('leftColumnViewManager').on('change:currentView', function (changedModel) {
      if (changedModel.isMultiOrder()) {
        this.rightToolbarDisabled({}, {
          status: true,
          exceptionPanel: 'payment'
        });
        this.tabChange({}, {
          tabPanel: 'payment',
          keyboard: 'toolbarpayment'
        });
        return;
      }
      if (changedModel.isOrder()) {
        this.rightToolbarDisabled({}, {
          status: false
        });
        this.tabChange({}, {
          tabPanel: 'scan',
          keyboard: 'toolbarscan'
        });
        return;
      }
    }, this);

    LeftColumnCurrentView = enyo.json.parse(localStorage.getItem('leftColumnCurrentView'));
    if (LeftColumnCurrentView === null) {
      LeftColumnCurrentView = {
        name: 'order',
        params: []
      };
    }
    this.model.get('leftColumnViewManager').set('currentView', LeftColumnCurrentView);

    this.model.get('subWindowManager').on('change:currentWindow', function (changedModel) {

      function restorePreviousState(swManager, changedModel) {
        swManager.set('currentWindow', changedModel.previousAttributes().currentWindow, {
          silent: true
        });
      }

      var showNewSubWindow = false,
          currentWindowClosed = true;
      if (this.$[changedModel.get('currentWindow').name]) {
        if (!changedModel.get('currentWindow').params) {
          changedModel.get('currentWindow').params = {};
        }
        changedModel.get('currentWindow').params.caller = changedModel.previousAttributes().currentWindow.name;
        if (this.$[changedModel.previousAttributes().currentWindow.name].mainBeforeClose) {
          currentWindowClosed = this.$[changedModel.previousAttributes().currentWindow.name].mainBeforeClose(changedModel.get('currentWindow').name);
        }
        if (currentWindowClosed) {
          if (this.$[changedModel.get('currentWindow').name].mainBeforeSetShowing) {
            showNewSubWindow = this.$[changedModel.get('currentWindow').name].mainBeforeSetShowing(changedModel.get('currentWindow').params);
            if (showNewSubWindow) {
              this.$[changedModel.previousAttributes().currentWindow.name].setShowing(false);
              this.$[changedModel.get('currentWindow').name].setShowing(true);
              if (this.$[changedModel.get('currentWindow').name].mainAfterShow) {
                this.$[changedModel.get('currentWindow').name].mainAfterShow();
              }
            } else {
              restorePreviousState(this.model.get('subWindowManager'), changedModel);
            }
          } else {
            if (this.$[changedModel.get('currentWindow').name].isMainSubWindow) {
              this.$[changedModel.previousAttributes().currentWindow.name].setShowing(false);
              this.$[changedModel.get('currentWindow').name].setShowing(true);
              OB.MobileApp.view.scanningFocus(true);
              //OB.POS.terminal.$.focusKeeper.focus();
            } else {
              //developers helps
              //console.log("Error! A subwindow must inherits from OB.UI.subwindow -> restore previous state");
              restorePreviousState(this.model.get('subWindowManager'), changedModel);
            }
          }
        } else {
          restorePreviousState(this.model.get('subWindowManager'), changedModel);
        }
      } else {
        //developers helps
        //console.log("The subwindow to navigate doesn't exists -> restore previous state");
        restorePreviousState(this.model.get('subWindowManager'), changedModel);
      }
    }, this);

    // show properties when needed...
    receipt.get('lines').on('created', function (line) {
      this.classModel.trigger('createdLine', this, line);
    }, this);
    receipt.get('lines').on('removed', function (line) {
      this.classModel.trigger('removedLine', this, line);
    }, this);

    this.$.multiColumn.$.leftPanel.$.receiptview.setOrder(receipt);
    this.$.multiColumn.$.leftPanel.$.receiptview.setOrderList(receiptList);
    this.$.multiColumn.$.rightPanel.$.toolbarpane.setModel(this.model);
    this.$.multiColumn.$.rightPanel.$.keyboard.setReceipt(receipt);
    this.$.multiColumn.$.rightToolbar.$.rightToolbar.setReceipt(receipt);
  },
  initComponents: function () {
    this.inherited(arguments);
  }
});

OB.OBPOSPointOfSale.UICustomization = OB.OBPOSPointOfSale.UICustomization || {};
OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow = 'productdetailsview';

OB.POS.registerWindow({
  windowClass: OB.OBPOSPointOfSale.UI.PointOfSale,
  route: 'retail.pointofsale',
  menuPosition: null,
  permission: 'OBPOS_retail.pointofsale',
  // Not to display it in the menu
  menuLabel: 'POS'
});