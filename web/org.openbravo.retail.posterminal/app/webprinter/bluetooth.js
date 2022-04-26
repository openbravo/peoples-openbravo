/*
 ************************************************************************************
 * Copyright (C) 2018-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  function Bluetooth(printertype) {
    this.printertype = printertype;
    this.device = null;
    this.deviceSelected = null;
    this.size = this.printertype.buffersize || 20;
  }

  Bluetooth.prototype.request = async function request() {
    if (!navigator.bluetooth || !navigator.bluetooth.requestDevice) {
      return Promise.reject(new Error('Bluetooth not supported.'));
    }

    if (!this.device) {
      // Not found paired device, ask the user gesture to pair
      if (
        !(await OB.App.View.DialogUIHandler.askConfirmation({
          title: 'OBPOS_WebPrinter',
          message: 'OBPOS_WebPrinterPair'
        }))
      ) {
        // To unify behaviour with navigator.usb.requestDevice
        // that fails if cancelled by the user
        throw new Error('Cancelled by the user.');
      }
      // Request the device
      const filters = [];
      this.printertype.devices.forEach(item =>
        filters.push({
          services: item.services
        })
      );
      // Fails if cancelled by the user or not possible to pair device.
      const device = await navigator.bluetooth.requestDevice({
        filters
      });

      this.device = device;
      this.deviceSelected =
        this.printertype.devices.find(d => d.genericName === device.name) ||
        this.printertype.devices.find(
          d => d.genericName === 'BlueTooth Printer'
        );
    }

    return this.deviceSelected;
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
