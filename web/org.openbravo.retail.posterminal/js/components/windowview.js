/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

//OB.UI.WindowView = Backbone.View.extend({
//  windowmodel: null,
//
//  initialize: function() {
//    var me = this;
//    this.model = new this.windowmodel();
//    this.model.on('ready', function() {
//      OB.UTIL.initContentView(me);
//      if (me.init) {
//        me.init();
//      }
//      OB.POS.modelterminal.trigger('window:ready', me);
//    });
//  }
//});
enyo.kind({
  name: 'OB.UI.WindowView',
  windowmodel: null,
  create: function() {
    this.inherited(arguments);
    this.model = new this.windowmodel();
    console.log('model in window created');
    this.model.on('ready', function() {
      if (this.init) {
        this.init();
      }
      OB.POS.modelterminal.trigger('window:ready', this);
    }, this);
  },
  init: function() {
    // Calling init in sub components
    enyo.forEach(this.getComponents(), function(component) {
      if (component.init) {
        component.init();
      }
    });
  }
});