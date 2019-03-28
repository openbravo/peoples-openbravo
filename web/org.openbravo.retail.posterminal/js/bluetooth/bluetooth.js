/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise  */

(function () {

  window.OB = window.OB || {};

  OB.Bluetooth = function () {
    this.size = 20;
    this.device = null;
    this.onDisconnected = this.onDisconnected.bind(this);
  };

  OB.Bluetooth.prototype.connected = function () {
    return this.device !== null;
  };

  OB.Bluetooth.prototype.request = function () {

    if (!navigator.bluetooth || !navigator.bluetooth.requestDevice) {
      return Promise.reject('Bluetooth not supported.');
    }

    return navigator.bluetooth.requestDevice({
      // "filters": [{
      // }],
      optionalServices: ['e7810a71-73ae-499d-8c15-faa9aef0c3f2'],
      acceptAllDevices: true
    }).then(function (device) {
      this.device = device;
      this.device.addEventListener('gattserverdisconnected', this.onDisconnected);
    }.bind(this));
  };

  OB.Bluetooth.prototype.print = function (data) {
    if (!this.device) {
      return Promise.reject('Device is not connected.');
    }

    if (this.characteristic) {
      return this.printArray(data);
    }

    return this.device.gatt.connect().then(function (server) {
      this.server = server;
      return server.getPrimaryService('e7810a71-73ae-499d-8c15-faa9aef0c3f2');
    }.bind(this)).then(function (service) {
      return service.getCharacteristic('bef8d6c9-9c21-4c9e-b632-bd58c1009f9f');
    }.bind(this)).then(function (characteristic) {
      this.characteristic = characteristic;
      this.printArray(data);
    }.bind(this));
  };

  OB.Bluetooth.prototype.printArray = function (data) {
    var i, result;

    result = Promise.resolve();
    for (i = 0; i < data.length; i += this.size) {
      result = result.then(this.printChunk(data.slice(i, i + this.size)));
    }
    return result;
  };

  OB.Bluetooth.prototype.printChunk = function (chunk) {
    return function () {
      return this.characteristic.writeValue(chunk);
    }.bind(this);
  };

  OB.Bluetooth.prototype.onDisconnected = function () {
    this.device = null;
    this.characteristic = null;
    this.server = null;
  };

}());