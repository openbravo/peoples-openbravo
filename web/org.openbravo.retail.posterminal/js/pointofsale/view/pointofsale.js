/*global OB, enyo */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*left toolbar*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
  tag: 'li',
  classes: 'span3',
  components: [{
    name: 'theButton',
    attributes: {
      style: 'margin: 0px 5px 0px 5px;'
    }
  }],
  initComponents: function() {
    this.inherited(arguments);
    this.$.theButton.createComponent(this.button);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbar',
  classes: 'span4',
  components: [{
    tag: 'ul',
    classes: 'unstyled nav-pos row-fluid',
    name: 'toolbar'
  }],
  initComponents: function() {
    this.inherited(arguments);
    enyo.forEach(this.buttons, function(btn) {
      this.$.toolbar.createComponent({
        kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
        button: btn
      });
    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl',
  kind: 'OB.OBPOSPointOfSale.UI.LeftToolbar',
  buttons: [{
    kind: 'OB.UI.ButtonNew'
  }, {
    kind: 'OB.UI.ButtonDelete'
  }, {
    kind: 'OB.UI.ButtonPrint'
  }, {
    kind: 'OB.OBPOSPointOfSale.UI.StandardMenu'
  }]
});

/* right toolbar */

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
      delete this.button.containerCssClass;
    }
    this.$.theButton.createComponent(this.button);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.RightToolbar',
  classes: 'span8',
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
  kind: 'OB.OBPOSPointOfSale.UI.RightToolbar',
  buttons: [{
    kind: 'OB.UI.ButtonTabPayment',
    containerCssClass: 'span3'
  }, {
    kind: 'OB.UI.ButtonTabScan',
    containerCssClass: 'span3'
  }, {
    kind: 'OB.UI.ButtonTabBrowse',
    containerCssClass: 'span2'
  }, {
    kind: 'OB.UI.ButtonTabSearch',
    containerCssClass: 'span2'
  }, {
    kind: 'OB.UI.ButtonTabEditLine',
    containerCssClass: 'span2'
  }]
});

// Point of sale main window view
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PointOfSale',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSPointOfSale.Model.PointOfSale,
  tag: 'section',
  handlers: {
    onAddProduct: 'addProductToOrder'
  },
  components: [{
    classes: 'row',
    style: 'margin-bottom: 5px;',
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.RightToolbarImpl'
    }]
  }, {
    classes: 'row',
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.ReceiptView',
      name: 'receiptview'
    }, {
      classes: 'span6',
      components: [{
        classes: 'tab-content',
        components: [{
          kind: 'OB.UI.TabScan'
        }, {
          kind: 'OB.UI.TabBrowse'
        }, {
          kind: 'OB.UI.TabSearch'
        }, {
          kind: 'OB.UI.TabPayment',
        }, {
          kind: 'OB.UI.TabEditLine'
        }]
      }, {
        kind: 'OB.UI.KeyboardOrder',
        name: 'keyboard'
      }]
    }]
  }],
  addProductToOrder: function(inSender, inEvent){
    this.model.get('order').addProduct(inEvent.originator.modelAdd);
    return true;// not continue
  },
  init: function() {
    this.inherited(arguments);
    debugger;
    this.$.receiptview.setOrder(this.model.get('order'));
    console.log('order: ' + this.model.get('order'));
    console.log('main init')
  }

});

OB.POS.registerWindow({
  windowClass: OB.OBPOSPointOfSale.UI.PointOfSale,
  route: 'retail.pointofsale',
  menuPosition: null,
  menuLabel: 'POS'
});