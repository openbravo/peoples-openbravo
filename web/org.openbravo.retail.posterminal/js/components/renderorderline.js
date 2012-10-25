/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  kind: 'OB.UI.SelectButton',
  name: 'OB.UI.RenderOrderLine',
  classes: 'btnselect-orderline',
  handlers: {
    onChangeEditMode: 'changeEditMode'
  },
  components: [{
    name: 'product',
    attributes: {
      style: 'float: left; width: 40%;'
    }
  }, {
    name: 'quantity',
    attributes: {
      style: 'float: left; width: 20%; text-align: right;'
    }
  }, {
    name: 'price',
    attributes: {
      style: 'float: left; width: 20%; text-align: right;'
    }
  }, {
    name: 'gross',
    attributes: {
      style: 'float: left; width: 20%; text-align: right;'
    }
  }, {
    style: 'clear: both;'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.product.setContent(this.model.get('product').get('_identifier'));
    this.$.quantity.setContent(this.model.printQty());
    this.$.price.setContent(this.model.printPrice());
    this.$.gross.setContent(this.model.printGross());
    if (this.model.get('promotions')) {
      enyo.forEach(this.model.get('promotions'), function(d) {
        if (d.hidden) {
          // continue
          return;
        }
        this.createComponent({
          components: [{
            content: '-- ' + d.name,
            attributes: {
              style: 'float: left; width: 80%;'
            }
          }, {
            content: OB.I18N.formatCurrency(-d.amt),
            attributes: {
              style: 'float: right; width: 20%; text-align: right;'
            }
          }]
        });
      }, this);

    }
  },
  changeEditMode: function (sender, event) {
    this.addRemoveClass('btnselect-orderline-edit', event.edit);
  }

});

enyo.kind({
  name: 'OB.UI.RenderOrderLineEmpty',
  style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBPOS_ReceiptNew'));
  }
});