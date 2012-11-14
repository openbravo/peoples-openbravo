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

  OB.UI.UTILS = {};
  OB.UI.UTILS.domIdEnyoReference = {};
  // alert all errors
  window.onerror = function (e) {
    if (typeof (e) === 'string') {
      OB.UTIL.showError(e);
    }
  };

  modelterminal.on('ready', function () {
    var webwindow, terminal = OB.POS.modelterminal.get('terminal');

    // We are Logged !!!
    $(window).off('keypress');
    $(window).off('keydown');

    function searchCurrentBP() {
      function errorCallback(tx, error) {
        OB.UTIL.showError("OBDAL error: " + error);
      }

      function successCallbackBPs(dataBps) {
        if (dataBps) {
          OB.POS.modelterminal.set('businessPartner', dataBps);
          OB.POS.navigate('retail.pointofsale');
        }
      }
      OB.Dal.get(OB.Model.BusinessPartner, OB.POS.modelterminal.get('businesspartner'), successCallbackBPs, errorCallback);
    }

    // Set Hardware..
    OB.POS.hwserver = new OB.DS.HWServer(terminal.hardwareurl, terminal.scaleurl);

    // Set Arithmetic properties:
    OB.DEC.setContext(OB.POS.modelterminal.get('currency').pricePrecision, BigDecimal.prototype.ROUND_HALF_EVEN);
    webwindow = OB.POS.windows.where({
      route: OB.POS.paramWindow
    })[0].get('windowClass');

    if (webwindow) {
      if (OB.POS.modelterminal.hasPermission(OB.POS.paramWindow)) {
        searchCurrentBP();
      } else {
        OB.UTIL.showLoading(false);
        alert(OB.I18N.getLabel('OBPOS_WindowNotPermissions', [OB.POS.paramWindow]));
      }
    } else {
      OB.UTIL.showLoading(false);
      alert(OB.I18N.getLabel('OBPOS_WindowNotFound', [OB.POS.paramWindow]));
    }
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
    $(window).off('keypress');
    $(window).off('keydown');

    // Redirect to login window
    localStorage.setItem('target-window', window.location.href);
    window.location = window.location.pathname + '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal);
  });

  executeWhenDOMReady = function () {
    if (document.readyState === "interactive" || document.readyState === "complete") {
      modelterminal.load();
      modelterminal.on('ready', function () {
        OB.POS.terminal.$.dialogsContainer.createComponent({
          kind: 'OB.UI.ModalLogout',
          name: 'logoutDialog'
        }).render();
        OB.POS.terminal.$.dialogsContainer.createComponent({
          kind: 'OB.UI.ModalProfile',
          name: 'profileDialog'
        }).render();
      });

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