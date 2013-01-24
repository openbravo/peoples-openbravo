/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone */

enyo.kind({
  name: 'OB.UI.TotalReceiptLine',
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxForTicketLines'
  },
  style: 'position: relative; padding: 10px;',
  components: [{
    name: 'lblTotal',
    style: 'float: left; width: 40%;',
    content: OB.I18N.getLabel('OBPOS_LblTotal')
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
  }
});

enyo.kind({
  name: 'OB.UI.TotalTaxLine',
  handlers: {
    onCheckBoxBehaviorForTicketLine: 'checkBoxForTicketLines'
  },
  style: 'position: relative; padding: 10px;',
  components: [{
    name: 'lblTotalTax',
    style: 'float: left; width: 40%;',
    content: OB.I18N.getLabel('OBPOS_LblTotalTax')
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
  checkBoxForTicketLines: function (inSender, inEvent) {
    if (inEvent.status) {
      this.$.lblTotalTax.hasNode().style.width = '48%';
      this.$.totalbase.hasNode().style.width = '16%';
      this.$.totaltax.hasNode().style.width = '36%';
    } else {
      this.$.lblTotalTax.hasNode().style.width = '40%';
      this.$.totalbase.hasNode().style.width = '20%';
      this.$.totaltax.hasNode().style.width = '40%';
    }
  }
});

enyo.kind({
  name: 'OB.UI.TaxBreakdown',
  style: 'position: relative; padding: 10px;',
  components: [{
    name: 'lblTotalTax',
    style: 'float: left; width: 40%;',
    content: OB.I18N.getLabel('OBPOS_LblTaxBreakdown')
  }, {
    style: 'clear: both;'
  }]
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
    style: 'font-weight:bold; ',
    content: 'Invoice'
  }]
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
          name: 'divreturn',
          style: 'float: right; width: 50%; text-align: right; font-weight:bold; font-size: 30px; color: #f8941d;',
          showing: false,
          content: OB.I18N.getLabel('OBPOS_ToBeReturned')
        }, {
          name: 'divbtnquotation',
          showing: false,
          style: 'float: right; width: 100%; text-align: right; font-weight:bold; font-size: 30px; color: #f8941d;',
          content: OB.I18N.getLabel('OBPOS_QuotationDraft')
        }, {
          name: 'divispaid',
          showing: false,
          style: 'float: right; width: 50%; text-align: right; font-weight:bold; font-size: 30px; color: #f8941d;',
          content: OB.I18N.getLabel('OBPOS_paid')
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
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
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
    this.setTaxes();
    this.order.on('change:gross change:net change:taxes', function (model) {
      this.$.totalReceiptLine.renderTotal(model.getTotal());
      this.$.totalTaxLine.renderTax(model.getTotal() - model.getNet());
      this.setTaxes();
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
        this.$.divreturn.show();
      } else {
        this.$.divreturn.hide();
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
        this.$.divbtnquotation.show();
        this.$.listOrderLines.children[4].children[0].setContent(OB.I18N.getLabel('OBPOS_QuotationNew'));
        if (model.get('hasbeenpaid') === 'Y') {
          this.$.divbtnquotation.setContent(OB.I18N.getLabel('OBPOS_QuotationUnderEvaluation'));
        } else {
          this.$.divbtnquotation.setContent(OB.I18N.getLabel('OBPOS_QuotationDraft'));
        }
      } else {
        this.$.divbtnquotation.hide();
        this.$.listOrderLines.children[4].children[0].setContent(OB.I18N.getLabel('OBPOS_ReceiptNew'));
      }
    }, this);
    this.order.on('change:hasbeenpaid', function (model) {
      if (model.get('isQuotation') && model.get('hasbeenpaid') === 'Y') {
        this.$.divbtnquotation.setContent(OB.I18N.getLabel('OBPOS_QuotationUnderEvaluation'));
      } else if (model.get('isQuotation') && model.get('hasbeenpaid') === 'N') {
        this.$.divbtnquotation.setContent(OB.I18N.getLabel('OBPOS_QuotationDraft'));
      }
    }, this);
    this.order.on('change:isPaid', function (model) {
      if (model.get('isPaid') === true) {
        this.$.divispaid.show();
      } else {
        this.$.divispaid.hide();
      }
    }, this);
  }
});