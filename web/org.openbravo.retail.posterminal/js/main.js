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
	console.log('Entry Point1');

  var modelterminal = OB.POS.modelterminal;

  var terminal = new OB.COMP.Terminal($("#terminal"), $('#yourcompany'), $('#yourcompanyproperties'), $('#loggeduser'), $('#loggeduserproperties'));
  terminal.setModel(modelterminal);

  var modalProfile = new OB.COMP.ModalProfile($('#dialogsContainer'));
  modalProfile.setModel(modelterminal);

  // alert all errors
  window.onerror = function (e) {
    if (typeof(e) === 'string') {
      OB.UTIL.showError(e);
    }
  };

//  // global components.
//  debugger;
//  OB.POS = {
//    modelterminal: modelterminal,
//    paramWindow: OB.UTIL.getParameterByName("window") || "retail.pointofsale",
//    paramTerminal: OB.UTIL.getParameterByName("terminal") || "POS-1",
//    hrefWindow: function (windowname) {
//      return '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal) + '&window=' + window.encodeURIComponent(windowname);
//    },
//    logout: function (callback) {
//      modelterminal.logout();
//    },
//    lock: function (callback) {
//      modelterminal.lock();
//    },
//    paymentProviders: {},
//    windows: {}
//  };

  modelterminal.on('ready', function () {
    var webwindow, w,
        c = _.extend({}, Backbone.Events),
        terminal = OB.POS.modelterminal.get('terminal'),
        queue = {}, emptyQueue = false;

    // We are Logged !!!
    $(window).off('keypress');
    $('#logoutlink').css('visibility', 'visible');

    function createWindow() {
      w = new webwindow(c);
      if (w.render) {
        w = w.render();
      }
      $("#containerWindow").empty().append(w.$el);
      c.trigger('domready');
      OB.UTIL.showLoading(false);
    }

    function searchCurrentBP (){
      function errorCallback(tx, error) {
        OB.UTIL.showError("OBDAL error: " + error);
      }

      function successCallbackBPs(dataBps) {
        if (dataBps){
          OB.POS.modelterminal.set('businessPartner', dataBps);
          createWindow();
        }
      }
      OB.Dal.get(OB.Model.BusinessPartner, OB.POS.modelterminal.get('businesspartner'), successCallbackBPs, errorCallback);
    }

    // Set Hardware..
    OB.POS.hwserver = new OB.DS.HWServer(terminal.hardwareurl, terminal.scaleurl);

    // Set Arithmetic properties:
    OB.DEC.setContext(OB.POS.modelterminal.get('currency').pricePrecision, BigDecimal.prototype.ROUND_HALF_EVEN);

    webwindow = OB.POS.windows[OB.POS.paramWindow];

    if (webwindow) {
      if (OB.DATA[OB.POS.paramWindow]) {
        // loading/refreshing required data/models for window
        _.each(OB.DATA[OB.POS.paramWindow], function (model) {
        	console.log('loading model', model.prototype.modelName,model.prototype.local);
          var ds;
          if (model.prototype.local) {
        	  console.log('local');
            OB.Dal.initCache(model, [], function () { window.console.log('init success: ' + model.prototype.modelName);}, function () { window.console.error('init error', arguments);});
          } else {
        	  console.log('no local');
            ds = new OB.DS.DataSource(new OB.DS.Request(model, terminal.client, terminal.organization, terminal.id));
            ds.on('ready', function () {

              queue[model.prototype.source] = true;
              emptyQueue = OB.UTIL.queueStatus(queue);

              if(emptyQueue) {
                searchCurrentBP();
              }
            });
            ds.load();
            queue[model.prototype.source] = false;
          }
        });
      } else {
        createWindow();
      }
    } else {
      OB.UTIL.showLoading(false);
      alert(OB.I18N.getLabel('OBPOS_WindowNotFound', [OB.POS.paramWindow]));
    }
  });

  modelterminal.on('loginsuccess', function () {
    modelterminal.load();
  });

  modelterminal.on('logout', function () {

    // Logged out. go to login window
    modelterminal.off('loginfail');
    $(window).off('keypress');
    $('#logoutlink').css('visibility', 'hidden');

//    var c = _.extend({}, Backbone.Events);
//    $("#containerWindow").empty().append((new login(c)).$el);
//    c.trigger('domready'); window.location=

    // Redirect to login window
    localStorage.setItem('target-window', window.location.href);
    //window.location = window.location.pathname + 'login.jsp' + '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal);
  });

 // function () {
    // Entry Point
  console.log('Entry Point');
    $('#dialogsContainer').append(B({kind: OB.COMP.ModalLogout}).$el);
    modelterminal.load();

    modelterminal.on('online', function () {
      OB.UTIL.setConnectivityLabel('Online');
    });

    modelterminal.on('offline', function () {
      OB.UTIL.setConnectivityLabel('Offline');
    });

    OB.UTIL.checkConnectivityStatus(); //Initial check;
    setInterval(OB.UTIL.checkConnectivityStatus, 5000);

    beforeUnloadCallback = function() {
      if (!OB.POS.modelterminal.get('connectedToERP')) {
        return OB.I18N.getLabel('OBPOS_ShouldNotCloseWindow'); 
      }
    };

    $(window).on('beforeunload', function() {
      if (!OB.POS.modelterminal.get('connectedToERP')) {
        return OB.I18N.getLabel('OBPOS_ShouldNotCloseWindow');
      }
    });
 // }

}());