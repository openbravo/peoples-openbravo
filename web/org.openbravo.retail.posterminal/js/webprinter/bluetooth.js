/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var Bluetooth = function(printertype) {
    this.printertype = printertype;
    this.device = null;
    this.size = this.printertype.buffersize || 20;
  };

  Bluetooth.prototype.connected = function() {
    return this.device !== null;
  };

  Bluetooth.prototype.request = function() {
    if (!navigator.bluetooth || !navigator.bluetooth.requestDevice) {
      return Promise.reject('Bluetooth not supported.');
    }

    const filters = [];
    this.printertype.devices.forEach(function(item) {
      filters.push({
        services: item.services,
        characteristict: item.characteristic
      });
    });

    return navigator.bluetooth
      .requestDevice({
        filters: filters
      })
      .then(
        function(device) {
          this.device = device;
          this.deviceSelected = this.printertype.devices.find(function(d) {
            return d.genericName === device.name;
          });
          if (OB.UTIL.isNullOrUndefined(this.deviceSelected)) {
            this.deviceSelected = this.printertype.devices.find(function(d) {
              return d.genericName === 'BlueTooth Printer';
            });
          }
          return this.deviceSelected;
        }.bind(this)
      );
  };

  Bluetooth.prototype.print = function(data) {
    var result;

    if (!this.device) {
      return Promise.reject('Device is not connected.');
    }

    if (this.characteristic) {
      result = OB.ARRAYS.printArray(
        this.printChunk.bind(this),
        this.size,
        data
      );
    } else {
      result = this.device.gatt
        .connect()
        .then(
          function(server) {
            this.server = server;
            return server.getPrimaryService(this.deviceSelected.services);
          }.bind(this)
        )
        .then(
          function(service) {
            return service.getCharacteristic(
              this.deviceSelected.characteristic
            );
          }.bind(this)
        )
        .then(
          function(characteristic) {
            this.characteristic = characteristic;
            return OB.ARRAYS.printArray(
              this.printChunk.bind(this),
              this.size,
              data
            );
          }.bind(this)
        );
    }

    return result.catch(
      function(error) {
        this.onDisconnected();
        throw error;
      }.bind(this)
    );
  };

  Bluetooth.prototype.printChunk = function(chunk) {
    return function() {
      return this.characteristic.writeValue(chunk);
    }.bind(this);
  };

  Bluetooth.prototype.onDisconnected = function() {
    this.device = null;
    this.characteristic = null;
    this.server = null;
  };

  window.OB = window.OB || {};
  OB.Bluetooth = Bluetooth;
})();
