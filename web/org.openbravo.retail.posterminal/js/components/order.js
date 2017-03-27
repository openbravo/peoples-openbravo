/*
 ************************************************************************************
 * Copyright (C) 2013-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _, $ */

enyo.kind({
  name: 'OB.UI.OrderMultiSelect',
  kind: 'Image',
  src: '../org.openbravo.retail.posterminal/img/iconPinSelected.png',
  sizing: "cover",
  width: 28,
  height: 28,
  style: 'float: right; cursor: pointer; margin-top: 8px;',
  showing: false,
  events: {
    onToggleSelection: ''
  },
  published: {
    disabled: false
  },
  tap: function () {
    this.doToggleSelection({
      multiselection: false
    });
  }
});

enyo.kind({
  name: 'OB.UI.OrderSingleSelect',
  kind: 'Image',
  src: '../org.openbravo.retail.posterminal/img/iconPinUnselected.png',
  sizing: "cover",
  width: 28,
  height: 28,
  style: 'float: right; cursor: pointer; margin-top: 8px;',
  events: {
    onToggleSelection: ''
  },
  published: {
    disabled: false
  },
  tap: function () {
    this.doToggleSelection({
      multiselection: true
    });
  }
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.OrderMultiSelectAll',
  i18nContent: 'OBPOS_lblSelectAll',
  classes: 'btnlink-orange',
  style: 'float: right; margin-top: 6px;',
  showing: false,
  events: {
    onMultiSelectAll: ''
  },
  published: {
    disabled: false
  },
  tap: function () {
    this.doMultiSelectAll();
  }
});

enyo.kind({
  name: 'OB.UI.OrderHeader',
  classes: 'row-fluid span12',
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
  newLabelComponents: [{
    kind: 'OB.UI.OrderDetails',
    name: 'orderdetails'
  }, {
    kind: 'OB.UI.OrderMultiSelect',
    name: 'btnMultiSelection'
  }, {
    kind: 'OB.UI.OrderMultiSelectAll',
    name: 'btnMultiSelectAll'
  }, {
    kind: 'OB.UI.OrderSingleSelect',
    name: 'btnSingleSelection'
  }, {
    style: 'clear: both;'
  }],
  newButtonComponents: [{
    kind: 'OB.UI.BusinessPartnerSelector',
    name: 'bpbutton'
  }, {
    name: 'separator',
    classes: 'customer-buttons-separator'
  }, {
    kind: 'OB.UI.BPLocation',
    name: 'bplocbutton'
  }, {
    kind: 'OB.UI.BPLocationShip',
    name: 'bplocshipbutton'
  }],
  style: 'border-bottom: 1px solid #cccccc;',
  components: [{
    name: 'receiptLabels'
  }, {
    name: 'receiptButtons',
    style: 'clear: both; ',
    classes: 'standardFlexContainer'
  }],
  resizeHandler: function () {
    this.inherited(arguments);
    this.setOrderDetailWidth(this.showPin, this.showSelectAll);
  },
  orderChanged: function (oldValue) {
    _.each(this.$.receiptLabels.$, function (comp) {
      if (comp.setOrder) {
        comp.setOrder(this.order);
      }
    }, this);
    _.each(this.$.receiptButtons.$, function (comp) {
      if (comp.setOrder) {
        comp.setOrder(this.order);
      }
    }, this);
  },
  setOrderDetailWidth: function (pin, selectAll) {
    this.showPin = pin;
    this.showSelectAll = selectAll;
    var w = $("#" + this.$.receiptLabels.id).width() - 25;
    if (pin) {
      w = w - $("#" + this.$.receiptLabels.$.btnSingleSelection.id).width() - 20;
    }
    if (selectAll) {
      w = w - $("#" + this.$.receiptLabels.$.btnMultiSelectAll.id).width() - 20;
    }
    $("#" + this.$.receiptLabels.$.orderdetails.id).width(w + 'px');
  },
  showMultiSelected: function (inSender, inEvent) {
    if (inEvent.show) {
      this.$.receiptLabels.$.btnSingleSelection.setShowing(true);
    } else {
      this.$.receiptLabels.$.btnSingleSelection.setShowing(false);
    }
    this.$.receiptLabels.$.btnMultiSelection.setShowing(false);
    this.$.receiptLabels.$.btnMultiSelectAll.setShowing(false);
    this.setOrderDetailWidth(inEvent.show, false);
    this.doToggleSelectionMode({
      multiselection: false
    });
  },
  toggleSelection: function (inSender, inEvent) {
    if (inEvent.multiselection) {
      this.$.receiptLabels.$.btnSingleSelection.setShowing(false);
      this.$.receiptLabels.$.btnMultiSelection.setShowing(true);
      this.$.receiptLabels.$.btnMultiSelectAll.setShowing(true);
    } else {
      this.$.receiptLabels.$.btnSingleSelection.setShowing(true);
      this.$.receiptLabels.$.btnMultiSelection.setShowing(false);
      this.$.receiptLabels.$.btnMultiSelectAll.setShowing(false);
    }
    this.setOrderDetailWidth(true, inEvent.multiselection);
    this.doToggleSelectionMode(inEvent);
  },
  multiSelectAll: function (inSender, inEvent) {
    this.doTableMultiSelectAll();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.showPin = false;
    this.showSelectAll = false;
    enyo.forEach(this.newLabelComponents, function (comp) {
      this.$.receiptLabels.createComponent(comp);
    }, this);
    enyo.forEach(this.newButtonComponents, function (comp) {
      this.$.receiptButtons.createComponent(comp);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.OrderFooter',
  classes: 'row-fluid span12',
  published: {
    order: null
  },
  style: 'border-bottom: 1px solid #cccccc;',
  newComponents: [],
  orderChanged: function () {
    _.each(this.$, function (comp) {
      if (comp.setOrder) {
        comp.setOrder(this.order);
      }
    }, this);
  },
  initComponents: function () {
    this.inherited(arguments);
    enyo.forEach(this.newComponents, function (comp) {
      this.createComponent(comp);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.TotalMultiReceiptLine',
  style: 'position: relative; padding: 10px;',
  components: [{
    name: 'lblTotal',
    style: 'float: left; width: 40%;'
  }, {
    name: 'totalqty',
    style: 'float: left; width: 20%; text-align:right; font-weight:bold;'
  }, {
    name: 'totalgross',
    style: 'float: left; width: 40%; text-align:right; font-weight:bold;'
  }, {
    style: 'clear: both;'
  }],
  renderTotal: function (newTotal) {
    this.$.totalgross.setContent(OB.I18N.formatCurrency(newTotal));
  },
  renderQty: function (newQty) {
    this.$.totalqty.setContent(newQty);
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.lblTotal.setContent(OB.I18N.getLabel('OBPOS_LblTotal'));
  }
});
enyo.kind({
  name: 'OB.UI.TotalReceiptLine',
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxForTicketLines'
  },
  style: 'position: relative; padding: 10px; height: 35px',
  components: [{
    name: 'lblTotal',
    classes: 'order-total-label'
  }, {
    kind: 'OB.UI.FitText',
    classes: 'order-total-qty fitText',
    components: [{
      tag: 'span',
      name: 'totalqty'
    }]
  }, {
    kind: 'OB.UI.FitText',
    classes: 'order-total-gross fitText',
    components: [{
      tag: 'span',
      name: 'totalgross'
    }]
  }, {
    style: 'clear: both;'
  }],
  renderTotal: function (newTotal) {
    if (newTotal !== this.$.totalgross.getContent()) {
      this.$.totalgross.setContent(OB.I18N.formatCurrency(newTotal));
      OB.UTIL.HookManager.executeHooks('OBPOS_UpdateTotalReceiptLine', {
        totalline: this
      });
    }
  },
  renderQty: function (newQty) {
    this.$.totalqty.setContent(newQty);
  },
  checkBoxForTicketLines: function (inSender, inEvent) {
    if (inEvent.status) {
      this.$.lblTotal.hasNode().style.width = '48%';
      this.$.totalqty.hasNode().style.width = '16%';
      this.$.totalgross.hasNode().style.width = '36%';
    } else {
      this.$.lblTotal.hasNode().style.width = '40%';
      this.$.totalqty.hasNode().style.width = '20%';
      this.$.totalgross.hasNode().style.width = '40%';
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.lblTotal.setContent(OB.I18N.getLabel('OBPOS_LblTotal'));
    OB.UTIL.HookManager.executeHooks('OBPOS_RenderTotalReceiptLine', {
      totalline: this
    });
  }
});

enyo.kind({
  name: 'OB.UI.TotalTaxLine',
  style: 'position: relative; padding: 10px;',
  components: [{
    name: 'lblTotalTax',
    style: 'float: left; width: 40%;'
  }, {
    name: 'totalbase',
    style: 'float: left; width: 20%; text-align:right; font-weight:bold;'
  }, {
    name: 'totaltax',
    style: 'float: left; width: 60%; text-align:right; font-weight:bold;'
  }, {
    style: 'clear: both;'
  }],
  renderTax: function (newTax) {
    this.$.totaltax.setContent(OB.I18N.formatCurrency(newTax));
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.lblTotalTax.setContent(OB.I18N.getLabel('OBPOS_LblTotalTax'));
  }
});

enyo.kind({
  name: 'OB.UI.TaxBreakdown',
  style: 'position: relative; padding: 10px;',
  components: [{
    name: 'lblTotalTaxBreakdown',
    style: 'float: left; width: 40%;'
  }, {
    style: 'clear: both;'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.lblTotalTaxBreakdown.setContent(OB.I18N.getLabel('OBPOS_LblTaxBreakdown'));
  }
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BtnReceiptToInvoice',
  events: {
    onCancelReceiptToInvoice: ''
  },
  style: 'width: 40px;',
  classes: 'btnlink-white btnlink-payment-clear btn-icon-small btn-icon-check',
  tap: function () {
    this.doCancelReceiptToInvoice();
  }
});

enyo.kind({
  name: 'btninvoice',
  showing: false,
  style: 'float: left; width: 40%;',
  components: [{
    kind: 'OB.UI.BtnReceiptToInvoice'
  }, {
    tag: 'span',
    content: ' '
  }, {
    tag: 'span',
    name: 'lblInvoiceReceipt',
    style: 'font-weight:bold; '
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.lblInvoiceReceipt.setContent(OB.I18N.getLabel('OBPOS_LblInvoiceReceipt'));
  }
});

enyo.kind({
  name: 'OB.UI.OrderView',
  published: {
    order: null
  },
  events: {
    onReceiptLineSelected: ''
  },
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxBehavior',
    onAllTicketLinesChecked: 'allTicketLinesChecked',
    onToggleSelectionTable: 'toggleSelectionTable',
    onMultiSelectAllTable: 'multiSelectAllTable',
    onTableMultiSelectedItems: 'tableMultiSelectedItems'
  },
  components: [{
    kind: 'OB.UI.ScrollableTable',
    name: 'listOrderLines',
    columns: ['product', 'quantity', 'price', 'gross'],
    renderLine: 'OB.UI.RenderOrderLine',
    renderEmpty: 'OB.UI.RenderOrderLineEmpty',
    //defined on redenderorderline.js
    listStyle: 'edit',
    isSelectableLine: function (model) {
      if (!OB.UTIL.isNullOrUndefined(model) && !OB.UTIL.isNullOrUndefined(model.attributes) && !OB.UTIL.isNullOrUndefined(model.attributes.originalOrderLineId)) {
        return false;
      }
      return true;
    }
  }, {
    tag: 'ul',
    classes: 'unstyled',
    components: [{
      tag: 'li',
      components: [{
        kind: 'OB.UI.TotalTaxLine',
        name: 'totalTaxLine'
      }, {
        kind: 'OB.UI.TotalReceiptLine',
        name: 'totalReceiptLine'
      }]
    }, {
      tag: 'li',
      components: [{
        style: 'padding: 10px; border-top: 1px solid #cccccc; min-height: 40px;',
        components: [{
          kind: 'btninvoice',
          name: 'divbtninvoice',
          showing: false
        }, {
          name: 'divText',
          style: 'float: right; text-align: right; font-weight:bold; font-size: 30px; line-height: 30px;',
          showing: false,
          content: ''
        }, {
          style: 'clear: both;'
        }]
      }]
    }, {
      tag: 'li',
      components: [{
        name: 'taxBreakdownDiv',
        style: 'padding: 10px; border-top: 1px solid #cccccc; height: 40px;',
        components: [{
          kind: 'OB.UI.TaxBreakdown',
          name: 'taxBreakdown'
        }]
      }]
    }, {
      kind: 'OB.UI.ScrollableTable',
      name: 'listTaxLines',
      scrollAreaMaxHeight: '250px',
      renderLine: 'OB.UI.RenderTaxLine',
      renderEmpty: 'OB.UI.RenderTaxLineEmpty',
      //defined on redenderorderline.js
      listStyle: 'nonselectablelist',
      columns: ['tax', 'base', 'totaltax'],
      executeAfterRender: function () {
        if (this.owner.$.listOrderLines.scrollToBottom) {
          this.owner.$.listOrderLines.getScrollArea().scrollToBottom();
        }
        this.owner.$.listOrderLines.scrollToBottom = false;
      }
    }, {
      tag: 'li',
      components: [{
        name: 'paymentBreakdown',
        style: 'padding: 10px; height: 40px;',
        showing: false,
        components: [{
          style: 'position: relative; padding: 10px;',
          components: [{
            name: 'lblTotalPayment',
            style: 'float: left; width: 40%;'
          }, {
            style: 'clear: both;'
          }]
        }]
      }]
    }, {
      kind: 'OB.UI.ScrollableTable',
      style: 'border-bottom: 1px solid #cccccc;',
      name: 'listPaymentLines',
      showing: false,
      scrollAreaMaxHeight: '250px',
      renderLine: 'OB.UI.RenderPaymentLine',
      renderEmpty: 'OB.UI.RenderPaymentLineEmpty',
      //defined on redenderorderline.js
      listStyle: 'nonselectablelist'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    var scrollMax = 250;
    if (!OB.MobileApp.model.get('terminal').terminalType.showtaxbreakdown) {
      scrollMax = scrollMax + 143;
    }
    this.$.listOrderLines.scrollAreaMaxHeight = scrollMax + 'px';
    this.$.lblTotalPayment.setContent(OB.I18N.getLabel('OBPOS_LblPaymentBreakdown'));
  },
  checkBoxBehavior: function (inSender, inEvent) {
    if (inEvent.status) {
      this.$.listOrderLines.setListStyle('checkboxlist');
    } else {
      this.$.listOrderLines.setListStyle('edit');
    }
  },
  allTicketLinesChecked: function (inSender, inEvent) {
    if (inEvent.status) {
      this.order.get('lines').trigger('checkAll');
    } else {
      this.order.get('lines').trigger('unCheckAll');
    }
  },
  setTaxes: function () {
    if (OB.MobileApp.model.get('terminal').terminalType.showtaxbreakdown) {
      var taxList = new Backbone.Collection();
      var taxes = this.order.get('taxes');
      var empty = true,
          prop;

      for (prop in taxes) {
        if (taxes.hasOwnProperty(prop)) {
          taxList.add(new OB.Model.TaxLine(taxes[prop]));
          empty = false;
        }
      }
      if (empty) {
        this.$.taxBreakdown.hide();
      } else {
        this.$.taxBreakdown.show();
      }

      taxList.models = _.sortBy(taxList.models, function (taxLine) {
        return taxLine.get('name');
      });

      this.$.listTaxLines.setCollection(taxList);
    } else {
      this.$.taxBreakdownDiv.hide();
    }
  },
  toggleSelectionTable: function (inSender, inEvent) {
    this.$.listOrderLines.setSelectionMode(inEvent.multiselection ? 'multiple' : 'single');
  },
  multiSelectAllTable: function () {
    this.$.listOrderLines.selectAll();
    this.doReceiptLineSelected();
  },
  tableMultiSelectedItems: function (inSender, inEvent) {
    this.$.listOrderLines.setSelectedModels(inEvent.selection);
  },
  orderChanged: function (oldValue) {
    var me = this;
    this.$.totalReceiptLine.renderTotal(this.order.getTotal());
    this.$.totalReceiptLine.renderQty(this.order.getQty());
    this.$.totalTaxLine.renderTax(OB.DEC.sub(this.order.getTotal(), this.order.getNet()));
    this.$.listOrderLines.setCollection(this.order.get('lines'));
    this.$.listOrderLines.collection.on('add change:qty change:promotions', function (model, list) {
      me.$.listOrderLines.scrollToBottom = false;
      if (me.$.listOrderLines.collection.models.length > 0 && me.$.listOrderLines.collection.models[me.$.listOrderLines.collection.models.length - 1]._changing) {
        me.$.listOrderLines.scrollToBottom = true;
      } else if (list && list.models && list.length > 0 && model.id === list.models[list.length - 1].id) {
        me.$.listOrderLines.scrollToBottom = true;
      }
    });
    this.$.listPaymentLines.setCollection(this.order.get('payments'));
    this.setTaxes();
    this.order.on('change:gross change:net', function (model) {
      if (model.get('orderType') !== 3) {
        this.$.totalReceiptLine.renderTotal(model.getTotal());
        this.$.totalTaxLine.renderTax(OB.DEC.sub(model.getTotal(), model.getNet()));
      }
    }, this);
    this.order.on('paintTaxes', function () {
      if (this.order.get('orderType') !== 3) {
        this.setTaxes();
      }
    }, this);
    this.order.on('change:priceIncludesTax ', function (model) {
      if (this.order.get('priceIncludesTax')) {
        this.$.totalTaxLine.hide();
      } else {
        this.$.totalTaxLine.show();
      }
    }, this);
    this.order.on('change:qty', function (model) {
      this.$.totalReceiptLine.renderQty(model.getQty());
    }, this);
    this.order.on('change:orderType change:documentNo', function (model) {
      if (model.get('orderType') === 1) {
        this.$.divText.addStyles('width: 50%; color: #f8941d;');
        if (model.get('isPaid') !== true) {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_ToBeReturned'));
          this.$.divText.show();
        }
      } else if (model.get('orderType') === 2 && !model.get('replacedorder')) {
        this.$.divText.addStyles('width: 60%; color: lightblue;');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_ToBeLaidaway'));
        this.$.divText.show();
        //We have to ensure that there is not another handler showing this div
      } else if (model.get('orderType') === 2 && model.get('replacedorder')) {
        this.$.divText.addStyles('width: 90%; color: #5353C5; line-height:30px');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_CancelAndReplaceOf', [model.get('replacedorder_documentNo')]));
        this.$.divText.show();
        //We have to ensure that there is not another handler showing this div
      } else if (model.get('orderType') === 3) {
        this.$.divText.addStyles('width: 60%; color: lightblue;');
        if (model.get('cancelLayaway')) {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_CancelLayaway'));
        } else {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_VoidLayaway'));
        }
        this.$.divText.show();
        //We have to ensure that there is not another handler showing this div
      } else if (model.get('isLayaway')) {
        this.$.divText.addStyles('width: 50%; color: lightblue;');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
        this.$.divText.show();
        //We have to ensure that there is not another handler showing this div
      } else if (this.$.divText.content === OB.I18N.getLabel('OBPOS_ToBeReturned') || this.$.divText.content === OB.I18N.getLabel('OBPOS_ToBeLaidaway') || this.$.divText.content === OB.I18N.getLabel('OBPOS_VoidLayaway') || this.$.divText.content === OB.I18N.getLabel('OBPOS_CancelLayaway') || (this.$.divText.content.indexOf(OB.I18N.getLabel('OBPOS_CancelReplace')) !== -1 && !model.get('replacedorder'))) {
        this.$.divText.hide();
      }
    }, this);
    this.order.on('change:generateInvoice', function (model) {
      if (model.get('generateInvoice')) {
        this.$.divbtninvoice.show();
      } else {
        this.$.divbtninvoice.hide();
      }
    }, this);
    this.order.on('change:isQuotation', function (model) {
      if (model.get('isQuotation')) {
        this.$.divText.addStyles('width: 100%; color: #f8941d;');
        this.$.listOrderLines.children[4].children[0].setContent(OB.I18N.getLabel('OBPOS_QuotationNew'));
        if (model.get('hasbeenpaid') === 'Y') {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_QuotationUnderEvaluation'));
        } else {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_QuotationDraft'));
        }
        this.$.divText.show();
      } else {
        this.$.listOrderLines.children[4].children[0].setContent(OB.I18N.getLabel('OBPOS_ReceiptNew'));

        // We have to ensure that there is not another handler showing this div
        if (this.$.divText.content === OB.I18N.getLabel('OBPOS_QuotationUnderEvaluation') || this.$.divText.content === OB.I18N.getLabel('OBPOS_QuotationDraft')) {
          this.$.divText.hide();
        }
      }
    }, this);
    this.order.on('change:hasbeenpaid', function (model) {
      if (model.get('isQuotation') && model.get('hasbeenpaid') === 'Y' && !model.get('obposIsDeleted') && this.$.divText.content && (this.$.divText.content === OB.I18N.getLabel('OBPOS_QuotationNew') || this.$.divText.content === OB.I18N.getLabel('OBPOS_QuotationDraft'))) {
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_QuotationUnderEvaluation'));
      } else if (model.get('isQuotation') && model.get('hasbeenpaid') === 'N' && !model.get('isLayaway')) {
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_QuotationDraft'));
      }
    }, this);
    this.order.on('change:isPaid change:paidOnCredit change:isQuotation change:documentNo change:paidPartiallyOnCredit', function (model) {
      if (model.get('isPaid') === true && !model.get('isQuotation')) {
        this.$.divText.addStyles('width: 50%; color: #f8941d;');
        if (model.get('paidOnCredit')) {
          if (model.get('paidPartiallyOnCredit')) {
            this.$.divText.setContent(OB.I18N.getLabel('OBPOS_paidPartiallyOnCredit', [OB.I18N.formatCurrency(model.get('creditAmount'))]));
          } else {
            this.$.divText.setContent(OB.I18N.getLabel('OBPOS_paidOnCredit'));
          }
        } else if (model.get('documentType') === OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns) {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_paidReturn'));
        } else {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_paid'));
        }
        this.$.divText.show();
        this.$.listPaymentLines.show();
        this.$.paymentBreakdown.show();
        //We have to ensure that there is not another handler showing this div
      } else if (this.$.divText.content === OB.I18N.getLabel('OBPOS_paid') || this.$.divText.content === OB.I18N.getLabel('OBPOS_paidReturn') || this.$.divText.content === OB.I18N.getLabel('OBPOS_paidOnCredit') || this.$.divText.content === OB.I18N.getLabel('OBPOS_paidPartiallyOnCredit', [OB.I18N.formatCurrency(model.get('creditAmount'))]) || (this.$.divText.content.indexOf(OB.I18N.getLabel('OBPOS_CancelReplace')) !== -1 && !model.get('replacedorder'))) {
        this.$.divText.hide();
        this.$.listPaymentLines.hide();
        this.$.paymentBreakdown.hide();
      }
    }, this);
    this.order.on('change:isLayaway', function (model) {
      if (model.get('isLayaway') === true) {
        this.$.divText.addStyles('width: 50%; color: lightblue;');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
        this.$.divText.show();
        this.$.listPaymentLines.show();
        this.$.paymentBreakdown.show();
        //We have to ensure that there is not another handler showing this div
      } else if (this.$.divText.content === OB.I18N.getLabel('OBPOS_LblLayaway')) {
        this.$.divText.hide();
        this.$.listPaymentLines.hide();
        this.$.paymentBreakdown.hide();
      }
    }, this);
    this.order.on('change:replacedorder', function (model) {
      if (model.get('replacedorder')) {
        this.$.divText.addStyles('width: 90%; color: #5353C5; line-height:30px');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_CancelAndReplaceOf', [model.get('replacedorder_documentNo')]));
        this.$.divText.show();
        this.$.listPaymentLines.show();
        this.$.paymentBreakdown.show();
      } else if (model.get('orderType') === 2) {
        this.$.divText.addStyles('width: 60%; color: lightblue;');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_ToBeLaidaway'));
        this.$.divText.show();
        this.$.listPaymentLines.hide();
        this.$.paymentBreakdown.hide();
      } else if (this.$.divText.content.indexOf(OB.I18N.getLabel('OBPOS_CancelReplace')) !== -1) {
        this.$.divText.hide();
        this.$.listPaymentLines.hide();
        this.$.paymentBreakdown.hide();
      }
    }, this);
    this.order.get('lines').on('add change:qty change:relatedLines updateRelations', function () {
      var approvalNeeded = false,
          linesToRemove = [],
          servicesToApprove = '',
          line, k, oldUndo = this.order.get('undo');

      if (this.updating || this.order.get('preventServicesUpdate')) {
        return;
      }
      this.updating = true;
      //Check Return No logic and change
      if (!this.order.get('isLayaway') && !this.order.get('isQuotation') && !this.order.get('doCancelAndReplace')) {
        var negativeLinesLength = _.filter(this.order.get('lines').models, function (line) {
          return line.get('qty') < 0;
        }).length;
        if (negativeLinesLength === this.order.get('lines').models.length && negativeLinesLength > 0) {
          //isReturn
          OB.MobileApp.model.receipt.setDocumentNo(true, false);
        } else {
          //isOrder
          OB.MobileApp.model.receipt.setDocumentNo(false, true);
        }
      }

      function getServiceLines(service) {
        var serviceLines;
        if (service.get('groupService')) {
          serviceLines = _.filter(me.order.get('lines').models, function (l) {
            return (l.get('product').get('id') === service.get('product').get('id')) && !l.get('originalOrderLineId');
          });
        }
        serviceLines = [service];
        return serviceLines;
      }

      function filterLines(newRelatedLines, lines) {
        return _.filter(newRelatedLines, function (rl) {
          return _.indexOf(_.pluck(lines, 'id'), rl.orderlineId) !== -1;
        });
      }

      function getSiblingServicesLines(productId, orderlineId) {
        var serviceLines = _.filter(me.order.get('lines').models, function (l) {
          return l.has('relatedLines') && l.get('relatedLines').length > 0 && !l.get('originalOrderLineId') //
          && l.get('product').id === productId && l.get('relatedLines')[0].orderlineId === orderlineId;
        });
        return serviceLines;
      }

      function adjustNotGroupedServices(line, qty) {
        if (line.get('product').get('quantityRule') === 'PP' && !line.get('groupService')) {
          var qtyService = OB.DEC.abs(qty),
              qtyLineServ = qty > 0 ? 1 : -1;

          // Split/Remove services lines
          var siblingServicesLines = getSiblingServicesLines(line.get('product').id, line.get('relatedLines')[0].orderlineId);
          if (!me.order.get('deleting') && siblingServicesLines.length < qtyService) {
            var i, p, newLine;
            for (i = 0; i < qtyService - siblingServicesLines.length; i++) {
              p = line.get('product').clone();
              p.set('groupProduct', false);
              newLine = me.order.createLine(p, qtyLineServ);
              newLine.set('relatedLines', siblingServicesLines[0].get('relatedLines'));
              newLine.set('groupService', false);
            }
          } else if (siblingServicesLines.length > qtyService) {
            linesToRemove = OB.UTIL.mergeArrays(linesToRemove, _.initial(siblingServicesLines, qtyService));
          }

          return qtyLineServ;
        }
        return qty;
      }

      if (!OB.MobileApp.model.receipt.get('notApprove')) {
        // First check if there is any service modified to negative quantity amount in order to know if approval will be required
        var prod, i, j, l, newqtyplus, newqtyminus, serviceLines, positiveLines, negativeLines, newRelatedLines;
        for (k = 0; k < this.order.get('lines').length; k++) {
          line = this.order.get('lines').models[k];
          prod = line.get('product');
          newqtyplus = 0;
          newqtyminus = 0;
          serviceLines = [];
          positiveLines = [];
          negativeLines = [];
          newRelatedLines = [];

          if (line.has('relatedLines') && line.get('relatedLines').length > 0 && !line.get('originalOrderLineId')) {

            serviceLines = getServiceLines(line);

            for (i = 0; i < serviceLines.length; i++) {
              newRelatedLines = OB.UTIL.mergeArrays(newRelatedLines, (serviceLines[i].get('relatedLines') || []));
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
              newqtyplus = (newqtyplus ? 1 : 0);
              newqtyminus = (newqtyminus ? -1 : 0);
            }

            for (i = 0; i < serviceLines.length; i++) {
              l = serviceLines[i];
              if (l.get('qty') > 0 && serviceLines.length === 1 && newqtyminus) {
                if (!l.get('product').get('returnable')) { // Cannot add not returnable service to a negative product
                  me.order.get('lines').remove(l);
                  OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_UnreturnableProduct'), OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [l.get('product').get('_identifier')]));
                  this.updating = false;
                  return;
                }
                if (!approvalNeeded) {
                  approvalNeeded = true;
                }
                servicesToApprove += '<br>· ' + line.get('product').get('_identifier');
              }
            }
          }
        }
      }

      function fixServiceOrderLines(approved) {
        linesToRemove = [];
        me.order.get('lines').forEach(function (line) {
          var prod = line.get('product'),
              newLine, i, j, l, rlp, rln, deferredLines, deferredQty, notDeferredRelatedLines, positiveLine, newqtyplus = 0,
              newqtyminus = 0,
              serviceLines = [],
              positiveLines = [],
              negativeLines = [],
              newRelatedLines = [];

          if (line.has('relatedLines') && line.get('relatedLines').length > 0 && !line.get('originalOrderLineId')) {

            serviceLines = getServiceLines(line);

            for (i = 0; i < serviceLines.length; i++) {
              newRelatedLines = OB.UTIL.mergeArrays(newRelatedLines, (serviceLines[i].get('relatedLines') || []));
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
              newqtyplus = (newqtyplus ? 1 : 0);
              newqtyminus = (newqtyminus ? -1 : 0);
            }

            serviceLines.forEach(function (l) {
              if (l.get('qty') > 0) {
                if (serviceLines.length === 1 && newqtyminus && newqtyplus) {
                  deferredLines = l.get('relatedLines').filter(function getDeferredServices(relatedLine) {
                    return relatedLine.deferred === true;
                  });
                  if (deferredLines) {
                    deferredQty = 0;
                    if (line.get('product').get('quantityRule') === 'PP') {
                      _.each(deferredLines, function (deferredLine) {
                        deferredQty += deferredLine.qty;
                      });
                    }
                    rlp = OB.UTIL.mergeArrays(rlp, (deferredLines || []));
                    newqtyplus += deferredQty;
                  }
                  newLine = me.order.createLine(prod, newqtyminus);
                  newLine.set('relatedLines', rln);
                  newLine.set('groupService', newLine.get('product').get('groupProduct'));
                  l.set('relatedLines', rlp);
                  l.set('qty', newqtyplus);
                } else if (serviceLines.length === 1 && newqtyminus) {
                  if (approved) {
                    deferredLines = l.get('relatedLines').filter(function getDeferredServices(relatedLine) {
                      return relatedLine.deferred === true;
                    });
                    if (deferredLines.length) {
                      deferredQty = 0;
                      if (line.get('product').get('quantityRule') === 'PP') {
                        _.each(deferredLines, function (deferredLine) {
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
                    newqtyminus = adjustNotGroupedServices(l, newqtyminus, linesToRemove);
                    l.set('qty', newqtyminus);
                  } else {
                    linesToRemove.push(l);
                  }
                } else if (newqtyplus && !me.positiveLineUpdated) {
                  me.positiveLineUpdated = true;
                  deferredLines = l.get('relatedLines').filter(function getDeferredServices(relatedLine) {
                    return relatedLine.deferred === true;
                  });
                  rlp = OB.UTIL.mergeArrays(rlp, (deferredLines || []));
                  l.set('relatedLines', rlp);
                  if (line.get('product').get('quantityRule') === 'PP') {
                    if (line.get('groupService')) {
                      _.each(deferredLines, function (deferredLine) {
                        newqtyplus += deferredLine.qty;
                      });
                    } else {
                      newqtyplus = adjustNotGroupedServices(line, newqtyplus, linesToRemove);
                    }
                  }
                  l.set('qty', newqtyplus);
                } else if (newqtyplus && newqtyminus && me.positiveLineUpdated) {
                  newLine = me.order.createLine(prod, newqtyminus);
                  newLine.set('relatedLines', rln);
                  newLine.set('groupService', newLine.get('product').get('groupProduct'));
                  me.order.get('lines').remove(l);
                } else {
                  deferredLines = l.get('relatedLines').filter(function getDeferredServices(relatedLine) {
                    return relatedLine.deferred === true;
                  });
                  if (!deferredLines.length) {
                    me.order.get('lines').remove(l);
                  } else {
                    deferredQty = 0;
                    if (line.get('product').get('quantityRule') === 'PP') {
                      _.each(deferredLines, function (deferredLine) {
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
                  newqtyplus = adjustNotGroupedServices(l, newqtyplus, linesToRemove);
                  l.set('qty', newqtyplus);
                } else if (newqtyminus && !me.negativeLineUpdated) {
                  me.negativeLineUpdated = true;
                  l.set('relatedLines', rln);
                  newqtyminus = adjustNotGroupedServices(l, newqtyminus, linesToRemove);
                  l.set('qty', newqtyminus);
                } else if (newqtyplus && newqtyminus && me.negativeLineUpdated) {
                  positiveLine = me.order.get('lines').filter(function getLine(currentLine) {
                    return currentLine.get('product').id === l.get('product').id && currentLine.get('qty') > 0;
                  });
                  if (positiveLine) {
                    deferredLines = l.get('relatedLines').filter(function getDeferredServices(relatedLine) {
                      return relatedLine.deferred === true;
                    });
                    rlp = OB.UTIL.mergeArrays(rlp, (deferredLines || []));
                    positiveLine.set('relatedLines', rlp);
                    positiveLine.set('qty', newqtyplus);
                  } else {
                    newLine = me.order.createLine(prod, newqtyplus);
                    newLine.set('relatedLines', rlp);
                  }
                  me.order.get('lines').remove(l);
                } else {
                  deferredLines = l.get('relatedLines').filter(function getDeferredServices(relatedLine) {
                    return relatedLine.deferred === true;
                  });
                  if (!deferredLines.length) {
                    me.order.get('lines').remove(l);
                  }
                }
              }
            });
            me.positiveLineUpdated = false;
            me.negativeLineUpdated = false;

            notDeferredRelatedLines = line.get('relatedLines').filter(function getNotDeferredLines(rl) {
              return !rl.deferred;
            });
            if (!line.get('groupService') && notDeferredRelatedLines.length > 1) {
              notDeferredRelatedLines.forEach(function (rl) {
                newLine = me.order.createLine(prod, me.order.get('lines').get(rl.orderlineId).get('qty'));
                newLine.set('relatedLines', [rl]);
                newLine.set('groupService', false);
              });
              me.order.get('lines').remove(line);
            }
          }
        });
        linesToRemove.forEach(function (l) {
          me.order.get('lines').remove(l);
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_DeletedService', [l.get('product').get('_identifier')]));
        });
        me.order.setUndo('FixOrderLines', oldUndo);
        me.updating = false;
        me.order.get('lines').trigger('updateServicePrices');
      }

      if (approvalNeeded) {
        OB.UTIL.Approval.requestApproval(
        OB.MobileApp.view.$.containerWindow.getRoot().model, [{
          approval: 'OBPOS_approval.returnService',
          message: 'OBPOS_approval.returnService',
          params: [servicesToApprove]
        }], function (approved, supervisor, approvalType) {
          if (approved) {
            fixServiceOrderLines(true);
          } else {
            fixServiceOrderLines(false);
          }
        });
      } else {
        fixServiceOrderLines(true);
      }
    }, this);
    this.order.on('change:net change:gross updateServicePrices', function () {
      var me = this,
          handleError;

      handleError = function (line, message) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_ErrorGettingServicePrice'), OB.I18N.getLabel(message, [line.get('product').get('_identifier')]), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true
        }], {
          onHideFunction: function () {
            me.order.get('lines').remove(line);
            me.order.set('undo', null);
            me.$.totalReceiptLine.renderQty();
          }
        });
      };

      if (this.updating || this.order.get('preventServicesUpdate')) {
        return;
      }

      this.order.get('lines').forEach(function (line) {
        var prod = line.get('product'),
            amountBeforeDiscounts = 0,
            amountAfterDiscounts = 0,
            rangeAmountBeforeDiscounts = 0,
            rangeAmountAfterDiscounts = 0,
            relatedQuantity = 0;
        if (prod.get('productType') === 'S' && prod.get('isPriceRuleBased') && !line.get('originalOrderLineId')) {
          var criteria = {};
          line.get('relatedLines').forEach(function (rl) {
            var l = me.order.get('lines').get(rl.orderlineId);
            if (l) {
              relatedQuantity += l.get('qty');
            } else {
              relatedQuantity += rl.qty;
            }
            if (me.order.get('priceIncludesTax')) {
              if (l) {
                amountBeforeDiscounts += Math.abs(l.get('gross'));
                amountAfterDiscounts += Math.abs(l.get('gross') - _.reduce(l.get('promotions'), function (memo, promo) {
                  return memo + promo.amt;
                }, 0));
                if (prod.get('quantityRule') === 'PP') {
                  rangeAmountBeforeDiscounts += Math.abs(OB.DEC.div(l.get('gross'), l.get('qty')));
                  rangeAmountAfterDiscounts += Math.abs(OB.DEC.div(l.get('gross') - _.reduce(l.get('promotions'), function (memo, promo) {
                    return memo + promo.amt;
                  }, 0), l.get('qty')));
                }
              } else {
                amountBeforeDiscounts += Math.abs(rl.gross);
                amountAfterDiscounts += Math.abs(rl.gross - _.reduce(rl.promotions, function (memo, promo) {
                  return memo + promo.amt;
                }, 0));
                if (prod.get('quantityRule') === 'PP') {
                  rangeAmountBeforeDiscounts += Math.abs(OB.DEC.div(rl.gross, rl.qty));
                  rangeAmountAfterDiscounts += Math.abs(OB.DEC.div(rl.gross - _.reduce(rl.promotions, function (memo, promo) {
                    return memo + promo.amt;
                  }, 0), rl.qty));
                }
              }
            } else {
              if (l) {
                amountBeforeDiscounts += Math.abs(l.get('net'));
                amountAfterDiscounts += Math.abs(l.get('net') - _.reduce(l.get('promotions'), function (memo, promo) {
                  return memo + promo.amt;
                }, 0));
                if (prod.get('quantityRule') === 'PP') {
                  rangeAmountBeforeDiscounts += Math.abs(OB.DEC.div(l.get('net'), l.get('qty')));
                  rangeAmountAfterDiscounts += Math.abs(OB.DEC.div(l.get('net') - _.reduce(l.get('promotions'), function (memo, promo) {
                    return memo + promo.amt;
                  }, 0), l.get('qty')));
                }
              } else {
                amountBeforeDiscounts += Math.abs(rl.net);
                amountAfterDiscounts += Math.abs(rl.net - _.reduce(rl.promotions, function (memo, promo) {
                  return memo + promo.amt;
                }, 0));
                if (prod.get('quantityRule') === 'PP') {
                  rangeAmountBeforeDiscounts += Math.abs(OB.DEC.div(rl.net, rl.qty));
                  rangeAmountAfterDiscounts += Math.abs(OB.DEC.div(rl.net - _.reduce(rl.promotions, function (memo, promo) {
                    return memo + promo.amt;
                  }, 0), rl.qty));
                }
              }
            }
          });
          if (prod.get('quantityRule') === 'UQ') {
            rangeAmountBeforeDiscounts = amountBeforeDiscounts;
            rangeAmountAfterDiscounts = amountAfterDiscounts;
          }
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
            criteria.remoteFilters = [];
            criteria.remoteFilters.push({
              columns: ['product'],
              operator: 'equals',
              value: line.get('product').get('id'),
              isId: true
            });
            criteria.remoteFilters.push({
              columns: [],
              operator: 'filter',
              value: 'ServicePriceRuleVersion_DateFilter',
              params: []
              //TODO: _limit -1
            });
          } else {
            criteria._whereClause = "where product = '" + line.get('product').get('id') + "' and validFromDate <= date('now')";
            criteria._orderByClause = 'validFromDate desc';
            criteria._limit = 1;
          }
          OB.Dal.find(OB.Model.ServicePriceRuleVersion, criteria, function (sprvs) {
            var priceruleVersion;
            if (sprvs && sprvs.length > 0) {
              priceruleVersion = sprvs.at(0);
              OB.Dal.get(OB.Model.ServicePriceRule, priceruleVersion.get('servicePriceRule'), function (spr) {
                if (spr.get('ruletype') === 'P') {
                  var amount, newprice, oldprice = line.get('priceList');
                  if (spr.get('afterdiscounts')) {
                    amount = amountAfterDiscounts * spr.get('percentage') / 100;
                  } else {
                    amount = amountBeforeDiscounts * spr.get('percentage') / 100;
                  }
                  if (!line.get('groupService')) {
                    amount = amount / relatedQuantity;
                  }
                  newprice = OB.Utilities.Number.roundJSNumber(oldprice + amount / line.get('qty'), 2);
                  me.order.setPrice(line, newprice, {
                    setUndo: false
                  });
                } else { //ruletype = 'R'
                  var rangeCriteria = {};
                  if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
                    rangeCriteria.remoteFilters = [];
                    rangeCriteria.remoteFilters.push({
                      columns: ['servicepricerule'],
                      operator: 'equals',
                      value: spr.get('id'),
                      isId: true
                    });
                    rangeCriteria.remoteFilters.push({
                      columns: [],
                      operator: 'filter',
                      value: 'ServicePriceRuleRange_AmountFilter',
                      params: [spr.get('afterdiscounts') ? rangeAmountAfterDiscounts : rangeAmountBeforeDiscounts]
                    });
                  } else {
                    rangeCriteria._whereClause = "where servicepricerule = '" + spr.get('id') + "' and (( amountUpTo >= " + (spr.get('afterdiscounts') ? rangeAmountAfterDiscounts : rangeAmountBeforeDiscounts) + ") or (amountUpTo is null))";
                    rangeCriteria._orderByClause = 'amountUpTo is null, amountUpTo';
                    rangeCriteria._limit = 1;
                  }
                  OB.Dal.find(OB.Model.ServicePriceRuleRange, rangeCriteria, function (sppr) {
                    var range, priceCriteria = {};
                    if (sppr && sppr.length > 0) {
                      range = sppr.at(0);
                      if (range.get('ruleType') === 'P') {
                        var amount, newprice, oldprice = line.get('priceList');
                        if (range.get('afterdiscounts')) {
                          amount = amountAfterDiscounts * range.get('percentage') / 100;
                        } else {
                          amount = amountBeforeDiscounts * range.get('percentage') / 100;
                        }
                        if (!line.get('groupService')) {
                          amount = amount / relatedQuantity;
                        }
                        newprice = OB.Utilities.Number.roundJSNumber(oldprice + amount / line.get('qty'), 2);
                        me.order.setPrice(line, newprice, {
                          setUndo: false
                        });
                      } else { //ruleType = 'F'
                        if (OB.MobileApp.model.hasPermission('OBPOS_remote.product', true)) {
                          priceCriteria.remoteFilters = [];
                          priceCriteria.remoteFilters.push({
                            columns: ['product'],
                            operator: 'equals',
                            value: prod.get('id'),
                            isId: true
                          });
                          priceCriteria.remoteFilters.push({
                            columns: ['priceList'],
                            operator: 'equals',
                            value: range.get('priceList'),
                            isId: true
                          });
                        } else {
                          priceCriteria.product = prod.get('id');
                          priceCriteria.priceList = range.get('priceList');
                        }
                        OB.Dal.find(OB.Model.ServicePriceRuleRangePrices, priceCriteria, function (price) {
                          var oldprice = line.get('priceList'),
                              newprice;
                          if (price && price.length > 0) {
                            newprice = OB.Utilities.Number.roundJSNumber(oldprice + price.at(0).get('listPrice'), 2);
                            me.order.setPrice(line, newprice, {
                              setUndo: false
                            });
                          } else {
                            handleError(line, 'OBPOS_ErrorPriceRuleRangePriceNotFound');
                          }
                        }, function () {
                          handleError(line, 'OBPOS_ErrorGettingPriceRuleRangePrice');
                        });
                      }
                    } else {
                      handleError(line, 'OBPOS_ErrorPriceRuleRangeNotFound');
                    }
                  }, function () {
                    handleError(line, 'OBPOS_ErrorGettingPriceRuleRange');
                  });
                }
              }, function () {
                handleError(line, 'OBPOS_ErrorGettingPriceRule');
              });
            } else {
              handleError(line, 'OBPOS_ErrorPriceRuleVersionNotFound');
            }
          }, function () {
            handleError(line, 'OBPOS_ErrorGettingPriceRuleVersion');
          });
        }
      });
    }, this);
    this.order.get('lines').on('remove', function (model, list, index) {
      var lineToDelete = model,
          removedId = model.get('id'),
          serviceLinesToCheck = [],
          text, linesToDelete, relations, deletedQty;

      if (OB.MobileApp.model.hasPermission('OBPOS_remove_ticket', true) && model.get('obposQtyDeleted') !== 0) {
        deletedQty = model.get('obposQtyDeleted');
      } else {
        deletedQty = model.get('qty');
      }

      if (!me.order.get('undo')) {
        text = OB.I18N.getLabel('OBPOS_DeleteLine') + ': ' + deletedQty + ' x ' + model.get('product').get('_identifier');
        linesToDelete = [model];
        relations = [];
        me.order.setUndo('DeleteLine', {
          text: text,
          lines: linesToDelete,
          relations: relations
        });
      } else {
        linesToDelete = me.order.get('undo').lines;
        if (!linesToDelete) {
          linesToDelete = [];
        }
        linesToDelete.push(model);
        text = me.order.get('undo').text;
        if (text) {
          text += ', ' + deletedQty + ' x ' + model.get('product').get('_identifier');
        } else {
          text = OB.I18N.getLabel('OBPOS_DeleteLine') + ': ' + deletedQty + ' x ' + model.get('product').get('_identifier');
        }
        relations = me.order.get('undo').relations;
        if (!relations) {
          relations = [];
        }
        me.order.get('undo').text = text;
        me.order.get('undo').lines = linesToDelete;
        me.order.get('undo').relations = relations;
      }

      me.order.get('lines').forEach(function (line, idx) {
        if (line.has('relatedLines') && line.get('relatedLines').length > 0) {
          var relationIds = _.pluck(line.get('relatedLines'), 'orderlineId');
          if (_.indexOf(relationIds, removedId) !== -1) {
            serviceLinesToCheck.push([line, idx]);
          }
        }
      });
      if (serviceLinesToCheck.length > 0) {
        serviceLinesToCheck.forEach(function (lineToCheck) {
          var rl, rls;
          if (lineToCheck[0].get('relatedLines').length > 1) {
            rl = _.filter(lineToCheck[0].get('relatedLines'), function (rl) {
              return rl.orderlineId === lineToDelete.get('id');
            });
            relations.push([lineToCheck[0], rl[0]]);
            //Effectively remove the relation from the service line
            rls = lineToCheck[0].get('relatedLines').slice();
            rls.splice(lineToCheck[0].get('relatedLines').indexOf(rl[0]), 1);
            lineToCheck[0].set('relatedLines', rls);
            if (lineToCheck[0].get('product').get('quantityRule') === 'PP') {
              text += ', ' + deletedQty + ' x ' + lineToCheck[0].get('product').get('_identifier');
              me.order.get('undo').text = text;
            }
          } else {
            me.order.deleteLine(lineToCheck[0], true);
          }
        });
      }
    }, this);
    this.order.on('change:selectedPayment', function (model) {
      OB.UTIL.HookManager.executeHooks('OBPOS_PaymentSelected', {
        order: this.order,
        paymentSelected: OB.MobileApp.model.paymentnames[model.get('selectedPayment')]
      });
    }, this);
  }
});
enyo.kind({
  name: 'OB.UI.MultiOrderView',
  published: {
    order: null
  },
  events: {
    onChangeTotal: ''
  },
  components: [{
    kind: 'OB.UI.ScrollableTable',
    name: 'listMultiOrderLines',
    scrollAreaMaxHeight: '450px',
    renderLine: 'OB.UI.RenderMultiOrdersLine',
    renderEmpty: 'OB.UI.RenderMultiOrdersLineEmpty',
    //defined on redenderorderline.js
    listStyle: 'edit'
  }, {
    tag: 'ul',
    classes: 'unstyled',
    components: [{
      tag: 'li',
      components: [{
        kind: 'OB.UI.TotalMultiReceiptLine',
        name: 'totalMultiReceiptLine'
      }]
    }, {
      tag: 'li',
      components: [{
        style: 'padding: 10px; border-top: 1px solid #cccccc; height: 40px;',
        components: [{
          kind: 'btninvoice',
          name: 'multiOrder_btninvoice',
          showing: false
        }, {
          style: 'clear: both;'
        }]
      }]
    }]
  }],
  listMultiOrders: null,
  init: function (model) {
    this.model = model;
    var me = this;
    this.total = 0;
    this.listMultiOrders = new Backbone.Collection();
    this.$.listMultiOrderLines.setCollection(this.listMultiOrders);
    this.model.get('multiOrders').on('change:additionalInfo', function (changedModel) {
      if (changedModel.get('additionalInfo') === 'I') {
        this.$.multiOrder_btninvoice.show();
        return;
      }
      this.$.multiOrder_btninvoice.hide();
    }, this);
    var orderList = this.model.get('multiOrders').get('multiOrdersList');
    orderList.on('reset add remove amountToLayaway', function () {
      me.total = _.reduce(me.model.get('multiOrders').get('multiOrdersList').models, function (memo, order) {
        return memo + ((!_.isUndefined(order.get('amountToLayaway')) && !_.isNull(order.get('amountToLayaway'))) ? order.get('amountToLayaway') : order.getPending());
      }, 0);
      this.model.get('multiOrders').set('total', this.total);
      this.model.get('multiOrders').on('change:total', function (model) {
        this.doChangeTotal({
          newTotal: model.get('total')
        });
      }, this);
      this.$.totalMultiReceiptLine.renderTotal(this.total);
      me.listMultiOrders.reset(me.model.get('multiOrders').get('multiOrdersList').models);

      if (this.model.get('leftColumnViewManager').isMultiOrder()) {
        this.doChangeTotal({
          newTotal: this.total
        });
      }
      this.$.totalMultiReceiptLine.renderQty(me.model.get('multiOrders').get('multiOrdersList').length);
    }, this);
  },
  initComponents: function () {
    this.inherited(arguments);
  }
});