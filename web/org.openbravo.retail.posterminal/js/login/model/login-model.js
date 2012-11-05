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

  function triggerReady(models) {
    if (models._LoadOnline && OB.UTIL.queueStatus(models._LoadQueue || {})) {
      models.trigger('ready');
    }
  }

  // global components.
  OB = window.OB || {};
  OB.Model = OB.Model || {};
  OB.Model.Util = {
    loadModels: function (online, models, data, incremental) {
      var somethigToLoad = false;

      models._LoadOnline = online;

      if (models.length === 0) {
        triggerReady(models);
        return;
      }

      _.each(models, function (item) {
        var ds, load;

        load = (online && item.prototype.online) || (!online && !item.prototype.online);
        //TODO: check permissions
        if (load) {
          if (item.prototype.local) {
            OB.Dal.initCache(item, [], function () {
              // window.console.log('init success: ' + item.prototype.modelName);
            }, function () {
              window.console.error('init error', arguments);
            });
          } else {
            ds = new OB.DS.DataSource(new OB.DS.Request(item, OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization, OB.POS.modelterminal.get('terminal').id, incremental));
            somethigToLoad = true;
            models._LoadQueue = models._LoadQueue || {};
            models._LoadQueue[item.prototype.modelName] = false;
            ds.on('ready', function () {
              if (data) {
                data[item.prototype.modelName] = new Backbone.Collection(ds.cache);
              }
              models._LoadQueue[item.prototype.modelName] = true;
              if (incremental) {
                window.localStorage.setItem('POSLastIncRefresh', new Date().getTime());
              } else {
                window.localStorage.setItem('POSLastTotalRefresh', new Date().getTime());
              }
              triggerReady(models);
            });
            ds.load(item.params, incremental);
          }
        }
      });
      if (!somethigToLoad) {
        triggerReady(models);
      }
    }
  };

  OB.Router = Backbone.Router.extend({
    routes: {
      main: 'main'
    },

    main: function (query, page) {},

    renderGenericWindow: function (windowName) {
      this.terminal.renderGenericWindow(windowName);
    }
  });

  // var modelterminal= ;
  OB.POS = {
    modelterminal: new OB.Model.Terminal(),
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

  OB.I18N.getLabel = function (key, params, object, property) {
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

  $(document).ready(function () {

    OB.POS.modelterminal.off('loginfail');
    $(window).off('keypress');
    $(window).off('keydown');

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
    OB.POS.modelterminal.on('online', function () {
      OB.UTIL.setConnectivityLabel('Online');
    });

    OB.POS.modelterminal.on('offline', function () {
      OB.UTIL.setConnectivityLabel('Offline');
    });

  });

}());