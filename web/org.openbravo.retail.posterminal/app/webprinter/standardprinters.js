/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  OB.PRINTERTYPES = {
    HWM: null,
    GENERICBT: {
      WebDevice: OB.Bluetooth,
      buffersize: 20,
      devices: [],
      register: function register(printerusb) {
        this.devices.push(printerusb);
      }
    },
    GENERICUSB: {
      WebDevice: OB.USB,
      devices: [],
      register: function register(printerusb) {
        this.devices.push(printerusb);
      }
    }
  };

  OB.PRINTERIMAGES = {
    imageFunctions: [],
    getImagesMap: function getImagesMap() {
      const images = [];
      this.imageFunctions.forEach(f => {
        f().forEach(imagedata => {
          images[imagedata.name] = imagedata.url;
        });
      });
      return images;
    },
    register: function register(f) {
      this.imageFunctions.push(f);
    }
  };

  // Register default *ticket-image.png* and example of PRINTERIMAGES extensibility:
  // ../../utility/ShowImageLogo?logo=yourcompanymenu
  // yourcompanylogin, youritservicelogin, yourcompanymenu, yourcompanybig, yourcompanydoc, yourcompanylegal
  OB.PRINTERIMAGES.register(() => {
    const terminal = OB.App.TerminalProperty.get('terminal');
    return [
      {
        name: 'yourcompanybig',
        url: `../../utility/ShowImageLogo?logo=yourcompanybig&orgId=${terminal.organization}`
      },
      {
        name: 'ticket-image.png',
        url: './img/openbravo-logo.png'
      }
    ];
  });
})();
