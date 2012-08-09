/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  enyo.kind({
    kind: 'OB.UI.SelectButton',
    name: 'OB.UI.RenderOrderLine',
    classes: 'btnselect-orderline',
    components: [{
      name:'product',
      attributes: {
        style: 'float: left; width: 40%;'
      },
    },{
      name: 'quantity',
      attributes: {
        style: 'float: left; width: 20%; text-align: right;'
      }
    },{
      name: 'price',
      attributes: {
        style: 'float: left; width: 20%; text-align: right;'
      }
    },{
      name: 'gross',
      attributes: {
        style: 'float: left; width: 20%; text-align: right;'
      }
    },{
      style: 'clear: both;'
    }],
    initComponents: function(){
      this.inherited(arguments);
      this.$.product.setContent(this.model.get('product').get('_identifier'));
      this.$.quantity.setContent(this.model.printQty());
      this.$.price.setContent(this.model.printPrice());
      this.$.gross.setContent(this.model.printGross());
    }
  });
  
  enyo.kind({
    name: 'OB.UI.RenderOrderLineEmpty',
    style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
    initComponents: function() {
      this.inherited(arguments);
      this.setContent(OB.I18N.getLabel('OBPOS_ReceiptNew'));
    }
  });
  
  //refactored as enyo view -> OB.UI.OrderLine 
  OB.COMP.RenderOrderLine = OB.COMP.SelectButton.extend({
    contentView: [{
      id: 'divproduct',
      tag: 'div',
      attributes: {
        style: 'float: left; width: 40%'
      }
    }, {
      id: 'divquantity',
      tag: 'div',
      attributes: {
        style: 'float: left; width: 20%; text-align: right'
      }
    }, {
      id: 'divprice',
      tag: 'div',
      attributes: {
        style: 'float: left; width: 20%; text-align: right'
      }
    }, {
      id: 'divgross',
      tag: 'div',
      attributes: {
        style: 'float: left; width: 20%; text-align: right'
      }
    }, {
      tag: 'div',
      attributes: {
        style: 'clear: both;'
      }
    }],
    render: function () {
      this.$el.addClass('btnselect-orderline');
      this.divproduct.text(this.model.get('product').get('_identifier'));
      this.divquantity.text(this.model.printQty());
      this.divprice.text(this.model.printPrice());
      this.divgross.text(this.model.printGross());
      return this;
    }
  });
}());