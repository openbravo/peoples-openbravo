/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global, WebSocket, _ Backbone */

OB.UTIL.RfidController = new Backbone.Model({
  connected: false
});

OB.UTIL.RfidController.isRfidConfigured = function () {
  if (OB.POS.hwserver && OB.POS.modelterminal.get('terminal')) {
    return OB.POS.hwserver.url && OB.POS.modelterminal.get('terminal').terminalType.useRfid && window.location.protocol === OB.POS.hwserver.url.split('/')[0];
  } else {
    return false;
  }
};

OB.UTIL.RfidController.startRfidWebsocket = function startRfidWebsocket(websocketServerLocation, reconnectTimeout, currentRetrials, retrialsBeforeWarning) {
  OB.UTIL.RfidController.set('retrialsBeforeThreadCancellation', 100);
  OB.UTIL.RfidController.set('rfidWebsocket', new WebSocket(websocketServerLocation));
  OB.UTIL.RfidController.set('rfidAckArray', []);
  OB.UTIL.RfidController.set('isRFIDEnabled', true);
  OB.UTIL.RfidController.set('reconnectOnScanningFocus', true);

  // Called when socket connection is established
  OB.UTIL.RfidController.get('rfidWebsocket').onopen = function () {
    if (currentRetrials >= retrialsBeforeWarning) {
      currentRetrials = 0;
      OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_ConnectedWithRFID'));
      OB.debug(OB.I18N.getLabel('OBPOS_ConnectedWithRFID'));
    }
    if (OB.POS.modelterminal.get('terminal').terminalType.rfidTimeout) {
      if (OB.UTIL.RfidController.get('rfidTimeout')) {
        clearTimeout(OB.UTIL.RfidController.get('rfidTimeout'));
      }
      OB.UTIL.RfidController.set('rfidTimeout', setTimeout(function () {
        OB.UTIL.RfidController.unset('rfidTimeout');
        OB.UTIL.RfidController.set('reconnectOnScanningFocus', false);
        OB.UTIL.RfidController.disconnectRFIDDevice();
      }, OB.POS.modelterminal.get('terminal').terminalType.rfidTimeout * 1000 * 60));
    }
    OB.UTIL.RfidController.set('barcodeActionHandler', new OB.UI.BarcodeActionHandler());
    OB.UTIL.RfidController.removeAllEpcs();
    OB.UTIL.RfidController.set('connected', true);
    OB.UTIL.RfidController.set('connectionLost', false);
  };

  // Called when a message is received from server
  OB.UTIL.RfidController.get('rfidWebsocket').onmessage = function (event) {
    var data, ean, i, line;
    if (event.data.startsWith('doNotReconnect')) {
      OB.UTIL.RfidController.get('rfidWebsocket').onclose = function () {};
      OB.UTIL.RfidController.set('connected', false);
      OB.UTIL.RfidController.set('connectionLost', true);
      OB.UTIL.RfidController.get('rfidWebsocket').close();
      return;
    }
    if (event.data.startsWith('uuid:')) {
      OB.UTIL.RfidController.get('rfidAckArray').push(event.data.split(':')[1]);
      return;
    }
    data = JSON.parse(event.data);
    if (OB.POS.modelterminal.get('terminal').terminalType.rfidTimeout) {
      if (OB.UTIL.RfidController.get('rfidTimeout')) {
        clearTimeout(OB.UTIL.RfidController.get('rfidTimeout'));
      }
      OB.UTIL.RfidController.set('rfidTimeout', setTimeout(function () {
        OB.UTIL.RfidController.unset('rfidTimeout');
        OB.UTIL.RfidController.set('reconnectOnScanningFocus', false);
        OB.UTIL.RfidController.disconnectRFIDDevice();
      }, OB.POS.modelterminal.get('terminal').terminalType.rfidTimeout * 1000 * 60));
    }
    for (i = 0; i < OB.MobileApp.model.receipt.get('lines').length; i++) {
      line = OB.MobileApp.model.receipt.get('lines').models[i];
      if (line.get('obposEpccode') === data.dataToSave.obposEpccode || ('0' + line.get('product').get('uPCEAN') === data.gtin && line.get('obposSerialNumber') === data.dataToSave.obposSerialNumber)) {
        return;
      }
    }
    ean = data.gtin.substring(1, data.gtin.length);
    OB.UTIL.RfidController.get('barcodeActionHandler').findProductByBarcode(ean, function (product) {
      product.set('groupProduct', false);

      OB.MobileApp.view.waterfall('onAddProduct', {
        product: product,
        qty: 1,
        options: {
          rfid: true,
          blockAddProduct: true
        },
        attrs: data.dataToSave
      });
    }, null, data.dataToSave);
  };

  // Called when socket connection closed
  OB.UTIL.RfidController.get('rfidWebsocket').onclose = function () {
    currentRetrials++;
    if (currentRetrials === retrialsBeforeWarning) {
      OB.UTIL.showI18NWarning('OBPOS_RFIDNotAvailable');
      OB.warn(OB.I18N.getLabel('OBPOS_RFIDNotAvailable'));
    }
    setTimeout(function () {
      OB.UTIL.RfidController.startRfidWebsocket(websocketServerLocation, reconnectTimeout, currentRetrials, retrialsBeforeWarning);
    }, reconnectTimeout);
    OB.UTIL.RfidController.set('connected', false);
    OB.UTIL.RfidController.set('connectionLost', true);
  };

  // Called in case of an error
  OB.UTIL.RfidController.get('rfidWebsocket').onerror = function (err) {
    OB.warn(err.data);
  };
};

OB.UTIL.RfidController.addEpcLine = function (line, callback, errorCallback) {
  OB.UTIL.RfidController.waitForAck(function (uuid) {
    OB.UTIL.RfidController.get('rfidWebsocket').send('addEpcs:' + uuid + ':' + line.get('obposEpccode'));
    OB.debug('addEpcLine sent, UUID: ' + uuid);
  }, function () {
    OB.debug('addEpcLine ended succesfully: ' + line.get('id'));
    if (callback) {
      callback();
    }
  }, function () {
    OB.debug('error while addEpcLine: ' + line.get('id'));
    if (errorCallback) {
      errorCallback();
    }
  }, 2000, OB.UTIL.get_UUID(), 5);
};

OB.UTIL.RfidController.eraseEpcOrder = function (order, callback, errorCallback) {
  var epcCodes = '';
  _.each(order.get('lines').models, function (line) {
    if (line.get('obposEpccode')) {
      epcCodes = epcCodes + line.get('obposEpccode') + ',';
    }
  });
  if (epcCodes) {
    OB.UTIL.RfidController.waitForAck(function (uuid) {
      OB.UTIL.RfidController.get('rfidWebsocket').send('removeEpcs:' + uuid + ':' + epcCodes.substring(0, epcCodes.length - 1));
      OB.debug('eraseEpcOrder sent, UUID: ' + uuid);
    }, function () {
      OB.debug('eraseEpcOrder ended succesfully: ' + order.get('id'));
      if (callback) {
        callback();
      }
    }, function () {
      OB.debug('error while eraseEpcOrder: ' + order.get('id'));
      if (errorCallback) {
        errorCallback();
      }
    }, 2000, OB.UTIL.get_UUID(), 5);
  }
};

OB.UTIL.RfidController.removeEpc = function (epc, callback, errorCallback) {
  OB.UTIL.RfidController.waitForAck(function (uuid) {
    OB.UTIL.RfidController.get('rfidWebsocket').send('removeEpcs:' + uuid + ':' + epc);
    OB.debug('removeEpc sent, UUID: ' + uuid);
  }, function () {
    OB.debug('removeEpc ended succesfully: ' + epc);
    if (callback) {
      callback();
    }
  }, function () {
    OB.debug('error while removeEpc: ' + epc);
    if (errorCallback) {
      errorCallback();
    }
  }, 2000, OB.UTIL.get_UUID(), 5);
};

OB.UTIL.RfidController.removeEpcLine = function (line, callback, errorCallback) {
  OB.UTIL.RfidController.waitForAck(function (uuid) {
    OB.UTIL.RfidController.get('rfidWebsocket').send('removeEpcs:' + uuid + ':' + line.get('obposEpccode'));
    OB.debug('removeEpcLine sent UUID: ' + uuid);
  }, function () {
    OB.debug('removeEpcLine ended succesfully: ' + line.get('id'));
    if (callback) {
      callback();
    }
  }, function () {
    OB.debug('error while removeEpcLine: ' + line.get('id'));
    if (errorCallback) {
      errorCallback();
    }
  }, 2000, OB.UTIL.get_UUID(), 5);
};

OB.UTIL.RfidController.updateEpcBuffers = function (callback, errorCallback) {
  OB.UTIL.RfidController.waitForAck(function (uuid) {
    OB.UTIL.RfidController.get('rfidWebsocket').send('updateEpcBuffers:' + uuid);
    OB.debug('updateEpcBuffers sent, UUID: ' + uuid);
  }, function () {
    OB.debug('updateEpcBuffers ended succesfully');
    if (callback) {
      callback();
    }
  }, function () {
    OB.debug('error while updateEpcBuffers');
    if (errorCallback) {
      errorCallback();
    }
  }, 2000, OB.UTIL.get_UUID(), 5);
};

OB.UTIL.RfidController.removeAllEpcs = function (callback, errorCallback) {
  OB.UTIL.RfidController.waitForAck(function (uuid) {
    OB.UTIL.RfidController.get('rfidWebsocket').send('removeAllEpcs:' + uuid);
    OB.debug('removeAllEpcs sent, UUID: ' + uuid);
  }, function () {
    OB.debug('removeAllEpcs ended succesfully');
    if (callback) {
      callback();
    }
  }, function () {
    OB.debug('error while removeAllEpcs');
    if (errorCallback) {
      errorCallback();
    }
  }, 2000, OB.UTIL.get_UUID(), 5);
};

OB.UTIL.RfidController.processRemainingCodes = function (order, callback, errorCallback) {
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
    OB.UTIL.RfidController.waitForAck(function (uuid) {
      OB.UTIL.RfidController.get('rfidWebsocket').send('addEpcs:' + uuid + ':' + epcCodesToAdd.substring(0, epcCodesToAdd.length - 1));
      OB.debug('add:processRemainingCodes sent, UUID: ' + uuid);
    }, function () {
      OB.debug('add processRemainingCodes ended succesfully: ' + epcCodesToAdd);
      if (callback) {
        callback();
      }
    }, function () {
      OB.debug('error while add processRemainingCodes: ' + epcCodesToAdd);
      if (errorCallback) {
        errorCallback();
      }
    }, 2000, OB.UTIL.get_UUID(), 5);
  }
  if (epcCodesToErase) {
    OB.UTIL.RfidController.waitForAck(function (uuid) {
      OB.UTIL.RfidController.get('rfidWebsocket').send('removeEpcs:' + uuid + ':' + epcCodesToErase.substring(0, epcCodesToErase.length - 1));
      OB.debug('remove processRemainingCodes sent, UUID: ' + uuid);
    }, function () {
      OB.debug('remove processRemainingCodes ended succesfully: ' + epcCodesToErase);
      if (callback) {
        callback();
      }
    }, function () {
      OB.debug('error while remove processRemainingCodes: ' + epcCodesToErase);
      if (errorCallback) {
        errorCallback();
      }
    }, 2000, OB.UTIL.get_UUID(), 5);
  }
  //Only if useSecurityGate check is enabled
  if (OB.POS.modelterminal.get('terminal').terminalType.useSecurityGate) {
    OB.UTIL.RfidController.waitForAck(function (uuid) {
      OB.UTIL.RfidController.get('rfidWebsocket').send('send:' + uuid + ':' + JSON.stringify(order));
      OB.debug('sent processRemaingingCodes sent, UUID: ' + uuid);
    }, function () {
      OB.debug('send processRemainingCodes ended succesfully: ' + order.get('id'));
      if (callback) {
        callback();
      }
    }, function () {
      OB.debug('error while send processRemainingCodes: ' + order.get('id'));
      if (errorCallback) {
        errorCallback();
      }
    }, 2000, OB.UTIL.get_UUID(), 5);
  }
};

OB.UTIL.RfidController.connectRFIDDevice = function (callback, errorCallback) {
  OB.UTIL.RfidController.set('isRFIDEnabled', true);
  OB.UTIL.RfidController.set('connected', true);
  OB.UTIL.RfidController.waitForAck(function (uuid) {
    OB.UTIL.RfidController.get('rfidWebsocket').send('connect:' + uuid);
    OB.debug('connectRFIDDevice sent, UUID: ' + uuid);
  }, function () {
    OB.debug('connectRFIDDevice ended succesfully');
    if (callback) {
      callback();
    }
  }, function () {
    OB.UTIL.RfidController.set('connectionLost', true);
    OB.UTIL.RfidController.waitForAck(function (uuid) {
      OB.UTIL.RfidController.get('rfidWebsocket').send('connect:' + uuid);
      OB.debug('connectRFIDDevice sent, UUID: ' + uuid);
    }, function () {
      OB.debug('connectRFIDDevice ended succesfully');
      if (callback) {
        callback();
      }
    }, function () {
      OB.UTIL.RfidController.set('connectionLost', true);
      OB.debug('error while connectRFIDDevice');
      if (errorCallback) {
        errorCallback();
      }
    }, 2000, OB.UTIL.get_UUID());
    OB.debug('error while connectRFIDDevice');
    if (errorCallback) {
      errorCallback();
    }
  }, 2000, OB.UTIL.get_UUID(), 3);
};

OB.UTIL.RfidController.disconnectRFIDDevice = function (callback, errorCallback) {
  OB.UTIL.RfidController.set('isRFIDEnabled', false);
  OB.UTIL.RfidController.set('connected', false);
  OB.UTIL.RfidController.waitForAck(function (uuid) {
    OB.UTIL.RfidController.get('rfidWebsocket').send('disconnect:' + uuid);
    OB.debug('disconnectRFIDDevice sent, UUID: ' + uuid);
  }, function () {
    OB.debug('disconnectRFIDDevice ended succesfully');
    if (callback) {
      callback();
    }
  }, function () {
    OB.UTIL.RfidController.set('connectionLost', true);
    OB.UTIL.RfidController.waitForAck(function (uuid) {
      OB.UTIL.RfidController.get('rfidWebsocket').send('disconnect:' + uuid);
      OB.debug('disconnectRFIDDevice sent, UUID: ' + uuid);
    }, function () {
      OB.debug('disconnectRFIDDevice ended succesfully');
      if (callback) {
        callback();
      }
    }, function () {
      OB.UTIL.RfidController.set('connectionLost', true);
      OB.debug('error while disconnectRFIDDevice');
      if (errorCallback) {
        errorCallback();
      }
    }, 2000, OB.UTIL.get_UUID());
    OB.debug('error while disconnectRFIDDevice');
    if (errorCallback) {
      errorCallback();
    }
  }, 2000, OB.UTIL.get_UUID(), 3);
};

// If trials is undefined the function will try to reach the Hardware Manager forever, this only happens during connect & disconnect
OB.UTIL.RfidController.waitForAck = function (functionToExecute, callback, errorCallback, interval, uuid, trials) {
  var index = OB.UTIL.RfidController.get('rfidAckArray').indexOf(uuid),
      i;
  if (index > -1) {
    OB.UTIL.RfidController.get('rfidAckArray').splice(index, 1);
    for (i = OB.UTIL.RfidController.get('rfidAckArray').length - 1; i >= 0; i--) {
      if (OB.UTIL.RfidController.get('rfidAckArray')[i] === uuid) {
        OB.UTIL.RfidController.get('rfidAckArray').splice(i, 1);
      }
    }
    callback();
  } else if (OB.UTIL.RfidController.get('rfidWebsocket').readyState !== 1) {
    setTimeout(function () {
      OB.UTIL.RfidController.waitForAck(functionToExecute, callback, errorCallback, interval, uuid, trials - 1);
    }, interval);
  } else if (!trials || trials > 0) {
    functionToExecute(uuid);
    setTimeout(function () {
      OB.UTIL.RfidController.waitForAck(functionToExecute, callback, errorCallback, interval, uuid, trials - 1);
    }, interval);
  } else {
    errorCallback();
  }
};

OB.UTIL.RfidController.sendTestEpc = function (epc, callback, errorCallback) {
  if (OB.UTIL.RfidController.get('isRFIDEnabled')) {
    OB.UTIL.RfidController.waitForAck(function (uuid) {
      OB.UTIL.RfidController.get('rfidWebsocket').send('test:' + uuid + ':' + epc);
      OB.debug('sendTestEpc sent, UUID: ' + uuid);
    }, function () {
      OB.debug('sendTestEpc ended succesfully:' + epc);
      if (callback) {
        callback();
      }
    }, function () {
      OB.debug('error while sendTestEpc:' + epc);
      if (errorCallback) {
        errorCallback();
      }
    }, 2000, OB.UTIL.get_UUID(), 5);
  }
};

OB.UTIL.RfidController.on('change:isRFIDEnabled', function (model) {
  OB.MobileApp.view.originalRFIDMode = OB.UTIL.RfidController.get('isRFIDEnabled');
}, this);