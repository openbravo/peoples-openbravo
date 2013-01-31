/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone, window, confirm */

(function () {
  var executeWhenDOMReady;

  function triggerReady(models) {
    if (models._LoadOnline && OB.UTIL.queueStatus(models._LoadQueue || {})) {
      models.trigger('ready');
    }
  }

  // global components.
  OB = window.OB || {};

  OB.Model.POSTerminal = OB.Model.Terminal.extend({
    defaults: {
      loginUtilsUrl: '../../org.openbravo.retail.posterminal.service.loginutils'
    }
  });

  // var modelterminal= ;
  OB.POS = {
    modelterminal: new OB.Model.POSTerminal(),
    paramWindow: OB.UTIL.getParameterByName("window") || "retail.pointofsale",
    paramTerminal: OB.UTIL.getParameterByName("terminal") || "POS-1",
    //    terminal: new OB.UI.Terminal({
    //      test:'1',
    //      terminal: this.modelterminal
    //    }),
    hrefWindow: function (windowname) {
      return '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal) + '&window=' + window.encodeURIComponent(windowname);
    },
    logout: function (callback) {
      this.modelterminal.logout();
    },
    lock: function (callback) {
      this.modelterminal.lock();
    },
    windows: null,
    navigate: function (route) {
      //HACK -> when f5 in login page
      //the route to navigate is the same that we are.
      //Backbone doesn't navigates
      //With this hack allways navigate.
      if (route === Backbone.history.fragment) {
        Backbone.history.fragment = '';
      }
      this.modelterminal.router.navigate(route, {
        trigger: true
      });
    },
    registerWindow: function (windowName, window) {
      this.modelterminal.registerWindow(windowName, window);

    },
    cleanWindows: function () {
      this.windows = new(Backbone.Collection.extend({
        comparator: function (window) {
          // sorts by menu position, 0 if not defined
          var position = window.get('menuPosition');
          return position ? position : 0;
        }
      }))();
    }
  };

  OB.POS.terminal = new OB.UI.Terminal({
    terminal: OB.POS.modelterminal
  });

  OB.Constants = {
    FIELDSEPARATOR: '$',
    IDENTIFIER: '_identifier'
  };

  OB.Format = window.OB.Format || {};

  OB.I18N = window.OB.I18N || {};

  OB.I18N.labels = {};

  executeWhenDOMReady = function () {
    if (document.readyState === "interactive" || document.readyState === "complete") {
      OB.POS.modelterminal.off('loginfail');
    } else {
      setTimeout(function () {
        executeWhenDOMReady();
      }, 50);
    }
  };
  executeWhenDOMReady();

}());