/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  enyo.kind({
    kind: 'OB.UI.SmallButton',
    name: 'OB.UI.BusinessPartner',
    classes: 'btnlink btnlink-small btnlink-gray',
    published: {
      customer: null
    },
    attributes: {
      'data-toggle': 'modal',
      'href': '#modalcustomer'
    },
    initComponents: function() {
    },
    renderCustomer: function(newCustomer){
      this.setContent(newCustomer);
    },
    customerChanged: function (oldValue){
      this.renderCustomer(this.customer.get('_identifier'));
      this.customer.on('change:id', function (model) {
        this.renderCustomer(model.get('_identifier'));
      }, this);
    }
  });
  
  //  Refactored as enyo view -> OB.UI.BusinessPartner
  OB.COMP.BusinessPartner = OB.COMP.SmallButton.extend({
      className: 'btnlink btnlink-small btnlink-gray',
      attributes: {'href': '#modalcustomer', 'data-toggle': 'modal'},
      initialize: function () {
        OB.COMP.SmallButton.prototype.initialize.call(this); // super.initialize();
        this.receipt = this.options.root.modelorder;
        this.receipt.on('clear change:bp change:bploc', function () {
          this.$el.text(this.receipt.get('bp') ? this.receipt.get('bp').get('_identifier') : '');
        }, this);
      }
  });
}());
