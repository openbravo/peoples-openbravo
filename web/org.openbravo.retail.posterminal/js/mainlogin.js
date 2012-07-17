/*global B, $, _, Backbone, window, confirm */

(function () {

  var modelterminal = new OB.Model.Terminal();
  
  // global components.
  OB = window.OB || {};
  OB.POS = {
      modelterminal: modelterminal,
      paramWindow: OB.UTIL.getParameterByName("window") || "retail.pointofsale",
      paramTerminal: OB.UTIL.getParameterByName("terminal") || "POS-1",
      hrefWindow: function (windowname) {
        return '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal) + '&window=' + window.encodeURIComponent(windowname);
      }
  };

  OB.Constants = {
      FIELDSEPARATOR: '$'
  };

  OB.Format = window.OB.Format || {};

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

  $(document).ready(function () {

    modelterminal.off('loginfail');
    $(window).off('keypress');

    function renderLoginPage() {
      var c = _.extend({}, Backbone.Events);
      $("#containerWindow").empty().append((new OB.COMP.Login(c)).$el);
      c.trigger('domready');
    }

    function preRenderActions() {
      $.ajax({
        url: '../../org.openbravo.retail.posterminal.service.loginutils',
        contentType: 'application/json;charset=utf-8',
        dataType: 'json',
        data: {
          command: 'preRenderActions',
          terminalName: OB.POS.paramTerminal
        },
        error: function (jqXHR, textStatus, errorThrown) {
          OB.UTIL.showError(errorThrown + ": " + this.url);
        },
        success: function (data, textStatus, jqXHR) {
          OB.I18N.labels = data.response[0].data[1];
          OB.Format = data.response[0].data[2];
          if ((data.response[0].data[0].strClient) !== 'none') {
            renderLoginPage();
          } else {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_NO_POS_TERMINAL_TITLE',[OB.POS.paramTerminal]));
          }
        }
      });
    }

    preRenderActions();

    modelterminal.on('online', function () {
      OB.UTIL.setConnectivityLabel('Online');
    });

    modelterminal.on('offline', function () {
      OB.UTIL.setConnectivityLabel('Offline');
    });

    OB.UTIL.checkConnectivityStatus(); //Initial check;
    setInterval(OB.UTIL.checkConnectivityStatus, 30*1000);
  });

}());