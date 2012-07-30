/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  OB.UI.OrderDetails = Backbone.View.extend({
    tagName: 'div',
    attributes: {
      'style': 'float:left; padding: 15px 15px 5px 10px; font-weight: bold; color: #6CB33F;'
    },
    initialize: function() {

      this.receipt = this.options.root.modelorder;
      this.receipt.on('clear change:orderDate change:documentNo', function() {
        this.$el.text(OB.I18N.formatHour(this.receipt.get('orderDate')) + ' - ' + this.receipt.get('documentNo'));
      }, this);
    }
  });
}());