/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */
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
  renderBase: function (newBase) {
    //this.$.totalbase.setContent(newBase);
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
  style: 'width: 50px;',
  classes: 'btnlink-white btnlink-payment-clear btn-icon-small btn-icon-check',
  tap: function () {
    this.doCancelReceiptToInvoice();
  }
});

enyo.kind({
  name: 'btninvoice',
  showing: false,
  style: 'float: left; width: 50%;',
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
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxBehavior',
    onAllTicketLinesChecked: 'allTicketLinesChecked'
  },
  components: [{
    kind: 'OB.UI.ScrollableTable',
    name: 'listOrderLines',
    scrollAreaMaxHeight: '250px',
    renderLine: 'OB.UI.RenderOrderLine',
    renderEmpty: 'OB.UI.RenderOrderLineEmpty',
    //defined on redenderorderline.js
    listStyle: 'edit'
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
        style: 'padding: 10px; border-top: 1px solid #cccccc; height: 40px;',
        components: [{
          kind: 'btninvoice',
          name: 'divbtninvoice',
          showing: false
        }, {
          name: 'divText',
          style: 'float: right; text-align: right; font-weight:bold; font-size: 30px;',
          showing: false,
          content: ''
        }, {
          style: 'clear: both;'
        }]
      }]
    }, {
      tag: 'li',
      components: [{
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
      listStyle: 'nonselectablelist'
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
    this.$.listTaxLines.setCollection(taxList);
  },
  orderChanged: function (oldValue) {
    this.$.totalReceiptLine.renderTotal(this.order.getTotal());
    this.$.totalReceiptLine.renderQty(this.order.getQty());
    this.$.totalTaxLine.renderTax(this.order.getTotal() - this.order.getNet());
    this.$.totalTaxLine.renderBase('');
    this.$.listOrderLines.setCollection(this.order.get('lines'));
    this.$.listPaymentLines.setCollection(this.order.get('payments'));
    this.setTaxes();
    this.order.on('change:gross change:net change:taxes', function (model) {
      if (model.get('orderType') !== 3) {
        this.$.totalReceiptLine.renderTotal(model.getTotal());
        this.$.totalTaxLine.renderTax(model.getTotal() - model.getNet());
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
    this.order.on('change:orderType', function (model) {
      if (model.get('orderType') === 1) {
        this.$.divText.addStyles('width: 50%; color: #f8941d;');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_ToBeReturned'));
        this.$.divText.show();
      } else if (model.get('orderType') === 2) {
        this.$.divText.addStyles('width: 50%; color: lightblue;');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_ToBeLaidaway'));
        this.$.divText.show();
        //We have to ensure that there is not another handler showing this div
      } else if (model.get('orderType') === 3) {
        this.$.divText.addStyles('width: 50%; color: lightblue;');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_VoidLayaway'));
        this.$.divText.show();
        //We have to ensure that there is not another handler showing this div
      } else if (model.get('isLayaway')) {
        this.$.divText.addStyles('width: 50%; color: lightblue;');
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_LblLayaway'));
        this.$.divText.show();
        //We have to ensure that there is not another handler showing this div
      } else if (this.$.divText.content === OB.I18N.getLabel('OBPOS_ToBeReturned') || this.$.divText.content === OB.I18N.getLabel('OBPOS_ToBeLaidaway') || this.$.divText.content === OB.I18N.getLabel('OBPOS_VoidLayaway')) {
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
      if (model.get('isQuotation') && model.get('hasbeenpaid') === 'Y' && this.$.divText.content && (this.$.divText.content === OB.I18N.getLabel('OBPOS_QuotationNew') || this.$.divText.content === OB.I18N.getLabel('OBPOS_QuotationDraft'))) {
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_QuotationUnderEvaluation'));
      } else if (model.get('isQuotation') && model.get('hasbeenpaid') === 'N' && !model.get('isLayaway')) {
        this.$.divText.setContent(OB.I18N.getLabel('OBPOS_QuotationDraft'));
      }
    }, this);
    this.order.on('change:isPaid change:paidOnCredit change:isQuotation', function (model) {
      if (model.get('isPaid') === true && !model.get('isQuotation')) {
        this.$.divText.addStyles('width: 50%; color: #f8941d;');
        if (model.get('paidOnCredit')) {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_paidOnCredit'));
        } else if (model.get('documentType') === OB.POS.modelterminal.get('terminal').terminalType.documentTypeForReturns) {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_paidReturn'));
        } else {
          this.$.divText.setContent(OB.I18N.getLabel('OBPOS_paid'));
        }
        this.$.divText.show();
        this.$.listPaymentLines.show();
        this.$.paymentBreakdown.show();
        //We have to ensure that there is not another handler showing this div
      } else if (this.$.divText.content === OB.I18N.getLabel('OBPOS_paid') || this.$.divText.content === OB.I18N.getLabel('OBPOS_paidReturn') || this.$.divText.content === OB.I18N.getLabel('OBPOS_paidOnCredit')) {
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
    this.model.get('multiOrders').get('multiOrdersList').on('reset add remove change', function () {
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