/*
 ************************************************************************************
 * Copyright (C) 2013-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo, confirm, _, localStorage */

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
    onShowRejectQuotation: 'showRejectQuotation',
    onRejectQuotation: 'rejectQuotation',
    onQuotations: 'quotations',
    onShowDivText: 'showDivText',
    onAddNewOrder: 'addNewOrder',
    onDeleteOrder: 'deleteCurrentOrder',
    onTabChange: 'tabChange',
    onDeleteLine: 'deleteLine',
    onEditLine: 'editLine',
    onReturnLine: 'returnLine',
    onExactPayment: 'exactPayment',
    onRemovePayment: 'removePayment',
    onReversePayment: 'reversePayment',
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
    onDisableBPLocSelection: 'BPLocSelectionDisabled',
    onDisableNewBP: 'newBPDisabled',
    onDisableNewBPLoc: 'newBPLocDisabled',
    onDisableOrderSelection: 'orderSelectionDisabled',
    onDisableKeyboard: 'keyboardDisabled',
    onDiscountsModeKeyboard: 'keyboardOnDiscountsMode',
    onCheckAllTicketLines: 'allTicketLinesChecked',
    onSetDiscountQty: 'discountQtyChanged',
    onLineChecked: 'checkedLine',
    onStatusChanged: 'statusChanged',
    onPaymentChanged: 'paymentChanged',
    onPaymentActionPay: 'paymentActionPay',
    onClearPaymentSelect: 'clearPaymentSelect',
    onLayaways: 'layaways',
    onChangeSalesRepresentative: 'changeSalesRepresentative',
    onMaxLimitAmountError: 'maxLimitAmountError',
    onMultiOrders: 'multiOrders',
    onSelectMultiOrders: 'selectMultiOrders',
    onRemoveMultiOrders: 'removeMultiOrders',
    onRightToolDisabled: 'rightToolbarDisabled',
    onSelectCharacteristicValue: 'selectCharacteristicValue',
    onSelectBrand: 'selectBrand',
    onSelectFilter: 'selectFilter',
    onSelectCategoryTreeItem: 'selectCategoryTreeItem',
    onShowLeftHeader: 'doShowLeftHeader',
    onWarehouseSelected: 'warehouseSelected',
    onClearUserInput: 'clearUserInput',
    onPricelistChanged: 'pricelistChanged',
    onChangeDiscount: 'changeDiscount',
    onReceiptLineSelected: 'receiptLineSelected',
    onManageServiceProposal: 'manageServiceProposal',
    onDisableUserInterface: 'disableUserInterface',
    onEnableUserInterface: 'enableUserInterface',
    onShowActionIcons: 'showActionIcons',
    onSetMultiSelection: 'setMultiSelection',
    onShowMultiSelection: 'showMultiSelection',
    onSetMultiSelectionItems: 'setMultiSelectionItems',
    onToggleLineSelection: 'toggleLineSelection',
    onFinishServiceProposal: 'finishServiceProposal',
    onSetBusinessPartnerTarget: 'setBusinessPartnerTarget',
    onPreSetCustomer: 'preSetCustomer',
    onPreSaveCustomer: 'preSaveCustomer',
    onkeydown: 'keyDownHandler',
    onkeyup: 'keyUpHandler',
    onRearrangeEditButtonBar: 'rearrangeEditButtonBar',
    onModalSelectPrinters: 'modalSelectPrinters',
    onModalSelectPDFPrinters: 'modalSelectPDFPrinters',
    onChangeFilterSelector: 'changeFilterSelector',
    onClearAllFilterSelector: 'clearAllFilterSelector',
    onCheckPresetFilterSelector: 'checkPresetFilterSelector',
    onAdvancedFilterSelector: 'advancedFilterSelector',
    onSetSelectorAdvancedSearch: 'setSelectorAdvancedSearch',
    onCloseSelector: 'closeSelector',
    onErrorCalcLineTax: 'errorCalcLineTax'
  },
  events: {
    onShowPopup: '',
    onHidePopup: '',
    onButtonStatusChanged: ''
  },
  components: [{
    name: 'other_SubWindows_Container',
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.customers.ModalConfigurationRequiredForCreateCustomers',
      name: 'modalConfigurationRequiredForCreateNewCustomers'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.customers.newcustomer',
      name: 'customerCreateAndEdit'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.customers.editcustomer',
      name: 'customerView'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.customeraddr.newcustomeraddr',
      name: 'customerAddrCreateAndEdit'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.customeraddr.editcustomeraddr',
      name: 'customerAddressView'
    }, {
      kind: 'OB.UI.ModalSelectorBusinessPartners',
      name: 'modalcustomer'
    }, {
      kind: 'OB.UI.ModalAdvancedFilterBP',
      name: 'modalAdvancedFilterBP'
    }, {
      kind: 'OB.UI.ModalBPLocation',
      name: 'modalcustomeraddress'
    }, {
      kind: 'OB.UI.ModalBPLocationShip',
      name: 'modalcustomershipaddress'
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
      kind: 'OB.UI.ModalReceipts',
      name: 'modalreceipts'
    }, {
      kind: 'OB.UI.ModalPaidReceipts',
      name: 'modalPaidReceipts'
    }, {
      kind: 'OBPOS.UI.ReceiptSelector',
      name: 'modalReceiptSelector'
    }, {
      kind: 'OB.UI.ModalAdvancedFilterReceipts',
      name: 'OB_UI_ModalAdvancedFilterReceipts'
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
      kind: 'OB.UI.ModalRejectQuotation',
      name: 'modalRejectQuotation'
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
      kind: 'OB.UI.ModalPaymentVoid',
      name: "modalpaymentvoid"
    }, {
      kind: 'OB.UI.ModalProviderGroup',
      name: 'modalprovidergroup'
    }, {
      kind: 'OB.UI.ModalProviderGroupVoid',
      name: 'modalprovidergroupvoid'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalConfigurationRequiredForCrossStore',
      name: 'modalConfigurationRequiredForCrossStore'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStore',
      name: 'modalLocalStock'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockInStoreClickable',
      name: 'modalLocalStockClickable'
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
    }, {
      kind: 'OB.UI.ModalProductCharacteristic',
      name: "modalproductcharacteristic"
    }, {
      kind: 'OB.UI.ModalProductBrand',
      name: "modalproductbrand"
    }, {
      kind: 'OB.UI.ModalCategoryTree',
      name: 'modalcategorytree'
    }, {
      kind: 'OB.UI.ModalSearchFilterBuilder',
      name: 'modalsearchfilterbuilder'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalPaymentsSelect',
      name: 'modalPaymentsSelect'
    }, {
      kind: 'OB.UI.ModalSelectPrinters',
      name: 'modalSelectPrinters'
    }, {
      kind: 'OB.UI.ModalSelectPDFPrinters',
      name: 'modalSelectPDFPrinters'
    }, {
      kind: 'OB.UI.ModalModulesInDev',
      name: 'modalModulesInDev'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.PaymentMethods',
      name: 'OBPOS_UI_PaymentMethods'
    }, {
      kind: 'OB.UI.ModalSelectOpenedReceipt',
      name: 'OBPOS_modalSelectOpenedReceipt'
    }, {
      kind: 'OB.UI.ModalSplitLine',
      name: 'OBPOS_modalSplitLine'
    }, {
      kind: 'OB.UI.ModalDeleteDiscount',
      name: 'modalDeleteDiscount'
    }, {
      kind: 'OB.UI.ModalProductAttributes',
      name: 'modalProductAttribute'
    }, {
      kind: 'OB.UI.ModalQuotationProductAttributes',
      name: 'modalQuotationProductAttributes'
    }, {
      kind: 'OB.UI.ModalOpenRelatedReceipts',
      name: 'modalOpenRelatedReceipts'
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
        style: 'max-height: 622px;',
        components: [{
          classes: 'span12',
          kind: 'OB.OBPOSPointOfSale.UI.LeftHeader',
          style: 'height: 35px;',
          name: 'divHeader'
        }, {
          classes: 'span12',
          kind: 'OB.OBPOSPointOfSale.UI.ReceiptView',
          name: 'receiptview',
          init: function (model) {
            this.model = model;
            this.model.get('leftColumnViewManager').on('change:currentView', function (changedModel) {
              this.setShowing(changedModel.isOrder());
            }, this);
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
    if (OB.MobileApp.model.hasPermission('OBPOS_print.receipt')) {
      if (this.model.get('leftColumnViewManager').isOrder()) {
        var receipt = this.model.get('order');
        if (receipt.get("isPaid")) {
          OB.UTIL.HookManager.executeHooks('OBPOS_PrePrintPaidReceipt', {
            context: this,
            receipt: this.model.get('order')
          }, function (args) {
            if (args && args.cancelOperation && args.cancelOperation === true) {
              return;
            }
            receipt.trigger('print', receipt, {
              forcePrint: true
            });
          });

          return;
        }
        receipt.trigger('print', receipt, {
          forcePrint: true
        });
        return;
      }
      if (this.model.get('leftColumnViewManager').isMultiOrder()) {
        _.each(this.model.get('multiOrders').get('multiOrdersList').models, function (order) {
          this.model.get('multiOrders').trigger('print', order, {
            forcePrint: true
          });
        }, this);
      }
    }
  },
  keyDownHandler: function (inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    OB.MobileApp.model.ctrlPressed = keyCode === 17;
    OB.MobileApp.model.shiftPressed = keyCode === 16;
  },
  keyUpHandler: function (inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 17) {
      OB.MobileApp.model.ctrlPressed = false;
    }
    if (keyCode === 16) {
      OB.MobileApp.model.shiftPressed = false;
    }
  },
  paidReceipts: function (inSender, inEvent) {
    var receipt = this.model.get('order');
    if (inEvent && inEvent.isReturn) {
      if (receipt && receipt.get('bp') && receipt.get('bp').get('id') !== OB.MobileApp.model.get('businessPartner').get('id')) {
        inEvent.bpartner = receipt.get('bp');
      } else if (receipt && receipt.get('lines').length > 0) {
        inEvent.bpartner = receipt.get('bp');
        inEvent.defaultBP = true;
      }
    }
    this.$.modalPaidReceipts.setParams(inEvent);
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
    this.$.receiptPropertiesDialog.resetProperties();
    this.model.get('orderList').addNewOrder();
    return true;
  },
  deleteCurrentOrder: function (inSender, inEvent) {
    var me = this;
    this.leftToolbarDisabled(inSender, inEvent);
    var receipt = this.model.get('order');
    receipt.deleteOrder(this, function () {
      inEvent.status = false;
      me.leftToolbarDisabled(inSender, inEvent);
      OB.MobileApp.model.runSyncProcess();
    });
  },
  addProductToOrder: function (inSender, inEvent) {
    var targetOrder, me = this,
        attrs;
    if (inEvent.product.get('ignoreAddProduct')) {
      inEvent.product.unset('ignoreAddProduct');
      return;
    }
    if (inEvent && inEvent.targetOrder) {
      targetOrder = inEvent.targetOrder;
    } else {
      targetOrder = this.model.get('order');
    }
    if (targetOrder.addProcess && targetOrder.addProcess.pending === true) {
      targetOrder.addProcess.hasProduct = true;
      targetOrder.addProcess.products.push({
        inSender: inSender,
        inEvent: inEvent
      });
      return false;
    }
    if (targetOrder.get('isEditable') === false) {
      targetOrder.canAddAsServices(this.model, inEvent.product, function (addAsServices) {
        if (addAsServices !== 'ABORT') {
          if (addAsServices === 'OK') {
            // Get approval
            var deferedSellApproval = _.find(targetOrder.get('approvals'), function (approval) {
              return approval.approvalType.approval === 'OBPOS_approval.deferred_sell_max_days';
            });
            if (deferedSellApproval) {
              deferedSellApproval.approvalType.message = 'OBPOS_approval.deferred_sell_max_days_erp';
              deferedSellApproval.approvalType.params.push(inEvent.attrs.relatedLines[0].productName);
              deferedSellApproval.approvalType.params.push(targetOrder.get('documentNo'));
            }
            _.each(inEvent.attrs.relatedLines, function (relatedLine) {
              relatedLine.orderDocumentNo = targetOrder.get('documentNo');
              relatedLine.otherTicket = OB.UTIL.isNullOrUndefined(inEvent.targetOrder);
              relatedLine.deferred = true;
              var currentLine = targetOrder.get('lines').models.filter(function getCurrentLine(line) {
                return line.id === relatedLine.orderlineId;
              });
              relatedLine.qty = currentLine[0].get('qty');
              relatedLine.gross = currentLine[0].get('gross');
              relatedLine.net = currentLine[0].get('net');
              if (currentLine[0].get('promotions')) {
                relatedLine.promotions = currentLine[0].get('promotions').slice();
              }
            });

            // Select open ticket or create a new one
            this.doShowPopup({
              popup: 'OBPOS_modalSelectOpenedReceipt',
              args: {
                product: inEvent.product,
                approval: deferedSellApproval,
                attrs: inEvent.attrs,
                context: inEvent.context,
                callback: inEvent.callback
              }
            });
            // Remove approval from not editable ticket
            if (deferedSellApproval) {
              var index = _.indexOf(targetOrder.get('approvals'), deferedSellApproval);
              if (index >= 0) {
                targetOrder.get('approvals').splice(index, 1);
              }
            }
          } else {
            if (inEvent.callback) {
              inEvent.callback.call(inEvent.context, false);
            }
          }
        } else {
          this.doShowPopup({
            popup: 'modalNotEditableOrder'
          });
          if (inEvent.callback) {
            inEvent.callback.call(inEvent.context, false);
          }
        }
      }, this);
      return true;
    }

    // If a deferred service has 'As per product' quantity rule, the product quantity must be set to the quantity of the line
    if (inEvent.attrs && inEvent.attrs.relatedLines && inEvent.attrs.relatedLines[0].deferred && inEvent.product.get('quantityRule') === 'PP') {
      inEvent.qty = inEvent.attrs.relatedLines[0].qty;
    }

    if (inEvent.ignoreStockTab) {
      this.showOrder(inSender, inEvent);
    } else {
      if (!targetOrder.get('lines').isProductPresent(inEvent.product) && inEvent.product.get('showstock') && !inEvent.product.get('ispack') && OB.MobileApp.model.get('connectedToERP')) {
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

    attrs = inEvent.attrs || {};
    attrs.kindOriginator = inEvent.originator && inEvent.originator.kind;
    OB.UTIL.HookManager.executeHooks('OBPOS_PreAddProductToOrder', {
      context: this,
      receipt: targetOrder,
      productToAdd: inEvent.product,
      qtyToAdd: inEvent.qty ? inEvent.qty : 1,
      options: inEvent.options,
      attrs: attrs
    }, function (args) {
      if (args.cancelOperation && args.cancelOperation === true) {
        if (inEvent.callback) {
          inEvent.callback.call(inEvent.context, false);
        }
        return true;
      }
      args.receipt.addProcess = {};
      args.receipt.addProcess.pending = true;
      args.receipt.addProcess.hasProduct = false;
      args.receipt.addProcess.products = [];
      args.receipt.addProduct(args.productToAdd, args.qtyToAdd, args.options, args.attrs, function (success, orderline) {
        args.receipt.addProcess.pending = false;
        if (success && orderline) {
          if (orderline.get('hasMandatoryServices') === false && args.receipt.addProcess.hasProduct === true) {
            args.receipt.addProcess.products.forEach(function (product) {
              me.addProductToOrder(product.inSender, product.inEvent);
            });
          }
        }
        args.context.model.get('orderList').saveCurrent();
        if (inEvent.callback) {
          inEvent.callback.call(inEvent.context, success, orderline);
        }
      });
    });
    return true;
  },
  showOrder: function (inSender, inEvent) {
    var allHidden = true;
    if (this.model.get('leftColumnViewManager').isMultiOrder()) {
      return false;
    }
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
      return true;
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
          this.$.multiColumn.$.leftPanel.$[inEvent.leftSubWindow].inEvent = inEvent;
        }
      }
    }
  },
  viewProductDetails: function (inSender, inEvent) {
    this.$.multiColumn.$.leftPanel.$.receiptview.setShowing(false);
    this.$.multiColumn.$.leftPanel.$.productdetailsview.updateProduct(inEvent.product);
    this.$.multiColumn.$.leftPanel.$.productdetailsview.setShowing(true);
    return true;
  },
  hideProductDetails: function (inSender, inEvent) {
    if (this.$.multiColumn.$.leftPanel.$.productdetailsview.showing) {
      this.$.multiColumn.$.leftPanel.$.productdetailsview.setShowing(false);
    }
    if (!this.model.get('leftColumnViewManager').isMultiOrder() && !this.$.multiColumn.$.leftPanel.$.receiptview.showing) {
      this.$.multiColumn.$.leftPanel.$.receiptview.setShowing(true);
    }
    return true;
  },
  changeBusinessPartner: function (inSender, inEvent) {
    var component = this,
        isBPChange = component.model.get('order').get('bp').get('id') !== inEvent.businessPartner.get('id'),
        isShippingChange = component.model.get('order').get('bp').get('shipLocId') !== inEvent.businessPartner.get('shipLocId'),
        isInvoicingChange = component.model.get('order').get('bp').get('locId') !== inEvent.businessPartner.get('locId'),
        bp = this.model.get('order').get('bp'),
        eventBP = inEvent.businessPartner;
    OB.UTIL.HookManager.executeHooks('OBPOS_preChangeBusinessPartner', {
      bp: inEvent.businessPartner,
      isBPChange: isBPChange,
      isShippingChange: isShippingChange,
      isInvoicingChange: isInvoicingChange
    }, function () {
      if (inEvent.target === 'order' || inEvent.target === undefined) {
        if (component.model.get('order').get('isEditable') === false && (isBPChange || isInvoicingChange || isShippingChange)) {
          component.doShowPopup({
            popup: 'modalNotEditableOrder'
          });
          return true;
        }
        component.model.get('order').setBPandBPLoc(eventBP, false, true);
        component.model.get('orderList').saveCurrent();
      } else {
        component.waterfall('onChangeBPartner', inEvent);
      }
    });
    return true;
  },
  receiptToInvoice: function () {
    if (this.model.get('leftColumnViewManager').isOrder()) {
      if (this.model.get('order').get('isEditable') === false && !this.model.get('order').get('isLayaway')) {
        if (!this.model.get('order').get('hasbeenpaid')) {
          this.doShowPopup({
            popup: 'modalNotEditableOrder'
          });
        }
        return true;
      }
      this.model.get('order').setOrderInvoice();
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
    var me = this;
    this.model.get('order').createOrderFromQuotation(false, function (success) {
      if (success) {
        me.model.get('orderList').saveCurrent();
      }
    });
    return true;
  },

  showReactivateQuotation: function () {
    this.doShowPopup({
      popup: 'modalReactivateQuotation'
    });
  },

  showRejectQuotation: function () {
    this.doShowPopup({
      popup: 'modalRejectQuotation'
    });
  },

  reactivateQuotation: function () {
    this.model.get('order').reactivateQuotation();
    this.model.get('orderList').saveCurrent();
    if (this.$.multiColumn.$.rightPanel.$.toolbarpane.$.edit.$.editTabContent.$.actionButtonsContainer.$.descriptionButton) {
      if (this.model.get('order').get('isEditable') && this.model.get('order').get('isQuotation')) {
        this.$.multiColumn.$.rightPanel.$.toolbarpane.$.edit.$.editTabContent.$.actionButtonsContainer.$.descriptionButton.show();
      }
    }
    return true;
  },
  rejectQuotation: function (inSender, inEvent) {
    this.model.get('order').rejectQuotation(inEvent.rejectReason, this, function (success) {
      if (success) {
        this.deleteCurrentOrder(inSender, inEvent);
      }
    });
    return true;
  },
  showDivText: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false && !this.model.get('order').get('isLayaway') && !this.model.get('order').get('cancelLayaway')) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    //Void Layaway must block keyboard actions
    if (inEvent.orderType === 3) {
      this.$.multiColumn.$.rightPanel.$.keyboard.setStatus('');
    }
    this.model.get('order').setOrderType(inEvent.permission, inEvent.orderType, {
      applyPromotions: false
    });
    this.model.get('orderList').saveCurrent();
    return true;
  },

  cancelReceiptToInvoice: function (inSender, inEvent) {
    if (this.model.get('leftColumnViewManager').isOrder()) {
      if (this.model.get('order').get('isEditable') === false && !this.model.get('order').get('isLayaway')) {
        this.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return true;
      }
      this.model.get('order').resetOrderInvoice();
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
  changeDiscount: function (inSender, inEvent) {
    this.waterfall('onDiscountChanged', inEvent);
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
  BPLocSelectionDisabled: function (inSender, inEvent) {
    this.waterfall('onBPLocSelectionDisabled', inEvent);
  },
  newBPDisabled: function (inSender, inEvent) {
    this.waterfall('onNewBPDisabled', inEvent);
  },
  newBPLocDisabled: function (inSender, inEvent) {
    this.waterfall('onNewBPLocDisabled', inEvent);
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
    this.BPLocSelectionDisabled(inSender, {
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
  disableUserInterface: function (inSender, inEvent) {
    this.leftToolbarDisabled(inSender, {
      status: true
    });
    this.rightToolbarDisabled(inSender, {
      status: true
    });
    this.BPSelectionDisabled(inSender, {
      status: true
    });
    this.BPLocSelectionDisabled(inSender, {
      status: true
    });
    this.orderSelectionDisabled(inSender, {
      status: true
    });
  },
  enableUserInterface: function (inSender, inEvent) {
    this.leftToolbarDisabled(inSender, {
      status: false
    });
    this.rightToolbarDisabled(inSender, {
      status: false
    });
    this.BPSelectionDisabled(inSender, {
      status: false
    });
    this.BPLocSelectionDisabled(inSender, {
      status: false
    });
    this.orderSelectionDisabled(inSender, {
      status: false
    });
  },
  showActionIcons: function (inSender, inEvent) {
    this.waterfall('onShowingActionIcons', inEvent);
  },
  tabChange: function (inSender, inEvent) {
    this.leftToolbarDisabled(inSender, {
      status: inEvent.status || false,
      disableMenu: (inEvent.keyboard === 'toolbardiscounts' || this.model.get('leftColumnViewManager').isMultiOrder() ? true : false),
      disableButtonNew: (inEvent.keyboard === 'toolbardiscounts' || this.model.get('leftColumnViewManager').isMultiOrder() ? true : false)
    });
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
      this.$.multiColumn.$.rightPanel.$.keyboard.lastStatus = '';
      this.$.multiColumn.$.rightPanel.$.keyboard.setStatus(inEvent.status);
    }
    if (inEvent.tabPanel === 'payment') {
      OB.MobileApp.model.set('inPaymentTab', true);
    } else {
      OB.MobileApp.model.set('inPaymentTab', false);
    }
    this.hideProductDetails();
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

    this.BPLocSelectionDisabled(inSender, {
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
    var me = this,
        selectedModels = inEvent.selectedModels,
        receipt = this.model.get('order');

    receipt.deleteLinesFromOrder(selectedModels);
  },
  editLine: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    this.doShowPopup({
      popup: 'receiptLinesPropertiesDialog',
      args: inEvent ? inEvent.args : null
    });
  },
  returnLine: function (inSender, inEvent) {
    if (this.model.get('order').get('isEditable') === false) {
      this.doShowPopup({
        popup: 'modalNotEditableOrder'
      });
      return true;
    }
    if (this.model.get('order').get('replacedorder') && inEvent.line.get('remainingQuantity')) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_CancelReplaceReturnLines'));
      return;
    }
    this.model.get('order').returnLine(inEvent.line);
  },
  exactPayment: function (inSender, inEvent) {
    this.$.multiColumn.$.rightPanel.$.keyboard.execStatelessCommand('cashexact');
  },
  changeCurrentOrder: function (inSender, inEvent) {
    this.model.get('orderList').load(inEvent.newCurrentOrder);
    return true;
  },
  removePayment: function (inSender, inEvent) {
    var me = this,
        voidTransaction, voidConfirmation, paymentProvider;

    var removeTransaction = function () {
        //      if (!me.model.get('multiOrders').get('isMultiOrders')) {
        //        me.model.get('order').removePayment(inEvent.payment);
        //      } else {
        //        me.model.get('multiOrders').removePayment(inEvent.payment);
        //      }
        if (me.model.get('leftColumnViewManager').isOrder()) {
          me.model.get('order').removePayment(inEvent.payment, inEvent.removeCallback);
          me.model.get('order').trigger('displayTotal');
          return;
        }
        if (me.model.get('leftColumnViewManager').isMultiOrder()) {
          me.model.get('multiOrders').removePayment(inEvent.payment, inEvent.removeCallback);
          me.model.get('multiOrders').trigger('displayTotal');
          return;
        }
        };

    var callVoidTransaction = function () {
        //To remove this payment we've to connect with server
        //a callback is defined to receive the confirmation
        me.doShowPopup({
          popup: 'modalpaymentvoid',
          args: {
            'amount': inEvent.payment.get('amount')
          }
        });

        voidTransaction(function (hasError, error) {
          me.doHidePopup({
            popup: 'modalpaymentvoid'
          });

          if (inEvent.removeCallback) {
            inEvent.removeCallback();
          }
          if (hasError) {
            OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblPaymentMethod'), error, [{
              label: OB.I18N.getLabel('OBMOBC_LblOk'),
              isConfirmButton: true
            }], {
              autoDismiss: false
            });
          } else {
            removeTransaction();
          }
        }, me.model.get('order'), inEvent.payment);
        };

    var callRemoveCallback = function () {
        if (inEvent.removeCallback) {
          inEvent.removeCallback();
        }
        };

    var showProviderVoidInstance = function (providerinstance) {
        me.doShowPopup({
          popup: 'modalprovidergroupvoid',
          args: {
            'removeTransaction': removeTransaction,
            'onhide': callRemoveCallback,
            'receipt': me.model.get('order'),
            'payment': inEvent.payment,
            'providerinstance': providerinstance
          }
        });
        };

    var callProviderVoidInstance = function () {
        var providerinstance = enyo.createFromKind(inEvent.payment.get('paymentData').provider.provider + 'Void');
        if (providerinstance.voidConfirmation) {
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblPaymentMethod'), OB.I18N.getLabel('OBPOS_MsgConfirmRemovePayment'), [{
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            isConfirmButton: true,
            action: function () {
              showProviderVoidInstance(providerinstance);
            }
          }, {
            label: OB.I18N.getLabel('OBMOBC_LblCancel'),
            action: callRemoveCallback
          }], {
            autoDismiss: false,
            onHideFunction: callRemoveCallback
          });
        } else {
          showProviderVoidInstance(providerinstance);
        }
        };

    if (inEvent.payment.get('paymentData')) {
      paymentProvider = eval(OB.MobileApp.model.paymentnames[inEvent.payment.get('kind')].paymentMethod.paymentProvider);
      if (paymentProvider && paymentProvider.prototype.voidTransaction && paymentProvider.prototype.voidTransaction instanceof Function) {
        voidTransaction = paymentProvider.prototype.voidTransaction;
      }
      if (!voidTransaction) {
        voidTransaction = inEvent.payment.get('paymentData').voidTransaction;
      }
      voidConfirmation = inEvent.payment.get('paymentData').voidConfirmation;

      if (voidConfirmation === false) {
        if (inEvent.payment.get('paymentData').provider) {
          callProviderVoidInstance();
        } else if (voidTransaction !== undefined) {
          callVoidTransaction();
        } else {
          removeTransaction();
        }
        return;
      }

      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblPaymentMethod'), OB.I18N.getLabel('OBPOS_MsgConfirmRemovePayment'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        isConfirmButton: true,
        action: function () {
          if (inEvent.payment.get('paymentData').provider) {
            callProviderVoidInstance();
          } else if (voidTransaction !== undefined) {
            callVoidTransaction();
          } else {
            removeTransaction();
          }
          return true;
        }
      }, {
        label: OB.I18N.getLabel('OBMOBC_LblCancel'),
        action: function () {
          if (inEvent.removeCallback) {
            inEvent.removeCallback();
          }
          return true;
        }
      }], {
        autoDismiss: false,
        onHideFunction: function () {
          if (inEvent.removeCallback) {
            inEvent.removeCallback();
          }
        }
      });
    } else {
      removeTransaction();
    }
  },
  reversePayment: function (inSender, inEvent) {
    var me = this;
    if (me.model.get('leftColumnViewManager').isOrder()) {
      me.model.get('order').reversePayment(inEvent.payment, inEvent.sender, inEvent.reverseCallback);
      me.model.get('order').trigger('displayTotal');
      return;
    }
  },
  changeSubWindow: function (inSender, inEvent) {
    this.model.get('subWindowManager').set('currentWindow', inEvent.newWindow);
  },
  setReceiptsList: function (inSender, inEvent) {
    this.$.modalreceipts.setReceiptsList(inEvent.orderList);
    this.$.OBPOS_modalSelectOpenedReceipt.setReceiptsList(inEvent.orderList);
  },
  showModalReceiptProperties: function (inSender, inEvent) {
    this.doShowPopup({
      popup: 'receiptPropertiesDialog'
    });
    return true;
  },
  modalSelectPrinters: function (inSender, inEvent) {
    this.doShowPopup({
      popup: 'modalSelectPrinters'
    });
    return true;
  },
  modalSelectPDFPrinters: function (inSender, inEvent) {
    this.doShowPopup({
      popup: 'modalSelectPDFPrinters'
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
  paymentChanged: function (inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onButtonPaymentChanged', inEvent);
  },
  paymentActionPay: function (inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onActionPay', inEvent);
  },
  clearPaymentSelect: function (inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onClearPaymentMethodSelect', inEvent);
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
  selectCharacteristicValue: function (inSender, inEvent) {
    this.waterfall('onUpdateFilter', {
      value: inEvent
    });
  },
  multiOrders: function (inSender, inEvent) {
    this.doShowPopup({
      popup: 'modalMultiOrders'
    });
    return true;
  },
  maxLimitAmountError: function (inSender, inEvent) {
    this.waterfallDown('onMaxLimitAmountError', inEvent);
    return true;
  },
  selectBrand: function (inSender, inEvent) {
    this.waterfall('onUpdateBrandFilter', {
      value: inEvent
    });
  },
  selectCategoryTreeItem: function (inSender, inEvent) {
    this.waterfall('onSelectCategoryItem', inEvent);
  },
  selectFilter: function (inSender, inEvent) {
    this.waterfall('onCustomFilterUpdate', inEvent);
  },
  warehouseSelected: function (inSender, inEvent) {
    this.waterfall('onModifyWarehouse', inEvent);
  },
  selectMultiOrders: function (inSender, inEvent) {
    var me = this;
    me.model.get('multiOrders').get('multiOrdersList').reset();
    _.each(inEvent.value, function (iter) {
      //iter.set('isMultiOrder', true);
      iter.set('belongsToMultiOrder', true);
      me.model.get('orderList').addMultiReceipt(iter);
      me.model.get('multiOrders').get('multiOrdersList').add(iter);
    });
    this.model.get('leftColumnViewManager').setMultiOrderMode();
    return true;
  },
  removeOrderAndExitMultiOrder: function (model) {
    model.deleteMultiOrderList();
    model.get('multiOrders').resetValues();
    model.get('leftColumnViewManager').setOrderMode();
  },
  cancelRemoveMultiOrders: function (inSender, originator) {
    if (originator.kind === 'OB.UI.RemoveMultiOrders') {
      originator.deleting = false;
      originator.removeClass('btn-icon-loading');
      originator.addClass('btn-icon-clearPayment');
    }
    this.leftToolbarDisabled(inSender, {
      status: false,
      disableButtonNew: true
    });
  },
  removeMultiOrders: function (inSender, inEvent) {
    var me = this,
        originator = inEvent.originator;
    if (me.model.get('multiOrders').checkMultiOrderPayment()) {
      me.cancelRemoveMultiOrders(inSender, originator);
      return true;
    }
    // If there are more than 1 order, do as usual
    if (me.model.get('multiOrders').get('multiOrdersList').length > 1 && inEvent.order) {
      me.model.get('multiOrders').get('multiOrdersList').remove(inEvent.order);
      if (inEvent && inEvent.order && inEvent.order.get('loadedFromServer')) {
        me.model.get('orderList').current = inEvent.order;
        me.model.get('orderList').deleteCurrent();
        me.model.get('orderList').deleteCurrentFromDatabase(inEvent.order);
      }
      return true;
    } else {
      // Delete and exit the multiorder
      me.removeOrderAndExitMultiOrder(me.model);
      return true;
    }
  },
  doShowLeftHeader: function (inSender, inEvent) {
    this.waterfall('onLeftHeaderShow', inEvent);
  },
  clearUserInput: function (inSender, inEvent) {
    this.waterfall('onClearEditBox', inEvent);
  },
  pricelistChanged: function (inSender, inEvent) {
    this.waterfall('onChangePricelist', inEvent);
  },
  changeFilterSelector: function (inSender, inEvent) {
    this.waterfall('onUpdateFilterSelector', inEvent);
  },
  clearAllFilterSelector: function (inSender, inEvent) {
    this.waterfall('onClearFilterSelector', inEvent);
  },
  checkPresetFilterSelector: function (inSender, inEvent) {
    this.waterfall('onHasPresetFilterSelector', inEvent);
  },
  advancedFilterSelector: function (inSender, inEvent) {
    this.waterfall('onGetAdvancedFilterSelector', inEvent);
  },
  setSelectorAdvancedSearch: function (inSender, inEvent) {
    this.waterfall('onSetAdvancedSearchMode', inEvent);
  },
  receiptLineSelected: function (inSender, inEvent) {
    var enableButton = true,
        selectedLines = this.$.multiColumn.$.rightPanel.$.keyboard.selectedModels,
        selectedLinesSameQty = this.$.multiColumn.$.rightPanel.$.keyboard.selectedModelsSameQty,
        selectedLinesLength = selectedLines ? this.$.multiColumn.$.rightPanel.$.keyboard.selectedModels.length : 0,
        product, i;
    if (selectedLinesLength > 1) {
      for (i = 0; i < selectedLinesLength; i++) {
        product = selectedLines[i].get('product');
        if (!product.get('groupProduct') || (product.get('productType') === 'S' && product.get('isLinkedToProduct')) || !selectedLines[i].get('isEditable') || product.get('isSerialNo')) {
          enableButton = false;
          break;
        }
      }
      if (enableButton && !selectedLinesSameQty) {
        enableButton = false;
      }
    } else if (selectedLinesLength === 1) {
      product = selectedLines[0].get('product');
      if (!product.get('groupProduct') || (product.get('productType') === 'S' && product.get('isLinkedToProduct')) || !selectedLines[0].get('isEditable') || product.get('isSerialNo')) {
        enableButton = false;
      }
    } else {
      enableButton = false;
    }
    this.enableKeyboardButton(enableButton);
    OB.UTIL.HookManager.executeHooks('OBPOS_LineSelected', {
      line: inEvent.line,
      selectedLines: selectedLines,
      context: this
    }, function (args) {});
  },
  enableKeyboardButton: function (enableButton) {
    if (enableButton && !this.model.get('order').get('isEditable')) {
      enableButton = false;
    }
    this.waterfall('onEnableQtyButton', {
      enable: enableButton
    });
    this.waterfall('onEnablePlusButton', {
      enable: enableButton
    });
    this.waterfall('onEnableMinusButton', {
      enable: enableButton
    });
  },
  manageServiceProposal: function (inSender, inEvent) {
    this.waterfallDown('onManageServiceProposal', inEvent);
  },
  toggleLineSelection: function (inSender, inEvent) {
    this.waterfall('onToggledLineSelection', inEvent);
  },
  finishServiceProposal: function (inSender, inEvent) {
    this.waterfallDown('onFinishServiceProposal', inEvent);
  },
  setBusinessPartnerTarget: function (inSender, inEvent) {
    this.waterfallDown('onSetBPartnerTarget', inEvent);
  },
  preSetCustomer: function (inSender, inEvent) {
    this.waterfallDown('onSetCustomer', inEvent);
  },
  preSaveCustomer: function (inSender, inEvent) {
    this.waterfallDown('onSaveCustomer', inEvent);
  },
  setMultiSelection: function (inSender, inEvent) {
    this.waterfall('onSetMultiSelected', inEvent);
  },
  showMultiSelection: function (inSender, inEvent) {
    this.waterfall('onShowMultiSelected', inEvent);
  },
  setMultiSelectionItems: function (inSender, inEvent) {
    this.waterfall('onTableMultiSelectedItems', inEvent);
  },
  closeSelector: function (inSender, inEvent) {
    this.waterfall('onCloseCancelSelector', inEvent);
  },
  errorCalcLineTax: function (inSender, inEvent) {
    if (OB.MobileApp.model.get('serviceSearchMode')) {
      OB.MobileApp.view.$.containerWindow.getRoot().$.multiColumn.$.rightToolbar.$.rightToolbar.manualTap('edit');
    }
  },
  rearrangeEditButtonBar: function (inSender, inEvent) {
    this.waterfall('onRearrangedEditButtonBar', inEvent);
  },
  init: function () {
    var receipt, receiptList, LeftColumnCurrentView;
    this.inherited(arguments);
    receipt = this.model.get('order');
    receiptList = this.model.get('orderList');
    OB.MobileApp.view.scanningFocus(true);

    this.waterfall('onPointOfSaleLoad');

    // Try to print the pending receipts.
    OB.Model.OfflinePrinter.printPendingJobs();

    this.model.get('leftColumnViewManager').on('change:currentView', function (changedModel) {
      this.$.multiColumn.$.rightPanel.$.keyboard.clearInput();
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

    LeftColumnCurrentView = enyo.json.parse(OB.UTIL.localStorage.getItem('leftColumnCurrentView'));
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
            } else {
              //developers helps
              //OB.info("Error! A subwindow must inherits from OB.UI.subwindow -> restore previous state");
              restorePreviousState(this.model.get('subWindowManager'), changedModel);
            }
          }
        } else {
          restorePreviousState(this.model.get('subWindowManager'), changedModel);
        }
      } else {
        //developers helps
        //OB.info("The subwindow to navigate doesn't exists -> restore previous state");
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

    receipt.on('change:isEditable', function (model) {
      this.enableKeyboardButton(true);
    }, this);

    this.$.multiColumn.$.leftPanel.$.receiptview.setOrder(receipt);
    this.$.multiColumn.$.leftPanel.$.receiptview.setOrderList(receiptList);
    this.$.multiColumn.$.rightPanel.$.toolbarpane.setModel(this.model);
    this.$.multiColumn.$.rightPanel.$.keyboard.setReceipt(receipt);
    this.$.multiColumn.$.rightToolbar.$.rightToolbar.setReceipt(receipt);
  },
  initComponents: function () {
    this.inherited(arguments);
    if (OB.UTIL.Debug.isDebug()) {
      document.body.style.background = '';
      document.body.className += ' indev-background';
      this.waterfall('onInDevHeaderShow');
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftHeader',
  showing: false,
  published: {
    text: null
  },
  handlers: {
    onLeftHeaderShow: 'doShowHeader'
  },
  doShowHeader: function (inSender, inEvent) {
    this.setText(inEvent.text);
    if (inEvent.style) {
      this.$.innerDiv.addStyles(inEvent.style);
    }
    this.show();
  },

  components: [{
    name: 'innerDiv',
    style: 'text-align: center; font-size: 30px; padding: 5px; padding-top: 0px;',
    components: [{
      name: 'headerText',
      attributes: {
        style: 'background-color: #ffffff; height: 30px; font-weight:bold; padding-top: 15px;'
      },
      content: ''
    }]
  }],
  textChanged: function () {
    this.$.headerText.setContent(this.text);
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
  menuLabel: 'POS',
  defaultWindow: true
});