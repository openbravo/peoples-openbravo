/*global OB, enyo, $, confirm */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

// Point of sale main window view
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PointOfSale',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSPointOfSale.Model.PointOfSale,
  tag: 'section',
  handlers: {
    onAddProduct: 'addProductToOrder',
    onCancelReceiptToInvoice: 'cancelReceiptToInvoice',
    onReceiptToInvoice: 'receiptToInvoice',
    onCreateQuotation: 'createQuotation',
    onCreateOrderFromQuotation: 'createOrderFromQuotation',
    onReactivateQuotation: 'reactivateQuotation',
    onRejectQuotation: 'rejectQuotation',
    onQuotations: 'quotations',
    onShowReturnText: 'showReturnText',
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
    onSetProperty: 'setProperty',
    onSetLineProperty: 'setLineProperty',
    onShowReceiptProperties: 'showModalReceiptProperties'
  },
  components: [{
    name: 'otherSubWindowsContainer',
    components: [{
      style: 'background-color: #FFFFFF;',
      name: 'subWindow_customers',
      beforeSetShowing: function(value, params) {
        this.setShowing(value);
      },
      showing: false,
      components: [{
        tag: 'div',
        style: 'padding: 9px 15px;',
        components: [{
          tag: 'a',
          classes: 'close',
          components: [{
            tag: 'span',
            style: 'font-size: 150%',
            allowHtml: true,
            content: '&times;'
          }],
          tap: function() {
            this.model.get('subWindowManager').set('currentWindow', {
              name: 'mainSubWindow',
              params: []
            });
          },
          init: function(model) {
            this.model = model;
          }
        }, {
          tag: 'h3',
          name: 'divheaderCustomerAdvancedSearch',
          content: OB.I18N.getLabel('OBPOS_TitleCustomerAdvancedSearch')
        }]
      }, {
        kind: 'OB.UI.ListCustomers'
      }]
    }]
  }, {
    name: 'NewCustomerSubWindowsContainer',
    components: [{
      kind: 'OB.UI.ModalConfigurationRequiredForCreateCustomers'
    }, {
      style: 'background-color: #FFFFFF;',
      name: 'subWindow_new_customer',
      beforeSetShowing: function(value, params) {
        this.setShowing(value);
        this.waterfall('onSetCustomer', {
          customer: params.businessPartner,
          callerWindow: params.callerWindow
        });
      },
      showing: false,
      components: [{
        tag: 'div',
        style: 'padding: 9px 15px;',
        components: [{
          tag: 'a',
          classes: 'close',
          components: [{
            tag: 'span',
            style: 'font-size: 150%',
            allowHtml: true,
            content: '&times;'
          }],
          tap: function() {
            this.model.get('subWindowManager').set('currentWindow', {
              name: 'subWindow_customers',
              params: []
            });
          },
          init: function(model) {
            this.model = model;
          }
        }, {
          tag: 'h3',
          name: 'divheaderCustomerEditNew',
          content: OB.I18N.getLabel('OBPOS_TitleEditNewCustomer')
        }]
      }, {
        name: 'OB.UI.NewCustomerWindowImpl',
        kind: 'OB.UI.NewCustomerWindow',
        windowHeader: 'OB.UI.NewCustomerWindowHeader',
        newAttributes: [{
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerName',
          modelProperty: 'name',
          label: OB.I18N.getLabel('OBPOS_LblName')
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerLocName',
          modelProperty: 'locName',
          label: OB.I18N.getLabel('OBPOS_LblAddress')
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerPostalCode',
          modelProperty: 'postalcode',
          label: OB.I18N.getLabel('OBPOS_LblPostalCode')
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerCity',
          modelProperty: 'city',
          label: OB.I18N.getLabel('OBPOS_LblCity')
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerPhone',
          modelProperty: 'phone',
          label: OB.I18N.getLabel('OBPOS_LblPhone')
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerEmail',
          modelProperty: 'email',
          label: OB.I18N.getLabel('OBPOS_LblEmail')
        }]
      }]
    }]
  }, {
    name: 'EditCustomerSubWindowsContainer',
    components: [{
      style: 'background-color: #FFFFFF;',
      name: 'subWindow_edit_customer',
      beforeSetShowing: function(value, params) {
        this.setShowing(value);
        this.waterfall('onSetCustomer', {
          customer: params.businessPartner
        });
      },
      showing: false,
      components: [{
        name: 'OB.UI.EditCustomerWindowImpl',
        kind: 'OB.UI.NewCustomerWindow',
        windowHeader: 'OB.UI.EditCustomerWindowHeader',
        newAttributes: [{
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerName',
          modelProperty: 'name',
          label: OB.I18N.getLabel('OBPOS_LblName'),
          readOnly: true
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerLocName',
          modelProperty: 'locName',
          label: OB.I18N.getLabel('OBPOS_LblAddress'),
          readOnly: true
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerPostalCode',
          modelProperty: 'postalcode',
          label: OB.I18N.getLabel('OBPOS_LblPostalCode'),
          readOnly: true
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerCity',
          modelProperty: 'city',
          label: OB.I18N.getLabel('OBPOS_LblCity'),
          readOnly: true
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerPhone',
          modelProperty: 'phone',
          label: OB.I18N.getLabel('OBPOS_LblPhone'),
          readOnly: true
        }, {
          kind: 'OB.UI.CustomerTextProperty',
          name: 'customerEmail',
          modelProperty: 'email',
          label: OB.I18N.getLabel('OBPOS_LblEmail'),
          readOnly: true
        }]
      }]
    }]
  }, {
    name: 'mainSubWindow',
    components: [{
      kind: 'OB.UI.ModalDeleteReceipt'
    },{
      kind: 'OB.UI.Modalnoteditableorder'
    },{
      kind: 'OB.UI.ModalBusinessPartners'
    }, {
      kind: 'OB.UI.ModalPaidReceipts',
      name: 'paidReceiptsView'
    }, {
      kind: 'OB.UI.ModalCreateOrderFromQuotation',
      name: 'modalCreateOrderFromQuotation'
    }, {
      kind: 'OB.UI.ModalReceiptPropertiesImpl'
    }, {
      kind: 'OB.UI.ModalReceiptLinesPropertiesImpl'
    }, {
      classes: 'row',
      style: 'margin-bottom: 5px;',
      components: [{
        kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl'
      }, {
        kind: 'OB.OBPOSPointOfSale.UI.RightToolbarImpl',
        name: 'rightToolbar'
      }]
    }, {
      classes: 'row',
      components: [{
        kind: 'OB.OBPOSPointOfSale.UI.ReceiptView',
        name: 'receiptview'
      }, {
        classes: 'span6',
        components: [{
          kind: 'OB.OBPOSPointOfSale.UI.RightToolbarPane',
          name: 'toolbarpane'
        }, {
          kind: 'OB.OBPOSPointOfSale.UI.KeyboardOrder',
          name: 'keyboard'
        }]
      }]
    }]
  }],
  printReceipt: function() {

    if (OB.POS.modelterminal.hasPermission('OBPOS_print.receipt')) {
      var receipt = this.model.get('order');
      if(receipt.get("isPaid")){
        receipt.trigger('print');
        return;
      }
      receipt.calculateTaxes(function() {
        receipt.trigger('print');
      });
    }
  },
 paidReceipts: function(inSender, inEvent) {
   this.$.paidReceiptsView.setParams({quotation: false});
    $('#modalPaidReceipts').modal('show');
    return true;
  },
  
  quotations: function(inSender, inEvent) {
    this.$.paidReceiptsView.setParams({quotation: true});
    $('#modalPaidReceipts').modal('show');
  },
  
  backOffice: function(inSender, inEvent) {
    if (inEvent.url) {
      window.open(inEvent.url, '_blank');
    }
  },
  addNewOrder: function(inSender, inEvent) {
    this.model.get('orderList').addNewOrder();
    return true;
  },
  deleteCurrentOrder: function() {
    if (this.model.get('order').get('id')) {
      this.model.get('orderList').saveCurrent();
      OB.Dal.remove(this.model.get('orderList').current, null, null);
    }
    this.model.get('orderList').deleteCurrent();
    return true;
  },
  addProductToOrder: function(inSender, inEvent) {
    if(this.model.get('order').get('isEditable') === false){
      $("#modalNotEditableOrder").modal("show");
      return true;
    }
    this.model.get('order').addProduct(inEvent.product);
    this.model.get('orderList').saveCurrent();
    return true;
  },
  changeBusinessPartner: function(inSender, inEvent) {
    if(this.model.get('order').get('isEditable') === false){
      $("#modalNotEditableOrder").modal("show");
      return true;
    }
    this.model.get('order').setBPandBPLoc(inEvent.businessPartner, false, true);
    this.model.get('orderList').saveCurrent();
    return true;
  },
  receiptToInvoice: function() {
    if(this.model.get('order').get('isEditable') === false){
      $("#modalNotEditableOrder").modal("show");
      return true;
    }
    this.model.get('order').setOrderInvoice();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  createQuotation: function(){
    this.model.get('orderList').addNewQuotation();
    return true;
  },
  createOrderFromQuotation: function(){
    this.model.get('order').createOrderFromQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  reactivateQuotation: function(){
    this.model.get('order').reactivateQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  rejectQuotation: function(){
    this.model.get('order').rejectQuotation();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  showReturnText: function(inSender, inEvent) {
    if(this.model.get('order').get('isEditable') === false){
      $("#modalNotEditableOrder").modal("show");
      return true;
    }
    this.model.get('order').setOrderTypeReturn();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  cancelReceiptToInvoice: function(inSender, inEvent) {
    if(this.model.get('order').get('isEditable') === false){
      $("#modalNotEditableOrder").modal("show");
      return true;
    }
    this.model.get('order').resetOrderInvoice();
    this.model.get('orderList').saveCurrent();
    return true;
  },
  tabChange: function(sender, event) {
    this.waterfall('onChangeEditMode', {
      edit: event.edit
    });
    if (event.keyboard) {
      this.$.keyboard.showToolbar(event.keyboard);
    } else {
      this.$.keyboard.hide();
    }
  },
  deleteLine: function(sender, event) {
    if(this.model.get('order').get('isEditable') === false){
      $("#modalNotEditableOrder").modal("show");
      return true;
    }
    var line = event.line,
        receipt = this.model.get('order');
    if (line && receipt) {
      receipt.deleteLine(line);
      receipt.trigger('scan');
    }
  },
  editLine: function(sender, event) {
    if(this.model.get('order').get('isEditable') === false){
      $("#modalNotEditableOrder").modal("show");
      return true;
    }
    $("#receiptLinesPropertiesDialog").modal('show');
  },
  exactPayment: function(sender, event) {
    this.$.keyboard.execStatelessCommand('cashexact');
  },
  changeCurrentOrder: function(inSender, inEvent) {
    this.model.get('orderList').load(inEvent.newCurrentOrder);
    return true;
  },
  removePayment: function(sender, event) {
    if (this.model.get('paymentData') && !confirm(OB.I18N.getLabel('OBPOS_MsgConfirmRemovePayment'))) {
      return;
    }
    this.model.get('order').removePayment(event.payment);
  },
  changeSubWindow: function(sender, event) {
    this.model.get('subWindowManager').set('currentWindow', event.newWindow);
  },
  showModalReceiptProperties: function(inSender, inEvent) {
    $('#receiptPropertiesDialog').modal('show');
    return true;
  },
  setProperty: function(inSender, inEvent) {
    if(this.model.get('order').get('isEditable') === false){
      $("#modalNotEditableOrder").modal("show");
      return true;
    }
    this.model.get('order').setProperty(inEvent.property, inEvent.value);
    this.model.get('orderList').saveCurrent();
    return true;
  },
  setLineProperty: function(inSender, inEvent) {
    if(this.model.get('order').get('isEditable') === false){
      $("#modalNotEditableOrder").modal("show");
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
  beforeSetShowing: function(value, params) {
    this.setShowing(value);
  },
  init: function() {
    var receipt, receiptList;
    this.inherited(arguments);
    receipt = this.model.get('order');
    receiptList = this.model.get('orderList');
    this.model.get('subWindowManager').on('change:currentWindow', function(changedModel) {
      //TODO backbone route
      if (this.$[changedModel.get('currentWindow').name]) {
        this.$[changedModel.previousAttributes().currentWindow.name].setShowing(false);
        if (this.$[changedModel.get('currentWindow').name].beforeSetShowing) {
          this.$[changedModel.get('currentWindow').name].beforeSetShowing(true, changedModel.get('currentWindow').params);
        } else {
          this.$[changedModel.get('currentWindow').name].setShowing(true);
        }
      } else {
        this.model.get('subWindowManager').set('currentWindow', changedModel.previousAttributes().currentWindow, {
          silent: true
        });
      }
    }, this);
    this.$.receiptview.setOrder(receipt);
    this.$.receiptview.setOrderList(receiptList);
    this.$.toolbarpane.setModel(this.model);
    this.$.keyboard.setReceipt(receipt);
    this.$.rightToolbar.setReceipt(receipt);
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSPointOfSale.UI.PointOfSale,
  route: 'retail.pointofsale',
  menuPosition: null,
  // Not to display it in the menu
  menuLabel: 'POS'
});
OB.POS.modelterminal.set('windowRegistered', true);
OB.POS.modelterminal.triggerReady();