/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, $, _ */

(function () {

  OB = window.OB || {};
  OB.UTIL = window.OB.UTIL || {};

  OB.UTIL.getParameterByName = function (name) {
    var n = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    var regexS = '[\\?&]' + n + '=([^&#]*)';
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    return (results) ? decodeURIComponent(results[1].replace(/\+/g, ' ')) : '';
  };

  OB.UTIL.escapeRegExp = function (text) {
    return text.replace(/[\-\[\]{}()+?.,\\\^$|#\s]/g, '\\$&');
  };

  OB.UTIL.padNumber = function (n, p) {
    var s = n.toString();
    while (s.length < p) {
      s = '0' + s;
    }
    return s;
  };

  OB.UTIL.encodeXMLComponent = function (s, title, type) {
    return s.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace('\'', '&apos;').replace('\"', '&quot;');
  };

  OB.UTIL.loadResource = function (res, callback, context) {
    $.ajax({
      url: res,
      dataType: 'text',
      type: 'GET',
      success: function (data, textStatus, jqXHR) {
        callback.call(context || this, data);
      },
      error: function (jqXHR, textStatus, errorThrown) {
        callback.call(context || this);
      }
    });
  };

  OB.UTIL.queueStatus = function (queue) {
    // Expects an object where the value element is true/false depending if is processed or not
    if (!_.isObject(queue)) {
      throw 'Object expected';
    }
    return _.reduce(queue, function (memo, val) {
      return memo && val;
    }, true);
  };


  function _initContentView(view, child) {
    var obj, inst, i, max;
    if (typeof (child) === 'string') {
      inst = $(document.createTextNode(child));
    } else if (child.tag) {
      inst = $('<' + child.tag + '/>');
      if (child.attributes) {
        inst.attr(child.attributes);
      }
      if (child.content) {
        if (typeof(child.content) === 'string') {
          inst.html(child.content);
        } else {
          for (i = 0, max = child.content.length; i < max; i++) {
            inst.append(_initContentView(view, child.content[i]));
          }
        }
      }
      if (child.id) {
        view[child.id] = inst;
      }
    } else if (child.view) {
      obj = new child.view({root: view.options.root, parent: view});
      inst = obj.render().$el;
      if (child.id) {
        view[child.id] = obj;
      }
    } else if (child.$el) {
      inst = child.$el;
    }
    return inst;
  }

  OB.UTIL.initContentView = function (view) {
    var i, max;
    if (view.contentView) {
      for (i = 0, max = view.contentView.length; i < max; i++) {
        view.$el.append(_initContentView(view, view.contentView[i]));
      }
    }
  };

  OB.UTIL.processOrders = function (context, orders, successCallback, errorCallback) {
    var ordersToJson = [];
    orders.each(function (order){
      ordersToJson.push(order.serializeToJSON());
    });
    this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessOrder');
    if (OB.POS.modelterminal.get('connectedToERP')) {
      this.proc.exec({
        order: ordersToJson
      }, function (data, message) {
        if (data && data.exception) {
          // Orders have not been processed
          orders.each(function (order){
            order.set('isbeingprocessed', 'N');
            OB.Dal.save(order, null, function (tx, err) { OB.UTIL.showError(err);});
          });
          if (errorCallback) {
            errorCallback();
          }
        } else {
          // Orders have been processed, delete them
          orders.each(function (order){
            context.modelorderlist.remove(order);
            OB.Dal.remove(order,null, function (tx, err) { OB.UTIL.showError(err);});
          });
          if (successCallback) {
            successCallback();
          }
        }
      });
    }
  };

  OB.UTIL.checkConnectivityStatus = function() {
    var ajaxParams,
    currentlyConnected = OB.POS.modelterminal.get('connectedToERP');
    if (navigator.onLine) {
      // It can be a false positive, make sure with the ping
      ajaxParams = {
          async: true,
          cache: false,
          context: $("#status"),
          dataType: "json",
          error: function (req, status, ex) {
            if (currentlyConnected !== false) {
              if (OB.POS.modelterminal) {
                OB.POS.modelterminal.triggerOffLine();
              }
            }
          },
          success: function (data, status, req) {
            if (currentlyConnected !== true) {
              if (OB.POS.modelterminal) {
                OB.POS.modelterminal.triggerOnLine();
              }
            }
          },
          timeout: 5000,
          type: "GET",
          url: "../../security/SessionActive?id=0"
      };
      $.ajax(ajaxParams);
    } else {
      if (currentlyConnected) {
        if (OB.POS.modelterminal) {
          OB.POS.modelterminal.triggerOffLine();
        }
      }
    }
  };

  OB.UTIL.setConnectivityLabel = function(status) {
    var label = OB.I18N.getLabel('OBPOS_' + status);
    if (label.indexOf('OBPOS_' + status) === -1) { // If the *good* label is ready (can be retrieved), set the label
      $($('#online > span')[0]).css('background-image', 'url("./img/icon' + status + '.png")');
      $($('#online > span')[1]).text(label);
      $($('#online')[0]).css('visibility', 'visible');
    } else { // else, retry after 300ms
      setTimeout(function() { OB.UTIL.setConnectivityLabel(status); }, 300);
    }
  };

  OB.UTIL.updateDocumentSequenceInDB = function(documentNo) {
    var docSeqModel, criteria = {
      'posSearchKey': OB.POS.modelterminal.get('terminal').searchKey
    };
    OB.Dal.find(OB.Model.DocumentSequence, criteria, function(documentSequenceList) {
      var posDocumentNoPrefix = OB.POS.modelterminal.get('terminal').docNoPrefix,
      orderDocumentSequence = parseInt(documentNo.substr(posDocumentNoPrefix.length + 1), 10), docSeqModel;
      if (documentSequenceList) {
        docSeqModel = documentSequenceList.at(0);
        if (orderDocumentSequence > docSeqModel.get('documentSequence')) {
          docSeqModel.set('documentSequence', orderDocumentSequence);
        }
      } else {
        docSeqModel = new OB.Model.DocumentSequence();
        docSeqModel.set('posSearchKey', OB.POS.modelterminal.get('terminal').searchKey);
        docSeqModel.set('documentSequence', orderDocumentSequence);
      }
      OB.Dal.save(docSeqModel, null, null);
    });
  };

}());