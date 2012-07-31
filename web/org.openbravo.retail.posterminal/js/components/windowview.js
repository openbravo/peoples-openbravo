/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.UI.WindowView = Backbone.View.extend({
  windowmodel: null,

  initialize: function() {
    var me = this;
    this.model = new this.windowmodel();
    this.model.on('ready', function() {
      OB.UTIL.initContentView(me);
      if (me.init) {
        me.init();
      }
      OB.POS.modelterminal.trigger('window:ready', me);
    });
  }
});