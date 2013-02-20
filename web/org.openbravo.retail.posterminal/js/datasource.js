/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, Backbone, _, enyo */

(function () {

  OB = window.OB || {};
  OB.DS = window.OB.DS || {};

  OB.DS.MAXSIZE = 100;

  function serviceSuccess(inResponse, callback) {
    if (inResponse._entityname) {
      callback([inResponse]);
    } else {
      var response = inResponse.response;
      var status = response.status;
      if (status === 0) {
        callback(response.data, response.message, response.lastUpdated);
      } else if (response.errors) {
        callback({
          exception: {
            message: response.errors.id
          }
        });
      } else {
        callback({
          exception: {
            message: response.error.message
          }
        });
      }
    }
  }

  function serviceError(inResponse, callback, callbackError) {
    if (callbackError) {
      callbackError({
        exception: {
          message: OB.I18N.getLabel('OBPOS_MsgApplicationServerNotAvailable'),
          status: inResponse
        }
      });
    } else {
      callback({
        exception: {
          message: OB.I18N.getLabel('OBPOS_MsgApplicationServerNotAvailable'),
          status: inResponse
        }
      });
    }
  }

  function servicePOST(source, dataparams, callback, callbackError, async) {
    if (async !== false) {
      async = true;
    }
    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.retail.posterminal.service.jsonrest/' + source,
      cacheBust: false,
      sync: !async,
      method: 'POST',
      handleAs: 'json',
      contentType: 'application/json;charset=utf-8',
      data: JSON.stringify(dataparams),
      success: function (inSender, inResponse) {
        serviceSuccess(inResponse, callback);
      },
      fail: function (inSender, inResponse) {
        serviceError(inResponse, callback, callbackError);
      }
    });
    ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
  }

  function serviceGET(source, dataparams, callback, callbackError, async) {
    if (async !== false) {
      async = true;
    }
    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.retail.posterminal.service.jsonrest/' + source + '/' + encodeURI(JSON.stringify(dataparams)),
      cacheBust: false,
      sync: !async,
      method: 'GET',
      handleAs: 'json',
      contentType: 'application/json;charset=utf-8',
      success: function (inSender, inResponse) {
        serviceSuccess(inResponse, callback);
      },
      fail: function (inSender, inResponse) {
        serviceError(inResponse, callback, callbackError);
      }
    });
    ajaxRequest.go().response('success').error('fail');
  }

  // Process object
  OB.DS.Process = function (source) {
    this.source = source;
  };

  OB.DS.Process.prototype.exec = function (params, callback, callbackError, async) {
    var attr;
    var data = {};

    for (attr in params) {
      if (params.hasOwnProperty(attr)) {
        data[attr] = params[attr];
      }
    }

    servicePOST(this.source, data, callback, callbackError, async);
  };

  // Source object
  OB.DS.Request = function (source, client, org, pos, lastUpdated) {
    this.model = source && source.prototype && source.prototype.modelName && source; // we're using a Backbone.Model as source
    this.source = (this.model && this.model.prototype.source) || source; // we're using a plain String as source
    if (!this.source) {
      throw 'A Request must have a source';
    }
    this.client = client;
    this.org = org;
    this.pos = pos;
    this.lastUpdated = lastUpdated;
  };

  OB.DS.Request.prototype.exec = function (params, callback, callbackError, async) {
    var p, i;
    var data = {};

    if (params) {
      p = {};
      for (i in params) {
        if (params.hasOwnProperty(i)) {
          if (typeof params[i] === 'string') {
            p[i] = {
              value: params[i],
              type: 'string'
            };
          } else if (typeof params[i] === 'number') {
            if (params[i] === Math.round(params[i])) {
              p[i] = {
                value: params[i],
                type: 'long'
              };
            } else {
              p[i] = {
                value: params[i],
                type: 'bigdecimal'
              };
            }
          } else if (typeof params[i] === 'boolean') {
            p[i] = {
              value: params[i],
              type: 'boolean'
            };
          } else {
            p[i] = params[i];
          }
        }
      }
      data.parameters = p;
    }

    if (this.client) {
      data.client = this.client;
    }

    if (this.org) {
      data.organization = this.org;
    }

    if (this.pos) {
      data.pos = this.pos;
    }

    if (this.lastUpdated) {
      data.lastUpdated = this.lastUpdated;
    }

    serviceGET(this.source, data, callback, callbackError, async);
  };

  function check(elem, filter) {
    var p;

    for (p in filter) {
      if (filter.hasOwnProperty(p)) {
        if (typeof (filter[p]) === 'object') {
          return check(elem[p], filter[p]);
        } else {
          if (filter[p].substring(0, 2) === '%i') {
            if (!new RegExp(filter[p].substring(2), 'i').test(elem[p])) {
              return false;
            }
          } else if (filter[p].substring(0, 2) === '%%') {
            if (!new RegExp(filter[p].substring(2)).test(elem[p])) {
              return false;
            }
          } else if (filter[p] !== elem[p]) {
            return false;
          }
        }
      }
    }
    return true;
  }

  function findInData(data, filter) {
    var i, max;

    if ($.isEmptyObject(filter)) {
      return {
        exception: 'filter not defined'
      };
    } else {
      for (i = 0, max = data.length; i < max; i++) {
        if (check(data[i], filter)) {
          return data[i];
        }
      }
      return null;
    }
  }

  function execInData(data, filter, filterfunction) {
    var newdata, info, i, max, f, item;

    if ($.isEmptyObject(filter) && !filterfunction) {
      return {
        data: data.slice(0, OB.DS.MAXSIZE),
        info: (data.length > OB.DS.MAXSIZE ? 'OBPOS_DataMaxReached' : null)
      };
    } else {
      f = filterfunction ||
      function (item) {
        return item;
      };
      newdata = [];
      info = null;
      for (i = 0, max = data.length; i < max; i++) {
        if (check(data[i], filter)) {
          item = f(data[i]);
          if (item) {
            if (newdata.length >= OB.DS.MAXSIZE) {
              info = 'OBPOS_DataMaxReached';
              break;
            }
            newdata.push(data[i]);
          }
        }
      }
      return {
        data: newdata,
        info: info
      };
    }
  }

  // DataSource objects
  // OFFLINE GOES HERE
  OB.DS.DataSource = function (request) {
    this.request = request;
    this.cache = null;
  };
  _.extend(OB.DS.DataSource.prototype, Backbone.Events);

  OB.DS.DataSource.prototype.load = function (params, incremental) {
    var me = this;
    this.cache = null;

    this.request.exec(params, function (data, message, lastUpdated) {

      if (data.exception) {
        throw data.exception;
      }

      if (lastUpdated) {
        window.localStorage.setItem('lastUpdatedTimestamp' + me.request.model.prototype.modelName, lastUpdated);
      }

      me.cache = data;

      if (me.request.model && !me.request.model.prototype.online) {
        OB.Dal.initCache(me.request.model, data, function () {
          me.trigger('ready');
        }, function () {
          window.console.error(arguments, me.request.model.prototype);
        }, incremental);
      } else {
        me.trigger('ready');
      }
    });
  };

  OB.DS.DataSource.prototype.find = function (filter, callback) {
    if (this.cache) {
      callback(findInData(this.cache, filter));
    } else {
      this.on('ready', function () {
        callback(findInData(this.cache, filter));
      }, this);
    }
  };

  OB.DS.DataSource.prototype.exec = function (filter, callback) {
    if (this.cache) {
      var result1 = execInData(this.cache, filter);
      callback(result1.data, result1.info);
    } else {
      this.on('ready', function () {
        var result2 = execInData(this.cache, filter);
        callback(result2.data, result2.info);
      }, this);
    }
  };

  // HWServer
  OB.DS.HWResource = function (res) {
    this.resource = res;
    this.resourcedata = null;
  };

  OB.DS.HWResource.prototype.getData = function (callback) {
    if (this.resourcedata) {
      callback(this.resourcedata);
    } else {
      OB.UTIL.loadResource(this.resource, function (data) {
        this.resourcedata = data;
        callback(this.resourcedata);
      }, this);
    }
  };

  OB.DS.HWServer = function (url, scaleurl) {
    this.url = url;
    this.scaleurl = scaleurl;
  };

  OB.DS.HWServer.prototype.getWeight = function (callback) {
    if (this.scaleurl) {
      var me = this;
      var ajaxRequest = new enyo.Ajax({
        url: me.scaleurl,
        cacheBust: false,
        method: 'GET',
        handleAs: 'json',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        success: function (inSender, inResponse) {
          callback(inResponse);
        },
        fail: function (inSender, inResponse) {
          if (callback) {
            callback({
              exception: {
                message: (OB.I18N.getLabel('OBPOS_MsgScaleServerNotAvailable'))
              },
              result: 1
            });
          }
        }
      });
      ajaxRequest.go().response('success').error('fail');
    } else {
      callback({
        result: 1
      });
    }
  };

  OB.DS.HWServer.prototype.print = function (template, params, callback) {

    if (template.getData) {
      var me = this;
      template.getData(function (data) {
        me.print(data, params, callback);
      });
    } else {
      this._print(template, params, callback);
    }
  };

  OB.DS.HWServer.prototype._print = function (templatedata, params, callback) {
    if (this.url) {
      var me = this;
      var ajaxRequest = new enyo.Ajax({
        url: me.url,
        cacheBust: false,
        method: 'POST',
        handleAs: 'json',
        contentType: 'application/xml;charset=utf-8',
        data: params ? _.template(templatedata, params) : templatedata,
        success: function (inSender, inResponse) {
          if (callback) {
            callback(inResponse);
          }
        },
        fail: function (inSender, inResponse) {
          if (callback) {
            callback({
              exception: {
                message: (OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'))
              }
            });
          } else {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'));
          }
        }
      });
      ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
    }
  };
}());