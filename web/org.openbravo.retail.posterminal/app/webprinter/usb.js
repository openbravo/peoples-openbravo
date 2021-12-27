/*
 ************************************************************************************
 * Copyright (C) 2018-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  function USB(printertype) {
    this.printertype = printertype;
    this.device = null;
    this.onDisconnected = this.onDisconnected.bind(this);
    if (navigator.usb && navigator.usb.addEventListener) {
      navigator.usb.addEventListener('disconnect', event => {
        if (event.device === this.device) {
          this.onDisconnected();
        }
      });
    }
  }

  USB.prototype.connected = function connected() {
    return this.device !== null;
  };

  USB.prototype.request = function request() {
    if (!navigator.usb || !navigator.usb.requestDevice) {
      return Promise.reject(new Error('USB not supported.'));
    }

    const filters = [];
    this.printertype.devices.forEach(item => {
      filters.push({
        vendorId: item.vendorId,
        productId: item.productId
      });
    });

    return navigator.usb
      .requestDevice({
        filters
      })
      .then(device => {
        this.device = device;
        return this.printertype.devices.find(
          d =>
            d.vendorId === device.vendorId && d.productId === device.productId
        );
      });
  };

  USB.prototype.print = function print(data) {
    if (!this.device) {
      return Promise.reject(new Error('Device is not connected.'));
    }

    return this.device
      .open()
      .then(() =>
        this.device.selectConfiguration(
          this.device.configuration.configurationValue
        )
      )
      .then(() =>
        this.device.claimInterface(
          this.device.configuration.interfaces[0].interfaceNumber
        )
      )
      .then(() => OB.ARRAYS.printArray(this.printChunk.bind(this), 64, data))
      .then(() => this.device.close())
      .catch(error => {
        this.onDisconnected();
        throw error;
      });
  };

  USB.prototype.printChunk = function printChunk(chunk) {
    let out = 1;
    const { endpoints } = this.device.configuration.interfaces[0].alternate;
    for (let i = 0; endpoints.length; i += 1) {
      if (endpoints[i].direction === 'out') {
        out = endpoints[i].endpointNumber;
        break;
      }
    }
    return () => this.device.transferOut(out, chunk.buffer);
  };

  USB.prototype.onDisconnected = function onDisconnected() {
    this.device = null;
  };

  OB.USB = USB;
})();
