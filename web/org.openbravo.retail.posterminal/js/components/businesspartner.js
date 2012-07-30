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
