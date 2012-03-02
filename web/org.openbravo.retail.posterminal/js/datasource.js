

define([], function () {

  OB = window.OB || {};
  OB.DS = window.OB.DS || {};

  // Query object
  OB.DS.Query = function (query) {
    this.query = query;
  }

  OB.DS.Query.prototype.exec = function (params, callback) {

    var data = {
      query: this.query
    }

    // build parameters
    if (params) {
      var p = {};
      for (var i in params) {
        if (typeof params[i] === 'string') {
          p[i] = {
            value: params[i],
            type: 'string'
          }
        } else if (typeof params[i] === 'number') {
          if (params[i] === Math.round(params[i])) {
            p[i] = {
              value: params[i],
              type: 'long'
            }
          } else {
            p[i] = {
              value: params[i],
              type: 'bigdecimal'
            }
          }
        } else if (typeof params[i] === 'boolean') {
          p[i] = {
            value: params[i],
            type: 'boolean'
          }
        } else {
          p[i] = params[i];
        }
      }
      data.parameters = p;
    }

    $.ajax({
      url: '../../org.openbravo.service.retail.posterminal.jsonrest/hql/?auth=false',
      contentType: 'application/json;charset=utf-8',
      dataType: 'json',
      type: 'POST',
      data: JSON.stringify(data),
      success: function (data, textStatus, jqXHR) {
        if (data._entityname) {
          callback([data]);
        } else {
          var response = data.response;
          var status = response.status;
          if (status === 0) {
            callback(response.data);
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
      },
      error: function (jqXHR, textStatus, errorThrown) {
        callback({
          exception: {
            message: (errorThrown ? errorThrown : "Application server is not available.")
          }
        });
      }
    });
  };
  
  // DataSource object
  // OFFLINE GOES HERE
  OB.DS.DataSource = function (query) {
    this.query = query;
    this.params = null;
    this.cache = null; 
  };
  
  OB.DS.DataSource.prototype.load = function (params) {
    this.params = params;
    this.cache = null;
  }
  
  OB.DS.DataSource.prototype.find = function (filter, callback) {
    // OFFLINE GOES HERE
    
    if (this.cache) { // Check cache validity
      // Data cached...
      findInData(this.cache, filter, callback);
    } else {  
      // Execute query...
      var me = this;      
      this.query.exec(this.params, function(data) {
        if (data.exception) {
          callback(data);
        } else {
          me.cache = data;
          findInData(me.cache, filter, callback);
        }
      });  
    }
  }

  OB.DS.DataSource.prototype.exec = function (filter, callback) {
    // OFFLINE GOES HERE
    
    if (this.cache) { // Check cache validity
      // Data cached...
      execInData(this.cache, filter, callback);
    } else {  
      // Execute Query
      var me = this;
      this.query.exec(this.params, function(data) {
        if (data.exception) {
          callback(data);
        } else {
          me.cache = data;
          execInData(me.cache, filter, callback);
        }
      });  
    }
  };
  
  function findInData(data, filter, callback) {
    if ($.isEmptyObject(filter)) {
      callback({
        exception: 'filter not defined'
      });
    } else {
      for (var i = 0, max = data.length; i < max; i++) {
        if (check(data[i], filter)) {
          callback(data[i]);
          return;
        }
      }        
      callback(null);
    }    
  }
  
  function execInData(data, filter, callback) {  
    if ($.isEmptyObject(filter)) {
      callback(data);
    } else {
      var newdata = [];
      for (var i = 0, max = data.length; i < max; i++) {
        if (check(data[i], filter)) {
          newdata.push(data[i]);
        }
      }        
      callback(newdata);
    }    
  }

  function check(elem, filter) {
    for (var p in filter) {
      if (typeof(filter[p]) === 'object') {
        return check(elem[p], filter[p]);
      } else {
        if (filter[p] !== elem[p]) {
          return false;
        }
      }
    }
    return true;
  }

  OB.DS.HWServer = function (url) {
    this.url = url;
  };
  
  OB.DS.HWServer.prototype.print = function (template, params, callback) {
    var me = this;
    $.ajax({
      url: template,
      dataType: 'text',
      type: 'GET',
      success: function (templatedata, textStatus, jqXHR) {      
        
        $.ajax({
          url: me.url,
          contentType: 'application/json;charset=utf-8',
          dataType: 'jsonp',
          type: 'GET',
          data: {content: params ? _.template(templatedata, params) : templatedata},
          success: function (data, textStatus, jqXHR) {
            if (callback) {
              callback(data);
            }
          },
          error: function (jqXHR, textStatus, errorThrown) {
            if (callback) {
              callback({
                exception: {
                  message: (errorThrown ? errorThrown : "Hardware server is not available.")
                }
              });
            }
          }
        });
              
      },
      error: function (jqXHR, textStatus, errorThrown) {
        callback({
          exception: {
            message: (errorThrown ? errorThrown : "Template not available.")
          }
        });        
      }
    });  
  };
  

});