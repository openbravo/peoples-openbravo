/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, $, Backbone, _, enyo, Audio, setTimeout, setInterval, clearTimeout, clearInterval, Promise, localStorage */

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

OB.DS.HWServer = function (urllist, url, scaleurl) {
  this.urllist = urllist;
  this.mainurl = url;
  this.scaleurl = scaleurl;

  // Remove suffix if needed
  if (this.mainurl.indexOf('/printer', this.mainurl.length - 8) !== -1) { // endswith '/printer'
    this.mainurl = this.mainurl.substring(0, this.mainurl.length - 8);
  }

  // load activeurl from localStorage
  this.setActiveURL(localStorage.getItem('hw_activeurl'));
};

OB.DS.HWServer.PRINTER = 0;
OB.DS.HWServer.DISPLAY = 1;
OB.DS.HWServer.DRAWER = 2;

OB.DS.HWServer.prototype.setActiveURL = function (url) {

  // assign the active url
  this.activeurl = url;

  // validate urls
  var validurl = _.some(this.urllist, function (item) {
    return item.hasReceiptPrinter && item.hardwareURL === this.activeurl;
  }, this);
  if (!validurl) {
    this.activeurl = this.mainurl;
  }

  // save
  localStorage.setItem('hw_activeurl', this.activeurl);
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
OB.DS.HWServer.prototype.openDrawer = function (popup, timeout) {
  var template = new OB.DS.HWResource(this.openDrawerTemplate);
  this.print(template, null, function (args) {
    if (args && args.exception && args.exception.message) {
      OB.info('Error opening the drawer');
    }
  }, OB.DS.HWServer.DRAWER);
  if (OB.MobileApp.model.get('permissions').OBPOS_closeDrawerBeforeContinue) {
    this.drawerClosed = false;
    OB.POS.hwserver.isDrawerClosed(popup, timeout);
  }
};

OB.DS.HWServer.prototype.openCheckDrawer = function (popup, timeout) {
  this.checkDrawer(function () {
    this.openDrawer(popup, timeout);
  }, this);
};

OB.DS.HWServer.prototype.checkDrawer = function (callback, context) {
  if (!OB.MobileApp.model.get('permissions').OBPOS_closeDrawerBeforeContinue || this.drawerClosed) {
    if (context) {
      return callback.apply(context);
    } else {
      return callback();
    }
  } else {
    OB.UTIL.showError(OB.I18N.getLabel('OBPOS_drawerOpened'));
    return false;
  }
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
            symbol = OB.MobileApp.model.paymentnames[payment.get('kind')].symbol;
            symbolAtRight = OB.MobileApp.model.paymentnames[payment.get('kind')].currencySymbolAtTheRight;
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
        me.drawerClosed = true;
      } else {
        if (args.resultData === "Closed") {
          me.drawerClosed = true;
          errorCounter = 0;
        } else if (args.resultData === "Opened") {
          me.drawerClosed = false;
          errorCounter = 0;
          if (popup && !popupDrawerOpened.showing) {
            OB.UTIL.showLoading(false);
            popupDrawerOpened.show();
          }
        } else if (args.resultData === "Error") {
          errorCounter++;
          if (popup) {
            if (errorCounter >= 15) {
              me.drawerClosed = true;
              OB.info('Error checking the status of the drawer');
            } else {
              me.drawerClosed = false;
              if (!popupDrawerOpened.showing) {
                OB.UTIL.showLoading(false);
                popupDrawerOpened.show();
              }
            }
          } else {
            if (errorCounter >= 4) {
              me.drawerClosed = true;
              OB.info('Error checking the status of the drawer');
            } else {
              me.drawerClosed = false;
            }
          }
        } else {
          me.drawerClosed = true;
          OB.info('Error checking the status of the drawer');
        }
      }
      if (me.drawerClosed) {
        clearTimeout(beepTimeout);
        sound.pause();
        clearInterval(statusChecker);
        if (popup && popupDrawerOpened.showing) {
          popupDrawerOpened.hide();
        }
      }
    }, OB.DS.HWServer.DRAWER);
  }, 700);
};

OB.DS.HWServer.prototype.print = function (template, params, callback, device) {
  if (template) {
    if (template.getData) {
      var me = this;
      template.getData(function (data) {
        me.print(data, params, callback, device);
      });
    } else {
      this._print(template, params, callback, device);
    }
  }
};

OB.DS.HWServer.prototype._print = function (templatedata, params, callback, device) {

  var promisedata = function (template) {
      return new Promise(function (resolve, reject) {
        template.getData(function () {
          resolve();
        });
      });

      };
  var computeddata;
  try {
    computeddata = this._template(templatedata, params).trim();

    if (computeddata.substr(0, 6) === 'jrxml:') {
      // Print a jasper PDF report. We need to rebuild the parameters to adjust the _printPDF() call...
      var returnedparams = JSON.parse(computeddata.substr(6));
      var getdatas = [];
      var i = 0;
      var me = this;

      var newtemplate = new OB.DS.HWResource(returnedparams.report);
      newtemplate.ispdf = true;
      newtemplate.printer = returnedparams.printer || 1;
      newtemplate.dateFormat = OB.Format.date;
      getdatas.push(promisedata(newtemplate));
      newtemplate.subreports = [];

      for (i = 0; i < returnedparams.subreports.length; i++) {
        newtemplate.subreports[i] = new OB.DS.HWResource(returnedparams.subreports[i]);
        getdatas.push(promisedata(newtemplate.subreports[i]));
      }

      Promise.all(getdatas).then(function () {
        me._printPDF({
          param: params.order.serializeToJSON(),
          mainReport: newtemplate,
          subReports: newtemplate.subreports
        }, callback);
      });
    } else {
      // Print the computed receipt as usual
      this._send(computeddata, callback, device);
    }
  } catch (ex) {
    OB.error('Error computing the template to print.', ex);
    callback({
      data: templatedata,
      exception: {
        message: OB.I18N.getLabel('OBPOS_MsgPrintError')
      }
    });
  }
};

OB.DS.HWServer.prototype._template = function (templatedata, params) {
  return params ? _.template(templatedata, params) : templatedata;
};

OB.DS.HWServer.prototype._send = function (data, callback, device) {

  var sendurl;
  if (OB.DS.HWServer.DRAWER === device || OB.DS.HWServer.DISPLAY === device) {
    // DRAWER and DISPLAY URL is allways the main url defined in POS terminal
    sendurl = this.mainurl;
  } else {
    // PRINTER and default is the active URL
    sendurl = this.activeurl;
  }

  if (sendurl) {
    var me = this;
    var ajaxRequest = new enyo.Ajax({
      url: sendurl + '/printer',
      cacheBust: false,
      method: 'POST',
      handleAs: 'json',
      timeout: 20000,
      contentType: 'application/xml;charset=utf-8',
      data: data,
      success: function (inSender, inResponse) {
        if (callback) {
          callback(inResponse, data);
        }
      },
      fail: function (inSender, inResponse) {
        // prevent more than one entry.
        if (this.failed) {
          return;
        }
        this.failed = true;

        if (callback) {
          callback({
            data: data,
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
  } else {
    if (callback) {
      callback();
    }
  }
};

OB.DS.HWServer.prototype._printPDF = function (params, callback) {
  this._sendPDF(JSON.stringify(params), callback);
};

OB.DS.HWServer.prototype._sendPDF = function (data, callback) {
  if (this.activeurl) {
    var me = this;
    var ajaxRequest = new enyo.Ajax({
      url: me.activeurl + '/printerpdf',
      cacheBust: false,
      method: 'POST',
      handleAs: 'json',
      timeout: 20000,
      contentType: 'application/json;charset=utf-8',
      data: data,
      success: function (inSender, inResponse) {
        if (callback) {
          callback(inResponse);
        }
      },
      fail: function (inSender, inResponse) {
        // prevent more than one entry.
        if (this.failed) {
          return;
        }
        this.failed = true;

        if (callback) {
          callback({
            exception: {
              data: data,
              message: (OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'))
            }
          });
        } else {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'));
        }
      }
    });
    ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
  } else {
    if (callback) {
      callback();
    }
  }
};
