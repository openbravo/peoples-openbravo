/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

(function() {
  window.OB = window.OB || {};

  OB.PRINTERTYPES = {
    HWM: null,
    GENERICBT: {
      WebDevice: OB.Bluetooth,
      buffersize: 20,
      device: {
        name: 'Generic Bluetooth Receipt Printer',
        service: 'e7810a71-73ae-499d-8c15-faa9aef0c3f2',
        characteristic: 'bef8d6c9-9c21-4c9e-b632-bd58c1009f9f',
        ESCPOS: OB.ESCPOS.Standard
      }
    },
    GENERICUSB: {
      WebDevice: OB.USB,
      devices: [],
      register: function(printerusb) {
        this.devices.push(printerusb);
      }
    }
  };

  OB.PRINTERIMAGES = {
    imageFunctions: [],
    getImagesMap: function() {
      var images = [];
      this.imageFunctions.forEach(function(f) {
        f().forEach(function(imagedata) {
          images[imagedata.name] = imagedata.url;
        });
      });
      return images;
    },
    register: function(f) {
      this.imageFunctions.push(f);
    }
  };

  // Register default *ticket-image.png* and example of PRINTERIMAGES extensibility:
  // ../../utility/ShowImageLogo?logo=yourcompanymenu
  // yourcompanylogin, youritservicelogin, yourcompanymenu, yourcompanybig, yourcompanydoc, yourcompanylegal
  OB.PRINTERIMAGES.register(function() {
    return [
      {
        name: 'yourcompanybig',
        url:
          '../../utility/ShowImageLogo?logo=yourcompanybig&orgId=' +
          OB.MobileApp.model.get('terminal').organization
      },
      {
        name: 'ticket-image.png',
        url: './img/openbravo-logo.png'
      }
    ];
  });
})();
