/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

// Toolbar container
// ----------------------------------------------------------------------------
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RightToolbar',
  classes: 'span9',
  components: [{
    tag: 'ul',
    classes: 'unstyled nav-pos row-fluid',
    name: 'toolbar'
  }],
  initComponents: function() {
    this.inherited(arguments);
    enyo.forEach(this.buttons, function(btn) {
      this.$.toolbar.createComponent({
        kind: 'OB.OBPOSPointOfSale.UI.RightToolbarButton',
        button: btn
      });
    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RightToolbarImpl',
  published: {
    receipt: null
  },
  kind: 'OB.OBPOSPointOfSale.UI.RightToolbar',
  buttons: [{
    kind: 'OB.OBPOSPointOfSale.UI.ButtonTabPayment',
    containerCssClass: 'span4'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.ButtonTabScan',
    containerCssClass: 'span2'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.ButtonTabBrowse',
    containerCssClass: 'span2'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.ButtonTabSearch',
    containerCssClass: 'span2'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.ButtonTabEditLine',
    containerCssClass: 'span2'
  }],

  manualTap: function(tab) {
    //Hack to manually tap on bootstrap tab
    var domButton = $(tab.tabPanel + '_button');
    domButton.tab('show');
    domButton.parent().parent().addClass('active');
    tab.tap();
  },

  receiptChanged: function() {
    var totalPrinterComponent;

    this.receipt.on('clear scan', function() {
      if (this.receipt.get('isEditable') === false) {
        this.manualTap(this.edit);
      } else {
        this.manualTap(this.scan);
      }
    }, this);

    this.receipt.get('lines').on('click', function() {
      this.manualTap(this.edit);
    }, this);

    //some button will draw the total
    this.waterfall('onChangeTotal', {
      newTotal: this.receipt.getTotal()
    });
    this.receipt.on('change:gross', function(model) {
      this.waterfall('onChangeTotal', {
        newTotal: this.receipt.getTotal()
      });
    }, this);
  },
  initComponents: function() {
    var me = this;

    function getButtonInPos(pos) {
      return me.$.toolbar.getComponents()[pos].$.theButton.getComponents()[0];
    }

    this.inherited(arguments);

    this.scan = getButtonInPos(1);
    this.edit = getButtonInPos(4);
  }
});


enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RightToolbarButton',
  tag: 'li',
  components: [{
    name: 'theButton',
    attributes: {
      'data-toggle': 'tab',
      style: 'margin: 0px 5px 0px 5px;'
    }
  }],
  initComponents: function() {
    this.inherited(arguments);
    if (this.button.containerCssClass) {
      this.setClassAttribute(this.button.containerCssClass);
    }
    this.$.theButton.createComponent(this.button);
  }
});


// Toolbar buttons
// ----------------------------------------------------------------------------
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabScan',
  kind: 'OB.UI.ToolbarButtonTab',
  tabPanel: '#scan',
  label: OB.I18N.getLabel('OBPOS_LblScan'),
  events: {
    onTabChange: ''
  },
  tap: function() {
    this.doTabChange({
      keyboard: 'toolbarscan',
      edit: false
    });
  },
  makeId: function() {
    return 'scan_button';
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabBrowse',
  kind: 'OB.UI.ToolbarButtonTab',
  events: {
    onTabChange: ''
  },
  tabPanel: '#catalog',
  label: OB.I18N.getLabel('OBPOS_LblBrowse'),
  tap: function() {
    this.doTabChange({
      keyboard: false,
      edit: false
    });
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabSearch',
  kind: 'OB.UI.ToolbarButtonTab',
  tabPanel: '#search',
  label: OB.I18N.getLabel('OBPOS_LblSearch'),
  events: {
    onTabChange: ''
  },
  tap: function() {
    this.doTabChange({
      keyboard: false,
      edit: false
    });
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabPayment',
  kind: 'OB.UI.ToolbarButtonTab',
  tabPanel: '#payment',
  handlers: {
    onChangeTotal: 'renderTotal'
  },
  events: {
    onTabChange: ''
  },
  tap: function() {
    var receipt = this.model.get('order');
    if(receipt.get('isQuotation')){
      if(receipt.get('hasbeenpaid')!=='Y'){
        receipt.calculateTaxes(function() {
          receipt.trigger('closed');
          receipt.trigger('scan'); 
        });
      }else{
        receipt.calculateTaxes(function() {
        receipt.trigger('scan'); 
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_QuotationClosed'));
        });
      }
      return;
    }
    if (this.model.get('order').get('isEditable') === false) {
      return true;
    }
    this.doTabChange({
      keyboard: 'toolbarpayment',
      edit: false
    });
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
  initComponents: function() {
    this.inherited(arguments);
    this.removeClass('btnlink-gray');
  },
  init: function(model){
    this.model = model
  },
  renderTotal: function(sender, event) {
    this.$.totalPrinter.renderTotal(event.newTotal);
  },
  init: function(model) {
    this.model = model;
    this.model.get('order').on('change:isEditable', function(newValue) {
      if (newValue) {
        if (newValue.get('isEditable') === false) {
          this.setAttribute('data-toogle', null);
          this.setAttribute('disabled', 'disabled');
          this.setAttribute('href', null);
          this.disabled = true;
          return;
        }
      }
      this.setAttribute('data-toogle', 'tab');
      this.setAttribute('disabled', null);
      this.setAttribute('href', '#payment');
      this.disabled = true;
    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ButtonTabEditLine',
  published: {
    ticketLines: null
  },
  kind: 'OB.UI.ToolbarButtonTab',
  tabPanel: '#edition',
  label: OB.I18N.getLabel('OBPOS_LblEdit'),
  events: {
    onTabChange: ''
  },
  tap: function() {
    this.doTabChange({
      keyboard: 'toolbarscan',
      edit: true
    });
  },
  makeId: function() {
    return 'edition_button';
  }
});


// Toolbar panes
//----------------------------------------------------------------------------
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RightToolbarPane',
  published: {
    model: null
  },
  classes: 'tab-content',
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.TabScan',
    name: 'scan'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.TabBrowse'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.TabSearch',
    name: 'search'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.TabPayment',
    name: 'payment'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.TabEditLine',
    name: 'edit'
  }],
  modelChanged: function() {
    var receipt = this.model.get('order');
    this.$.scan.setReceipt(receipt);
    this.$.search.setReceipt(receipt);
    this.$.payment.setReceipt(receipt);
    this.$.edit.setReceipt(receipt);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabSearch',
  published: {
    receipt: null
  },
  classes: 'tab-pane',
  components: [{
    style: 'overflow: auto; margin: 5px',
    components: [{
      style: 'background-color: #ffffff; color: black; padding: 5px',
      components: [{
        kind: 'OB.UI.SearchProduct',
        name: 'search'
      }]
    }]
  }],
  makeId: function() {
    return 'search';
  },
  receiptChanged: function() {
    this.$.search.setReceipt(this.receipt);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabBrowse',
  classes: 'tab-pane',
  components: [{
    kind: 'OB.UI.ProductBrowser'
  }],

  makeId: function() {
    return 'catalog';
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabScan',
  published: {
    receipt: null
  },
  classes: 'tab-pane',
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.Scan',
    name: 'scan'
  }],
  makeId: function() {
    return 'scan';
  },
  receiptChanged: function() {
    this.$.scan.setReceipt(this.receipt);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabEditLine',
  published: {
    receipt: null
  },
  classes: 'tab-pane',
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.EditLine',
    name: 'edit'
  }],
  makeId: function() {
    return 'edition';
  },
  receiptChanged: function() {
    this.$.edit.setReceipt(this.receipt);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.TabPayment',
  published: {
    receipt: null
  },
  classes: 'tab-pane',
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.Payment',
    name: 'payment'
  }],
  makeId: function() {
    return 'payment';
  },
  receiptChanged: function() {
    this.$.payment.setReceipt(this.receipt);
  }
});