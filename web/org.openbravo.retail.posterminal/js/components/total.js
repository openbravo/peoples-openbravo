/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.Total = Backbone.View.extend({
    tagName: 'span',
    initialize: function () {

      this.totalgross = $('<strong/>');
      this.$el.append(this.totalgross);

      // Set Model
      this.receipt = this.options.modelorder;

      this.receipt.on('change:gross', function() {
        this.totalgross.text(this.receipt.printTotal());
      }, this);

      // Initial total display
      this.totalgross.text(this.receipt.printTotal());
    }
  });
}());