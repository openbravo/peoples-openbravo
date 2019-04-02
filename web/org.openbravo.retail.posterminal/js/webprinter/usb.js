/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise, console */

(function () {

  var USB = function (info) {
      this.info = info;
      this.device = null;
      this.onDisconnected = this.onDisconnected.bind(this);
      if (!navigator.usb && navigator.usb.addEventListener) {
        navigator.usb.addEventListener('disconnect', function (event) {
          if (event.device === this.device) {
            this.onDisconnected();
          }
        }.bind(this));
      }
      };

  USB.prototype.connected = function () {
    return this.device !== null;
  };

  USB.prototype.request = function () {

    if (!navigator.usb || !navigator.usb.requestDevice) {
      return Promise.reject('USB not supported.');
    }

    return navigator.usb.requestDevice({
      filters: [{
        vendorId: this.info.vendorId,
        productId: this.info.productId
      }]
    }).then(function (device) {
      this.device = device;
      console.log(device.productName);
      console.log(device.manufacturerName);
      return;
    }.bind(this));

  };

  USB.prototype.print = function (data) {

    if (!this.device) {
      return Promise.reject('Device is not connected.');
    }

    return this.device.open().then(function () {
      console.log('Configuring');
      return this.device.selectConfiguration(1);
    }.bind(this)).then(function () {
      console.log('claiming');
      return this.device.claimInterface(0);
    }.bind(this)).then(function () {
      return OB.ESCPOS.printArray(this.printChunk.bind(this), 64, data);
    }.bind(this)).then(function () {
      return this.device.close();
    }.bind(this))['catch'](function (error) {
      this.onDisconnected();
      throw error;
    }.bind(this));
  };

  USB.prototype.printChunk = function (chunk) {
    return function () {
      console.log('transfering');
      return this.device.transferOut(1, chunk.buffer);
    }.bind(this);
  };

  USB.prototype.onDisconnected = function () {
    this.device = null;
  };

  window.OB = window.OB || {};
  OB.USB = USB;
}());