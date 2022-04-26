/*
 ************************************************************************************
 * Copyright (C) 2018-2022 Openbravo S.L.U.
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

  USB.prototype.request = async function request() {
    if (
      !navigator.usb ||
      !navigator.usb.requestDevice ||
      !navigator.usb.getDevices
    ) {
      throw new Error('USB not supported.');
    }

    // Find supported usb device already paired
    const usbdevices = await navigator.usb.getDevices();
    this.device = usbdevices.find(device =>
      this.printertype.devices.some(
        info =>
          device.vendorId === info.vendorId &&
          device.productId === info.productId
      )
    );

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
      this.printertype.devices.forEach(item => {
        filters.push({
          vendorId: item.vendorId,
          productId: item.productId
        });
      });
      // Fails if cancelled by the user or not possible to pair device.
      this.device = await navigator.usb.requestDevice({
        filters
      });
    }

    return this.printertype.devices.find(
      d =>
        d.vendorId === this.device.vendorId &&
        d.productId === this.device.productId
    );
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
