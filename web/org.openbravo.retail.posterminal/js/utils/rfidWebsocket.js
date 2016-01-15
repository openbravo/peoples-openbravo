/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global, WebSocket _ */

OB.UTIL.startRfidWebsocket = function startRfidWebsocket(websocketServerLocation, reconnectTimeout, currentRetrials, retrialsBeforeWarning) {
  var barcodeActionHandler, retrialsBeforeThreadCancellation = 100;
  OB.UTIL.rfidWebsocket = new WebSocket(websocketServerLocation);
  OB.UTIL.rfidAckArray = [];
  OB.UTIL.isRFIDEnabled = true;

  // Called when socket connection is established
  OB.UTIL.rfidWebsocket.onopen = function () {
    if (currentRetrials >= retrialsBeforeWarning) {
      currentRetrials = 0;
      OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_ConnectedWithRFID'));
      OB.info(OB.I18N.getLabel('OBPOS_ConnectedWithRFID'));
    }
    if (OB.POS.modelterminal.get('terminal').terminalType.rfidtimeout) {
      if (OB.UTIL.rfidTimeout) {
        clearTimeout(OB.UTIL.rfidTimeout);
      }
      OB.UTIL.rfidTimeout = setTimeout(function () {
        OB.UTIL.disconnectRFIDDevice();
      }, OB.POS.modelterminal.get('terminal').terminalType.rfidtimeout * 1000 * 60);
    }
    barcodeActionHandler = new OB.UI.BarcodeActionHandler();
    OB.UTIL.removeAllEpcs();
    OB.UTIL.isRFIDEnabled = true;
    OB.MobileApp.view.waterfall('onRfidConnectionRecovered');
  };

  // Called when a message is received from server
  OB.UTIL.rfidWebsocket.onmessage = function (event) {
    var data, ean, i, line;
    if (event.data.startsWith('uuid:')) {
      OB.UTIL.rfidAckArray.push(event.data.split(':')[1]);
      return
    }
    data = JSON.parse(event.data)
    if (OB.UTIL.rfidTimeout) {
      clearTimeout(OB.UTIL.rfidTimeout);
    }
    OB.UTIL.rfidTimeout = setTimeout(function () {
      OB.UTIL.disconnectRFIDDevice();
    }, OB.POS.modelterminal.get('terminal').terminalType.rfidtimeout * 1000 * 60);
    for (i = 0; i < OB.MobileApp.model.receipt.get('lines').length; i++) {
      line = OB.MobileApp.model.receipt.get('lines').models[i];
      if (line.get('obposEpccode') === data.dataToSave.obposEpccode) {
        return;
      }
    }
    ean = data.gtin.substring(1, data.gtin.length);
    barcodeActionHandler.findProductByBarcode(ean, function (product) {
      product.set('groupProduct', false);
      OB.MobileApp.model.receipt.addProduct(product, '1', {
        rfid: true
      }, data.dataToSave);
    });
  };

  // Called when socket connection closed
  OB.UTIL.rfidWebsocket.onclose = function () {
    currentRetrials++;
    if (currentRetrials === retrialsBeforeWarning) {
      OB.UTIL.showI18NWarning('OBPOS_RFIDNotAvailable');
      OB.warn(OB.I18N.getLabel('OBPOS_RFIDNotAvailable'));
    }
    setTimeout(function () {
      startRfidWebsocket(websocketServerLocation, reconnectTimeout, currentRetrials, retrialsBeforeWarning);
    }, reconnectTimeout);
    OB.MobileApp.view.waterfall('onRfidConnectionLost');
  };

  // Called in case of an error
  OB.UTIL.rfidWebsocket.onerror = function (err) {
    OB.warn(err.data);
  };
};

OB.UTIL.addEpcLine = function (line) {
  this.waitForAck(function (uuid) {
    OB.UTIL.rfidWebsocket.send('addEpcs:' + uuid + ':' + line.get('obposEpccode'));
  }, function () {}, function () {}, 3000, OB.UTIL.get_UUID(), 5);
};

OB.UTIL.eraseEpcOrder = function (order) {
  var epcCodes = '';
  _.each(order.get('lines').models, function (line) {
    if (line.get('obposEpccode')) {
      epcCodes = epcCodes + line.get('obposEpccode') + ',';
    }

  });
  if (epcCodes) {
    this.waitForAck(function (uuid) {
      OB.UTIL.rfidWebsocket.send('removeEpcs:' + uuid + ':' + epcCodes.substring(0, epcCodes.length - 1));
    }, function () {}, function () {}, 3000, OB.UTIL.get_UUID(), 5);
  }
};

OB.UTIL.removeEpcLine = function (line) {
  this.waitForAck(function (uuid) {
    OB.UTIL.rfidWebsocket.send('removeEpcs:' + uuid + ':' + line.get('obposEpccode'));
  }, function () {}, function () {}, 3000, OB.UTIL.get_UUID(), 5);
};

OB.UTIL.updateEpcBuffers = function () {
  this.waitForAck(function (uuid) {
    OB.UTIL.rfidWebsocket.send('updateEpcBuffers:' + uuid);
  }, function () {}, function () {}, 3000, OB.UTIL.get_UUID(), 5);
};

OB.UTIL.removeAllEpcs = function () {
  this.waitForAck(function (uuid) {
    OB.UTIL.rfidWebsocket.send('removeAllEpcs:' + uuid);
  }, function () {}, function () {}, 3000, OB.UTIL.get_UUID(), 5);
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
    this.waitForAck(function (uuid) {
      OB.UTIL.rfidWebsocket.send('addEpc:' + uuid + ':' + epcCodesToAdd.substring(0, epcCodesToAdd.length - 1));
    }, function () {}, function () {}, 3000, OB.UTIL.get_UUID(), 5);
  }
  if (epcCodesToErase) {
    this.waitForAck(function (uuid) {
      OB.UTIL.rfidWebsocket.send('removeEpc:' + uuid + ':' + epcCodesToErase.substring(0, epcCodesToErase.length - 1));
    }, function () {}, function () {}, 3000, OB.UTIL.get_UUID(), 5);
  }
  //Only if useSecurityGate check is enabled
  if (OB.POS.modelterminal.get('terminal').terminalType.usesecuritygate) {
    this.waitForAck(function (uuid) {
      OB.UTIL.rfidWebsocket.send('send:' + uuid + ':' + JSON.stringify(order));
    }, function () {}, function () {}, 3000, OB.UTIL.get_UUID(), 5);
  }
};

OB.UTIL.connectRFIDDevice = function () {
  this.waitForAck(function (uuid) {
    OB.UTIL.rfidWebsocket.send('connect:' + uuid);
  }, function () {
    OB.MobileApp.view.waterfall('onConnectRfidDevice');
  }, function () {
    OB.MobileApp.view.waterfall('onRfidConnectionLost');
  }, 3000, OB.UTIL.get_UUID());
};

OB.UTIL.disconnectRFIDDevice = function () {
  this.waitForAck(function (uuid) {
    OB.UTIL.rfidWebsocket.send('disconnect:' + uuid);
  }, function () {
    OB.MobileApp.view.waterfall('onDisconnectRfidDevice');
  }, function () {
    OB.MobileApp.view.waterfall('onRfidConnectionLost');
  }, 3000, OB.UTIL.get_UUID());
};

OB.UTIL.waitForAck = function (functionToExecute, callback, errorCallback, interval, uuid, trials) {
  var index = OB.UTIL.rfidAckArray.indexOf(uuid),
      i;
  if (index > -1) {
    OB.UTIL.rfidAckArray.splice(index, 1);
    for (i = OB.UTIL.rfidAckArray.length - 1; i >= 0; i--) {
      if (OB.UTIL.rfidAckArray[i] === uuid) {
        OB.UTIL.rfidAckArray.splice(i, 1);
      }
    }
    callback();
  } else if (OB.UTIL.rfidWebsocket.readyState !== 1) {
    setTimeout(function () {
      OB.UTIL.waitForAck(functionToExecute, callback, errorCallback, interval, uuid, trials - 1);
    }, interval);
  } else if (!trials || trials > 0) {
    functionToExecute(uuid);
    setTimeout(function () {
      OB.UTIL.waitForAck(functionToExecute, callback, errorCallback, interval, uuid, trials - 1);
    }, interval);
  } else {
    errorCallback();
  }
};