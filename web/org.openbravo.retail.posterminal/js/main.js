/*global B, $, _, Backbone, window, confirm, OB, localStorage */

(function () {

  var modelterminal = new OB.MODEL.Terminal();

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

  // global components.
  OB.POS = {
    modelterminal: modelterminal,
    paramWindow: OB.UTIL.getParameterByName("window") || "retail.pointofsale",
    paramTerminal: OB.UTIL.getParameterByName("terminal") || "POS-1",
    hrefWindow: function (windowname) {
      return '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal) + '&window=' + window.encodeURIComponent(windowname);
    },
    logout: function (callback) {
      modelterminal.logout();
    },
    lock: function (callback) {
      modelterminal.lock();
    },
    paymentProviders: {},
    windows: {}         
  };

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
          var ds;
          if (model.prototype.local) {
            OB.Dal.initCache(model, [], function () { window.console.log('init success');}, function () { window.console.error('init error', arguments);});
          } else {
            ds = new OB.DS.DataSource(new OB.DS.Request(model, terminal.client, terminal.organization));
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
    window.location = window.location.pathname + 'login.jsp' + '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal);
  });

  $(document).ready(function () {
    // Entry Point
    $('#dialogsContainer').append(B({kind: OB.COMP.ModalLogout}).$el);
    modelterminal.load();

    modelterminal.on('online', function () {
      $($('#online > span')[0]).css("background-image", "url('./img/login-connected.png')");
      $($('#online > span')[1]).text(OB.I18N.getLabel('OBPOS_Online'));
    });

    modelterminal.on('offline', function () {
      $($('#online > span')[0]).css("background-image", "url('./img/login-not-connected.png')");
      $($('#online > span')[1]).text(OB.I18N.getLabel('OBPOS_Offline'));
    });
  });

}());