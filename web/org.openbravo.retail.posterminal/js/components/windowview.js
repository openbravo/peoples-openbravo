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
  create: function () {

    this.inherited(arguments);
    this.model = new this.windowmodel();
    this.model.on('ready', function () {
      if (this.init) {
        this.init();
      }
      OB.POS.modelterminal.trigger('window:ready', this);
    }, this);
    this.model.load();
  },
  statics: {
    initChildren: function (view, model) {
      if (!view || !view.getComponents) {
        return;
      }
      enyo.forEach(view.getComponents(), function (child) {
        OB.UI.WindowView.initChildren(child, model);
        if (child.init) {
          child.init(model);
        }
      });
    },
    destroyModels: function (view) {
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

      enyo.forEach(view.getComponents(), function (child) {
        OB.UI.WindowView.destroyModels(child);
      });
    }
  },
  init: function () {
    //Modularity
    //Add new dialogs
    var customDialogsContainerName = this.name + "_customDialogsContainer",
        view = this;
    this.createComponent({
      name: customDialogsContainerName,
      initComponents: function () {
        if (OB.Customizations) {
          if (OB.Customizations[this.parent.name]) {
            if (OB.Customizations[this.parent.name].dialogs) {
              enyo.forEach(OB.Customizations[this.parent.name].dialogs, function (dialog, component) {
                this.createComponent(dialog);
              }, this);
            }
          }
        }
      }
    });

    // Calling init in sub components
    OB.UI.WindowView.initChildren(this, this.model);
    //    enyo.forEach(this.getComponents(), function(component) {
    //      if (component.init) {
    //        component.init();
    //      }
    //    });
  },

  destroy: function () {
    this.model.setOff();
    this.model = null;
    OB.UI.WindowView.destroyModels(this);

    this.inherited(arguments);
  }
});