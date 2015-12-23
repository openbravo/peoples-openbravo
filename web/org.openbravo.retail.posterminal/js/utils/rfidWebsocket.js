/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global, WebSocket _ */

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
    OB.MobileApp.model.isRFIDEnabled = true;
  };

  // Called when a message is received from server
  OB.UTIL.rfidWebsocket.onmessage = function (event) {
    var message = JSON.parse(event.data);
    var existingEpc = false;
    var ean;
    _.each(OB.MobileApp.model.receipt.get('lines').models, function (line) {
      if (line.get('obposEpccode') === message.dataToSave.obposEpccode) {
        existingEpc = true;
        return;
      }
    });
    if (existingEpc) {
      return;
    }
    ean = message.gtin.substring(1, message.gtin.length);
    barcodeActionHandler.findProductByBarcode(ean, function (product) {
      product.set('groupProduct', false);
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
  this.waitForConnection(function () {
    OB.UTIL.rfidWebsocket.send('add:' + line.get('obposEpccode'));
  }, 1000);
};

OB.UTIL.eraseEpcOrderFromDeviceBuffer = function (order) {
  var epcCodes = '';
  _.each(order.get('lines').models, function (line) {
    if (line.get('obposEpccode')) {
      epcCodes = epcCodes + line.get('obposEpccode') + ',';
    }

  });
  if (epcCodes) {
    this.waitForConnection(function () {
      OB.UTIL.rfidWebsocket.send('erase:' + epcCodes.substring(0, epcCodes.length - 1));
    }, 1000);
  }
};

OB.UTIL.eraseEpcLineFromDeviceBuffer = function (line) {
  this.waitForConnection(function () {
    OB.UTIL.rfidWebsocket.send('erase:' + line.get('obposEpccode'));
  }, 1000);
};

OB.UTIL.eraseEpcOrderFromDeviceBufferBecauseTicketIsCompleted = function () {
  this.waitForConnection(function () {
    OB.UTIL.rfidWebsocket.send('erase2:');
  }, 1000);
};

OB.UTIL.eraseEpcBuffer = function () {
  this.waitForConnection(function () {
    OB.UTIL.rfidWebsocket.send('erase3:');
  }, 1000);
};

OB.UTIL.processRemainingCodes = function (order) {
  var epcCodesToAdd = '',
      epcCodesToErase = '';
  _.each(order.get('lines').models, function (line) {
    if (line.get('obposEpccode')) {
      if (line.get('qty') > 0) {
        epcCodesToAdd = epcCodesToAdd + line.get('obposEpccode') + ',';
      } else {
        epcCodesToErase = epcCodesToErase + line.get('obposEpccode') + ',';
      }
    }
  });
  if (epcCodesToAdd) {
    this.waitForConnection(function () {
      OB.UTIL.rfidWebsocket.send('add:' + epcCodesToAdd.substring(0, epcCodesToAdd.length - 1));
    }, 1000);
  }
  if (epcCodesToErase) {
    this.waitForConnection(function () {
      OB.UTIL.rfidWebsocket.send('erase:' + epcCodesToErase.substring(0, epcCodesToErase.length - 1));
    }, 1000);
  }
  this.waitForConnection(function () {
    OB.UTIL.rfidWebsocket.send('send:' + JSON.stringify(order));
  }, 1000);
};

OB.UTIL.disconnectRFIDDevice = function () {
  this.waitForConnection(function () {
    OB.UTIL.rfidWebsocket.send('disconnect:');
  }, 1000);
};

OB.UTIL.connectRFIDDevice = function () {
  this.waitForConnection(function () {
    OB.UTIL.rfidWebsocket.send('connect:');
  }, 1000);
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