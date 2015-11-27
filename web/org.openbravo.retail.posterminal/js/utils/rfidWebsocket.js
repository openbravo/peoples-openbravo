/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Websocket */

OB.UTIL.rfidWebsocket = null;
OB.UTIL.startRfidWebsocket = function startRfidWebsocket(websocketServerLocation, reconnectTimeout, currentRetrials, retrialsBeforeWarning) {
  OB.UTIL.rfidWebsocket = new WebSocket(websocketServerLocation);
  var barcodeActionHandler;

  // Called when socket connection is established
  OB.UTIL.rfidWebsocket.onopen = function () {
    if (currentRetrials >= retrialsBeforeWarning) {
      currentRetrials = 0;
      OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_ConnectedWithRFID'));
      OB.info(OB.I18N.getLabel('OBPOS_ConnectedWithRFID'));
    }
    barcodeActionHandler = new OB.UI.BarcodeActionHandler();
  };

  // Called when a message is received from server
  OB.UTIL.rfidWebsocket.onmessage = function (event) {
    var message = JSON.parse(event.data);
    _.each(OB.MobileApp.model.receipt.get('lines').models, function (line) {
      if (line.get('obposEpccode') === message.dataToSave.obposEpccode) {
        return;
      }
    });
    barcodeActionHandler.findProductByBarcode(message.uPCEAN, function (product) {
      product.set('groupProduct', false)
      OB.MobileApp.model.receipt.addProduct(product, '1', {
        rfid: true
      }, message.dataToSave);
    });
  };

  // Called when socket connection closed
  OB.UTIL.rfidWebsocket.onclose = function () {
    currentRetrials++;
    if (currentRetrials === retrialsBeforeWarning) {
      OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_RFIDNotAvailable'));
      OB.warn(OB.I18N.getLabel('OBPOS_RFIDNotAvailable'));
    }
    setTimeout(function () {
      startRfidWebsocket(websocketServerLocation, reconnectTimeout, currentRetrials, retrialsBeforeWarning);
    }, reconnectTimeout);
  };

  // Called in case of an error
  OB.UTIL.rfidWebsocket.onerror = function (err) {};
};

OB.UTIL.addEpcLineToDeviceBuffer = function (line) {
  OB.UTIL.rfidWebsocket.send('add:' + line.get('obposEpccode'));
};

OB.UTIL.eraseEpcOrderFromDeviceBuffer = function (order) {
  var epcCodes = '';
  _.each(order.get('lines').models, function (line) {
    if (line.get('obposEpccode')) {
      epcCodes = epcCodes + line.get('obposEpccode') + ',';
    }

  });
  if (epcCodes) {
    OB.UTIL.rfidWebsocket.send('erase:' + epcCodes.substring(0, epcCodes.length - 1));
  }
};

OB.UTIL.eraseEpcLineFromDeviceBuffer = function (line) {
  OB.UTIL.rfidWebsocket.send('erase:' + line.get('obposEpccode'));
};

OB.UTIL.eraseEpcOrderFromDeviceBufferBecauseTicketIsCompleted = function () {
  OB.UTIL.rfidWebsocket.send('erase2:');
};

OB.UTIL.eraseEpcBuffer = function () {
  this.waitForConnection(function () {
    OB.UTIL.rfidWebsocket.send('erase3:');
    if (typeof callback !== 'undefined') {
      callback();
    }
  }, 1000);
};

OB.UTIL.checkEpcOrderInDeviceBuffer = function (order) {
  var epcCodes = '';
  _.each(order.get('lines').models, function (line) {
    if (line.get('obposEpccode')) {
      epcCodes = epcCodes + line.get('obposEpccode') + ',';
    }

  });
  if (epcCodes) {
    OB.UTIL.rfidWebsocket.send('check:' + epcCodes.substring(0, epcCodes.length - 1));
  }
};

OB.UTIL.waitForConnection = function (callback, interval) {
  if (OB.UTIL.rfidWebsocket.readyState === 1) {
    callback();
  } else {
    var that = this;
    setTimeout(function () {
      that.waitForConnection(callback, interval);
    }, interval);
  }
};