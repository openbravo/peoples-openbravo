/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, Backbone, _, enyo, Audio */

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

OB.DS.HWServer.prototype.openDrawerTemplate = 'res/opendrawer.xml';
OB.DS.HWServer.prototype.openDrawer = function () {
  var template = new OB.DS.HWResource(this.openDrawerTemplate);
  this.print(template, null, function (args) {
    if (args && args.exception && args.exception.message) {
      OB.info('Error opening the drawer');
    }
  });
};

OB.DS.HWServer.prototype.isDrawerClosedTemplate = 'res/checkdrawerstatus.xml';
OB.DS.HWServer.prototype.isDrawerClosed = function (popup, timeout) {
  var statusChecker, beepTimeout, sound = new Audio('sounds/drawerAlert.mp3'),
      template = new OB.DS.HWResource(this.isDrawerClosedTemplate),
      me = this,
      errorCounter = 0,
      symbol, symbolAtRight, popupDrawerOpened = OB.MobileApp.view.$.confirmationContainer.$.popupDrawerOpened;

  if (timeout && !isNaN(parseInt(timeout, 10))) {

    beepTimeout = setTimeout(function () {
      sound.loop = true;
      sound.play();
    }, parseInt(timeout, 10));
  }

  if (popup) {

    if (!popupDrawerOpened) {
      popupDrawerOpened = OB.MobileApp.view.$.confirmationContainer.createComponent({
        kind: 'OB.UI.PopupDrawerOpened'
      });
    }

    if (popup.receipt) {
      var paymentStatus = popup.receipt.getPaymentStatus();
      popupDrawerOpened.$.table.setStyle('position:relative; top: 0px;');
      if (paymentStatus.isReturn || paymentStatus.isNegative) {
        popupDrawerOpened.$.bodyContent.$.label.setContent(OB.I18N.getLabel('OBPOS_ToReturn') + ':');
        _.each(popup.receipt.get('payments').models, function (payment) {
          if (payment.get('isCash')) {
            symbol = OB.POS.terminal.terminal.paymentnames[payment.get('kind')].symbol;
            symbolAtRight = OB.POS.terminal.terminal.paymentnames[payment.get('kind')].currencySymbolAtTheRight;
            popupDrawerOpened.$.bodyContent.$.label.setContent(popupDrawerOpened.$.bodyContent.$.label.getContent() + ' ' + OB.I18N.formatCurrencyWithSymbol(payment.get('paid'), symbol, symbolAtRight));
          }
        });
      } else {
        if (!paymentStatus.change) {
          popupDrawerOpened.$.bodyContent.$.label.setContent(OB.I18N.getLabel('OBPOS_PaymentsExact'));
        } else {
          popupDrawerOpened.$.bodyContent.$.label.setContent(OB.I18N.getLabel('OBPOS_ticketChange') + ': ' + OB.MobileApp.model.get('changeReceipt'));
        }
      }
    } else {
      popupDrawerOpened.$.bodyContent.$.label.setContent('');
      popupDrawerOpened.$.table.setStyle('position:relative; top: 35px;');
    }

    if (popup.openFirst) {
      OB.UTIL.showLoading(false);
      popupDrawerOpened.show();
    }
  }

  statusChecker = setInterval(function () {
    me.print(template, null, function (args) {
      if (args && args.exception && args.exception.message) {
        OB.info('Error checking the status of the drawer');
        OB.MobileApp.model.set("isDrawerClosed", true);
      } else {
        if (args.resultData[0] === 1) {
          OB.MobileApp.model.set("isDrawerClosed", true);
          errorCounter = 0;
        } else if (args.resultData[0] === 0) {
          OB.MobileApp.model.set("isDrawerClosed", false);
          errorCounter = 0;
          if (popup && !popupDrawerOpened.showing) {
            OB.UTIL.showLoading(false);
            popupDrawerOpened.show();
          }
        } else if (args.resultData[0] === -1 && popup) {
          errorCounter++;
          if (errorCounter >= 20) {
            OB.MobileApp.model.set("isDrawerClosed", true);
            OB.info('Error checking the status of the drawer');
          } else {
            OB.MobileApp.model.set("isDrawerClosed", false);
            if (!popupDrawerOpened.showing) {
              OB.UTIL.showLoading(false);
              popupDrawerOpened.show();
            }
          }
        } else {
          OB.MobileApp.model.set("isDrawerClosed", true);
          OB.info('Error checking the status of the drawer');
        }
      }
      if (OB.MobileApp.model.get("isDrawerClosed")) {
        clearTimeout(beepTimeout);
        sound.pause();
        clearInterval(statusChecker);
        if (popup && popupDrawerOpened.showing) {
          popupDrawerOpened.hide();
        }
      }
    });
  }, 500);
};

OB.DS.HWServer.prototype.print = function (template, params, callback) {
  if (template && template.getData) {
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
      timeout: 6000,
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
