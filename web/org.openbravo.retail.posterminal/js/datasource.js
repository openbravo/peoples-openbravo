/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, enyo, Audio, setTimeout, setInterval, clearTimeout, clearInterval, Promise */

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
    }, this, {
      hash: OB.UTIL.localStorage.getItem('templateVersion')
    });
  }
};

OB.DS.HWServer = function (urllist, url, scaleurl) {
  this.urllist = urllist;
  this.mainurl = url;
  this.url = this.mainurl; // keep backward compatibility
  this.scaleurl = scaleurl;

  // Remove suffix if needed
  if (this.mainurl && this.mainurl.indexOf('/printer', this.mainurl.length - 8) !== -1) { // endswith '/printer'
    this.mainurl = this.mainurl.substring(0, this.mainurl.length - 8);
  }
  // load activeurl from OB.UTIL.localStorage
  this.setActiveURL(OB.UTIL.localStorage.getItem('hw_activeurl'));
  //load activepdfurl from OB.UTIL.localStorage
  this.setActivePDFURL(OB.UTIL.localStorage.getItem('hw_activepdfurl'));

  // WebPrinter
  var printertypeinfo = OB.PRINTERTYPES[OB.MobileApp.model.get('terminal').printertype];
  this.webprinter = printertypeinfo ? new OB.WEBPrinter(printertypeinfo) : null;
  if (this.webprinter) {
    // For Hardware Manager compatilibity register standard image
    // Posible URLs
    // https://livebuilds.openbravo.com/retail_pi_pgsql/org.openbravo.retail.posterminal.service.loginutils?terminalName=VBS-1&command=companyLogo&appModuleId=FF808181326CC34901326D53DBCF0018&appName=WebPOS&0.38810264271123907
    // {"logoUrl":"data:image\/png;base64,iVBORw0KGgoAAAANSUhEUgAAAJQAAAA3CAYAAADqtGOUAAAGOklEQVR42u1dy3HbMBBNBykhJaQDeZzJPSX4louTcQkqISWoBM7YzikHlIASXAJKUAQSkIDlYvElSNq7MzhEJigReNh9u3hgPn1iY2NjY2NjY2PblP389\/3z7+f7u18vh+Pj693p18udmLfD8dKe9HU8YmwoiDRALmCRl3bObOrShsfXwwOP5Ae3x7\/fvhgvdG7RLvd6095LA5RH98OFtcOfVkDCvBZ7rA9imvdMnmQxMLltKPFW+jde28v91+g1AS6n+8JrLEe0jfp9Xn\/nd2Cfa28Pf1NSq+i\/OpgMTzp3bioEivDv9Lkczvn87wncx71GWiC6n1MTY5IQe62gPp8SlaLxKe6\/Ll9qyJWWBhUcXNgXggIDhu7jX3M4MqDeB5iyQQUnHfIxfPAPT\/4zHx4w4CwFKPN9SIkFJi3+3zWXDTyTiLWVwOQP7F5AZcoRZtKnQXf+NiAZ5skHpZd0qBBYWwGKuMfMS6Z45W0S8Jnb30STKUQdgEZgHEuveCfBkAQYBgZUg9JAx2wuq0GPk+JZsUkyoXygroEhkwFVntEdtwimlIm0aTRGzH1A6Kz1lrleeRLwzPpeBKGXBE9RawEqVjLoWjyGk7HRJuJe6uZhrZeBAIIAQ7ybjGWIub+3B6BqF+R7zOqqB8Ul1jZMus+GhcBQPwZU1Uav56q33AZyYTzf\/5jXa65FT4EUQiWccH0PGlCTagJroMDaG1BkySC3WLy3anj5hrLDb2IT4v7b9TyuR8L6UIBiUl64f2c1TiYr2pAH8wuSdPngNuhu5uZzJm9BDQyoRuCJZQRTJlSsgeoW9lyAeCTdzdz8rE5SYGVAZYIpRix1eNC8wgWZnpAVibwqKM4qqrIe2gNkQC3Pn0alpQWXUW4ee4fEGI9CCrRDZPJHb5ay6LYKqJS9vMU1ZxXFTKU9lJ3Y1krO6iLn7LfMJwY+O9zb2yGgUvjncWFA+au0UMZ7sh6rlxgPpvaR8gEKgplCIXBPBlRnQN0OHkyEtoNcODowKSrLVCXmUorNlCSJCu0lis0YVdgSoK4Z0zUMTl5CrQEottVIeXNAeQcOTDb41rsWxbaSLUmkbWXabO3IJUk5dP814RAeVIiFiZxw1rxfxqGG1AMLcHyytmyWlqxYwt4aVDE9OEXaQ8pMouQgM7y8KIwOYsGoIrB+IVDNMZFBL2A2tKTikgKV1U5Pkz0eWwdtHAxJFfFcIFCCPPc+sFQQGg\/KS+0FUIhESQTu7\/JelaWj6qiDkkDZIErfdRA+d3fzPKEiJXxe6MnCFCDM2VYGlIqqDJzFBZ9vRh1m5wkKkp9+sl98Uswm9JOzEY0Oiv47lbZTqsvAgCVtx8TC3sqAyjrJEvNSvkDx7q1I5bl0hdutqIMHk62Lmv7imAPYVSIkhLshJeztCVDYfNvnih1Fa7lBXF1Bt29rGU+dGEBgR5uqN4Z9wj1QHigS7tT8tDHuYfcGKCiotAvLExkGKMNaYU8ge3wzoleiFI2dfoFhz1NI+AtHIYN8RgZ5iIW9lQElS95ngB1waOKdwmSsfvslokIQJVlmytaBxwMcLwRI+4l6fttv9jny\/VvP8lK8VIrqoreX8rZcUu7lhD7RwjvFgOOVC0C4A55IhTwXrlrYH6CoGmQziUspl7KTZg6KnjL6jVlE4mnl5HqIez+72kB2Q4Y7yL0AkX\/bW9kgx4k0804BUptcCjCTKEs3eWNAjMlVyCLnuJ94C12xcKfHwD\/JQr\/ZZW+knAz1rQV4OVsk7gZwhapA3V76WhfqwgtjVKUORLgTNYnBngGVo\/UqR+0UHlQKmAzCVV15wZ7wRSe2aMBAGBWhcFeyUwDDAgMqfUIUGaba7QPKQKYpa87jY4kBDHeIpj7ERWQo7DGgKkB1PbpdF+bO2GkTQI5F7csdMD6IhDuZQkhnL+Jwwl723poBY69+1KnhroBCONXoMVJCYql2SnOdEs6UUuSMhbvY94bAl8\/Bpknr1Y8CSndAuTULZ4XIJdUITfmgv9lJhruYkCx0PQNq\/Yo6VmE\/LhW2Q8pL7BXSMW+NKTlzFZTXE0Kd+rU6gLEot2qhUOD\/RYFthm7tsUZ5Svq2jbSFQx5BtqQ0lBLKs7GxsbGxsbGxsX1U+w9z2RUjLLYRUQAAAABJRU5ErkJggg=="}
    // https://livebuilds.openbravo.com/retail_pi_pgsql/utility/ShowImageLogo?logo=yourcompanymenu
    // yourcompanylogin, youritservicelogin, yourcompanymenu, yourcompanybig, yourcompanydoc, yourcompanylegal
    this.webprinter.registerImage('ticket-image.png', '../../utility/ShowImageLogo?logo=yourcompanybig');
  }
};

OB.DS.HWServer.PRINTER = 0;
OB.DS.HWServer.DISPLAY = 1;
OB.DS.HWServer.DRAWER = 2;

OB.DS.HWServer.prototype.showSelected = function () {
  OB.UTIL.showAlert.display(OB.I18N.getLabel('OBPOS_SelectedReceiptPrinter', [this.activeidentifier]), OB.I18N.getLabel('OBMOBC_LblSuccess'), 'alert-success', false, 'OBPOS_SelectedReceiptPrinter');
  OB.UTIL.showAlert.display(OB.I18N.getLabel('OBPOS_SelectedPDFPrinter', [this.activepdfidentifier]), OB.I18N.getLabel('OBMOBC_LblSuccess'), 'alert-success', false, 'OBPOS_SelectedPDFPrinter');
};


OB.DS.HWServer.prototype.setActiveURL = function (url) {

  // validate urls
  if (this.urllist) { // Check only in the case urllist is a valid to prevent wrong initializations
    var validprinter = _.find(this.urllist, function (item) {
      return item.hasReceiptPrinter && item.hardwareURL === url;
    }, this);
    if (validprinter) {
      this.activeurl = validprinter.hardwareURL;
      this.activeidentifier = validprinter._identifier;
    } else {
      this.activeurl = this.mainurl;
      this.activeidentifier = OB.I18N.getLabel('OBPOS_MainPrinter');
    }
  } else {
    this.activeurl = this.mainurl;
    this.activeidentifier = OB.I18N.getLabel('OBPOS_MainPrinter');
  }

  // save
  if (this.activeurl) {
    OB.UTIL.localStorage.setItem('hw_activeurl', this.activeurl);
    OB.UTIL.localStorage.setItem('hw_activeidentifier', this.activeidentifier);
  } else {
    OB.UTIL.localStorage.removeItem('hw_activeurl');
    OB.UTIL.localStorage.removeItem('hw_activeidentifier');
  }
};

OB.DS.HWServer.prototype.setActivePDFURL = function (url) {

  // validate urls
  if (this.urllist) { // Check only in the case urllist is a valid to prevent wrong initializations
    var validprinter = _.find(this.urllist, function (item) {
      return item.hasPDFPrinter && item.hardwareURL === url;
    }, this);
    if (validprinter) {
      this.activepdfurl = validprinter.hardwareURL;
      this.activepdfidentifier = validprinter._identifier;
    } else {
      this.activepdfurl = this.mainurl;
      this.activepdfidentifier = OB.I18N.getLabel('OBPOS_MainPrinter');
    }
  } else {
    this.activepdfurl = this.mainurl;
    this.activepdfidentifier = OB.I18N.getLabel('OBPOS_MainPrinter');
  }

  // save
  if (this.activepdfurl) {
    OB.UTIL.localStorage.setItem('hw_activepdfurl', this.activepdfurl);
    OB.UTIL.localStorage.setItem('hw_activepdfidentifier', this.activepdfidentifier);
  } else {
    OB.UTIL.localStorage.removeItem('hw_activepdfurl');
    OB.UTIL.localStorage.removeItem('hw_activepdfidentifier');
  }
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

OB.DS.HWServer.prototype.cleanDisplay = function () {

  if (!this.cleanDisplayTemplate) {
    this.cleanDisplayTemplate = new OB.DS.HWResource('res/cleandisplay.xml');
  }

  this.print(this.cleanDisplayTemplate, null, function (args) {
    if (args && args.exception && args.exception.message) {
      OB.info('Error cleaning display.');
    }
  }, OB.DS.HWServer.DISPLAY);
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
        if (OB.DEC.compare(popup.receipt.get('change')) <= 0) {
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

OB.DS.HWServer.prototype.status = function (callback) {
  this._status(callback);
};

OB.DS.HWServer.prototype._status = function (callback) {
  if (this.activeurl) {
    var me = this;
    var ajaxRequest = new enyo.Ajax({
      url: me.activeurl + '/status.json',
      cacheBust: false,
      method: 'GET',
      handleAs: 'json',
      timeout: 5000,
      contentType: 'application/json;charset=utf-8',
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
              message: (OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'))
            }
          });
        } else {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'));
        }
      }
    });
    ajaxRequest.go().response('success').error('fail');
  } else {
    if (callback) {
      callback();
    }
  }
};

OB.DS.HWServer.prototype._template = function (templatedata, params) {
  return params ? _.template(templatedata, params) : templatedata;
};


OB.DS.HWServer.prototype.confirm = function (title, message) {
  return new Promise(function (resolve, reject) {
    OB.UTIL.showConfirmation.display(title, message, [{
      label: OB.I18N.getLabel('OBMOBC_LblOk'),
      action: resolve
    }], {
      onHideFunction: reject
    });
  });
};

OB.DS.HWServer.prototype._sendWebPrinter = function () {
  return function (data) {
    if (this.webprinter.connected()) {
      return this.webprinter.print(data);
    } else {
      return this.confirm(OB.I18N.getLabel('OBPOS_WebPrinter'), OB.I18N.getLabel('OBPOS_WebPrinterPair')).then(function () {
        return this.webprinter.request();
      }.bind(this)).then(function () {
        return this.webprinter.print(data);
      }.bind(this));
    }
  }.bind(this);
};

OB.DS.HWServer.prototype._sendHWMPrinter = function (sendurl) {
  return function (data) {
    return new Promise(function (resolve, reject) {
      if (sendurl) {
        var ajaxRequest = new enyo.Ajax({
          url: sendurl + '/printer',
          cacheBust: false,
          method: 'POST',
          handleAs: 'json',
          timeout: 20000,
          contentType: 'application/xml;charset=utf-8',
          data: data,
          success: resolve,
          fail: function (inSender, inResponse) {
            // prevent more than one entry.
            if (this.failed) {
              return;
            }
            this.failed = true;
            reject();
          }
        });
        ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
      } else {
        resolve();
      }
    });
  }.bind(this);
};

OB.DS.HWServer.prototype._send = function (data, callback, device) {
  var sendfunction;
  if (OB.DS.HWServer.DISPLAY === device) {
    // DISPLAY requests always go to HARDWARE MANAGER MAIN URL
    sendfunction = this._sendHWMPrinter(this.mainurl);
  } else if (OB.DS.HWServer.DRAWER === device) {
    // DRAWER requests go to WebPrinter if exists, otherwise to HARDWARE MANAGER MAIN URL
    if (this.webprinter) {
      sendfunction = this._sendWebPrinter();
    } else {
      sendfunction = this._sendHWMPrinter(this.mainurl);
    }
  } else {
    // PRINTER requests go to WebPrinter if exists and MAIN, otherwise to HARDWARE MANAGER ACTIVE URL
    if (this.webprinter && this.activeurl === this.mainurl) {
      sendfunction = this._sendWebPrinter();
    } else {
      sendfunction = this._sendHWMPrinter(this.activeurl);
    }
  }
  sendfunction(data).then(function () {
    if (callback) {
      callback();
    }
  })['catch'](function (error) {
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
  });
};

OB.DS.HWServer.prototype._printPDF = function (params, callback) {
  this._sendPDF(JSON.stringify(params), callback);
};

OB.DS.HWServer.prototype._sendPDF = function (data, callback) {
  if (this.activepdfurl) {
    var me = this;
    var ajaxRequest = new enyo.Ajax({
      url: me.activepdfurl + '/printerpdf',
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