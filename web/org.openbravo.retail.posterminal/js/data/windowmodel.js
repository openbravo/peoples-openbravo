/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.Model.WindowModel = Backbone.Model.extend({
  data: {},

  initialize: function() {
    var me = this,
        queue = {};
    if (!this.models) {
      this.models = [];
    }

    _.extend(this.models, Backbone.Events);

    this.models.on('ready', function() {
      this.trigger('ready');
      if (this.init) {
        this.init();
      }
    }, this);

    OB.Model.Util.loadModels(true, this.models, this.data);

    //TODO: load offline models when regesitering window
  },

  getData: function(dsName) {
    return this.data[dsName];
  }
});