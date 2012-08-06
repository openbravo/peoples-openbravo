/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone, window, confirm */

(function() {



  // global components.
  OB = window.OB || {};
  OB.Model = OB.Model || {};
  OB.Model.Util = {
    loadModels: function(online, models, data) {
      var queue = {};
      if (models.length === 0) {
        models.trigger('ready');
      }

      _.each(models, function(item) {
        var ds, load;

        load = (online && item.prototype.online) || (!online && !item.prototype.online);
        //TODO: check permissions
        if (load) {
          if (item.prototype.local) {
            OB.Dal.initCache(item, [], function() {
              window.console.log('init success: ' + item.prototype.modelName);
            }, function() {
              window.console.error('init error', arguments);
            });
          } else {
            ds = new OB.DS.DataSource(new OB.DS.Request(item, OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization, OB.POS.modelterminal.get('terminal').id));

            queue[item.prototype.modelName] = false;
            ds.on('ready', function() {
              if (data) {
                data[item.prototype.modelName] = new Backbone.Collection(ds.cache);
              }
              queue[item.prototype.modelName] = true;
              if (OB.UTIL.queueStatus(queue)) {
                models.trigger('ready');
              }
            });
            ds.load(item.params);
          }
        }
      });
    }
  };

  OB.Router = Backbone.Router.extend({
    routes: {
      main: 'main'
    },

    main: function(query, page) {},

    renderGenericWindow: function(windowName) {
      this.terminal.renderGenericWindow(windowName);
    }
  });

  // var modelterminal= ;
  OB.POS = {
    modelterminal: new OB.Model.Terminal(),
    paramWindow: OB.UTIL.getParameterByName("window") || "retail.pointofsale",
    paramTerminal: OB.UTIL.getParameterByName("terminal") || "POS-1",
//    terminal: new OB.UI.Terminal({
//    	test:'1',
//      terminal: this.modelterminal
//    }),
    hrefWindow: function(windowname) {
      return '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal) + '&window=' + window.encodeURIComponent(windowname);
    },
    logout: function(callback) {
      this.modelterminal.logout();
    },
    lock: function(callback) {
      this.modelterminal.lock();
    },
    paymentProviders: [],
    windows: {},
    navigate: function(route) {
      this.modelterminal.router.navigate(route, {
        trigger: true
      });
    },
    registerWindow: function(windowName, window) {
      this.modelterminal.registerWindow(windowName, window);

    }
  };
  
  OB.POS.terminal = new OB.UI.Terminal({terminal:OB.POS.modelterminal});

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
    var label = OB.I18N.labels[key],
        i;
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

  $(document).ready(function() {

    OB.POS.modelterminal.off('loginfail');
    $(window).off('keypress');

    //    function renderLoginPage() {
    //      var c = _.extend({}, Backbone.Events);
    //      $("#containerWindow").empty().append((new OB.COMP.Login(c)).$el);
    //      c.trigger('domready');
    //    }
    //    function preRenderActions() {
    //      $.ajax({
    //        url: '../../org.openbravo.retail.posterminal.service.loginutils',
    //        contentType: 'application/json;charset=utf-8',
    //        dataType: 'json',
    //        data: {
    //          command: 'preRenderActions',
    //          terminalName: OB.POS.paramTerminal
    //        },
    //        error: function (jqXHR, textStatus, errorThrown) {
    //          OB.UTIL.showError(errorThrown + ": " + this.url);
    //        },
    //        success: function (data, textStatus, jqXHR) {
    //          OB.I18N.labels = data.response[0].data[1];
    //          OB.Format = data.response[0].data[2];
    //          if ((data.response[0].data[0].strClient) !== 'none') {
    //            renderLoginPage();
    //          } else {
    //            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_NO_POS_TERMINAL_TITLE',[OB.POS.paramTerminal]));
    //          }
    //        }
    //      });
    //    }
    //
    //    preRenderActions();
    OB.POS.modelterminal.on('online', function() {
      OB.UTIL.setConnectivityLabel('Online');
    });

    OB.POS.modelterminal.on('offline', function() {
      OB.UTIL.setConnectivityLabel('Offline');
    });

    OB.UTIL.checkConnectivityStatus(); //Initial check;
    setInterval(OB.UTIL.checkConnectivityStatus, 5000);
  });

}());