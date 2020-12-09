/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  window.OB = window.OB || {};

  OB.PRINTERTYPES = {
    HWM: null,
    GENERICBT: {
      WebDevice: OB.Bluetooth,
      buffersize: 20,
      devices: [],
      register: function(printerusb) {
        this.devices.push(printerusb);
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
