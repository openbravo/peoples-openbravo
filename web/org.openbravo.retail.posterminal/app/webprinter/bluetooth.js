/*
 ************************************************************************************
 * Copyright (C) 2018-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  function Bluetooth(printertype) {
    this.printertype = printertype;
    this.device = null;
    this.size = this.printertype.buffersize || 20;
  }

  Bluetooth.prototype.connected = function connected() {
    return this.device !== null;
  };

  Bluetooth.prototype.request = function request() {
    if (!navigator.bluetooth || !navigator.bluetooth.requestDevice) {
      return Promise.reject(new Error('Bluetooth not supported.'));
    }

    const filters = [];
    this.printertype.devices.forEach(item =>
      filters.push({
        services: item.services,
        characteristict: item.characteristic
      })
    );

    return navigator.bluetooth
      .requestDevice({
        filters
      })
      .then(device => {
        this.device = device;
        this.deviceSelected = this.printertype.devices.find(
          d => d.genericName === device.name
        );
        if (OB.UTIL.isNullOrUndefined(this.deviceSelected)) {
          this.deviceSelected = this.printertype.devices.find(
            d => d.genericName === 'BlueTooth Printer'
          );
        }
        return this.deviceSelected;
      });
  };

  Bluetooth.prototype.print = function print(data) {
    let result;

    if (!this.device) {
      return Promise.reject(new Error('Device is not connected.'));
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
        .then(server => {
          this.server = server;
          return server.getPrimaryService(this.deviceSelected.services);
        })
        .then(service =>
          service.getCharacteristic(this.deviceSelected.characteristic)
        )
        .then(characteristic => {
          this.characteristic = characteristic;
          return OB.ARRAYS.printArray(
            this.printChunk.bind(this),
            this.size,
            data
          );
        });
    }

    return result.catch(error => {
      this.onDisconnected();
      throw error;
    });
  };

  Bluetooth.prototype.printChunk = function printChunck(chunk) {
    return () => this.characteristic.writeValue(chunk);
  };

  Bluetooth.prototype.onDisconnected = function onDisconnected() {
    this.device = null;
    this.characteristic = null;
    this.server = null;
  };

  OB.Bluetooth = Bluetooth;
})();
