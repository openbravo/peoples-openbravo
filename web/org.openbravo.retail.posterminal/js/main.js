/*global require,$, _, Backbone, window, confirm, OB, localStorage */

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
      paramWindow: OB.UTIL.getParameterByName("window") || "org.openbravo.retail.posterminal/js/windows/pointofsale",
      paramTerminal: OB.UTIL.getParameterByName("terminal") || "POS-1",
      hrefWindow: function (windowname) {
        return '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal) + '&window=' + window.encodeURIComponent(windowname);
      },
      logout: function (callback) {
        modelterminal.logout();
      }
  };

  modelterminal.on('ready', function () {
    // We are Logged !!!
    $(window).off('keypress');
    $('#logoutlink').css('visibility', 'visible');

    // Set Hardware..
    OB.POS.hwserver = new OB.DS.HWServer(modelterminal.get('terminal').hardwareurl, modelterminal.get('terminal').scaleurl);

    // Set Arithmetic properties:
    OB.DEC.setContext(OB.POS.modelterminal.get('currency').pricePrecision, BigDecimal.prototype.ROUND_HALF_EVEN);

    var webwindowname = "../../" + OB.POS.paramWindow;

    require([webwindowname], function (webwindow) { // load window...
      var c = _.extend({}, Backbone.Events);
      $("#containerwindow").empty().append((new webwindow(c)).$el);
      c.trigger('domready');
    });
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
//    $("#containerwindow").empty().append((new login(c)).$el);
//    c.trigger('domready'); window.location=

    // Redirect to login window
    localStorage.setItem('target-window', window.location.href);
    window.location = window.location.pathname + 'login.jsp' + '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal);
  });

  $(document).ready(function () {
    // Entry Point
    modelterminal.load();
  });

});