/*global B,$,Backbone,_ */

(function () {

  OB = window.OB || {};
  OB.DS = window.OB.DS || {};

  OB.DS.MAXSIZE = 100;

  function serviceSuccess(data, textStatus, jqXHR, callback) {
    if (data._entityname) {
      callback([data]);
    } else {
      var response = data.response;
      var status = response.status;
      if (status === 0) {
        callback(response.data, response.message);
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

  function serviceError(jqXHR, textStatus, errorThrown, callback) {
    callback({
      exception: {
        message: (errorThrown ? errorThrown : OB.I18N.getLabel('OBPOS_MsgApplicationServerNotAvailable')),
        status: jqXHR.status
      }
    });
  }

  function servicePOST(source, dataparams, callback) {
    $.ajax({
      url: '../../org.openbravo.retail.posterminal.service.jsonrest/' + source,
      contentType: 'application/json;charset=utf-8',
      dataType: 'json',
      type: 'POST',
      data: JSON.stringify(dataparams),
      success: function (data, textStatus, jqXHR) {
        serviceSuccess(data, textStatus, jqXHR, callback);
      },
      error: function (jqXHR, textStatus, errorThrown) {
        serviceError(jqXHR, textStatus, errorThrown, callback);
      }
    });
  }

  function serviceGET(source, dataparams, callback) {
    $.ajax({
      url: '../../org.openbravo.retail.posterminal.service.jsonrest/' + source + '/' + encodeURI(JSON.stringify(dataparams)),
      contentType: 'application/json;charset=utf-8',
      dataType: 'json',
      type: 'GET',
      success: function (data, textStatus, jqXHR) {
        serviceSuccess(data, textStatus, jqXHR, callback);
      },
      error: function (jqXHR, textStatus, errorThrown) {
        serviceError(jqXHR, textStatus, errorThrown, callback);
      }
    });
  }

  // Process object
  OB.DS.Process = function (source) {
    this.source = source;
  };

  OB.DS.Process.prototype.exec = function (params, callback) {
    var attr;
    var data = {};

    for (attr in params) {
      if (params.hasOwnProperty(attr)) {
        data[attr] = params[attr];
      }
    }

    servicePOST(this.source, data, callback);
  };

  // Source object
  OB.DS.Query = function (source, client, org) {
    this.model = source && source.prototype && source.prototype.modelName && source; // we're using a Backbone.Model as source
    this.source = (this.model && this.model.prototype.source) || source; // we're using a plain String as source
    if (!this.source) {
      throw 'Query must have a source';
    }
    this.client = client;
    this.org = org;
  };

  OB.DS.Query.prototype.exec = function (params, callback) {
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

    if (this.terminal) {
      data.terminal = this.terminal;
    }

    serviceGET(this.source, data, callback);
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
  OB.DS.DataSource = function (query) {
    this.query = query;
    this.cache = null;
  };
  _.extend(OB.DS.DataSource.prototype, Backbone.Events);

  OB.DS.DataSource.prototype.load = function (params) {
    var me = this;
    this.cache = null;

    this.query.exec(params, function (data) {

      //var db = OB.DATA.OfflineDB;

      if (data.exception) {
        throw data.exception;
      }

      me.cache = data;

      if (me.query.model) {
        OB.Dal.initCache(me.query.model, data, function () {
          me.trigger('ready');
        }, function () {
          window.console.error(arguments);
        });
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

  // Datasource for product and prices...
  OB.DS.DataSourceProductPrice = function (pquery, ppquery) {
    this.pquery = pquery;
    this.ppquery = ppquery;
    this.pcache = null;
    this.ppcache = null;
  };
  _.extend(OB.DS.DataSourceProductPrice.prototype, Backbone.Events);

  OB.DS.DataSourceProductPrice.prototype.load = function (params) {
    this.pcache = null;
    this.ppcache = null;

    // OFFLINE GOES HERE
    var me = this;
    me.pquery.exec(params.product, function (pdata) {
      me.ppquery.exec(params.productprice, function (ppdata) {
        if (!pdata.exception && !ppdata.exception) {
          me.pcache = pdata;
          me.ppcache = ppdata;
        }
        me.trigger('ready');
      });
    });
  };

  function findPrice(item, prices, priceListVersion) {
    if (item) {
      var price = findInData(prices, {
        'priceListVersion': priceListVersion,
        'product': item.product.id
      });
      if (price) {
        item.price = price;
        return item;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  OB.DS.DataSourceProductPrice.prototype.find = function (filter, callback) {
    if (this.pcache && this.ppcache) {
      callback(findPrice(findInData(this.pcache, filter.product), this.ppcache, filter.priceListVersion));
    } else {
      this.on('ready', function () {
        callback(findPrice(findInData(this.pcache, filter.product), this.ppcache, filter.priceListVersion));
      }, this);
    }
  };

  OB.DS.DataSourceProductPrice.prototype.exec = function (filter, callback) {

    var filterfunction = function (cache) {
        return function (item) {
          return findPrice(item, cache, filter.priceListVersion);
        };
        };

    if (this.pcache && this.ppcache) {
      var result1 = execInData(this.pcache, filter.product, filterfunction(this.ppcache));
      callback(result1.data, result1.info);
    } else {
      this.on('ready', function () {
        var result2 = execInData(this.pcache, filter.product, filterfunction(this.ppcache));
        callback(result2.data, result2.info);
      }, this);
    }
  };

  // HWServer
  OB.DS.HWServer = function (url, scaleurl) {
    this.url = url;
    this.scaleurl = scaleurl;
  };

  OB.DS.HWServer.prototype.getWeight = function (callback) {
    if (this.scaleurl) {
      var me = this;
      $.ajax({
        url: me.scaleurl,
        dataType: 'json',
        type: 'GET',
        success: function (data, textStatus, jqXHR) {
          callback(data);
        },
        error: function (jqXHR, textStatus, errorThrown) {
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
    } else {
      callback({
        result: 1
      });
    }
  };

  OB.DS.HWServer.prototype.print = function (templatedata, params, callback) {
    if (this.url) {
      var me = this;
      $.ajax({
        url: me.url,
        contentType: 'application/xml;charset=utf-8',
        dataType: 'json',
        type: 'POST',
        data: params ? _.template(templatedata, params) : templatedata,
        success: function (data, textStatus, jqXHR) {
          if (callback) {
            callback(data);
          }
        },
        error: function (jqXHR, textStatus, errorThrown) {
          if (callback) {
            callback({
              exception: {
                message: (OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'))
              }
            });
          }
        }
      });
    }
  };
}());