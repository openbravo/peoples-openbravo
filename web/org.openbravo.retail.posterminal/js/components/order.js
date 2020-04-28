/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _ */

enyo.kind({
  name: 'OB.UI.OrderMultiSelect',
  classes: 'obUiOrderMultiSelect',
  kind: 'OB.UI.Button',
  i18nLabel: 'OBPOS_LblDisableMultiselection',
  showing: false,
  events: {
    onToggleSelection: ''
  },
  published: {
    disabled: false
  },
  tap: function() {
    this.doToggleSelection({
      multiselection: false
    });
  }
});

enyo.kind({
  name: 'OB.UI.OrderSingleSelect',
  classes: 'obUiOrderSingleSelect',
  kind: 'OB.UI.Button',
  i18nLabel: 'OBPOS_LblEnableMultiselection',
  events: {
    onToggleSelection: ''
  },
  published: {
    disabled: false
  },
  tap: function() {
    this.doToggleSelection({
      multiselection: true
    });
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.OrderMultiSelectAll',
  i18nContent: 'OBPOS_lblSelectAll',
  classes: 'obUiOrderMultiSelectAll',
  showing: false,
  events: {
    onMultiSelectAll: ''
  },
  published: {
    disabled: false
  },
  tap: function() {
    this.doMultiSelectAll();
  }
});

enyo.kind({
  name: 'OB.UI.OrderHeader',
  classes: 'obUiOrderHeader',
  published: {
    order: null
  },
  events: {
    onToggleSelectionMode: '',
    onTableMultiSelectAll: ''
  },
  handlers: {
    onShowMultiSelected: 'showMultiSelected',
    onToggleSelection: 'toggleSelection',
    onMultiSelectAll: 'multiSelectAll'
  },
  newLabelComponents: [
    {
      classes: 'obUiOrderHeader-labelComponents-orderdetails',
      kind: 'OB.UI.OrderDetails',
      name: 'orderdetails'
    },
    {
      classes: 'obUiOrderHeader-labelComponents-btnMultiSelectAll',
      kind: 'OB.UI.OrderMultiSelectAll',
      name: 'btnMultiSelectAll'
    },
    {
      classes: 'obUiOrderHeader-labelComponents-btnMultiSelection',
      kind: 'OB.UI.OrderMultiSelect',
      name: 'btnMultiSelection'
    },
    {
      classes: 'obUiOrderHeader-labelComponents-btnSingleSelection',
      kind: 'OB.UI.OrderSingleSelect',
      name: 'btnSingleSelection'
    }
  ],
  newButtonComponents: [
    {
      classes:
        'obUiFormElement_dataEntry obUiOrderHeader-buttonComponents-formElementBpbutton',
      kind: 'OB.UI.FormElement',
      name: 'formElementBpbutton',
      coreElement: {
        classes:
          'obUiOrderHeader-buttonComponents-formElementBpbutton-bpbutton',
        kind: 'OB.UI.BusinessPartnerSelector',
        i18nLabel: 'OBPOS_LblCustomer',
        hideNullifyButton: true,
        name: 'bpbutton'
      }
    },
    {
      name: 'separator',
      classes: 'obUiOrderHeader-buttonComponents-separator'
    },
    {
      classes:
        'obUiFormElement_dataEntry obUiOrderHeader-buttonComponents-formElementBplocbutton',
      kind: 'OB.UI.FormElement',
      name: 'formElementBplocbutton',
      coreElement: {
        classes:
          'obUiOrderHeader-buttonComponents-formElementBplocbutton-bplocbutton',
        kind: 'OB.UI.BPLocation',
        i18nLabel: 'OBPOS_LblBillAddr',
        hideNullifyButton: true,
        name: 'bplocbutton'
      }
    },
    {
      classes:
        'obUiFormElement_dataEntry obUiOrderHeader-buttonComponents-formElementBplocshipbutton',
      kind: 'OB.UI.FormElement',
      name: 'formElementBplocshipbutton',
      showing: false,
      coreElement: {
        classes:
          'obUiOrderHeader-buttonComponents-formElementBplocshipbutton-bplocshipbutton',
        kind: 'OB.UI.BPLocationShip',
        i18nLabel: 'OBPOS_LblShipAddr',
        hideNullifyButton: true,
        name: 'bplocshipbutton'
      }
    }
  ],
  components: [
    {
      name: 'receiptLabels',
      classes: 'obUiOrderHeader-receiptLabels'
    },
    {
      kind: 'OB.UI.ActionButtonArea',
      name: 'abaReceiptToolbar1',
      abaIdentifier: 'obpos_pointofsale-receipttoolbar1',
      classes: 'obUiOrderHeader-abaReceiptToolbar1'
    },
    {
      name: 'receiptButtons',
      classes: 'obUiOrderHeader-receiptButtons'
    }
  ],
  resizeHandler: function() {
    this.inherited(arguments);
  },
  orderChanged: function(oldValue) {
    _.each(
      this.$.receiptLabels.$,
      function(comp) {
        if (comp.setOrder) {
          comp.setOrder(this.order);
        }
      },
      this
    );
    _.each(
      this.$.receiptButtons.$,
      function(comp) {
        if (comp.setOrder) {
          comp.setOrder(this.order);
        } else if (comp.coreElement && comp.coreElement.setOrder) {
          comp.coreElement.setOrder(this.order);
        }
      },
      this
    );
  },
  showMultiSelected: function(inSender, inEvent) {
    if (inEvent.show) {
      this.$.receiptLabels.$.btnSingleSelection.setShowing(true);
    } else {
      this.$.receiptLabels.$.btnSingleSelection.setShowing(false);
    }
    this.$.receiptLabels.$.btnMultiSelection.setShowing(false);
    this.$.receiptLabels.$.btnMultiSelectAll.setShowing(false);
    this.doToggleSelectionMode({
      multiselection: false
    });
  },
  toggleSelection: function(inSender, inEvent) {
    if (inEvent.multiselection) {
      this.$.receiptLabels.$.btnSingleSelection.setShowing(false);
      this.$.receiptLabels.$.btnMultiSelection.setShowing(true);
      this.$.receiptLabels.$.btnMultiSelectAll.setShowing(true);
    } else {
      this.$.receiptLabels.$.btnSingleSelection.setShowing(true);
      this.$.receiptLabels.$.btnMultiSelection.setShowing(false);
      this.$.receiptLabels.$.btnMultiSelectAll.setShowing(false);
    }
    this.doToggleSelectionMode(inEvent);
  },
  multiSelectAll: function(inSender, inEvent) {
    this.doTableMultiSelectAll();
  },
  initComponents: function() {
    this.inherited(arguments);
    this.showPin = false;
    this.showSelectAll = false;
    enyo.forEach(
      this.newLabelComponents,
      function(comp) {
        this.$.receiptLabels.createComponent(comp);
      },
      this
    );
    enyo.forEach(
      this.newButtonComponents,
      function(comp) {
        this.$.receiptButtons.createComponent(comp);
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.UI.OrderCaptions',
  classes: 'obUiOrderCaptions',
  events: {
    onAdjustOrderCaption: ''
  },
  components: [
    {
      name: 'description',
      classes: 'obUiOrderCaptions-description',
      i18nContent: 'OBPOS_LineDescription',
      initComponents: function() {
        this.setContent(OB.I18N.getLabel(this.i18nContent));
      }
    },
    {
      name: 'quantity',
      classes: 'obUiOrderCaptions-quantity',
      i18nContent: 'OBPOS_LineQuantity',
      initComponents: function() {
        this.setContent(OB.I18N.getLabel(this.i18nContent));
      }
    },
    {
      name: 'unitprice',
      classes: 'obUiOrderCaptions-unitprice',
      i18nContent: 'OBPOS_LineUnitPrice',
      initComponents: function() {
        this.setContent(OB.I18N.getLabel(this.i18nContent));
      }
    },
    {
      name: 'linetotal',
      classes: 'obUiOrderCaptions-linetotal',
      i18nContent: 'OBPOS_LineLineTotal',
      initComponents: function() {
        this.setContent(OB.I18N.getLabel(this.i18nContent));
      }
    }
  ],
  resizeHandler: function() {
    this.doAdjustOrderCaption();
  }
});

enyo.kind({
  name: 'OB.UI.OrderFooter',
  classes: 'obUiOrderFooter',
  published: {
    order: null
  },
  newComponents: [],
  orderChanged: function() {
    _.each(
      this.$,
      function(comp) {
        if (comp.setOrder) {
          comp.setOrder(this.order);
        }
      },
      this
    );
  },
  initComponents: function() {
    this.inherited(arguments);
    enyo.forEach(
      this.newComponents,
      function(comp) {
        this.createComponent(comp);
      },
      this
    );
  }
});

enyo.kind({
  name: 'OB.UI.TotalMultiReceiptLine',
  classes: 'obUiTotalMultiReceiptLine',
  components: [
    {
      name: 'lblTotal',
      classes: 'obUiTotalMultiReceiptLine-lblTotal'
    },
    {
      name: 'totalqty',
      classes: 'obUiTotalMultiReceiptLine-totalqty'
    },
    {
      name: 'totalgross',
      classes: 'obUiTotalMultiReceiptLine-totalgross'
    }
  ],
  renderTotal: function(newTotal) {
    this.$.totalgross.setContent(OB.I18N.formatCurrency(newTotal));
  },
  renderQty: function(newQty) {
    this.$.totalqty.setContent(newQty);
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.lblTotal.setContent(OB.I18N.getLabel('OBPOS_LblTotal'));
  }
});
enyo.kind({
  name: 'OB.UI.TotalReceiptLine',
  classes: 'obUiTotalReceiptLine',
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxForTicketLines'
  },
  components: [
    {
      name: 'lblTotal',
      classes: 'obUiTotalReceiptLine-lblTotal'
    },
    {
      kind: 'OB.UI.FitText',
      maxFontSize: 20,
      classes: 'obUiTotalReceiptLine-obUiFitTextQty fitText',
      components: [
        {
          tag: 'span',
          classes: 'obUiTotalReceiptLine-obUiFitTextQty-totalqty',
          name: 'totalqty'
        }
      ]
    },
    {
      kind: 'OB.UI.FitText',
      maxFontSize: 20,
      classes: 'obUiTotalReceiptLine-obUiFitTextGross fitText',
      components: [
        {
          tag: 'span',
          classes: 'obUiTotalReceiptLine-obUiFitTextGross-totalgross',
          name: 'totalgross'
        }
      ]
    }
  ],
  renderTotal: function(newTotal) {
    if (newTotal !== this.$.totalgross.getContent()) {
      this.$.totalgross.setContent(OB.I18N.formatCurrency(newTotal));
      OB.UTIL.HookManager.executeHooks('OBPOS_UpdateTotalReceiptLine', {
        totalline: this
      });
    }
  },
  renderQty: function(newQty) {
    this.$.totalqty.setContent(newQty);
  },
  checkBoxForTicketLines: function(inSender, inEvent) {
    if (inEvent.status) {
      this.$.lblTotal.addClass('obUiTotalReceiptLine-lblTotal_large');
      this.$.totalqty.addClass(
        'obUiTotalReceiptLine-obUiFitTextQty-totalqty_small'
      );
      this.$.totalgross.addClass(
        'obUiTotalReceiptLine-obUiFitTextGross-totalgross_small'
      );
    } else {
      this.$.lblTotal.addClass('obUiTotalReceiptLine-lblTotal_small');
      this.$.totalqty.addClass(
        'obUiTotalReceiptLine-obUiFitTextQty-totalqty_large'
      );
      this.$.totalgross.addClass(
        'obUiTotalReceiptLine-obUiFitTextGross-totalgross_large'
      );
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.lblTotal.setContent(OB.I18N.getLabel('OBPOS_LblTotal'));
    OB.UTIL.HookManager.executeHooks('OBPOS_RenderTotalReceiptLine', {
      totalline: this
    });
  }
});

enyo.kind({
  name: 'OB.UI.TotalTaxLine',
  classes: 'obUiTotalTaxLine',
  components: [
    {
      name: 'lblTotalTax',
      classes: 'obUiTotalTaxLine-lblTotalTax'
    },
    {
      name: 'totalbase',
      classes: 'obUiTotalTaxLine-totalbase'
    },
    {
      name: 'totaltax',
      classes: 'obUiTotalTaxLine-totaltax'
    },
    {
      classes: 'obUiTotalTaxLine-element1'
    }
  ],
  renderTax: function(newTax) {
    this.$.totaltax.setContent(OB.I18N.formatCurrency(newTax));
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.lblTotalTax.setContent(OB.I18N.getLabel('OBPOS_LblTotalTax'));
  }
});

enyo.kind({
  name: 'OB.UI.TaxBreakdown',
  classes: 'obUiTaxBreakdown',
  components: [
    {
      classes: 'obUiTaxBreakdown-lblTotalTaxBreakdown',
      name: 'lblTotalTaxBreakdown'
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.lblTotalTaxBreakdown.setContent(
      OB.I18N.getLabel('OBPOS_LblTaxBreakdown')
    );
  }
});

enyo.kind({
  kind: 'OB.UI.FormElement',
  name: 'OB.UI.BtnReceiptToInvoice',
  classes: 'obUiFormElement_dataEntry obUiBtnReceiptToInvoice',
  events: {
    onCancelReceiptToInvoice: ''
  },
  coreElement: {
    kind: 'OB.UI.FormElement.Checkbox',
    i18nLabel: 'OBPOS_LblInvoiceReceipt',
    checked: true,
    tap: function() {
      this.formElement.doCancelReceiptToInvoice();
    }
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.UI.OrderViewDivText',
  classes: 'obUiOrderViewDivText',
  showing: false,
  content: '',

  clearLabel: function() {
    this.removeClass('obUiOrderViewDivText_ToBeLaidaway');
    this.removeClass('obUiOrderViewDivText_ToBeReturned');
    this.removeClass('obUiOrderViewDivText_CancelLayaway');
    this.removeClass('obUiOrderViewDivText_CancelLayawayFromLayaway');
    this.removeClass('obUiOrderViewDivText_CancelAndReplaceType1');
    this.removeClass('obUiOrderViewDivText_CancelAndReplaceType2');
    this.removeClass('obUiOrderViewDivText_layaway');
    this.removeClass('obUiOrderViewDivText_paid');
    this.removeClass('obUiOrderViewDivText_quotation');
  },

  changeHasbeenpaid: function(model) {
    if (
      model.get('isQuotation') &&
      model.get('hasbeenpaid') === 'Y' &&
      !model.get('obposIsDeleted') &&
      this.content &&
      (this.content === OB.I18N.getLabel('OBPOS_QuotationNew') ||
        this.content === OB.I18N.getLabel('OBPOS_QuotationDraft'))
    ) {
      this.setContent(OB.I18N.getLabel('OBPOS_QuotationUnderEvaluation'));
    } else if (
      model.get('isQuotation') &&
      model.get('hasbeenpaid') === 'N' &&
      !model.get('isLayaway')
    ) {
      this.setContent(OB.I18N.getLabel('OBPOS_QuotationDraft'));
    }
  },

  setQuotationLabel: function(model) {
    this.clearLabel();
    this.addClass('obUiOrderViewDivText_quotation');
    if (model.get('hasbeenpaid') === 'Y') {
      this.setContent(OB.I18N.getLabel('OBPOS_QuotationUnderEvaluation'));
    } else {
      this.setContent(OB.I18N.getLabel('OBPOS_QuotationDraft'));
    }
    this.show();
  },

  setPaidLabel: function(model) {
    this.clearLabel();
    this.addClass('obUiOrderViewDivText_paid');
    if (model.get('iscancelled')) {
      this.setContent(OB.I18N.getLabel('OBPOS_Cancelled'));
    } else if (model.get('paidOnCredit')) {
      if (model.get('paidPartiallyOnCredit')) {
        this.setContent(
          OB.I18N.getLabel('OBPOS_paidPartiallyOnCredit', [
            OB.I18N.formatCurrency(model.get('creditAmount'))
          ])
        );
      } else {
        this.setContent(OB.I18N.getLabel('OBPOS_paidOnCredit'));
      }
    } else if (
      model.get('documentType') ===
      OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns
    ) {
      this.setContent(OB.I18N.getLabel('OBPOS_paidReturn'));
    } else {
      this.setContent(OB.I18N.getLabel('OBPOS_paid'));
    }
    this.show();
  },

  setLayawayLabel: function(model) {
    this.clearLabel();
    this.addClass('obUiOrderViewDivText_layaway');
    if (model.get('iscancelled')) {
      this.setContent(OB.I18N.getLabel('OBPOS_Cancelled'));
    } else {
      this.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
    }
    this.show();
  },

  setCancelAndReplaceLabel: function(model) {
    this.clearLabel();
    if (model.get('orderType') === 2) {
      this.addClass('obUiOrderViewDivText_CancelAndReplaceType1');
      this.setContent(
        OB.I18N.getLabel('OBPOS_ToBeLaidaway') +
          ': ' +
          OB.I18N.getLabel('OBPOS_CancelAndReplaceOf', [
            model.get('replacedorder_documentNo')
          ])
      );
    } else {
      this.addClass('obUiOrderViewDivText_CancelAndReplaceType2');
      this.setContent(
        OB.I18N.getLabel('OBPOS_CancelAndReplaceOf', [
          model.get('replacedorder_documentNo')
        ])
      );
    }
    this.show();
  },

  setCancelLayawayLabel: function(model) {
    this.clearLabel();
    if (model.get('fromLayaway')) {
      this.addClass('obUiOrderViewDivText_CancelLayawayFromLayaway');
      this.setContent(OB.I18N.getLabel('OBPOS_CancelLayaway'));
    } else {
      this.addClass('obUiOrderViewDivText_CancelLayaway');
      this.setContent(OB.I18N.getLabel('OBPOS_CancelOrder'));
    }
    this.show();
  },

  setToBeReturnedLabel: function(model) {
    this.clearLabel();
    this.addClass('obUiOrderViewDivText_ToBeReturned');
    this.setContent(OB.I18N.getLabel('OBPOS_ToBeReturned'));
    this.show();
  },

  setToBeLaidawayLabel: function(model) {
    this.clearLabel();
    this.addClass('obUiOrderViewDivText_ToBeLaidaway');
    this.setContent(OB.I18N.getLabel('OBPOS_ToBeLaidaway'));
    this.show();
  }
});

enyo.kind({
  name: 'OB.UI.OrderView',
  classes: 'obUiOrderView',
  published: {
    order: null
  },
  events: {
    onReceiptLineSelected: '',
    onRenderPaymentLine: '',
    onAdjustOrderCaption: ''
  },
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxBehavior',
    onAllTicketLinesChecked: 'allTicketLinesChecked',
    onToggleSelectionTable: 'toggleSelectionTable',
    onMultiSelectAllTable: 'multiSelectAllTable',
    onTableMultiSelectedItems: 'tableMultiSelectedItems'
  },
  processesToListen: ['calculateReceipt'],
  processStarted: function() {},
  processFinished: function(process, execution, processesInExec) {
    var removedServices = [],
      servicesToBeDeleted = [];
    removedServices.push(OB.I18N.getLabel('OBPOS_ServiceRemoved'));
    _.each(
      OB.MobileApp.model.receipt.get('lines').models,
      function(line) {
        var trancheValues = [],
          totalAmountSelected = 0,
          minimumSelected = Infinity,
          maximumSelected = 0,
          uniqueQuantityServiceToBeDeleted,
          asPerProductServiceToBeDeleted;

        if (line.get('obposIsDeleted')) {
          return;
        }

        if (line.has('relatedLines') && line.get('relatedLines').length > 0) {
          _.each(
            line.get('relatedLines'),
            function(line2) {
              if (
                !line2.deferred &&
                !line.get('originalOrderLineId') &&
                OB.MobileApp.model.receipt.get('lines').get(line2.orderlineId)
              ) {
                line2 = OB.MobileApp.model.receipt
                  .get('lines')
                  .get(line2.orderlineId).attributes;
              }
              trancheValues = OB.UI.SearchServicesFilter.prototype.calculateTranche(
                line2,
                trancheValues
              );
            },
            this
          );
          totalAmountSelected = trancheValues[0];
          minimumSelected = trancheValues[1];
          maximumSelected = trancheValues[2];
          uniqueQuantityServiceToBeDeleted =
            line.get('product').get('quantityRule') === 'UQ' &&
            ((line.has('serviceTrancheMaximum') &&
              totalAmountSelected > line.get('serviceTrancheMaximum')) ||
              (line.has('serviceTrancheMinimum') &&
                totalAmountSelected < line.get('serviceTrancheMinimum')));
          asPerProductServiceToBeDeleted =
            line.get('product').get('quantityRule') === 'PP' &&
            ((line.has('serviceTrancheMaximum') &&
              maximumSelected > line.get('serviceTrancheMaximum')) ||
              (line.has('serviceTrancheMinimum') &&
                minimumSelected < line.get('serviceTrancheMinimum')));
          if (
            (!line.has('deliveredQuantity') ||
              line.get('deliveredQuantity') <= 0) &&
            (uniqueQuantityServiceToBeDeleted || asPerProductServiceToBeDeleted)
          ) {
            servicesToBeDeleted.push(line);
            removedServices.push(line.get('product').get('_identifier'));
          }
        }
      },
      this
    );
    if (servicesToBeDeleted.length > 0) {
      OB.MobileApp.model.receipt.deleteLinesFromOrder(servicesToBeDeleted);
      OB.MobileApp.model.receipt.set('undo', null);
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_ServiceRemovedHeader'),
        removedServices
      );
    }
  },
  components: [
    {
      kind: 'OB.UI.ScrollableTable',
      name: 'listOrderLines',
      classes: 'obUiOrderView-listOrderLines',
      columns: ['product', 'quantity', 'price', 'gross'],
      scrollWhenSelected: true,
      renderLine: 'OB.UI.RenderOrderLine',
      renderEmpty: 'OB.UI.RenderOrderLineEmpty',
      //defined on redenderorderline.js
      listStyle: 'edit',
      isSelectableLine: function(model) {
        if (
          !OB.UTIL.isNullOrUndefined(model) &&
          !OB.UTIL.isNullOrUndefined(model.attributes) &&
          !model.attributes.isEditable
        ) {
          return false;
        }
        return true;
      }
    },
    {
      kind: 'Scroller',
      thumb: true,
      classes: 'obUiOrderView-totalAndBreakdowns',
      name: 'totalAndBreakdowns',
      components: [
        {
          tag: 'ul',
          name: 'list',
          classes: 'obUiOrderView-totalAndBreakdowns-list',
          components: [
            {
              tag: 'li',
              classes: 'obUiOrderView-totalAndBreakdowns-list-row1',
              components: [
                {
                  classes:
                    'obUiOrderView-totalAndBreakdowns-list-row1-totalTaxLine',
                  kind: 'OB.UI.TotalTaxLine',
                  name: 'totalTaxLine'
                },
                {
                  classes:
                    'obUiOrderView-totalAndBreakdowns-list-row1-totalReceiptLine',
                  kind: 'OB.UI.TotalReceiptLine',
                  name: 'totalReceiptLine'
                }
              ]
            },
            {
              tag: 'li',
              classes: 'obUiOrderView-totalAndBreakdowns-list-row2',
              components: [
                {
                  classes:
                    'obUiOrderView-totalAndBreakdowns-list-row2-injectedFooter',
                  name: 'injectedFooter'
                },
                {
                  classes: 'obUiOrderView-totalAndBreakdowns-list-row2-status',
                  components: [
                    {
                      kind: 'OB.UI.BtnReceiptToInvoice',
                      classes:
                        'obUiOrderView-totalAndBreakdowns-list-row2-status-divbtninvoice',
                      name: 'divbtninvoice',
                      showing: false
                    },
                    {
                      kind: 'OB.UI.OrderViewDivText',
                      classes:
                        'obUiOrderView-totalAndBreakdowns-list-row2-status-divText',
                      name: 'divText'
                    }
                  ]
                }
              ]
            },
            {
              tag: 'li',
              classes: 'obUiOrderView-totalAndBreakdowns-list-row3',
              components: [
                {
                  kind: 'OB.UI.TaxBreakdown',
                  classes:
                    'obUiOrderView-totalAndBreakdowns-list-row3-taxBreakdown',
                  name: 'taxBreakdown'
                }
              ]
            },
            {
              kind: 'OB.UI.Table',
              classes: 'obUiOrderView-totalAndBreakdowns-list-listTaxLines',
              name: 'listTaxLines',
              renderLine: 'OB.UI.RenderTaxLine',
              renderEmpty: 'OB.UI.RenderTaxLineEmpty',
              //defined on redenderorderline.js
              listStyle: 'nonselectablelist',
              columns: ['tax', 'base', 'totaltax']
            },
            {
              tag: 'li',
              classes: 'obUiOrderView-totalAndBreakdowns-list-row5',
              components: [
                {
                  name: 'paymentBreakdown',
                  classes:
                    'obUiOrderView-totalAndBreakdowns-list-row5-paymentBreakdown',
                  showing: false,
                  components: [
                    {
                      classes:
                        'obUiOrderView-paymentBreakdown-list-lblTotalPayment',
                      name: 'lblTotalPayment'
                    }
                  ]
                }
              ]
            },
            {
              kind: 'OB.UI.Table',
              classes: 'obUiOrderView-totalAndBreakdowns-list-listPaymentLines',
              name: 'listPaymentLines',
              showing: false,
              renderLine: 'OB.UI.RenderPaymentLine',
              renderEmpty: 'OB.UI.RenderPaymentLineEmpty',
              //defined on redenderorderline.js
              listStyle: 'nonselectablelist'
            }
          ]
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    if (!OB.MobileApp.model.get('terminal').terminalType.showtaxbreakdown) {
      this.$.listOrderLines.addClass('obUiOrderView-listOrderLines-bigger');
    }
    this.$.lblTotalPayment.setContent(
      OB.I18N.getLabel('OBPOS_LblPaymentBreakdown')
    );

    // Inject the footer components
    var prop;
    for (prop in OB.POS.ORDERFOOTER) {
      if (OB.POS.ORDERFOOTER.hasOwnProperty(prop)) {
        this.$.injectedFooter
          .createComponent({
            kind: OB.POS.ORDERFOOTER[prop],
            name: prop
          })
          .render();
      }
    }
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  },
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  },
  checkBoxBehavior: function(inSender, inEvent) {
    if (inEvent.status) {
      this.$.listOrderLines.setListStyle('checkboxlist');
    } else {
      this.$.listOrderLines.setListStyle('edit');
    }
  },
  allTicketLinesChecked: function(inSender, inEvent) {
    if (inEvent.status) {
      this.order.get('lines').trigger('checkAll');
    } else {
      this.order.get('lines').trigger('unCheckAll');
    }
  },
  setTaxes: function() {
    if (OB.UTIL.isNullOrUndefined(OB.MobileApp.model.get('terminal'))) {
      return;
    }
    if (OB.MobileApp.model.get('terminal').terminalType.showtaxbreakdown) {
      var taxList = new Backbone.Collection(),
        taxes = this.order.get('taxes'),
        remainingTaxesIds = [];

      _.each(this.order.get('lines').models, function(line) {
        if (!line.get('obposIsDeleted') && line.get('taxLines')) {
          remainingTaxesIds.push(...Object.keys(line.get('taxLines')));
        }
      });
      Object.keys(taxes).forEach(function(id) {
        if (remainingTaxesIds.includes(id)) {
          taxList.add(new OB.Model.TaxLine(taxes[id]));
        }
      });

      if (taxList.length === 0) {
        this.$.taxBreakdown.hide();
      } else {
        this.$.taxBreakdown.show();
      }

      taxList.models = _.sortBy(taxList.models, function(taxLine) {
        return taxLine.get('name');
      });

      this.$.listTaxLines.setCollection(taxList);
    } else {
      this.$.taxBreakdown.hide();
    }
  },
  toggleSelectionTable: function(inSender, inEvent) {
    this.$.listOrderLines.setSelectionMode(
      inEvent.multiselection ? 'multiple' : 'single'
    );
  },
  multiSelectAllTable: function() {
    this.$.listOrderLines.selectAll();
    this.doReceiptLineSelected();
  },
  tableMultiSelectedItems: function(inSender, inEvent) {
    this.$.listOrderLines.setSelectedModels(inEvent.selection);
  },
  orderChanged: function(oldValue) {
    var me = this;
    this.$.totalReceiptLine.renderTotal(this.order.getTotal());
    this.$.totalReceiptLine.renderQty(this.order.getQty());
    this.$.totalTaxLine.renderTax(
      OB.DEC.sub(this.order.getTotal(), this.order.getNet())
    );
    this.$.listOrderLines.setCollection(this.order.get('lines'));
    this.$.listPaymentLines.setCollection(this.order.get('payments'));
    this.setTaxes();
    this.order.on(
      'change:isNegative',
      function(model) {
        if (model.get('doCancelAndReplace')) {
          // Render the payments because it's possible that the amount must be shown with another
          // sign (depends on the gross and the isNegative properties)
          this.$.listPaymentLines.waterfall('onRenderPaymentLine');
        }
      },
      this
    );
    this.order.on(
      'change:gross change:net',
      function(model) {
        this.$.totalReceiptLine.renderTotal(model.getTotal());
        this.$.totalTaxLine.renderTax(
          OB.DEC.sub(model.getTotal(), model.getNet())
        );
      },
      this
    );
    this.order.on(
      'paintTaxes',
      function() {
        this.setTaxes();
      },
      this
    );
    this.order.on(
      'change:priceIncludesTax ',
      function(model) {
        if (this.order.get('priceIncludesTax')) {
          this.$.totalTaxLine.hide();
        } else {
          this.$.totalTaxLine.show();
        }
      },
      this
    );
    this.order.on(
      'change:qty',
      function(model) {
        this.$.totalReceiptLine.renderQty(model.getQty());
      },
      this
    );
    this.order.on(
      'change:generateInvoice',
      function(model) {
        if (model.get('generateInvoice')) {
          this.$.divbtninvoice.show();
        } else {
          this.$.divbtninvoice.hide();
        }
      },
      this
    );
    this.order.on(
      'change:hasbeenpaid',
      function(model) {
        this.$.divText.changeHasbeenpaid(model);
      },
      this
    );
    this.order.on(
      'change:isPaid change:isLayaway change:isQuotation change:documentNo change:orderType change:doCancelAndReplace change:cancelLayaway change:replacedorder_documentNo change:paidOnCredit change:paidPartiallyOnCredit change:fromLayaway change:documentType change:iscancelled',
      function(model) {
        // Unified the logic to show/hide the 'divText', the 'listPaymentLines' and the 'paymentBreakdown' panels
        if (model.get('doCancelAndReplace')) {
          // Set the label for C&R
          this.$.divText.setCancelAndReplaceLabel(model);
        } else if (model.get('cancelLayaway')) {
          // Set the label for CL
          this.$.divText.setCancelLayawayLabel(model);
        } else if (model.get('isQuotation')) {
          // Set the label for quotations
          this.$.divText.setQuotationLabel(model);
        } else if (model.get('isLayaway')) {
          // Set the label for layaways
          this.$.divText.setLayawayLabel(model);
        } else if (model.get('isPaid')) {
          // Set the label for paid receipts (also on credit and canceled)
          this.$.divText.setPaidLabel(model);
        } else {
          if (model.get('orderType') === 1) {
            // Set the label for draft returns
            this.$.divText.setToBeReturnedLabel(model);
          } else if (model.get('orderType') === 2) {
            // Set the label for draft layaways
            this.$.divText.setToBeLaidawayLabel(model);
          } else {
            this.$.divText.hide();
          }
        }

        // Set the 'New receipt'/'New quotation' labels when converting to a quotation or receipt
        if (!_.isUndefined(model.changed.isQuotation)) {
          if (model.get('isQuotation')) {
            this.$.listOrderLines.children[4].children[0].setContent(
              OB.I18N.getLabel('OBPOS_QuotationNew')
            );
          } else {
            this.$.listOrderLines.children[4].children[0].setContent(
              OB.I18N.getLabel('OBPOS_ReceiptNew')
            );
          }
        }

        // Show the payment list only in synchronized tickets and in C&R
        if (
          (model.get('isLayaway') ||
            model.get('isPaid') ||
            model.get('doCancelAndReplace')) &&
          model.getPayment() > 0
        ) {
          this.$.listPaymentLines.show();
          this.$.paymentBreakdown.show();
        } else {
          this.$.listPaymentLines.hide();
          this.$.paymentBreakdown.hide();
        }
      },
      this
    );
    this.order.get('lines').on(
      'add remove reset',
      function() {
        this.doAdjustOrderCaption();
      },
      this
    );
    this.order.get('lines').on(
      'updatedView',
      function() {
        this.$.listOrderLines.setScrollAfterAdd();
      },
      this
    );
    // Change Document No based on return lines
    this.order.get('lines').on(
      'add change:qty change:relatedLines updateRelations',
      function() {
        if (
          this.order.get('isEditable') &&
          !this.order.get('isModified') &&
          !this.order.get('isLayaway') &&
          !this.order.get('isQuotation') &&
          !this.order.get('doCancelAndReplace') &&
          !this.order.get('cancelLayaway')
        ) {
          var negativeLinesLength = _.filter(
            this.order.get('lines').models,
            function(line) {
              return line.get('qty') < 0;
            }
          ).length;
          if (
            (negativeLinesLength > 0 &&
              negativeLinesLength === this.order.get('lines').models.length) ||
            (negativeLinesLength > 0 &&
              OB.MobileApp.model.get('permissions')
                .OBPOS_SalesWithOneLineNegativeAsReturns)
          ) {
            //isReturn
            this.order.setDocumentNo(true, false);
          } else {
            //isOrder
            this.order.setDocumentNo(false, true);
          }
        }
      },
      this
    );

    this.order.get('lines').on(
      'add change:qty change:relatedLines updateRelations',
      function() {
        var approvalNeeded = false,
          linesToRemove = [],
          servicesToApprove = '',
          line,
          k,
          oldUndo = this.order.get('undo');

        if (
          !this.order.get('hasServices') ||
          this.updating ||
          this.order.get('preventServicesUpdate') ||
          !this.order.get('isEditable')
        ) {
          return;
        }
        this.updating = true;

        function getServiceLines(service) {
          var serviceLines;
          if (service.get('groupService')) {
            serviceLines = _.filter(me.order.get('lines').models, function(l) {
              return (
                l.get('product').get('id') ===
                  service.get('product').get('id') &&
                !l.get('originalOrderLineId')
              );
            });
          }
          serviceLines = [service];
          return serviceLines;
        }

        function filterLines(newRelatedLines, lines) {
          return _.filter(newRelatedLines, function(rl) {
            return _.indexOf(_.pluck(lines, 'id'), rl.orderlineId) !== -1;
          });
        }

        function getSiblingServicesLines(productId, orderlineId) {
          var serviceLines = _.filter(me.order.get('lines').models, function(
            l
          ) {
            return (
              l.has('relatedLines') &&
              l.get('relatedLines').length > 0 &&
              !l.get('originalOrderLineId') && //
              l.get('product').id === productId &&
              l.get('relatedLines')[0].orderlineId === orderlineId
            );
          });
          return serviceLines;
        }

        function adjustNotGroupedServices(line, qty) {
          if (
            line.get('product').get('quantityRule') === 'PP' &&
            !line.get('groupService')
          ) {
            var qtyService = OB.DEC.abs(qty),
              qtyLineServ = qty > 0 ? 1 : -1;

            // Split/Remove services lines
            var siblingServicesLines = getSiblingServicesLines(
              line.get('product').id,
              line.get('relatedLines')[0].orderlineId
            );
            if (
              !me.order.get('deleting') &&
              siblingServicesLines.length < qtyService
            ) {
              var i, p, newLine;
              for (i = 0; i < qtyService - siblingServicesLines.length; i++) {
                p = line.get('product').clone();
                p.set('groupProduct', false);
                newLine = me.order.createLine(p, qtyLineServ);
                newLine.set(
                  'relatedLines',
                  siblingServicesLines[0].get('relatedLines')
                );
                newLine.set('groupService', false);
              }
            } else if (siblingServicesLines.length > qtyService) {
              linesToRemove = OB.UTIL.mergeArrays(
                linesToRemove,
                _.initial(siblingServicesLines, qtyService)
              );
            }

            return qtyLineServ;
          }
          return qty;
        }

        if (!this.order.get('notApprove')) {
          // First check if there is any service modified to negative quantity amount in order to know if approval will be required
          var prod,
            i,
            j,
            l,
            newqtyplus,
            newqtyminus,
            serviceLines,
            positiveLines,
            negativeLines,
            newRelatedLines;
          for (k = 0; k < this.order.get('lines').length; k++) {
            line = this.order.get('lines').models[k];
            prod = line.get('product');
            newqtyplus = 0;
            newqtyminus = 0;
            serviceLines = [];
            positiveLines = [];
            negativeLines = [];
            newRelatedLines = [];

            if (
              line.has('relatedLines') &&
              line.get('relatedLines').length > 0 &&
              !line.get('originalOrderLineId')
            ) {
              serviceLines = getServiceLines(line);

              for (i = 0; i < serviceLines.length; i++) {
                newRelatedLines = OB.UTIL.mergeArrays(
                  newRelatedLines,
                  serviceLines[i].get('relatedLines') || []
                );
              }
              for (j = 0; j < newRelatedLines.length; j++) {
                l = me.order.get('lines').get(newRelatedLines[j].orderlineId);
                if (l && l.get('qty') > 0) {
                  newqtyplus += l.get('qty');
                  positiveLines.push(l);
                } else if (l && l.get('qty') < 0) {
                  newqtyminus += l.get('qty');
                  negativeLines.push(l);
                }
              }

              if (prod.get('quantityRule') === 'UQ') {
                newqtyplus = newqtyplus ? 1 : 0;
                newqtyminus = newqtyminus ? -1 : 0;
              }

              for (i = 0; i < serviceLines.length; i++) {
                l = serviceLines[i];
                if (
                  l.get('qty') > 0 &&
                  serviceLines.length === 1 &&
                  newqtyminus
                ) {
                  if (!l.get('product').get('returnable')) {
                    // Cannot add not returnable service to a negative product
                    me.order.get('lines').remove(l);
                    OB.UTIL.showConfirmation.display(
                      OB.I18N.getLabel('OBPOS_UnreturnableProduct'),
                      OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [
                        l.get('product').get('_identifier')
                      ])
                    );
                    this.updating = false;
                    return;
                  }
                  if (!approvalNeeded) {
                    approvalNeeded = true;
                  }
                  servicesToApprove +=
                    '<br>' +
                    OB.I18N.getLabel('OBMOBC_Character')[1] +
                    ' ' +
                    line.get('product').get('_identifier');
                }
              }
            }
          }
        }

        function fixServiceOrderLines(approved) {
          linesToRemove = [];
          me.order.get('lines').forEach(function(line) {
            var prod = line.get('product'),
              newLine,
              i,
              j,
              l,
              rlp,
              rln,
              deferredLines,
              deferredQty,
              notDeferredRelatedLines,
              positiveLine,
              newqtyplus = 0,
              newqtyminus = 0,
              serviceLines = [],
              positiveLines = [],
              negativeLines = [],
              newRelatedLines = [];

            if (
              line.has('relatedLines') &&
              line.get('relatedLines').length > 0 &&
              !line.get('originalOrderLineId')
            ) {
              serviceLines = getServiceLines(line);

              for (i = 0; i < serviceLines.length; i++) {
                newRelatedLines = OB.UTIL.mergeArrays(
                  newRelatedLines,
                  serviceLines[i].get('relatedLines') || []
                );
              }
              for (j = 0; j < newRelatedLines.length; j++) {
                l = me.order.get('lines').get(newRelatedLines[j].orderlineId);
                if (l && l.get('qty') > 0) {
                  newqtyplus += l.get('qty');
                  positiveLines.push(l);
                } else if (l && l.get('qty') < 0) {
                  newqtyminus += l.get('qty');
                  negativeLines.push(l);
                }
              }
              rlp = filterLines(newRelatedLines, positiveLines);

              rln = filterLines(newRelatedLines, negativeLines);

              if (prod.get('quantityRule') === 'UQ') {
                newqtyplus = newqtyplus ? 1 : 0;
                newqtyminus = newqtyminus ? -1 : 0;
              }

              serviceLines.forEach(function(l) {
                if (l.get('qty') > 0) {
                  if (serviceLines.length === 1 && newqtyminus && newqtyplus) {
                    deferredLines = l
                      .get('relatedLines')
                      .filter(function getDeferredServices(relatedLine) {
                        return relatedLine.deferred === true;
                      });
                    if (deferredLines) {
                      deferredQty = 0;
                      if (line.get('product').get('quantityRule') === 'PP') {
                        _.each(deferredLines, function(deferredLine) {
                          deferredQty += deferredLine.qty;
                        });
                      }
                      rlp = OB.UTIL.mergeArrays(rlp, deferredLines || []);
                      newqtyplus += deferredQty;
                    }
                    newLine = me.order.createLine(prod, newqtyminus);
                    newLine.set('relatedLines', rln);
                    newLine.set(
                      'groupService',
                      newLine.get('product').get('groupProduct')
                    );
                    l.set('relatedLines', rlp);
                    l.set('qty', newqtyplus);
                  } else if (serviceLines.length === 1 && newqtyminus) {
                    if (approved) {
                      deferredLines = l
                        .get('relatedLines')
                        .filter(function getDeferredServices(relatedLine) {
                          return relatedLine.deferred === true;
                        });
                      if (deferredLines.length) {
                        deferredQty = 0;
                        if (line.get('product').get('quantityRule') === 'PP') {
                          _.each(deferredLines, function(deferredLine) {
                            deferredQty += deferredLine.qty;
                          });
                        } else {
                          deferredQty = 1;
                        }
                        newLine = me.order.createLine(prod, deferredQty);
                        newLine.set('relatedLines', deferredLines);
                        newLine.set('qty', deferredQty);
                      }
                      l.set('relatedLines', rln);
                      newqtyminus = adjustNotGroupedServices(
                        l,
                        newqtyminus,
                        linesToRemove
                      );
                      l.set('qty', newqtyminus);
                    } else {
                      linesToRemove.push(l);
                    }
                  } else if (newqtyplus && !me.positiveLineUpdated) {
                    me.positiveLineUpdated = true;
                    deferredLines = l
                      .get('relatedLines')
                      .filter(function getDeferredServices(relatedLine) {
                        return relatedLine.deferred === true;
                      });
                    rlp = OB.UTIL.mergeArrays(rlp, deferredLines || []);
                    l.set('relatedLines', rlp);
                    if (line.get('product').get('quantityRule') === 'PP') {
                      if (line.get('groupService')) {
                        _.each(deferredLines, function(deferredLine) {
                          newqtyplus += deferredLine.qty;
                        });
                      } else {
                        newqtyplus = adjustNotGroupedServices(
                          line,
                          newqtyplus,
                          linesToRemove
                        );
                      }
                    }
                    l.set('qty', newqtyplus);
                  } else if (
                    newqtyplus &&
                    newqtyminus &&
                    me.positiveLineUpdated
                  ) {
                    newLine = me.order.createLine(prod, newqtyminus);
                    newLine.set('relatedLines', rln);
                    newLine.set(
                      'groupService',
                      newLine.get('product').get('groupProduct')
                    );
                    me.order.get('lines').remove(l);
                  } else {
                    deferredLines = l
                      .get('relatedLines')
                      .filter(function getDeferredServices(relatedLine) {
                        return relatedLine.deferred === true;
                      });
                    if (!deferredLines.length) {
                      me.order.get('lines').remove(l);
                    } else {
                      deferredQty = 0;
                      if (
                        line.get('product').get('quantityRule') === 'PP' &&
                        line.get('product').get('groupProduct')
                      ) {
                        _.each(deferredLines, function(deferredLine) {
                          deferredQty += deferredLine.qty;
                        });
                      } else {
                        deferredQty = 1;
                      }
                      l.set('relatedLines', deferredLines);
                      l.set('qty', deferredQty);
                    }
                  }
                } else {
                  if (serviceLines.length === 1 && newqtyminus && newqtyplus) {
                    newLine = me.order.createLine(prod, newqtyplus);
                    newLine.set('relatedLines', rlp);
                    l.set('relatedLines', rln);
                    l.set('qty', newqtyminus);
                  } else if (serviceLines.length === 1 && newqtyplus) {
                    l.set('relatedLines', rlp);
                    newqtyplus = adjustNotGroupedServices(
                      l,
                      newqtyplus,
                      linesToRemove
                    );
                    l.set('qty', newqtyplus);
                  } else if (newqtyminus && !me.negativeLineUpdated) {
                    me.negativeLineUpdated = true;
                    l.set('relatedLines', rln);
                    newqtyminus = adjustNotGroupedServices(
                      l,
                      newqtyminus,
                      linesToRemove
                    );
                    l.set('qty', newqtyminus);
                  } else if (
                    newqtyplus &&
                    newqtyminus &&
                    me.negativeLineUpdated
                  ) {
                    positiveLine = me.order
                      .get('lines')
                      .filter(function getLine(currentLine) {
                        return (
                          currentLine.get('product').id ===
                            l.get('product').id && currentLine.get('qty') > 0
                        );
                      });
                    if (positiveLine) {
                      deferredLines = l
                        .get('relatedLines')
                        .filter(function getDeferredServices(relatedLine) {
                          return relatedLine.deferred === true;
                        });
                      rlp = OB.UTIL.mergeArrays(rlp, deferredLines || []);
                      positiveLine.set('relatedLines', rlp);
                      positiveLine.set('qty', newqtyplus);
                    } else {
                      newLine = me.order.createLine(prod, newqtyplus);
                      newLine.set('relatedLines', rlp);
                    }
                    me.order.get('lines').remove(l);
                  } else {
                    deferredLines = l
                      .get('relatedLines')
                      .filter(function getDeferredServices(relatedLine) {
                        return relatedLine.deferred === true;
                      });
                    if (!deferredLines.length && !l.get('obposIsDeleted')) {
                      me.order.get('lines').remove(l);
                    }
                  }
                }
              });
              me.positiveLineUpdated = false;
              me.negativeLineUpdated = false;

              notDeferredRelatedLines = line
                .get('relatedLines')
                .filter(function getNotDeferredLines(rl) {
                  if (OB.UTIL.isNullOrUndefined(rl.deferred)) {
                    return false;
                  }
                  return !rl.deferred;
                });
              if (
                !line.get('groupService') &&
                notDeferredRelatedLines.length > 1
              ) {
                notDeferredRelatedLines.forEach(function(rl) {
                  newLine = me.order.createLine(
                    prod,
                    me.order
                      .get('lines')
                      .get(rl.orderlineId)
                      .get('qty')
                  );
                  newLine.set('relatedLines', [rl]);
                  newLine.set('groupService', false);
                });
                me.order.get('lines').remove(line);
              }
            }
          });
          linesToRemove.forEach(function(l) {
            me.order.get('lines').remove(l);
            OB.UTIL.showWarning(
              OB.I18N.getLabel('OBPOS_DeletedService', [
                l.get('product').get('_identifier')
              ])
            );
          });
          me.order.setUndo('FixOrderLines', oldUndo);
          me.updating = false;
          me.order.trigger('updateServicePrices');
        }

        if (approvalNeeded) {
          OB.UTIL.Approval.requestApproval(
            OB.MobileApp.view.$.containerWindow.getRoot().model,
            [
              {
                approval: 'OBPOS_approval.returnService',
                message: 'OBPOS_approval.returnService',
                params: [servicesToApprove]
              }
            ],
            function(approved, supervisor, approvalType) {
              if (approved) {
                fixServiceOrderLines(true);
              } else {
                fixServiceOrderLines(false);
              }
            }
          );
        } else {
          fixServiceOrderLines(true);
        }
      },
      this
    );
    this.order.get('lines').on(
      'add change:obrdmDeliveryMode',
      function(line) {
        if (line.get('obrdmDeliveryMode')) {
          line.set(
            'nameDelivery',
            _.find(OB.MobileApp.model.get('deliveryModes'), function(dm) {
              return dm.id === line.get('obrdmDeliveryMode');
            }).name
          );
        }
      },
      this
    );
    this.order.on(
      'calculatedReceipt updateServicePrices',
      function() {
        var me = this,
          setPriceCallback,
          changePriceCallback,
          handleError,
          serviceLines,
          i;

        if (
          !this.order.get('hasServices') ||
          this.updating ||
          this.order.get('preventServicesUpdate') ||
          !this.order.get('isEditable') ||
          (this.order.get('isQuotation') &&
            this.order.get('hasbeenpaid') === 'Y') ||
          OB.UTIL.ProcessController.isProcessActive('calculateReceipt')
        ) {
          return;
        }

        setPriceCallback = function(line, newprice, priceChanged) {
          OB.UTIL.HookManager.executeHooks(
            'OBPOS_ServicePriceRules_PreSetPriceToLine',
            {
              newprice: newprice,
              line: line,
              priceChanged: priceChanged
            },
            function(args) {
              if (args.newprice !== line.get('price')) {
                me.order.setPrice(args.line, args.newprice, {
                  setUndo: false
                });
              }
            }
          );
        };

        changePriceCallback = function(line, newprice) {
          setPriceCallback(line, newprice, true);
        };

        handleError = function(line, message) {
          if (OB.MobileApp.view.openedPopup === null) {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_ErrorGettingServicePrice'),
              OB.I18N.getLabel(message, [
                line.get('product').get('_identifier')
              ]),
              [
                {
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  isConfirmButton: true
                }
              ],
              {
                onHideFunction: function() {
                  me.order.get('lines').remove(line);
                  me.order.set('undo', null);
                  me.$.totalReceiptLine.renderQty();
                }
              }
            );
          }
        };

        serviceLines = this.order.get('lines').filter(function(l) {
          return l.get('product').get('productType') === 'S';
        });

        for (i = 0; i < serviceLines.length; i++) {
          var line = serviceLines[i];
          if (line.get('product').get('isPriceRuleBased')) {
            OB.UTIL.getCalculatedPriceForService(
              line,
              line.get('product'),
              line.get('relatedLines'),
              line.get('qty'),
              changePriceCallback,
              handleError
            );
          } else {
            setPriceCallback(line, line.get('price'), false);
          }
        }
      },
      this
    );
    this.order.on(
      'change:selectedPayment',
      function(model) {
        OB.UTIL.HookManager.executeHooks('OBPOS_PaymentSelected', {
          order: this.order,
          paymentSelected:
            OB.MobileApp.model.paymentnames[model.get('selectedPayment')]
        });
      },
      this
    );
  }
});
enyo.kind({
  name: 'OB.UI.MultiOrderView',
  classes: 'obUiMultiOrderView',
  published: {
    order: null
  },
  events: {
    onChangeTotal: ''
  },
  components: [
    {
      kind: 'OB.UI.ScrollableTable',
      classes: 'obUiMultiOrderView-listMultiOrderLines',
      name: 'listMultiOrderLines',
      scrollAreaClasses: 'obUiMultiOrderView-listMultiOrderLines-scrollArea',
      renderLine: 'OB.UI.RenderMultiOrdersLine',
      renderEmpty: 'OB.UI.RenderMultiOrdersLineEmpty',
      //defined on redenderorderline.js
      listStyle: 'edit'
    },
    {
      tag: 'ul',
      classes: 'obUiMultiOrderView-totalAndBreakdowns',
      components: [
        {
          classes: 'obUiMultiOrderView-totalAndBreakdowns-row1',
          tag: 'li',
          components: [
            {
              classes:
                'obUiMultiOrderView-totalAndBreakdowns-row1-totalMultiReceiptLine',
              kind: 'OB.UI.TotalMultiReceiptLine',
              name: 'totalMultiReceiptLine'
            }
          ]
        },
        {
          tag: 'li',
          classes: 'obUiMultiOrderView-totalAndBreakdowns-row2',
          components: [
            {
              classes:
                'obUiMultiOrderView-totalAndBreakdowns-row2-multiOrderBtnInvoice',
              kind: 'OB.UI.BtnReceiptToInvoice',
              name: 'multiOrder_btninvoice',
              showing: false
            }
          ]
        }
      ]
    }
  ],
  listMultiOrders: null,
  init: function(model) {
    this.multiOrders = model.get('multiOrders');
    this.orderList = this.multiOrders.get('multiOrdersList');
    this.orderListPayment = this.multiOrders.get('payments');

    this.total = 0;
    this.listMultiOrders = new Backbone.Collection();
    this.$.listMultiOrderLines.setCollection(this.listMultiOrders);

    this.multiOrders.on(
      'change:additionalInfo',
      function(changedModel) {
        this.$.multiOrder_btninvoice.setShowing(
          changedModel.get('additionalInfo') === 'I'
        );
      },
      this
    );
    this.multiOrders.on(
      'change:total',
      function(model) {
        this.doChangeTotal({
          newTotal: model.get('total')
        });
      },
      this
    );
    this.orderList.on(
      'loadedMultiOrder remove amountToLayaway',
      function(callback) {
        var me = this,
          total = OB.DEC.Zero,
          prepayment = OB.DEC.Zero,
          prepaymentLimit = OB.DEC.Zero,
          existingPayment = OB.DEC.Zero,
          amountToLayaway = OB.DEC.Zero;

        var calculatePrepayment = function(idx) {
          if (idx === me.orderList.length) {
            me.total = total;
            me.prepayment = prepayment;
            me.prepaymentLimit = prepaymentLimit;
            me.amountToLayaway = amountToLayaway;
            me.existingPayment = existingPayment;
            me.multiOrders.set('total', me.total);
            me.multiOrders.set('obposPrepaymentamt', me.prepayment);
            me.multiOrders.set('obposPrepaymentlimitamt', me.prepaymentLimit);
            me.multiOrders.set('amountToLayaway', me.amountToLayaway);
            me.multiOrders.set('existingPayment', me.existingPayment);
            me.$.totalMultiReceiptLine.renderTotal(me.total);
            me.listMultiOrders.reset(me.orderList.models);
            if (model.get('leftColumnViewManager').isMultiOrder()) {
              me.doChangeTotal({
                newTotal: me.total
              });
            }
            me.$.totalMultiReceiptLine.renderQty(me.orderList.length);
            if (callback && callback instanceof Function) {
              callback();
            }
            return;
          }
          var order = me.orderList.at(idx);
          order.getPrepaymentAmount(function() {
            if (OB.UTIL.isNullOrUndefined(order.get('amountToLayaway'))) {
              total = OB.DEC.add(total, order.getPending());
            } else {
              total = OB.DEC.add(total, order.get('amountToLayaway'));
              amountToLayaway = OB.DEC.add(
                amountToLayaway,
                order.get('amountToLayaway')
              );
            }
            prepayment = OB.DEC.add(
              prepayment,
              order.get('obposPrepaymentamt') - order.get('payment') > 0
                ? order.get('obposPrepaymentamt') - order.get('payment')
                : 0
            );
            if (
              order.get('amountToLayaway') &&
              order.get('amountToLayaway') < order.getGross()
            ) {
              prepaymentLimit = OB.DEC.add(
                prepaymentLimit,
                order.get('obposPrepaymentlaylimitamt')
              );
            } else {
              prepaymentLimit = OB.DEC.add(
                prepaymentLimit,
                order.get('obposPrepaymentlimitamt')
              );
            }
            existingPayment = OB.DEC.add(existingPayment, order.get('payment'));
            calculatePrepayment(idx + 1);
          }, true);
        };
        calculatePrepayment(0);
      },
      this
    );
    this.multiOrders.on(
      'change:selectedPayment',
      function(model) {
        OB.UTIL.HookManager.executeHooks(
          'OBPOS_PayOpenTicketsPaymentSelected',
          {
            order: OB.MobileApp.view.$.containerWindow
              .getRoot()
              .model.get('multiOrders'),
            paymentSelected:
              OB.MobileApp.model.paymentnames[model.get('selectedPayment')]
          }
        );
      },
      this
    );
    this.orderListPayment.on(
      'add remove',
      function() {
        OB.UTIL.localStorage.setItem(
          'multiOrdersPayment',
          JSON.stringify(this.multiOrders.get('payments').toJSON())
        );
      },
      this
    );
  },
  initComponents: function() {
    this.inherited(arguments);
  },
  destroyComponents: function() {
    this.inherited(arguments);
    if (this.multiOrders) {
      this.multiOrders.off('change:additionalInfo', null, this);
      this.multiOrders.off('change:total', null, this);
    }
    if (this.orderList) {
      this.orderList.off('reset add remove amountToLayaway', null, this);
    }
    if (this.orderListPayment) {
      this.orderListPayment.off('add remove', null, this);
    }
  }
});
