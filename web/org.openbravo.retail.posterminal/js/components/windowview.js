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
  name: 'OB.UI.WindowView',
  windowmodel: null,
  create: function() {

    this.inherited(arguments);
    this.model = new this.windowmodel();
    this.model.on('ready', function() {
      if (this.init) {
        this.init();
      }
      OB.POS.modelterminal.trigger('window:ready', this);
    }, this);
    this.model.load();
  },
  statics: {
    initChildren: function(view) {
      if (!view || !view.getComponents) {
        return;
      }
      enyo.forEach(view.getComponents(), function(child) {
        OB.UI.WindowView.initChildren(child);
        if (child.init) {
          child.init();
        }
      });
    },
    destroyModels: function(view) {
      var p;
      if (!view) {
        return;
      }
      for (p in view) {
        if (view.hasOwnProperty(p) && view[p] && view[p].off) {
          view[p].off();
          delete view[p];
        }
      }
      if (!view.getComponents) {
        return;
      }

      enyo.forEach(view.getComponents(), function(child) {
        OB.UI.WindowView.destroyModels(child);
      });
    }
  },
  init: function() {
    // Calling init in sub components
    OB.UI.WindowView.initChildren(this);
    //    enyo.forEach(this.getComponents(), function(component) {
    //      if (component.init) {
    //        component.init();
    //      }
    //    });
  },

  destroy: function() {
    this.model.setOff();
    this.model = null;
    OB.UI.WindowView.destroyModels(this);

    this.inherited(arguments);
  }
});