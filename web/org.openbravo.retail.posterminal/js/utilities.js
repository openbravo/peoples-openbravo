/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, $, _, enyo */

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

  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1).toUpperCase();
  }

  OB.UTIL.get_UUID = function () {
    return (S4() + S4() + S4() + S4() + S4() + S4() + S4() + S4());
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
    var ajaxRequest = new enyo.Ajax({
      url: res,
      cacheBust: false,
      method: 'GET',
      handleAs: 'text',
      contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
      success: function (inSender, inResponse) {
        callback.call(context || this, inResponse);
      },
      fail: function (inSender, inResponse) {
        callback.call(context || this);
      }
    });
    ajaxRequest.go().response('success').error('fail');
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

  OB.UTIL.processPaidOrders = function (model) {
    // Processes the paid, unprocessed orders
    var me = this,
        criteria = {
        hasbeenpaid: 'Y'
        };
    if (OB.POS.modelterminal.get('connectedToERP')) {
      OB.Dal.find(OB.Model.Order, criteria, function (ordersPaidNotProcessed) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!ordersPaidNotProcessed || ordersPaidNotProcessed.length === 0) {
          return;
        }
        ordersPaidNotProcessed.each(function (order) {
          order.set('isbeingretriggered', 'Y');
        });
        successCallback = function () {
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessProcessOrder'));
        };
        errorCallback = function () {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorProcessOrder'));
        };
        OB.UTIL.showAlert.display(OB.I18N.getLabel('OBPOS_ProcessPendingOrders'), OB.I18N.getLabel('OBPOS_Info'));
        OB.UTIL.processOrders(model, ordersPaidNotProcessed, successCallback, errorCallback);
      });
    }
  };

  OB.UTIL.processOrderClass = 'org.openbravo.retail.posterminal.OrderLoader';

  OB.UTIL.processOrders = function (model, orders, successCallback, errorCallback) {
    var ordersToJson = [];
    orders.each(function (order) {
      ordersToJson.push(order.serializeToJSON());
    });
    this.proc = new OB.DS.Process(OB.UTIL.processOrderClass);
    if (OB.POS.modelterminal.get('connectedToERP')) {
      this.proc.exec({
        order: ordersToJson
      }, function (data, message) {
        if (data && data.exception) {
          // Orders have not been processed
          orders.each(function (order) {
            order.set('isbeingprocessed', 'N');
            OB.Dal.save(order, null, function (tx, err) {
              OB.UTIL.showError(err);
            });
          });
          if (errorCallback) {
            errorCallback();
          }
        } else {
          // Orders have been processed, delete them
          orders.each(function (order) {
            if (model) {
              model.get('orderList').remove(order);
            }
            OB.Dal.remove(order, null, function (tx, err) {
              OB.UTIL.showError(err);
            });
          });
          if (successCallback) {
            successCallback();
          }
        }
      });
    }
  };

  OB.UTIL.processCustomerClass = 'org.openbravo.retail.posterminal.CustomerLoader';

  OB.UTIL.processCustomers = function (changedCustomers, successCallback, errorCallback) {
    var customersToJson = [];
    changedCustomers.each(function (customer) {
      customersToJson.push(customer.get('json'));
    });
    this.proc = new OB.DS.Process(OB.UTIL.processCustomerClass);
    if (OB.POS.modelterminal.get('connectedToERP')) {
      this.proc.exec({
        customer: customersToJson
      }, function (data, message) {
        if (data && data.exception) {
          // The server response is an Error! -> Orders have not been processed
          changedCustomers.each(function (changedCustomer) {
            changedCustomer.set('isbeingprocessed', 'N');
            changedCustomer.set('json', JSON.stringify(changedCustomer.get('json')));
            OB.Dal.save(changedCustomer, null, function (tx, err) {
              OB.UTIL.showError(err);
            });
          });
          if (errorCallback) {
            errorCallback();
          }
        } else {
          // Customers have been processed, delete them from the queue
          OB.Dal.removeAll(OB.Model.ChangedBusinessPartners, null, function () {
            successCallback();
          }, function (tx, err) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorRemovingLocallyProcessedCustomer'));
          });
        }
      });
    }
  };

  OB.UTIL.checkConnectivityStatus = function () {
    var ajaxParams, currentlyConnected = OB.POS.modelterminal.get('connectedToERP');
    if (navigator.onLine) {
      // It can be a false positive, make sure with the ping
      var ajaxRequest = new enyo.Ajax({
        url: '../../security/SessionActive?id=0',
        cacheBust: true,
        timeout: 5000,
        method: 'GET',
        handleAs: 'json',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        success: function (inSender, inResponse) {
          if (currentlyConnected !== true) {
            if (OB.POS.modelterminal) {
              OB.POS.modelterminal.triggerOnLine();
            }
          }
        },
        fail: function (inSender, inResponse) {
          if (currentlyConnected !== false) {
            if (OB.POS.modelterminal) {
              OB.POS.modelterminal.triggerOffLine();
            }
          }
        }
      });
      ajaxRequest.go().response('success').error('fail');
    } else {
      if (currentlyConnected) {
        if (OB.POS.modelterminal) {
          OB.POS.modelterminal.triggerOffLine();
        }
      }
    }
  };

  OB.UTIL.updateDocumentSequenceInDB = function (documentNo) {
    var docSeqModel, criteria = {
      'posSearchKey': OB.POS.modelterminal.get('terminal').searchKey
    };
    OB.Dal.find(OB.Model.DocumentSequence, criteria, function (documentSequenceList) {
      var posDocumentNoPrefix = OB.POS.modelterminal.get('terminal').docNoPrefix,
          orderDocumentSequence = parseInt(documentNo.substr(posDocumentNoPrefix.length + 1), 10),
          docSeqModel;
      if (documentSequenceList && documentSequenceList.length !== 0) {
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

  OB.UTIL.isWritableOrganization = function (orgId) {
    if (OB.POS.modelterminal.get('writableOrganizations')) {
      var result = false;
      result = _.find(OB.POS.modelterminal.get('writableOrganizations'), function (curOrg) {
        if (orgId === curOrg) {
          return true;
        }
      });
      if (result === true) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  };

}());