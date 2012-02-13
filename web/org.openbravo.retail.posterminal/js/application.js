// INFO: Executing service: /org.openbravo.service.retail.posterminal.jsonrest, 
//posting: {
//    "query":"from OBPOS_ProductView where $readableCriteria and priceListVersion.id = :parameter0 and id = :parameter1",
//		"parameters":{
//		  "parameter0":{"value":"8A64B71A2B0B2946012B0BD97329018B","type":"string"},
//		  "parameter1":{"value":"8A64B71A2B0B2946012B0BC4386F011A","type":"string"}
//		  }
//}
//INFO: Executing service: /org.openbravo.service.retail.posterminal.jsonrest, " +
//		posting: {
//		  "query":"from OBPOS_ProductView where $readableCriteria and priceListVersion.id = :parameter0 and pOSCategory.id = :parameter1 and isCatalog = true order by pOSLine, name",
//		  "parameters":{
//		    "parameter0":{"value":"8A64B71A2B0B2946012B0BD97329018B","type":"string"},
//		    "parameter1":{"value":"456FE871DA3C46A4B76D1EE9E905048A","type":"string"}
//		    }
//}
//var q = new OBPOS.Query("from OBPOS_ProductView where $readableCriteria and priceListVersion.id = :parameter0 and pOSCategory.id = :parameter1 and isCatalog = true order by pOSLine, name");
//q.exec({
//        "parameter0":"8A64B71A2B0B2946012B0BD97329018B",
//        "parameter1":"456FE871DA3C46A4B76D1EE9E905048A"
//});

(function (w) {

  var OBPOS = {};

  // Configuration
  var config = {

  };

  OBPOS.config = config;


  // Query object
  OBPOS.Query = function (query) {
    this.query = query;
  }

  OBPOS.Query.prototype.exec = function (params, callback) {

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
  }

  OBPOS.getParameterByName = function (name) {
    var n = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + n + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    return (results) ? decodeURIComponent(results[1].replace(/\+/g, " ")) : "";
  }

  w.OBPOS = OBPOS;

}(window));