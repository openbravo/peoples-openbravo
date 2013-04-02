/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone, window, confirm, OB, localStorage */

(function () {
  var modelterminal = OB.POS.modelterminal,
      executeWhenDOMReady;

  // alert all errors
  window.onerror = function (e) {
    if (typeof (e) === 'string') {
      OB.UTIL.showError(e);
    }
  };

  modelterminal.on('ready', function () {
    var webwindow, terminal = OB.POS.modelterminal.get('terminal');

    // We are Logged !!!
    // Set Hardware..
    OB.POS.hwserver = new OB.DS.HWServer(terminal.hardwareurl, terminal.scaleurl);

    // Set Arithmetic properties:
    OB.DEC.setContext(OB.POS.modelterminal.get('currency').pricePrecision, BigDecimal.prototype.ROUND_HALF_UP);


    // TODO: check permissions: this has been removed because windows are not already loaded
    // at this point
    //    webwindow = OB.MobileApp.model.windows.where({
    //      route: OB.POS.paramWindow
    //    })[0].get('windowClass');
    //
    //    if (webwindow) {
    //      if (OB.POS.modelterminal.hasPermission(OB.POS.paramWindow)) {
    //        OB.POS.navigate('retail.pointofsale');
    //      } else {
    //        OB.UTIL.showLoading(false);
    //        alert(OB.I18N.getLabel('OBPOS_WindowNotPermissions', [OB.POS.paramWindow]));
    //      }
    //    } else {
    //      OB.UTIL.showLoading(false);
    //      alert(OB.I18N.getLabel('OBPOS_WindowNotFound', [OB.POS.paramWindow]));
    //    }
    OB.POS.navigate('retail.pointofsale');
    if (OB.POS.modelterminal.get('loggedOffline') === true) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_OfflineLogin'));
    }

  });

  modelterminal.on('loginsuccess', function () {
    modelterminal.load();
  });

  modelterminal.on('logout', function () {

    // Logged out. go to login window
    modelterminal.off('loginfail');

    // Redirect to login window
    localStorage.setItem('target-window', window.location.href);
    window.location = window.location.pathname + '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal);
  });

  executeWhenDOMReady = function () {
    if (document.readyState === "interactive" || document.readyState === "complete") {
      //modelterminal.load();
      //TODO: this is required but should be changed to make it possible to be included in terminal component


      OB.UTIL.checkConnectivityStatus(); //Initial check;
      setInterval(OB.UTIL.checkConnectivityStatus, 5000);

      window.onbeforeunload = function () {
        if (!OB.POS.modelterminal.get('connectedToERP')) {
          return OB.I18N.getLabel('OBPOS_ShouldNotCloseWindow');
        }
      };
    } else {
      setTimeout(function () {
        executeWhenDOMReady();
      }, 50);
    }
  };
  executeWhenDOMReady();

}());