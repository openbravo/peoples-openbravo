/*
 ************************************************************************************
 * Copyright (C) 2013-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _ */

enyo.kind({
  name: 'OB.UI.OrderMultiSelect',
  kind: 'Image',
  src: '../org.openbravo.retail.posterminal/img/iconPinSelected.png',
  sizing: "cover",
  width: 28,
  height: 28,
  style: 'float: right; cursor: pointer; margin-top: 10px;',
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
  style: 'float: right; cursor: pointer; margin-top: 10px;',
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
  style: 'float: right;',
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
  }],
  newButtonComponents: [{
    kind: 'OB.UI.BusinessPartner',
    name: 'bpbutton'
  }, {
    kind: 'OB.UI.BPLocation',
    name: 'bplocbutton'
  }],
  style: 'border-bottom: 1px solid #cccccc;',
  components: [{
    name: 'receiptLabels'
  }, {
    name: 'receiptButtons',
    style: 'clear: both; '
  }],
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
  showMultiSelected: function (inSender, inEvent) {
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
    this.doToggleSelectionMode(inEvent);
  },
  multiSelectAll: function (inSender, inEvent) {
    this.doTableMultiSelectAll();
  },
  initComponents: function () {
    this.inherited(arguments);
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
    OB.UTIL.HookManager.executeHooks('OBPOS_UpdateTotalReceiptLine', {
      totalline: this
    }, function (args) {
      //All should be done in module side
    });
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
    }, function (args) {
      //All should be done in module side
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
    onAllTicketLinesChecked: 'allTicketLinesChecked',
    onToggleSelectionTable: 'toggleSelectionTable',
    onMultiSelectAllTable: 'multiSelectAllTable',
    onTableMultiSelectedItems: 'tableMultiSelectedItems'
  },
  components: [{
    kind: 'OB.UI.ScrollableTable',
    name: 'listOrderLines',
    columns: ['product', 'quantity', 'price', 'gross'],
    scrollAreaMaxHeight: '250px',
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

    taxList.models = _.sortBy(taxList.models, function (taxLine) {
      return taxLine.get('name');
    });

    this.$.listTaxLines.setCollection(taxList);
  },
  toggleSelectionTable: function (inSender, inEvent) {
    this.$.listOrderLines.setSelectionMode(inEvent.multiselection ? 'multiple' : 'single');
  },
  multiSelectAllTable: function () {
    this.$.listOrderLines.selectAll();
  },
  tableMultiSelectedItems: function (inSender, inEvent) {
    this.$.listOrderLines.setSelectedModels(inEvent.selection);
  },
  orderChanged: function (oldValue) {
    var me = this;
    this.$.totalReceiptLine.renderTotal(this.order.getTotal());
    this.$.totalReceiptLine.renderQty(this.order.getQty());
    this.$.totalTaxLine.renderTax(OB.DEC.sub(this.order.getTotal(), this.order.getNet()));
    this.$.totalTaxLine.renderBase('');
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
        } else if (model.get('documentType') === OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns) {
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
    this.order.get('lines').on('add change:qty change:relatedLines updateRelations', function () {
      var me = this;

      if (this.updating || this.order.get('preventServicesUpdate')) {
        return;
      } else {
        this.updating = true;
      }

      function getServiceLines(service) {
        var serviceLines;
        if (service.get('product').get('groupProduct')) {
          serviceLines = _.filter(me.order.get('lines').models, function (l) {
            return l.get('product').get('id') === service.get('product').get('id');
          });
        } else {
          serviceLines = [service];
          if (service.get('mirrorLine')) {
            serviceLines.push(service.get('mirrorLine'));
          }
        }
        return serviceLines;
      }

      this.order.get('lines').forEach(function (line) {
        var prod = line.get('product'),
            newLine, i, j, l, rlp, rln, newqtyplus = 0,
            newqtyminus = 0,
            serviceLines = [],
            positiveLines = [],
            negativeLines = [],
            newRelatedLines = [];

        if (line.has('relatedLines') && line.get('relatedLines').length > 0) {

          serviceLines = getServiceLines(line);

          for (i = 0; i < serviceLines.length; i++) {
            newRelatedLines = newRelatedLines.concat(serviceLines[i].get('relatedLines'));
            for (j = 0; j < serviceLines[i].get('relatedLines').length; j++) {
              l = me.order.get('lines').get(serviceLines[i].get('relatedLines')[j].orderlineId);
              if (l && l.get('qty') > 0) {
                newqtyplus += l.get('qty');
                positiveLines.push(l);
              } else if (l && l.get('qty') < 0) {
                newqtyminus += l.get('qty');
                negativeLines.push(l);
              }
            }
          }
          rlp = _.filter(newRelatedLines, function (rl) {
            return _.indexOf(_.pluck(positiveLines, 'id'), rl.orderlineId) !== -1;
          });

          rln = _.filter(newRelatedLines, function (rl) {
            return _.indexOf(_.pluck(negativeLines, 'id'), rl.orderlineId) !== -1;
          });

          if (prod.get('quantityRule') === 'UQ') {
            newqtyplus = (newqtyplus ? 1 : 0);
            newqtyminus = (newqtyminus ? -1 : 0);
          }

          serviceLines.forEach(function (l) {
            if (l.get('qty') > 0) {
              if (serviceLines.length === 1 && newqtyminus && newqtyplus) {
                newLine = me.order.createLine(prod, newqtyminus);
                newLine.set('relatedLines', rln);
                l.set('relatedLines', rlp);
                l.set('qty', newqtyplus);
              } else if (serviceLines.length === 1 && newqtyminus) {
                if (!l.get('product').get('returnable')) { // Cannot add not returnable service to a negative product
                  me.order.get('lines').remove(l);
                  OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_UnreturnableProduct'), OB.I18N.getLabel('OBPOS_UnreturnableProductMessage', [l.get('product').get('_identifier')]));
                  return;
                }
                if (l.get('previouslyApproved')) { // A service needs an approval to return
                  l.set('relatedLines', rln);
                  l.set('qty', newqtyminus);
                } else {
                  OB.UTIL.Approval.requestApproval(
                  OB.MobileApp.view.$.containerWindow.$.pointOfSale.model, 'OBPOS_approval.returnService', function (approved, supervisor, approvalType) {
                    if (approved) {
                      l.set('previouslyApproved', true);
                      l.set('relatedLines', rln);
                      l.set('qty', newqtyminus);
                    } else {
                      me.order.get('lines').remove(l);
                    }
                  });
                }
              } else if (newqtyplus) {
                l.set('relatedLines', rlp);
                l.set('qty', newqtyplus);
              } else {
                me.order.get('lines').remove(l);
              }
              if (!prod.get('groupProduct')) {
                newLine.set('mirrorLine', line);
                line.set('mirrorLine', newLine);
              }
            } else {
              if (serviceLines.length === 1 && newqtyminus && newqtyplus) {
                newLine = me.order.createLine(prod, newqtyplus);
                newLine.set('relatedLines', rlp);
                l.set('relatedLines', rln);
                l.set('qty', newqtyminus);
              } else if (serviceLines.length === 1 && newqtyplus) {
                l.set('relatedLines', rlp);
                l.set('qty', newqtyplus);
              } else if (newqtyminus) {
                l.set('relatedLines', rln);
                l.set('qty', newqtyminus);
              } else {
                me.order.get('lines').remove(l);
              }
              if (!prod.get('groupProduct')) {
                newLine.set('mirrorLine', line);
                line.set('mirrorLine', newLine);
              }
            }
          });
        }
      });
      this.updating = false;
      this.order.get('lines').trigger('updateServicePrices');
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
          }
        });
      };

      if (this.updating || this.order.get('preventServicesUpdate')) {
        return;
      }

      this.order.get('lines').forEach(function (line) {
        var prod = line.get('product'),
            amountBeforeDiscounts = 0,
            amountAfterDiscounts = 0;
        if (prod.get('productType') === 'S' && prod.get('isPriceRuleBased')) {
          var criteria = {};
          line.get('relatedLines').forEach(function (rl) {
            var l = me.order.get('lines').get(rl.orderlineId);
            if (me.order.get('priceIncludesTax')) {
              amountBeforeDiscounts += l.get('gross');
              amountAfterDiscounts += (l.get('discountedLinePrice') ? l.get('discountedLinePrice') : l.get('gross'));
            } else {
              amountBeforeDiscounts += l.get('net');
              amountAfterDiscounts += (l.get('discountedNet') ? l.get('discountedNet') : l.get('net'));
            }
          });
          criteria._whereClause = "where product = '" + line.get('product').get('id') + "' and validFromDate <= date('now')";
          criteria._orderByClause = 'validFromDate desc';
          criteria._limit = 1;
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
                  newprice = OB.Utilities.Number.roundJSNumber(oldprice + amount / line.get('qty'), 2);
                  me.order.setPrice(line, newprice, {
                    setUndo: false
                  });
                } else { //ruletype = 'R'
                  var rangeCriteria = {};
                  rangeCriteria._whereClause = "where servicepricerule = '" + spr.get('id') + "' and ((afterdiscounts = 'false' and amountUpTo >= " + amountBeforeDiscounts + ") or (afterdiscounts = 'true' and amountUpTo >= " + amountAfterDiscounts + ") or (amountUpTo is null))";
                  rangeCriteria._orderByClause = 'amountUpTo is null, amountUpTo';
                  rangeCriteria._limit = 1;
                  OB.Dal.find(OB.Model.ServicePriceRuleRange, rangeCriteria, function (sppr) {
                    var range;
                    if (sppr && sppr.length > 0) {
                      range = sppr.at(0);
                      if (range.get('ruleType') === 'P') {
                        var amount, newprice, oldprice = line.get('priceList');
                        if (range.get('afterdiscounts')) {
                          amount = amountAfterDiscounts * range.get('percentage') / 100;
                        } else {
                          amount = amountBeforeDiscounts * range.get('percentage') / 100;
                        }
                        newprice = OB.Utilities.Number.roundJSNumber(oldprice + amount / line.get('qty'), 2);
                        me.order.setPrice(line, newprice, {
                          setUndo: false
                        });
                      } else { //ruleType = 'F'
                        OB.Dal.find(OB.Model.ServicePriceRuleRangePrices, {
                          product: prod.get('id'),
                          priceList: range.get('priceList')
                        }, function (price) {
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
      var removedId = model.get('id'),
          removedIndex = index.index,
          serviceLinesToCheck = [],
          deletedServices = [],
          changedServices = [];

      this.order.unset('changedServices');
      this.order.unset('deletedServices');

      this.order.get('lines').forEach(function (line, idx) {
        var prod = line.get('product');
        if (line.has('relatedLines') && line.get('relatedLines').length > 0) {
          var i, l = 0,
              relationIds = _.pluck(line.get('relatedLines'), 'orderlineId');
          if (_.indexOf(relationIds, removedId) !== -1) {
            serviceLinesToCheck.push([line, idx]);
          }
        }
      });
      if (serviceLinesToCheck.length > 0) {
        serviceLinesToCheck.forEach(function (lineToCheck) {
          if (lineToCheck[0].get('relatedLines').length > 1) {
            changedServices.push(lineToCheck[0]);
          } else {
            deletedServices.push(lineToCheck[0]);
          }
        });
        this.order.set({
          changedServices: changedServices,
          deletedServices: deletedServices
        });
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