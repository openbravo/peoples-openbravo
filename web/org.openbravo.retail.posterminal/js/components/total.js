/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, Backbone */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  enyo.kind({
    name: 'OB.UI.Total',
    tag: 'span',
    attributes: {
      style: 'font-weight:bold;'
    },
    initComponents: function() {
      //FIXME
      this.setContent('100.25');
      //TODO
      //this.receipt.on('change:gross', function() {
      //  this.render();
      //}, this)
    }
  });

  OB.COMP.Total = Backbone.View.extend({
    tagName: 'span',
    attributes: {
      'style': 'font-weight:bold;'
    },
    initialize: function() {
      this.receipt = this.options.root.modelorder;

      this.receipt.on('change:gross', function() {
        this.render();
      }, this);
    },
    render: function() {
      this.$el.text(this.receipt.printTotal());
      return this;
    }
  });
}());