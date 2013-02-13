/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, Backbone, _, enyo */

// HWServer: TODO: this should be implemented in HW Manager module
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
