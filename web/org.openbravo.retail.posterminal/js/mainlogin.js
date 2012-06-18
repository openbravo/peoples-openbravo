/*global require,$, _, Backbone, window, confirm, OB */

require.config({
  paths: {
    jQuery: 'libs/jquery/jquery',
    Underscore: 'libs/underscore/underscore',
    Backbone: 'libs/backbone/backbone',
    text: 'libs/text-1.0.8.min'
  }
});

require(['builder', 'windows/login', 'utilitiesui', 'arithmetic', 'datasource', 'model/terminal', 'components/terminal'], function(B, login) {

  var modelterminal = new OB.MODEL.Terminal();

  var terminal = new OB.COMP.Terminal($("#terminal"), $('#yourcompany'), $('#yourcompanyproperties'), $('#loggeduser'), $('#loggeduserproperties'));
  terminal.setModel(modelterminal);

  // alert all errors
  window.onerror = function (e) {
    if (typeof(e) === 'string') {
      OB.UTIL.showError(e);
    }
  };

  // global components.
  OB.POS = {
      modelterminal: modelterminal,
      paramWindow: OB.UTIL.getParameterByName("window") || "retail.pointofsale",
      paramTerminal: OB.UTIL.getParameterByName("terminal") || "POS-1",
      hrefWindow: function (windowname) {
        return '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal) + '&window=' + window.encodeURIComponent(windowname);
      },
      logout: function (callback) {
        if (window.confirm('Are you sure that you want to logout from the application?')) {
          modelterminal.logout();
        }
      }
  };

  OB.Constants = {
      FIELDSEPARATOR: '$'
  };

  OB.I18N = window.OB.I18N || {};

  OB.I18N.labels = {};

  OB.I18N.getLabel = function(key, params, object, property) {
    if (!OB.I18N.labels[key]) {
      if (object && property) {
        OB.I18N.getLabelFromServer(key, params, object, property);
      }
      return 'UNDEFINED ' + key;
    }
    var label = OB.I18N.labels[key], i;
    if (params && params.length && params.length > 0) {
        for (i = 0; i < params.length; i++) {
            label = label.replace("%" + i, params[i]);
        }
    }
    if (object && property) {
      if (Object.prototype.toString.call(object[property]) === '[object Function]') {
        object[property](label);
      } else {
        object[property] = label;
      }
    }
    return label;
  };

//  modelterminal.on('ready', function () {
//    // We are Logged !!!
//    $(window).off('keypress');
//    $('#logoutDialogLogout').css('visibility', 'visible');
//
//    // Set Hardware..
//    OB.POS.hwserver = new OB.DS.HWServer(modelterminal.get('terminal').hardwareurl, modelterminal.get('terminal').scaleurl);
//
//    // Set Arithmetic properties:
//    OB.DEC.setContext(OB.POS.modelterminal.get('currency').pricePrecision, BigDecimal.prototype.ROUND_HALF_EVEN);
//
//    var webwindowname = "../../" + OB.POS.paramWindow;
//
//    require([webwindowname], function (webwindow) { // load window...
//      var c = _.extend({}, Backbone.Events);
//      $("#containerwindow").empty().append((new webwindow(c)).$el);
//      c.trigger('domready');
//    });
//  });
//
//  modelterminal.on('loginsuccess', function () {
//    modelterminal.load();
//  });
//
//  modelterminal.on('logout', function () {
//
//    // Logged out. go to login window
//    modelterminal.off('loginfail');
//    $(window).off('keypress');
//    $('#logoutDialogLogout').css('visibility', 'hidden');
//
////    var c = _.extend({}, Backbone.Events);
////    $("#containerwindow").empty().append((new login(c)).$el);
////    c.trigger('domready'); window.location=
//
//    // Redirect to login window
//    localStorage.setItem('target-window', window.location.href);
//    window.location = window.location.pathname + 'login';
//  });
//
  $(document).ready(function () {
    // Entry Point
    // modelterminal.load();

    modelterminal.off('loginfail');
    $(window).off('keypress');

    function renderLoginPage() {
      var c = _.extend({}, Backbone.Events);
      $("#containerwindow").empty().append((new login(c)).$el);
      c.trigger('domready');
    }

    function checkPOSTerminal() {
      $.ajax({
        url: '../../org.openbravo.service.retail.posterminal.loginutils',
        contentType: 'application/json;charset=utf-8',
        dataType: 'json',
        data: {
          command: 'checkPOSTerminal',
          terminalName: OB.POS.paramTerminal
        },
        error: function (jqXHR, textStatus, errorThrown) {
          OB.UTIL.showError(errorThrown + ": " + this.url);
        },
        success: function (data, textStatus, jqXHR) {
          if ((data.response[0].data[0].strClient) !== 'none') {
            renderLoginPage();
          } else {
            OB.UTIL.showError("Terminal does not exists: " + OB.POS.paramTerminal);
          }
        }
      });
    }

    function getLoginLabels() {
      $.ajax({
        url: '../../org.openbravo.service.retail.posterminal.loginutils',
        contentType: 'application/json;charset=utf-8',
        dataType: 'json',
        data: {
          command: 'getLoginLabels',
          terminalName: OB.POS.paramTerminal
        },
        error: function (jqXHR, textStatus, errorThrown) {
          OB.UTIL.showError(errorThrown + ": " + this.url);
        },
        success: function (data, textStatus, jqXHR) {
          OB.I18N.labels = data.response[0].data[0];
          checkPOSTerminal();
        }
      });
    }

    getLoginLabels();
  });

});