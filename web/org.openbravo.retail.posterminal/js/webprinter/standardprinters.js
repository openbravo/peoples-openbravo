/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

(function () {

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
      register: function (printerusb) {
        this.devices.push(printerusb);
      }
    }
  };

  OB.PRINTERIMAGES = {
    // For Hardware Manager compatilibity register standard image
    // Posible URLs
    // https://livebuilds.openbravo.com/retail_pi_pgsql/org.openbravo.retail.posterminal.service.loginutils?terminalName=VBS-1&command=companyLogo&appModuleId=FF808181326CC34901326D53DBCF0018&appName=WebPOS&0.38810264271123907
    // {"logoUrl":"data:image\/png;base64,iVBORw0KGgoAAAANSUhEUgAAAJQAAAA3CAYAAADqtGOUAAAGOklEQVR42u1dy3HbMBBNBykhJaQDeZzJPSX4louTcQkqISWoBM7YzikHlIASXAJKUAQSkIDlYvElSNq7MzhEJigReNh9u3hgPn1iY2NjY2NjY2PblP389\/3z7+f7u18vh+Pj693p18udmLfD8dKe9HU8YmwoiDRALmCRl3bObOrShsfXwwOP5Ae3x7\/fvhgvdG7RLvd6095LA5RH98OFtcOfVkDCvBZ7rA9imvdMnmQxMLltKPFW+jde28v91+g1AS6n+8JrLEe0jfp9Xn\/nd2Cfa28Pf1NSq+i\/OpgMTzp3bioEivDv9Lkczvn87wncx71GWiC6n1MTY5IQe62gPp8SlaLxKe6\/Ll9qyJWWBhUcXNgXggIDhu7jX3M4MqDeB5iyQQUnHfIxfPAPT\/4zHx4w4CwFKPN9SIkFJi3+3zWXDTyTiLWVwOQP7F5AZcoRZtKnQXf+NiAZ5skHpZd0qBBYWwGKuMfMS6Z45W0S8Jnb30STKUQdgEZgHEuveCfBkAQYBgZUg9JAx2wuq0GPk+JZsUkyoXygroEhkwFVntEdtwimlIm0aTRGzH1A6Kz1lrleeRLwzPpeBKGXBE9RawEqVjLoWjyGk7HRJuJe6uZhrZeBAIIAQ7ybjGWIub+3B6BqF+R7zOqqB8Ul1jZMus+GhcBQPwZU1Uav56q33AZyYTzf\/5jXa65FT4EUQiWccH0PGlCTagJroMDaG1BkySC3WLy3anj5hrLDb2IT4v7b9TyuR8L6UIBiUl64f2c1TiYr2pAH8wuSdPngNuhu5uZzJm9BDQyoRuCJZQRTJlSsgeoW9lyAeCTdzdz8rE5SYGVAZYIpRix1eNC8wgWZnpAVibwqKM4qqrIe2gNkQC3Pn0alpQWXUW4ee4fEGI9CCrRDZPJHb5ay6LYKqJS9vMU1ZxXFTKU9lJ3Y1krO6iLn7LfMJwY+O9zb2yGgUvjncWFA+au0UMZ7sh6rlxgPpvaR8gEKgplCIXBPBlRnQN0OHkyEtoNcODowKSrLVCXmUorNlCSJCu0lis0YVdgSoK4Z0zUMTl5CrQEottVIeXNAeQcOTDb41rsWxbaSLUmkbWXabO3IJUk5dP814RAeVIiFiZxw1rxfxqGG1AMLcHyytmyWlqxYwt4aVDE9OEXaQ8pMouQgM7y8KIwOYsGoIrB+IVDNMZFBL2A2tKTikgKV1U5Pkz0eWwdtHAxJFfFcIFCCPPc+sFQQGg\/KS+0FUIhESQTu7\/JelaWj6qiDkkDZIErfdRA+d3fzPKEiJXxe6MnCFCDM2VYGlIqqDJzFBZ9vRh1m5wkKkp9+sl98Uswm9JOzEY0Oiv47lbZTqsvAgCVtx8TC3sqAyjrJEvNSvkDx7q1I5bl0hdutqIMHk62Lmv7imAPYVSIkhLshJeztCVDYfNvnih1Fa7lBXF1Bt29rGU+dGEBgR5uqN4Z9wj1QHigS7tT8tDHuYfcGKCiotAvLExkGKMNaYU8ge3wzoleiFI2dfoFhz1NI+AtHIYN8RgZ5iIW9lQElS95ngB1waOKdwmSsfvslokIQJVlmytaBxwMcLwRI+4l6fttv9jny\/VvP8lK8VIrqoreX8rZcUu7lhD7RwjvFgOOVC0C4A55IhTwXrlrYH6CoGmQziUspl7KTZg6KnjL6jVlE4mnl5HqIez+72kB2Q4Y7yL0AkX\/bW9kgx4k0804BUptcCjCTKEs3eWNAjMlVyCLnuJ94C12xcKfHwD\/JQr\/ZZW+knAz1rQV4OVsk7gZwhapA3V76WhfqwgtjVKUORLgTNYnBngGVo\/UqR+0UHlQKmAzCVV15wZ7wRSe2aMBAGBWhcFeyUwDDAgMqfUIUGaba7QPKQKYpa87jY4kBDHeIpj7ERWQo7DGgKkB1PbpdF+bO2GkTQI5F7csdMD6IhDuZQkhnL+Jwwl723poBY69+1KnhroBCONXoMVJCYql2SnOdEs6UUuSMhbvY94bAl8\/Bpknr1Y8CSndAuTULZ4XIJdUITfmgv9lJhruYkCx0PQNq\/Yo6VmE\/LhW2Q8pL7BXSMW+NKTlzFZTXE0Kd+rU6gLEot2qhUOD\/RYFthm7tsUZ5Svq2jbSFQx5BtqQ0lBLKs7GxsbGxsbGxsX1U+w9z2RUjLLYRUQAAAABJRU5ErkJggg=="}
    // https://livebuilds.openbravo.com/retail_pi_pgsql/utility/ShowImageLogo?logo=yourcompanymenu
    // yourcompanylogin, youritservicelogin, yourcompanymenu, yourcompanybig, yourcompanydoc, yourcompanylegal
    images: {
      'ticket-image.png': '../../utility/ShowImageLogo?logo=yourcompanybig'
    },
    register: function (name, url) {
      this.images[name] = url;
    }
  };
}());