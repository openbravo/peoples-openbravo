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
      barcodeActionHandler.findProductByBarcode(message.uPCEAN, function (product) {
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